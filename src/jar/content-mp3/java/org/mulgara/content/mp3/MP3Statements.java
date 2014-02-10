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
 * Contributor(s): N/A.5
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.content.mp3;

// Java 2 standard packages
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

// Apache Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;
import org.jrdf.util.ClosableIterator;

// Mp3 Library
import org.blinkenlights.id3.*;

// Locally written packages
import org.mulgara.content.Content;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.mp3.parser.*;
import org.mulgara.content.mp3.parser.api.*;
import org.mulgara.content.mp3.parser.exception.*;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.util.TempDir;

/**
 * Parses an MP3's ID3 tag into {@link Statements};
 *
 * @created 2004-08-13
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/02/02 21:13:04 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MP3Statements extends AbstractTuples implements Statements {

  /** Logger. */
  private static final Logger logger =
      Logger.getLogger(MP3Statements.class.getName());

  /** Column index for subjects */
  public static final int SUBJECT = 0;

  /** Column index for predicates */
  public static final int PREDICATE = 1;

  /** Column index for predicates */
  public static final int OBJECT = 2;

  /** The session used to globalize the RDF nodes from the stream. */
  private ResolverSession resolverSession;

  /** The number of statements in the ID3 tag. */
  private long rowCount;

  /** The current row. If the cursor is not on a row, this will be <code>null</code> */
  private Triple tripleStatement;

  /** An interator into triples positioned at the next triple. */
  private ClosableIterator<Triple> nextTriple;

  /** The content representing the MP3 file */
  private Content content;

  /** The model which will store the content of parsed mp3 files */
  private Graph model;

  /**
   * Map ARP anonymous node IDs to {@link BlankNode}s.
   *
   * This is <code>null</code> if no parsing is in progress.
   */
  private Map<Node,Long> blankNodeMap = new HashMap<Node,Long>();

  //
  // Constructors
  //

  /**
   * Construct an RDF/XML stream parser.
   *
   * @param content  the content object representing our MP3 file
   * @param resolverSession  session against which to localize RDF nodes
   * @throws IllegalArgumentException if <var>inputStream</var> or
   *   <var>resolverSession</var> are <code>null</code>
   * @throws NotModifiedException if there's a valid cached copy of these
   *   statements
   * @throws TuplesException if the <var>inputStream</var> can't be parsed as
   *   RDF/XML
   */
  MP3Statements(Content content, ResolverSession resolverSession) throws
      NotModifiedException, TuplesException {

    // Validate "content" parameter
    if (content == null) {
      throw new IllegalArgumentException("Null \"content\" parameter");
    }

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }

    // Initialize fields
    this.content = content;
    this.resolverSession = resolverSession;

    // Fix the magical column names for RDF statements
    setVariables(new Variable[] {new Variable("subject"),
                 new Variable("predicate"),
                 new Variable("object")});

    try {

      // Initialise the parser factory
      ParserFactory.getInstance().initialiseFactory();
    } catch (FactoryException factoryException) {

      throw new TuplesException("Unable to initialise factory for parsers.",
                                factoryException);
    }

    // Find the blank node map of our content object
    blankNodeMap = null;

    // Load in the RDF conversion of the given mp3 content
    loadURL();
  }

  /**
   * Load in the RDF conversion from the content object.
   *
   * @throws NotModifiedException if a valid cached version of the URL exists
   * @throws TuplesException
   */
  private void loadURL() throws NotModifiedException, TuplesException {
    if (content.getURI() == null) throw new TuplesException("Unable to load MP3 from a stream. Use a file.");

    try {
      // Initialise the model to be a memory based graph
      model = new GraphImpl();
    } catch (GraphException graphException) {
      throw new TuplesException("Unable to create a new graph object.", graphException);
    }

    // Create a container for our file
    File contentFile = null;

    if (!content.getURI().getScheme().equals("file")) {
      // If we are dealing with anything other than a file then use the caching process
      try {
        // Convert the URI into a file
        contentFile = getCachedFile(content.newInputStream(), content.getURI());
      } catch (IOException ioException) {
        throw new TuplesException(
            "Unable to open a stream to the content file [" +
            content.getURI().toString() + "]", ioException);
      }
    } else {
      // Files are local and do not need caching
      contentFile = new File(content.getURI());
    }

    // Parse the content of the file/directory to the model
    parseFile(contentFile);

    // Parse the stream into RDF statements
    blankNodeMap = new HashMap<Node,Long>();

    try {
      // Initialize the metadata now that we know the statements
      rowCount = model.getNumberOfTriples();
    } catch (GraphException graphException) {
      throw new TuplesException(
          "Unable to retrieve number of triples in graph.",
          graphException);
    }

    if (logger.isDebugEnabled()) logger.debug("Parsed MP3: Found " + rowCount + " triples");
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
      // Get the iterator for statements in the model
      nextTriple = model.find(null, null, null);
    } catch (GraphException graphException) {
      throw new TuplesException("Unable to retrieve triple iterator for graph.", graphException);
    }

    if (logger.isDebugEnabled()) {
      try {
        logger.debug("-- Getting the before first value from model " + model +
                     " which has statements " + nextTriple.hasNext() + " from " +
                     model.getNumberOfTriples() + " triples");
      } catch (GraphException graphException) {

        // Since we are debugging, it is not important if this exception is
        // ignored
      }
    }
  }

  public Object clone() {

    MP3Statements cloned = (MP3Statements)super.clone();

    // Copy immutable fields by reference
    cloned.resolverSession = resolverSession;
    cloned.rowCount = rowCount;
    cloned.tripleStatement = tripleStatement;
    cloned.content = content;

    return cloned;
  }

  /**
   * Close the RDF/XML formatted input stream.
   */
  public void close() throws TuplesException {

    resolverSession = null;
    tripleStatement = null;
    content = null;
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

    if (column == SUBJECT && logger.isInfoEnabled()) {
      logger.info("!! Using node value of: " + result.longValue());
    }

    return result.longValue();
  }

  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  public long getRowCount() throws TuplesException {
    return rowCount;
  }

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
  }

  public boolean isEmpty() throws TuplesException {
    return rowCount == 0;
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
    if (nextTriple.hasNext()) {
      // Get the next statement in the iterator
      tripleStatement = nextTriple.next();
      if (logger.isDebugEnabled()) logger.debug("-- Getting next statement: " + tripleStatement.toString());
      return true;
    } else {
      tripleStatement = null;
      return false;
    }
  }

  /**
   * Checks whether the given file is a file or directory and then acts
   * accordingly.  It should not be confused with the parseFile method which
   * does the actual conversion from an ID3 tag to RDF.  This method is
   * recursive so subdirectories will be navigated.
   *
   * @param file The file or directory we are checking the content of
   *
   * @throws TuplesException
   */
  private void parseFile(File file) throws TuplesException {

    if (file.getName().endsWith(".mp3")) {
      // If the file is a valid mp3 file then parse the content into the model

      // Container for our mp3 file
      MP3File mp3File = null;

      // Create a new MP3 file to represent our content
      mp3File = new MP3File(file);

      // container for our extended URI
      URI escapedURI = null;

      try {
        // Create an extended version of the URI for the file
        escapedURI = new URI(file.toURI().getScheme() + "://" + file.toURI().getRawPath());
      } catch (URISyntaxException uriSyntaxException) {
        throw new TuplesException("Failed to create a valid extended uri from" + file.toURI(), uriSyntaxException);
      }

      // Create a new conversion object
      MP3Conversion conversion = new MP3Conversion(mp3File, model, escapedURI);

      // Container for our parser object
      ID3Parser parser = null;

      try {
        // Get a parser instance
        parser = ParserFactory.getInstance().createID3Parser();
      } catch (FactoryException factoryException) {
        throw new TuplesException("Unable to create a new ID3Parser due to a factory error.", factoryException);
      }

      try {
        // Parse the mp3 into the model
        parser.parseTags(conversion);
      } catch (ParserException parserException) {
        throw new TuplesException("Unable to parse tags for file: " + file, parserException);
      }
    } else {
      throw new TuplesException("Content object did not contain a valid mime  type for parsing.");
    }
  }

  /**
   * Creates a locally cached version of a file from an input stream.  If the
   * file already exists then it will not download the file but instead use the
   * cached version.
   *
   * @param inputStream The stream of data we are caching
   * @param uri The uri of the data we are caching
   *
   * @return The file handle to the cached file
   *
   * @throws TuplesException
   */
  private File getCachedFile(InputStream inputStream, URI uri) throws
      TuplesException {

    // Retrieve the path to the file on the remote host
    String remotePath = uri.getPath();

    // Retrieve the actual name of the file
    String fileName = remotePath.substring(remotePath.lastIndexOf("/") + 1,
                                           remotePath.length());

    if (logger.isDebugEnabled()) {
      logger.debug("Transferring [" + uri + "] to cached file [" + fileName + "]");
    }

    // Create a temporary cache directory handle
    File cache = new File(TempDir.getTempDir(), "resolvercache");

    if (!cache.exists()) {

      // Check that the directory exists and if not then create it
      cache.mkdirs();
    }

    if (!cache.isDirectory()) {

      // If we can't use the directory name because a file has it, then just use
      // the temporary directory
      cache = TempDir.getTempDir();
    }

    // Create a new file representing the cached file
    File file = new File(cache, fileName);

    if (!file.exists()) {

      // If the file does not exists in the cache already then download the file

      // Container for our OutputStream to the file
      OutputStream outStream = null;

      try {

        // Attempt to create an output stream to the output file
        outStream = new FileOutputStream(file);
      } catch (FileNotFoundException fileNotFoundException) {

        throw new TuplesException("Unable to locate output file for caching " +
                                  "of local version of: " + uri.toString(),
                                  fileNotFoundException);
      }

      // Create an inputStream to read from
      InputStreamReader reader = new InputStreamReader(inputStream);

      // Container for the bytes in our stream
      int nextByte = 0;

      try {

        // Get the first byte of the stream
        nextByte = reader.read();

        while (nextByte != -1) {

          // Write out the current byte
          outStream.write(nextByte);

          // Read the next byte of the file
          nextByte = reader.read();
        }
      } catch (IOException ioException) {

        throw new TuplesException("Failed to transfer bytes from source to " +
                                  "cache due to an I/O error.", ioException);
      } finally {

        try {

          // Attempt to shutdown the output stream
          outStream.flush();
          outStream.close();
        } catch (IOException ioException) {

          throw new TuplesException("Failed to close output stream to cache",
                                    ioException);
        }

        try {

          // Attempt to close down the input stream reader
          reader.close();
        } catch (IOException ioException) {

          throw new TuplesException("Failed to close input stream from " +
                                    uri.toString(), ioException);
        }
      }
    }

    return file;
  }
  
}
