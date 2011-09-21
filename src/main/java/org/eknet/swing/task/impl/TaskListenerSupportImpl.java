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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.NotNull;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.State;
import org.eknet.swing.task.Task;
import org.eknet.swing.task.TaskListener;
import org.eknet.swing.task.TaskListenerSupport;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 21:30
 */
public class TaskListenerSupportImpl implements TaskListenerSupport {
  private final static Logger log = LoggerFactory.getLogger(TaskListenerSupportImpl.class);

  private final LoggingExceptionHandler<TaskListener> loggingExceptionHandler = new LoggingExceptionHandler<TaskListener>(log);
  private final Map<String, EventEmitter<TaskListener>> taskListeners = new HashMap<String, EventEmitter<TaskListener>>();
  private final EventEmitter<TaskListener> globalListeners = EventEmitter.newEmitter(TaskListener.class, loggingExceptionHandler);

  @Override
  public void addListener(TaskListener listener) {
    if (listener != null) {
      globalListeners.addListener(listener);
    }
  }

  @Override
  public void removeListener(TaskListener listener) {
    if (listener != null) {
      globalListeners.removeListener(listener);
    }
  }

  @Override
  public void addListener(@NotNull String taskId, TaskListener listener) {
    if (listener != null) {
      EventEmitter<TaskListener> emitter = getTaskListener(taskId);
      emitter.addListener(listener);
    }
  }

  public EventEmitter<TaskListener> getTaskListener(String taskId) {
    EventEmitter<TaskListener> emitter;
    synchronized (taskListeners) {
      emitter = taskListeners.get(taskId);
    }
    if (emitter == null) {
      emitter = EventEmitter.newEmitter(TaskListener.class, loggingExceptionHandler);
      synchronized (taskListeners) {
        taskListeners.put(taskId, emitter);
      }
    }
    return emitter;
  }

  @Override
  public void removeListener(@NotNull String taskId, TaskListener listener) {
    if (listener != null) {
      EventEmitter<TaskListener> emitter = getTaskListener(taskId);
      emitter.removeListener(listener);
    }
  }

  public EventEmitter<TaskListener> getGlobalListeners() {
    return globalListeners;
  }


  public void fireStateChanged(@NotNull ChangeEvent<State> event) {
    Task task = event.getSource().getTask();
    getTaskListener(task.getId()).emitter().stateChanged(event);
    globalListeners.emitter().stateChanged(event);
  }

  public void fireProgressChanged(@NotNull ChangeEvent<Integer> event) {
    Task task = event.getSource().getTask();
    getTaskListener(task.getId()).emitter().progressChanged(event);
    globalListeners.emitter().progressChanged(event);
  }

  public void firePhaseChanged(@NotNull ChangeEvent<String> event) {
    Task task = event.getSource().getTask();
    getTaskListener(task.getId()).emitter().phaseChanged(event);
    globalListeners.emitter().phaseChanged(event);
  }
}
