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

/**
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 19.07.11 22:45
 */
public enum Mode {

  /**
   * Like {@link #BACKGROUND} but this task will not
   * throw any events during execution.
   *
   */
  SILENT,

  /**
   * Executed in the background. The ui is still
   * responsive.
   *
   */
  BACKGROUND,

  /**
   * Blocks the UI while the tasks is executed.
   * 
   */
  BLOCKING
  
}
