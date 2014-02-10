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
import javax.swing.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.util.*;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.demo.mp3.swing.widgets.NodeListRenderer;

import java.util.*;


/**
 * Dropdown menu that contains a List of Properties/Predicates to choose from.
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
public class PropertyComboBox extends JComboBox {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(PropertyComboBox.class.
      getName());

  /** The properties to be listed */
  private ClosableIterator properties = null;

  /**
   * Constructor.
   *
   * @param properties ClosableIterator containing Triples
   * (Subjects are properties).
   * @throws Exception
   */
  public PropertyComboBox(ClosableIterator properties) throws Exception {
    if (properties == null) {
      throw new IllegalArgumentException("'properties' are null");
    }
    this.properties = properties;
    setup();
  }

  /**
   * Initilizes and sets up components.
   *
   * @throws Exception
   */
  private void setup() throws Exception {

    //initialize
    removeAllItems();
    setPropertyRenderer();
    populate();
  }

  /**
   * Returns the selected property from the list.
   * @throws IllegalStateException
   * @return URIReference
   */
  public URIReference getSelected() throws IllegalStateException {
    try {
      return (URIReference) getSelectedItem();
    } catch (ClassCastException castException) {
      throw new IllegalStateException("List should only contain URIReferences.");
    }
  }

  /**
   * Adds all properties from the Iterator.
   * @throws Exception
   */
  private void populate() throws Exception {
    try {
      //always add a null Node for wildcard
      addItem(null);
      List nodeList = new ArrayList();
      Triple current = null;
      while (properties.hasNext()) {
        current = (Triple) properties.next();
        nodeList.add(current.getSubject());
      }
      //sort
      Collections.sort(nodeList);
      Iterator iter = nodeList.iterator();
      while (iter.hasNext()) {
        addItem(iter.next());
      }
    } catch (ClassCastException castException) {
      throw new IllegalStateException("Properties should contain Triples.");
    }
  }

  /**
   * Sets a Renderer that can Render Properties (PredicateNodes).
   */
  private void setPropertyRenderer() {
    setRenderer(new NodeListRenderer());
  }

}
