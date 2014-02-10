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

package org.mulgara.resolver.url;

// Java 2 standard packages
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import org.xml.sax.*;

// Third party packages
import com.hp.hpl.jena.rdf.arp.ARP;  // ARP (Jena RDF/XML parser)
import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;
import org.apache.log4j.Logger;      // Apache Log4J
import org.jrdf.graph.*;             // JRDF

// Locally written packages
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.spi.StatementsWrapperResolution;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;

/**
 * Parses an {@link InputStream} into {@link Statements}.
 *
 * This particular implementation is complicated by the need to adapt the Jena
 * ARP RDF/XML &quot;push&quot; parser to be a &quot;pull&quot; parser instead.
 *
 * @created 2004-04-02
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:56 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class URLStatements extends AbstractTuples implements Statements {
  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(URLStatements.class.getName());

  /**
   * The session used to globalize the RDF nodes from the stream.
   */
  private ResolverSession resolverSession;

  private Map<String,BlankNode> blankNodeMap;

  /**
   * The current row.
   *
   * if the cursor is not on a row, this will be <code>null</code>
   */
  private Triple triple;

  /**
   * The location of the RDF/XML document.
   */
  private URL url;

  private Parser parser = null;

  private long rowCount;

  private boolean rowCountIsValid = false;

  //
  // Constructors
  //

  /**
   * Construct an RDF/XML stream parser.
   *
   * @param url  the location of the RDF/XML formatted document
   * @param resolverSession  session against which to localize RDF nodes
   * @param blankNodeMap  map of blank node IDs populated by any earlier
   *   parsings of the same document within the scope of the same
   *   <var>resolverSession</var>
   * @throws IllegalArgumentException if <var>inputStream</var> or
   *   <var>resolverSession</var> are <code>null</code>
   * @throws TuplesException if the <var>inputStream</var> can't be parsed as
   *   RDF/XML
   */
  URLStatements(URL url, ResolverSession resolverSession, Map<String,BlankNode> blankNodeMap) throws TuplesException {
    // Validate "url" parameter
    if (url == null) throw new IllegalArgumentException( "Null \"url\" parameter");

    // Validate "resolverSession" parameter
    if (resolverSession == null) throw new IllegalArgumentException("Null \"resolverSession\" parameter");

    // Validate "blankNodeMap" parameter
    if (blankNodeMap == null) throw new IllegalArgumentException("Null \"blankNodeMap\" parameter");

    // Initialize fields
    this.url             = url;
    this.resolverSession = resolverSession;
    this.blankNodeMap    = blankNodeMap;

    // Fix the magical column names for RDF statements
    setVariables(new Variable[] { new Variable("subject"),
                                  new Variable("predicate"),
                                  new Variable("object") });
  }

  //
  // Methods implementing Statements
  //

  public long getSubject() throws TuplesException
  {
    return getColumnValue(0);
  }

  public long getPredicate() throws TuplesException
  {
    return getColumnValue(1);
  }

  public long getObject() throws TuplesException
  {
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
   * {@link StatementsWrapperResolution} which {@link URLResolver} always
   * applies to this class before returning one.
   *
   * @param prefix {@inheritDoc}; for this particular implementation, non-zero
   *   length prefixes are not supported
   * @throws {@inheritDoc}; also if <var>prefix</var> is non-zero length
   */
  public void beforeFirst(long[] prefix, int suffixTruncation)
    throws TuplesException
  {
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
    parser = new Parser(url, blankNodeMap);
    parser.start();

    // TODO skip forward to the first triple that matches prefix
  }

  /**
   * The cursor position isn't cloned by this method.
   */
  public Object clone() {
    URLStatements cloned = (URLStatements) super.clone();

    // Copy immutable fields by reference
    cloned.resolverSession = resolverSession;
    cloned.url             = url;

    // Even though this a mutable field, want to share the blank node IDs
    cloned.blankNodeMap    = blankNodeMap;

    // The cursor position is not cloned.
    cloned.triple          = null;
    cloned.parser          = null;

    return cloned;
  }

  /**
   * Close the RDF/XML formatted input stream.
   */
  public void close() throws TuplesException
  {
    stopThread();
  }

  /**
   * @param column  0 for the subject, 1 for the predicate, 2 for the object
   */
  public long getColumnValue(int column) throws TuplesException
  {
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
        p = new Parser(url, blankNodeMap);
        p.start();
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
        // Create a new parser and consume the entire file.
        Parser p = new Parser(url, blankNodeMap);
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

/**
 * This {@link Runnable}
 */
class Parser extends Thread implements ErrorHandler, StatementHandler {
  /** Logger. */
  private static final Logger logger =
    Logger.getLogger(Parser.class.getName());

  private final int BUFFER_SIZE = 1000;

  /**
   * Maximum size that {@link #queue} can attain without the
   * parser deliberately blocking and waiting for it to drain.
   */
  private final int QUEUE_MAX_BUFFERS = 10;


  /**
   * The ARP parser instance to use.
   */
  private final ARP arp = new ARP();

  /**
   * Map ARP anonymous node IDs to {@link BlankNode}s.
   */
  private final Map<String,BlankNode> blankNodeMap;

  private URL url;

  private Triple[] headBuffer = null;

  private int headIndex = 0;

  private Triple[] tailBuffer = null;

  private int tailIndex = 0;

  /**
   * The queue of buffers of triples generated by the RDF/XML parser.
   */
  private LinkedList<Triple[]> queue = new LinkedList<Triple[]>();

  /**
   * The number of statements parsed so far.
   *
   * When {@link #complete} is <code>true</code>, this will be the number of
   * statements in the RDF/XML document.
   */
  private long statementCount = 0;

  /**
   * true if statementCount is the count of the total number of statements in
   * the entire file because the parser has reached the end of the file without
   * error.
   */
  private boolean statementCountIsTotal = false;

  /**
   * Flag used to indicate that the end of the RDF/XML file has been reached.
   */
  private boolean complete = false;

  /**
   * The exception which interrupted parsing, or <code>null</code> is parsing
   * is successful.
   */
  private Throwable exception = null;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   */
  @SuppressWarnings("deprecation")
  Parser(URL url, Map<String,BlankNode> blankNodeMap) {
    // Validate "url" parameter
    if (url == null) throw new IllegalArgumentException("Null \"url\" parameter");

    // Validate "blankNodeMap" parameter
    if (blankNodeMap == null) throw new IllegalArgumentException("Null \"blankNodeMap\" parameter");

    // Initialize fields
    this.url          = url;
    this.blankNodeMap = blankNodeMap;

    // Configure the RDF/XML parser
    arp.setEmbedding(true);
    arp.setLaxErrorMode();
    arp.setErrorHandler(this);
    arp.setStatementHandler(this);
  }

  /**
   * @return the number of statements parsed so far
   */
  synchronized long getStatementCount() throws TuplesException
  {
    checkForException();
    return statementCount;
  }

  /**
   * @return the total number of statements in the file
   */
  synchronized long waitForStatementTotal() throws TuplesException {
    while (!complete) {
      checkForException();

      // Keep the LinkedList drained.
      queue.clear();
      notifyAll();

      try {
        wait();
      } catch (InterruptedException ex) {
        throw new TuplesException("Abort");
      }
    }
    checkForException();
    assert statementCountIsTotal;
    return statementCount;
  }

  /**
   * Returns true if getStatementCount() would return the total number
   * of statements in the file.
   */
  synchronized boolean isStatementCountTotal() throws TuplesException {
    checkForException();
    return statementCountIsTotal;
  }

  //
  // Method implementing Runnable
  //

  public void run() {
    Throwable t = null;

    // Parse the stream into RDF statements
    try {
      InputStream in = url.openStream();

      // Guess at transfer encoding (compression scheme) based on file extension
      if (url.getPath().endsWith(".gz")) {
        // The file name ends with ".gz", so assume it's a gzip'ed file
        in = new GZIPInputStream(in);
      } else if (url.getPath().endsWith(".zip")) {
        // The file name ends with ".zip", so assume it's a zip'ed file
        in = new ZipInputStream(in);

        ((ZipInputStream)in).getNextEntry();
      }

      arp.load(in, url.toString());
      if (logger.isDebugEnabled()) {
        logger.debug("Parsed RDF/XML");
      }
      return;
    } catch (Throwable th) {
      t = th;
    } finally {
      flushQueue(t);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Exception while parsing RDF/XML", exception);
    }
  }

  //
  // Methods implementing StatementHandler
  //

  public void statement(AResource subject,
                        AResource predicate,
                        ALiteral object) {
    if (logger.isDebugEnabled()) {
      logger.debug("Parsed " + subject + " " + predicate + " " + object + " from " + url);
    }

    Triple triple = new TripleImpl((SubjectNode)   toNode(subject),
                                   (PredicateNode) toNode(predicate),
                                   (ObjectNode)    toNode(object));

    addTriple(triple);
  }

  public void statement(AResource subject,
                        AResource predicate,
                        AResource object) {
    if (logger.isDebugEnabled()) {
      logger.debug("Parsed " + subject + " " + predicate + " " + object + " from " + url);
    }

    Triple triple = new TripleImpl((SubjectNode)   toNode(subject),
                                   (PredicateNode) toNode(predicate),
                                   (ObjectNode)    toNode(object));

    addTriple(triple);
  }

  //
  // Methods implementing ErrorHandler
  //

  /**
   * Recoverable error.
   *
   * @param e PARAMETER TO DO
   */
  public synchronized void error(SAXParseException e) {
    exception = e;
    logger.error("Error, " + e.getLineNumber() + ", column " +
                 e.getColumnNumber() + ": " + e.getMessage(), e);
  }

  /**
   * Non-recoverable error.
   *
   * @param e PARAMETER TO DO
   */
  public synchronized void fatalError(SAXParseException e) {
    exception = e;
    logger.error("Fatal error, line " + e.getLineNumber() + ", column " +
                 e.getColumnNumber() + ": " + e.getMessage(), e);
  }

  /**
   * Warning.
   *
   * @param e PARAMETER TO DO
   */
  public void warning(SAXParseException e) {
    logger.warn("Warning, line " + e.getLineNumber() + ", column " +
                e.getColumnNumber() + ": " + e.getMessage(), e);
  }

  //
  // Internal methods
  //

  /**
   * Create a JRDF {@link Literal} object from an ARP literal object.
   *
   * @param literal  the ARP literal
   * @return a JRDF literal
   */
  private Literal toNode(ALiteral literal) {
    URI type = null;
    if (literal.getDatatypeURI() != null) {
      try {
        type = new URI(literal.getDatatypeURI());
      } catch (URISyntaxException e) {
        throw new Error("ARP generated datatype for " + literal + " which isn't a URI", e);
      }
    }

    String lang = literal.getLang();
    if (type == null) {
      if (lang == null) lang = "";
    } else {
      lang = null;
    }

    if (type == null) {
      return new LiteralImpl(literal.toString(), lang);
    } else {
      return new LiteralImpl(literal.toString(), type);
    }
  }

  /**
   * Create a JRDF {@link Node} from an ARP resource object.
   *
   * @param resource  the ARP resource.
   * @return the JRDF {@link URIReference} or {@link BlankNode}
   */
  private Node toNode(AResource resource) {
    if (resource.isAnonymous()) {
      // Generate a blank node
      String anonymousID = resource.getAnonymousID();

      BlankNode blankNode = blankNodeMap.get(anonymousID);
      if (blankNode == null) blankNode = new BlankNodeImpl();
      assert blankNode != null;

      // Associate this new blank node with its anonymous ID and return it
      blankNodeMap.put(anonymousID, blankNode);
      return blankNode;
    } else {
      // Generate a URI reference
      try {
        return new URIReferenceImpl(new URI(resource.getURI().toString()));
      } catch (URISyntaxException e) {
        throw new Error("ARP generated a malformed URI", e);
      }
    }
  }

  /**
   * If an exception occurred in the parser, throws a TuplesException that
   * wraps the exception.
   */
  private void checkForException() throws TuplesException {
    if (exception != null) {
      queue.clear();
      headIndex = 0;
      headBuffer = null;
      throw new TuplesException("Exception while reading " + url, exception);
    }
  }

  /**
   * Returns a new triple from the queue or null if there are no more triples.
   */
  Triple getTriple() throws TuplesException {
    if (headBuffer == null || headIndex >= headBuffer.length) {
      // Get another buffer from the queue.
      headIndex = 0;
      headBuffer = null;
      headBuffer = getBufferFromQueue();
      if (headBuffer == null) {
        // No more triples.
        return null;
      }
      assert headBuffer.length > 0;
    }

    // Get a triple from the headBuffer.
    Triple triple = headBuffer[headIndex];
    headBuffer[headIndex++] = null;
    assert triple != null;
    return triple;
  }

  private synchronized Triple[] getBufferFromQueue() throws TuplesException {
    while (queue.isEmpty()) {
      checkForException();
      if (complete) {
        // No more buffers in the queue.
        return null;
      }

      // Wait for a buffer.
      try {
        wait();
      } catch (InterruptedException ex) {
        throw new TuplesException("Abort");
      }
    }
    checkForException();

    notifyAll();
    return (Triple[]) queue.removeFirst();
  }

  private void addTriple(Triple triple) {
    assert triple != null;
    if (tailBuffer == null) {
      tailBuffer = new Triple[BUFFER_SIZE];
      tailIndex = 0;
    }
    tailBuffer[tailIndex++] = triple;

    if (tailIndex >= tailBuffer.length) {
      // Add the buffer to the queue.
      addBufferToQueue(tailBuffer);
      tailBuffer = null;
      tailIndex = 0;
    }
  }

  private synchronized void flushQueue(Throwable t) {
    if (interrupted()) {
      if (t == null) {
        t = new InterruptedException();
      }
    }

    if (t != null) {
      exception = t;
      queue.clear();
    } else if (exception == null) {
      // End of file has been reached without error.
      if (tailBuffer != null) {
        // There is at least one triple in the tailBuffer.
        assert tailIndex > 0;
        Triple[] buf = new Triple[tailIndex];
        System.arraycopy(tailBuffer, 0, buf, 0, tailIndex);
        addBufferToQueue(buf);
      }
      statementCountIsTotal = true;
    } else {
      // An exception has already been reported.
      queue.clear();
    }
    tailBuffer = null;
    tailIndex = 0;
    complete = true;
    notifyAll();
  }

  private synchronized void addBufferToQueue(Triple[] buffer) {
    assert buffer != null;
    // Wait for the queue to drain a bit if it's too full
    while (queue.size() >= QUEUE_MAX_BUFFERS) {
      try {
        wait();
      } catch (InterruptedException ex) {
        throw new RuntimeException("Abort");
      }
    }
    queue.addLast(buffer);
    statementCount += buffer.length;
    notifyAll();
  }

  /**
   * Stops the thread.
   */
  synchronized void abort() {
    interrupt();

    // Clear the triples list and notify in case ARP uses an internal thread
    // which has become blocked on the list being MAX_TRIPLES in size.
    queue.clear();
    notifyAll();
  }

}
