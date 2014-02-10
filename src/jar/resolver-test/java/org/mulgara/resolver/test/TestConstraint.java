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
 *   The copyright in this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Mulgara Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 *   getModel() contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.test;

// Java packages
import java.util.HashSet;
import java.util.Set;

// Third party packages
// import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;


/**
 * @created 2005-05-04
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.4 $
 * @modified $Date: 2005/05/29 08:34:21 $ by $Author: raboczi $
 * @maintenanceAuthor $Author: raboczi $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class TestConstraint implements Constraint {
  // /** Logger */
  // private static Logger logger = Logger.getLogger(TestConstraint.class);

  /**
   * Fixme, generate proper version UID
   */
  static final long serialVersionUID = 0000000000000000001L;

  private Variable var1;
  private Variable var2;
  private String testSelection;
  private String testParam;

  public TestConstraint(Variable var1, Variable var2, String testSelection, String testParam) {
    if (var1 == null || var2 == null || testSelection == null || testParam == null) {
      throw new IllegalArgumentException("null parameter to TestConstraint(" +
          var1 + ", " + var2 + ", " + testSelection + ", " + testParam + ")");
    }

    this.var1 = var1;
    this.var2 = var2;
    this.testSelection = testSelection;
    this.testParam = testParam;
  }

  public Variable getVariable1() {
    return var1;
  }

  public Variable getVariable2() {
    return var2;
  }

  public String getTestSelection() {
    return testSelection;
  }

  public String getTestParam() {
    return testParam;
  }


  public ConstraintElement getElement(int index) {
    switch (index) {
      case 0:
        return var1;
      case 1:
        return var2;
      case 2:
        return new LiteralImpl(testSelection);
      case 3:
        return new LiteralImpl(testParam);
    }
    throw new IllegalArgumentException("Invalid index to getElement: " + index);
  }

  public boolean isRepeating() {
    return false;
  }

  public Set<Variable> getVariables() {
    Set<Variable> vars = new HashSet<Variable>();
    vars.add(var1);
    vars.add(var2);

    return vars;
  }

  public ConstraintElement getModel() {
    throw new IllegalStateException("TestConstraint is of fixed extent, no model available");
  }

  public boolean equals(Object object) {
    // FIXME: Refactor to exploit equals() method on ConstraintExpression.
    if (object == null) {
      return false;
    }

    if (object == this) {
      return true;
    }

    // Check that the given object is the correct class if so check each
    // element.
    if (!object.getClass().equals(this.getClass())) {
      return false;
    }

    TestConstraint rhs = (TestConstraint)object;

    return var1.equals(rhs.var1) &&
        var2.equals(rhs.var2) &&
        testSelection.equals(rhs.testSelection) &&
        testParam.equals(rhs.testParam);
  }

  public boolean isAssociative() {
    return false;
  }
}
