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


package org.mulgara.content.rlog;

// Java 2 standard packages

// Third party packages
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;      // Apache Log4J

// Locally written packages
import org.mulgara.content.Content;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.NotModifiedTuplesException;
import org.mulgara.krule.rlog.Interpreter;
import org.mulgara.krule.rlog.ParseException;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.StatementsWrapperResolution;
import org.mulgara.store.nodepool.NodePoolException;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;

import org.mulgara.krule.rlog.Rlog;
import org.mulgara.krule.rlog.ast.output.KruleGenerator;
import org.mulgara.krule.rlog.parser.TypeException;
import org.mulgara.krule.rlog.parser.URIParseException;

/**
 * Parses an {@link java.io.InputStream} into {@link Statements}.
 * This parser uses memory and does not stream.
 *
 * @created Feb 24, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RlogStatements extends AbstractTuples implements Statements {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(RlogStatements.class.getName());

  /** The session used to globalize the RDF nodes from the stream. */
  private ResolverSession resolverSession;

  private Interpreter rlogParser = null;

  private List<long[]> triples = null;

  /** The current row iterator. If the cursor is not on a row, this will be <code>null</code> */
  private Iterator<long[]> tripleIterator = null;

  /** The current row. If the cursor is not on a row, this will be <code>null</code> */
  private long[] triple;

  /** If this exception is not null then it has yet to be thrown to the calling process */
  TuplesException outstandingException = null;

  /**
   * Construct an RLog parser.
   *
   * @param content the RLog content
   * @param resolverSession session against which to localize RDF nodes
   * @throws IllegalArgumentException if <var>inputStream</var> or <var>resolverSession</var> are <code>null</code>
   */
  RlogStatements(Content content, ResolverSession resolverSession) {
    if (content == null) throw new IllegalArgumentException( "Null \"content\" parameter");
    if (resolverSession == null) throw new IllegalArgumentException("Null \"resolverSession\" parameter");

    this.resolverSession = resolverSession;

    // Fix the magical column names for RDF statements
    setVariables(new Variable[] { SUBJECT, PREDICATE, OBJECT } );

    try {
      InputStreamReader input = new InputStreamReader(content.newInputStream());
      try {
        rlogParser = new Rlog(input, content.getURI());
      } finally {
        input.close();
      }
    } catch (IOException e) {
      outstandingException = new TuplesException("Unable to access RLog data", e);
    } catch (ParseException e) {
      outstandingException = new TuplesException("Bad RLog structure", e);
    } catch (TypeException e) {
      outstandingException = new TuplesException("Head of rule has the wrong type", e);
    } catch (URIParseException e) {
      outstandingException = new TuplesException("Illegal URI in document", e);
    } catch (NotModifiedException e) {
      outstandingException = new TuplesException("Unexpected duplicate access to RLog file", e);
    }
  }


  public long getSubject() throws TuplesException {
    return getColumnValue(0);
  }

  public long getPredicate() throws TuplesException {
    return getColumnValue(1);
  }

  public long getObject() throws TuplesException {
    return getColumnValue(2);
  }


  /**
   * {@inheritDoc}
   *
   * Non-zero length <var>prefix</var> values don't need to be supported by
   * this class because prefix filtration is implemented by the
   * {@link StatementsWrapperResolution} which the existing external resolvers
   * always apply to their content before returning it.
   *
   * @param prefix {@inheritDoc}; for this particular implementation, non-zero
   *   length prefixes are not supported
   * @throws NotModifiedTuplesException if the underlying tuples are cached
   * @throws TuplesException {@inheritDoc}; also if <var>prefix</var> is non-zero length
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    if (logger.isDebugEnabled()) logger.debug("Before first");

    // If the parser found an error, then throw it now
    if (outstandingException != null) throw outstandingException;

    // Validate "prefix" parameter
    if (prefix == null) throw new IllegalArgumentException("Null \"prefix\" parameter");
    if (prefix.length != 0) {
      throw new TuplesException(getClass() + ".beforeFirst isn't implemented for non-zero length prefix");
    }

    // Validate "suffixTruncation" parameter
    if (suffixTruncation != 0) throw new IllegalArgumentException("Null \"suffixTruncation\" parameter");

    tripleIterator = getTriples().iterator();
  }


  /**
   * The cursor position isn't cloned by this method.
   */
  public Object clone() {
    RlogStatements cloned = (RlogStatements)super.clone();
    cloned.triple = null;
    return cloned;
  }


  /**
   * Parsing is not streamed, so the data stream is already closed.
   */
  public void close() throws TuplesException {
    // no op
  }


  /**
   * Get the localized value for the column.
   * @param column 0 for the subject, 1 for the predicate, 2 for the object
   * @return the localized GNode for the column
   */
  public long getColumnValue(int column) throws TuplesException {
    if (triple == null) throw new TuplesException("There is no current row");
    if (column >= 3) throw new IllegalArgumentException("Index out of bounds for triple: " + column);
    return triple[column];
  }


  /**
   * Get the operands this Tuples represents. Since this object is atomic, there are not operands.
   * @see org.mulgara.store.tuples.Tuples#getOperands()
   */
  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }


  /**
   * @see org.mulgara.store.tuples.AbstractTuples#getRowCardinality()
   */
  public int getRowCardinality() throws TuplesException {
    long statementCount = getTriples().size();

    // Convert the statement count into a cardinality class
    return statementCount == 0 ? Cursor.ZERO :
           statementCount == 1 ? Cursor.ONE :
                                 Cursor.MANY;
  }


  /**
   * @see org.mulgara.store.tuples.AbstractTuples#getRowCount()
   */
  public long getRowCount() throws TuplesException {
    return getTriples().size();
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#getRowUpperBound()
   */
  public long getRowUpperBound() throws TuplesException {
    return getTriples().size();
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#getRowExpectedCount()
   */
  public long getRowExpectedCount() throws TuplesException {
    return getTriples().size();
  }

  /**
   * @see org.mulgara.store.tuples.Tuples#hasNoDuplicates()
   */
  public boolean hasNoDuplicates() throws TuplesException {
    return false;
  }

  /**
   * @see org.mulgara.store.tuples.AbstractTuples#isColumnEverUnbound(int)
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    if (column > 2 || column < 0) throw new TuplesException("No such column " + column);
    return false;
  }


  /**
   * @see org.mulgara.store.tuples.AbstractTuples#next()
   */
  public boolean next() throws TuplesException {
    if (!tripleIterator.hasNext()) {
      tripleIterator = null;
      triple = null;
      return false;
    }
    triple = tripleIterator.next();
    return true;
  }


  /**
   * Retrieve the triples from the parser.
   * @return A List of all the triples.
   * @throws TuplesException if there was a Node pool or string pool persistence or access error,
   *         or if the RLog file contained bad data that wasn't caught in the initial parse.
   */
  private List<long[]> getTriples() throws TuplesException {
    try {
      if (triples == null) {
        KruleGenerator generator;
        generator = new KruleGenerator(rlogParser, resolverSession);
        triples = new ArrayList<long[]>();
        generator.emit(triples);
      }
    } catch (NodePoolException e) {
      throw new TuplesException("Unable to create new nodes during RLog parsing.", e);
    } catch (LocalizeException e) {
      throw new TuplesException("Unable to localize data during RLog parsing.", e);
    } catch (ParseException e) {
      throw new TuplesException("Errors in RLog file.", e);
    }
    return triples;
  }
}
