/*
 * Copyright 2010 Paul Gearon.
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
package org.mulgara.content.rdfa;

import java.util.Collections;
import java.util.List;

import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.mulgara.content.Content;
import org.mulgara.content.NotModifiedException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;

/**
 * This class works in tandem with a parser to provide Statements with a cursor
 * interface. The parser keeps a buffer, which this class sucks out of that
 * buffer to provide statements as required.
 */
public class RdfaStatements extends AbstractTuples implements Statements {

  /** The resolver session to convert parsed data into localized data. */
  ResolverSession session;

  /** The source of the statements. */
  StatementParser source;

  /** The current row. */
  Triple currentRow = null;

  /** Flag to indicate that the {@link #beforeFirst()} method has been called. */
  boolean beforeFirstCalled = false;

  /**
   * Initialize the statements with the parser that is to be the source
   * of the statements.
   */
  public RdfaStatements(ResolverSession session, Content content) throws TuplesException, NotModifiedException {
    this.session = session;
    source = new StatementParser(content, session);
    new Thread(source).start();
  }

  /**
   * @see org.mulgara.store.tuples.Tuples#hasNoDuplicates()
   */
  public boolean hasNoDuplicates() throws TuplesException {
    // no idea, so return false
    return false;
  }

  /**
   * @see org.mulgara.store.tuples.Tuples#getOperands()
   */
  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  /**
   * @see org.mulgara.resolver.spi.Statements#getSubject()
   */
  @Override
  public long getSubject() throws TuplesException {
    SubjectNode s = currentRow.getSubject();
    try {
      return session.localize(s);
    } catch (LocalizeException e) {
      throw new TuplesException("Unable to localize subject: " + s);
    }
  }

  /**
   * @see org.mulgara.resolver.spi.Statements#getPredicate()
   */
  @Override
  public long getPredicate() throws TuplesException {
    PredicateNode p = currentRow.getPredicate();
    try {
      return session.localize(p);
    } catch (LocalizeException e) {
      throw new TuplesException("Unable to localize predicate: " + p);
    }
  }

  /**
   * @see org.mulgara.resolver.spi.Statements#getObject()
   */
  @Override
  public long getObject() throws TuplesException {
    ObjectNode o = currentRow.getObject();
    try {
      return session.localize(o);
    } catch (LocalizeException e) {
      throw new TuplesException("Unable to localize object: " + o);
    }
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#getColumnValue(int)
   */
  @Override
  public long getColumnValue(int column) throws TuplesException {
    if (column == 0) return getSubject();
    if (column == 1) return getPredicate();
    if (column == 2) return getObject();
    throw new IndexOutOfBoundsException("Statements have 3 columns: " + column);
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#getRowUpperBound()
   */
  public long getRowUpperBound() throws TuplesException {
    // go for the max number of integers, not longs, since this is more reasonable
    return source.isFinished() ? source.getStatementCount() : Integer.MAX_VALUE;
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#getRowExpectedCount()
   */
  public long getRowExpectedCount() throws TuplesException {
    return source.isFinished() ? source.getStatementCount() : Short.MAX_VALUE;
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#isColumnEverUnbound(int)
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return false;
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#beforeFirst(long[], int)
   */
  @Override
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    // Validate params
    if (prefix != null && prefix.length != 0) {
      throw new IllegalArgumentException("Prefix on RDFa statements must be empty");
    }
    if (suffixTruncation != 0) {
      throw new IllegalArgumentException("Null \"suffixTruncation\" parameter");
    }

    if (beforeFirstCalled) throw new TuplesException("RDFa statements do not support rewinding");
    beforeFirstCalled = true;
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#next()
   */
  @Override
  public boolean next() throws TuplesException {
    try {
      currentRow = source.getTriple();
    } catch (TuplesException ex) {
      source.terminate();
      throw ex;
    }
    assert currentRow != null;

    if (currentRow == StatementParser.TERMINATOR) rowCount = source.getStatementCount();
    return currentRow != StatementParser.TERMINATOR;
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#close()
   */
  @Override
  public void close() throws TuplesException {
    source.terminate();
  }

}
