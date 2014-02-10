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

package org.mulgara.content.n3;

// Java 2 standard packages

// Third party packages
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;      // Apache Log4J
import org.jrdf.graph.*;             // JRDF

// Locally written packages
import org.mulgara.content.Content;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.NotModifiedTuplesException;
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.StatementsWrapperResolution;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;

/**
 * Parses an {@link java.io.InputStream} into {@link Statements}.
 *
 * This particular implementation is complicated by the need to adapt the Jena
 * N3 <q>push</q> parser to be a <q>pull</q> parser instead.
 *
 * @created 2004-04-02
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:02 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class N3Statements extends AbstractTuples implements Statements {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(N3Statements.class.getName());

  /** The RDF/XML formatted document. */
  private Content content;

  /** The session used to globalize the RDF nodes from the stream. */
  private ResolverSession resolverSession;

  /** The current row. if the cursor is not on a row, this will be <code>null</code> */
  private Triple triple;

  private Parser parser = null;

  private long rowCount;

  private boolean rowCountIsValid = false;

  //
  // Constructors
  //

  /**
   * Construct a Notation-3 stream parser.
   *
   * @param content  the Notation-3 content
   * @param resolverSession  session against which to localize RDF nodes
   * @throws IllegalArgumentException if <var>inputStream</var> or
   *   <var>resolverSession</var> are <code>null</code>
   */
  N3Statements(Content content, ResolverSession resolverSession) {
    // Validate "content" parameter
    if (content == null) {
      throw new IllegalArgumentException( "Null \"content\" parameter");
    }

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }

    // Initialize fields
    this.content         = content;
    this.resolverSession = resolverSession;

    // Fix the magical column names for RDF statements
    setVariables(new Variable[] { new Variable("subject"),
                                  new Variable("predicate"),
                                  new Variable("object") });
  }

  //
  // Methods implementing Statements
  //

  public long getSubject() throws TuplesException {
    return getColumnValue(0);
  }

  public long getPredicate() throws TuplesException {
    return getColumnValue(1);
  }

  public long getObject() throws TuplesException {
    return getColumnValue(2);
  }

  //
  // Methods implementing AbstractTuples
  //

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
    if (logger.isDebugEnabled()) {
      logger.debug("Before first");
    }

    // Validate "prefix" parameter
    if (prefix == null) {
      throw new IllegalArgumentException("Null \"prefix\" parameter");
    }
    if (prefix.length != 0) {
      throw new TuplesException(
        getClass() + ".beforeFirst isn't implemented for non-zero length prefix"
      );
    }

    // Validate "suffixTruncation" parameter
    if (suffixTruncation != 0) {
      throw new IllegalArgumentException("Null \"suffixTruncation\" parameter");
    }

    // Shut down any existing parsing thread
    if (parser != null) {
      stopThread();
    }

    // Create the parser and start the parsing thread
    try {
      parser = new Parser(content, resolverSession);
    } catch (NotModifiedException e) {
      throw new NotModifiedTuplesException(e);
    }
    parser.start();

    // TODO skip forward to the first triple that matches prefix
  }

  /**
   * The cursor position isn't cloned by this method.
   */
  public Object clone() {
    N3Statements cloned = (N3Statements) super.clone();

    // Copy immutable fields by reference
    cloned.content         = content;
    cloned.resolverSession = resolverSession;

    // The cursor position is not cloned.
    cloned.triple          = null;
    cloned.parser          = null;

    return cloned;
  }

  /**
   * Close the RDF/XML formatted input stream.
   */
  public void close() throws TuplesException {
    stopThread();
  }

  /**
   * @param column  0 for the subject, 1 for the predicate, 2 for the object
   */
  public long getColumnValue(int column) throws TuplesException {
    if (triple == null) {
      throw new TuplesException("There is no current row");
    }

    // Pull the appropriate field from the current triple as a JRDF Node
    Node node;
    switch (column) {
    case 0:  node = triple.getSubject();   break;
    case 1:  node = triple.getPredicate(); break;
    case 2:  node = triple.getObject();    break;
    default: throw new TuplesException("No such column " + column);
    }
    assert node != null;

    // Localize the node
    try {
      return resolverSession.localize(node);
    }
    catch (LocalizeException e) {
      throw new TuplesException("Couldn't get column " + column + " value", e);
    }
  }

  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  public int getRowCardinality() throws TuplesException {
    long statementCount;

    if (rowCountIsValid) {
      statementCount = rowCount;
    } else {
      Parser p;
      boolean newParser;
      if (parser != null) {
        // Use the existing parser.
        p = parser;
        newParser = false;
      } else {
        // Create a new parser.
        try {
          p = new Parser(content, resolverSession);
          p.start();
        } catch (NotModifiedException e) {
          throw new NotModifiedTuplesException(e);
        }
        newParser = true;
      }

      // We can do this since the queue holds more than two triples.
      try {
        synchronized (p) {
          while (p.getStatementCount() < 2 && !p.isStatementCountTotal()) {
            try {
              // Wait on the parser for changes to the statement count or
              // completion status.
              p.wait();
            } catch (InterruptedException ex) {
              throw new TuplesException("Abort");
            }
          }
          statementCount = p.getStatementCount();
        }
      } catch (TuplesException ex) {
        p.abort();
        if (!newParser) {
          // We just aborted the main parser, so nullify the reference.
          parser = null;
        }
        throw ex; // rethrow.
      } finally {
        if (newParser) {
          // Stop the thread.
          p.abort();
        }
      }
    }

    // Convert the statement count into a cardinality class
    return statementCount == 0 ? Cursor.ZERO :
           statementCount == 1 ? Cursor.ONE :
                                 Cursor.MANY;
  }

  public long getRowCount() throws TuplesException {
    if (!rowCountIsValid) {
      if (parser != null && parser.isStatementCountTotal()) {
        // Get the statement count from the parser.
        rowCount = parser.getStatementCount();
      } else {
        // Create a new parser.
        Parser p;
        try {
          p = new Parser(content, resolverSession);
        } catch (NotModifiedException e) {
          throw new NotModifiedTuplesException(e);
        }

        // Consume the entire file.
        p.start();
        try {
          rowCount = p.waitForStatementTotal();
        } finally {
          p.abort();
        }
      }
      rowCountIsValid = true;
    }
    return rowCount;
  }

  public long getRowUpperBound() throws TuplesException {
    // If the row count isn't yet available, return an absurdly huge value
    return parser != null && parser.isStatementCountTotal() ?
           parser.getStatementCount() : Long.MAX_VALUE;
  }

  /** Guess at a large number */
  private static final Long LARGE_FILE_SIZE = 1000000L;

  public long getRowExpectedCount() throws TuplesException {
    // If the row count isn't yet available, return an absurdly huge value
    return parser != null && parser.isStatementCountTotal() ?
           parser.getStatementCount() : LARGE_FILE_SIZE;
  }

  public boolean hasNoDuplicates() throws TuplesException {
    return false;
  }

  public boolean isColumnEverUnbound(int column) throws TuplesException {
    switch (column) {
    case 0: case 1: case 2:
      return false;
    default:
      throw new TuplesException("No such column " + column);
    }
  }

  public boolean next() throws TuplesException {
    if (parser == null) {
      // no current row
      return false;
    }

    try {
      triple = parser.getTriple();
    } catch (TuplesException ex) {
      stopThread();
      throw ex; // rethrow
    }

    if (triple == null) {
      // Hit the end of the file.
      assert parser.isStatementCountTotal();
      rowCount = parser.getStatementCount();
      rowCountIsValid = true;
      stopThread();
    }
    return triple != null;
  }

  /**
   * Stops the thread if it is running, and clears the current row.
   */
  private void stopThread() {
    if (parser != null) {
      parser.abort();
      parser = null;
    }
    triple = null;
  }

}
