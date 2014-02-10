/*
 * Copyright 2007 The Topaz Foundation 
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
package org.mulgara.server.rmi;

// Java 2 standard packages
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

// Local packages

/**
 * Remote XAResource.
 *
 */
/**
 * An analogue to XAResource that is suitable for implementing as an RMI object.
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 * @created 2007-11-28
 * @licence Apache License v2.0
 */
public interface RemoteXAResource extends Remote
{
  public void commit(Xid xid, boolean onePhase) throws XAException, RemoteException;

  public void end(Xid xid, int flags) throws XAException, RemoteException;

  public void forget(Xid xid) throws XAException, RemoteException;

  public int getTransactionTimeout() throws XAException, RemoteException;

  public int prepare(Xid xid) throws XAException, RemoteException;

  public Xid[] recover(int flag) throws XAException, RemoteException;

  public void rollback(Xid xid) throws XAException, RemoteException;

  public boolean setTransactionTimeout(int seconds) throws XAException, RemoteException;

  public void start(Xid xid, int flags) throws XAException, RemoteException;

  // Note: This provides distributed isSameRM
  public Serializable getRMId() throws RemoteException;
}
