/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.query;

// Java 2 standard packages
import java.util.*;

/**
 * A constraint expression composed of the difference between two
 * subexpressions.
 *
 * @created 2005-03-08
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @copyright &copy; 2005 <A href="mailto:pgearon@users.sourceforge.net">Paul Gearon</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ConstraintDifference extends ConstraintBinaryOperation {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 7601600010765365077L;

  //
  // Constructor
  //

  /**
   * Construct a constraint difference.
   *
   * @param minuend a non-<code>null</code> constraint expression
   * @param subtrahend another non-<code>null</code> constraint expression
   */
  public ConstraintDifference(ConstraintExpression minuend,
      ConstraintExpression subtrahend) {
    super(minuend, subtrahend);
  }

  /**
   * Gets the Filtered attribute of the ConstraintDifference object
   *
   * @return The Filtered value
   */
  public ConstraintDifference getFiltered() {

    List<ConstraintExpression> elements = new ArrayList<ConstraintExpression>(this.getElements());
    filter(elements);

    assert elements.size() == 2;
    return new ConstraintDifference(elements.get(0), elements.get(1));
  }


  /**
   * Gets the Name attribute of the ConstraintDifference object
   *
   * @return The Name value
   */
  String getName() {
    return " minus ";
  }

  /**
   * Minus is not an associative operation.
   * @return <code>false</code> since this operation is not associative.
   */
  public boolean isAssociative() {
    return false;
  }

}
