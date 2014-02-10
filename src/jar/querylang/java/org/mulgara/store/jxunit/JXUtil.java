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
package org.mulgara.store.jxunit;

import net.sourceforge.jxunit.JXProperties;

/**
 * Utility class for JXUnit tests.
 * @author Alex Hall
 * @created Sep 23, 2011
 */
public class JXUtil {

  /**
   * Adds a string to the given JXProperties object, first converting the string content to use
   * Unix-style line endings so that it can be compared to test data from the file system.
   * @param props The JXUnit properties; may not be null.
   * @param prop The property key; may not be null.
   * @param value The property value, or null.
   */
  @SuppressWarnings("unchecked")
  public static void putStringProp(JXProperties props, String prop, String value) {
    if (props == null || prop == null) throw new IllegalArgumentException("Can't set a null property");
    if (value != null) value = value.replace("\r\n", "\n");
    props.put(prop, value);
  }
}
