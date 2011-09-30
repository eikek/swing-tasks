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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.Mode;
import org.eknet.swing.task.State;
import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskListenerAdapter;
import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.TaskPredicate;
import org.eknet.swing.task.TaskPredicates;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.07.11 17:04
 */
public class TaskList extends JPanel {

  private final TaskManager taskManager;

  private final Map<String, TaskControlPanel> controls = new HashMap<String, TaskControlPanel>();
  private final ControlListener listener = new ControlListener();

  private JScrollPane scrollPane;
  private Container container;

  private TaskPredicate filter = TaskPredicates.backgroundTasks;

  private volatile boolean running = false;

  public TaskList(@NotNull TaskManager taskManager) {
    super(new BorderLayout());
    this.taskManager = taskManager;
    initComponent();
  }

  protected void initComponent() {
    setScrollPane(new JScrollPane());

    JPanel container = new ConatainerComponent();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    container.add(Box.createVerticalGlue());
    setContainer(container);
  }

  public synchronized void start() {
    if (!running) {
      //populate with running tasks
      for (TaskControl ctrl : taskManager.getTasks(filter)) {
        addTask(ctrl);
      }
      //add listener to pickup
      taskManager.getTaskListenerSupport().addListener(listener);
      running = true;
    }
  }

  public TaskPredicate getFilter() {
    return filter;
  }

  public void setFilter(TaskPredicate filter) {
    this.filter = filter;
  }

  public int getTaskCount() {
    synchronized (controls) {
      return controls.size();
    }
  }

  public void setContainer(Container container) {
    if (this.container != null) {
      this.container.removeAll();
    }
    if (this.scrollPane != null && this.container != null) {
      this.scrollPane.getViewport().remove(this.container);
    }
    this.container = container;
    if (this.scrollPane != null && this.container != null) {
      this.scrollPane.setViewportView(this.container);
    }
  }

  public void setScrollPane(JScrollPane scrollPane) {
    if (this.scrollPane != null) {
      this.scrollPane.getViewport().removeAll();
      remove(this.scrollPane);
    }
    this.scrollPane = scrollPane;
    if (this.scrollPane != null && this.container != null) {
      this.scrollPane.setViewportView(this.container);
    }
    if (this.scrollPane != null) {
      add(scrollPane, BorderLayout.CENTER);
    }
  }
  
  public Container getContainer() {
    return container;
  }

  public void addComponent(Component component) {
    if (component != null && this.container != null) {
      this.container.add(component, 0);
      if (this.container.isVisible()) {
        this.container.repaint();
      }
    }
  }

  public void removeComponent(Component component) {
    if (component != null && this.container != null) {
      this.container.remove(component);
      if (this.container.isVisible()) {
        this.container.repaint();
      }
    }
  }

  public synchronized void stop() {
    if (running) {
      taskManager.getTaskListenerSupport().removeListener(listener);
      synchronized (controls) {
        controls.clear();
      }
      getContainer().removeAll();
      running = false;
    }
  }

  public void addTask(@NotNull TaskControl taskControl) {
    TaskControlPanel panel = null;
    synchronized (controls) {
      if (!controls.containsKey(taskControl.getContext().getContextId())) {
        panel = new TaskControlPanel(taskControl);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5))));
        controls.put(taskControl.getContext().getContextId(), panel);
      }
    }
    if (panel != null) {
      onEDT(new AddRunnable(panel));
    }
  }

  public void removeTask(@NotNull String contextId) {
    synchronized (controls) {
      controls.remove(contextId);
    }
    onEDT(new RemoveRunnable(contextId));
  }

  private void onEDT(@NotNull Runnable run) {
    if (SwingUtilities.isEventDispatchThread()) {
      run.run();
    } else {
      SwingUtilities.invokeLater(run);
    }
  }

  private class RemoveRunnable implements Runnable {
    private final String contextId;

    private RemoveRunnable(String contextId) {
      this.contextId = contextId;
    }

    @Override
    public void run() {
      removeComponent(controls.get(contextId));
    }
  }


  private class AddRunnable implements Runnable {
    private final TaskControlPanel taskControl;

    private AddRunnable(@NotNull TaskControlPanel taskControl) {
      this.taskControl = taskControl;
    }

    @Override
    public void run() {
      addComponent(taskControl);
    }
  }

  private class ControlListener extends TaskListenerAdapter {
    @Override
    public void stateChanged(@NotNull ChangeEvent<State> event) {
      State state = event.getNewValue();
      String contextId = event.getSource().getContextId();
      if (state != null) {
        TaskControl tc = taskManager.getTask(contextId);
        if (tc != null && state == State.PENDING && filter.apply(tc)) {
          addTask(tc);
          TaskList.this.firePropertyChange("task", null, tc);
        }
        if (state.isFinalState()) {
          removeTask(contextId);
          TaskList.this.firePropertyChange("task", tc, null);
        }
      }
    }
  }

  private class ConatainerComponent extends JPanel implements Scrollable {
    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 8;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 4 * getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }
}
