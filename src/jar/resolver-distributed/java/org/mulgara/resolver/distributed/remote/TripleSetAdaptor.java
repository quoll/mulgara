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

import java.util.AbstractSet;
import java.util.Iterator;

import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Converts a StatementSet into a List of Triple.
 *
 * @created 2007-05-14
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class TripleSetAdaptor extends AbstractSet<Triple> {

  /** The underlying set of statements. */
  private Statements statements;
  
  /** A ResolverSession for use in globalizing nodes. */
  private ResolverSession session;
  
  /**
   * Builds a new iterable List of Triple, based on a Statements.
   * @param statements The statements to adapt to a List.
   */
  public TripleSetAdaptor(Statements statements, ResolverSession session) {
    this.statements = statements;
    this.session = session;
  }
  
  
  @Override
  public int size() {
    try {
      long size = statements.getRowCount();
      return (long)(int)size == size ? (int)size : Integer.MAX_VALUE;
    } catch (TuplesException te) {
      throw new RuntimeException("Unable to get result size", te);
    }
  }


  @Override
  public Iterator<Triple> iterator() {
    return new TripleCursorIterator(statements);
  }
  
  
  /**
   * An iterator class for Cursor objects.
   */
  public class TripleCursorIterator implements Iterator<Triple> {
    
    /** The cursor to iterate on. */
    private Statements s;

    /** A flag to indicate if there is more data. */
    private boolean hasNext;
    
    TripleCursorIterator(Statements s) {
      this.s = s;
      try {
        hasNext = s.getRowCardinality() != 0;
      } catch (TuplesException te) {
        throw new RuntimeException("Unable to access result size", te);
      }
    }

    public boolean hasNext() {
      return hasNext;
    }

    public Triple next() {
      try {
        hasNext = s.next();
        return new TripleImpl(
            (SubjectNode)session.globalize(statements.getSubject()),
            (PredicateNode)session.globalize(statements.getPredicate()),
            (ObjectNode)session.globalize(statements.getObject())
        );
      } catch (TuplesException te) {
        throw new RuntimeException("Unable to access result data", te);
      } catch (GlobalizeException ge) {
        throw new RuntimeException("Unable to globalize result data", ge);
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
  

}
