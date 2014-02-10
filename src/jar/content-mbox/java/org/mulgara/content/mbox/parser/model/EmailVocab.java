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

package org.mulgara.content.mbox.parser.model;

import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;

import org.jrdf.graph.*;

import org.mulgara.content.mbox.parser.model.exception.VocabularyException;

/**
 * Presents a vocabulary of email related properties.
 *
 * @created 2004-08-26
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
public class EmailVocab {

  /** The message resource uri */
  public static final String MESSAGE_URI =
      "http://xmlns.filsa.net/emir/0.2/#Message";

  /** The subject property uri */
  public static final String SUBJECT_URI =
      "http://xmlns.filsa.net/emir/0.2/#subject";

  /** The date property uri */
  public static final String DATE_URI = "http://xmlns.filsa.net/emir/0.2/#date";

  /** The to property uri */
  public static final String TO_URI = "http://xmlns.filsa.net/emir/0.2/#to";

  /** The from property uri */
  public static final String FROM_URI = "http://xmlns.filsa.net/emir/0.2/#from";

  /** The cc property uri */
  public static final String CC_URI = "http://xmlns.filsa.net/emir/0.2/#cc";

  /** The bcc property uri */
  public static final String BCC_URI = "http://xmlns.filsa.net/emir/0.2/#bcc";

  /** The measge ID property uri */
  public static final String MESSAGE_ID_URI =
      "http://xmlns.filsa.net/emir/0.2/#messageid";

  /** The in reply to property uri */
  public static final String IN_REPLY_TO_URI =
      "http://xmlns.filsa.net/emir/0.2/#inreplyto";

  /** The references property uri */
  public static final String REFERENCES_URI =
      "http://xmlns.filsa.net/emir/0.2/#references";

  /** The archive URL property uri */
  public static final String ARCHIVE_URL_URI =
      "http://xmlns.filsa.net/emir/0.2/#archiveurl";

  /** The body property uri (Optional with use of ArchiveURL) */
  public static final String BODY_URI = "http:/xmlns.filsa.net/emir/0.2/#body";

  /** The attachment resource uri */
  public static final String ATTACHMENT_URI =
      "http://xmlns.filsa.net/emir/0.2/#Attachment";

  /** The mime type property uri (for attachments) */
  public static final String MIME_TYPE_URI =
      "http://purl.org/dc/elements/1.1/format";

  /** The size property uri (for attachments) */
  public static final String SIZE_URI = "http://xmlns.filsa.net/emir/0.2/#size";

  /** The filename property uri (for attachments) */
  public static final String FILE_NAME_URI =
      "http://xmlns.filsa.net/emir/0.2/#filename";

  /** The property to state that there is an attachment */
  public static final String HAS_ATTACHMENT_URI =
      "http://xmlns.filsa.net/emir/0.2/#hasAttachment";

  /** The message resource key */
  public static final String MESSAGE = "message";

  /** The subject property key */
  public static final String SUBJECT = "subject";

  /** The date property key */
  public static final String DATE = "date";

  /** The to property key */
  public static final String TO = "to";

  /** The from property key */
  public static final String FROM = "from";

  /** The cc property key */
  public static final String CC = "cc";

  /** The bcc property key */
  public static final String BCC = "bcc";

  /** The measge ID property key */
  public static final String MESSAGE_ID = "messageid";

  /** The in reply to property key */
  public static final String IN_REPLY_TO = "inreplyto";

  /** The references property key */
  public static final String REFERENCES = "references";

  /** The archive URL property key */
  public static final String ARCHIVE_URL = "archiveurl";

  /** The body property key */
  public static final String BODY = "body";

  /** The attachment resource key */
  public static final String ATTACHMENT = "attachment";

  /** The mime type property key */
  public static final String MIME_TYPE = "format";

  /** The size property key */
  public static final String SIZE = "size";

  /** The filename property key */
  public static final String FILE_NAME = "filename";

  /** The has attachment property key */
  public static final String HAS_ATTACHMENT = "hasattachment";

  /**
   * Creates a set of properties which allow for access to predicates which are
   * used in emails for a given graph.
   *
   * @param graph The graph to create the predicates for
   *
   * @return The properties map
   *
   * @throws VocabularyException
   */
  public static Properties createVocabulary(Graph graph) throws
      VocabularyException {

    // Create a new properties object to store out vocab mappings
    Properties properties = new Properties();

    // Retrieve the factory for creating elements
    GraphElementFactory factory = graph.getElementFactory();

    // Container for the message URI
    URI messageURI = null;

    // Container for the subject URI
    URI subjectURI = null;

    // Container for the date URI
    URI dateURI = null;

    // Container for the to URI
    URI toURI = null;

    // Container for the from URI
    URI fromURI = null;

    // Container for the cc URI
    URI ccURI = null;

    // Container for the bcc URI
    URI bccURI = null;

    // Container for the message ID URI
    URI messageIdURI = null;

    // Container for the in reply to URI
    URI inReplyToURI = null;

    // Container for the references URI
    URI referencesURI = null;

    // Container for the archive url URI
    URI archiveUrlURI = null;

    // Container for the body URI
    URI bodyURI = null;

    // Container for the attachment URI
    URI attachmentURI = null;

    // Container for the format URI
    URI formatURI = null;

    // Container for the size URI
    URI sizeURI = null;

    // Container for the filename URI
    URI filenameURI = null;

    // Container for the has attachment URI
    URI hasAttachmentURI = null;

    try {

      // Create the message URI
      messageURI = new URI(MESSAGE_URI);

      // Create the subject URI
      subjectURI = new URI(SUBJECT_URI);

      // Create the date URI
      dateURI = new URI(DATE_URI);

      // Create the to URI
      toURI = new URI(TO_URI);

      // Create the from URI
      fromURI = new URI(FROM_URI);

      // Create the cc URI
      ccURI = new URI(CC_URI);

      // Create the bcc URI
      bccURI = new URI(BCC_URI);

      // Create the message ID URI
      messageIdURI = new URI(MESSAGE_ID_URI);

      // Create the in reply to URI
      inReplyToURI = new URI(IN_REPLY_TO_URI);

      // Create the references URI
      referencesURI = new URI(REFERENCES_URI);

      // Create the archive url URI
      archiveUrlURI = new URI(ARCHIVE_URL_URI);

      // Create the body URI
      bodyURI = new URI(BODY_URI);

      // Create the attachment URI
      attachmentURI = new URI(ATTACHMENT_URI);

      // Create the format URI
      formatURI = new URI(MIME_TYPE_URI);

      // Create the size URI
      sizeURI = new URI(SIZE_URI);

      // Create the filename URI
      filenameURI = new URI(FILE_NAME_URI);

      // Create the has attachment URI
      hasAttachmentURI = new URI(HAS_ATTACHMENT_URI);

    } catch (URISyntaxException uriSyntaxException) {

      throw new VocabularyException("Failed to create URI representations of " +
                                    "properties due to bad syntax.",
                                    uriSyntaxException);
    }

    try {

      // Add a mapping for the message predicate
      properties.put(MESSAGE, factory.createResource(messageURI));

      // Add a mapping for the subject predicate
      properties.put(SUBJECT, factory.createResource(subjectURI));

      // Add a mapping for the date predicate
      properties.put(DATE, factory.createResource(dateURI));

      // Add a mapping for the to predicate
      properties.put(TO, factory.createResource(toURI));

      // Add a mapping for the from predicate
      properties.put(FROM, factory.createResource(fromURI));

      // Add a mapping for the cc predicate
      properties.put(CC, factory.createResource(ccURI));

      // Add a mapping for the bcc predicate
      properties.put(BCC, factory.createResource(bccURI));

      // Add a mapping for the message ID predicate
      properties.put(MESSAGE_ID, factory.createResource(messageIdURI));

      // Add a mapping for the in reply to predicate
      properties.put(IN_REPLY_TO, factory.createResource(inReplyToURI));

      // Add a mapping for the references predicate
      properties.put(REFERENCES, factory.createResource(referencesURI));

      // Add a mapping for the archive URL predicate
      properties.put(ARCHIVE_URL, factory.createResource(archiveUrlURI));

      // Add a mapping for the body predicate
      properties.put(BODY, factory.createResource(bodyURI));

      // Add a mapping for the attachment predicate
      properties.put(ATTACHMENT, factory.createResource(attachmentURI));

      // Add a mapping for the mime type predicate
      properties.put(MIME_TYPE, factory.createResource(formatURI));

      // Add a mapping for the size predicate
      properties.put(SIZE, factory.createResource(sizeURI));

      // Add a mapping for the file name predicate
      properties.put(FILE_NAME, factory.createResource(filenameURI));

      // Add a mapping for the has attachment predicate
      properties.put(HAS_ATTACHMENT, factory.createResource(hasAttachmentURI));
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new VocabularyException("Failed to create predicates for vocabulary" +
                                    " elements.", graphElementFactoryException);
    }

    return properties;
  }
}
