/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.server;

import java.util.List;

/**
 * Represents a list of exceptions that may have occurred during a process.
 *
 * @created Sep 5, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ExceptionList extends Exception {

  /** The ID required for serialization. */
  private static final long serialVersionUID = -4409235739760552916L;

  /** A list of exceptions that accumulated in this condition. */
  private List<Throwable> exceptionList;


  /**
   * Creates an exception for representing a list of exceptions.
   * @param exceptions The list of exceptions causing the current condition.
   */
  public ExceptionList(List<Throwable> exceptions) {
    exceptionList = exceptions;
  }


  /**
   * Creates an exception for representing a list of exceptions.
   * @param exceptions The list of exceptions causing the current condition.
   */
  public ExceptionList(String message, List<Throwable> exceptions) {
    super(message);
    exceptionList = exceptions;
  }


  /**
   * Gets the list of exceptions that caused this condition.
   * @return The full list of Throwable objects.
   */
  public List<Throwable> getCauses() {
    return exceptionList;
  }
}
