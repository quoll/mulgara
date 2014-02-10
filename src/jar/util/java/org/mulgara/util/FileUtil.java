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

package org.mulgara.util;

// Java 2 standard packages
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.*;

/**
 * General file utility methods.
 *
 * @created 2003-11-27
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class FileUtil {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(FileUtil.class.getName());

  /** The size of the buffer used to read and write to files. 0.25MB. */
  private static final int BUFFER_SIZE = 1024 * 256;

  /**
   * Recursively delete a file or directory.
   *
   * This method is not transactional.  If it fails, it may have partially
   * deleted the contents of a file.
   *
   * @param directory the directory to delete, which must exist
   * @return whether the directory was successfully deleted
   */
  public static boolean deleteDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; ++i) {
        if (files[i].isFile()) {
          if (!files[i].delete()) {
            logger.warn("Failed to delete " + files[i]);
          }
        } else {
          deleteDirectory(files[i]);
        }
      }
    }

    return directory.delete();
  }


  /**
   * @see #copyFile(File, File)
   * @param src The path for the source file.
   * @param dest The destination file or directory.
   */
  public static String copyFile(String src, File dest) throws IOException {
    return copyFile(new File(src), dest);
  }


  /**
   * @see #copyFile(File, File)
   * @param src The source file.
   * @param dest The path for the destination file or directory.
   */
  public static String copyFile(File src, String dest) throws IOException {
    return copyFile(src, new File(dest));
  }


  /**
   * @see #copyFile(File, File)
   * @param src The path for the source file.
   * @param dest The path for the destination file or directory.
   */
  public static String copyFile(String src, String dest) throws IOException {
    return copyFile(new File(src), new File(dest));
  }


  /**
   * Copies a file from one place to another. This is similar to the "cp" utility.
   * @param src The source file.
   * @param dest The destination file or directory. If this specifies a directory,
   *        then the filename part of <value>src</value> will be used.
   * @return The path of the file that was written.
   * @throws IOException If there was a problem reading or writing the file.
   */
  public static String copyFile(File src, File dest) throws IOException {
    if (dest.isDirectory()) dest = new File(dest, src.getName());
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(src);
      out = new FileOutputStream(dest);
      byte[] buffer = new byte[BUFFER_SIZE];
      int len;
      while ((len = in.read(buffer)) >= 0) {
        if (len != 0) out.write(buffer, 0, len);
      }
    } finally {
      if (in != null) in.close();
      if (out != null) out.close();
    }
    return dest.getPath();
  }

}
