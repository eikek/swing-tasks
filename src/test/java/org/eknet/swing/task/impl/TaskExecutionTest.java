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

import org.testng.annotations.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.State;
import org.eknet.swing.task.Task;
import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskListener;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 19:16
 */
public class TaskExecutionTest {
  private final static Logger log = LoggerFactory.getLogger(TaskExecutionTest.class);
  private final TaskManagerImpl manager = new TaskManagerImpl();

  @Test(enabled = false)
  public void testExceptionTask() throws Exception {
    Task<Long, Long> task = new LongTask();
    TaskControl<Long> control = manager.create(task);
    control.getContext().addListener(new TaskListener() {
      @Override
      public void stateChanged(ChangeEvent<State> event) {
        log.info(">>> State: " + event.getOldValue() + " => " + event.getNewValue());
        log.info("Started: " + event.getSource().getStartedTimestamp());
      }

      @Override
      public void progressChanged(ChangeEvent<Integer> event) {
        log.info(">>> Progress: " + event.getOldValue() + " => " + event.getNewValue());
      }

      @Override
      public void phaseChanged(ChangeEvent<String> event) {
        log.info(">>> Phase: " + event.getOldValue() + " => " + event.getNewValue());
      }
    });
    Long value = control.waitFor();
    log.info("Waited for task: " + value);
  }
}
