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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.Mode;
import org.eknet.swing.task.State;
import org.eknet.swing.task.Task;
import org.eknet.swing.task.TaskContext;
import org.eknet.swing.task.TaskListener;


/**
 * Wraps the execution of a {@link Task} which is governed by a {@link TaskWorker}. This context holds
 * adds listener support. It registers itself as {@link java.beans.PropertyChangeListener} to the {@link TaskWorker}
 * object and translates the {@link javax.swing.SwingWorker} events to {@link org.eknet.swing.task.TaskEvent}s.
 * <p/>
 * Each execution can be identified by a growing id. For retrieving task execution properties it delegates
 * to the wrapped {@link TaskWorker}.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 21:40
 */
public class TaskContextImpl implements TaskContext, PropertyChangeListener {
  private final static Logger log = LoggerFactory.getLogger(TaskContextImpl.class);
  private final static AtomicLong counter = new AtomicLong(0);

  private final String contextId = String.valueOf(counter.getAndIncrement());

  private final TaskWorker worker;
  private final TaskListenerSupportImpl taskListenerSupport;
  private final EventEmitter<TaskListener> localListeners = EventEmitter
          .newEmitter(TaskListener.class, new LoggingExceptionHandler<TaskListener>(log));

  public TaskContextImpl(/*@NotNull*/ TaskWorker worker, /*@NotNull*/ TaskListenerSupportImpl taskListenerSupport) {
    Util.checkNotNullArgument(worker);
    Util.checkNotNullArgument(taskListenerSupport);
    this.worker = worker;
    this.worker.setContext(this);
    this.taskListenerSupport = taskListenerSupport;
    this.worker.addPropertyChangeListener(this);
  }

  /*@NotNull*/
  @Override
  public String getContextId() {
    return contextId;
  }

  @Override
  public Long getStartedTimestamp() {
    return worker.getStartedTimestamp();
  }

  @Override
  public Long getFinishTimestamp() {
    return worker.getFinishTimestamp();
  }

  /*@NotNull*/
  @Override
  public State getState() {
    if (worker.isCancelled()) {
      return State.CANCELLED;
    }
    return toState(worker.getState());
  }

  @Override
  public int getProgress() {
    return worker.getProgress();
  }

  @Override
  public String getPhase() {
    return worker.getPhase();
  }

  public Long getDuration() {
    Long start = getStartedTimestamp();
    if (start != null) {
      Long finish = getFinishTimestamp();
      long end = finish != null ? finish : System.currentTimeMillis();
      return end - start;
    }
    return null;
  }

  @Override
  public void addListener(TaskListener listener) {
    if (listener != null) {
      localListeners.addListener(listener);
    }
  }

  @Override
  public void removeListener(TaskListener listener) {
    if (listener != null) {
      localListeners.addListener(listener);
    }
  }

  TaskWorker getWorker() {
    return worker;
  }

  /*@NotNull*/
  @Override
  public Task getTask() {
    return worker.getTask();
  }

  public void fireStateChangeEvent(/*@Nullable*/ State oldValue, /*@Nullable*/ State newValue) {
    ChangeEvent<State> e = new ChangeEventImpl<State>(oldValue, newValue, this);
    taskListenerSupport.fireStateChanged(e);
    localListeners.emitter().stateChanged(e);
  }

  public void fireProgressChangeEvent(/*@Nullable*/ Integer oldValue, /*@Nullable*/ Integer newValue) {
    ChangeEvent<Integer> e = new ChangeEventImpl<Integer>(oldValue, newValue, this);
    taskListenerSupport.fireProgressChanged(e);
    localListeners.emitter().progressChanged(e);
  }

  public void firePhaseChangeEvent(/*@Nullable*/ String oldValue, /*@Nullable*/ String newValue) {
    ChangeEvent<String> e = new ChangeEventImpl<String>(oldValue, newValue, this);
    taskListenerSupport.firePhaseChanged(e);
    localListeners.emitter().phaseChanged(e);
  }
  // ~~ PropertyChangeListener

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (getTask().getMode() == Mode.SILENT) {
      return;
    }
    if (evt.getPropertyName().equals("state")) {
      SwingWorker.StateValue nv = (SwingWorker.StateValue) evt.getNewValue();
      if (nv == SwingWorker.StateValue.DONE) {
        if (worker.isError()) {
          fireStateChangeEvent(toState(evt.getOldValue()), State.FAILED);
        } else if (worker.isCancelled()) {
          fireStateChangeEvent(toState(evt.getOldValue()), State.CANCELLED);
        } else {
          fireStateChangeEvent(toState(evt.getOldValue()), toState(evt.getNewValue()));
        }
      } else {
        fireStateChangeEvent(toState(evt.getOldValue()), toState(evt.getNewValue()));
      }
    }
    if (evt.getPropertyName().equals("progress")) {
      fireProgressChangeEvent((Integer) evt.getOldValue(), (Integer) evt.getNewValue());
    }
    if (evt.getPropertyName().equals("phase")) {
      firePhaseChangeEvent((String) evt.getOldValue(), (String) evt.getNewValue());
    }
  }

  private State toState(Object value) {
    if (value == State.CANCELLED) {
      return (State) value;
    }
    switch ((SwingWorker.StateValue) value) {
      case DONE:
        return State.DONE;
      case PENDING:
        return State.PENDING;
      case STARTED:
        return State.STARTED;
    }
    throw new Error("unknown state: " + value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TaskContextImpl that = (TaskContextImpl) o;

    if (contextId != null ? !contextId.equals(that.contextId) : that.contextId != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return contextId != null ? contextId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "TaskContextImpl{" +
            "contextId='" + contextId + '\'' +
            '}';
  }
}
