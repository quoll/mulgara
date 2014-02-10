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

package org.mulgara.demo.mp3.playback;

// Java 2 standard packages
import java.io.*;

// Logging
import org.apache.log4j.Logger;

// Local packages


/**
 * An InputStream wrapper that can be blocked.
 *
 * @created 2004-12-10
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:06 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class BlockableInputStream extends InputStream {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(BlockableInputStream.class.
      getName());

  /** Wrapped Input Stream */
  private InputStream stream = null;

  /** Whether the Stream is blocked */
  private boolean blocked = false;

  /**
   * Constructor
   * @param stream InputStream
   * @throws IllegalArgumentException
   */
  public BlockableInputStream(InputStream stream) throws
      IllegalArgumentException {
    if (stream == null) {
      throw new IllegalArgumentException("InputStream is null.");
    }
    this.stream = stream;
  }

  /**
   * Blocks the stream until unblock() is called.
   */
  public synchronized void block() {
    blocked = true;
  }

  /**
   * unblocks the stream.
   */
  public synchronized void unblock() {
    blocked = false;
    notifyAll();
  }

  /**
   * Waits until blocked is false.
   */
  private synchronized void doBlock() {
    while (blocked) {
      try {
        wait();
      }
      catch (InterruptedException interruptException) {
        throw new RuntimeException("Block interrupted", interruptException);
      }
    }
  }

  /**
   * Delegates to the underlying stream.
   *
   * @return int
   * @throws IOException
   */
  public int available() throws IOException {
    return stream.available();
  }

  /**
   * Delegates to the underlying stream.
   *
   * @throws IOException
   */
  public void close() throws IOException {
    stream.close();
  }

  /**
   * Delegates to the underlying stream.
   * @param readlimit int
   */
  public void mark(int readlimit) {
    stream.mark(readlimit);
  }

  /**
   * Delegates to the underlying stream.
   * @return boolean
   */
  public boolean markSupported() {
    return stream.markSupported();
  }

  /**
   * Waits until unblocked and then delegates to the underlying stream.
   * @throws IOException
   * @return int
   */
  public int read() throws IOException {
    doBlock();
    return stream.read();
  }

  /**
   * Waits until unblocked and then delegates to the underlying stream.
   * @param b byte[]
   * @throws IOException
   * @return int
   */
  public int read(byte[] b) throws IOException {
    doBlock();
    return stream.read(b);
  }

  /**
   * Waits until unblocked and then delegates to the underlying stream.
   * @param b byte[]
   * @param off int
   * @param len int
   * @throws IOException
   * @return int
   */
  public int read(byte[] b, int off, int len) throws IOException {
    doBlock();
    return stream.read(b, off, len);
  }

  /**
   * Delegates to the underlying stream.
   * @throws IOException
   */
  public void reset() throws IOException {
    stream.reset();
  }

  /**
   * Delegates to the underlying stream.
   * @param n long
   * @throws IOException
   * @return long
   */
  public long skip(long n) throws IOException {
    return stream.skip(n);
  }

}

