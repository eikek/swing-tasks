/*
 * Copyright 2011 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.swing.task.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.Mode;
import org.eknet.swing.task.State;
import org.eknet.swing.task.TaskEvent;
import org.eknet.swing.task.TaskListener;
import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.TaskPredicate;
import org.eknet.swing.task.TaskPredicates;
import org.eknet.swing.task.impl.Util;

/**
 * A component designed to be placed in a status bar at the bottom of the application.
 * <p/>
 * It shows a overall view of all running tasks. On click, a dialog opens to show a more
 * detailed view of running tasks with the ability to cancel them.
 * 
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 22:53
 */
public class TaskBar extends JPanel {

  private JProgressBar progressBar;
  private JLabel taskLabel;
  private TaskList taskList;
  private JDialog dialog;

  private final TaskManager taskManager;

  private final TaskListener updateListener = new UpdateListener();
  private final MouseListener taskListActivator = new TaskListMouseListener();

  private TaskPredicate filter = TaskPredicates.backgroundTasks;
  
  public TaskBar(@NotNull TaskManager taskManager) {
    super(new BorderLayout(10, 10), true);
    this.taskManager = taskManager;

    progressBar = new JProgressBar(JProgressBar.CENTER);
    progressBar.setMinimumSize(new Dimension(40, progressBar.getPreferredSize().height));
    taskLabel = new JLabel();
    taskLabel.setMinimumSize(new Dimension(25, taskLabel.getPreferredSize().height));
    taskLabel.setHorizontalAlignment(SwingConstants.CENTER);
    taskList = new TaskList(taskManager);
    taskList.setFilter(filter);

    add(progressBar, BorderLayout.CENTER);
    add(taskLabel, BorderLayout.WEST);

    progressBar.addMouseListener(taskListActivator);
    taskLabel.addMouseListener(taskListActivator);

    taskManager.getTaskListenerSupport().addListener(updateListener);
  }

  public TaskPredicate getFilter() {
    return filter;
  }

  public void setFilter(TaskPredicate filter) {
    this.filter = filter;
  }

  private class TaskListMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      if (dialog == null) {
        final Window window = Util.findComponent(TaskBar.this, Window.class);
        dialog = new JDialog(window) {
          @Override
          public void setVisible(boolean b) {
            super.setVisible(b);
            if (!this.isVisible() && b) {
              taskList.start();
            } else if (this.isVisible() && !b) {
              taskList.stop();
            }
          }
        };
        dialog.setTitle("Backgroud Tasks");
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(taskList);
        dialog.setSize(new Dimension(300, 200));
        dialog.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            taskList.stop();
          }
        });
        Util.placeDialog(window, dialog);
        taskList.start();
      }
      dialog.setVisible(!dialog.isVisible());
    }
  }


  private class UpdateListener implements TaskListener {
    private String currentProgress;

    private void updateLabel(@Nullable Integer backgroundTasks, @Nullable Integer pendingTasks, @Nullable String phase) {
      int back = backgroundTasks == null ? getBackgroundTaskCount() : backgroundTasks;
      int pend = pendingTasks == null ? getPendingTasks() : pendingTasks;

      if (phase != null && phase.length() > 25) {
        phase = phase.substring(0, 22) + " ...";
      }
      if (pend == 0 && back == 0) {
        taskLabel.setText(null);
      }
      if (pend == 0 && back == 1 && phase != null) {
        taskLabel.setText(phase);
      } else if (pend == 0 && back > 0) {
        taskLabel.setText(back + " tasks running");
      }
      if (pend > 0 && back > 0) {
        taskLabel.setText(back + " tasks running; " + pend + " pending");
      }
    }

    @Override
    public void stateChanged(@NotNull ChangeEvent<State> event) {
      if (!isBackground(event)) {
        return;
      }
      if (event.getNewValue() != null) {
        State state = event.getNewValue();
        int count = getBackgroundTaskCount();
        updateLabel(count, null, event.getSource().getPhase());

        if (count > 0 && state == State.STARTED) {
          //new task came in
          if (currentProgress == null) {
            currentProgress = event.getSource().getContextId();
          }
          progressBar.setIndeterminate(true);
        }
        //noinspection ConstantConditions
        if (count > 0 && state.isFinalState()) {
          //some task finished, more to do
          currentProgress = null;
          progressBar.setIndeterminate(true);
        }
        //noinspection ConstantConditions
        if (count == 0 && state.isFinalState()) {
          //last task finished
          currentProgress = null;
          progressBar.setValue(0);
          progressBar.setIndeterminate(false);
          progressBar.setToolTipText(null);
        }
      }
    }

    @Override
    public void progressChanged(@NotNull ChangeEvent<Integer> event) {
      if (!isBackground(event)) {
        return;
      }
      String id = event.getSource().getContextId();
      Integer value = event.getNewValue();
      if (currentProgress == null) {
        currentProgress = id;
      }
      if (currentProgress.equals(id) && value != null) {
        if (progressBar.isIndeterminate()) {
          progressBar.setIndeterminate(false);
        }
        progressBar.setValue(value);
      }
    }

    @Override
    public void phaseChanged(@NotNull ChangeEvent<String> event) {
      if (!isBackground(event)) {
        return;
      }
      String phase = event.getNewValue();
      if (phase != null) {
        updateLabel(null, null, phase);
      }
    }

    private boolean isBackground(TaskEvent event) {
      return event.getSource().getTask().getMode() == Mode.BACKGROUND;
    }

    private int getBackgroundTaskCount() {
      return Util.size(taskManager.getTasks(TaskPredicates.backgroundTasks));
    }

    private int getPendingTasks() {
      return Util.size(taskManager.getTasks(TaskPredicates.pendingTasks));
    }
  }
}
