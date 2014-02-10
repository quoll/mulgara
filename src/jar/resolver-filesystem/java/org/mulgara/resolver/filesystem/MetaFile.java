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
import java.util.*;
import java.text.SimpleDateFormat;

// Third party packages
import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;
import org.jrdf.vocabulary.RDF;
import org.apache.log4j.Logger; // Apache Log4J

import org.mulgara.resolver.filesystem.exception.MetaDataException;
import org.mulgara.resolver.filesystem.exception.MetaIOException;
import org.mulgara.resolver.filesystem.exception.VocabularyException;
import org.mulgara.resolver.filesystem.util.GraphUtil;

/**
 * Extends the standard java File class to allow for a file that can retrieve
 * its metadata as well as link to the siblings files in its directory.
 *
 * @created 2004-11-25
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
public class MetaFile extends File {

  private static final long serialVersionUID = -6912916007828365901L;

  /** Logger. */
  private static final Logger log = Logger.getLogger(MetaFile.class);

  /** A graph containing the metadata for the file data */
  private Graph graph;

  /** The previous sibling */
  private MetaFile previousSibling;

  /** The next sibling */
  private MetaFile nextSibling;

  /**
   * Constructor.
   *
   * @param uri The uri of the file this object represents
   *
   * @throws MetaIOException
   */
  MetaFile(URI uri) throws MetaIOException {

    // Create a file with the given uri
    super(uri);

    try {

      // Initialise the graph to be a memory based graph
      graph = new GraphImpl();
    } catch (GraphException graphException) {

      throw new MetaIOException("Unable to create a new graph object.",
                                graphException);
    }
  }

  /**
   * Retrieves the next sibling of this metafile.
   *
   * @return The next sibling file to this one
   */
  public MetaFile nextSibling() {

    return nextSibling;
  }

  /**
   * Retrieves the previous sibling of this metafile.
   *
   * @return The previous sibling file to this one
   */
  public MetaFile previousSibling() {

    return previousSibling;
  }

  /**
   * Sets the metafile's next sibling.
   *
   * @param sibling The next sibling along from this metafile
   */
  public void setNextSibling(MetaFile sibling) {

    // Store the new next sibling
    nextSibling = sibling;
  }

  /**
   * Sets the metafile's previous sibling.
   *
   * @param sibling The previous sibling from this metafile
   */
  public void setPreviousSibling(MetaFile sibling) {

    // Store the new previous sibling
    previousSibling = sibling;
  }

  /**
   * Retrieves the children of this metafile if it is a directory.  This will
   * also set up the sibling relationships of the descendants of this file.
   *
   * @return The list of children files of this directory
   *
   * @throws MetaIOException
   */
  public MetaFile[] listChildren() throws MetaIOException {

    // Obtain the list of children files
    File[] children = this.listFiles();

    // Sort the array of child files by name
    Arrays.sort(children, new Comparator<File>() {
      public int compare(File file1, File file2) {
        // Return the natural comparison of the file names
        return file1.getName().compareTo(file2.getName());
      }
    });

    // Create a mirror array of children metafiles
    MetaFile[] metaChildren = new MetaFile[children.length];

    for (int i = 0; i < metaChildren.length; i++) {

      // Create a new metafile from the matching children array entry
      metaChildren[i] = new MetaFile(children[i].toURI());

      if (i > 0) {

        // If we are past the first entry then link the file to the previous
        // entry
        metaChildren[i].setPreviousSibling(metaChildren[i - 1]);

        // Link the previous entry to this sibling
        metaChildren[i - 1].setNextSibling(metaChildren[i]);
      }
    }

    return metaChildren;
  }

  /**
   * Retrieves the children of this metafile if it is a directory.  It uses a
   * filefilter to constrain the children that are contained in the list.
   * This will also set up the sibling relationships of the filtered descendants
   * of this file.
   *
   * @return The list of children files of this directory
   *
   * @throws MetaIOException
   */
  public MetaFile[] listChildrenByFilter(FileFilter filter) throws
      MetaIOException {

    if (log.isDebugEnabled()) {

      log.debug("Filtering children of " + this.toURI());
    }

    // Obtain the list of children files using the filter
    File[] children = this.listFiles(filter);

    if (children.length > 0) {

      // We can only produce a list of children if we found any fitting the
      // filter

      // Sort the array of filtered child files by name
      Arrays.sort(children, new Comparator<File>() {
        public int compare(File file1, File file2) {
          // Return the natural comparison of the file names
          return file1.getName().compareTo(file2.getName());
        }
      });

      // Create a mirror array of children metafiles
      MetaFile[] metaChildren = new MetaFile[children.length];

      for (int i = 0; i < metaChildren.length; i++) {

        // Create a new metafile from the matching children array entry
        metaChildren[i] = new MetaFile(children[i].toURI());

        if (i > 0) {

          // If we are past the first entry then link the file to the previous
          // entry
          metaChildren[i].setPreviousSibling(metaChildren[i - 1]);

          // Link the previous entry to this sibling
          metaChildren[i - 1].setNextSibling(metaChildren[i]);
        }
      }

      return metaChildren;
    }

    return new MetaFile[0];
  }

  /**
   * Store the metadata for the given file in the graph.
   *
   * @return The graph containing the metadata for this file
   *
   * @throws MetaDataException
   * @throws MetaIOException
   */
  public Graph getMetadata() throws MetaDataException, MetaIOException {

    // Flag to indicate if the graph is empty (Assumed false)
    boolean empty = false;

    try {

      // Check if the graph is empty
      empty = graph.isEmpty();
    } catch (GraphException graphException) {

      throw new MetaIOException("Unable to check if graph was empty.",
                                graphException);
    }

    if (graph != null && empty) {

      // The guard prevents unnecessary repetition of the metadata extraction
      // by checking if the graph is already populated.

      if (log.isDebugEnabled()) {

        log.debug("Obtaining metadata for file: " + getAbsolutePath());
      }

      // Obtain the element factory
      GraphElementFactory elementFactory = graph.getElementFactory();

      // Containers for our subject, predicate, and object nodes
      SubjectNode subject = null;
      PredicateNode predicate = null;
      ObjectNode object = null;

      try {

        // Create the subject using the file URI
        subject = (SubjectNode) elementFactory.createResource(toURI());
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new MetaDataException("Failed to create subject node for " +
                                    getAbsolutePath(),
                                    graphElementFactoryException);
      }

      // Create the RDF type predicate
      predicate = GraphUtil.createPredicateFromURI(RDF.TYPE, elementFactory);

      if (isDirectory()) {

        try {

          // If the file is a directory then set its type to a directory
          object = GraphUtil.createObjectResource(
              FileSystemVocab.getPredicate(FileSystemVocab.DIRECTORY_TYPE),
              elementFactory);
        } catch (VocabularyException vocabularyException) {

          throw new MetaDataException("Failed to find predicate for " +
                                      FileSystemVocab.DIRECTORY_TYPE,
                                      vocabularyException);
        }

      } else if (isHidden()) {

        try {

          // If the file is a hidden file then set its type to a hidden file
          object = GraphUtil.createObjectResource(
              FileSystemVocab.getPredicate(FileSystemVocab.HIDDEN_TYPE),
              elementFactory);
        } catch (VocabularyException vocabularyException) {

          throw new MetaDataException("Failed to find predicate for " +
                                      FileSystemVocab.HIDDEN_TYPE,
                                      vocabularyException);
        }
      } else if (!canWrite() && canRead()) {

        try {

          // If the file is read only then set its type to read only
          object = GraphUtil.createObjectResource(
              FileSystemVocab.getPredicate(FileSystemVocab.READ_ONLY_TYPE),
              elementFactory);
        } catch (VocabularyException vocabularyException) {

          throw new MetaDataException("Failed to find predicate for " +
                                      FileSystemVocab.READ_ONLY_TYPE,
                                      vocabularyException);
        }
      } else {

        try {

          // The default type is file
          object = GraphUtil.createObjectResource(
              FileSystemVocab.getPredicate(FileSystemVocab.FILE_TYPE),
              elementFactory);
        } catch (VocabularyException vocabularyException) {

          throw new MetaDataException("Failed to find predicate for " +
                                      FileSystemVocab.FILE_TYPE,
                                      vocabularyException);
        }
      }

      // Add the type triple
      GraphUtil.addTriple(subject, predicate, object, graph);

      // Container for predicate URIs
      URI predicateURI = null;

      try {

        // Obtain the filename predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.FILENAME);
      } catch (VocabularyException vocabularyException) {

        throw new MetaDataException("Failed to find predicate for " +
                                    FileSystemVocab.FILENAME,
                                    vocabularyException);
      }

      // Create the filename predicate
      predicate = GraphUtil.createPredicateFromURI(predicateURI, elementFactory);

      // Create the filename object
      object = GraphUtil.createObjectLiteral(getName(), elementFactory);

      // Add the filename triple
      GraphUtil.addTriple(subject, predicate, object, graph);

      if (isFile()) {

        // Only files have extensions

        try {

          // Obtain the extension predicate URI
          predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.EXTENSION);
        } catch (VocabularyException vocabularyException) {

          throw new MetaDataException("Failed to find predicate for " +
                                      FileSystemVocab.EXTENSION,
                                      vocabularyException);
        }

        // Create the extension predicate
        predicate = GraphUtil.createPredicateFromURI(predicateURI,
            elementFactory);

        // Get the file name
        String extension = getName();

        if (extension.indexOf(".") >= 0) {

          // Obtain the extension (if there is one)
          extension = extension.substring(extension.lastIndexOf(".") + 1,
                                          extension.length());
        } else {

          // If there is no extension then use the empty string
          extension = "";
        }

        // Create the extension object
        object = GraphUtil.createObjectLiteral(extension, elementFactory);

        // Add the extension triple
        GraphUtil.addTriple(subject, predicate, object, graph);
      }

      try {

        // Obtain the path predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.PATH);
      } catch (VocabularyException vocabularyException) {

        throw new MetaDataException("Failed to find predicate for " +
                                    FileSystemVocab.PATH, vocabularyException);
      }

      // Create the path predicate
      predicate = GraphUtil.createPredicateFromURI(predicateURI, elementFactory);

      // Create the path object
      object = GraphUtil.createObjectResource(toURI(), elementFactory);

      // Add the path triple
      GraphUtil.addTriple(subject, predicate, object, graph);

      try {

        // Obtain the modified predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.MODIFIED);
      } catch (VocabularyException vocabularyException) {

        throw new MetaDataException("Failed to find predicate for " +
                                    FileSystemVocab.MODIFIED,
                                    vocabularyException);
      }

      // Create the modified predicate
      predicate = GraphUtil.createPredicateFromURI(predicateURI, elementFactory);

      // Create the modified object
      object = GraphUtil.createObjectLiteral(asDate(lastModified()),
                                             elementFactory);

      // Add the modified triple
      GraphUtil.addTriple(subject, predicate, object, graph);

      try {

        // Obtain the parent predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.PARENT);
      } catch (VocabularyException vocabularyException) {

        throw new MetaDataException("Failed to find predicate for " +
                                    FileSystemVocab.PARENT, vocabularyException);
      }

      // Create the parent predicate
      predicate = GraphUtil.createPredicateFromURI(predicateURI, elementFactory);

      // Create the parent object
      object = GraphUtil.createObjectResource(getParentFile().toURI(),
                                              elementFactory);

      // Add the parent triple
      GraphUtil.addTriple(subject, predicate, object, graph);

      if (isFile()) {

        // Obtain the size of files

        try {

          // Obtain the modified predicate URI
          predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.SIZE);
        } catch (VocabularyException vocabularyException) {

          throw new MetaDataException("Failed to find predicate for " +
                                      FileSystemVocab.SIZE, vocabularyException);
        }

        // Create the size predicate
        predicate = GraphUtil.createPredicateFromURI(predicateURI,
            elementFactory);

        // Create the size object
        object = GraphUtil.createObjectLiteral("" + length(), elementFactory);

        // Add the size triple
        GraphUtil.addTriple(subject, predicate, object, graph);
      }

      try {

        // Obtain the canRead predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.CAN_READ);
      } catch (VocabularyException vocabularyException) {

        throw new MetaDataException("Failed to find predicate for " +
                                    FileSystemVocab.CAN_READ,
                                    vocabularyException);
      }

      // Create the canRead predicate
      predicate = GraphUtil.createPredicateFromURI(predicateURI, elementFactory);

      // Create the canRead object
      object = GraphUtil.createObjectLiteral("" + canRead(), elementFactory);

      // Add the canRead triple
      GraphUtil.addTriple(subject, predicate, object, graph);

      try {

        // Obtain the canWrite predicate URI
        predicateURI = FileSystemVocab.getPredicate(FileSystemVocab.CAN_WRITE);
      } catch (VocabularyException vocabularyException) {

        throw new MetaDataException("Failed to find predicate for " +
                                    FileSystemVocab.CAN_WRITE,
                                    vocabularyException);
      }

      // Create the canWrite predicate
      predicate = GraphUtil.createPredicateFromURI(predicateURI, elementFactory);

      // Create the canWrite object
      object = GraphUtil.createObjectLiteral("" + canWrite(), elementFactory);

      // Add the canWrite triple
      GraphUtil.addTriple(subject, predicate, object, graph);

      // JNI implementations are required for the creation date, last accessed,
      // owner and canExecute fields
    }

    if (log.isDebugEnabled()) {

      log.debug("Completed obtaining metadata for file: " + getAbsolutePath());
    }

    return graph;
  }

  /**
   * Converts the given date (as a long) to a string representation in the
   * dateTime XSD datatype format.
   *
   * @param dateTime The date and time as a long
   *
   * @return The dateTime string in XSD schema format
   */
  private String asDate(long dateTime) {

    // Obtain a calendar object
    Calendar calendar = Calendar.getInstance();

    // Set the time of the calendar to the date time passed in
    calendar.setTimeInMillis(dateTime);

    // create a formatter to process the date/time representation
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    return formatter.format(calendar.getTime());
  }

  /**
   * Closes a file that has been opened for metadata purposes.  Effectively
   * clears the graph and other resources that consume memory unnecessarily.
   *
   * @throws IOException
   */
  public void close() {

    // Close the graph
    ((GraphImpl) graph).close();
  }
}
