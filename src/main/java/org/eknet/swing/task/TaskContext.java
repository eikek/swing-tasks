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

package org.eknet.swing.task;

/**
 * Represents the context of a running task.
 * 
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 08:34
 */
public interface TaskContext {

  /**
   * The context id is generated on each task execution and
   * is (at least) unique among all executions in this application
   * run.
   *
   * @return the context id, never null
   */
  String getContextId();

  /**
   *
   * @return The task to this context, never null
   */
  Task getTask();

  /**
   *
   * @return the timestamp at which the task was started or null
   */
  Long getStartedTimestamp();

  /**
   * The timestamp at which the task finished, or null
   * @return
   */
  Long getFinishTimestamp();

  /**
   *
   * @return the tasks current state, never null
   */
  State getState();

  int getProgress();

  /**
   * Returns the tasks current phase, or null
   * @return
   */
  String getPhase();

  /**
   *
   * @return the duration this task is running or null (if not started)
   */
  Long getDuration();

  /**
   * Adds listener that receives events for this execution only. Use
   * {@link TaskListenerSupport} to add more global listeners.
   * 
   * @param listener
   */
  void addListener(TaskListener listener);
  void removeListener(TaskListener listener);
}
