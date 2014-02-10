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

package org.mulgara.util.conversion.html;

import java.io.*;

import org.apache.log4j.Logger;

/**
 * The thread that performs HTML parsing.
 */
class ParserThread extends Thread {

  private static final Logger logger = Logger.getLogger(ParserThread.class);
  
  /** The parser to use. */
  HTMLParser parser;

  /**
   * Creates the thread on the parser.
   * @param p The parser to use.
   */
  ParserThread(HTMLParser p) {
    parser = p;
  }

  /**
   * Main processing method for the ParserThread object
   */
  public void run() {

    // convert pipeOut to pipeIn
    try {
      try {
        // parse document to pipeOut
        parser.HTMLDocument();
      } catch (ParseException e) {
        logger.warn("Parse Aborted", e);
      } catch (TokenMgrError e) {
        logger.warn("Parse Aborted", e);
      } finally {
        parser.pipeOut.close();
        synchronized (parser) {
          parser.summary.setLength(HTMLParser.SUMMARY_LENGTH);
          parser.titleComplete = true;
          parser.notifyAll();
        }
      }
    } catch (IOException e) {
      logger.warn("Error reading HTML content", e);
    }
  }
}
