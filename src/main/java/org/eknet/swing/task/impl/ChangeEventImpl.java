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

import org.jetbrains.annotations.NotNull;

import org.eknet.swing.task.ChangeEvent;
import org.eknet.swing.task.TaskContext;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 19:01
 */
public class ChangeEventImpl<T> implements ChangeEvent<T> {

  private final TaskContext source;
  private final T oldValue;
  private final T newValue;

  public ChangeEventImpl(T oldValue, T newValue, TaskContext context) {
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.source = context;
  }

  @Override
  public T getOldValue() {
    return oldValue;
  }

  @Override
  public T getNewValue() {
    return newValue;
  }

  @NotNull
  @Override
  public TaskContext getSource() {
    return source;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ChangeEventImpl that = (ChangeEventImpl) o;

    if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null) return false;
    if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null) return false;
    if (source != null ? !source.equals(that.source) : that.source != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = source != null ? source.hashCode() : 0;
    result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
    result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
    return result;
  }
}
