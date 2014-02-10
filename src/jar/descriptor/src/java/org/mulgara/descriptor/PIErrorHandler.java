/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.descriptor;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.xml.transform.*;

import org.apache.log4j.Logger;
import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;

/**
 * <meta name="usage" content="general"/> Implement SAX error handler for
 * default reporting.
 *
 * @created 2002-03-15
 *
 * @author Keith Ahern
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:11 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class PIErrorHandler implements ErrorHandler, ErrorListener {

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * Description of the Field
   */
  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger("XALAN");

  /**
   * Description of the Field
   */
  PrintWriter m_pw;

  /**
   * Constructor DefaultErrorHandler
   *
   * @param pw PARAMETER TO DO
   */
  public PIErrorHandler(PrintWriter pw) {

    m_pw = pw;
  }

  /**
   * Constructor DefaultErrorHandler
   *
   * @param pw PARAMETER TO DO
   */
  public PIErrorHandler(PrintStream pw) {

    m_pw = new PrintWriter(pw, true);
  }

  /**
   * Constructor DefaultErrorHandler
   *
   */
  public PIErrorHandler() {

    m_pw = new PrintWriter(System.err, true);
  }

  /**
   * METHOD TO DO
   *
   * @param exception PARAMETER TO DO
   */
  public static void ensureLocationSet(TransformerException exception) {

    // SourceLocator locator = exception.getLocator();
    SourceLocator locator = null;
    Throwable cause = exception;

    // Try to find the locator closest to the cause.
    do {

      if (cause instanceof SAXParseException) {

        locator = new ComboLocator( (SAXParseException) cause);
      }
      else if (cause instanceof TransformerException) {

        SourceLocator causeLocator =
            ( (TransformerException) cause).getLocator();

        if (null != causeLocator) {

          locator = causeLocator;
        }
      }

      if (cause instanceof TransformerException) {

        cause = ( (TransformerException) cause).getCause();
      }
      else if (cause instanceof SAXException) {

        cause = ( (SAXException) cause).getException();
      }
      else {

        cause = null;
      }
    }
    while (null != cause);

    exception.setLocator(locator);
  }

  /**
   * Receive notification of a warning. <p>
   *
   * SAX parsers will use this method to report conditions that are not errors
   * or fatal errors as defined by the XML 1.0 recommendation. The default
   * behaviour is to take no action.</p> <p>
   *
   * The SAX parser must continue to provide normal parsing events after
   * invoking this method: it should still be possible for the application to
   * process the document through to the end.</p>
   *
   * @param exception The warning information encapsulated in a SAX parse
   *      exception.
   * @throws SAXException Any SAX exception, possibly wrapping another
   *      exception.
   */
  public void warning(SAXParseException exception) throws SAXException {

    printLocation(m_pw, exception);
  }

  /**
   * Receive notification of a recoverable error. <p>
   *
   * This corresponds to the definition of "error" in section 1.2 of the W3C XML
   * 1.0 Recommendation. For example, a validating parser would use this
   * callback to report the violation of a validity constraint. The default
   * behaviour is to take no action.</p> <p>
   *
   * The SAX parser must continue to provide normal parsing events after
   * invoking this method: it should still be possible for the application to
   * process the document through to the end. If the application cannot do so,
   * then the parser should report a fatal error even if the XML 1.0
   * recommendation does not require it to do so.</p>
   *
   * @param exception The error information encapsulated in a SAX parse
   *      exception.
   * @throws SAXException Any SAX exception, possibly wrapping another
   *      exception.
   */
  public void error(SAXParseException exception) throws SAXException {

    printLocation(m_pw, exception);

    throw exception;
  }

  /**
   * Receive notification of a non-recoverable error. <p>
   *
   * This corresponds to the definition of "fatal error" in section 1.2 of the
   * W3C XML 1.0 Recommendation. For example, a parser would use this callback
   * to report the violation of a well-formedness constraint.</p> <p>
   *
   * The application must assume that the document is unusable after the parser
   * has invoked this method, and should continue (if at all) only for the sake
   * of collecting addition error messages: in fact, SAX parsers are free to
   * stop reporting any other events once this method has been invoked.</p>
   *
   * @param exception The error information encapsulated in a SAX parse
   *      exception.
   * @throws SAXException Any SAX exception, possibly wrapping another
   *      exception.
   */
  public void fatalError(SAXParseException exception) throws SAXException {

    printLocation(m_pw, exception);
    throw exception;
  }

  /**
   * Receive notification of a warning. <p>
   *
   * SAX parsers will use this method to report conditions that are not errors
   * or fatal errors as defined by the XML 1.0 recommendation. The default
   * behaviour is to take no action.</p> <p>
   *
   * The SAX parser must continue to provide normal parsing events after
   * invoking this method: it should still be possible for the application to
   * process the document through to the end.</p>
   *
   * @param exception The warning information encapsulated in a SAX parse
   *      exception.
   * @throws javax.xml.transform.TransformerException Any SAX exception,
   *      possibly wrapping another exception.
   * @see javax.xml.transform.TransformerException
   * @throws TransformerException EXCEPTION TO DO
   */
  public void warning(TransformerException exception) throws
      TransformerException {

    printLocation(m_pw, exception);
  }

  /**
   * Receive notification of a recoverable error. <p>
   *
       * This corresponds to the definition of "error" in section 1.2 of the W3C XML
   * 1.0 Recommendation. For example, a validating parser would use this
   * callback to report the violation of a validity constraint. The default
   * behaviour is to take no action.</p> <p>
   *
   * The SAX parser must continue to provide normal parsing events after
   * invoking this method: it should still be possible for the application to
   * process the document through to the end. If the application cannot do so,
   * then the parser should report a fatal error even if the XML 1.0
   * recommendation does not require it to do so.</p>
   *
   * @param exception The error information encapsulated in a SAX parse
   *      exception.
   * @throws javax.xml.transform.TransformerException Any SAX exception,
   *      possibly wrapping another exception.
   * @see javax.xml.transform.TransformerException
   * @throws TransformerException EXCEPTION TO DO
   */
  public void error(TransformerException exception) throws TransformerException {

    printLocation(m_pw, exception);
    throw exception;
  }

  /**
   * Receive notification of a non-recoverable error. <p>
   *
   * This corresponds to the definition of "fatal error" in section 1.2 of the
   * W3C XML 1.0 Recommendation. For example, a parser would use this callback
   * to report the violation of a well-formedness constraint.</p> <p>
   *
   * The application must assume that the document is unusable after the parser
   * has invoked this method, and should continue (if at all) only for the sake
   * of collecting addition error messages: in fact, SAX parsers are free to
   * stop reporting any other events once this method has been invoked.</p>
   *
   * @param exception The error information encapsulated in a SAX parse
   *      exception.
   * @throws javax.xml.transform.TransformerException Any SAX exception,
   *      possibly wrapping another exception.
   * @see javax.xml.transform.TransformerException
   * @throws TransformerException EXCEPTION TO DO
   */
  public void fatalError(TransformerException exception) throws
      TransformerException {

    printLocation(m_pw, exception);
    throw exception;
  }

  /**
   * METHOD TO DO
   *
   * @param pw PARAMETER TO DO
   * @param exception PARAMETER TO DO
   */
  public void printLocation(PrintStream pw, TransformerException exception) {

    printLocation(new PrintWriter(pw), exception);
  }

  /**
   * METHOD TO DO
   *
   * @param pw PARAMETER TO DO
   * @param exception PARAMETER TO DO
   */
  public void printLocation(java.io.PrintStream pw,
                            org.xml.sax.SAXParseException exception) {

    printLocation(new PrintWriter(pw), exception);
  }

  /**
   * Print location of error to several destinations including log files, files
   * and console.
   *
   * @param pw PARAMETER TO DO
   * @param exception PARAMETER TO DO
   */
  public void printLocation(PrintWriter pw, Throwable exception) {

    // writes out stack trace
    Descriptor.writeStackTrace(exception, "XALAN ERROR");

    // Try to find the locator closest to the cause.
    SourceLocator locator = null;
    Throwable cause = exception;

    do {

      if (cause instanceof SAXParseException) {

        locator = new ComboLocator( (SAXParseException) cause);
      }
      else if (cause instanceof TransformerException) {

        SourceLocator causeLocator =
            ( (TransformerException) cause).getLocator();

        if (null != causeLocator) {

          locator = causeLocator;
        }
      }

      if (cause instanceof TransformerException) {

        cause = ( (TransformerException) cause).getCause();
      }
      else {

        cause = getWrappedException(cause);
      }
    }
    while (null != cause);

    if (null != locator) {

      String id =
          (locator.getPublicId() != null)
          ? locator.getPublicId()
          : ( (null != locator.getSystemId()) ? locator.getSystemId()
             : "SystemId Unknown");

      m_pw.print(id + "; Line " + locator.getLineNumber() + "; Column " +
                 locator.getColumnNumber() + "; ");
    }
    else {

      m_pw.print("(Location of error unknown)");
    }

    // Write Cause stack to decsriptor log file
    m_pw.println("Cause: " + exception.getMessage());

    // reset cause to top level cause
    cause = exception.getCause();

    while (cause != null) {

      // get Cause Exception
      String cname = cause.getClass().getName();

      // get Message
      String message = cause.getMessage();

      // fix up message if null
      message = (message == null) ? "No Exception Message" : message;

      m_pw.println("which was caused by:");

      m_pw.println("(" + cname + ") " + message);

      // go again
      cause = cause.getCause();
    }

    // write pointer to stack output
    m_pw.println(eol + "Full Stack Trace in file " +
                 Descriptor.DESCRIPTOR_STACKTRACE_OUTPUT +
                 " in directory where Mulgara was started from");
  }

  /**
   * Retrieve an exception from the .getException method of the parameter.
   * If the e parameter has no such method, then return null.
   *
   * @param e The object to call getException() on.
   * @return The wrapped exception, or <code>null</code> if there is no getException method on e.
   */
  private static Exception getWrappedException(Object e) {
    Class<?> c = e.getClass();
    try {
      Method m = c.getMethod("getException");
      return (Exception)m.invoke(e);
    } catch (Exception nse) {
      return null;
    }
  }

  /**
   * This class merges the interfaces of Locator and SourceLocator
   * as they are completely identical.
   */
  private static class ComboLocator extends LocatorImpl implements SourceLocator {

    /**
     * Main constructor.
     *
     * @param e The exception to get the location info from.
     */
    public ComboLocator(SAXParseException e) {
      setColumnNumber(e.getColumnNumber());
      setLineNumber(e.getLineNumber());
      setPublicId(e.getPublicId());
      setSystemId(e.getSystemId());
    }
  }

}
