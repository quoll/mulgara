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
 * Constants pertaining to the bit representation of data types.
 */
public class Bytes {

  /** Size of a long, in bytes */
  public static final int LONG_SIZE = Long.SIZE >> 3;

  /** Size of an int, in bytes */
  public static final int INT_SIZE = Integer.SIZE >> 3;
  
  /** Size of a short, in bytes */
  public static final int SHORT_SIZE = Short.SIZE >> 3;
  
  /** Size of a byte, in bytes */
  public static final int BYTE_SIZE = Byte.SIZE >> 3;

  /** Size of a float, in bytes */
  public static final int FLOAT_SIZE = Float.SIZE >> 3;

  /** Size of a double, in bytes */
  public static final int DOUBLE_SIZE = Double.SIZE >> 3;

}
