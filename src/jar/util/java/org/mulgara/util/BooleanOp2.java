/**
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
package org.mulgara.util;

/**
 * Functor interface for testing a pair of objects in some arbitrary way.
 * @created Oct 18, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface BooleanOp2 <T1,T2> {

  /**
   * Perform a test on an a pair of objects, returning <code>true</code> if it passes.
   * @param o1 The first object to test.
   * @param o2 The second object to test.
   * @return <code>true</code> for a successful test.
   */
  public boolean test(T1 o1, T2 o2);
}
