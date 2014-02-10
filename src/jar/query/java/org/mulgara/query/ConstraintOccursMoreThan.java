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
// import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.rdf.URIReferenceImpl;

/**
 * A constraint in the HAVING clause that only returns values which are more
 * that a given value.
 *
 * @created 2004-08-12
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/05/29 08:32:39 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ConstraintOccursMoreThan extends ConstraintHaving {

  // /** Logger. */
  // private final static Logger logger = Logger.getLogger(ConstraintOccursMoreThan.class);

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 8077840346515987908L;

  /**
   * Create the predicate to be used to indicate equality.
   */
  public static final URIReferenceImpl MULGARA_OCCURS_MORE_THAN =
      new URIReferenceImpl(SpecialPredicates.MULGARA_OCCURS_MORE_THAN);

  /**
   * Constructor.
   *
   * @param e0 The first statement constraint.
   * @param e2 The third statement constraint.
   * @param e3 The fourth (meta) statement constraint.
   */
  public ConstraintOccursMoreThan(ConstraintElement e0, ConstraintElement e2,
      ConstraintElement e3) {
    super(e0, MULGARA_OCCURS_MORE_THAN, e2, e3);
  }


  /**
   * Constructor. The meta node is initialized with the variable <code>$_from</code>.
   * This is only for back-compatibility, and will soon be deprecated.
   *
   * @param e0 The first statement constraint.
   * @param e2 The third statement constraint.
   */
  public ConstraintOccursMoreThan(ConstraintElement e0, ConstraintElement e2) {
    this(e0, e2, Variable.FROM);
  }


  /**
   * Localized factory.  Only for use when the predicate is a localized node.
   *
   * @param e0 The first statement constraint.
   * @param e1 The constraint predicate.  This must be a localized node for MULGARA_IS.
   * @param e2 The third statement constraint.
   * @param e3 The fourth (meta) statement constraint.
   * @return A new ConstraintOccurs with a localized predicate.
   */
  public static ConstraintOccursMoreThan newLocalConstraintOccurs(
      ConstraintElement e0, ConstraintElement e1,
      ConstraintElement e2, ConstraintElement e3
  ) {
    ConstraintOccursMoreThan ci = new ConstraintOccursMoreThan(e0, e2,
        e3 != null ? e3 : Variable.FROM);
    ci.element[1] = e1;
    return ci;
  }


  /**
   * Anyone calling this is responsible for setting {@link #element} themselves.
   *
   */
  protected ConstraintOccursMoreThan() {
    super();
  }

  public boolean isRepeating() {
    return false;
  }
}
