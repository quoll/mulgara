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

import java.util.Map;

/**
 * A pair of elements.
 *
 * @created Aug 5, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Pair<T1,T2> implements Map.Entry<T1,T2> {

  private T1 first;

  private T2 second;

  public Pair(T1 f, T2 s) { first = f; second = s; }

  public T1 first() { return first; }

  public T2 second() { return second; }

  public T1 getKey() { return first; }

  public T2 getValue() { return second; }

  public T2 setValue(T2 value) { throw new UnsupportedOperationException("Pairs are an immutable type"); }

  public static <C1,C2> Pair<C1,C2> p(C1 c1, C2 c2) { return new Pair<C1,C2>(c1, c2); }

  public Map<T1,T2> addTo(Map<T1,T2> map) { map.put(first, second); return map; }

  public String toString() { return "{" + first + "," + second + "}"; }
}
