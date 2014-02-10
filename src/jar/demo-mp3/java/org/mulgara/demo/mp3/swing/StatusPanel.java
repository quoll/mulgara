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

package org.mulgara.demo.mp3.swing;

// Java 2 standard packages
import javax.swing.*;

// Logging
import org.apache.log4j.Logger;
import java.awt.*;

// Local packages

/**
 * Displays the Application status.
 *
 * @created 2004-12-07
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:07 $
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
public class StatusPanel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(StatusPanel.class.getName());

  /** Used when setting the Cursor */
  private Mp3Application application = null;

  /** Used to show processing activity */
  private JProgressBar statusBar = null;

  /** Displays the status text */
  private JLabel statusLabel = null;

  /**
   * Default constructor
   *
   * @param application Mp3Application
   * @throws Exception
   */
  public StatusPanel(Mp3Application application) throws Exception {
    if (application == null) {
      throw new IllegalArgumentException("Mp3Application is null.");
    }
    this.application = application;
    setup();
  }

  /**
   * Initializes and sets up components.
   */
  private void setup() {

    //instantiate
    statusBar = new JProgressBar();
    statusLabel = new JLabel();

    //initialize
    setLayout(new BorderLayout());
    clear();

    //add
    add(statusBar, BorderLayout.EAST);
    add(statusLabel, BorderLayout.WEST);
  }

  /**
   * Displays the text and begins processing. Sets Cursor to WAIT_CURSOR.
   * @param text String
   */
  public void startProcessing(String text) {
    setText(text);
    setCursor(Cursor.WAIT_CURSOR);
    setProcessing(true);
  }

  /**
   * Clears the Panel and displays the text. Used to indicate success.
   * @param text String
   */
  public void stopProcessing(String text) {
    clear();
    setText(text);
  }

  /**
   * Clears the Panel and displays the text in RED. Used to indicate failure.
   * @param text String
   */
  public void stopProcessingError(String text) {
    clear();
    setErrorText(text);
  }

  /**
   * Resets the display, status and cursor.
   */
  public void clear() {
    setText("");
    setCursor(Cursor.DEFAULT_CURSOR);
    setProcessing(false);
  }

  /**
   * Sets the Progress Bar's processing status.
   * @param processing boolean
   */
  public void setProcessing(boolean processing) {
    /** @todo
     *  - this call must be replaced with a Thread to start/stop the status bar.
     */
    getStatusBar().setIndeterminate(processing);
  }

  /**
   * Sets the text to be displayed.
   * @param text String
   */
  public void setText(String text) {
    getStatusLabel().setForeground(Color.BLACK);
    getStatusLabel().setText(text);
  }

  /**
   * Sets the text to be displayed in RED.
   * @param text String
   */
  public void setErrorText(String text) {
    getStatusLabel().setForeground(Color.RED);
    getStatusLabel().setText(text);
  }

  /**
   * Returns the current Status text (Used to reset text after an action
   * has completed).
   * @return String
   */
  public String getText() {
    return getStatusLabel().getText();
  }

  /**
   * Sets the Application cursor to a predefined Cursor.
   * @param predefinedCursor int
   */
  public void setCursor(int predefinedCursor) {
    application.setCursor(Cursor.getPredefinedCursor(predefinedCursor));
  }

  /**
   * Returns the status bar.
   * @throws IllegalStateException
   * @return JProgressBar
   */
  private JProgressBar getStatusBar() throws IllegalStateException {
    if (statusBar == null) {
      throw new IllegalStateException("Status Bar does not exist.");
    }
    return statusBar;
  }

  /**
   * Returns the status label.
   * @throws IllegalStateException
   * @return JLabel
   */
  private JLabel getStatusLabel() throws IllegalStateException {
    if (statusLabel == null) {
      throw new IllegalStateException("Status Label does not exist.");
    }
    return statusLabel;
  }

}
