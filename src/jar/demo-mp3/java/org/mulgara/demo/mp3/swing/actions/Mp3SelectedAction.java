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
import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.demo.mp3.Mp3Controller;
import org.mulgara.demo.mp3.Mp3File;
import org.mulgara.demo.mp3.swing.ExceptionHandler;
import org.mulgara.demo.mp3.swing.id3.TagPanel;
import org.mulgara.demo.mp3.swing.results.ResultPanel;


/**
 * Action executed when an Mp3 is selected in the Results Panel.
 *
 * @created 2004-12-07
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:08 $
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
public class Mp3SelectedAction extends AbstractAction {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(Mp3SelectedAction.class.
      getName());

  /** Where the selected Directories are retrieved */
  private TagPanel tagPanel = null;

  /** Where the results are sent */
  private ResultPanel resultPanel = null;

  /** Does the work */
  private Mp3Controller controller = null;

  /**
   * Default constructor
   *
   * @param name String
   */
  public Mp3SelectedAction(String name) {
    super(name);
  }

  /**
   * Returns the TagPanel set by setTagPanel().
   * @throws IllegalStateException
   * @return TagPanel
   */
  protected TagPanel getTagPanel() throws IllegalStateException {
    if (tagPanel == null) {
      throw new IllegalStateException("TagPanel has not been set.");
    }
    return tagPanel;
  }

  /**
   * Sets the TagPanel used to retireve selected directories.
   *
   * @param tagPanel TagPanel
   * @throws IllegalArgumentException
   */
  public void setTagPanel(TagPanel tagPanel) throws
      IllegalArgumentException {
    if (tagPanel == null) {
      throw new IllegalArgumentException("TagPanel is null.");
    }
    this.tagPanel = tagPanel;
  }

  /**
   * Returns the ResultPanel set by setResultPanel().
   * @throws IllegalStateException
   * @return ResultPanel
   */
  protected ResultPanel getResultPanel() throws IllegalStateException {
    if (resultPanel == null) {
      throw new IllegalStateException("ResultPanel has not been set.");
    }
    return resultPanel;
  }

  /**
   * Sets the destination for results.
   * @param resultPanel ResultPanel
   * @throws IllegalArgumentException
   */
  public void setResultPanel(ResultPanel resultPanel) throws
      IllegalArgumentException {
    if (resultPanel == null) {
      throw new IllegalArgumentException("ResultPanel is null.");
    }
    this.resultPanel = resultPanel;
  }

  /**
   * Returns the Mp3Controller set by setMp3Controller().
   * @throws IllegalStateException
   * @return Mp3Controller
   */
  protected Mp3Controller getMp3Controller() throws IllegalStateException {
    if (controller == null) {
      throw new IllegalStateException("Mp3Controller has not been set.");
    }
    return controller;
  }

  /**
   * Sets the Mp3Controller that does all the work.
   * @param controller Mp3Controller
   * @throws IllegalArgumentException
   */
  public void setMp3Controller(Mp3Controller controller) throws
      IllegalArgumentException {
    if (controller == null) {
      throw new IllegalArgumentException("Mp3Controller is null.");
    }
    this.controller = controller;
  }

  /**
   * actionPerformed
   *
   * @param e ActionEvent
   */
  public void actionPerformed(ActionEvent e) {
    try {
      //show Id3 tag
      Mp3File selected = getResultPanel().getSelectedFile();
      getTagPanel().setMp3File(selected);
      //update playlist
      Iterator playList = getResultPanel().getSelectedFiles();
      getResultPanel().setPlayList(playList);
    } catch (Exception exception) {
      ExceptionHandler.handleException(exception);
    }
  }

}
