/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.util.functional;

/**
 * Functor template for a function that takes arguments of two different types
 * and returns a value of a third type, possibly throwing an exception.
 *
 * @created Aug 4, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface Fn2E<T1,T2,T3,E extends Exception> {

  /**
   * Declares a function template that takes two arguments and returns a value of
   * another type.
   * @param arg1 The first argument.
   * @param arg2 The first argument.
   * @return A value based on arg1 and arg2.
   * @throws E An exception that may be thrown from this method.
   */
  T3 fn(T1 arg1, T2 arg2) throws E;
}
