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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.Mode;
import org.eknet.swing.task.State;
import org.eknet.swing.task.Task;
import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskListenerAdapter;
import org.eknet.swing.task.TaskListenerSupport;
import org.eknet.swing.task.TaskManager;
import org.eknet.swing.task.TaskPredicate;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 18:36
 */
public class TaskManagerImpl implements TaskManager {

  private final static Logger log = LoggerFactory.getLogger(TaskManagerImpl.class);

  private final TaskListenerSupportImpl taskListenerSupport = new TaskListenerSupportImpl();
  private final Map<String, TaskControl> tasks = new ConcurrentHashMap<String, TaskControl>();

  private final Blocker blocker = new Blocker();

  private final ExecutorService executorService;

  private AtomicInteger blockingCounter = new AtomicInteger(0);

  private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
    private final ThreadFactory defaultTf = Executors.defaultThreadFactory();

    @Override
    public Thread newThread(Runnable r) {
      Thread t = defaultTf.newThread(r);
      t.setName("SwingTask-" + t.getName());
      t.setDaemon(true);
      return t;
    }
  };

  private static ExecutorService createExecutorService() {
    return new ThreadPoolExecutor(2, 10,
            20L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), THREAD_FACTORY);
  }

  public TaskManagerImpl() {
    this(createExecutorService());
  }

  /**
   * Creates a new task manager.
   * <p/>
   * The parameter {@code executorService} is only used when submitting {@link Runnable}s
   * or {@link Callable}s. If {@link Task}s are submitted, they are invoked on behalf of
   * the {@link javax.swing.SwingWorker} class that is using its own {@link ExecutorService}.
   *
   * @param executorService
   */
  public TaskManagerImpl(/*NotNull*/ ExecutorService executorService) {
    Util.checkNotNullArgument(executorService);
    this.executorService = executorService;
    taskListenerSupport.addListener(new TaskListenerAdapter() {
      @Override
      public void stateChanged(/*@NotNull*/ ChangeEvent<State> event) {
        Util.checkNotNullArgument(event);
        final State newState = event.getNewValue();
        final String contextId = event.getSource().getContextId();
        final Task task = event.getSource().getTask();
        if (newState != null) {
          if (newState.isFinalState()) {
            tasks.remove(contextId);
            if (task.getMode() == Mode.BLOCKING) {
              if (blockingCounter.decrementAndGet() == 0) {
                blocker.unblock(task.getComponent());
              }
            }
          }
          if (newState == State.STARTED) {
            if (task.getMode() == Mode.BLOCKING) {
              blockingCounter.incrementAndGet();
              blocker.block(task.getComponent());
            }
          }
        }
      }
    });
  }

  @Override
  public <V, C> TaskControl<V> create(/*@NotNull*/ Task<V, C> task) {
    Util.checkNotNullArgument(task);
    TaskContextImpl context = new TaskContextImpl(new TaskWorker<V, C>(task), taskListenerSupport);
    TaskControlImpl<V> control = new TaskControlImpl<V>(context);
    tasks.put(control.getContext().getContextId(), control);
    context.fireStateChangeEvent(null, State.PENDING); //must be after the task-control has been added to the map; so listeners can access it
    return control;
  }

  @Override
  public TaskListenerSupport getTaskListenerSupport() {
    return taskListenerSupport;
  }

  @Override
  public Iterable<TaskControl> getTasks(/*@NotNull*/ TaskPredicate predicate) {
    Util.checkNotNullArgument(predicate);
    return TaskIterable.filter(tasks.values(), predicate);
  }

  @Override
  public TaskControl findTask(/*@NotNull*/ TaskPredicate predicate) {
    Util.checkNotNullArgument(predicate);
    return TaskIterable.find(tasks.values(), predicate);
  }

  @Override
  public Future<?> submit(/*@NotNull*/ final Runnable task) {
    Util.checkNotNullArgument(task);
    return executorService.submit(task);
  }

  @Override
  public <T> Future<T> submit(/*@NotNull*/ Callable<T> task) {
    Util.checkNotNullArgument(task);
    return executorService.submit(task);
  }

  @Override
  public TaskControl getTask(/*@NotNull*/ String contextId) {
    Util.checkNotNullArgument(contextId);
    return tasks.get(contextId);
  }

}
