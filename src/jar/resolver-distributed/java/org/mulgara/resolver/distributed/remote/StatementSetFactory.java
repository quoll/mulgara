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

import java.rmi.RemoteException;
import java.util.Set;

import org.jrdf.graph.Triple;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Creates a Set of statements that be be shipped across a network.
 *
 * @created 2007-04-23
 * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class StatementSetFactory {
  
  /** The size for transitioning between a serializable set and a remote set. */
  static final long WATER_MARK = 0x2048L;

  public static Set<Triple> newStatementSet(Statements statements, ResolverSession session) throws TuplesException, GlobalizeException {
    // make sure the WATER_MARK refers to a set that is indexable by integer
    // assert (long)(int)WATER_MARK == WATER_MARK;
    if (statements.getRowUpperBound() < WATER_MARK) return new ShortGlobalStatementSet(statements, session);
    try {
      RemotePager<Triple> pager = new RemotePagerImpl<Triple>(Triple.class, new TripleSetAdaptor(statements, session));
      return new SetProxy<Triple>(pager);
    } catch (RemoteException re) {
      throw new TuplesException("Error accessing remote data", re);
    }
  }
}
