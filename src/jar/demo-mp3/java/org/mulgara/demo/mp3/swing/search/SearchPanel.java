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

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.demo.mp3.Mp3Controller;
import org.mulgara.demo.mp3.swing.IconLoader;
import org.mulgara.demo.mp3.swing.search.constraints.*;
import org.mulgara.demo.mp3.swing.widgets.*;

/**
 * Contains/manages search-related components.
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
public class SearchPanel extends JPanel implements ConstraintListener {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(SearchPanel.class.
      getName());

  /** Size of the JSplitPane Dividers */
  public static final int DIVIDER_SIZE = 2;

  /** Ratio used by JSplitPanes */
  public static final float DIVIDER_RATIO = 0.3F;

  /** Used to add/edit search constraints in a Browser UI */
  private BrowserPanel browserPanel = null;

  /** Action executed by the search button */
  private Action searchAction = null;

  /** Required by the BrowserPanel */
  private Mp3Controller controller = null;

  /**
   * Constructor
   *
   * @throws Exception
   * @param controller Mp3Controller
   */
  public SearchPanel(Mp3Controller controller) throws Exception {
    if (controller == null) {
      throw new IllegalArgumentException("Mp3Controller is null");
    }
    this.controller = controller;
    setup();
  }

  /**
   * Sets the Action that is executed by the Search Button.
   * @param action Action
   * @throws IllegalArgumentException
   */
  public void setSearchAction(Action action) throws IllegalArgumentException {
    if (action == null) {
      throw new IllegalArgumentException("Action is null");
    }
    searchAction = action;
  }

  /**
   * Initializes and sets up components.
   *
   * @throws Exception
   */
  private void setup() throws Exception {

    //instantiate
    browserPanel = (browserPanel == null)
        ? new BrowserPanel(getMp3Controller())
        : browserPanel;

    //initialize
    setLayout(new BorderLayout());
    browserPanel.addConstraintListener(this);

    //add
    add(getCenterPanel(), BorderLayout.CENTER);
    add(getSouthPanel(), BorderLayout.SOUTH);
  }

  /**
   * Returns the Middle panel.
   * @return JPanel
   */
  private JPanel getCenterPanel() {
    return browserPanel;
  }

  /**
   * Returns the Bottom panel.
   * @return JPanel
   */
  private JPanel getSouthPanel() {
    JPanel panel = new JPanel();
    return panel;
  }

  /**
   * Returns the List of search constraints.
   *
   * @return ConstraintList
   * @throws Exception
   */
  public ConstraintList getConstraints() throws Exception {
    return browserPanel.getConstraints();
  }

  /**
   * Adds a new BrowserConstraintPanel to the BrowserPanel.
   * @throws Exception
   */
  public void addNewConstraint() throws Exception {
    browserPanel.addNewBrowserConstraintPanel();
  }

  /**
   * Removes all selected BrowserConstraintPanels from the BrowserPanel.
   * @throws Exception
   */
  public void removeSelectedConstraints() throws Exception {
    browserPanel.removeSelectedPanels();
  }

  /**
   * Removes all Constraints from the BrowserPanel
   * @throws Exception
   */
  public void clearConstraints() throws Exception {
    browserPanel.clearConstraints();
  }

  /**
   * Returns the controller used by the BrowserPanel.
   * @throws IllegalStateException
   * @return Mp3Controller
   */
  private Mp3Controller getMp3Controller() throws IllegalStateException {
    if (controller == null) {
      throw new IllegalStateException("Mp3Controller has not been set.");
    }
    return controller;
  }

  /**
   * Re-Search with the new constraints.
   */
  public void constraintsHaveChanged () {
    getSearchAction().actionPerformed(null);
  }

  /**
   * Updates/reloads the mp3s based on constraints
   * @throws Exception
   */
  public void updateMp3s() throws Exception {
    constraintsHaveChanged();
  }

  /**
   * No-op
   * @param panel BrowserConstraintPanel
   */
  public void panelSelected(BrowserConstraintPanel panel) {}

  /**
   * Returns the Action used to search for mp3s
   * @throws IllegalStateException
   * @return Action
   */
  private Action getSearchAction() throws IllegalStateException {
    if (searchAction == null) {
      throw new IllegalStateException("Search Action has not been set.");
    }
    return searchAction;
  }

}
