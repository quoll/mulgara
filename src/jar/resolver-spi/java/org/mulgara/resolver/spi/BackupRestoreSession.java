
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
 * This file is an original work developed by Andrae Muys
 * Copyright (c) 2008 Andrae Muys andrae@muys.id.au
 * All Rights Reserved.
 */
package org.mulgara.resolver.spi;

// Third party packages

// Local packages
import org.mulgara.store.nodepool.NodePool;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.StringPoolException;
import org.mulgara.util.LongMapper;

/**
 * An interface to the string-pool required by the Backup and Restore
 * Operations.
 *
 *  Note: This interface is a temporary fix until the model-name deconflation
 *  work is put in place - at which point this we will hopefully be able to
 *  rely on the string-pool primitives in ResolverSession.
 *
 * @created 2008-03-25
 * @copyright &copy;2008 <a href="mailto:andrae@muys.id.au">Andrae Muys</a>
 * @licence Open Software Licence v3.0
 */

public interface BackupRestoreSession {

  public static final long NONE = NodePool.NONE;

  /**
   * Find a single object in the persistent string pool.
   * @param gNode The graph node to find.
   * @return the SPObject corresponding to <var>gNode</var>, or
   * <code>null</code> if the node is not in the pool.
   * @throws StringPoolException if an internal error occurs.
   */
  public SPObject findSPObject(long gNode) throws StringPoolException;

  /**
   * Gets an object that can map nodes from a resolver to other nodes specific to the resolver.
   * This provides a mechanism for resolvers to use optimizations specific to their node space.
   * @return A node-to-node mapper specific to the resolver.
   * @throws Exception An implementation specific exception.
   */
  public LongMapper getRestoreMapper() throws Exception;
}
