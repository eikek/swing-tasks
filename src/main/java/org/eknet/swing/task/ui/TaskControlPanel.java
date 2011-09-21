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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jetbrains.annotations.NotNull;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.State;
import org.eknet.swing.task.TaskContext;
import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskListener;
import org.eknet.swing.task.impl.Util;

/**
 * Displays {@link org.eknet.swing.task.TaskControl} properties.
 * 
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.07.11 13:36
 */
public class TaskControlPanel extends JPanel {

  private final Listener listener = new Listener();
  private final Timer labelTimer = new Timer(1000, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      taskLabel.setText(getTaskName(taskControl.getContext()));
    }
  });
  
  private TaskControl taskControl;

  private JProgressBar progressBar;
  private JLabel taskLabel;
  private JLabel phaseLabel;
  private CancelAction cancelAction;

  private Icon cancelIcon = new ImageIcon(TaskControlPanel.class.getResource("cancel.png"));

  public TaskControlPanel(TaskControl taskControl) {
    this();
    setTaskControl(taskControl);
  }

  public TaskControlPanel() {
    super(true);
    initComponents();
  }

  protected void initComponents() {
    BoxLayout vert = new BoxLayout(this, BoxLayout.Y_AXIS);
    setLayout(vert);

    progressBar = new JProgressBar(0, 100);
    progressBar.setStringPainted(true);
    progressBar.setValue(0);
    progressBar.setIndeterminate(true);
    taskLabel = new JLabel();
    taskLabel.setHorizontalAlignment(SwingConstants.LEFT);
    phaseLabel = new JLabel();
    phaseLabel.setHorizontalAlignment(SwingConstants.LEFT);
    cancelAction = new CancelAction("", getCancelIcon());
    cancelAction.setEnabled(taskControl != null && taskControl.getContext().getState() == State.STARTED);
    JButton cancelButton = new JButton(cancelAction);

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
    panel.add(taskLabel);
    add(panel);

    panel = new JPanel(new BorderLayout(5, 5));
    panel.add(progressBar, BorderLayout.CENTER);
    panel.add(cancelButton, BorderLayout.EAST);
    add(panel);

    panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
    panel.add(phaseLabel);
    add(panel);
  }

  private void initValues() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        phaseLabel.setText(getPhase());
        listener.updateState(getState(), getTaskName(taskControl.getContext()));
      }
      private String getPhase() {
        return taskControl.getContext().getPhase();
      }
      private State getState() {
        return taskControl.getContext().getState();
      }
    });
  }

  public synchronized void setTaskControl(@NotNull TaskControl control) {
    if (this.taskControl != null) {
      this.taskControl.getContext().removeListener(listener);
    }
    this.taskControl = control;
    this.taskControl.getContext().addListener(listener);
    initValues();
  }

  public Icon getCancelIcon() {
    return cancelIcon;
  }

  public void setCancelIcon(Icon cancelIcon) {
    this.cancelIcon = cancelIcon;
    if (this.cancelAction != null) {
      this.cancelAction.putValue(Action.SMALL_ICON, cancelIcon);
    }
  }

  private String getTaskName(TaskContext context) {
    Long dur = context.getDuration();
    return context.getContextId() + ") " + formatDuration(dur) + " " + context.getTask().getId();
  }

  protected String formatDuration(Long duration) {
    return Util.formatDuration(duration);
  }
  
  private class Listener implements TaskListener {
    @Override
    public void stateChanged(@NotNull ChangeEvent<State> event) {
      State state = event.getNewValue();
      updateState(state, getTaskName(event.getSource()));
    }

    public void updateState(State state, String taskId) {
      if (state != null) {
        switch (state) {
          case STARTED:
            taskLabel.setText(taskId + " started.");
            progressBar.setIndeterminate(true);
            cancelAction.setEnabled(true);
            labelTimer.start();
            break;
          case DONE:
            taskLabel.setText(taskId + " finished.");
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            cancelAction.setEnabled(false);
            labelTimer.stop();
            break;
          case CANCELLED:
            progressBar.setIndeterminate(false);
            taskLabel.setText(taskId + " cancelled.");
            cancelAction.setEnabled(false);
            labelTimer.stop();
            break;
          case FAILED:
            progressBar.setIndeterminate(false);
            taskLabel.setText(taskId + " failed");
            cancelAction.setEnabled(false);
            labelTimer.stop();
          case PENDING:
            taskLabel.setText(taskId + " pending.");
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            cancelAction.setEnabled(true);
            break;
        }
      }
    }

    @Override
    public void progressChanged(@NotNull ChangeEvent<Integer> event) {
      Integer value = event.getNewValue();
      if (value != null) {
        if (progressBar.isIndeterminate()) {
          progressBar.setIndeterminate(false);
        }
        progressBar.setValue(value);
      }
    }

    @Override
    public void phaseChanged(@NotNull ChangeEvent<String> event) {
      String phase = event.getNewValue();
      if (phase != null) {
        phaseLabel.setText(phase);
      }
    }
  }

  private class CancelAction extends AbstractAction {

    private CancelAction(String name, Icon icon) {
      super(name, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (taskControl != null) {
        taskControl.cancel();
      }
    }
    
  }
}
