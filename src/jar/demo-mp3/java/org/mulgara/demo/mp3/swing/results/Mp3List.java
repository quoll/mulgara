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
import javax.swing.event.*;
import java.util.*;

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.swing.ExceptionHandler;
import org.mulgara.demo.mp3.swing.results.list.Mp3FileListRenderer;


/**
 * List of Mp3's to display.
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
public class Mp3List extends JList {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(Mp3List.class.getName());

  /** Graph used to store list items */
  private DefaultListModel model = null;

  /** Renders the Mp3Files */
  private Mp3FileListRenderer renderer = null;

  /** Action to be executed when an Mp3 is selected from the list */
  private Action mp3SelectAction = null;


  /**
   * Default constructor
   */
  public Mp3List() {
    setup();
  }

  /**
   * Initializes and sets up componentes
   */
  private void setup () {

    //instantiate
    model = new DefaultListModel();
    renderer = new Mp3FileListRenderer();

    //initialize
    addListener();

    //add
    setModel(model);
    setCellRenderer(renderer);
  }

  /**
   * Removes all components and re-draws
   */
  private void redraw() {
    invalidate();
    removeAll();
    setup();
    repaint();
    revalidate();
  }

  /**
   * Lists all the Mp3's from the Iterator.
   * @param mp3s Mp3Iterator
   * @throws Exception
   */
  public void display(Mp3Iterator mp3s) throws Exception {
    if (mp3s == null) {
      throw new IllegalArgumentException("Mp3Iterator is null");
    }

    model.removeAllElements();
    Mp3File current = null;
    while (mp3s.hasNext()) {
      current = mp3s.nextMp3();
      model.addElement(current);
    }
  }

  /**
   * Returns the currently selected Mp3File (first selected if multiple
   * selections are made).
   * @throws Exception
   * @return Mp3File
   */
  public Mp3File getSelectedFile() throws Exception {
    return (Mp3File) getSelectedValue();
  }

  /**
   * Returns all the currently selected Mp3Files.
   * @throws Exception
   * @return Mp3File
   */
  public Iterator getSelectedFiles() throws Exception {
    List playList = new ArrayList();
    Object [] values = getSelectedValues();
    if (values != null) {
      playList = Arrays.asList(values);
    }
    return playList.iterator();
  }

  /**
   * Sets the action to be executed when a mp3 is selected from the results.
   * @param action Action
   * @throws IllegalArgumentException
   */
  public void setMp3SelectAction(Action action) throws IllegalArgumentException {
    if (action == null) {
      throw new IllegalArgumentException("Action is null");
    }
    mp3SelectAction = action;
  }

  /**
   * Adds a Listener to the Mp3List
   */
  private void addListener() {
    addListSelectionListener(new ListSelectionListener() {
      /** display selected file info
       * @param e ListSelectionEvent
       */
      public void valueChanged(ListSelectionEvent e) {
        try {
          mp3SelectAction.actionPerformed(null);
        } catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
  }

}
