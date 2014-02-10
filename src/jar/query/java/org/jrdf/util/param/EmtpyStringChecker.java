package org.jrdf.util.param;

/**
 * Checks that empty strings are not passed to methods.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
final class EmtpyStringChecker implements ParameterChecker {

  private static final String EMPTY_STRING = "";

  /**
   * Checks if this checker allows a parameter with the given value.
   * <p>
   * Note. This checker assumes that <var>param</var> is not <code>null</code>.
   * </p>
   *
   * @param param The parameter to check the value of.
   * @return <code>true</code> if the parameter is allowed by this checker.
   */
  public boolean paramAllowed(Object param) {
    return (paramAllowed((String) param));
  }

  private boolean paramAllowed(String param) {
    return !param.trim().equals(EMPTY_STRING);
  }
}
