/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.store.stringpool.xa;

import java.math.BigDecimal;

/**
 * Used to indicate that the interface is a number, and to do by-value inequality.
 *
 * @created Mar 2, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface SPNumber {

  /**
   * Compare the value of current object to a BigDecimal.
   * @param n The BigInteger to compare to.
   * @return -1 if the value of this object is smaller, +1 if larger, 0 if equal.
   */
  public int numericalCompareTo(BigDecimal n);

  /**
   * Compare the value of current object to a BigInteger.
   * @param d The double to compare to.
   * @return -1 if the value of this object is smaller, +1 if larger, 0 if equal.
   */
  public int numericalCompareTo(double d);

  /**
   * Compare the current object to a long.
   * @param l The long to compare to.
   * @return -1 if the value of this object is smaller, +1 if larger, 0 if equal.
   */
  public int numericalCompareTo(long l);
}
