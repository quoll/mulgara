package org.jrdf.util.param;


/**
 * Checks that nulls are not passed to methods.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
final class NullChecker implements ParameterChecker {

  /**
   * Checks if this checker allows a parameter with the given value.
   *
   * @param param The parameter to check the value of.
   * @return <code>true</code> if the parameter is allowed by this checker.
   */
  public boolean paramAllowed(Object param) {
    return param != null;
  }
}
