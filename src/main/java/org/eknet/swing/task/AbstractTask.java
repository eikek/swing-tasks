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
import java.util.EventObject;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 00:40
 */
public abstract class AbstractTask<V, C> implements Task<V, C> {

  private final String id;
  private Mode mode = Mode.BACKGROUND;
  private Component component;

  protected AbstractTask(String id) {
    this.id = id;
  }

  protected AbstractTask(String id, Mode mode) {
    this.id = id;
    this.mode = mode;
  }

  protected AbstractTask(String id, Component component) {
    this.id = id;
    this.mode = Mode.BLOCKING;
    this.component = component;
  }
  
  protected AbstractTask(String id, EventObject eventObject) {
    this.id = id;
    this.mode = Mode.BLOCKING;
    setEventObjectComponent(eventObject);
  }

  @NotNull
  @Override
  public String getId() {
    return id;
  }

  @Override
  public void done(@Nullable V value) {
  }

  @Override
  public void failed(@Nullable Throwable cause) {
  }

  @Override
  public void process(List<C> chunks) {
  }

  @NotNull
  @Override
  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  @Override
  public Component getComponent() {
    return component;
  }

  public void setEventObjectComponent(EventObject eventObject) {
    if (eventObject.getSource() instanceof Component) {
      setComponent((Component) eventObject.getSource());
    }
  }

  public void setComponent(Component component) {
    this.component = component;
  }
}
