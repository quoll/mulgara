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

package org.mulgara.store.xa;

import java.io.*;
import java.nio.channels.*;
import java.util.Set;
import java.util.HashSet;


// Third party packages
import org.apache.log4j.Logger;

/**
 * A lock file abstraction.
 *
 * @created 2004-02-16
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:31 $ @maintenanceAuthor $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LockFile {

  /**
   * Logger.
   */
  private final static Logger logger = Logger.getLogger(LockFile.class);

  /** The set of lock files that are in use by this JVM. */
  private static final Set<File> internalLocks = new HashSet<File>();

  /** The lock file name. */
  File lockFile;

  /** The RandomAccessFile for the lock file. */
  RandomAccessFile raf;

  /** The open file channel for the lock file. */
  FileChannel fc;

  /** The exclusive FileLock object. */
  FileLock fl;

  /**
   * Create the lock file if it doesn't already exist and lock it with
   * an exclusive lock.
   *
   * @param lockFile the name of the lock file
   * @throws IOException if the file has already been locked or an I/O error
   * occurs.
   */
  private LockFile(File lockFile) throws IOException {
    this.lockFile = lockFile;

    if (lockFile == null) {
      throw new IllegalArgumentException("Parameter lockFile is null");
    }
    lockInternal(lockFile);

    try {
      raf = new RandomAccessFile(lockFile, "rw");
      fc = raf.getChannel();
      fl = fc.tryLock();
      if (fl == null) throw new IOException("Lock file busy: " + lockFile);
    } finally {
      if (fl == null) {
        // An exception must have occurred.
        release();
      }
    }
  }


  /**
   * Create the lock file if it doesn't already exist and lock it with
   * an exclusive lock.
   *
   * @param lockFileName the name of the lock file
   * @return the FileLock object representing the lock file
   * @throws IOException if the file has already been locked or an I/O error
   * occurs.
   */
  public static LockFile createLockFile(
      String lockFileName
  ) throws IOException {
    return createLockFile(new File(lockFileName));
  }


  /**
   * Create the lock file if it doesn't already exist and lock it with
   * an exclusive lock.
   *
   * @param lockFile the name of the lock file
   * @return the FileLock object representing the lock file
   * @throws IOException if the file has already been locked or an I/O error
   * occurs.
   */
  public static LockFile createLockFile(File lockFile) throws IOException {
    return new LockFile(lockFile);
  }


  /**
   * Adds lockFile to the set of files currently locked by this JVM.
   * @throws IOException if the file has already been locked by a thread in
   * this JVM.
   */
  private static synchronized void lockInternal(
      File lockFile
  ) throws IOException {
    assert lockFile != null;
    if (!internalLocks.add(lockFile)) {
      throw new IOException("Lock file busy (internal): " + lockFile);
    }
  }


  /**
   * Removes lockFile from the set of files currently locked by this JVM.
   */
  private static synchronized void unlockInternal(File lockFile) {
    assert lockFile != null;
    internalLocks.remove(lockFile);
  }


  /**
   * Returns true if the lock is still valid.
   *
   * @return true if the lock is still valid.
   */
  public boolean isValid() {
    return fl != null && fl.isValid();
  }


  /**
   * Releases and removes the lock file.
   */
  public void release() {
    try {
      try {
        if (fl != null) fl.release();
      } finally {
        if (fc != null) fc.close();
        if (raf != null) raf.close();
      }
    } catch (IOException ex) {
      logger.warn("I/O exception while releasing lock file: " + lockFile, ex);
    } finally {
      if (lockFile != null) {
        lockFile.delete();
        unlockInternal(lockFile);
        lockFile = null;
      }
      fl = null;
      fc = null;
      raf = null;
    }
  }


  protected void finalize() throws Throwable {
    try {
      if (lockFile != null) {
        logger.warn("In finalize.  Lock file was not released: " + lockFile);
      }
      release();
    } finally {
      super.finalize();
    }
  }

}
