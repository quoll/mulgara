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

package org.mulgara.content.mbox;

// Java 2 standard packages
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

// Third party packages
import org.jrdf.graph.*;
import org.apache.log4j.Logger; // Apache Log4J

// Locally written packages
import org.mulgara.content.*;
import org.mulgara.content.mbox.parser.model.*;
import org.mulgara.content.mbox.parser.model.exception.*;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;

/**
 * Parses an RFC822 compliant mbox into {@link Statements};
 *
 * @created 2004-08-24
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:39 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MBoxStatements extends AbstractTuples implements Statements {

  /** Logger. */
  private static final Logger logger =
      Logger.getLogger(MBoxStatements.class.getName());

  /** Column index for subjects */
  public static final int SUBJECT = 0;

  /** Column index for predicates */
  public static final int PREDICATE = 1;

  /** Column index for predicates */
  public static final int OBJECT = 2;

  /** The session used to globalize the RDF nodes from the stream. */
  private ResolverSession resolverSession;

  /** The number of statements in the MBox data. */
  private long rowCount;

  /** The current row. If the cursor is not on a row, this will be <code>null</code> */
  private Triple tripleStatement;

  /** The MBox manager we will use to manage our mboxes */
  private MBoxManager mboxManager;

  /** The mbox we are dealing with */
  private MBox mbox;

  /** The location of the MBox file. */
  private URL url;

  /** The mapping of blank nodes to their localised value */
  protected static HashMap<Node,Long> blankNodeMap;

  /** The content object containing information about the mbox file */
  private Content content;

  //
  // Constructors
  //

  /**
   * Construct an mbox parser.
   *
   * @param content The content object allowing acces to an input stream
   * @param resolverSession  session against which to localize RDF nodes
   *
   * @throws IllegalArgumentException if <var>inputStream</var> or
   *   <var>resolverSession</var> are <code>null</code>
   * @throws NotModifiedException if these statements already exist in a valid
   *   cached copy
   * @throws TuplesException if the <var>inputStream</var> can't be parsed as
   *   mbox
   */
  MBoxStatements(Content content, ResolverSession resolverSession) throws
      NotModifiedException, TuplesException {

    // Validate "url" parameter
    if (content == null) {
      throw new IllegalArgumentException("Null \"content\" parameter");
    }

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }

    // Initialize fields
    try {
      this.url = content.getURI() == null ? null : content.getURI().toURL();
    } catch (MalformedURLException e) {
      this.url = null;
    }
    this.resolverSession = resolverSession;

    // Store the content object
    this.content = content;

    // Fix the magical column names for RDF statements
    setVariables(new Variable[] {new Variable("subject"),
                 new Variable("predicate"),
                 new Variable("object")});

    if (logger.isInfoEnabled()) {

      logger.info("!! Created MBox");
    }

    // Initialise blank node map
    blankNodeMap = new HashMap<Node,Long>();

    // Load in the RDF conversion of the given mbox url
    loadMBox();
  }

  /**
   * Load in the RDF conversion from the given URL to allow for navigation
   * and editing.
   *
   * @throws NotModifiedException if there's a valid cached version of this
   *   mbox
   * @throws TuplesException
   */
  private void loadMBox() throws NotModifiedException, TuplesException {

    if (logger.isInfoEnabled()) logger.info("!! Loading in mbox data");

    if (mboxManager == null) {

      // Container for the model factory
      ModelFactory factory = null;

      try {
        factory = ModelFactory.getInstance();
      } catch (FactoryException factoryException) {
        throw new TuplesException("Unable to initialise factory to create MBox parser.", factoryException);
      }

      try {
        // Initialise the mbox object using the factory
        mboxManager = factory.createMBoxManager();
      } catch (FactoryException factoryException) {
        throw new TuplesException("Unable to create a new mbox manager.", factoryException);
      }
    }

    try {
      // Get the mbox for our contentStream
      mbox = mboxManager.getMBox(content);
    } catch (ModelException modelException) {
      if (content.getURI() == null) {
        throw new TuplesException("Failed to create/retrieve MBox with content type " +
                                  content.getContentType(), modelException);
      }
      throw new TuplesException("Failed to create/retrieve MBox for URI " +
                               content.getURI().toString(), modelException);
    }

    try {
      // Perform the parsing and prepare for reading triples
      mbox.start();
    } catch (ModelException modelException) {
      if (content.getURI() == null) {
        throw new TuplesException("Failed to parse mbox data of type: " + content.getContentType(), modelException);
      }
      throw new TuplesException("Failed to parse mbox file: " + content.getURI().toString(), modelException);
    } catch (InvalidMBoxException invalidMBoxException) {
      logger.warn("MBox '" + content.getURI().toString() + "' was an invalid mbox file.", invalidMBoxException);

      try {
        // Remove the mbox from the cache
        mboxManager.delete(mbox);
      } catch (ModelException modelException) {
        // With the current implementation this shouldn't happen, but we
        // should still throw an exception just in case
        throw new TuplesException("Failed to delete invalid mbox from manager.", modelException);
      }

      // Since we can't use the file, throw a tuples exception and stop parsing
      throw new TuplesException("MBox '" + content.getURI().toString() +
                                "' was an invalid mbox file.", invalidMBoxException);

    } catch (VocabularyException vocabularyException) {
      throw new TuplesException("Unable to set up vocabulary for mbox parsing.", vocabularyException);
    }

    try {
      // Initialize the metadata now that we know the statements
      rowCount = mbox.getGraph().getNumberOfTriples();
    } catch (GraphException graphException) {
      throw new TuplesException("Unable to retrieve number of triples in graph.",graphException);
    }

    if (logger.isDebugEnabled()) logger.debug("Parsed MBox");
  }

  //
  // Methods implementing Statements
  //

  /**
   * Retrieves the value contained in the subject column for the current triple.
   *
   * @return The subject value for the current triple
   *
   * @throws TuplesException
   */
  public long getSubject() throws TuplesException {
    return getColumnValue(SUBJECT);
  }

  /**
   * Retrieves the value contained in the predicate column for the current triple.
   *
   * @return The predicate value for the current triple
   *
   * @throws TuplesException
   */
  public long getPredicate() throws TuplesException {
    return getColumnValue(PREDICATE);
  }

  /**
   * Retrieves the value contained in the object column for the current triple.
   *
   * @return The object value for the current triple
   *
   * @throws TuplesException
   */
  public long getObject() throws TuplesException {
    return getColumnValue(OBJECT);
  }

  //
  // Methods implementing AbstractTuples
  //

  /**
   * Resets the counter for triples to be the first.
   *
   * @param prefix The prefix to use
   * @param suffixTruncation The truncation of suffixes to use
   *
   * @throws TuplesException
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {

    try {
      // Reset the mbox
      mbox.reset();
    } catch (ModelException modelException) {
      throw new TuplesException("Unable to reset the MBox graph.", modelException);
    }

    if (logger.isDebugEnabled()) logger.debug("-- Getting the before first value");
  }

  public Object clone() {

    MBoxStatements cloned = (MBoxStatements)super.clone();

    // Copy immutable fields by reference
    cloned.resolverSession = resolverSession;
    cloned.rowCount = rowCount;
    cloned.tripleStatement = tripleStatement;
    cloned.url = url;

    return cloned;
  }

  /**
   * Close the RDF/XML formatted input stream.
   */
  public void close() throws TuplesException {
    resolverSession = null;
    tripleStatement = null;
    url = null;
  }

  /**
   * @param column  0 for the subject, 1 for the predicate, 2 for the object
   */
  public long getColumnValue(int column) throws TuplesException {

    // Pull the appropriate field from the current triple as a JRDF Node
    Node node = null;

    switch (column) {
      case SUBJECT:
        // Try creating the node with a URI reference
        node = tripleStatement.getSubject();
        break;
      case PREDICATE:
        // Try to create a URI reference node to represent the predicate
        node = tripleStatement.getPredicate();
        break;
      case OBJECT:
        // Create a literal node with the value for objects
        node = tripleStatement.getObject();
        break;
      default:
        throw new TuplesException("No such column " + column);
    }
    assert node != null;

    // Container for our result
    Long result = null;

    if (blankNodeMap.containsKey(node)) {
      // If the node is already mapped then get the value
      result = blankNodeMap.get(node);
    } else {
      // If we haven't mapped the node already then create a new value and store it

      // Localize the node and store the long object value
      try {
        result = new Long(resolverSession.localize(node));
      } catch (LocalizeException e) {
        throw new TuplesException("Couldn't get column " + column + " value", e);
      }

      // Store the new mapping
      blankNodeMap.put(node, result);
    }

    if (column == SUBJECT && logger.isInfoEnabled()) logger.info("!! Using node value of: " + result.longValue());

    return result.longValue();
  }

  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  public long getRowCount() throws TuplesException {
    // Since we don't know how many messages and how many triples within a
    // message we can't accurately determine the number of messages so we just
    // give the highest number we can to cater for large messages and mboxes
    return Long.MAX_VALUE;
  }

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowExpectedCount();
  }

  public boolean hasNoDuplicates() throws TuplesException {
    return false;
  }

  public boolean isColumnEverUnbound(int column) throws TuplesException {
    switch (column) {
      case 0:
      case 1:
      case 2:
        return false;
      default:
        throw new TuplesException("No such column " + column);
    }
  }

  public boolean next() throws TuplesException {
    try {
      // Get the next statement in the iterator
      tripleStatement = mbox.nextTriple();
    } catch (ModelException modelException) {
      throw new TuplesException("Failed to read next triple from mbox", modelException);
    }

    if (tripleStatement != null) {
      if (logger.isInfoEnabled()) logger.info("-- Getting next statement: " + tripleStatement.toString());
      // If there is a value for the triple then we have more data
      return true;
    } else {
      // There is no more data to navigate
      return false;
    }
  }
}
