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
 * The copyright to this file is held by:
 *   The Australian Commonwealth Government
 *   Department of Defense
 * Developed by Netymon Pty Ltd
 * under contract 4500430665
 * contributed to the Mulgara Project under the
 *   Mozilla Public License version 1.1
 * per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.xsd;

// Third party packages
import org.apache.log4j.Logger; // Apache Log4J

/**
 * The point representing the upper or lower extent of an interval.
 *
 * @created 2005-05-23
 *
 * @author <a href="mailto:raboczi@itee.uq.edu.au">Simon Raboczi</a>
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/06/09 09:26:20 $ @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class Bound
{
  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(Bound.class.getName());

  /**
   * The numerical edge of the interval.
   */
  private final double value;

  /**
   * Whether the {@link #value} is considered within the interval.
   */
  private final boolean closed;

  /**
   * Sole constructor.
   *
   * @param value  the numerical edge of the interval
   * @param closed whether the <var>value</var> is considered within the
   *   interval
   */
  Bound(double value, boolean closed)
  {
    // Initialize fields
    this.value  = value;
    this.closed = closed;
  }

  /**
   * @param lhs  a {@link Bound}, or <code>null</code>
   * @param rhs  another {@link Bound}, or <code>null</code>
   * @return whether <var>lhs</var> and <var>rhs</var> are equal
   */
  static boolean equals(Bound lhs, Bound rhs)
  {
    return (lhs == null) ? (rhs == null) : lhs.equals(rhs);
  }

  /**
   * @return the numerical edge of the interval
   */
  double getValue()
  {
    return value;
  }

  /**
   * @return whether the {@link #value} is included within the interval
   */
  boolean isClosed()
  {
    return closed;
  }

  //
  // Methods overriding Object
  //

  /**
   * Equality is by value rather than reference.
   */
  public boolean equals(Object object)
  {
    if (object == null) {
      return false;
    }

    if (object == this) {
      return true;
    }

    if (object.getClass() != Bound.class) {
      return false;
    }

    Bound bound = (Bound) object;
    return (value == bound.value) && (closed == bound.closed);
  }

  public int hashCode() {
    return Double.valueOf(value).hashCode() * (closed ? 3 : 1);
  }
}
