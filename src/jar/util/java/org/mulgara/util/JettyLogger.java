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

import java.util.Arrays;

import org.eclipse.jetty.util.log.Logger;

/**
 * This class is used for managing logging by the Jetty libraries.
 * It can be set to send to the standard logging facilities, or set to ignore everything.
 * @see org.eclipse.jetty.log.Logger
 *
 * @created Aug 6, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class JettyLogger implements Logger {

  /** The Log4J logger this class can proxy for. */
  protected static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JettyLogger.class.getName());

  /**
   * The property that has to be set to this class in order for this class to be loaded
   * as the logger for org.eclipse.jetty.* classes.
   */
  public static final String LOGGING_CLASS_PROPERTY = "org.eclipse.jetty.util.log.class";

  /** This flag turns logging on and off. */
  static boolean enabled = false;

  /** This flag turns debug logging on and off. */
  boolean debugEnabled = true;

  /** The name of this object. */
  private final String name;


  /**
   * Sets all instances of this logger on and off.
   * @param enabled when <code>false</code> this logger will not do anything.
   */
  public static void setEnabled(boolean enabled) {
    JettyLogger.enabled = enabled;
  }


  /**
   * Returns the enabled state for all instances of this logging class.
   * @return <code>true</code> if this class logs anything.
   */
  public static boolean isEnabled() {
    return enabled;
  }


  /**
   * Creates a logger with a default name.
   */
  public JettyLogger() {
    this.name = getClass().getName() + System.identityHashCode(this);
  }


  /**
   * Creates a named logger.
   * @param name The name for this logger.
   */
  public JettyLogger(String name) {
    this.name = name;
  }


  /**
   * Logs a debug message and Throwable.
   * @param msg The message to log.
   * @param th The Throwable relevant to this log.
   */
  @Override
public void debug(String msg, Throwable th) {
    if (enabled && debugEnabled) log.debug(msg, th);
  }


  /**
   * Logs a debug message, and optionally up to 2 objects. These appear in the message
   * replacing the first 2 instances of "{}" characters, if such characters exist.
   * @param msg The message to log.
   * @param arg0 Data to replace the first "{}" sequence in <var>msg</var>.
   * @param arg1 Data to replace the second "{}" sequence in <var>msg</var>.
   */
  public void debug(String msg, Object arg0, Object arg1) {
    if (enabled && debugEnabled) log.debug(format(msg, arg0, arg1));
  }


  /**
   * Get an instance of a Logger for a given name.
   * @param name The name of the logger to return. If <code>null</code> or empty
   *        then the name of the current logger is used.
   * @return A logger with the name from the <var>name</var> parameter, or the current logger
   *         if no name was given.
   */
  @Override
public Logger getLogger(String name) {
    if (name == null || name.equals("") || name.equals(this.name)) return this;
    return new JettyLogger(name);
  }


  /**
   * Logs an info message, and optionally up to 2 objects. These appear in the message
   * replacing the first 2 instances of "{}" characters, if such characters exist.
   * @param msg The message to log.
   * @param arg0 Data to replace the first "{}" sequence in <var>msg</var>.
   * @param arg1 Data to replace the second "{}" sequence in <var>msg</var>.
   */
  public void info(String msg, Object arg0, Object arg1) {
    if (enabled) log.info(format(msg, arg0, arg1));
  }


  /**
   * Indicates if debug messages are logged.
   * @return <code>true</code> if debug messages are acted on.
   */
  @Override
public boolean isDebugEnabled() {
    return enabled && debugEnabled;
  }


  /**
   * Set whether or not to log debug messages.
   * @param enabled <code>true</code> if debug messages are to be acted on.
   */
  @Override
public void setDebugEnabled(boolean enabled) {
    debugEnabled = enabled;
  }


  /**
   * Logs a warning message and Throwable.
   * @param msg The message to log.
   * @param th The Throwable relevant to this log.
   */
  @Override
public void warn(String msg, Throwable th) {
    if (enabled) log.warn(msg, th);
  }


  /**
   * Logs a warning message, and optionally up to 2 objects. These appear in the message
   * replacing the first 2 instances of "{}" characters, if such characters exist.
   * @param msg The message to log.
   * @param arg0 Data to replace the first "{}" sequence in <var>msg</var>.
   * @param arg1 Data to replace the second "{}" sequence in <var>msg</var>.
   */
  public void warn(String msg, Object arg0, Object arg1) {
    if (enabled) log.warn(format(msg, arg0, arg1));
  }


  /**
   * Writes 2 objects into a string, replacing the instances of {} with the objects.
   * @param msg The message with the two {} instances.
   * @param arg0 The first object to print.
   * @param arg1 The second object to print.
   * @return The new string with the objects inserted.
   */
  private String format(String msg, Object arg0, Object arg1) {
    int i0 = msg.indexOf("{}");
    int i1 = i0 < 0 ? -1 : msg.indexOf("{}", i0 + 2);

    if (arg1 != null && i1 >= 0) {
        msg=msg.substring(0, i1) + arg1 + msg.substring(i1 + 2);
    }
    if (arg0 != null && i0 >= 0) {
        msg = msg.substring(0, i0) + arg0 + msg.substring(i0 + 2);
    }
    return msg;
  }

  /**
   * Duplicate the operation of String.format, but replace URI spaces with an escaped version.
   * Falls back to an unformated version on failure.
   * @param arg0 The format string.
   * @param arg1 The parameters to put into the formatted string.
   * @return A formatted string.
   */
  private String format(String arg0, Object... arg1) {
    try {
      return String.format(arg0.replace("%20", "%%20"), arg1);
    } catch (Exception e) {
      return "Unformatted log: " + arg0 + " : " + (arg1 != null ? Arrays.asList(arg1) : "(null)");
    }
  }

  @Override
  public void debug(Throwable msg) {
    if (enabled && debugEnabled) log.debug(msg);
  }


  @Override
  public void debug(String arg0, Object... arg1) {
    if (enabled && debugEnabled) log.debug(format(arg0, arg1));
  }


  @Override
  public String getName() {
    return JettyLogger.class.getName() + "@" + System.identityHashCode(this);
  }


  @Override
  public void info(Throwable arg0) {
    if (enabled) log.info(arg0);
  }


  @Override
  public void info(String arg0, Object... arg1) {
    if (enabled) log.info(format(arg0, arg1));
  }


  @Override
  public void info(String arg0, Throwable arg1) {
    if (enabled) log.info(arg0, arg1);
  }


  @Override
  public void warn(Throwable arg0) {
    if (enabled) log.warn(arg0);
  }


  @Override
  public void warn(String arg0, Object... arg1) {
    if (enabled) log.warn(format(arg0, arg1));
  }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jetty.util.log.Logger#ignore(java.lang.Throwable)
	 */
	@Override
	public void ignore(Throwable arg0) {
		// TODO Auto-generated method stub

	}
}
