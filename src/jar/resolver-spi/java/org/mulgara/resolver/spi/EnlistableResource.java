/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.rosenlaw.com/OSL3.0.htm
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * This file is an original work developed by Netymon Pty Ltd
 * (http://www.netymon.com, mailto:mail@netymon.com). Portions
 * created
 * by Netymon Pty Ltd are Copyright (c) 2006 Netymon Pty Ltd.
 * All Rights Reserved.
 */

package org.mulgara.resolver.spi;

// Java 2 standard packages
import javax.transaction.xa.XAResource;

/**
 * A resource that can participate within a JTA transaction.
 *
 * @created 2006-11-14
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 * @maintenanceAuthor $Author: andrae $
 * @company <a href="mailto:mail@netymon.com">Netymon Pty Ltd</a>
 * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
 * @licence Open Software License v3.0
 */

public interface EnlistableResource
{
  /**
   * Expose a callback object for enlistment by a transaction manager.
   *
   * Note: Resources that do not wish to participate in the transaction 
   *       should return a new DummyXAResource instead.
   *
   * @return an {@link XAResource} that can be used by a transaction manager to
   *   coordinate this resolver's participation in a distributed transaction
   * @see javax.resource.spi.ManagedConnection#getXAResource
   */
  public XAResource getXAResource();

  /**
   * Abort current transaction; release all resources.
   *
   * This method is called in the event of a catastrophic transaction control
   * failure that has rendered it impossible to terminate the transaction
   * normally - even as a rollback.  The resource should treat this as a
   * rollback operation, abort all updates, and release all resources.
   */
  public void abort();
}
