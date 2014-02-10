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
 * 
 *
 * @created Aug 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class F {

  static final <T1,T2> Fn<T2> curry(final Fn1<T1,T2> fna, final T1 arg) {
    return new Fn<T2>() { public T2 fn() { return fna.fn(arg); } };
  }

  static final <T1,T2,R> Fn1<T2,R> curry(final Fn2<T1,T2,R> fna, final T1 arg) {
    return new Fn1<T2,R>() { public R fn(T2 a) { return fna.fn(arg, a); } };
  }

}
