/*
 * Copyright 2010 Revelytix.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.util;

/**
 * Utility class for working with threads.
 * @created 2010-02-02
 * @author Alex Hall
 * @copyright &copy; 2010 <a href="http://www.revelytix.com/">Revelytix</a>
 * @license <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License, v2.0</a>
 */
public class ThreadUtil {
  
  /** Default exception message to use when a thread was interrupted. */
  private static final String DEFAULT_INTERRUPT_MSG = "Process was interrupted.";
  
  /**
   * Checks whether the current thread has been interrupted, either locally via the
   * {@link Thread#interrupt()} method or remotely by an interruptible RMI operation.
   * If the thread was interrupted, then throw an exception.
   * @param <E> The type of exception to throw if the thread was interrupted.
   * @param exceptionClass The class used to instantiate the exception.
   * @throws E If the thread has been interrupted.
   */
  public static <E extends Exception> void checkForInterrupt(Class<E> exceptionClass) throws E {
    checkForInterrupt(exceptionClass, DEFAULT_INTERRUPT_MSG);
  }

  /**
   * Checks whether the current thread has been interrupted, either locally via the
   * {@link Thread#interrupt()} method or remotely by an interruptible RMI operation.
   * If the thread was interrupted, then throw an exception with the given message.
   * @param <E> The type of exception to throw if the thread was interrupted.
   * @param exceptionClass The class used to instantiate the exception.
   * @param msg The message to use when constructing the exception.
   * @throws E If the thread has been interrupted.
   */
  public static <E extends Exception> void checkForInterrupt(Class<E> exceptionClass, String msg) throws E {
    if (Thread.currentThread().isInterrupted() || Rmi.isInterrupted()) {
      throw Reflect.newInstance(exceptionClass, msg);
    }
  }
}
