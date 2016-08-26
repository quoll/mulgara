/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.util;


// Java 2 standard packages
import java.util.*;

import org.mulgara.util.functional.C;

/**
 * A wrapper for {@link Map}s which makes it convenient to deal with keys that
 * map to multiple values.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi/">Simon Raboczi</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Multimap<K,V> implements Map<K,V>  {

  protected Map<K,Set<V>> map;

  /**
   * Constructor.
   *
   * @param map the underlying {@link Map}, whose values will be {@link Set}s of
   *      values
   */
  public Multimap(Map<K,Set<V>> map) {
    this.map = map;
  }

  //
  // Methods overriding the MapWrapper
  //
  // TODO - the performance is linear in the number of keys; could improve

  /**
   * METHOD TO DO
   *
   * @param value PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public boolean containsValue(Object value) {
    for (Iterator<Set<V>> i = map.values().iterator(); i.hasNext();) {
      if (i.next().contains(value)) return true;
    }
    return false;
  }

  /**
   * METHOD TO DO
   *
   * @param key PARAMETER TO DO
   * @param value PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public V put(K key, V value) {

    Set<V> values = map.get(key);

    if (values == null) {
      values = new HashSet<V>();
    }
    V old = values.contains(value) ? value : null;
    values.add(value);

    map.put(key, values);
    return old;
  }

  /**
   * Remove an object from being mapped to a specified key.
   *
   * @param key the key to remove the mapping from
   * @param value the value to remove from the mapping
   * @return whether the object existed and was unmapped
   */
  public boolean removeMapping(K key, V value) {

    Set<V> values = map.get(key);

    if (values != null) {
      boolean removed = values.remove(value);
      if (values.isEmpty()) map.remove(key);
      return removed;
    } else {
      return false;
    }
  }

  /**
   * METHOD TO DO
   *
   * @return RETURNED VALUE TO DO
   */
  public Collection<V> values() {

    Set<V> result = new HashSet<V>();

    for (Iterator<Set<V>> i = map.values().iterator(); i.hasNext();) {
      result.addAll(i.next());
    }

    return result;
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public Set<java.util.Map.Entry<K, V>> entrySet() {
    Set<Map.Entry<K, V>> entries = new HashSet<Map.Entry<K, V>>();
    for (Map.Entry<K,Set<V>> entry: map.entrySet()) {
      for (V value: entry.getValue()) {
        entries.add(new KV(entry.getKey(), value));
      }
    }
    return entries;
  }

  /**
   * This once broke the interface by returning a Set. Now it meets the interface
   * but is useless.
   * @see java.util.Map#get(java.lang.Object)
   */
  @Override
  public V get(Object key) {
    return C.first(map.get(key));
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> e: m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public V remove(Object key) {
    Set<V> v = map.remove(key);
    return v == null ? null : C.first(v);
  }

  @Override
  public int size() {
    int total = 0;
    for (Set<V> v: map.values()) total += v.size();
    return total;
  }

  class KV implements Map.Entry<K,V> {
    K key;
    V value;
    KV(K k, V v) {
      key = k;
      value = v;
    }
    @Override
    public K getKey() { return key; }
    @Override
    public V getValue() { return value; }
    @Override
    public V setValue(V v) {
      V tmp = value;
      value = v;
      return tmp;
    }
  }
}
