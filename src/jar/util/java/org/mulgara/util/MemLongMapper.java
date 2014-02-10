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

package org.mulgara.util;

import java.util.HashMap;

/**
 * A minimal LongMapper implementation for use in tests.
 *
 * @created Sep 26, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class MemLongMapper extends HashMap<Long,Long> implements LongMapper {

  /** Generated UID */
  private static final long serialVersionUID = -2373833159148862815L;

  /**
   * @see org.mulgara.util.LongMapper#delete()
   */
  public void delete() throws Exception {
  }

  /**
   * @see org.mulgara.util.LongMapper#getLong(long)
   */
  public long getLong(long key) {
    return get(key);
  }

  /**
   * @see org.mulgara.util.LongMapper#putLong(long, long)
   */
  public void putLong(long key, long value) {
    put(key,value);
  }

}
