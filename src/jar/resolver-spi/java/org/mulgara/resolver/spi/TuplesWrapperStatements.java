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
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.WrappedTuples;

/**
 * A wrapper around {@link Tuples} to turn them into {@link Statements}.
 *
 * @created 2004-05-01
 * @author <a href="http://staff.tucanatech.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class TuplesWrapperStatements extends WrappedTuples implements Statements
{
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(TuplesWrapperStatements.class.getName());

  /** Column index of statement subjects.  */
  private final int subjectColumnIndex;

  /** Column index of statement predicates.  */
  private final int predicateColumnIndex;

  /** Column index of statement objects.  */
  private final int objectColumnIndex;

  //
  // Constructors
  //

  /**
   * Construct {@link Statements} from {@link Tuples} by specifying which
   * columns of each row are the subject, predicate and object.
   *
   * @param subject  the column containing the subject of each row's statement,
   *   never <code>null</code>
   * @param predicate  the column containing the predicate of each row's
   *   statement, never <code>null</code>
   * @param object  the column containing the object of each row's statement,
   *   never <code>null</code>
   * @throws IllegalArgumentException if any parameter is <code>null</code>
   * @throws TuplesException if <var>subject</var>, <var>predicate</var> or
   *   <var>object</var> aren't columns of the <var>tuples</var>
   */
  public TuplesWrapperStatements(Tuples   tuples,
                                 Variable subject,
                                 Variable predicate,
                                 Variable object) throws TuplesException
  {
    super(tuples);
    
    // Validate "subject" parameter
    if (subject == null) {
      throw new IllegalArgumentException("Null \"subject\" parameter");
    }
    
    // Validate "predicate" parameter
    if (predicate == null) {
      throw new IllegalArgumentException("Null \"predicate\" parameter");
    }
    
    // Validate "object" parameter
    if (object == null) {
      throw new IllegalArgumentException("Null \"object\" parameter");
    }
    
    // Initialize fields
    subjectColumnIndex   = tuples.getColumnIndex(subject);
    predicateColumnIndex = tuples.getColumnIndex(predicate);
    objectColumnIndex    = tuples.getColumnIndex(object);

    if (logger.isDebugEnabled()) {
      logger.debug("Constructed");
    }
  }

  //
  // Methods implementing Statements
  //

  public long getSubject() throws TuplesException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Getting subject");
    }
    return getColumnValue(subjectColumnIndex);
  }

  public long getPredicate() throws TuplesException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Getting predicate");
    }
    return getColumnValue(predicateColumnIndex);
  }

  public long getObject() throws TuplesException
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Getting object");
    }
    return getColumnValue(objectColumnIndex);
  }
}
