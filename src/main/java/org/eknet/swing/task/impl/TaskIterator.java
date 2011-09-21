package org.eknet.swing.task.impl;

import java.util.Iterator;

import org.eknet.swing.task.TaskControl;
import org.eknet.swing.task.TaskPredicate;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.09.11 21:13
 */
public class TaskIterator implements Iterator<TaskControl> {

  private final Iterator<TaskControl> delegate;
  private final TaskPredicate filter;

  private TaskControl current;
  
  public TaskIterator(Iterator<TaskControl> delegate, TaskPredicate filter) {
    this.delegate = delegate;
    this.filter = filter;
  }

  @Override
  public boolean hasNext() {
    if (delegate.hasNext()) {
      this.current = delegate.next();
      return filter.apply(current) || hasNext();
    }
    return false;
  }

  @Override
  public TaskControl next() {
    return current;
  }

  @Override
  public void remove() {
    delegate.remove();
  }
}
