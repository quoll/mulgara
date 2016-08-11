/*
 * Copyright 2011 Paula Gearon.
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

/**
 * An interface for serializing data into and out of set data.
 * Set data is either the raw data of a Map-key, Map-value, or Map.Entry.
 */
public interface SetDataConverter<T, SD> {

  /**
   * Convert data to set-data .
   * @param d The data to convert.
   * @return The set-data representing this data.
   */
  public SD toSetData(T d);

  /**
   * Convert set-data to data.
   * @param d The set-data containing the data.
   * @return The data interpreted from the set-data.
   */
  public T fromSetData(SD d);

}
