/*
 * Copyright 2011 Revelytix, Inc.
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

package org.mulgara.util.io;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.log4j.Logger;

import sun.misc.Cleaner;

/**
 * Common utilities for working with memory-mapped files. The main purpose of these
 * utilities is to provide a central place for workarounds for known issues with
 * memory mapping under Windows.
 * 
 * @author Alex Hall
 */
public class MappingUtil {

  private static final Logger logger = Logger.getLogger(MappingUtil.class);
  
  /** System property to determine operating system. */
  private static final String OS_PROP = "os.name";
  
  /** Only look up the operating system once and then cache it. */
  private static final boolean IS_WINDOWS;
  static {
    String os = System.getProperty(OS_PROP);
    IS_WINDOWS = (os != null && os.toLowerCase().indexOf("win") >= 0);
  }
  
  /** The maximum number of attempts for truncating a mapped file. */
  private static final int MAX_RETRIES = 10;
  
  /** Determines whether we are running under Windows. */
  public static boolean isWindows() {
    return IS_WINDOWS;
  }
  
  /**
   * Truncate the given file to the given size. This method will re-attempt
   * the truncation several times, to account for the fact that the truncation fails
   * in Windows if there exist mapped buffers which have not yet been cleaned up
   * by the garbage collector.
   * 
   * All calls in Mulgara to FileChannel.truncate() should pass through this method.
   * 
   * This method does nothing if the given size is greater than or equal to the current
   * size of the file.
   * 
   * @param fc The file channel to truncate.
   * @param size The new size after truncation.
   * @throws IOException if the truncation still failed after attempting to clean up system resources.
   */
  public static void truncate(FileChannel fc, long size) throws IOException {
    long s = fc.size();
    if (s <= size) return;
    int retries = MAX_RETRIES;
    for (;;) {
      try {
        fc.truncate(size);
        break;
      } catch (IOException e) {
        // on failure, attempt to clean up all resources keeping the file open
        if (retries-- == 0) {
          logger.error("Unable to truncate mapped file of size " + s + " to size " + size, e);
          throw e;
        }
        systemCleanup();
      }
    }
  }

  /**
   * Prompt the system to clean up outstanding objects, thereby releasing unique resources
   * for re-use. This is required for MappedByteBuffers as the Java NIO cannot release the
   * resources explicitly without putting a guard on every access (thereby compromising the
   * speed advantages of memory mapping) or allowing continuing access to memory that is
   * no longer accessible. Therefore, the resources must be released implicitly (by setting
   * all references null) and then calling this code to prompt the system to clean the
   * resources up. Depending on the host OS, this method may need to be called several times.
   * Linux typically only requires 1 or 2 invocations, while Windows regularly needs more than
   * 2 and can require >6.
   */
  public static void systemCleanup() {
    System.gc();
    try { Thread.sleep(100); } catch (InterruptedException ie) { }
    System.runFinalization();
  }

  /**
   * Releases the mapped byte buffer, attempting to pre-emptively clean up the associated
   * mapping under Windows.
   * @param buffer A mapped byte buffer.
   */
  public static void release(MappedByteBuffer buffer) {
    if (isWindows()) clean(buffer);
  }
  
  /**
   * Releases an array of mapped byte buffers, setting the references to null to allow
   * them to be garbage collected.
   * @param buffers An array of buffers.
   */
  public static void release(MappedByteBuffer[] buffers) {
    if (buffers == null) return;
    for (int i = 0; i < buffers.length; i++) {
      release(buffers[i]);
      buffers[i] = null;
    }
  }
  
  /**
   * Attempts to pre-emptively invoke the cleaner method on the given object instead
   * of waiting for the garbage collector to do it. We do this under Windows because
   * errors can result if a mapped byte buffer is cleaned after its associated file
   * is closed, truncated, deleted, etc.
   * @param buffer The buffer to release.
   */
  private static void clean(final Object buffer) {
    if (buffer != null) {
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
        public Object run() {
          try {
            Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
            getCleanerMethod.setAccessible(true);
            Cleaner cleaner = (Cleaner)getCleanerMethod.invoke(buffer, new Object[0]);
            cleaner.clean();
          } catch (Exception e) {
            logger.warn("Error cleaning buffer", e);
          }
          return null;
        }
      });
    }
  }

}
