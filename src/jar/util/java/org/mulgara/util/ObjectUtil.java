/*
 * Copyright 2011 Revelytix, Inc.
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

package org.mulgara.util;

/**
 * Purpose: A utility class for general objects.<P>
 *
 * Creation Date: 7 September 2011 <P>
 *
 * Original Author: Paula Gearon (pgearon@revelytix.com) <P>
 *
 * Company: Revelytix, Inc. <P>
 *
 * License: LGPL by default, or as assigned
 */
public class ObjectUtil {

  /**
   * Simple test to see if two objects are equal, even if null.
   * @param a The first object to test.
   * @param b The second object to test.
   * @return <code>true</code> only if both objects are null, or equal.
   */
  public static final boolean eq(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }

  /**
   * Obtain a class loader for an object.
   * @param o The object to get the classloader for.
   * @return The classloader that loaded the class of o.
   */
  public static ClassLoader getClassLoader(Object o) {
    ClassLoader c = o.getClass().getClassLoader();
    if (c == null) c = ClassLoader.getSystemClassLoader();
    if (c == null) throw new RuntimeException("Can't find a class loader");
    return c;
  }
}
