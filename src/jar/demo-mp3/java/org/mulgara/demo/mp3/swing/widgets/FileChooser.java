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

package org.mulgara.demo.mp3.swing.widgets;

// Java 2 standard packages
import javax.swing.*;
import java.awt.*;

// Logging
import org.apache.log4j.Logger;
import java.io.*;

// Local packages


/**
 * Utility class for launching a JFileChooser.
 *
 * @created 2004-12-16
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:10 $
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
public abstract class FileChooser {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(FileChooser.class.getName());

  /** Single instance that maintains it's own state (eg. working directory) */
  private static JFileChooser chooser = null;

  /**
   * Default Constructor
   */
  private FileChooser() {
  }

  /**
   * Returns the File selected by the file chooser. Or null if Cancel is
   * selected.
   *
   * @param parent Component
   * @param buttonText String
   * @return File
   */
  public static synchronized File chooseFile(Component parent, String buttonText) {
    File file = null;
    getChooser().setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    int action = getChooser().showDialog(parent, buttonText);
    if (action == JFileChooser.APPROVE_OPTION) {
      file = getChooser().getSelectedFile();
    }
    return file;
  }

  /**
   * Returns the Directory selected by the file chooser. Or null if Cancel is
   * selected.
   *
   * @param parent Component
   * @param buttonText String
   * @return File
   */
  public static synchronized File chooseDirectory(Component parent, String buttonText) {
    File file = null;
    getChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int action = getChooser().showDialog(parent, buttonText);
    if (action == JFileChooser.APPROVE_OPTION) {
      file = getChooser().getSelectedFile();
    }
    return file;
  }

  /**
   * Returns the FileChooser instance, or creates one if it is null.
   * @return JFileChooser
   */
  private static JFileChooser getChooser() {
    if (chooser == null) {
      chooser = new JFileChooser();
    }
    return chooser;
  }

}
