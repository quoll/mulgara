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

package org.mulgara.demo.mp3.swing.actions;

// Java 2 standard packages
import java.awt.event.ActionEvent;

// Logging
import org.apache.log4j.Logger;

// Local packages
import java.io.*;

import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.swing.*;
import org.mulgara.demo.mp3.swing.widgets.*;

/**
 * Contains a List of Id3 constraints for searching.
 *
 * @created 2004-12-13
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.4 $
 *
 * @modified $Date: 2005/04/20 19:02:51 $
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
public class LoadMp3sAction extends AbstractMp3Action {

  /**
   * 
   */
  private static final long serialVersionUID = -4140158890813350562L;
  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(LoadMp3sAction.class.
      getName());

  /**
   * Default constructor
   *
   * @param name String
   */
  public LoadMp3sAction(String name) {
    super(name);
  }

  /**
   * Searches the selected directories in the filesystem for Mp3 files.
   * @param e ActionEvent
   */
  public synchronized void actionPerformed(ActionEvent e) {
    try {
      Mp3Controller controller = getMp3Controller();
      //launch filechooser and import selected directory
      FileSystemModel fsModel = controller.getFileSystemModel();
      fsModel.clear();
      File file = FileChooser.chooseDirectory(super.getSearchPanel(), "Import");
      if (file != null) {
        getStatusPanel().startProcessing("Loading... " + file);
        fsModel.includeDirectory(file.toURI().toURL());
        Mp3Model mp3Model = controller.getMp3Model();
        mp3Model.setMp3ModelListener(getMp3ModelListener());
        mp3Model.loadMp3s(fsModel);
      }
      //underlying model/data has changed
      getSearchPanel().constraintsHaveChanged();
      getStatusPanel().stopProcessing("Successfully loaded: " + file);
    }
    catch (Exception exception) {
      getStatusPanel().stopProcessingError("Error(s) occurred while loading Mp3s");
      ExceptionHandler.handleException(exception);
    }
  }
  
  /**
   * Returns a new Mp3ModelListener that logs the exception.
   * @return
   */
  private Mp3ModelListener getMp3ModelListener() {
      return new Mp3ModelListener() {
        public void loadExceptionOccurred(Mp3File file, Exception e) {
            //just print it
            log.warn("An exception occurred while loading: " + file, e);
        }         
      };
  }

}

