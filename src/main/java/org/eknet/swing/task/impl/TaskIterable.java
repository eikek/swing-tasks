package org.eknet.swing.task.impl;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskPredicate;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.09.11 21:16
 */
public class TaskIterable implements Iterable<TaskControl> {

  private final Iterable<TaskControl> delegate;
  private final TaskPredicate filter;

  protected TaskIterable(Iterable<TaskControl> delegate, TaskPredicate filter) {
    this.delegate = delegate;
    this.filter = filter;
  }

  @Override
  public Iterator<TaskControl> iterator() {
    return new TaskIterator(delegate.iterator(), filter);
  }

  public static TaskIterable filter(@NotNull Iterable<TaskControl> iterable, @NotNull TaskPredicate filter) {
    return new TaskIterable(iterable, filter);
  }

  public static TaskControl find(@NotNull Iterable<TaskControl> iterable, @NotNull TaskPredicate filter) {
    Iterator<TaskControl> iter = filter(iterable, filter).iterator();
    if (iter.hasNext()) {
      return iter.next();
    }
    return null;
  }
}
