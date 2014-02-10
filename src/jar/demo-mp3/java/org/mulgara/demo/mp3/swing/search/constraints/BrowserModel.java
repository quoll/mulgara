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

package org.mulgara.demo.mp3.swing.search.constraints;

// Java 2 standard packages
import java.util.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.util.*;

// Local packages
import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.id3.*;
import org.mulgara.demo.mp3.swing.*;

/**
 * ConstraintController used by a BrowserPanel.
 *
 * @created 2004-12-13
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
public class BrowserModel implements ConstraintController {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(BrowserModel.class.getName());

  /** Domain used to obtain schema properties */
  private static final URIReference SCHEMA_DOMAIN = Id3Tag.ID3V2;

  /** All the current BrowserConstraintPanels */
  private List panelList = null;

  /** Notified when changes are made */
  private Set listeners = null;

  /** Used to retrieve data */
  private Mp3Controller mp3Controller = null;

  /**
   * Constructor.
   *
   * @param controller Mp3Controller
   */
  public BrowserModel(Mp3Controller controller) {
    if (controller == null) {
      throw new IllegalArgumentException("Mp3Controller is null.");
    }
    panelList = new ArrayList();
    listeners = new HashSet();
    this.mp3Controller = controller;
    //must have at least one panel
  }

  /**
   * Called by the BrowserConstraintPanel when one of it's Properties are
   * selected.
   * @param panel BrowserConstraintPanel
   * @param property URIReference
   */
  public void propertySelected(BrowserConstraintPanel panel,
      URIReference property) {
    try {
      panelSelected(panel);
      // find the values
      ClosableIterator values = getValues(panel, property);
      // update the panel
      panel.setValues(values);
      notifySelection(panel);
      notifyChange();
    }
    catch (Exception exception) {
      ExceptionHandler.handleException(exception);
    }
  }

  /**
   * Called by the BrowserConstraintPanel when one of it's Values are
   * selected.
   * @param panel BrowserConstraintPanel
   * @param value ObjectNode
   */
  public void valueSelected(BrowserConstraintPanel panel, ObjectNode value) {
    try {
      panelSelected(panel);
      notifySelection(panel);
      notifyChange();
    }
    catch (Exception exception) {
      ExceptionHandler.handleException(exception);
    }
  }

  /**
   * Adds a new BrowserConstraintPanel to the end of the List.
   * @throws Exception
   * @return BrowserConstraintPanel The panel that was added
   */
  public BrowserConstraintPanel addNewBrowserConstraintPanel() throws Exception {
    BrowserConstraintPanel constraintPanel = new BrowserConstraintPanel(
        getSchemaProperties(), this);
    panelList.add(constraintPanel);
    notifyChange();
    return constraintPanel;
  }

  /**
   * Removes the selected BrowserConstraintPanel and all subsequent
   * BrowserConstraintPanels.
   *
   * @throws Exception
   * @param panel BrowserConstraintPanel
   */
  public void removeConstraintPanel(BrowserConstraintPanel panel) throws
      Exception {
    if (panel == null) {
      throw new IllegalArgumentException("BrowserConstraintPanel is null");
    }
    //all Panels must be removed after the selected Panel.
    int index = findIndex(panel);
    while (panelList.size() > index) {
      panelList.remove(index);
    }
    notifyChange();
  }

  /**
   * Returns a List of Constraints for the specified BrowserConstraintPanel.
   * Constraints for a BrowserConstraintPanel are the Union of all preceeding
   * constraints.
   * <p>Constraints in a Panel restrict all Panels that follow it
   * @param panel BrowserConstraintPanel
   * @throws Exception
   * @return ConstraintList
   */
  public ConstraintList getConstraints(BrowserConstraintPanel panel) throws
      Exception {
    ConstraintList constraints = new ConstraintList();
    BrowserConstraintPanel current = null;
    int index = findIndex(panel);
    for (int i = 0; i < index; i++) {
      current = (BrowserConstraintPanel) panelList.get(i);
      constraints.addConstraint(current.getProperty(), current.getValue());
    }
    return constraints;
  }

  /**
   * Returns the Union of all BrowserPanel constraints.
   * @throws Exception
   * @return ConstraintList
   */
  public ConstraintList getConstraints() throws Exception {
    ConstraintList constraints = new ConstraintList();
    BrowserConstraintPanel current = null;
    for (int i = 0; i < panelList.size(); i++) {
      current = (BrowserConstraintPanel) panelList.get(i);
      constraints.addConstraint(current.getProperty(), current.getValue());
    }
    return constraints;
  }

  /**
   * Returns all the BrowserConstraintPanels.
   * @return List
   */
  public List getBrowserConstraintPanels() {
    return Collections.unmodifiableList(panelList);
  }

  /**
   * Returns the number of panels the model contains.
   * @return int
   */
  public int getPanelCount() {
    return (panelList != null) ? panelList.size() : 0;
  }

  /**
   * Returns true if the panel is in the List of panels.
   * @param panel BrowserConstraintPanel
   * @throws IllegalArgumentException
   * @return boolean
   */
  public boolean contains(BrowserConstraintPanel panel) throws
      IllegalArgumentException {
    if (panel == null) {
      throw new IllegalArgumentException("BrowserConstraintPanel is null");
    }
    return panelList.contains(panel);
  }

  /**
   * Adds a ConstraintListener to be notified when changes are made.
   * @param listener ConstraintListener
   * @throws IllegalArgumentException
   */
  public void addConstraintListener(ConstraintListener listener) throws
      IllegalArgumentException {
    if (listener == null) {
      throw new IllegalArgumentException("ConstraintListener is null");
    }
    listeners.add(listener);
  }

  /**
   * Returns all the schema properties for the specified domain.
   *
   * @return ClosableIterator
   * @throws Exception
   */
  private ClosableIterator getSchemaProperties() throws Exception {
    return mp3Controller.getSchemaModel().getDomainProperties(SCHEMA_DOMAIN);
  }

  /**
   * Returns a list of values for the specified panel, based on all the
   * accumulated constraints and the selected property.
   *
   * @param panel BrowserConstraintPanel
   * @param property URIReference
   * @throws Exception
   * @return ClosableIterator
   */
  private ClosableIterator getValues(BrowserConstraintPanel panel,
      URIReference property) throws Exception {
    //find the constraints up to the panel
    ConstraintList constraints = getConstraints(panel);
    //add the property constraint
    constraints.addConstraint(property, null);
    return mp3Controller.getMp3Model().find(constraints.getProperties(),
        constraints.getValues());
  }

  /**
   * Notifies all ConstraintListeners that a change has occurred.
   *
   * @throws IllegalStateException
   */
  private void notifyChange() throws IllegalStateException {
    try {
      Iterator iter = listeners.iterator();
      ConstraintListener current = null;
      while (iter.hasNext()) {
        current = (ConstraintListener) iter.next();
        current.constraintsHaveChanged();
      }
    }
    catch (ClassCastException castException) {
      throw new IllegalStateException("Listener Set should only contain " +
          "ConstraintListeners");
    }
  }

  /**
   * Notifies all ConstraintListeners that a panel has been selected.
   *
   * @throws IllegalStateException
   * @param panel BrowserConstraintPanel
   */
  private void notifySelection(BrowserConstraintPanel panel) throws
      IllegalStateException {
    try {
      Iterator iter = listeners.iterator();
      ConstraintListener current = null;
      while (iter.hasNext()) {
        current = (ConstraintListener) iter.next();
        current.panelSelected(panel);
      }
    }
    catch (ClassCastException castException) {
      throw new IllegalStateException("Listener Set should only contain " +
          "ConstraintListeners");
    }
  }

  /**
   * Returns the list index of the Panel. Throws an Exception if the panel is
   * not found.
   * @param panel BrowserConstraintPanel
   * @throws IllegalArgumentException
   * @return int
   */
  private int findIndex(BrowserConstraintPanel panel) throws
      IllegalArgumentException {
    if (!contains(panel)) {
      throw new IllegalArgumentException("List does not contain the " +
          "BrowserConstraintPanel.");
    }
    return panelList.indexOf(panel);
  }

  /**
   * Called when a property or value has been selected in a panel.
   * @param panel BrowserConstraintPanel
   * @throws Exception
   */
  private void panelSelected(BrowserConstraintPanel panel) throws Exception {
    //must remove all panels after the selected panel
    int index = findIndex(panel) + 1;
    if (index < panelList.size()) {
      removeConstraintPanel((BrowserConstraintPanel) panelList.get(index));
    }
  }

}
