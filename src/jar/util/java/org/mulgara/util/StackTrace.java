/*
 * Copyright 2008 Fedora Commons, Inc.
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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Creates a stack trace for the current position at creation, or from a provided
 * Throwable object.
 *
 * @created Jul 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StackTrace {

  /** The string output for this stack. */
  private String fullTrace;

  /** The Throwable containing all the information for this stack. */
  private final Throwable throwable;

  /**
   * The offset for accessing the stack array. Either 0 or 1.
   * 1 is used to skip the frame for the constructor of this object.
   */
  private final int offset;

  /**
   * Build a new stack around a provided Throwable.
   * @param throwable The throwable object with the stack to encode.
   */
  public StackTrace(Throwable throwable) {
    this.throwable = throwable;
    offset = 0;
    fullTrace = getStringTrace();
  }

  /**
   * Build a new stack based on the current position.
   */
  public StackTrace() {
    this.throwable = new Throwable();
    offset = 1;
    fullTrace = getStringTrace();
  }

  /**
   * Output this stack as a string.
   */
  public String toString() {
    return fullTrace;
  }

  /**
   * Get a stack frame for a given level from this stack. 
   * @param level The level of the frame to retrieve. 0 <= level < {@link #getStackDepth()}
   * @return A StackTraceElement encoding the frame for the given level.
   */
  public StackTraceElement getFrame(int level) {
    return throwable.getStackTrace()[level + offset];
  }

  /**
   * The maximum depth of this stack.
   * @return The number of the deepest frame + 1.
   */
  public int getStackDepth() {
    return throwable.getStackTrace().length - offset;
  }

  /**
   * Gets the string for this stack trace.
   * @return A string encoding of the stack trace.
   */
  private String getStringTrace() {
    StringBuilder b = new StringBuilder("STACK TRACE:\n");
    StackTraceElement[] stack = throwable.getStackTrace();
    for (int level = offset; level < stack.length; level++) {
      b.append("  ").append(stack[level].toString()).append("\n");
    }
    Throwable reason = throwable.getCause();
    while (reason != null) {
      b.append(" Caused by:");
      b.append(reason.getClass().toString());
      b.append(": ");
      b.append(reason.getMessage());
      stack = reason.getStackTrace();
      for (int level = 0; level < stack.length; level++) {
        b.append("  ").append(stack[level].toString()).append("\n");
      }
      reason = reason.getCause();
    }
    return b.toString();
  }

  /**
   * Converts a throwable to a string. This appears an Exception/Error/etc
   * instead of using the STACK TRACE label.
   * @param t The Throwable to print.
   * @return A String with the output from the Throwable.
   */
  public static String throwableToString(Throwable t) {
    StringWriter strWriter = new StringWriter();
    t.printStackTrace(new PrintWriter(strWriter));
    return strWriter.toString();
  }

  /**
   * Gets the base of the cause chain.
   * @param t The Throwable to get the cause from.
   * @return The base of the cause chain for the throwable.
   */
  public static Throwable getReason(Throwable t) {
    while (t.getCause() != null) t = t.getCause();
    return t;
  }

  /**
   * Gets the message from the base cause for a Throwable.
   * @param t The throwable to get the base of the cause chain for.
   * @return The message from the base case.
   */
  public static String getReasonMessage(Throwable t) {
    String msg = getReason(t).getMessage();
    return msg != null ? msg : t.getClass().getName();
  }

}
