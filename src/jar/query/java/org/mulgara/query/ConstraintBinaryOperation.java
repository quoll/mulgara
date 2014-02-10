package org.mulgara.query;

import java.util.ArrayList;
import java.util.List;

/**
 * A common base class for operations that can only take binary operands.
 * The {@link ConstraintOperation} class explicitly flattens out it's parameters, and this
 * class avoids that, while still providing the functionality of that class.
 *
 * @created May 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class ConstraintBinaryOperation extends ConstraintOperation {

  /** Serialization ID */
  private static final long serialVersionUID = -267804943672931500L;

  /**
   * A binary-only constructor.
   * @param lhs The LHS for the operation.
   * @param rhs The RHS for the operation.
   */
  public ConstraintBinaryOperation(ConstraintExpression lhs, ConstraintExpression rhs) {
    super(testedList(lhs, rhs));
  }

  /**
   * Validate parameters and set them up as a list.
   * @param lhs The main pattern
   * @param rhs The optional pattern
   * @return A 2 element list containing {lhs, rhs}
   */
  protected static List<ConstraintExpression> testedList(ConstraintExpression lhs, ConstraintExpression rhs) {
    // Validate "lhs" parameter
    if (lhs == null) throw new IllegalArgumentException("Null \"lhs\" parameter");
  
    // Validate "rhs" parameter
    if (rhs == null) throw new IllegalArgumentException("Null \"optional\" parameter");
  
    // Initialize fields
    List<ConstraintExpression> ops = new ArrayList<ConstraintExpression>(2);
    ops.add(lhs);
    ops.add(rhs);
    return ops;
  }

  /**
   * Get the LHS of this operation
   * @return The LHS operand.
   */
  public ConstraintExpression getLhs() {
    return elements.get(0);
  }

  /**
   * Get the RHS of this operation
   * @return The RHS operand.
   */
  public ConstraintExpression getRhs() {
    return elements.get(1);
  }

}