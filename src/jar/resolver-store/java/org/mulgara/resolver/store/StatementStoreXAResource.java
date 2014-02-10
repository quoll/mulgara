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
 *   Migration to AbstractXAResource copyright 2008 The Topaz Foundation
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.store;

// Java 2 standard packages
import java.util.HashSet;
import java.util.Set;

// Third party packages
import org.apache.log4j.Logger;

import org.mulgara.resolver.spi.AbstractXAResource;
import org.mulgara.resolver.spi.AbstractXAResource.RMInfo;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.store.xa.SimpleXAResource;
import org.mulgara.store.xa.SimpleXAResourceException;
import org.mulgara.store.xa.XAResolverSession;
import org.mulgara.resolver.spi.AbstractXAResource.TxInfo;

/**
 * Implements the XAResource for the {@link StatementStoreResolver}.
 *
 * @created 2004-05-12
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:55 $
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technoogies, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class StatementStoreXAResource
    extends AbstractXAResource<RMInfo<StatementStoreXAResource.StatementStoreTxInfo>, StatementStoreXAResource.StatementStoreTxInfo> {
  /** Logger.  */
  private static final Logger logger =
    Logger.getLogger(StatementStoreXAResource.class.getName());

  // Used to prevent multiple calls to prepare on the store layer.
  // Set of session's that have been prepared.
  private static final Set<XAResolverSession> preparing = new HashSet<XAResolverSession>();

  private final XAResolverSession session;

  private final SimpleXAResource[] resources;

  //
  // Constructor
  //

  /**
   * Construct a {@link StatementStoreXAResource} with a specified transaction timeout.
   *
   * @param transactionTimeout  transaction timeout period, in seconds
   * @param session             the underlying resolver-session to use
   * @param resources           
   * @param resolverFactory     the resolver-factory we belong to
   */
  public StatementStoreXAResource(int transactionTimeout,
                                  XAResolverSession session,
                                  SimpleXAResource[] resources,
                                  ResolverFactory resolverFactory) {
    super(transactionTimeout, resolverFactory);
    this.session = session;
    this.resources = resources;
  }

  protected RMInfo<StatementStoreTxInfo> newResourceManager() {
    return new RMInfo<StatementStoreTxInfo>();
  }

  protected StatementStoreTxInfo newTransactionInfo() {
    StatementStoreTxInfo ti = new StatementStoreTxInfo();
    ti.session = session;
    ti.resources = resources;
    return ti;
  }

  //
  // Methods implementing XAResource
  //

  protected void doStart(StatementStoreTxInfo tx, int flags, boolean isNew) throws Exception {
    if (flags == TMNOFLAGS) {
      tx.session.refresh(tx.resources);
    }
  }

  protected void doEnd(StatementStoreTxInfo tx, int flags) {
  }

  protected int doPrepare(StatementStoreTxInfo tx) throws Exception {
    synchronized (preparing) {
      if (preparing.contains(tx.session)) {
        return XA_OK;
      } else {
        preparing.add(tx.session);
      }
    }

    try {
      tx.session.prepare();
    } catch (SimpleXAResourceException es) {
      synchronized (preparing) {
        preparing.remove(tx.session);
      }
      throw es;
    }

    return XA_OK;
  }

  protected void doCommit(StatementStoreTxInfo tx) throws Exception {
    try {
      tx.session.commit();
    } finally {
      cleanup("commit", tx);
    }
  }

  protected void doForget(StatementStoreTxInfo tx) throws Exception {
    try {
      synchronized (preparing) {
        if (preparing.contains(tx.session)) {
          doRollback(tx);
        }
      }
    } finally {
      cleanup("forget", tx);
    }
  }

  protected void doRollback(StatementStoreTxInfo tx) throws Exception {
    try {
      tx.session.rollback();
    } finally {
      cleanup("rollback", tx);
    }
  }

  //
  // Internal methods
  //

  private void cleanup(String operation, StatementStoreTxInfo tx) {
    if (logger.isDebugEnabled()) {
      logger.debug("Performing cleanup from " + operation);
    }
    try {
      synchronized (preparing) {
        if (preparing.contains(tx.session)) {
          preparing.remove(tx.session);
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug("Already committed/rolledback in this transaction");
          }
        }
      }
    } finally {
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Releasing session after " + operation + " " + tx.session);
        }
        tx.session.release();
        tx.session = null;
      } catch (SimpleXAResourceException es) {
        logger.error("Attempt to release store failed", es);
      }
    }
  }

  static class StatementStoreTxInfo extends TxInfo {
    /** the underlying resolver-session to use */
    public XAResolverSession session;

    /** the underlying resources */
    public SimpleXAResource[] resources;
  }
}
