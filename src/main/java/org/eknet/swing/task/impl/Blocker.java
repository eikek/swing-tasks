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
/*
 * Copyright 2010 Raffael Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.swing.task.impl;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.WeakHashMap;

import javax.swing.RootPaneContainer;
import javax.swing.Timer;

import org.jetbrains.annotations.NotNull;

import org.eknet.swing.task.GlassPaneContainer;

// Note, this is a modified version of ch.raffael.util.swing.tasks.DefaultTaskTracker from
// Raffael Herzog's cru-swing module.

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 04.09.11 09:41
 */
public class Blocker {

  public static final MouseListener EMPTY_MOUSE_LISTENER = new MouseAdapter() {
  };
  public static final KeyListener EMPTY_KEY_LISTENER = new KeyAdapter() {
  };

  private WeakHashMap<Component, Object> knownGlassPanes = new WeakHashMap<Component, Object>();
  private final Timer globalCursorTimer;
  private final Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
  
  public Blocker() {
    globalCursorTimer = new Timer(250, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for (Window window : Window.getWindows()) {
          if (window instanceof RootPaneContainer) {
            Component glassPane = ((RootPaneContainer) window).getGlassPane();
            if (!knownGlassPanes.containsKey(glassPane)) {
              glassPane.addMouseListener(EMPTY_MOUSE_LISTENER);
              glassPane.addKeyListener(EMPTY_KEY_LISTENER);
            }
            glassPane.setCursor(waitCursor);
            glassPane.setVisible(true);
          }
        }
      }
    });
    globalCursorTimer.setRepeats(false);
  }

  public void block(Component component) {
    if (component == null) {
      blockAll();
      globalCursorTimer.start();
    } else {
      blockComponent(component);
    }
  }

  public void unblock(Component component) {
    if (component == null) {
      unblockAll();
      globalCursorTimer.stop();
    } else {
      unblockComponent(component);
    }
  }

  protected void blockComponent(@NotNull Component component) {
    Component glassPane = findGlassPane(component);
    if (glassPane != null) {
      setupGlassPane(glassPane);
      glassPane.setCursor(waitCursor);
      glassPane.setVisible(true);
    } else {
      blockAll();
    }
  }

  private Component findGlassPane(Component component) {
    Component glassPane = null;
    GlassPaneContainer contentPanel = Util.findComponent(component, GlassPaneContainer.class);
    if (contentPanel != null) {
      glassPane = contentPanel.getGlassPane();
    }
    if (glassPane == null) {
      RootPaneContainer rootPane = Util.findComponent(component, RootPaneContainer.class);
      glassPane = rootPane.getGlassPane();
    }
    return glassPane;
  }

  protected void unblockComponent(@NotNull Component component) {
    Component glassPane = findGlassPane(component);
    if (glassPane != null) {
      glassPane.setCursor(Cursor.getDefaultCursor());
      glassPane.setVisible(false);
    } else {
      unblockAll();
    }
  }


  protected void blockAll() {
    for (Window window : Window.getWindows()) {
      if (window instanceof RootPaneContainer) {
        Component glassPane = ((RootPaneContainer) window).getGlassPane();
        setupGlassPane(glassPane);
        glassPane.setVisible(true);
      }
    }
  }

  private void setupGlassPane(Component glassPane) {
    if (!knownGlassPanes.containsKey(glassPane)) {
      glassPane.addMouseListener(EMPTY_MOUSE_LISTENER);
      glassPane.addKeyListener(EMPTY_KEY_LISTENER);
      knownGlassPanes.put(glassPane, this);
    }
  }

  protected void unblockAll() {
    for (Window window : Window.getWindows()) {
      if (window instanceof RootPaneContainer) {
        Component glassPane = ((RootPaneContainer) window).getGlassPane();
        glassPane.setCursor(Cursor.getDefaultCursor());
        glassPane.setVisible(false);
      }
    }
  }

}
