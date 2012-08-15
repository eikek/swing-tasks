package org.eknet.swing.task.impl;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.09.11 21:05
 */
public final class Util {

  private Util() {}

  public static <T> T findComponent(Component component, /*@NotNull*/ Class<T> type) {
    checkNotNullArgument(type);
    while (component != null) {
      if (type.isInstance(component)) {
        //noinspection unchecked
        return (T) component;
      }
      component = component.getParent();
    }
    return null;
  }

  //This method is copied from class {@code DefaulWindowPlacementManager} by <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  public static void placeDialog(Window parent, Window window) {
      Rectangle rect = new Rectangle();
      rect.width = window.getWidth();
      rect.height = window.getHeight();
      rect.x = parent.getX() + parent.getWidth() / 2 - rect.width / 2;
      rect.y = parent.getY() + parent.getHeight() / 2 - rect.height / 2;
      fitRectangle(rect, window.getGraphicsConfiguration().getBounds());
      window.setBounds(rect);
  }

  // This method is copied from class {@code SwingUtil} by <a href="mailto:herzog@raffael.ch">Raffael Herzog</a> 
  public static void fitRectangle(Rectangle rect, Rectangle target) {
        if ( rect.width > target.width ) {
            rect.width = target.width;
        }
        if ( rect.height > target.height ) {
            rect.height = target.height;
        }
        if ( rect.x < target.x ) {
            rect.x = target.x;
        }
        else if ( rect.x + rect.width > target.x + target.width ) {
            rect.x = target.x + target.width - rect.width;
        }
        if ( rect.y < target.y ) {
            rect.y = target.y;
        }
        else if ( rect.y + rect.height > target.y + target.height ) {
            rect.y = target.y + target.height - rect.height;
        }
    }


  public static int size(Iterable<?> iterable) {
    if (iterable instanceof Collection) {
      Collection collection = (Collection) iterable;
      return collection.size();
    } else {
      Iterator iter = iterable.iterator();
      int counter = 0;
      while (iter.hasNext()) {
        iter.next();
        counter++;
      }
      return counter;
    }
  }

  private static final long day = 24 * 60 * 60;
  private static final long hour = 60 * 60;

  public static String formatDuration(Long millis) {
    if (millis == null) {
      return "00:00";
    }
    StringBuilder buffer = new StringBuilder();
    double time = ((double) millis) / 1000;
    long seconds = Math.round(time);

    long days = seconds / day;
    if (days > 0) {
      buffer.append(String.format("%02d", days)).append(":");
      seconds = seconds - (day * days);
    }

    long hours = seconds / hour;
    if (hours > 0) {
      buffer.append(String.format("%02d", hours)).append(":");
      seconds = seconds - (hour * hours);
    } else {
      buffer.append("00:");
    }

    if (seconds < 0) {
      seconds = 0;
    }
    buffer.append(String.format("%02d", seconds));
    return buffer.toString();
  }

  public static void checkNotNullArgument(Object obj, String message) {
    String msg = message == null ? "violation of notnull constraint" : message;
    if (obj == null) {
      throw new IllegalArgumentException(msg);
    }
  }

  public static void checkNotNullArgument(Object obj) {
    checkNotNullArgument(obj, null);
  }

  public static void checkNotNullState(Object obj, String message) {
    String msg = message == null ? "violation of notnull constraint" : message;
    if (obj == null) {
      throw new IllegalStateException(msg);
    }
  }

  public static void checkNotNullState(Object obj) {
    checkNotNullArgument(obj, null);
  }
}
