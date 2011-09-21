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

package org.eknet.swing.task.impl;

import javax.swing.JFrame;

import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.ui.TaskBar;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 21:54
 */
public class TestTaskInfoBar {

  static TaskManager manager = new TaskManagerImpl();

  public static void main(String[] args) throws InterruptedException {
    JFrame frame = new JFrame("Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.getContentPane().add(new TaskBar(manager));

//    fram.setSize(new Dimension(400, 400));
    frame.pack();
    frame.setVisible(true);

    manager.create(new LongTask()).execute();
    Thread.sleep(6000);

    manager.create(new LongTask()).execute();
    Thread.sleep(2000);
    manager.create(new LongTask()).execute();
  }
}
