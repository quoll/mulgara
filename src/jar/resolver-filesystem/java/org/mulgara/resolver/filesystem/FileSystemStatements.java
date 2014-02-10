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

package org.mulgara.resolver.filesystem;

// Java 2 standard packages
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.text.SimpleDateFormat;

// Third party packages
import org.jrdf.graph.*;
import org.jrdf.vocabulary.RDF;
import org.apache.log4j.Logger; // Apache Log4J

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.filesystem.exception.*;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Tuples;

/**
 * Obtains the collective file system data from a set of tuples defining which
 * directories (file systems) to include or exclude.
 *
 * @created 2004-11-18
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:27 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileSystemStatements extends AbstractTuples implements Statements {

  /** Logger. */
  private static final Logger log = Logger.getLogger(FileSystemStatements.class);

  /** Column index for subjects */
  public static final int SUBJECT = 0;

  /** Column index for predicates */
  public static final int PREDICATE = 1;

  /** Column index for predicates */
  public static final int OBJECT = 2;

  /** The session used to globalize the RDF nodes from the stream */
  private ResolverSession resolverSession;

  /** The number of statements in the results */
  private long rowCount;

  /** The current row. If the cursor is not on a row, this will be null */
  private Triple tripleStatement;

  /** The model which will store the content of the resulting file system data */
  private Graph model;

  /** The list of excluded filesystems */
  private ArrayList<File> exclusionList;

  /** The meta data file manager we will be using for our conversions */
  private MetaFileManager manager;

  /**
   * Constructor.
   *
   * @param includeTuples The tuples detailing the directories to include
   * @param excludeTuples The tuples detailing the directories to exclude
   * @param resolverSession The resolver session we should globalise and
   *                        localise against
   *
   * @throws TuplesException
   */
  FileSystemStatements(Tuples includeTuples, Tuples excludeTuples,
                       ResolverSession resolverSession) throws TuplesException {

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }

    // Initialize fields
    this.resolverSession = resolverSession;

    // Fix the magical column names for RDF statements
    setVariables(new Variable[] {new Variable("subject"),
                 new Variable("predicate"),
                 new Variable("object")});

    // Create the list of excluded URIs
    createExclusionList(excludeTuples);

    // Generate the graph
    createInclusionList(includeTuples);
  }

  /**
   * Construct a list of URIs to omit from the inclusion file systems.
   *
   * @param excludeTuples The tuples detailing the file systems to exclude
   *
   * @throws TuplesException
   */
  private void createExclusionList(Tuples excludeTuples) throws TuplesException {

    // Initialise the exclusion list
    exclusionList = new ArrayList<File>();

    if (excludeTuples != null) {

      // Since it is entirely valid to have no exclusions, we allow there to be
      // null exclusion tuples which we have to check for before constructing
      // the list

      // Initiate iteration of the tuples
      excludeTuples.beforeFirst();

      // Iterate through the exclusion tuples and store the relevant file system
      // references
      while (excludeTuples.next()) {

        if (log.isDebugEnabled()) {

          // Obtain a list of variable headers
          Variable [] variables = excludeTuples.getVariables();

          // Container for our debug message
          String debugString = "-- Looking at Tuple: [";

          // Iterate through the variable list and print the value for each
          // entry
          for (int i = 0; i < variables.length; i++) {

            // Find the column for the current variable
            int column = excludeTuples.getColumnIndex(variables[i]);

            // Append the entry to the tuple list
            debugString += excludeTuples.getColumnValue(column) + ", ";
          }

          // Finish the debug message
          debugString += "]";

          log.debug(debugString);
        }

        // Obtain the object (file system URI)
        long fileSystemURI = excludeTuples.getColumnValue(
            excludeTuples.getColumnIndex(new Variable("fileSystemRef")));

        // Container for the globalised uri node
        Node fileSystemURINode = null;

        if (log.isDebugEnabled()) {

          log.debug("-- Looking at excluded filesystem URI: " + fileSystemURI);
        }

        try {

          // Globalise the file system URI node id
          fileSystemURINode = resolverSession.globalize(fileSystemURI);
        } catch (GlobalizeException globaliseException) {

          throw new TuplesException("Unable to globalise file system URI in " +
                                    "exclusion list", globaliseException);
        }

        if (log.isDebugEnabled()) {

          log.debug("-- Found excluded filesystem URI: " + fileSystemURINode);
        }

        if (fileSystemURINode instanceof URIReference) {

          // If the node is a uri reference then we extract the uri and
          // store it
          URIReference fileSystemURIRef = (URIReference) fileSystemURINode;

          // Add the file to the exclusion list
          exclusionList.add(new File(fileSystemURIRef.getURI()));

          if (log.isDebugEnabled()) {

            log.debug("-- Added excluded filesystem: " +
                      fileSystemURIRef.getURI());
          }
        } else {

          // We simply ignore excluded file systems with a warning
          log.warn("Found an exclusion triple that did not reference the file " +
                   "system correctly.");
        }
      }
    }

    // Create the file manager
    manager = new MetaFileManager(exclusionList);
  }

  /**
   * Construct a graph containging triples detailing the data about files and
   * directories in the list of included file systems.  If any of the
   * directories are contained within the exclusion list they will be ignored.
   *
   * @param includeTuples The tuples list of included file systems
   *
   * @throws TuplesException
   */
  private void createInclusionList(Tuples includeTuples) throws TuplesException {

    if (includeTuples != null) {

      // It is entirely possible that there are no inclusions so we need to
      // check before processing

      // Initiate iteration of the tuples
      includeTuples.beforeFirst();

      // Iterate through the inclusion tuples and generate the file system
      // information based on the files and directories within
      while (includeTuples.next()) {

        // Obtain the object (file system URI)
        long fileSystemURI = includeTuples.getColumnValue(
            includeTuples.getColumnIndex(new Variable("fileSystemRef")));

        // Container for the globalised uri node
        Node fileSystemURINode = null;

        try {

          // Globalise the file system URI node id
          fileSystemURINode = resolverSession.globalize(fileSystemURI);
        } catch (GlobalizeException globaliseException) {

          throw new TuplesException("Unable to globalise file system URI in " +
                                    "exclusion list", globaliseException);
        }

        if (fileSystemURINode instanceof URIReference) {

          // If the node is a uri reference then we extract the uri and
          // store it
          URIReference fileSystemURIRef = (URIReference) fileSystemURINode;

          // Generate the file system information using recursion
          manager.addFileSystem(fileSystemURIRef.getURI());
        } else {

          // We cannot work with non-URI nodes so warn and ignore
          log.warn("Found an inclusion triple that did not reference the file " +
                   "system correctly.  Triples must be of the format: " +
                   "[$subject <http://mulgara.org/mulgara/filesystem#Include> " +
                   "<file://path/myDirectory>] or [$subject " +
                   "<http://mulgara.org/mulgara/filesystem#Exclude> " +
                   "<file://path/myDirectory>]");
        }
      }
    }
  }

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

  /**
   * Resets the counter for triples to be the first.
   *
   * @param prefix The prefix to use
   * @param suffixTruncation The truncation of suffixes to use
   *
   * @throws TuplesException
   */
  public void beforeFirst(long[] prefix, int suffixTruncation) throws
      TuplesException {

    // Since we do not require anything from the parameters, we simply default
    // to the empty parameter method signature.
    beforeFirst();
  }

  /**
   * Resets the counter for triples to be the first.
   *
   * @throws TuplesException
   */
  public void beforeFirst() throws TuplesException {

    /*try {

      // Get the iterator for statements in the model
      nextTriple = model.find(null, null, null);
    } catch (GraphException graphException) {

      throw new TuplesException("Unable to retrieve triple iterator for graph.",
                                graphException);
    }

    if (log.isDebugEnabled()) {

      try {

        log.debug("-- Getting the before first value from model " + model +
                  " which has statements " + nextTriple.hasNext() + " from " +
                  model.getNumberOfTriples() + " triples");
      } catch (GraphException graphException) {

        // Since we are debugging, it is not important if this exception is
        // ignored
      }
    }*/

    try {

      // Prepare the manager for iteration
      manager.prepare();
    } catch (FileManagerException fileManagerException) {

      throw new TuplesException("Failed to prepare file manager for iteration.",
                                fileManagerException);
    }
  }

  public Object clone() {

    FileSystemStatements cloned = (FileSystemStatements)super.clone();

    // Copy immutable fields by reference
    cloned.resolverSession = resolverSession;
    cloned.rowCount = rowCount;
    cloned.tripleStatement = tripleStatement;

    return cloned;
  }

  /**
   * Close the RDF/XML formatted input stream.
   */
  public void close() throws TuplesException {

    resolverSession = null;
    tripleStatement = null;
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

    // Localize the node
    try {

      return resolverSession.localize(node);
    } catch (LocalizeException e) {

      throw new TuplesException("Couldn't get column " + column + " value", e);
    }
  }

  public List<Tuples> getOperands() {
    return Collections.emptyList();
  }

  public long getRowCount() throws TuplesException {
    // We have no way of telling how many files there will be due to the 'pull
    // parser' structure we have set up so we just give a large estimate to
    // account for large file systems
    return Long.MAX_VALUE;
  }

  public long getRowUpperBound() throws TuplesException {
    return getRowCount();
  }

  public long getRowExpectedCount() throws TuplesException {
    return getRowCount();
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

    /*if (nextTriple.hasNext()) {

      // Get the next statement in the iterator
      tripleStatement = (Triple) nextTriple.next();

      if (log.isDebugEnabled()) {

        log.debug("-- Getting next statement: " + tripleStatement.toString());
      }

      return true;
    } else {

      tripleStatement = null;

      return false;
    }*/

    try {

      // Obtain the next triple
      tripleStatement = manager.nextTriple();
    } catch (FileManagerException fileManagerException) {

      throw new TuplesException("Failed to retrieve next triple from file " +
                                "manager", fileManagerException);
    }

    return tripleStatement != null;
  }

  /**
   * Recurse the given URI and construct triples about the files and directories
   * contained within.
   *
   * @param fileURI The URI to recurse
   *
   * @throws TuplesException
   */
  @SuppressWarnings("unused")
  private void getFileSystemInformation(URI fileURI) throws TuplesException {

    // Container for the actual file on the filesystem
    File file = null;

    try {

      // Construct a file from the URI
      file = new File(fileURI);
    } catch (IllegalArgumentException illegalArgumentException) {

      // Obtain the element factory
      GraphElementFactory elementFactory = model.getElementFactory();

      // Containers for our subject, predicate, and object nodes
      SubjectNode subject = null;
      PredicateNode predicate = null;
      ObjectNode object = null;

      try {

        // Create the subject using the file URI
        subject = (SubjectNode) elementFactory.createResource(fileURI);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new TuplesException("Failed to create subject node for " +
                                  fileURI.toString(),
                                  graphElementFactoryException);
      }

      // Create the RDF type predicate
      predicate = createPredicateFromURI(RDF.TYPE, elementFactory);

      try {

        // The default type is file
        object = (ObjectNode) elementFactory.createResource(
            new URI("http://mulgara.org/mulgara#InvalidFileSystem"));
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new TuplesException(
            "Failed to create object node for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#InvalidFileSystem>",
            graphElementFactoryException);
      } catch (URISyntaxException uriSyntaxException) {

        throw new TuplesException(
            "Failed to create object uri for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#InvalidFileSystem>",
            uriSyntaxException);
      }

      // If the file uri was bad then add a triple to indicate this before
      // going on to the next entry
      addTriple(subject, predicate, object);

      // If the file cannot be created because the URI is invalid then skip
      // processing it
      return;
    }

    if (!file.exists()) {

      // Obtain the element factory
      GraphElementFactory elementFactory = model.getElementFactory();

      // Containers for our subject, predicate, and object nodes
      SubjectNode subject = null;
      PredicateNode predicate = null;
      ObjectNode object = null;

      try {

        // Create the subject using the file URI
        subject = (SubjectNode) elementFactory.createResource(fileURI);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new TuplesException("Failed to create subject node for " +
                                  fileURI.toString(),
                                  graphElementFactoryException);
      }

      // Create the RDF type predicate
      predicate = createPredicateFromURI(RDF.TYPE, elementFactory);

      try {

        // The default type is file
        object = (ObjectNode) elementFactory.createResource(
            new URI("http://mulgara.org/mulgara#NonExistantFileSystem"));
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new TuplesException(
            "Failed to create object node for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#NonExistantFileSystem>",
            graphElementFactoryException);
      } catch (URISyntaxException uriSyntaxException) {

        throw new TuplesException(
            "Failed to create object uri for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#NonExistantFileSystem>",
            uriSyntaxException);
      }

      // If the file uri was bad then add a triple to indicate this before
      // going on to the next entry
      addTriple(subject, predicate, object);

      // If the file cannot be created because the URI is invalid then skip
      // processing it
      return;

    }

    if (!exclusionList.contains(file.getParentFile()) &&
        !exclusionList.contains(file)) {

      // If the file is not part of the exclusion list then process it

      // Store the metadata for the file
      storeMetadata(file);

      if (file.isDirectory()) {

        // Retrieve the children of the file if it is a directory
        File[] children = file.listFiles();

        // Iterate through the list of children and process each one's metadata
        for (int i = 0; i < children.length; i++) {

          // Retrieve the file metadata for the next child
          getFileSystemInformation(children[i].toURI());
        }
      }
    }
  }

  /**
   * Store the metadata for the given file in the graph.
   *
   * @param file The file to generate metadata for
   *
   * @throws TuplesException
   */
  public void storeMetadata(File file) throws TuplesException {

    if (log.isDebugEnabled()) {

      log.debug("Obtaining metadata for file: " + file.getAbsolutePath());
    }

    // Obtain the element factory
    GraphElementFactory elementFactory = model.getElementFactory();

    // Containers for our subject, predicate, and object nodes
    SubjectNode subject = null;
    PredicateNode predicate = null;
    ObjectNode object = null;

    try {

      // Create the subject using the file URI
      subject = (SubjectNode) elementFactory.createResource(file.
          toURI());
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new TuplesException("Failed to create subject node for " +
                                file.getAbsolutePath(),
                                graphElementFactoryException);
    }

    // Create the RDF type predicate
    predicate = createPredicateFromURI(RDF.TYPE, elementFactory);

    if (file.isDirectory()) {

      try {

        // If the file is a directory then set its type to a directory
        object = createObjectResource(
            FileSystemVocab.getPredicate(FileSystemVocab.DIRECTORY_TYPE),
            elementFactory);
      } catch (VocabularyException vocabularyException) {

        throw new TuplesException("Failed to find predicate for " +
                                  FileSystemVocab.DIRECTORY_TYPE,
                                  vocabularyException);
      }

    } else if (file.isHidden()) {

      try {

        // If the file is a hidden file then set its type to a hidden file
        object = createObjectResource(
            FileSystemVocab.getPredicate(FileSystemVocab.HIDDEN_TYPE),
            elementFactory);
      } catch (VocabularyException vocabularyException) {

        throw new TuplesException("Failed to find predicate for " +
                                  FileSystemVocab.HIDDEN_TYPE,
                                  vocabularyException);
      }
    } else if (!file.canWrite() && file.canRead()) {

      try {

        // If the file is read only then set its type to read only
        object = createObjectResource(
            FileSystemVocab.getPredicate(FileSystemVocab.READ_ONLY_TYPE),
            elementFactory);
      } catch (VocabularyException vocabularyException) {

        throw new TuplesException("Failed to find predicate for " +
                                  FileSystemVocab.READ_ONLY_TYPE,
                                  vocabularyException);
      }
    } else {

      try {

        // The default type is file
        object = createObjectResource(
            FileSystemVocab.getPredicate(FileSystemVocab.FILE_TYPE),
            elementFactory);
      } catch (VocabularyException vocabularyException) {

        throw new TuplesException("Failed to find predicate for " +
                                  FileSystemVocab.FILE_TYPE,
                                  vocabularyException);
      }
    }

    // Add the type triple
    addTriple(subject, predicate, object);

    // Container for predicate URIs
    URI predicateURI = null;

    try {

      // Obtain the filename predicate URI
      predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.FILENAME);
    } catch (VocabularyException vocabularyException) {

      throw new TuplesException("Failed to find predicate for " +
                                FileSystemVocab.FILENAME, vocabularyException);
    }

    // Create the filename predicate
    predicate = createPredicateFromURI(predicateURI, elementFactory);

    // Create the filename object
    object = createObjectLiteral(file.getName(), elementFactory);

    // Add the filename triple
    addTriple(subject, predicate, object);

    if (file.isFile()) {

      // Only files have extensions

      try {

        // Obtain the extension predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.EXTENSION);
      } catch (VocabularyException vocabularyException) {

        throw new TuplesException("Failed to find predicate for " +
                                  FileSystemVocab.EXTENSION,
                                  vocabularyException);
      }

      // Create the extension predicate
      predicate = createPredicateFromURI(predicateURI, elementFactory);

      // Get the file name
      String extension = file.getName();

      if (extension.indexOf(".") >= 0) {

        // Obtain the extension (if there is one)
        extension = extension.substring(extension.lastIndexOf(".") + 1,
                                        extension.length());
      } else {

        // If there is no extension then use the empty string
        extension = "";
      }

      // Create the extension object
      object = createObjectLiteral(extension, elementFactory);

      // Add the extension triple
      addTriple(subject, predicate, object);
    }

    try {

      // Obtain the path predicate URI
      predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.PATH);
    } catch (VocabularyException vocabularyException) {

      throw new TuplesException("Failed to find predicate for " +
                                FileSystemVocab.PATH, vocabularyException);
    }

    // Create the path predicate
    predicate = createPredicateFromURI(predicateURI, elementFactory);

    // Create the path object
    object = createObjectResource(file.toURI(), elementFactory);

    // Add the path triple
    addTriple(subject, predicate, object);

    try {

      // Obtain the modified predicate URI
      predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.MODIFIED);
    } catch (VocabularyException vocabularyException) {

      throw new TuplesException("Failed to find predicate for " +
                                FileSystemVocab.MODIFIED, vocabularyException);
    }

    // Create the modified predicate
    predicate = createPredicateFromURI(predicateURI, elementFactory);

    // Create the modified object
    object = createObjectLiteral(asDate(file.lastModified()), elementFactory);

    // Add the modified triple
    addTriple(subject, predicate, object);

    try {

      // Obtain the parent predicate URI
      predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.PARENT);
    } catch (VocabularyException vocabularyException) {

      throw new TuplesException("Failed to find predicate for " +
                                FileSystemVocab.PARENT, vocabularyException);
    }

    // Create the parent predicate
    predicate = createPredicateFromURI(predicateURI, elementFactory);

    // Create the parent object
    object = createObjectResource(file.getParentFile().toURI(), elementFactory);

    // Add the parent triple
    addTriple(subject, predicate, object);

    if (file.isFile()) {

      // Obtain the size of files

      try {

        // Obtain the modified predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.SIZE);
      } catch (VocabularyException vocabularyException) {

        throw new TuplesException("Failed to find predicate for " +
                                  FileSystemVocab.SIZE, vocabularyException);
      }

      // Create the size predicate
      predicate = createPredicateFromURI(predicateURI, elementFactory);

      // Create the size object
      object = createObjectLiteral("" + file.length(), elementFactory);

      // Add the size triple
      addTriple(subject, predicate, object);
    }

    try {

      // Obtain the canRead predicate URI
      predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.CAN_READ);
    } catch (VocabularyException vocabularyException) {

      throw new TuplesException("Failed to find predicate for " +
                                FileSystemVocab.CAN_READ, vocabularyException);
    }

    // Create the canRead predicate
    predicate = createPredicateFromURI(predicateURI, elementFactory);

    // Create the canRead object
    object = createObjectLiteral("" + file.canRead(), elementFactory);

    // Add the canRead triple
    addTriple(subject, predicate, object);

    try {

      // Obtain the canWrite predicate URI
      predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.CAN_WRITE);
    } catch (VocabularyException vocabularyException) {

      throw new TuplesException("Failed to find predicate for " +
                                FileSystemVocab.CAN_WRITE, vocabularyException);
    }

    // Create the canWrite predicate
    predicate = createPredicateFromURI(predicateURI, elementFactory);

    // Create the canWrite object
    object = createObjectLiteral("" + file.canWrite(), elementFactory);

    // Add the canWrite triple
    addTriple(subject, predicate, object);

    // JNI implementations are required for the creation date, last accessed,
    // owner and canExecute fields
  }

  /**
   * Creates a predicate node using the given string as a lookup in the
   * dictionary.
   *
   * @param key The key into the dictionary
   * @param elementFactory The factory for creating nodes
   *
   * @return The predicate as a node
   *
   * @throws TuplesException
   */
  @SuppressWarnings("unused")
  private PredicateNode createPredicate(String key, GraphElementFactory elementFactory) throws TuplesException {
    try {
      // Create the type node
      return (PredicateNode) elementFactory.createResource(RDF.TYPE);
    } catch (GraphElementFactoryException graphElementFactoryException) {
      throw new TuplesException("Failed to create predicate node for key: " + key,
                                graphElementFactoryException);
    }
  }

  /**
   * Creates a predicate node using the given given uri.
   *
   * @param reference The URI reference to use as the predicate
   * @param elementFactory The factory for creating nodes
   *
   * @return The predicate as a node
   *
   * @throws TuplesException
   */
  private PredicateNode createPredicateFromURI(URI reference,
                                               GraphElementFactory
                                               elementFactory) throws
      TuplesException {

    try {

      // Create the type node
      return (PredicateNode) elementFactory.createResource(reference);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new TuplesException("Failed to create predicate node for URI " +
                                reference.toString(),
                                graphElementFactoryException);
    }

  }

  /**
   * Creates an object node using a resource URI.
   *
   * @param value The resource URI
   * @param elementFactory The factory for creating nodes
   *
   * @return The object node
   *
   * @throws TuplesException
   */
  private ObjectNode createObjectResource(URI value,
                                          GraphElementFactory elementFactory) throws
      TuplesException {

    try {

      // The default type is file
      return (ObjectNode) elementFactory.createResource(value);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new TuplesException("Failed to create object node for " +
                                value.toString(),
                                graphElementFactoryException);
    }
  }

  /**
   * Creates an object node using a literal value.
   *
   * @param value The value of the literal
   * @param elementFactory The factory for creating nodes
   *
   * @return The object node representing the literal
   *
   * @throws TuplesException
   */
  private ObjectNode createObjectLiteral(String value,
                                         GraphElementFactory elementFactory) throws
      TuplesException {

    try {

      // The default type is file
      return (ObjectNode) elementFactory.createLiteral(value);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new TuplesException("Failed to create object node for " +
                                value, graphElementFactoryException);
    }
  }

  /**
   * Adds the given triple to the graph.
   *
   * @param subject The subject node
   * @param predicate The predicate node
   * @param object The object node
   *
   * @throws TuplesException
   */
  private void addTriple(Node subject, Node predicate, Node object) throws
      TuplesException {

    try {

      // Add the type triple
      model.add((SubjectNode) subject, (PredicateNode) predicate,
                (ObjectNode) object);
    } catch (GraphException graphException) {

      throw new TuplesException("Failed to add triple for predicate: " +
                                predicate, graphException);
    }
  }

  private String asDate(long dateTime) {

    // Obtain a calendar object
    Calendar calendar = Calendar.getInstance();

    // Set the time of the calendar to the date time passed in
    calendar.setTimeInMillis(dateTime);

    // create a formatter to process the date/time representation
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    return formatter.format(calendar.getTime());
  }
}
