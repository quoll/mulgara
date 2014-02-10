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
import java.rmi.RemoteException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

// Third party packages
import org.apache.log4j.*;

// Local packages
import org.mulgara.server.ResourceManagerInstanceAdaptor;
import org.mulgara.util.Rmi;

/**
 * Wraps an XAResource and makes it available as an RMI object.
 *
 * @created 2007-11-28
 * @author Andrae Muys
 * @licence Apache License v2.0
 */
public class XAResourceWrapperRemoteXAResource extends Rmi implements RemoteXAResource {

  /** Generated UID */
  private static final long serialVersionUID = 1L;

  /** logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(XAResourceWrapperRemoteXAResource.class.getName());

  protected final XAResource resource;
  protected final ResourceManagerInstanceAdaptor adaptor;

  public XAResourceWrapperRemoteXAResource(XAResource resource) throws RemoteException {
    if (resource == null) {
      throw new IllegalArgumentException("Null 'resource' parameter");
    }

    this.resource = resource;
    this.adaptor = resource instanceof ResourceManagerInstanceAdaptor ?
      (ResourceManagerInstanceAdaptor)resource : null;
  }

  public void commit(Xid xid, boolean onePhase) throws XAException, RemoteException {
    resource.commit(xid, onePhase);
  }

  public void end(Xid xid, int flags) throws XAException, RemoteException {
    resource.end(xid, flags);
  }

  public void forget(Xid xid) throws XAException, RemoteException {
    resource.forget(xid);
  }

  public int getTransactionTimeout() throws XAException, RemoteException {
    return resource.getTransactionTimeout();
  }

  public boolean isSameRM(XAResource xares) throws XAException, RemoteException {
    return resource.isSameRM(xares);
  }

  public int prepare(Xid xid) throws XAException, RemoteException {
    return resource.prepare(xid);
  }

  public Xid[] recover(int flag) throws XAException, RemoteException {
    return resource.recover(flag);
  }

  public void rollback(Xid xid) throws XAException, RemoteException {
    resource.rollback(xid);
  }

  public boolean setTransactionTimeout(int seconds) throws XAException, RemoteException {
    return resource.setTransactionTimeout(seconds);
  }

  public void start(Xid xid, int flags) throws XAException, RemoteException {
    resource.start(xid, flags);
  }

  public Serializable getRMId() throws RemoteException, UnsupportedOperationException {
    if (adaptor == null) {
      throw new UnsupportedOperationException("Wrapped XAResource does not support remote-id");
    } else {
      return adaptor.getRMId();
    }
  }
}
