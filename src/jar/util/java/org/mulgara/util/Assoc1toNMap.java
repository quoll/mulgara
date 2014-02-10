/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.rosenlaw.com/OSL3.0.htm
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * This file is an original work developed by Netymon Pty Ltd
 * (http://www.netymon.com, mailto:mail@netymon.com) under contract to 
 * Topaz Foundation. Portions created under this contract are
 * Copyright (c) 2007 Topaz Foundation
 * All Rights Reserved.
 */

package org.mulgara.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A 1:N Associative Map.
 *
 * @created 2007-11-12
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 *
 * @copyright &copy;2007 <a href="http://www.topazproject.org/">Topaz Project</a>
 *
 * @licence Open Software License v3.0</a>
 */

public class Assoc1toNMap<T1,T2> implements Iterable<Map.Entry<T1,Set<T2>>> {
  private Map<T1, Set<T2>> map1toN;
  private Map<T2, T1> mapNto1;

  public Assoc1toNMap() {
    map1toN = new HashMap<T1, Set<T2>>();
    mapNto1 = new HashMap<T2, T1>();
  }

  public T1 get1(T2 t2) {
    return mapNto1.get(t2);
  }

  public Set<T2> getN(T1 t1) {
    return map1toN.get(t1);
  }

  public void put(T1 t1, T2 t2) {
    Set<T2> t2set = getN(t1);
    if (t2set == null) {
      t2set = new HashSet<T2>();
      map1toN.put(t1, t2set);
    }
    t2set.add(t2);
    
    mapNto1.put(t2, t1);
  }

  public void remove1(T1 t1) {
    Set<T2> t2set = map1toN.remove(t1);
    for (T2 t2 : t2set) {
      mapNto1.remove(t2);
    }
  }

  public void removeN(T2 t2) {
    T1 t1 = mapNto1.remove(t2);
    if (t1 != null) {
      Set<T2> t2set = map1toN.get(t1);
      t2set.remove(t2);
      if (t2set.isEmpty()) {
        map1toN.remove(t1);
      }
    }
  }

  public boolean contains1(T1 t1) {
    return map1toN.containsKey(t1);
  }

  public boolean containsN(T2 t2) {
    return mapNto1.containsKey(t2);
  }

  public Set<T1> getKeySet() {
    return map1toN.keySet();
  }

  public Iterator<Map.Entry<T1,Set<T2>>> iterator() {
    return map1toN.entrySet().iterator();
  }
}
