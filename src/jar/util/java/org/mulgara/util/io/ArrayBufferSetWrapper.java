/*
 * Copyright 2011 Paul Gearon.
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
package org.mulgara.util.io;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A wrapper class for wrapping the sets of data returned from FileHashMap.
 * The data is either ByteBuffer ({@link FileHashMap#keySet()} or {@link FileHashMap#values()})
 * or {@link java.util.Map.Entry} of ByteBuffer -> ByteBuffer.
 */
public class ArrayBufferSetWrapper<E,SD> implements Set<E> {

  private final Set<SD> dataset;

  private final SetDataConverter<E,SD> serializer;

  public ArrayBufferSetWrapper(Set<SD> ds, SetDataConverter<E,SD> ser) {
    dataset = ds;
    serializer = ser;
  }

  @Override
  public boolean add(E a) { throw new UnsupportedOperationException(); }

  @Override
  public boolean addAll(Collection<? extends E> a) { throw new UnsupportedOperationException(); }

  @Override
  public void clear() {
    dataset.clear();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean contains(Object a) {
    return dataset.contains(serializer.toSetData((E)a));
  }

  @Override
  public boolean containsAll(Collection<?> a) {
    for (Object v: a) if (!contains(v)) return false;
    return true;
  }

  @Override
  public boolean isEmpty() {
    return dataset.isEmpty();
  }

  @Override
  public boolean remove(Object arg0) { throw new UnsupportedOperationException(); }

  @Override
  public boolean removeAll(Collection<?> arg0) { throw new UnsupportedOperationException(); }

  @Override
  public boolean retainAll(Collection<?> arg0) { throw new UnsupportedOperationException(); }

  @Override
  public int size() {
    return dataset.size();
  }

  @Override
  public Object[] toArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T[] toArray(T[] arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Iterator<E> iterator() {
    return new DataIterator(dataset.iterator());
  }

  /**
   * Implementation of the iterator.
   * @param <E> The object type to be returned by the iterator.
   */
  private class DataIterator implements Iterator<E> {

    private final Iterator<SD> dataIterator;

    public DataIterator(Iterator<SD> it) {
      dataIterator = it;
    }

    @Override
    public boolean hasNext() {
      return dataIterator.hasNext();
    }

    @Override
    public E next() {
      return serializer.fromSetData(dataIterator.next());
    }

    @Override
    public void remove() { throw new UnsupportedOperationException(); }
    
  }
}
