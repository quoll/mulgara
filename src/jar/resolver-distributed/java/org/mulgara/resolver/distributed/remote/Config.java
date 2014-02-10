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

package org.mulgara.resolver.distributed.remote;

/**
 * Defines a set of property names and default values for use with paged remote sets.
 *
 * @created 2007-04-23
 * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Config {

  /** Default maximum number of pages that may be queued. */
  private static final int DEFAULT_MAX_PAGES = 100;

  /** Name of the Maximum Pages property. */
  private static final String MAX_PAGES_PROPERTY = "pagedset.pages.max";

  /** Default maximum time to wait for a page to arrive, in milliseconds. */
  private static final long DEFAULT_TIMEOUT = 10000;

  /** Name of the timeout property. */
  private static final String TIMEOUT_PROPERTY = "pagedset.timeout";

  /** Default number of entries in a page. */
  private static final int DEFAULT_PAGE_SIZE = 128;

  /** Name of the page size property. */
  private static final String PAGE_SIZE_PROPERTY = "pagedset.pages.size";


  /**
   * Get the maximum number of pages to keep in memory at once.
   * @return The maximum number of pages to keep.
   */
  public static int getMaxPages() {
    return Integer.getInteger(MAX_PAGES_PROPERTY, DEFAULT_MAX_PAGES).intValue();
  }

  /**
   * Get the maximum time to wait for a page to arrive, in milliseconds.
   * @return The maximum timeout for a remote call.
   */
  public static long getTimeout() {
    return Long.getLong(TIMEOUT_PROPERTY, DEFAULT_TIMEOUT).longValue();
  }

  /**
   * Get the size of pages to transfer.
   * @return The number of entries in a page.
   */
  public static int getPageSize() {
    return Integer.getInteger(PAGE_SIZE_PROPERTY, DEFAULT_PAGE_SIZE).intValue();
  }

}
