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
import java.io.IOException;

// Third party packages
import org.apache.log4j.Logger;


/**
 * Manages the creation of temporary files in a controlled location.
 *
 * @created 2004-12-03
 * @author David Makepeace
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/01/05 04:59:29 $
 * @maintenanceAuthor $Author: newmana $
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class TempDir {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(TempDir.class);
  private static final File systemTempDir = new File(System.getProperty("java.io.tmpdir"));

  private static File tempDir = null;

  public static File getTempDir() {
    return getTempDir(true);
  }

  public static synchronized File getTempDir(boolean failIfNotDir) {
    if (tempDir == null) {
      // Initialize tempDir.
      File dir = new File(systemTempDir, "mulgara_" + System.getProperty("user.name"));
      try {
        dir.mkdirs();
        if (dir.isDirectory()) {
          tempDir = dir;
        } else {
          logger.warn("Could not create temporary directory: " + dir + ". Using: " + systemTempDir);
          tempDir = systemTempDir;
        }
      } catch (SecurityException e) {
        logger.warn("Insufficient permissions to create the standard temporary directory. Using: " + systemTempDir);
        tempDir = systemTempDir;
      }
    }

    if (!tempDir.isDirectory() && failIfNotDir) {
      // The temporary directory has gone away!  This is a fatal error because,
      // for security reasons, we don't want to just revert to using the system
      // temporary directory.
      logger.error("The temporary directory no longer exists!  (" + tempDir + ")");
      System.exit(1);
    }

    return tempDir;
  }


  public static synchronized void setTempDir(File dir) {
    if (dir == null) {
      throw new IllegalArgumentException("dir is null");
    }
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException("dir (" + dir + ") is not a directory");
    }
    tempDir = dir;
  }


  /**
   * Creates an empty file in the current mulgara temp directory.
   */
  public static synchronized File createTempFile(String prefix, String suffix) throws IOException {
    return File.createTempFile(prefix, suffix, getTempDir());
  }
}
