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

package org.mulgara.demo.mp3.swing.results;

// Java 2 standard packages
import javax.swing.*;
import java.awt.*;
import java.util.*;

// Logging
import org.apache.log4j.Logger;

// JLayer

// Local packages
import org.mulgara.demo.mp3.Mp3File;
import org.mulgara.demo.mp3.Mp3Iterator;
import org.mulgara.demo.mp3.playback.*;


/**
 * Panel for displaying the results of a search
 *
 * @created 2004-12-07
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:09 $
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
public class ResultPanel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(ResultPanel.class.getName());

  /** List of all Mp3's being displayed */
  private Mp3List mp3List = null;

  /** Plays the Selected mp3(s) */
  private Mp3PlayerPanel playerPanel = null;

  /**
   * Default constructor
   *
   * @throws Exception
   */
  public ResultPanel() throws Exception {

    setup();
  }

  /**
   * Initialized and sets up components.
   *
   * @throws Exception
   */
  private void setup() throws Exception {

    //instantiate
    mp3List = new Mp3List();
    playerPanel = (playerPanel == null) ? newPlayerPanel() : playerPanel;

    //initialize
    setLayout(new BorderLayout());

    //add
    add(new JScrollPane(getCenterPanel()), BorderLayout.CENTER);
    add(getSouthPanel(), BorderLayout.SOUTH);
  }

  /**
   * Returns the Middle Panel.
   * @return JPanel
   */
  private JPanel getCenterPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(getMp3List(), BorderLayout.CENTER);
    return panel;
  }

  /**
   * REturns the Bottom Panel.
   * @return JPanel
   */
  private JPanel getSouthPanel() {
    return playerPanel;
  }

  /**
   * Retrieves the currently selected Item from the Mp3List.
   * @throws Exception
   * @return Mp3File
   */
  public Mp3File getSelectedFile() throws Exception {

    return getMp3List().getSelectedFile();
  }

  /**
   * Returns all the currently selected Mp3Files.
   * @throws Exception
   * @return Iterator
   */
  public Iterator getSelectedFiles() throws Exception {
    return getMp3List().getSelectedFiles();
  }

  /**
   * Sets the action to be executed when a mp3 is selected from the results.
   * @param action Action
   * @throws IllegalArgumentException
   */
  public void setMp3SelectAction(Action action) throws IllegalArgumentException {
    getMp3List().setMp3SelectAction(action);
  }

  /**
   * Tells the Mp3List to display the results.
   * @param mp3s Mp3Iterator
   * @throws Exception
   */
  public void display(Mp3Iterator mp3s) throws Exception {
    getMp3List().display(mp3s);
  }

  /**
   * Returns the Mp3List
   * @throws IllegalStateException
   * @return Mp3List
   */
  private Mp3List getMp3List() throws IllegalStateException {
    if (mp3List == null) {
      throw new IllegalStateException("Mp3List does not exist.");
    }
    return mp3List;
  }

  /**
   * Sets the Mp3 to be played (is play selected).
   * @param playList Iterator
   * @throws IllegalArgumentException
   */
  public void setPlayList(Iterator playList) throws IllegalArgumentException {
    getMp3PlayerPanel().setPlayList(playList);
  }

  /**
   * Creates a new Mp3PlayerPanel and returns.
   * @throws Exception
   * @return Mp3PlayerPanel
   */
  private Mp3PlayerPanel newPlayerPanel() throws Exception {
    if (playerPanel != null) {
      playerPanel.close();
    }
    playerPanel = new Mp3PlayerPanel();
    return playerPanel;
  }

  /**
   * Returns the Mp3PlayerPanel (playerPanel).
   * @throws IllegalStateException
   * @return Mp3PlayerPanel
   */
  private Mp3PlayerPanel getMp3PlayerPanel() throws IllegalStateException {
    if (playerPanel == null) {
      throw new IllegalStateException("Mp3PlayerPanel does not exist.");
    }
    return playerPanel;
  }

  /**
   * Adds the Listener to the Mp3PlayerPanel.
   * @param listener PlaybackThreadListener
   * @throws IllegalArgumentException
   */
  public void addPlaybackListener(PlaybackThreadListener listener) throws
      IllegalArgumentException {
    getMp3PlayerPanel().addPlaybackListener(listener);
  }

  /**
   * Kills any current playback.
   * @throws Exception
   */
  public void stopPlayback() throws Exception {
    getMp3PlayerPanel().stopPlayback();
  }

}
