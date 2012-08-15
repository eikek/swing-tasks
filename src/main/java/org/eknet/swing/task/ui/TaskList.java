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

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.State;
import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskListenerAdapter;
import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.TaskPredicate;
import org.eknet.swing.task.TaskPredicates;
import org.eknet.swing.task.impl.Util;

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

  public TaskList(/*@NotNull*/ TaskManager taskManager) {
    super(new BorderLayout());
    Util.checkNotNullArgument(taskManager);
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

  public synchronized int getTaskCount() {
    return controls.size();
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

  protected TaskControlPanel newTaskControlPanel(TaskControl control) {
    TaskControlPanel panel = new TaskControlPanel(control);
    panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5))));
    return panel;
  }

  public synchronized void addTask(/*@NotNull*/ TaskControl taskControl) {
    Util.checkNotNullArgument(taskControl);
    String contextId = taskControl.getContext().getContextId();
    if (!controls.containsKey(contextId)) {
      TaskControlPanel panel = newTaskControlPanel(taskControl);
      controls.put(contextId, panel);
      addComponent(panel);
    }
  }

  public synchronized void removeTask(/*@NotNull*/ final String contextId) {
    Util.checkNotNullArgument(contextId);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        TaskControlPanel panel = controls.get(contextId);
        if (panel != null) {
          removeComponent(panel);
        }
        controls.remove(contextId);
      }
    });
  }

  private class ControlListener extends TaskListenerAdapter {
    @Override
    public void stateChanged(/*@NotNull*/ ChangeEvent<State> event) {
      Util.checkNotNullArgument(event);
      State state = event.getNewValue();
      String contextId = event.getSource().getContextId();
      if (state != null) {
        TaskControl tc = taskManager.getTask(contextId);
        if (tc != null && state == State.PENDING && filter.apply(tc)) {
          addTask(tc);
        }
        if (state.isFinalState()) {
          removeTask(contextId);
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
