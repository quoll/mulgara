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

/**
 * Wraps an RMI RemoteXAResource and presents it as a pure XAResource.
 *
 * @created 2007-11-28
 * @author Andrae Muys
 * @licence Apache License v2.0
 */
class RemoteXAResourceWrapperXAResource implements XAResource, ResourceManagerInstanceAdaptor {
  /** logger */
  private static final Logger logger = Logger.getLogger(RemoteXAResourceWrapperXAResource.class.getName());

  /**
   * The wrapped instance.
   */
  private RemoteXAResource remoteResource;

  /**
   * Wrap a {@link RemoteAnswer} to make it into an {@link org.mulgara.query.Answer}.
   *
   * @param remoteResource  the instance to wrap
   * @throws IllegalArgumentException  if <var>remoteAnswer</var> is <code>null</code>
   */
  RemoteXAResourceWrapperXAResource(RemoteXAResource remoteResource) throws RemoteException {
    if (remoteResource == null) {
      throw new IllegalArgumentException("Null 'remoteResource' parameter");
    }

    this.remoteResource = remoteResource;
  }

  //
  // Methods implementing XAResource
  //

  public void commit(Xid xid, boolean onePhase) throws XAException {
    try {
      remoteResource.commit(convertXid(xid), onePhase);
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public void end(Xid xid, int flags) throws XAException {
    try {
      remoteResource.end(convertXid(xid), flags);
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public void forget(Xid xid) throws XAException {
    try {
      remoteResource.forget(convertXid(xid));
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public int getTransactionTimeout() throws XAException {
    try {
      return remoteResource.getTransactionTimeout();
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public boolean isSameRM(XAResource xares) throws XAException {
    try {
      if (xares == this) {
        return true;
      } else if (xares instanceof ResourceManagerInstanceAdaptor) {
        try {
          return ((ResourceManagerInstanceAdaptor)xares).getRMId().equals(remoteResource.getRMId());
        } catch (UnsupportedOperationException eu) {
          logger.debug("getRMId() unsupported on XAResource", eu);
          return false;
        }
      } else {
        return false;
      }
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public Serializable getRMId() {
    try {
      return remoteResource.getRMId();
    } catch (RemoteException er) {
      throw new UnsupportedOperationException("Failed to obtain RMid", er);
    }
  }

  public int prepare(Xid xid) throws XAException {
    try {
      return remoteResource.prepare(convertXid(xid));
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public Xid[] recover(int flag) throws XAException {
    try {
      return remoteResource.recover(flag);
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public void rollback(Xid xid) throws XAException {
    try {
      remoteResource.rollback(convertXid(xid));
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public boolean setTransactionTimeout(int seconds) throws XAException {
    try {
      return remoteResource.setTransactionTimeout(seconds);
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  public void start(Xid xid, int flags) throws XAException {
    try {
      remoteResource.start(convertXid(xid), flags);
    } catch (RemoteException re) {
      logger.warn("RMI Error in XAResource", re);
      throw new XAException(XAException.XAER_RMFAIL);
    }
  }

  private SerializableXid convertXid(Xid xid) {
    return new SerializableXid(xid);
  }

  private static class SerializableXid implements Xid, Serializable {
    /** Generated UID */
    private static final long serialVersionUID = 2421196761757350942L;

    private byte[] bq;
    private int fi;
    private byte[] gtid;

    public SerializableXid(Xid xid) {
      byte[] tbq = xid.getBranchQualifier();
      byte[] tgtid = xid.getGlobalTransactionId();
      this.bq = new byte[tbq.length];
      this.fi = xid.getFormatId();
      this.gtid = new byte[tgtid.length];
      System.arraycopy(tbq, 0, this.bq, 0, tbq.length);
      System.arraycopy(tgtid, 0, this.gtid, 0, tgtid.length);
    }

    public byte[] getBranchQualifier() {
      return bq;
    }

    public int getFormatId() {
      return fi;
    }

    public byte[] getGlobalTransactionId() {
      return gtid;
    }
  }
}
