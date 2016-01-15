/*
 * Copyright 2010 Paula Gearon.
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

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * A serializable object with a fixed length
 * @author Paula Gearon
 */
public interface FixedLengthSerializable extends Serializable {
  
  /** Returns the length of the data after serializing */
  public int getSize();

  /** Write the contents of this object into the buffer */
  public ByteBuffer writeTo(ByteBuffer data);

  /** Overwrite the contents of this object with the data in the buffer */
  public FixedLengthSerializable readFrom(ByteBuffer data);
}
