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

// Third party packages
import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;
import org.jrdf.vocabulary.RDF;
import org.apache.log4j.Logger; // Apache Log4J
import org.jrdf.util.ClosableIterator; // JRDF

// Locally written packages
import org.mulgara.resolver.filesystem.exception.*;
import org.mulgara.resolver.filesystem.util.*;

/**
 * Manages the navigation, caching, retrieval and meta-data construction of file
 * system information.
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
public class MetaFileManager {

  /** Logger. */
  private static final Logger log = Logger.getLogger(MetaFileManager.class);

  /** An interator into triples positioned at the next triple. */
  private ClosableIterator<Triple> nextTriple;

  /** The model which will store the content of the current file metadata */
  private Graph currentEntry;

  /** Pointer to the currently looked at directory */
  private MetaFile currentDirectory;

  /** Pointer to the currently looked at child file */
  private MetaFile currentChild;

  /** Pointer to the current root metafile */
  private MetaFile root;

  /** The current included URI index we are looking at */
  private int currentFileSystem;

  /** The list of excluded filesystems */
  private ArrayList<File> exclusionList;

  /** The list of included filesystems */
  private ArrayList<URI> inclusionList;

  /**
   * Flag to state whether we have completed our run of the inclusion list
   * or not
   */
  private boolean complete;

  /**
   * Constructor.
   *
   * @param exclusions The list of excluded URIs from the included filesystems
   */
  MetaFileManager(ArrayList<File> exclusions) {

    if (exclusions == null) {

      // If the exclusions list was null then use an empty list
      exclusions = new ArrayList<File>();
    }

    // Store the excluded URIs
    exclusionList = exclusions;

    // Initialise the included file system list
    inclusionList = new ArrayList<URI>();
  }

  /**
   * Adds a filesystem to be included in the manager.
   *
   * @param filesystem The filesystem to be included
   */
  public void addFileSystem(URI filesystem) {

    // Add the filesystem to the manager
    inclusionList.add(filesystem);
  }

  /**
   * Readies the MetaFileManager for navigation of the filesystems.
   *
   * @throws FileManagerException
   */
  public void prepare() throws FileManagerException {

    // Reset all tracking variables
    currentEntry = null;
    currentDirectory = null;
    currentChild = null;
    complete = false;
    currentFileSystem = -1;

    if (inclusionList.size() > 0) {

      // We need to check if we have any included files otherwise we can ignore
      // all processing

      try {

        // Initialise the graph for this file
        currentEntry = new GraphImpl();
      } catch (GraphException graphException) {

        throw new FileManagerException("Failed to create graph during prepare " +
                                       "phase", graphException);
      }

      // Initialise the first file system
      complete = loadNextFileSystem();
    } else {

      // We have no list to process so mark the processing as complete
      complete = true;
    }
  }

  /**
   * Readies the next file system in the list to be read from and returns
   * whether there was another filesystem or the end of the list is reached.
   *
   * @return Whether there are more filesystems
   *
   * @throws FileManagerException
   */
  private boolean loadNextFileSystem() throws FileManagerException {

    // Increment the filesystem pointer
    currentFileSystem++;

    try {

      // Remove all statements in the graph (Normal methods of removal are
      // currently broken)
      currentEntry = new GraphImpl();
    } catch (GraphException graphException) {

      throw new FileManagerException("Failed to clear graph before loading " +
                                     "next file system");
    }

    // Container for our next filesystem's URI
    URI nextURI = null;

    try {

      // Retrieve the next filesystem URI from the inclusion list
      nextURI = inclusionList.get(currentFileSystem);
    } catch (IndexOutOfBoundsException outOfBoundsException) {

      // An invalid index means we have no more entries
      return true;
    }

    // Flag to indicate whether we should skip the exclusion test due to a bad
    // protocol which needs to be reported
    boolean badProtocol = !nextURI.getScheme().equals("file");

    // Cycle through the list of inclusions until we either reach the end or
    // find a non-excluded value (This may include bad protocol entries)
    while (!badProtocol && exclusionList.contains(new File(nextURI))) {

      // Increment our counter
      currentFileSystem++;

      try {

        // Retrieve the next filesystem URI from the inclusion list
        nextURI = inclusionList.get(currentFileSystem);
      } catch (IndexOutOfBoundsException outOfBoundsException) {

        // An invalid index means we have no more entries
        return true;
      }

      // Check the protocol
      badProtocol = !nextURI.getScheme().equals("file");
    }

    // Flag to indicate root creation success (Assumed to succeed)
    boolean couldCreate = true;

    try {

      // Create the root metafile
      root = new MetaFile(nextURI);
    } catch (IllegalArgumentException illegalArgumentException) {

      // Obtain the element factory
      GraphElementFactory elementFactory = currentEntry.getElementFactory();

      // Containers for our subject, predicate, and object nodes
      SubjectNode subject = null;
      PredicateNode predicate = null;
      ObjectNode object = null;

      try {

        // Create the subject using the file URI
        subject = (SubjectNode) elementFactory.createResource(nextURI);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new FileManagerException("Failed to create subject node for " +
                                       nextURI.toString(),
                                       graphElementFactoryException);
      }

      try {

        // Create the RDF type predicate
        predicate = GraphUtil.createPredicateFromURI(RDF.TYPE, elementFactory);
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Failed to create predicate node for " +
                                       RDF.TYPE, metaDataException);
      }

      try {

        // The default type is file
        object = (ObjectNode) elementFactory.createResource(
            new URI("http://mulgara.org/mulgara#InvalidFileSystem"));
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new FileManagerException(
            "Failed to create object node for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#InvalidFileSystem>",
            graphElementFactoryException);
      } catch (URISyntaxException uriSyntaxException) {

        throw new FileManagerException(
            "Failed to create object uri for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#InvalidFileSystem>",
            uriSyntaxException);
      }

      try {

        // If the file uri was bad then add a triple to indicate this
        GraphUtil.addTriple(subject, predicate, object, currentEntry);
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Failed to add triple [" +
                                       subject + ", " + predicate + ", " +
                                       object + "]", metaDataException);
      }

      // If the file cannot be created because the URI is invalid then fail
      // the creation
      couldCreate = false;
    } catch (MetaIOException metaIOException) {

      // Obtain the element factory
      GraphElementFactory elementFactory = currentEntry.getElementFactory();

      // Containers for our subject, predicate, and object nodes
      SubjectNode subject = null;
      PredicateNode predicate = null;
      ObjectNode object = null;

      try {

        // Create the subject using the file URI
        subject = (SubjectNode) elementFactory.createResource(nextURI);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new FileManagerException("Failed to create subject node for " +
                                       nextURI.toString(),
                                       graphElementFactoryException);
      }

      try {

        // Create the RDF type predicate
        predicate = GraphUtil.createPredicateFromURI(RDF.TYPE, elementFactory);
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Failed to create predicate node for " +
                                       RDF.TYPE, metaDataException);
      }

      try {

        // The default type is file
        object = (ObjectNode) elementFactory.createResource(
            new URI("http://mulgara.org/mulgara#InvalidFileSystem"));
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new FileManagerException(
            "Failed to create object node for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#FailedFileSystem>",
            graphElementFactoryException);
      } catch (URISyntaxException uriSyntaxException) {

        throw new FileManagerException(
            "Failed to create object uri for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#FailedFileSystem>",
            uriSyntaxException);
      }

      try {

        // If the graph was unable to be created for the metafile then state that we
        // have a failed filesystem
        GraphUtil.addTriple(subject, predicate, object, currentEntry);
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Failed to add triple [" +
                                       subject + ", " + predicate + ", " +
                                       object + "]", metaDataException);
      }

      // If the graph was unable to be created for the metafile then ignore it
      // as we cannot use the metafile object
      couldCreate = false;
    }

    if (couldCreate && !root.exists()) {

      // Obtain the element factory
      GraphElementFactory elementFactory = currentEntry.getElementFactory();

      // Containers for our subject, predicate, and object nodes
      SubjectNode subject = null;
      PredicateNode predicate = null;
      ObjectNode object = null;

      try {

        // Create the subject using the file URI
        subject = (SubjectNode) elementFactory.createResource(nextURI);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new FileManagerException("Failed to create subject node for " +
                                       nextURI.toString(),
                                       graphElementFactoryException);
      }

      try {

        // Create the RDF type predicate
        predicate = GraphUtil.createPredicateFromURI(RDF.TYPE, elementFactory);
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Failed to create predicate node for " +
                                       RDF.TYPE, metaDataException);
      }

      try {

        // The default type is file
        object = (ObjectNode) elementFactory.createResource(
            new URI("http://mulgara.org/mulgara#NonExistantFileSystem"));
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new FileManagerException(
            "Failed to create object node for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#NonExistantFileSystem>",
            graphElementFactoryException);
      } catch (URISyntaxException uriSyntaxException) {

        throw new FileManagerException(
            "Failed to create object uri for invalid file " +
            "system node " +
            "<http://mulgara.org/mulgara#NonExistantFileSystem>",
            uriSyntaxException);
      }

      try {

        // If the file uri was bad then add a triple to indicate this before
        // going on to the next entry
        GraphUtil.addTriple(subject, predicate, object, currentEntry);
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Failed to add triple [" +
                                       subject + ", " + predicate + ", " +
                                       object + "]", metaDataException);
      }

      // If the file cannot be created because the URI is invalid then skip
      // processing it
      couldCreate = false;
    }

    if (couldCreate) {

      // Find the starting point of the traversal from the first inclusion Uri
      findStartPoint(root);

      try {

        // Obtain the current child's metadata
        currentEntry = currentChild.getMetadata();
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Unable to retrieve metadata for " +
                                       currentChild.toURI(),
                                       metaDataException);
      } catch (MetaIOException metaDataException) {

        throw new FileManagerException("Unable to perform metadata IO for " +
                                       currentChild.toURI(),
                                       metaDataException);
      }
    } else {

      // Set the current child to be the root file so we will skip it because
      // something has gone wrong during the startpoint process
      currentChild = root;
    }

    try {

      // Obtain the iterator for the graph
      nextTriple = currentEntry.find(null, null, null);
    } catch (GraphException graphException) {

      throw new FileManagerException("Unable to obtain triple iterator for " +
                                     currentChild.toURI(), graphException);
    }

    return false;
  }

  /**
   * Obtains the next triple in the current filesystem.  This will come from the
   * graph of the current file's metadata, however, if the current file has been
   * completed then the next will be found and asked for its metadata.
   *
   * @return The next triple in the filesystem's metadata
   *
   * @throws FileManagerException
   */
  public Triple nextTriple() throws FileManagerException {

    if (!complete) {

      // Only obtain triples if we are not completed

      if (!nextTriple.hasNext()) {

        if (currentChild != null && currentChild.equals(root)) {

          if (log.isDebugEnabled()) {

            log.debug("Finished processing filesystem [" + root.toURI() + "]");
          }

          // Ready the system for the next entry if we have reached the root
          // entry for this inclusion
          complete = loadNextFileSystem();

          if (complete) {

            // If this was the last filesystem then stop processing
            return null;
          }
        }

        // Advance the current child by one
        currentChild = currentChild.nextSibling();

        if (!cycleToNextFile()) {

          // If there are no children left in the directory then obtain the next
          // directory's children
          getNextDirectory();
        } else {

          try {

            // Obtain the new child's metadata
            currentEntry = currentChild.getMetadata();
          } catch (MetaDataException metaDataException) {

            throw new FileManagerException("Unable to retrieve metadata for " +
                                           currentChild.toURI(),
                                           metaDataException);
          } catch (MetaIOException metaDataException) {

            throw new FileManagerException("Unable to perform metadata IO for " +
                                           currentChild.toURI(),
                                           metaDataException);
          }

          try {

            // Obtain the iterator for the graph
            nextTriple = currentEntry.find(null, null, null);
          } catch (GraphException graphException) {

            throw new FileManagerException(
                "Unable to obtain triple iterator for " +
                currentChild.toURI(), graphException);
          }

        }
      }

      return nextTriple.next();
    }

    return null;
  }

  /**
   * Does a depth first traversal of the given metafile tree to obtain the
   * starting point for data collection.
   *
   * @param file The file that we wish to find the starting point to
   *
   * @throws FileManagerException
   */
  private void findStartPoint(MetaFile file) throws FileManagerException {

    if (file.isDirectory()) {

      // We can only find the start point of a directory.  If it is a file then
      // the start point is obviously the file.

      // Container for our directory list
      MetaFile[] directories = new MetaFile[0];

      try {

        // Obtain a list of the directories in the root
        directories = file.listChildrenByFilter(new DirectoryFilter());
      } catch (MetaIOException metaIOException) {

        throw new FileManagerException("Failed to obtain directory list for " +
                                       file.toURI(), metaIOException);
      }

      // Start the currentDirectory at the given file
      currentDirectory = file;

      // Descend through the first directory of each sub-directory until we reach
      // a directory with no children.
      while (directories.length > 0) {

        // Container for our directory index marker
        int i = 0;

        // Iterate through the list of directories and find the first
        // non-excluded directory
        for (i = 0; i < directories.length; i++) {

          if (!exclusionList.contains(directories[i])) {

            // Prevent further checking as we've found our first non-excluded
            // directory
            break;
          }
        }

        if (i < directories.length) {

          // If we found a non-excluded file then store it as the new current
          // directory
          currentDirectory = directories[i];
        } else {

          // If we couldn't find any non-excluded files then do not proceed any
          // further
          break;
        }

        try {

          // Obtain the directories of the current directory
          directories = currentDirectory.listChildrenByFilter(new
              DirectoryFilter());
        } catch (MetaIOException metaIOException) {

          throw new FileManagerException("Failed to obtain children list for " +
                                         directories[0].toURI(),
                                         metaIOException);
        }
      }

      // Container for our child list
      MetaFile[] children = new MetaFile[0];

      try {

        // Obtain the list of children for the current directory
        children = currentDirectory.listChildren();
      } catch (MetaIOException metaIOException) {

        throw new FileManagerException("Failed to obtain directory list for " +
                                       file.toURI(), metaIOException);
      }

      if (children.length > 0) {

        // Set the current file to be the beginning of the child list for the
        // current directory if we have children
        currentChild = children[0];
      }
    } else {

      // Since we are using a file then just set it to be the current child
      currentChild = file;
    }
  }

  /**
   * Retrieve the next directory in the list of included directories and obtain
   * the children for it.  The metadata for the current directory should be
   * stored before moving on else it will be skipped.
   *
   * @throws FileManagerException
   */
  private void getNextDirectory() throws FileManagerException {

    if (currentDirectory.equals(root)) {

      if (log.isDebugEnabled()) {

        log.debug("Finalising processing of filesystem [" + root.toURI() + "]");
      }

      // Before switching to the next inclusion, get the metadata for the root
      // file
      currentChild = root;

      try {

        // Obtain the current child's metadata
        currentEntry = currentChild.getMetadata();
      } catch (MetaDataException metaDataException) {

        throw new FileManagerException("Failed to obtain metadata for " +
                                       currentChild.toURI(),
                                       metaDataException);
      } catch (MetaIOException metaIOException) {

        throw new FileManagerException("Failed to perform meta data io for " +
                                       currentChild.toURI(), metaIOException);
      }

      try {

        // Obtain the iterator for the graph
        nextTriple = currentEntry.find(null, null, null);
      } catch (GraphException graphException) {

        throw new FileManagerException(
            "Failed to obtain triple iterator for " +
            currentChild.toURI(), graphException);
      }
    } else {

      // Obtain the parent file to the current directory
      File parentFile = currentDirectory.getParentFile();

      // Advance the current directory by one
      currentDirectory = currentDirectory.nextSibling();

      if (!cycleToNextDirectory()) {

        // Container for our parent metafile
        MetaFile parent = null;

        try {

          // Get the parent of this directory as a directory
          parent = new MetaFile(parentFile.toURI());
        } catch (MetaIOException metaIOException) {

          throw new FileManagerException("Failed to create parent metafile" +
                                         "of " + parentFile.toURI(),
                                         metaIOException);
        }

        if (!parent.equals(root)) {

          if (log.isDebugEnabled()) {

            log.debug("Finding parent file of: " + parent.getPath() +
                      " from root " +
                      root.getPath());
          }

          // Check that we haven't traversed the last included directory and
          // find out where the parent directory falls within the structure of
          // its directory

          // Container for the super parent metafile
          MetaFile superParent = null;

          try {

            // Find the parent of the parent
            superParent = new MetaFile(parent.getParentFile().toURI());
          } catch (MetaIOException metaIOException) {

            throw new FileManagerException(
                "Failed to create super parent metafile" +
                "for " + parent.toURI(),
                metaIOException);
          }

          // Container for our directory list
          MetaFile[] directories = new MetaFile[0];

          try {

            // Find the directories of the super parent
            directories = superParent.listChildrenByFilter(new DirectoryFilter());
          } catch (MetaIOException metaIOException) {

            throw new FileManagerException(
                "Failed to obtain directory list for " +
                superParent.toURI(), metaIOException);
          }

          // Convert the array to a list
          List<MetaFile> directoryList = Arrays.asList(directories);

          // Obtain the ordered and linked parent metafile from the list
          parent = directoryList.get(directoryList.indexOf(parent));

          // Set the current directory to be the parent
          currentDirectory = parent;
        } else {

          // Set the current directory to be the root if we are back at the root
          currentDirectory = root;
        }
      } else {

        // Initialise the search using the new directory
        findStartPoint(currentDirectory);
      }

      // Container for the list of children
      MetaFile[] children = new MetaFile[0];

      try {

        // Obtain the list of children for the current directory
        children = currentDirectory.listChildren();
      } catch (MetaIOException metaIOException) {

        throw new FileManagerException("Failed to obtain children list for " +
                                       currentDirectory.toURI(),
                                       metaIOException);
      }

      if (children.length > 0) {

        // Set the current file to be the beginning of the child list for the
        // current directory if we have children
        currentChild = children[0];

        if (cycleToNextFile()) {

          try {

            // Obtain the current child's metadata
            currentEntry = currentChild.getMetadata();
          } catch (MetaDataException metaDataException) {

            throw new FileManagerException("Failed to obtain metadata for " +
                                           currentChild.toURI(),
                                           metaDataException);
          } catch (MetaIOException metaIOException) {

            throw new FileManagerException(
                "Failed to perform meta data io for " +
                currentChild.toURI(), metaIOException);
          }

          try {

            // Obtain the iterator for the graph
            nextTriple = currentEntry.find(null, null, null);
          } catch (GraphException graphException) {

            throw new FileManagerException(
                "Failed to obtain triple iterator for " +
                currentChild.toURI(), graphException);
          }
        } else {

          // If we found no included children in the directory then get the
          // next directory
          getNextDirectory();
        }
      } else {

        // If the directory is empty then skip it and try again
        getNextDirectory();
      }
    }
  }

  /**
   * Cycles through the currentChild list until either the next non-excluded
   * file is found or the end of the list reached.  The boolean returned
   * indicates whether a sibling was found (true) or the end of the list was
   * reached. (false)
   *
   * @return Whether a sibling was found or the end of the list was reached
   */
  private boolean cycleToNextFile() {

    // Cycle through the list of siblings until we find either the end of the
    // list or a non-excluded child
    while (currentChild != null && exclusionList.contains(currentChild)) {

      // Get the next child in the list
      currentChild = currentChild.nextSibling();
    }

    if (currentChild == null) {

      // If we have a null value then we reached the end of the list so report
      // false
      return false;
    }

    return true;
  }

  /**
   * Cycles through the currentDirectory list until either the next non-excluded
   * directory is found or the end of the list reached.  The boolean returned
   * indicates whether a sibling was found (true) or the end of the list was
   * reached. (false)
   *
   * @return Whether a sibling was found or the end of the list was reached
   */
  private boolean cycleToNextDirectory() {

    // Cycle through the list of siblings until we find either the end of the
    // list or a non-excluded directory
    while (currentDirectory != null && exclusionList.contains(currentDirectory)) {

      // Get the next child in the list
      currentDirectory = currentDirectory.nextSibling();
    }

    if (currentDirectory == null) {

      // If we have a null value then we reached the end of the list so report
      // false
      return false;
    }

    return true;
  }
}
