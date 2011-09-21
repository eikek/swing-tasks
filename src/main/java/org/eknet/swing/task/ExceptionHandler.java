package org.eknet.swing.task;

import java.util.EventListener;

// note, this is a slightly modified copy from here:
//  https://scm.ops4j.org/repos/ops4j/laboratory/users/raffael/hiveapp/trunk/hiveapp/src/main/ch/raffael/hiveapp/services/ExceptionHandler.java

/**
 * Handler for exceptions that occurred in an event listener.
 *
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public interface ExceptionHandler<T extends EventListener> {

    ExceptionHandler RETHROW_EXCEPTION_HANDLER = new ExceptionHandler() {
      @Override
      public boolean handleException(EventListener listener, Throwable exception) throws Throwable {
        throw exception;
      }
    };

    /**
     * Handle an exception that occurred in an event listener. The exception may also be
     * rethrown.
     *
     * @param listener  The listener that throw the exception.
     * @param exception The thrown exception.
     *
     * @return <code>true</code>, if the remaining listeners should be notified,
     *         <code>false</code> to cancel.
     *
     * @throws Throwable Rethrowing or converting exceptions.
     */
    boolean handleException(T listener, Throwable exception) throws Throwable;

}