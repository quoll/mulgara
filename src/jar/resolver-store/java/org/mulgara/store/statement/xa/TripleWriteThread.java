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
package org.mulgara.store.statement.xa;

// Java 2 standard packages
import java.io.*;
import java.util.LinkedList;

// Third party packages
import org.apache.log4j.Logger;


final class TripleWriteThread extends Thread {

  /**
   * Logger.
   */
  private final static Logger logger = Logger.getLogger(TripleWriteThread.class);

  private static final int BUFFER_SIZE = 50000;

  private static final int QUEUE_MAX_BUFFERS = 10;

  /** The current phase of the TripleAVLFile. */
  private TripleAVLFile.Phase phase;

  private File file;

  private long[][] buffer = null;

  private int index = 0;

  /** The queue of triples to add to the TripleAVLFile. */
  private LinkedList<long[][]> queue = new LinkedList<long[][]>();

  private boolean threadRunning = true;

  private boolean processing = false;

  private Throwable t = null;

  TripleWriteThread(File file) {
    super("TripleWriteThread: " + file);
    this.file = file;
    setDaemon(true);
    start();
  }

  synchronized void setPhase(TripleAVLFile.Phase phase) {
    if (buffer != null || processing || !queue.isEmpty()) {
      throw new IllegalStateException(
        "Attempt to change phase while still processing. " + getName()
      );
    }
    checkForException(); // This should never detect an exception.
    this.phase = phase;
  }


  void addTriple(long[] triple) {
    if (buffer == null) {
      buffer = new long[BUFFER_SIZE][];
      index = 0;
    }
    buffer[index++] = triple;
    if (index == buffer.length) {
      synchronized (this) {
        checkForException();

        // Make sure the Queue doesn't exceed its maximum size.
        if (queue.size() == QUEUE_MAX_BUFFERS) {
          if (logger.isInfoEnabled()) {
            logger.info("Triple write queue full for file: " + file + "  Waiting.");
          }

          // Wait for the Queue to be less than full.
          do {
            try {
              wait();
            } catch (InterruptedException ie) {
              throw new RuntimeException("Exception in " + getName(), ie);
            }
          } while (queue.size() == QUEUE_MAX_BUFFERS);
        }

        // Put the buffer in the queue.
        if (queue.isEmpty()) {
          // Transition from empty to not empty.
          notifyAll();
        }

        queue.addLast(buffer);
      }
      buffer = null;
    }
  }


  public void run() {
    try {
      for (;;) {
        long[][] buffer;
        synchronized (this) {
          if (queue.isEmpty()) {
            // Queue is empty so processing is complete.
            processing = false;
            notifyAll();

            // Wait for more triples to process.
            do {
              wait();
            } while (queue.isEmpty());
          }

          if (queue.size() == QUEUE_MAX_BUFFERS) {
            // Transition from full to not full.
            notifyAll();
          }

          // Get the next triple to process.
          buffer = (long[][])queue.removeFirst();
          processing = true;
        }

        try {
          phase.syncAddTriples(buffer);
        } catch (Throwable t) {
          reportException(t);
        } finally {
          buffer = null;
        }
      }
    } catch (InterruptedException ie) {
      // Allow the thread to terminate silently.
    } catch (Throwable t) {
      logger.error("Unhandled exception in " + getName(), t);
    }

    synchronized (this) {
      threadRunning = false;
      notifyAll();
    }
  }


  private synchronized void reportException(Throwable t) {
    this.t = t;
    logger.error("Exception in TripleWriteThread", t);
    queue.clear();
    processing = false;
    notifyAll();
  }


  private void checkForException() {
    if (this.t != null) {
      Throwable t = this.t;
      this.t = null;
      throw new RuntimeException("Exception in " + getName(), t);
    }

    checkThreadRunning();
  }


  private void checkThreadRunning() {
    if (!threadRunning)
      throw new RuntimeException("Thread not running: " + getName());
  }


  synchronized void abort() {
    buffer = null;
    queue.clear();
    checkThreadRunning();
    try {
      while (processing) {
        wait();
        checkThreadRunning();
      }
    } catch (InterruptedException ex) {
      throw new RuntimeException("Interrupted", ex);
    }

    if (t != null) {
      // Log any exception that occurred in the TripleWriteThread.
      logger.warn("Exception during abort of " + getName(), t);
      t = null;
    }
  }


  synchronized void drain() {
    checkForException();
    if (buffer != null) {
      if (index > 0) {
        long[][] newBuffer = new long[index][];
        System.arraycopy(buffer, 0, newBuffer, 0, index);
        if (queue.isEmpty()) notifyAll();
        queue.addLast(newBuffer);
      }
      buffer = null;
    }
    try {
      while (processing || !queue.isEmpty()) {
        wait();
        checkForException();
      }
    } catch (InterruptedException ex) {
      throw new RuntimeException("Interrupted", ex);
    }
  }


  synchronized void close() {
    try {
      abort();
    } catch (Throwable t) {
      // Log and ignore.
      logger.warn("Exception while shutting down " + getName(), t);
    }
    phase = null;

    // Request the thread to exit.
    interrupt();
  }

}
