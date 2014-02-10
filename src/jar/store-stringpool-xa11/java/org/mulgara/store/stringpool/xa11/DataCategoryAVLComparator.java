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

package org.mulgara.store.stringpool.xa11;

import org.mulgara.store.xa.AVLComparator;
import org.mulgara.store.xa.AVLNode;

/**
 * Comparator for category information only.
 *
 * @created Aug 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class DataCategoryAVLComparator implements AVLComparator {

  private final int typeCategoryId;

  DataCategoryAVLComparator(int typeCategoryId) {
    this.typeCategoryId = typeCategoryId;
  }

  public int compare(long[] key, AVLNode avlNode) {
    // NOTE: ignore key.

    // First, order by type category ID.
    int nodeTypeCategoryId = DataStruct.getTypeCategoryId(avlNode);
    return typeCategoryId <= nodeTypeCategoryId ? -1 : 1;
  }

}
