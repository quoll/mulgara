/**
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
package org.mulgara.itql;

/**
 * A set of utilities to provide legacy support for ItqlInterpreter operations.
 * @created Oct 11, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
class ItqlUtil {

  /** System line separator character */
  private static final String EOL = System.getProperty("line.separator");

  /**
   * Pretty prints the Exception Chain.
   *
   * @param e the throwable exception
   * @param preferredDepth the preferred depth to go into the exception to
   *   retrieve the root cause.  A depth of zero will chain all exceptions
   *   together.
   * @return A string containing the pretty printed Exception Chain.
   */
  public static String getCause(Throwable e, int preferredDepth) {

    // Keep getting the cause of the message until we reach preferred depth or null cause.
    Throwable preferredException = e;
    int index = 0;
    while ((preferredException != null) && (index != preferredDepth)) {
      // Pre-check next exception and increment index if it's not null.
      if (preferredException.getCause() != null) index++;
      // Get next exception in the chain
      preferredException = preferredException.getCause();
    }

    // Update e to the preferred exception if it is not null
    if (preferredException != null) e = preferredException;

    String message = e.getMessage();
    if (message == null) message = "";

    // get the cause of the exception
    Throwable cause = e.getCause();

    // descend into exception if possible
    if (cause != null) {
      // pretty print the cause
      String causeMsg = getCause(cause, 0);
      // only add the cause's message if there was one
      if (causeMsg != null) {
        // Extract the class name from the full path
        String exceptionClassName = cause.getClass().getName();
        exceptionClassName = exceptionClassName.substring(exceptionClassName.lastIndexOf('.') + 1);
        message += (EOL + "Caused by: (" + exceptionClassName + ") " + causeMsg);
      }
    }
    return message;
  }

}
