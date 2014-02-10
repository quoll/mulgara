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

package org.mulgara.client.jrdf.util;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;

/**
 * Comparator used to compare the ObjectNodes in a Triple.
 *
 * @created 2004-08-16
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:37 $
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
public class ObjectNodeComparator extends NodeComparator<Triple> {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(ObjectNodeComparator.class.getName());

  /**
   * Compares two JRDF Triples.
   *
   * @param triple1 Triple
   * @param triple2 Triple
   * @return int
   */
  public int compare (Triple triple1, Triple triple2) {

    //validate
    if (triple1 == null) {

      throw new IllegalArgumentException("'triple1' argument is null.");
    }

    if (triple2 == null) {

      throw new IllegalArgumentException("'triple2' argument is null.");
    }

    //compare objects
    return super.nodeCompare(triple1.getObject(), triple2.getObject());
  }
}
