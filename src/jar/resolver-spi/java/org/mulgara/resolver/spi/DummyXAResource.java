/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.spi;

// Java 2 standard packages
import java.util.*;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

// Third party packages
import org.apache.log4j.Logger;


/**
 * A dummy implementation of the {@link XAResource} interface which logs the
 * calls made to it, but otherwise ignores them.
 *
 * @created 2004-05-12
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technoogies, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class DummyXAResource implements XAResource
{
  /** Logger.  */
  private static final Logger logger = Logger.getLogger(DummyXAResource.class.getName());

  /**
   * Map from keyed from the {@link Integer} value of the various flags
   * defined in {@link XAResource} and mapping to the formatted name for that
   * flag.
   */
  protected final static Map<Integer,String> flagMap = new HashMap<Integer,String>();

  static {
    flagMap.put(new Integer(XAResource.TMENDRSCAN),   "TMENDRSCAN");
    flagMap.put(new Integer(XAResource.TMFAIL),       "TMFAIL");
    flagMap.put(new Integer(XAResource.TMJOIN),       "TMJOIN");
    flagMap.put(new Integer(XAResource.TMONEPHASE),   "TMONEPHASE");
    flagMap.put(new Integer(XAResource.TMRESUME),     "TMRESUME");
    flagMap.put(new Integer(XAResource.TMSTARTRSCAN), "TMSTARTRSCAN");
    flagMap.put(new Integer(XAResource.TMSUCCESS),    "TMSUCCESS");
    flagMap.put(new Integer(XAResource.TMSUSPEND),    "TMSUSPEND");
  }

  /** The transaction timeout value in seconds.  */
  protected int transactionTimeout = 0;

  //
  // Constructor
  //

  /**
   * Construct a {@link DummyXAResource} with a default 10 second transaction timeout.
   */
  public DummyXAResource() {
    this(10);
  }

  /**
   * Construct a {@link DummyXAResource} with a specified transaction timeout.
   *
   * @param transactionTimeout  transaction timeout period, in seconds
   */
  public DummyXAResource(int transactionTimeout) {
    if (logger.isDebugEnabled()) {
      logger.debug("Creating " + getClass().getName() + " with timeout " + transactionTimeout);
    }
    this.transactionTimeout = transactionTimeout;
  }

  //
  // Methods implementing XAResource
  //

  public void commit(Xid xid, boolean onePhase) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Commit xid=" + System.identityHashCode(xid) + " onePhase=" + onePhase);
    }
  }

  public void end(Xid xid, int flags) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("End xid=" + System.identityHashCode(xid) + " flags=" + formatFlags(flags));
    }
  }

  public void forget(Xid xid) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Forget xid=" + System.identityHashCode(xid));
    }
  }

  public int getTransactionTimeout() throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Get transaction timeout: " + transactionTimeout);
    }
    return transactionTimeout;
  }

  public boolean isSameRM(XAResource xaResource) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Is same resource manager? " + (xaResource == this));
    }
    return xaResource == this;
  }

  public int prepare(Xid xid) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Prepare " + System.identityHashCode(xid));
    }
    return XA_OK;
  }

  public Xid[] recover(int flag) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Recover flag=" + formatFlags(flag));
    }
    throw new XAException(XAException.XAER_RMERR);
  }

  public void rollback(Xid xid) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Rollback " + System.identityHashCode(xid));
    }
  }

  public boolean setTransactionTimeout(int transactionTimeout) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Set transaction timeout: " + transactionTimeout);
    }
    this.transactionTimeout = transactionTimeout;
    return true;
  }

  public void start(Xid xid, int flags) throws XAException {
    if (logger.isDebugEnabled()) {
      logger.debug("Start " + System.identityHashCode(xid) + " flags=" + formatFlags(flags));
    }
  }

  //
  // Internal methods
  //

  /**
   * @param t the exception
   * @return true if <var>t</var> is an XAException that represents a heuristic completion
   */
  public static boolean isHeuristic(Throwable t) {
    return (t instanceof XAException) && isHeuristic(((XAException)t).errorCode);
  }

  /**
   * @param errorCode the xa error code
   * @return true if <var>errorCode</var> represents a heuristic completion
   */
  public static boolean isHeuristic(int errorCode) {
    return (errorCode == XAException.XA_HEURHAZ || errorCode == XAException.XA_HEURCOM ||
            errorCode == XAException.XA_HEURRB  || errorCode == XAException.XA_HEURMIX);
  }

  /**
   * @param t the exception
   * @return true if <var>t</var> is an XAException that represents a rollback
   */
  public static boolean isRollback(Throwable t) {
    return (t instanceof XAException) && isRollback(((XAException)t).errorCode);
  }

  /**
   * @param errorCode the xa error code
   * @return true if <var>errorCode</var> represents a rollback
   */
  public static boolean isRollback(int errorCode) {
    return (errorCode >= XAException.XA_RBBASE && errorCode <= XAException.XA_RBEND);
  }

  /**
   * Format an xid for printing.
   *
   * @param xid the xid to format
   * @return a string representation of the xid
   */
  protected static final String formatXid(Xid xid) {
    return "[" + xid.getFormatId() + ":" + Arrays.hashCode(xid.getGlobalTransactionId()) + ":" +
           Arrays.hashCode(xid.getBranchQualifier()) + "]";
  }

  /**
   * Format bitmasks defined by {@link XAResource}.
   *
   * @param flags  a bitmask composed from the constants defined in {@link XAResource}
   * @return a formatted representation of the <var>flags</var>
   */
  protected static final String formatFlags(int flags) {
    // Short-circuit evaluation if we've been explicitly passed no flags
    if (flags == XAResource.TMNOFLAGS) {
      return "TMNOFLAGS";
    }

    StringBuilder buffer = new StringBuilder();

    // Add any flags that are present
    for (Map.Entry<Integer,String> entry : flagMap.entrySet()) {
      int entryFlag = entry.getKey().intValue();

      // If this flag is present, add it to the formatted output and remove
      // from the bitmask
      if ((entryFlag & flags) == entryFlag) {
        if (buffer.length() > 0) {
          buffer.append(",");
        }
        buffer.append(entry.getValue());
        flags &= ~entryFlag;
      }
    }

    // We would expect to have removed all flags by this point
    // If there's some unknown flag we've missed, format it as hexadecimal
    if (flags != 0) {
      if (buffer.length() > 0) {
        buffer.append(",");
      }
      buffer.append("0x").append(Integer.toHexString(flags));
    }

    return buffer.toString();
  }
}
