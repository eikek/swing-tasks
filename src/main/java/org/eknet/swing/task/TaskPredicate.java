package org.eknet.swing.task;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.09.11 21:11
 */
public interface TaskPredicate {

  boolean apply(/*@NotNull*/ TaskControl control);
  
}
