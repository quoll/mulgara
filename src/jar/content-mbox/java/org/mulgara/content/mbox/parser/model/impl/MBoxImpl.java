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

package org.mulgara.content.mbox.parser.model.impl;

import java.io.*;
import java.net.URI;
import java.util.Properties;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.mem.GraphImpl;
import org.jrdf.graph.mem.BlankNodeImpl;
import org.jrdf.vocabulary.RDF;
import org.apache.log4j.*;

import org.mulgara.content.Content;
import org.mulgara.content.NotModifiedException;
import org.mulgara.content.mbox.parser.*;
import org.mulgara.content.mbox.parser.exception.*;
import org.mulgara.content.mbox.parser.model.EmailVocab;
import org.mulgara.content.mbox.parser.model.MBox;
import org.mulgara.content.mbox.parser.model.exception.InvalidMBoxException;
import org.mulgara.content.mbox.parser.model.exception.ModelException;
import org.mulgara.content.mbox.parser.model.exception.VocabularyException;

/**
 * implementation of the MBox object interface which representes an MBox along
 * with its messages and RDF statements.  In addition to being the storage of an
 * MBox, it also allows for navigation of the parsed RDF statements.
 *
 * @created 2004-08-24
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.7 $
 *
 * @modified $Date: 2005/01/05 04:57:42 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MBoxImpl implements MBox {

  /** Logger */
  private static final Logger log = Logger.getLogger(MBoxImpl.class);

  /** The graph we will be using to store triples retrieved from messages */
  private Graph graph;

  /** The iterator to use for iterating the graph */
  private ClosableIterator<Triple> iterator;

  /** The tool to perform the actual parsing of an mbox into message beans */
  private MimeMessageToPart mimeMessageToPart;

  /** The content object this object represents */
  private Content mBoxContent;

  /**
   * Constructor creates a new MBox object.  The class using the object will be
   * required to set the graph and file to use.
   */
  public MBoxImpl(Content content) {

    // Instantiate our parsing tool
    mimeMessageToPart = new MimeMessageToPart();

    // Store the content object
    mBoxContent = content;
  }

  /**
   * Retrieves the mbox file location as an URI.
   *
   * @return The location of the mbox as an URI
   */
  public URI getURI() {
    return mBoxContent.getURI();
  }

  /**
   * Returns the mbox as a file object.
   *
   * @return The mbox as a file object
   */
  public InputStream getMBoxStream() throws IOException, NotModifiedException {

    // Get the actual input stream
    InputStream contentStream = mBoxContent.newInputStream();

    // Mark the beginning for resetting
    if (contentStream != null) contentStream.mark(contentStream.available());

    return contentStream;
  }

  /**
   * Retrieves the graph object that the mbox is using to store its content.
   *
   * @return The graph object that the mbox is using to store its content
   */
  public Graph getGraph() {
    return graph;
  }

  /**
   * Sets the graph object the mbox should be using to store its content.
   *
   * @param graph The graph object the mbox should use to store its content
   */
  public void setGraph(Graph graph) {
    // Store the new graph
    this.graph = graph;
  }

  /**
   * Retrieves the next triple in the mbox.  If there are no triples left to
   * navigate through then null is returned.
   *
   * @return The next triple in the mbox navigation cycle
   *
   * @throws ModelException
   */
  public Triple nextTriple() throws ModelException {

    // Container for our next triple if there is one
    Triple nextTriple = null;

    if (iterator.hasNext()) {

      // Get the next triple in the iterator
      nextTriple = iterator.next();
    } else {

      try {

        // Create a new graph to erase all triples (as the proper method
        // is currently broken)
        graph = new GraphImpl();
      } catch (GraphException graphException) {

        throw new ModelException("Unable to create new graph for triples",
                                 graphException);
      }

      try {

        // Retrieve the next set of triples for the next message
        obtainNextGraph();
      } catch (VocabularyException vocabularyException) {

        throw new ModelException("Unable to reference vocabulary to construct" +
                                 "triples", vocabularyException);
      }

      if (iterator.hasNext()) {

        // Get the next triple in the iterator if we have any, as the message
        // we just finished with may have been the last
        nextTriple = iterator.next();
      } else {

        // If we could not find any more messages then end the triple search
        nextTriple = null;
      }
    }

    if (nextTriple == null) {

      try {

        // If we have reached the end of the messages then close the message folder
        mimeMessageToPart.close();
      } catch (MimeMessageProcessException mimeMessageProcessException) {

        throw new ModelException("Unable to close mbox message folder",
                                 mimeMessageProcessException);
      }
    }

    return nextTriple;
  }

  /**
   * Resets the navigation of the mbox to return to the start of the file and
   * clears the graph, ready for starting again.
   *
   * @throws ModelException
   */
  public void reset() throws ModelException {

    try {

      // Reset the iterator
      iterator = graph.find(null, null, null);
    } catch (GraphException graphException) {

      throw new ModelException("Failed to remove contents of graph.",
                               graphException);
    }
  }

  /**
   * Clears all triples from the graph.
   *
   * @throws ModelException
   */
  public void clear() throws ModelException {

    try {

      // Clear the graph of its current content
      graph.remove(graph.find(null, null, null));
    } catch (GraphException graphException) {

      throw new ModelException("Failed to remove contents of graph.",
                               graphException);
    }
  }

  /**
   * Begins the parsing of an mbox and sets up the navigation to be ready for
   * iterating through the statements of the mbox.
   *
   * @throws ModelException
   * @throws InvalidMBoxException
   * @throws VocabularyException
   */
  public void start() throws ModelException, InvalidMBoxException,
      VocabularyException, NotModifiedException {

    // Container for our input stream
    InputStream contentStream = null;

    try {

      // Open a stream to the original resource
      contentStream = getMBoxStream();
    } catch (IOException ioException) {

      throw new ModelException("Failed to open stream to original resource: " +
                               getUriString(), ioException);
    }

    // Validate the file
    try {
      validate(contentStream);
    } finally {
      try {
        if (contentStream != null) contentStream.close();
      } catch (IOException e) {
        log.warn("Ignoring exception closing MBox input stream", e);
      }
    }

    try {

      // Create a new graph to store our triples
      graph = new GraphImpl();
    } catch (GraphException graphException) {

      throw new ModelException("Unable to create new graph for triples",
                               graphException);
    }

    try {

      // Open a stream to the original resource (As the other is now
      // not at the start of the stream
      contentStream = getMBoxStream();
    } catch (IOException ioException) {

      throw new ModelException("Failed to reopen stream to original resource: " +
                               getUriString(), ioException);
    }

    try {

      // Initialise the mbox for reading
      mimeMessageToPart.initialise(getURI(), contentStream);
    } catch (IOProcessingException ioProcessingException) {

      throw new ModelException("Failed to initialise mbox for processing due " +
                               "to an IO error.", ioProcessingException);
    } catch (MimeMessageProcessException mimeMessageProcessException) {

      throw new ModelException(
          "Failed to initalise mbox for processing due to " +
          "a bad mbox structure.",
          mimeMessageProcessException);
    }

    // Get the first set of metadata
    obtainNextGraph();
  }

  /**
   * Obtains the next set of triples from the next message in the mbox and
   * stores them in the graph.
   *
   * @throws ModelException
   * @throws VocabularyException
   */
  private void obtainNextGraph() throws ModelException, VocabularyException {

    // Container for our parts bean
    MimeMessageToPartBean bean = null;

    try {

      // Parse the next mime message
      bean = mimeMessageToPart.processNextMessage();
    } catch (IOProcessingException ioProcessingException) {

      // If we are only processing a single file then we want to report
      // exceptions, otherwise we just ignore the mbox because we don't
      // want to interrupt processing
      throw new ModelException(
          "Unable to process mbox '" + getUriString() +
          "' into parts due to I/O failure.", ioProcessingException);
    } catch (UnsupportedEncodingProcessingException
             unsupportedEncodingProcessingException) {

      // If we are only processing a single file then we want to report
      // exceptions, otherwise we just ignore the mbox because we don't
      // want to interrupt processing
      throw new ModelException(
          "Unable to process mbox into parts due to an " +
          "unsupported encoding.",
          unsupportedEncodingProcessingException);
    } catch (MimeMessageProcessException mimeMessageProcessingException) {

      // If we are only processing a single file then we want to report
      // exceptions, otherwise we just ignore the mbox because we don't
      // want to interrupt processing
      throw new ModelException(
          "Failed to process mbox into mime message parts.",
          mimeMessageProcessingException);
    }

    if (bean != null) {

      // Store the RDF of the bean if we received one (otherwise we are at the
      // end of the mbox)
      storeBeansRDF(bean);
    }

    try {

      // Initialise the iterator for iterating over the nodes of the graph
      iterator = graph.find(null, null, null);
    } catch (GraphException graphException) {

      throw new ModelException("Failed to retrieve iterator for graph.",
                               graphException);
    }
  }

  /**
   * Validates the given input stream and determines whether it is a proper mbox
   * or not.
   *
   * @param stream The stream to validate
   *
   * @throws InvalidMBoxException
   */
  private void validate(InputStream stream) throws InvalidMBoxException {

    if (stream == null) {

      // The mbox file cannot be null
      throw new InvalidMBoxException("Cannot parse null mbox objects.");
    }

    // Create an input stream reader
    InputStreamReader inputReader = new InputStreamReader(stream);

    // Create a buffered reader to read our file
    BufferedReader reader = new BufferedReader(inputReader);

    // Container for our line of the file
    String line = "";

    try {

      while (line != null && line.trim().equals("")) {

        // Get the first line of text from the mbox file
        line = reader.readLine();
      }
    } catch (IOException ioException) {

      // We could not read the mbox file
      throw new InvalidMBoxException("MBox file [" + getUriString() +
                                     "] was not able to be read from.",
                                     ioException);
    }

    if (line == null || !line.toLowerCase().startsWith("from ")) {

      // The mbox is not RFC822 compliant
      throw new InvalidMBoxException("MBox file [" + getUriString() +
                                     "] was not a valid RFC822 mbox.");
    } else {

      try {

        // Get the next line of text (if any)
        line = reader.readLine();
      } catch (IOException ioException) {

        // We could not read the mbox file
        throw new InvalidMBoxException("MBox file [" + getUriString() +
                                       "] was not able to be read from.",
                                       ioException);
      }

      if (line != null) {

        if ((line.trim().length() > 0 &&
             !line.split(" ")[0].endsWith(":")) || line.trim().length() == 0) {

          // We require a check for either a blank line or an invalid header

          // The mbox is not RFC822 compliant if the next line is not a header
          throw new InvalidMBoxException("MBox file [" + getUriString() +
                                         "] was not a valid RFC822 mbox.");
        }
      }
    }
  }

  /**
   * Stores the given bean in the graph as RDF triples.
   *
   * @param bean The bean containing the data to store in the graph
   *
   * @throws ModelException
   * @throws VocabularyException
   */
  private void storeBeansRDF(MimeMessageToPartBean bean) throws ModelException, VocabularyException {

    // Create the vocabulary for the graph
    Properties vocab = EmailVocab.createVocabulary(graph);

    // Retrieve the element factory from the graph
    GraphElementFactory factory = graph.getElementFactory();

    // Container for our subject node
    SubjectNode subject = null;

    if (log.isDebugEnabled()) log.debug("Creating graph nodes for bean: " + bean);

    try {
      // Create a subject blank node
      subject = factory.createResource();
    } catch (GraphElementFactoryException graphElementFactoryException) {
      throw new ModelException("Unable to create resource node for subject.",
                               graphElementFactoryException);
    }

    // Get the list of bccs
    List<QuollEmailAddress> bccList = bean.getBCCAddresses();

    for (int j = 0; j < bccList.size(); j++) {

      // Retrieve the next address
      QuollEmailAddress address = (QuollEmailAddress) bccList.get(j);

      // Container for our object node
      ObjectNode objectNode = null;

      try {

        // Create a literal out of the address
        objectNode = factory.createLiteral(address.getAddress());
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ModelException("Failed to create literal for mbox: " +
                                 getUriString(),
                                 graphElementFactoryException);
      }

      try {

        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.BCC),
                  objectNode);
      } catch (GraphException graphException) {

        throw new ModelException("Failed to add BCC address '" +
                                 address.getAddress() + "' to mbox: " +
                                 getUriString(), graphException);
      }

      if (log.isDebugEnabled()) {

        log.debug("Added BCC triple: [" + subject + ", " +
                  (PredicateNode) vocab.get(EmailVocab.BCC) +
                  ", " + objectNode + "]");
      }
    }

    // Get the list of ccs
    for (QuollEmailAddress address: bean.getCCAddresses()) {

      // Container for our object node
      ObjectNode objectNode = null;

      try {

        // Create a literal out of the address
        objectNode = factory.createLiteral(address.getAddress());
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ModelException("Failed to create literal for mbox: " +
                                 getUriString(),
                                 graphElementFactoryException);
      }

      try {

        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.CC),
                  objectNode);
      } catch (GraphException graphException) {

        throw new ModelException("Failed to add CC address '" +
                                 address.getAddress() + "' to mbox: " +
                                 getUriString(), graphException);
      }

      if (log.isDebugEnabled()) {

        log.debug("Added cc triple: [" + subject + ", " +
                  (PredicateNode) vocab.get(EmailVocab.CC) +
                  ", " + objectNode + "]");
      }
    }

    for (QuollEmailAddress address: bean.getToAddresses()) {

      // Container for our object node
      ObjectNode objectNode = null;

      try {

        // Create a literal out of the address
        objectNode = factory.createLiteral(address.getAddress());
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ModelException("Failed to create literal for mbox: " +
                                 getUriString(),
                                 graphElementFactoryException);
      }

      try {

        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.TO),
                  objectNode);
      } catch (GraphException graphException) {

        throw new ModelException("Failed to add TO address '" +
                                 address.getAddress() + "' to mbox: " +
                                 getUriString(), graphException);
      }

      if (log.isDebugEnabled()) {

        log.debug("Added to triple: [" + ((BlankNodeImpl) subject).getId() +
                  ", " +
                  (PredicateNode) vocab.get(EmailVocab.TO) +
                  ", " + objectNode + "]");
      }

    }

    // Get the list of ccs
    QuollEmailAddress from = bean.getFromAddress();

    // Container for our object node
    ObjectNode objectNode = null;

    if (from != null) {

      try {
        // Create a literal out of the address
        objectNode = factory.createLiteral(from.getAddress());
      } catch (GraphElementFactoryException graphElementFactoryException) {
        throw new ModelException("Failed to create literal for mbox: " + getUriString(), graphElementFactoryException);
      }

      try {
        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.FROM), objectNode);
      } catch (GraphException graphException) {
        throw new ModelException("Failed to add from address '" + from.getAddress() + "' to mbox: " +
                                 getUriString(), graphException);
      }
    }

    // Retrieve the date of the message
    Date date = bean.getDate();

    if (date != null) {

      try {
        // Create a literal out of the date
        objectNode = factory.createLiteral(formatDateTime(date));
      } catch (GraphElementFactoryException graphElementFactoryException) {
        throw new ModelException("Failed to create literal for mbox: " +
                                 getUriString(),
                                 graphElementFactoryException);
      }

      try {

        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.DATE), objectNode);
      } catch (GraphException graphException) {
        throw new ModelException("Failed to add date '" + formatDateTime(date) +
                                 "' to mbox: " + getUriString(),
                                 graphException);
      }
    }

    // Retrieve the ID of the message
    String id = bean.getMessageID();

    if (id != null) {

      try {

        // Create a literal out of the id
        objectNode = factory.createLiteral(id);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ModelException("Failed to create literal for mbox: " +
                                 getUriString(),
                                 graphElementFactoryException);
      }

      try {

        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.MESSAGE_ID),
                  objectNode);
      } catch (GraphException graphException) {

        throw new ModelException("Failed to add ID '" + id + "' to mbox: " +
                                 getUriString(), graphException);
      }

      if (log.isDebugEnabled()) {

        log.debug("Added messageID triple: [" +
                  ((BlankNodeImpl) subject).getId() + ", " +
                  (PredicateNode) vocab.get(EmailVocab.MESSAGE_ID) +
                  ", " + objectNode + "]");
      }
    }

    // Retrieve the references of the message
    for (String reference: bean.getReferences()) {
      try {
        // Create a literal out of the reference
        objectNode = factory.createLiteral(reference);
      } catch (GraphElementFactoryException graphElementFactoryException) {
        throw new ModelException("Failed to create literal for mbox: " +
                                 getUriString(),
                                 graphElementFactoryException);
      }

      try {
        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.REFERENCES), objectNode);
      } catch (GraphException graphException) {
        throw new ModelException("Failed to add reference '" + reference +
                                 "' to mbox: " + getUriString(), graphException);
      }
    }

    // Retrieve the subject of the message
    String subjectString = bean.getSubject();

    if (subjectString != null) {
      try {
        // Create a literal out of the subject
        objectNode = factory.createLiteral(subjectString);
      } catch (GraphElementFactoryException graphElementFactoryException) {
        throw new ModelException("Failed to create literal for mbox: " +
                                 getUriString(),
                                 graphElementFactoryException);
      }

      try {
        // Add the triple to the graph
        graph.add(subject, (PredicateNode) vocab.get(EmailVocab.SUBJECT), objectNode);
      } catch (GraphException graphException) {
        throw new ModelException("Failed to add subject '" + subjectString +
                                 "' to mbox: " + getUriString(), graphException);
      }
    }

    // Container for our type predicate
    PredicateNode typePredicate = null;

    try {
      // Create a predicate for type
      typePredicate = factory.createResource(RDF.TYPE);
    } catch (GraphElementFactoryException graphElementFactoryException) {
      throw new ModelException("Failed to create type predicate: " + RDF.TYPE, graphElementFactoryException);
    }

    try {
      // Add the type triple to the graph
      graph.add(subject, typePredicate, (ObjectNode) vocab.get(EmailVocab.MESSAGE));
    } catch (GraphException graphException) {
      throw new ModelException("Failed to add type '" + RDF.TYPE +
                               "' to mbox: " + getUriString(), graphException);
    }

    for (Attachment attachment: bean.getParts()) {
      // Add the next attachment to the resource
      addAttachment(subject, attachment, vocab);
    }

  }

  /**
   * Adds the given attachment as a resource of the given message subject.
   *
   * @param subject The node representing the message resource
   * @param attachment The attachment we are adding to the message
   * @param vocab The vocabulary mappings for emails
   *
   * @throws ModelException
   */
  private void addAttachment(SubjectNode subject, Attachment attachment,
                             Properties vocab) throws ModelException {

    // Retrieve the element factory from the graph
    GraphElementFactory factory = graph.getElementFactory();

    // Container for our attachment subject
    SubjectNode attSubject = null;

    // Container for our literals
    ObjectNode objectNode = null;

    try {

      // Create a blank node for the attachment subject
      attSubject = (SubjectNode) factory.createResource();
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ModelException("Failed to create subject for attachment: " +
                               attachment.getFilename(),
                               graphElementFactoryException);
    }

    // Retrieve the filename
    String filename = attachment.getFilename();

    if (filename != null) {

      try {

        // Create a literal out of the filename
        objectNode = factory.createLiteral(filename);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ModelException("Failed to create literal for attachment: " +
                                 attachment.getFilename(),
                                 graphElementFactoryException);
      }

      try {

        // Add the triple to the graph
        graph.add(attSubject, (PredicateNode) vocab.get(EmailVocab.FILE_NAME),
                  objectNode);
      } catch (GraphException graphException) {

        throw new ModelException("Failed to add filename '" + filename +
                                 "' to attachment: " + attachment.getFilename(),
                                 graphException);
      }
    }

    // Retrieve the mime type
    String mimeType = attachment.getMimeType();

    if (mimeType != null) {

      try {

        // Create a literal out of the mime type
        objectNode = factory.createLiteral(mimeType);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ModelException("Failed to create literal for attachment: " +
                                 attachment.getFilename(),
                                 graphElementFactoryException);
      }

      try {

        // Add the triple to the graph
        graph.add(attSubject, (PredicateNode) vocab.get(EmailVocab.MIME_TYPE),
                  objectNode);
      } catch (GraphException graphException) {

        throw new ModelException("Failed to add mime type '" + mimeType +
                                 "' to attachment: " + attachment.getFilename(),
                                 graphException);
      }
    }

    // Retrieve the size
    int size = attachment.getSize();

    try {

      // Create a literal out of the size
      objectNode = factory.createLiteral("" + size);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ModelException("Failed to create literal for attachment: " +
                               attachment.getFilename(),
                               graphElementFactoryException);
    }

    try {

      // Add the triple to the graph
      graph.add(attSubject, (PredicateNode) vocab.get(EmailVocab.SIZE),
                objectNode);
    } catch (GraphException graphException) {

      throw new ModelException("Failed to add size '" + size +
                               "' to attachment: " + attachment.getFilename(),
                               graphException);
    }

    // Container for our type predicate
    PredicateNode typePredicate = null;

    try {

      // Create a predicate for type
      typePredicate = factory.createResource(RDF.TYPE);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ModelException("Failed to create type predicate: " + RDF.TYPE,
                               graphElementFactoryException);
    }

    try {

      // Add the type triple to the graph
      graph.add(attSubject, typePredicate,
                (ObjectNode) vocab.get(EmailVocab.ATTACHMENT));
    } catch (GraphException graphException) {

      throw new ModelException("Failed to add type '" + RDF.TYPE +
                               "' to attachment: " +
                               attachment.getFilename(), graphException);
    }

    try {

      // Add the attachment as a resource of the message
      graph.add(subject, (PredicateNode) vocab.get(EmailVocab.HAS_ATTACHMENT),
                (ObjectNode) attSubject);
    } catch (GraphException graphException) {

      throw new ModelException("Failed to add attachment resource to mbox " +
                               "resource.", graphException);
    }
  }

  /**
   * Formats a date object to a date time string (yyyy-MM-ddTHH:mm:ss).
   *
   * @param date The date object to format
   * @return The string containing the parsed date time format
   */
  private String formatDateTime(Date date) {

    // Create a formatter to parse the date
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    // Return the parsed date
    return formatter.format(date);
  }

  /**
   * Gets a string representation of the URI.
   * @return A String for the URI of the content.
   */
  private String getUriString() {
    return mBoxContent.getURIString();
  }

  /**
   * Calculates whether the given object is equal to the current mbox.  Only the
   * file is required to be the same as the class is the encapsulation of a file
   * handle and will always have the same content for a particular handle.
   * There are three factors determining equality and they are:
   *
   * - The object is not null
   * - The object is an MBox
   * - The object has the same file handle
   *
   * @param object The object to compare to
   *
   * @return Whether the object is equal to the MBox
   */
  public boolean equals(Object object) {

    if (object == null) {

      // We cannot compare null objects
      return false;
    }

    // Container for the converted form of the object
    MBox mbox = null;

    try {
      // Cast the object to an MBox
      mbox = (MBox) object;
    } catch (ClassCastException classCastException) {
      // If the object is not of the right cast then it is not equal
      return false;
    }

    if (mbox.getURI() == null) {
      return getURI() == null;
    }

    if (mbox.getURI().compareTo(getURI()) == 0) {

      // If the file handles of the mboxes are equal then they match
      return true;
    } else {

      // The file handles are different and so the mboxes are different
      return false;
    }
  }
}
