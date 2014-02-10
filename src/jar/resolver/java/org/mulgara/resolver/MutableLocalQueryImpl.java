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
 * Contributor(s):
 *    This file copyright 2006 - Australian Department of Defense
 *    Developed by Netymon Pty Ltd under contract to the Australian
 *    Defense Science and Technology Organisation.
 *
 *    Extracted and modified by Andrae Muys to reflect move to immutable
 *    mutation interface.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver;

// Java 2 standard packages

// Third party packages

// Local packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.MutableLocalQuery;

/**
 * Allows symbolic transformers to notify the transformation logic that a
 * transformation has been required.
 *
 * @created 2007-09-11
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @company <a href="mailto:info@netymon.com">Netymon Pty Ltd</a>
 * @copyright &copy;2006 Australian Department of Defense</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class MutableLocalQueryImpl implements MutableLocalQuery
{
  private ConstraintExpression constraintExpression;

  private boolean modified;

  MutableLocalQueryImpl(Query query) {
    this.constraintExpression = query.getConstraintExpression();
    this.modified = false;
  }

  /**
   * @return whether this instance has been used to mutate the value of the outer class
   */
  boolean isModified() {
    return modified;
  }

  public ConstraintExpression getConstraintExpression() {
    return constraintExpression;
  }

  public void setConstraintExpression(ConstraintExpression constraintExpression) {
    if (constraintExpression == null) {
      throw new IllegalArgumentException("Null \"constraintExpression\" parameter");
    }

    if (constraintExpression != this.constraintExpression) {
      this.constraintExpression = constraintExpression;
      modified = true;
    }
  }

  public String toString() {
    return getClass().toString() + ":=:" + constraintExpression.toString();
  }
}
