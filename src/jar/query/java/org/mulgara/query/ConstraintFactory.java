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

package org.mulgara.query;

// Third party packages
import org.jrdf.graph.URIReference;
import org.apache.log4j.*;


/**
 * A class for creating constraints.
 *
 * @created 2004-08-10
 *
 * @author Paul Gearon
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ConstraintFactory {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger logger =
      Logger.getLogger(ConstraintFactory.class.getName());


  /**
   * Factory to create a Constraint implementation from a set of elements.
   *
   * @param e0 The first statement constraint.
   * @param e1 The second statement constraint.
   * @param e2 The third statement constraint.
   * @param e3 The fourth (meta) statement constraint.
   */
  public static Constraint newConstraint(
      ConstraintElement e0, ConstraintElement e1, ConstraintElement e2, ConstraintElement e3
  ) {
    // test the predicate
    if (e1 instanceof URIReference) {
      URIReference predicate = (URIReference)e1;

      // may be a magic predicate
      if (SpecialPredicates.isSpecialPredicate(predicate.getURI())) {

        // check for the equality predicate
        if (predicate.getURI().equals(SpecialPredicates.MULGARA_IS)) {
          return new ConstraintIs(e0, e2, e3);
        }

        // check for the cardinality equality predicate
        if (predicate.getURI().equals(SpecialPredicates.MULGARA_OCCURS)) {
          return new ConstraintOccurs(e0, e2, e3);
        }

        // check for the cardinality equality predicate
        if (predicate.getURI().equals(SpecialPredicates.MULGARA_NOT_OCCURS)) {
          return new ConstraintNotOccurs(e0, e2, e3);
        }

        // check for the cardinality less than predicate
        if (predicate.getURI().equals(SpecialPredicates.MULGARA_OCCURS_LESS_THAN)) {
          return new ConstraintOccursLessThan(e0, e2, e3);
        }

        // check for the cardinality greater than predicate
        if (predicate.getURI().equals(SpecialPredicates.MULGARA_OCCURS_MORE_THAN)) {
          return new ConstraintOccursMoreThan(e0, e2, e3);
        }

        // This is really an error, but for now we'll let it go through as a normal predicate
        logger.error("Special predicate not handled: " + predicate.getURI());
      }
    }

    // create a normal constraint implementation
    return new ConstraintImpl(e0, e1, e2, e3);
  }


  /**
   * Factory to create a Constraint implementation from a set of elements.
   *
   * @param e0 The first statement constraint.
   * @param e1 The second statement constraint.
   * @param e2 The third statement constraint.
   */
  public static Constraint newConstraint(
    ConstraintElement e0, ConstraintElement e1, ConstraintElement e2
  ) {
    return newConstraint(e0, e1, e2, Variable.FROM);
  }

}
