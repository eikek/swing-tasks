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

import java.awt.Component;
import java.util.List;

/**
 * A long-running task.
 * <p/>
 * The contract is this of {@link javax.swing.SwingWorker} with the difference that in case
 * of an execution exception the {@link #failed(Throwable)} method is invoked and {@link #done(Object)}
 * is not.
 * <p/>
 * The class {@link AbstractTask} is meant to be extended for implementing tasks.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 19.07.11 22:13
 */
public interface Task<V, C> {

  /**
   * A id that identifies this task (or a group of tasks). This doesn't have
   * to be unique among all tasks. It's used to register {@link TaskListener}
   * on this id  that will receive events solely for tasks with a certain id.
   * 
   * @return the task id, never null
   */
  /*@NotNull*/
  String getId();

  /**
   * This method implements the long-running work and returns a result. This
   * method is invoked on a worker thread.
   * 
   * @param tracker to publish intermediate results, never null
   * @return the result value of the task or null
   * @throws Exception
   */
  V execute(/*@NotNull*/ Tracker<C> tracker) throws Exception;

  /**
   * This method is invoked once {@link #execute(Tracker)} has successfully finished
   * and is called on the EDT - so it's safe to access swing componentes in this method.
   *
   * @param value
   */
  void done(/*@Nullable*/ V value);

  /**
   * This method is invoked if {@link #execute(Tracker)} threw an exception. This method
   * is executed on the EDT.
   * 
   * @param cause
   */
  void failed(/*@Nullable*/ Throwable cause);

  /**
   * Calls going to {@link Tracker#publish(Object[])} from within
   * {@link #execute(Tracker)} are routed to this method. This method
   * is invoked on the EDT.
   * 
   * @param chunks
   */
  void process(List<C> chunks);

  /**
   *
   * @return the task's mode, never null
   */
  Mode getMode();

  /**
   * When {@link Mode#BLOCKING} is used, a component may be specified to find the
   * glasspane to show. If this is {@code null}, all available glasspanes of all
   * windows are made visible, so all windows of the application are blocked. Otherwise,
   * if this returns a component, only its nearest compatible parent component will
   * be blocked.
   * <p/>
   * "Compatible parent" means, it will try to find a parent component that contains a
   * glass pane. These are components that implement either {@link GlassPaneContainer}
   * or {@link javax.swing.RootPaneContainer}.
   * 
   * @return the component attached to this task or null
   */
  Component getComponent();

}
