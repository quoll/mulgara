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

// JRDF
import org.jrdf.graph.*;
import java.util.*;

// Local packages


/**
 * Methods for managing multiple BrowserConstraintPanels.
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
public interface ConstraintController {

  /**
   * Called by the BrowserConstraintPanel when one of it's Properties are
   * selected.
   * @param panel BrowserConstraintPanel
   * @param property URIReference
   */
  public void propertySelected(BrowserConstraintPanel panel,
      URIReference property);

  /**
   * Called by the BrowserConstraintPanel when one of it's Values are
   * selected.
   * @param panel BrowserConstraintPanel
   * @param value ObjectNode
   */
  public void valueSelected(BrowserConstraintPanel panel, ObjectNode value);

  /**
   * Adds a new BrowserConstraintPanel to the end.
   *
   * @throws Exception
   * @return BrowserConstraintPanel The panel that was added
   */
  public BrowserConstraintPanel addNewBrowserConstraintPanel() throws Exception;

  /**
   * Removes the selected BrowserConstraintPanel and all subsequent
   * BrowserConstraintPanels.
   *
   * @throws Exception
   * @param panel BrowserConstraintPanel
   */
  public void removeConstraintPanel(BrowserConstraintPanel panel) throws
      Exception;

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
      Exception;

  /**
   * Returns the Union of all BrowserPanel constraints.
   * @throws Exception
   * @return ConstraintList
   */
  public ConstraintList getConstraints() throws Exception ;

  /**
   * Returns all the BrowserConstraintPanels.
   * @return List
   */
  public List getBrowserConstraintPanels();

  /**
   * Returns the number of panels the model contains.
   * @return int
   */
  public int getPanelCount();

  /**
   * Returns true if the panel is in the List of panels.
   * @param panel BrowserConstraintPanel
   * @throws IllegalArgumentException
   * @return boolean
   */
  public boolean contains(BrowserConstraintPanel panel) throws
      IllegalArgumentException;

  /**
   * Adds a ConstraintListener to be notified when changes are made.
   * @param listener ConstraintListener
   * @throws IllegalArgumentException
   */
  public void addConstraintListener(ConstraintListener listener) throws
      IllegalArgumentException;

}
