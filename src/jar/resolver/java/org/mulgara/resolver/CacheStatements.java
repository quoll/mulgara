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

package org.mulgara.resolver;

// Local packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.Statements;

/**
 * {@link Statements} that might come from a cached copy rather than from the
 * wrapped instance.
 *
 * This class intercepts the exceptions which the {@link Statements} interface
 * throws when it detects that its 
 *
 * @created 2004-12-10
 * @author <a href="http://staff.tucanatech.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.3 $
 * @modified $Date: 2005/01/05 04:58:23 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Tucana
 *   Technology, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class CacheStatements implements Cloneable, Statements
{
  /**
   * Wrapped instance.
   *
   * This could be <code>final</code> exception for the needs of the
   * {@link #clone} method.
   */
  private Statements statements;

  //
  // Constructor
  //

  /**
   * Construct a single localized RDF statement.
   *
   * @param statements  the instance to cache
   */
  public CacheStatements(Statements statements)
  {
    // Validate "statements" parameter
    if (statements == null) {
      throw new IllegalArgumentException("Null \"statements\" parameter");
    }

    // Initialize fields
    this.statements = statements;
  }

  //
  // Methods implementing Cursor (superinterface of Statements)
  //

  public void beforeFirst() throws TuplesException
  {
    statements.beforeFirst();
  }

  public void close() throws TuplesException
  {
    statements.close();
  }

  public int getColumnIndex(Variable variable) throws TuplesException
  {
    return statements.getColumnIndex(variable);
  }

  public int getNumberOfVariables()
  {
    return statements.getNumberOfVariables();
  }

  public long getRowCount() throws TuplesException
  {
    return statements.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException
  {
    return statements.getRowUpperBound();
  }

  public long getRowExpectedCount() throws TuplesException
  {
    return statements.getRowExpectedCount();
  }

  public int getRowCardinality() throws TuplesException
  {
    return statements.getRowCardinality();
  }

  public boolean isEmpty() throws TuplesException
  {
    return statements.isEmpty();
  }
  
  public Variable[] getVariables()
  {
    return statements.getVariables();
  }

  public boolean isUnconstrained() throws TuplesException
  {
    return statements.isUnconstrained();
  }

  public boolean next() throws TuplesException
  {
    return statements.next();
  }

  //
  // Methods implementing Statements
  //

  public long getSubject() throws TuplesException
  {
    return statements.getSubject();
  }

  public long getPredicate() throws TuplesException
  {
    return statements.getPredicate();
  }

  public long getObject() throws TuplesException
  {
    return statements.getObject();
  }

  /**
   * Cloning is always supported.
   */
  public Object clone()
  {
    try {
      CacheStatements cloned = (CacheStatements) super.clone();
      cloned.statements = (Statements) statements.clone();
      return cloned;
    }
    catch (CloneNotSupportedException e) {
      throw new Error(getClass() + " doesn't support cloning", e);
    }
  }

  /**
   * @return the {@link Object#toString} method of the wrapped instance
   */
  public String toString()
  {
    return statements.toString();
  }
}
