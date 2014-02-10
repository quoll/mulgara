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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Utility methods for reflection.
 *
 * @created Aug 15, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Reflect {

  /**
   * Create a new instance of the given class, using the supplied arguments.
   * @param clazz The class to create an instance of.
   * @param args The arguments to supply to the constructor of clazz.
   * @return A new instance of clazz, constructed with the arguments of args.
   */
  public static <T> T newInstance(Class<T> clazz, Object... args) {
    try {
      return findConstructor(clazz, args).newInstance(args);
    } catch (SecurityException e) {
      throw new RuntimeException("Not permitted to create " + clazz.getName(), e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("No constructor of the requested form: " + clazz.getName() + argList(args), e);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Bad arguments supplied to constructor for: " + clazz.getName(), e);
    } catch (InstantiationException e) {
      throw new RuntimeException("Not legal to create objects of type: " + clazz.getName() + "(try using a subclass)", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Not permitted to access constructor for " + clazz.getName(), e);
    } catch (InvocationTargetException e) {
      // wrap the exception, since we don't know what type it is
      throw new RuntimeException(e.getCause());
    }
  }


  /**
   * Do an exhaustive search for a constructor, given a list of parameters.
   * If multiple constructors would match, then only the first is returned.
   * @param <T> The class type to retrieve a constructor for.
   * @param clazz The class object representing the class T.
   * @param args The argument list to use with the constructor.
   * @return A constructor that can be used on the given arguments.
   * @throws NoSuchMethodException No such constructor could be found.
   */
  public static <T> Constructor<T> findConstructor(Class<T> clazz, Object... args) throws NoSuchMethodException {
    Class<?>[] argTypes = getTypes(args);

    // do a standard search
    try {
      return clazz.getConstructor(argTypes);
    } catch (NoSuchMethodException e) {
      /* failed - try again */
  
      // search for constructors with supertype parameters
      Constructor<T> result = openConstructorSearch(clazz, argTypes, getAssignableTester());
      // search for constructors allowing nulls as parameters
      if (result == null) result = openConstructorSearch(clazz, argTypes, getNullAssignTester());
      
      if (result == null) throw new NoSuchMethodException("Unable to find a method for: " + clazz.getName() + "<init>(" + Arrays.toString(argTypes) + ")");
      return result;
    }
  }


  /**
   * Search for the first constructor that matches a given argument type list,
   * given a testing function for argument compatibility. More than one constructor
   * may match, in which case only the first is returned.
   * @param <T> The type of constructor to return.
   * @param clazz The class to get a constructor for.
   * @param argTypes An array of the required types acceptable to the constructor.
   *        This list may include nulls.
   * @param assignFrom The function for testing if the constructor parameter may be
   *        given an data of the type described in the argTypes array.
   * @return A constructor for clazz if one could be found, otherwise <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  private static <T> Constructor<T> openConstructorSearch(Class<T> clazz, Class<?>[] argTypes, BooleanOp2<Class<?>,Class<?>> assignFrom) {
    for (Constructor<T> con: (Constructor<T>[])clazz.getConstructors()) {
      boolean match = true;
      Class<?>[] paramTypes = con.getParameterTypes();
      
      // Make sure the candidate constructor has the appropriate number of arguments.
      if (paramTypes.length != argTypes.length) continue;
      
      // If the argument count matches, test the argument classes one-by-one.
      for (int p = 0; p < paramTypes.length; p++) {
        if (match && !assignFrom.test(paramTypes[p], argTypes[p])) {
          match = false;
          break;
        }
      }
      if (match) return con;
    }
    return null;
  }


  /**
   * Get a type list of objects.
   * @param args An array of objects to obtain types for.
   * @return An array containing the types of the objects from args,
   *         where args[x] instanceof return[x]
   */
  public static Class<?>[] getTypes(Object[] args) {
    Class<?>[] types = new Class<?>[args.length];
    for (int a = 0; a < args.length; a++) types[a] = (args[a] == null) ? null : args[a].getClass();
    return types;
  }


  /**
   * Converts an array of argument data into a parenthesized string of comma-separated names.
   * @param args The data to convert to type names.
   * @return A parenthesized type list.
   */
  private static String argList(Object[] args) {
    StringBuffer result = new StringBuffer("(");
    Class<?>[] types = getTypes(args);
    for (int i = 0; i < types.length; i++) {
      if (i != 0) result.append(", ");
      result.append(types[i].getName());
    }
    result.append(")");
    return result.toString();
  }


  /**
   * Create a tester that can test for assignability of types, excluding the case when
   * the given type is null.  This differs from {@link Class#isAssignableFrom} in that
   * it will not throw a NullPointerException.
   * @return A {@link BooleanOp2} object that can test that the type defined in the second
   *  parameter can be assigned to the type in the first parameter.
   */
  private static BooleanOp2<Class<?>,Class<?>> getAssignableTester() {
    return new BooleanOp2<Class<?>,Class<?>>() {
      public boolean test(Class<?> o1, Class<?> o2) {
        return o2 != null && o1.isAssignableFrom(o2);
       }
    };
  }


  /**
   * Create a tester that can test for assignability of types, including the case when
   * the given type is null.
   * @return A {@link BooleanOp2} object that can test that the type defined in the second
   *  parameter can be assigned to the type in the first parameter.
   */
  private static BooleanOp2<Class<?>,Class<?>> getNullAssignTester() {
    return new BooleanOp2<Class<?>,Class<?>>() {
      public boolean test(Class<?> o1, Class<?> o2) {
        return o2 == null || o1.isAssignableFrom(o2);
      }
    };
  }


  /**
   * Debug only method for printing out the complete stack trace and attempts at
   * finding a constructor, when getConstructor fails with a {@link NoSuchMethodException}.
   * @param clazz The class that a constructor was searched for in.
   * @param e The exception that required this log.
   * @param args The arguments used to find the constructor.
   * @return A string with all the available details coming from the failure to find
   * the constructor.
   */
  @SuppressWarnings("unused")
  private static String fullLog(Class<?> clazz, Throwable e, Object[] args) {
    String result = argList(args) + "[" + e.getMessage() + "]\n" + StringUtil.strackTraceToString(e);
    result += "Available constructors:\n";
    for (Constructor<?> con: clazz.getConstructors()) {
      result += "  <init>(";
      boolean match = true;
      Class<?>[] paramTypes = con.getParameterTypes();
      for (int p = 0; p < paramTypes.length; p++) {
        if (p != 0) result += ", ";
        result += paramTypes[p].getCanonicalName();
        if (match && !paramTypes[p].isAssignableFrom(args[p].getClass())) match = false;
      }
      result += ")";
      result += (match ? "*\n" : "\n");
    }
    return result;
  }
}
