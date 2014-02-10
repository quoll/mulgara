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
 * Contributor(s):
 *    Migration to AbstractXAResource copyright 2008 The Topaz Foundation
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.memory;

import org.mulgara.resolver.spi.AbstractXAResource;
import org.mulgara.resolver.spi.AbstractXAResource.RMInfo;
import org.mulgara.resolver.spi.AbstractXAResource.TxInfo;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.store.xa.SimpleXAResource;
import org.mulgara.store.xa.XAResolverSession;

/**
 * Implements the XAResource for the {@link MemoryResolver}.
 *
 * @created 2004-05-12
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:48 $
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technoogies, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
@SuppressWarnings("unused")
public class MemoryXAResource
    extends AbstractXAResource<RMInfo<MemoryXAResource.MemoryTxInfo>,MemoryXAResource.MemoryTxInfo> {
  private final XAResolverSession session;

  //
  // Constructor
  //

  /**
   * Construct a {@link MemoryXAResource} with a specified transaction timeout.
   *
   * @param transactionTimeout  transaction timeout period, in seconds
   * @param session             the underlying resolver-session to use
   * @param resolverFactory     the resolver-factory we belong to
   */
  public MemoryXAResource(int transactionTimeout,
                          XAResolverSession session,
                          ResolverFactory resolverFactory) {
    super(transactionTimeout, resolverFactory);
    this.session = session;
  }

  protected RMInfo<MemoryTxInfo> newResourceManager() {
    return new RMInfo<MemoryTxInfo>();
  }

  protected MemoryTxInfo newTransactionInfo() {
    MemoryTxInfo ti = new MemoryTxInfo();
    ti.session = session;
    return ti;
  }

  //
  // Methods implementing XAResource
  //

  protected void doStart(MemoryTxInfo tx, int flags, boolean isNew) throws Exception {
    if (flags == TMNOFLAGS || flags == TMJOIN) {
      tx.session.refresh(new SimpleXAResource[] {});
    }
  }

  protected void doEnd(MemoryTxInfo tx, int flags) {
  }

  protected int doPrepare(MemoryTxInfo tx) throws Exception {
    tx.session.prepare();
    return XA_OK;
  }

  protected void doCommit(MemoryTxInfo tx) throws Exception {
    tx.session.commit();
  }

  protected void doRollback(MemoryTxInfo tx) throws Exception {
    tx.session.rollback();
  }

  protected void doForget(MemoryTxInfo tx) {
  }

  static class MemoryTxInfo extends TxInfo {
    /** the underlying resolver-session to use */
    public XAResolverSession session;
  }
}
