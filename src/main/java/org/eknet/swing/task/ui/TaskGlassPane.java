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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.eknet.swing.task.Mode;
import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.TaskPredicates;

/**
 * A simple panel that shows a list of current {@link Mode#BLOCKING} tasks.
 * <p/>
 * It draws a semi translucent black background and a centered task control
 * list. This can be used as a glass pane.
 * 
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.07.11 13:32
 */
public class TaskGlassPane extends JPanel {

  private Color color1;
  private Color color2;

  private TaskList taskList;

  /*@Nullable*/
  private final TaskManager taskManager;

  public TaskGlassPane(/*@Nullable*/ TaskManager taskManager) {
    this.color1 = new Color(0, 0, 0, 175);
    this.color2 = new Color(0, 0, 0, 120);
    this.taskManager = taskManager;
    initComponents();
  }


  protected void initComponents() {
    if (getTaskManager() != null) {
      taskList = new TaskList(getTaskManager());
      taskList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      taskList.setFilter(TaskPredicates.blockingTasks);

      GridBagLayout gbl = new GridBagLayout();
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.ipadx = 200;
      gbc.ipady = 60;
      this.setLayout(gbl);

      this.add(taskList, gbc);
    }
    this.setVisible(false);
  }

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    if (getTaskList() != null) {
      if (aFlag) {
        getTaskList().start();
      } else {
        getTaskList().stop();
      }
    }
  }

  public void visible(boolean flag) {
    super.setVisible(flag);
  }

  /*@Nullable*/
  public TaskManager getTaskManager() {
    return taskManager;
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