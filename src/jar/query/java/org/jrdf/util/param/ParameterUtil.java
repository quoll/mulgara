package org.jrdf.util.param;

/**
 * Utility for checking parameters to methods.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class ParameterUtil {

  private static final ParameterChecker NULL_CHECKER = new NullChecker();
  private static final ParameterChecker EMPTY_STRING_CHECKER = new EmtpyStringChecker();

  private ParameterUtil() { }

  /**
   * Checks if <var>param</var> is <code>null</code> and throws an exception if it is.
   *
   * @param name The name of the parameter to check.
   * @param param The parameter to check.
   * @throws IllegalArgumentException If <car>param</var> is <code>null</code>.
   */
  public static void checkNotNull(String name, Object param) throws IllegalArgumentException {
    if (!NULL_CHECKER.paramAllowed(param)) {
      throw new IllegalArgumentException(name + " parameter cannot be null");
    }
  }

  /**
   * Checks if <var>param</var> is <code>null</code> or the empty string and throws an exception if it is.
   *
   * @param name The name of the parameter to check.
   * @param param The parameter to check.
   * @throws IllegalArgumentException If <car>param</var> is <code>null</code> or the empty string.
   */
  public static void checkNotEmptyString(String name, String param) throws IllegalArgumentException {
    checkNotNull(name, param);
    if (!EMPTY_STRING_CHECKER.paramAllowed(param)) {
      throw new IllegalArgumentException(name + " parameter cannot be the empty string");
    }
  }
}
