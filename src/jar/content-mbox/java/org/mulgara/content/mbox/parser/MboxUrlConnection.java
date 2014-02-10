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

package org.mulgara.content.mbox.parser;

import java.io.*;
import java.net.*;

/**
 * Handler for the mbox URL.  Basically, uses a file to implement the handler.
 *
 * @created 2001-08-01
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:41 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/../../LICENCE_LGPL.txt">Licence description</A>
 */
public class MboxUrlConnection extends java.net.URLConnection {

  /**
   * The file that's a mbox.
   */
  File file;

  /**
   * The buffered output stream to write to the file.
   */
  BufferedOutputStream outStream = null;

  /**
   * The buffered input stream to read the file.
   */
  BufferedInputStream inStream = null;

  /**
   * Create a new mbox handler.
   *
   * @param url the specified url.
   * @throws IOException if an i/o exception occurs.
   */
  protected MboxUrlConnection(URL url) throws IOException {

    super(url);
  }

  /**
   * Opens the file.
   *
   * @throws IOException if the file open fails.
   */
  public void connect() throws IOException {

    file = new File(url.getFile());
    connected = true;
  }

  /**
   * Connects to the file if it is not connected and creates a new buffered
   * input stream.
   *
   * @return the input stream.
   * @throws IOException if an i/o exception occurs.
   */
  public InputStream getInputStream() throws IOException {

    if (this.connected) {

      connect();
    }

    if (inStream == null) {

      inStream = new BufferedInputStream(new FileInputStream(file));
    }
    return inStream;
  }

  /**
   * Connects to the file if it is not connected and creates a new buffered
   * output stream.
   *
   * @return the output stream.
   * @throws IOException if an i/o exception occurs.
   */
  public OutputStream getOutputStream() throws IOException {

    if (this.connected) {

      connect();
    }

    if (outStream == null) {

      outStream = new BufferedOutputStream(new FileOutputStream(file));
    }
    return outStream;
  }
}
