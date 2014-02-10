/*
 * Copyright 2008 The Topaz Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver.distributed;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * Interface representating a transaction coordinator for the backends.
 *
 * @created 2008-02-16
 * @author Ronald Tschalär
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">Topaz Project</a>
 * @licence Apache License v2.0
 */
public interface TransactionCoordinator {
  /**
   * Enlist the given resource in the current transaction.
   *
   * @param xares the XAResource to enlist
   * @throws XAException if a problem occurs during enlistment
   */
  public void enlistResource(XAResource xares) throws XAException;
}
