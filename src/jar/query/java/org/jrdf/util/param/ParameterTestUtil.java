package org.jrdf.util.param;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import junit.framework.Assert;

/**
 * Test utility for checking parameters bad to methods.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class ParameterTestUtil {
  public static final String NULL_STRING = null;
  public static final String EMPTY_STRING = "";
  public static final String SINGLE_SPACE = " ";
  public static final String NON_EMPTY_STRING = "FOO";
  public static final Object NON_NULL_OBJECT = new Object();

  private ParameterTestUtil() {
  }

  public static void checkBadStringParam(Object ref, String methodName, String param) throws Exception {
    try {
      invokeMethod(ref, methodName, String.class, param);
      Assert.fail("Bad argument should have throw IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  private static void invokeMethod(Object cls, String methodName, Class<?> paramClass, String paramValue) throws Exception {
    try {
      Method method = cls.getClass().getMethod(methodName, new Class[]{paramClass});
      method.invoke(cls, new Object[]{paramValue});
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw e;
    }
  }
}
