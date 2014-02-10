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

package org.mulgara.resolver.filesystem;


// Third party packages
import org.apache.log4j.Logger;      // Apache Log4J

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.WrappedTuples;

/**
 * Resolution of a query against the file system.
 *
 * @created 2004-11-17
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/02 20:07:56 $ @maintenanceAuthor $Author: raboczi $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileSystemResolution extends WrappedTuples implements Resolution {

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(FileSystemResolution.class);

  /** The constraint this instance resolves */
  private final Constraint constraint;

  /**
   * Construct the resolution to a constraint from a set of statings.
   *
   * @param constraint  the constraint to resolver, never <code>null</code>
   * @param result  the set of statings
   * @throws IllegalArgumentException if the <var>constraint</var> or
   *   <var>result</var> are <code>null</code>
   */
  FileSystemResolution(Constraint constraint, Tuples result) throws TuplesException {

    super(result);

    // Validate parameters
    if (constraint == null) {

      throw new IllegalArgumentException( "Null 'constraint' parameter");
    } else if (result == null) {

      throw new IllegalArgumentException( "Null 'result' parameter");
    }

    // Initialize fields
    this.constraint = constraint;
  }

  public Constraint getConstraint() {

    return constraint;
  }

  public boolean isComplete() {

    return false;
  }
}
