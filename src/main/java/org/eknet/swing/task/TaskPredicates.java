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

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.07.11 17:34
 */
public class TaskPredicates {

  public static TaskPredicate blockingTasks = new TaskPredicate() {
    @Override
    public boolean apply(@NotNull TaskControl input) {
      return input.getContext().getTask().getMode() == Mode.BLOCKING;
    }
  };

  public static TaskPredicate backgroundTasks = new TaskPredicate() {
    @Override
    public boolean apply(@NotNull TaskControl input) {
      return input.getContext().getTask().getMode() == Mode.BACKGROUND;
    }
  };

  public static TaskPredicate pendingTasks = new TaskPredicate() {
    @Override
    public boolean apply(@NotNull TaskControl input) {
      return input.getContext().getState() == State.PENDING;
    }
  };

  public static TaskPredicate startedTasks = new TaskPredicate() {
    @Override
    public boolean apply(@NotNull TaskControl input) {
      return input.getContext().getState() == State.STARTED;
    }
  };

  public static TaskPredicate allTasks = new TaskPredicate() {
    @Override
    public boolean apply(@NotNull TaskControl control) {
      return true;
    }
  };

}
