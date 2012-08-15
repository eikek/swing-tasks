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

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eknet.swing.task.State;
import org.eknet.swing.task.TaskContext;
import org.eknet.swing.task.TaskControl;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 21:22
 */
public class TaskControlImpl<V> implements TaskControl<V> {
  private final static Logger log = LoggerFactory.getLogger(TaskControlImpl.class);
  private final TaskContextImpl taskContext;

  public TaskControlImpl(/*@NotNull*/ TaskContextImpl taskContext) {
    Util.checkNotNullArgument(taskContext);
    this.taskContext = taskContext;
  }

  @Override
  public V waitFor() {
    TaskWorker<V, ?> worker = getWorker();
    if (taskContext.getState() == State.PENDING) {
      worker.execute();
    }
    try {
      return worker.get();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Error executing task!", e);
    }
  }

  @SuppressWarnings({"unchecked"})
  private TaskWorker<V, ?> getWorker() {
    return taskContext.getWorker();
  }

  @Override
  public void execute() {
    getWorker().execute();
  }

  @Override
  public void cancel() {
    log.info("About to cancel task: " + getContext().getContextId());
    getWorker().cancel(true);
  }

  /*@NotNull*/
  @Override
  public TaskContext getContext() {
    return taskContext;
  }

}
