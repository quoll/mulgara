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
import org.jrdf.graph.*;

// Local packages


/**
 * Represents a List of contraints for querying.
 *
 * @created 2004-12-07
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
public class ConstraintList {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(ConstraintList.class.
      getName());

  /** List of properties */
  private List properties = null;

  /** List of values */
  private List values = null;

  /**
   * Default Constructor.
   */
  public ConstraintList() {
    this(null, null);
  }

  /**
   * Constructor.
   *
   * @param properties List - property constraints
   * @param values List - value constraints (correspond to property constraints)
   * @throws IllegalArgumentException
   */
  public ConstraintList(List properties,
      List values) throws IllegalArgumentException {

    //they both have to either be null or not null.
    if ((properties == null) ^ (values == null)) {
      throw new IllegalArgumentException("Property and Value Lists must BOTH " +
          "be either null or not null. PropertyList: " + properties +
          ", ValueList: " + values);
    }
    //must be the same length if not null
    if ((properties != null)
        && (values != null)
        && (properties.size() != values.size())) {
      throw new IllegalArgumentException("PropertyList and ValueList must be " +
          "the same size.");
    }
    this.properties = properties;
    this.values = values;
  }

  /**
   * Returns an Iterator to the PropertyList, or null if the List does not exist.
   * @return Iterator
   */
  public Iterator getProperties() {
    return (properties == null) ? null : properties.iterator();
  }

  /**
   * Returns an Iterator to the ValueList, or null if the List does not exist.
   * @return Iterator
   */
  public Iterator getValues() {
    return (values == null) ? null : values.iterator();
  }

  /**
   * Adds a new constraint pair to the List.
   * @param property URIReference
   * @param value ObjectNode
   * @throws IllegalArgumentException
   */
  public void addConstraint(URIReference property,
      ObjectNode value) throws IllegalArgumentException {
    if ((properties == null)
        || (values == null)) {
      properties = new ArrayList();
      values = new ArrayList();
    }
    properties.add(property);
    values.add(value);
  }

}
