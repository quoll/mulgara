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

package org.mulgara.demo.mp3.swing.search;

// Java 2 standard packages
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.swing.*;
import org.mulgara.demo.mp3.swing.search.constraints.*;
import org.mulgara.demo.mp3.swing.widgets.*;


/**
 * Contains a List of Id3 constraints for searching.
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
public class BrowserPanel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(BrowserPanel.class.
      getName());

  /** Manages and controls multiple BrowserConstraintPanels */
  private ConstraintController model = null;

  /** Interface to the Triplestore */
  private Mp3Controller mp3Controller = null;

  /** Used to detect changes in the model */
  private ConstraintListener listener = null;

  /** The currently selected BrowserConstraintPanels */
  private List selectedPanels = null;


  /**
   * Constructor
   *
   * @throws Exception
   * @param controller Mp3Controller
   */
  public BrowserPanel(Mp3Controller controller) throws Exception {
    if (controller == null) {
      throw new IllegalArgumentException("Mp3Controller is null");
    }
    mp3Controller = controller;
    selectedPanels = new ArrayList();
    setup();
  }

  /**
   * Initializes and sets up components.
   * @throws Exception
   */
  private void setup() throws Exception {

    //instantiate
    model = getController();

    //initialize
    setLayout(new BorderLayout());
    if (model.getPanelCount() <= 0) {
      addNewBrowserConstraintPanel();
    }

    //add
    add(getNorthPanel(), BorderLayout.NORTH);
    add(new JScrollPane(getCenterPanel(),
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
  }

  /**
   * Removes all components and redraws the component.
   * @throws Exception
   */
  private void redraw() throws Exception {
    invalidate();
    removeAll();
    setup();
    repaint();
    revalidate();
  }

  /**
   * Returns the Top panel.
   * @return JPanel
   */
  private JPanel getNorthPanel() {
    Icon icon = IconLoader.getIcon(IconLoader.SEARCH_24);
    return new HeadingPanel("Browse", icon, HeadingPanel.MEDIUM);
  }

  /**
   * Returns the Middle panel.
   * @throws Exception
   * @return JPanel
   */
  private JPanel getCenterPanel() throws Exception {
    return getConstraintPanel();
  }

  /**
   * Returns the List of search constraints.
   *
   * @return ConstraintList
   * @throws Exception
   */
  public ConstraintList getConstraints() throws Exception {
    return model.getConstraints();
  }

  /**
   * Resets the ConstraintController and refreshes.
   *
   * @throws Exception
   */
  public void clearConstraints() throws Exception {
    model = new BrowserModel(getMp3Controller());
    addConstraintListener();
    redraw();
  }

  /**
   * Returns all the currently selected panels.
   * @return List
   */
  public List getSelectedPanels() {
    return Collections.unmodifiableList(selectedPanels);
  }

  /**
   * Adds a new BrowserConstraintPanel to the end.
   *
   * @throws Exception
   * @return BrowserConstraintPanel
   */
  public BrowserConstraintPanel addNewBrowserConstraintPanel() throws Exception {
    BrowserConstraintPanel panel = getController().addNewBrowserConstraintPanel();
    panel.setParent(this);
    redraw();
    return panel;
  }

  /**
   * Removes all selected BrowserConstraintPanels from the list.
   * @throws Exception
   */
  public void removeSelectedPanels() throws Exception {
    List panels = getSelectedPanels();
    Iterator iter = panels.iterator();
    BrowserConstraintPanel current = null;
    while (iter.hasNext()) {
      current = (BrowserConstraintPanel) iter.next();
      getController().removeConstraintPanel(current);
    }
    //reset list
    selectedPanels = new ArrayList();
  }

  /**
   * Removes the BrowserConstraintPanel. Throws an Exception if the Browser does
   * not contain the panel.
   *
   * @throws Exception
   * @param panel BrowserConstraintPanel
   */
  public void removePanel(BrowserConstraintPanel panel) throws Exception {
    if (selectedPanels.contains(panel)) {
      selectedPanels.remove(panel);
    }
    getController().removeConstraintPanel(panel);
  }


  /**
   * Returns the Mp3Controller.
   * @throws IllegalStateException
   * @return Mp3Controller
   */
  private Mp3Controller getMp3Controller() throws IllegalStateException {
    if (mp3Controller == null) {
      throw new IllegalStateException("Mp3Controller has not been set");
    }
    return mp3Controller;
  }

  /**
   * Returns a Panel containing all the BrowserConstraintPanels
   *
   * @throws Exception
   * @return JPanel
   */
  private JPanel getConstraintPanel() throws Exception {
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.LEFT));
    List panels = getController().getBrowserConstraintPanels();
    Iterator iter = panels.iterator();
    BrowserConstraintPanel current = null;
    while (iter.hasNext()) {
      current = (BrowserConstraintPanel) iter.next();
      panel.add(current);
    }
    return panel;
  }

  /**
   * Adds the ConstraintListener to the ConstraintController.
   */
  private void addConstraintListener() {
    getController().addConstraintListener(getConstraintListener());
  }

  /**
   * Returns the ConstraintListener for detecting changes to the
   * ConstraintController.
   * @return ConstraintListener
   */
  private ConstraintListener getConstraintListener() {
    if (listener == null) {
      listener = new ConstraintListener() {
        /** Controller has changed */
        public void constraintsHaveChanged() {
          try {
            redraw();
          }
          catch (Exception exception) {
            ExceptionHandler.handleException(exception);
          }
        }
        /** A panel has been selected
         * @param panel BrowserConstraintPanel
         */
        public void panelSelected(BrowserConstraintPanel panel) {
          if (selectedPanels == null) {
            selectedPanels = new ArrayList();
          }
          selectedPanels.clear();
          selectedPanels.add(panel);
        }
      };
    }
    return listener;
  }

  /**
   * Registers the Constraint Listener with the Graph.
   * @param listener ConstraintListener
   * @throws IllegalArgumentException
   */
  public void addConstraintListener(ConstraintListener listener) throws
      IllegalArgumentException {
    getController().addConstraintListener(listener);
  }

  /**
   * Returns the Controller that is used to obtain mp3 information.
   * @return ConstraintController
   */
  private ConstraintController getController() {
    if (model == null) {
      model = new BrowserModel(getMp3Controller());
      model.addConstraintListener(getConstraintListener());
    }
    return model;
  }

}
