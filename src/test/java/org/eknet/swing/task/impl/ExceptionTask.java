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

import java.awt.Component;
import java.util.List;

import org.eknet.swing.task.Mode;
import org.eknet.swing.task.Task;
import org.eknet.swing.task.Tracker;

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 20.07.11 19:20
 */
public class ExceptionTask implements Task<Long, Long> {

  @Override
  public String getId() {
    return "exception-task";
  }

  @Override
  public Long execute(Tracker<Long> longTracker) throws Exception {
    toString();
    throw new RuntimeException("Exception while executing.");
  }

  @Override
  public void done(Long value) {
    toString();
  }

  @Override
  public void failed(Throwable cause) {
    toString();
  }

  @Override
  public void process(List<Long> chunks) {
    toString();
  }

  @Override
  public Mode getMode() {
    return Mode.BACKGROUND;
  }

  @Override
  public Component getComponent() {
    return null;
  }
}
