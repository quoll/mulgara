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

package org.mulgara.resolver.spi;

// Third party packages
import org.apache.log4j.Logger;      // Apache Log4J

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.store.tuples.EmptyTuples;

/**
 * {@link Resolution} from {@link Statements}.
 *
 * @created 2004-05-05
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:18 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class EmptyResolution extends EmptyTuples implements Resolution {
  /** Logger.  */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(EmptyResolution.class.getName());

  /** The constraint this instance resolves.  */
  private final Constraint constraint;

  /** Precalculated return value fot the {@link #isComplete} method.  */
  private final boolean complete;

  //
  // Constructors
  //

  /**
   * Construct the resolution to a constraint from a set of statings.
   *
   * We assume that the <var>statements</var> are a complete resolution to the
   * model element of the <var>constraint</var> if that model isn't variable.
   *
   * @param constraint  the constraint to resolve, never <code>null</code>
   * @param complete  whether the <var>statements</var> are the complete and
   *   definitive solution to the <var>constraint</var>
   * @throws IllegalArgumentException if the <var>constraint</var> or
   *   <var>statements</var> are <code>null</code>
   */
  public EmptyResolution(Constraint constraint, boolean complete) {
    super();

    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }

    // Initialize fields
    this.complete = complete;
    this.constraint = constraint;
  }

  //
  // Methods implementing Resolution
  //

  public Constraint getConstraint() {
    return constraint;
  }

  /**
   * @return <code>true</code> only if the constraint specifies a model
   */
  public boolean isComplete() {
    return complete;
  }
}
