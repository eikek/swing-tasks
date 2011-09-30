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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.jetbrains.annotations.NotNull;

import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.TaskPredicates;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.07.11 13:32
 */
public class TaskGlassPane extends JPanel {

  private Color color1;
  private Color color2;

  private JPanel container;
  private TaskList taskList;

  private final TaskManager taskManager;

  public TaskGlassPane(@NotNull TaskManager taskManager) {
    this.color1 = new Color(0, 0, 0, 175);
    this.color2 = new Color(0, 0, 0, 120);
    this.taskManager = taskManager;
    initComponents();
  }


  protected void initComponents() {
    container = new JPanel(new BorderLayout());
    this.setVisible(false);
    add(container, BorderLayout.CENTER);

    container.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    container.setVisible(false);

    taskList = new TaskList(taskManager);
    taskList.setMinimumSize(new Dimension(400, 80));
    taskList.setFilter(TaskPredicates.blockingTasks);
    taskList.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("task") && container != null) {
          container.setVisible(taskList.getTaskCount() > 0);
        }
      }
    });

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.CENTER;
    gbl.setConstraints(this, gbc);
    this.setLayout(gbl);
    container.add(taskList);
  }

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    if (taskList != null) {
      if (aFlag) {
        taskList.start();
      } else {
        taskList.stop();
      }
    }
  }

  public void visible(boolean flag) {
    super.setVisible(flag);
  }

  public TaskManager getTaskManager() {
    return taskManager;
  }

  /**
   * Returns the component that contains the taskList component.
   *
   * @return
   */
  public JPanel getContainer() {
    return container;
  }

  public TaskList getTaskList() {
    return taskList;
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (!isOpaque()) {
      super.paintComponent(g);
      return;
    }

    Graphics2D g2d = (Graphics2D) g;
    GradientPaint fill = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
    g2d.setPaint(fill);
    g2d.fillRect(0, 0, getWidth(), getHeight());

    setOpaque(false);
    super.paintComponent(g);
    setOpaque(true);

  }

}