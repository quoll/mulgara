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

package org.mulgara.content.mbox.parser;

// Java standard packages
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.URI;
import java.net.MalformedURLException;

// Java enterprise packages
import javax.mail.*;
import javax.mail.internet.*;

// Third party packages
import org.apache.log4j.*;

// Other quoll tool packages
import org.mulgara.content.mbox.parser.exception.*;
import org.mulgara.util.TempDir;

/**
 * A tool for splitting an internet <acronym title="Multipurpose Internet Mail
 * Extensions">MIME</acronym> into its parts (body and attachments, not
 * headers).
 *
 * @created 2003-03-07
 *
 * @author <a href="http://staff.pisoftware.com/raboczi"/>Simon Raboczi</a>
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:41 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/LICENCE">License description</A>
 */

public final class MimeMessageToPart {

  /** The category to log to. */
  private static final Logger logger = Logger.getLogger(MimeMessageToPart.class);

  /** The mbox splitter's session */
  private Session session;

  /** The mail message store */
  private Store store;

  /** The main folder of the store */
  private Folder folder;

  /** Flag to indicate whether we are using a chached file or not */
  private boolean cachedFile;

  /** Pointer to the message we are currently reading */
  private int messagePointer;

  /** URI pointing to the content file */
  private URI contentURI;

  /** Number of messages in the mbox */
  private int messageCount;

  /**
   * Constructor.
   */
  public MimeMessageToPart() {

  }

  /**
   * Initialises the mail store and associated folders using the given uri and
   * content stream.
   *
   * @param uri The uri of the mbox
   * @param contentStream A stream which contains the actual mbox data
   *
   * @throws IOProcessingException
   * @throws MimeMessageProcessException
   */
  public void initialise(URI uri, InputStream contentStream) throws
      IOProcessingException, MimeMessageProcessException {

    // Validate arguments
    if (uri == null) {

      throw new IllegalArgumentException("Null input uri");
    }

    // Boolean to flag if we used a cached file
    cachedFile = false;

    // Store the uri
    contentURI = uri;

    if (!uri.getScheme().equals("file")) {

      // If we have a non-file protocol then we need to cache the content before
      // parsing it into messages
      uri = cacheFile(contentStream, uri);

      // Set that we are using a cached file
      cachedFile = true;
    }

    // Create the session to our mbox
    session = Session.getDefaultInstance(new Properties());

    try {

      // Obtain the store representing our mbox
      store = new MboxStoreImpl(session, new URLName(uri.toURL()));
    } catch (MalformedURLException malformedURLException) {

      throw new IOProcessingException("Could not create URL from mbox uri: " +
                                      contentURI, malformedURLException);
    }

    try {

      // Obtain the inbox folder
      folder = store.getDefaultFolder();
    } catch (MessagingException messagingException) {

      throw new MimeMessageProcessException("Failed to obtain default folder " +
                                            "from mbox: " + contentURI,
                                            messagingException);
    }

    try {

      // store the number of messages in the mbox
      messageCount = folder.getMessageCount();
    } catch (MessagingException messagingException) {

      throw new MimeMessageProcessException("Failed to obtain message count " +
                                            "for mbox: " + contentURI,
                                            messagingException);

    }


    if (logger.isDebugEnabled()) {

       logger.debug("Found " + messageCount + " messages in folder");
    }

    // Reset the current message pointer
    messagePointer = 0;
  }

  /**
   * Retrieves the next message from the mbox.
   *
   * @return The next message in the mbox's bean
   *
   * @throws UnsupportedEncodingProcessingException
   * @throws MimeMessageProcessException
   * @throws IOProcessingException
   */
  public MimeMessageToPartBean processNextMessage() throws
      UnsupportedEncodingProcessingException, MimeMessageProcessException,
      IOProcessingException {

    if (messagePointer >= messageCount) {

      // If we have reached the end of the available messages then stop reading
      return null;
    }

    // Create a bean to store the information from the message
    MimeMessageToPartBean bean = new MimeMessageToPartBean();

    // Container for our message
    Part part = null;

    try {

      // Get the single message and write it out
      part = folder.getMessage(messagePointer);
    } catch (MessagingException messagingException) {

      throw new MimeMessageProcessException("Failed to find next message in " +
                                            "mbox", messagingException);
    }

    try {

      if (part instanceof MimeMessage) {

        if (logger.isDebugEnabled()) {

          logger.debug("Part was a mime message.  Now processing: " +
                       part.getDescription());
        }

        MimeMessage mimeMessage = (MimeMessage) part;

        // BCC recipients
        try {

          Address[] recipients =
              mimeMessage.getRecipients(Message.RecipientType.BCC);

          if (recipients != null) {

            for (int i = 0; i < recipients.length; i++) {

              bean.addBCCAddress(makeQuollEmailAddress(recipients[i]));
            }
          }
        } catch (AddressException e) {
          // discard misformatted header and press on
        }

        // CC recipients
        try {

          Address[] recipients =
              mimeMessage.getRecipients(Message.RecipientType.CC);

          if (recipients != null) {

            for (int i = 0; i < recipients.length; i++) {

              bean.addCCAddress(makeQuollEmailAddress(recipients[i]));
            }
          }
        } catch (AddressException e) {

          // discard misformatted header and press on
        }

        // Date
        bean.setDate(mimeMessage.getSentDate());

        // From
        try {
          Address[] fromAddresses = mimeMessage.getFrom();
          if (fromAddresses != null) {

            switch (fromAddresses.length) {

              case 1:

                // No @ symbol found - probably exchange alias
                if (fromAddresses[0].toString().indexOf('@') == -1) {

                  /*
                   * All the code in this conditional block is kludgery to handle
                   * the compounded effects of previous kludgery elsewhere in the
                   * system.  Uniaccess's output of Lotus Notes address headers
                   * conflicts with the output for internet headers, particularly
                   * with regard to the separator character.  The unit test data
                   * had quotes manually inserted around the addresses in which
                   * this occurred, to avoid confusing JavaMail with embedded
                   * commas.
                   */
                  String[] rawFromAddresses = mimeMessage.getHeader("From");
                  if (rawFromAddresses.length == 1) {

                    // Strip quotes
                    Matcher matcher =
                        Pattern.compile("\"([^\"]*)\"").matcher(
                        rawFromAddresses[0]);

                    if (matcher.matches()) {

                      rawFromAddresses[0] = matcher.group(1);
                    }

                    bean.setFromAddress(new QuollEmailAddress(
                        rawFromAddresses[0]));
                  } else {

                    StringBuffer buffer = new StringBuffer();

                    if (rawFromAddresses.length > 0) {

                      buffer.append(rawFromAddresses[0]);

                      for (int i = 1; i < rawFromAddresses.length; i++) {

                        buffer.append(" ").append(rawFromAddresses[i]);
                      }
                    }
                    logger.debug(
                        "Message with " + rawFromAddresses.length +
                        " FROM headers: " +
                        buffer
                        );

                    bean.setFromAddress(new QuollEmailAddress(buffer.
                        toString()));
                  }
                } else {

                  bean.setFromAddress(makeQuollEmailAddress(fromAddresses[0]));
                }
              case 0:
                break;
              default:
                StringBuffer buffer = new StringBuffer();
                if (fromAddresses.length > 0) {

                  buffer.append(fromAddresses[0]);

                  for (int i = 1; i < fromAddresses.length; i++) {

                    buffer.append(" ").append(fromAddresses[i]);
                  }
                }
                logger.debug(
                    "Message with " + fromAddresses.length + " senders: " +
                    buffer
                    );

                bean.setFromAddress(new QuollEmailAddress(buffer.toString()));
            }
          }
        } catch (AddressException e) {

          // discard misformatted header and press on
        }

        // Message ID
        bean.setMessageID(mimeMessage.getMessageID());

        // Subject
        bean.setSubject(mimeMessage.getSubject());

        // Reference ID's
        String[] refs = mimeMessage.getHeader("references");

        if (refs != null) {

          StringTokenizer refsTok = new StringTokenizer(refs[0], "<> \t", false);
          int count = refsTok.countTokens();

          for (int i = 1; i <= count; i++) {

            String ref = refsTok.nextToken();

            if (ref != null) {

              bean.addReference(ref);
            }
          }
        }

        // To recipients
        try {

          Address[] recipients =
              mimeMessage.getRecipients(Message.RecipientType.TO);

          if (recipients != null) {

            for (int i = 0; i < recipients.length; i++) {

              bean.addToAddress(makeQuollEmailAddress(recipients[i]));
            }
          }
        } catch (AddressException e) {

          // discard misformatted header and press on
        }
      } else {

        throw new MimeMessageProcessException("Can't process " +
                                              part.getClass());
      }

      // Container for our content
      Object content = null;

      try {

        // Try to get the content of the message.  This is prone to fail if
        // the Content-Type header's charset value ends with a semi-colon so
        // we need to catch runtime exceptions in order to avoid crashing on
        // such messages
        content = part.getContent();

        if (logger.isDebugEnabled()) {

          logger.debug("Type of content is: " + content.getClass());
        }
      } catch (RuntimeException runtimeException) {

        // We do not want to halt progress of the processing so just warn and
        // ignore
        logger.warn("Failed to get content of part '" + bean.getMessageID() +
                    "' due to bad header formatting", runtimeException);
      }

      // Populate content
      if (content instanceof Multipart) {

        if (logger.isDebugEnabled()) {

          logger.debug("Getting parts of multipart object.");
        }

        // Split into subparts
        Multipart multipart;
        multipart = (Multipart) part.getContent();
        try {

          for (int i = 0; i < multipart.getCount(); i++) {

            BodyPart bodyPart = multipart.getBodyPart(i);

            // This is a kludge to handle spurious empty parts caused by
            // trailing blank lines; any part lacking a Content-Type header
            // is assumed to be spurious
            String[] rawContentType = bodyPart.getHeader("Content-Type");

            if (rawContentType == null) {

              continue;
            }

            // Get the details of the attachment
            findAttachmentDetails(bean, bodyPart);
          }
        } catch (javax.mail.MessagingException e) {

          // This is a kludge to handle the case where a multipart/* message
           // has zero attachments; Uniaccess tends to generate such mboxes
           // from Lotus Notes calendars.
           if (e.getMessage().equals("Missing start boundary")) {

             // We ignore this problem and just go to the next message
           } else throw e;
        }
      } else {

        // Get the details of the attachment
        //findAttachmentDetails(beans[j], part);
      }

      if (logger.isDebugEnabled()) {

        logger.debug("Created a message bean: " + bean);
      }

      // Increment our message pointer to the next message
      messagePointer++;

      // Return the list of generated mime message beans
      return bean;
    } catch (javax.mail.MessagingException me) {

      throw new MimeMessageProcessException(
          "Couldn't process the message into " +
          "parts", me);
    } catch (java.io.UnsupportedEncodingException uee) {

      throw new UnsupportedEncodingProcessingException("Could decode message",
          uee);
    } catch (IOException e) {

      throw new IOProcessingException("Couldn't read/write the message", e);
    }
  }

  /**
   * Closes all associated services to the message splitter and releases any
   * resources.
   *
   * @throws MimeMessageProcessException
   */
  public void close() throws MimeMessageProcessException {

    if (cachedFile) {

      // If we used a cached file then clean it up
      tidyCachedFile(contentURI);
    }

    // Clear global variables
    session = null;
    store = null;

    try {

      // Close the
      folder.close(true);
    } catch (MessagingException messagingException) {

      throw new MimeMessageProcessException("Failed to close default folder " +
                                            "for mbox: " + contentURI,
                                            messagingException);
    }

    // Clear the folder object
    folder = null;
  }

  /**
   * Calculate the size, mime type, and filename of the given attachment.
   *
   * @param  bean  The container of information about a particular message
   * @param  part  The part we are finding the details of
   */
  private void findAttachmentDetails(MimeMessageToPartBean bean, Part part) throws
      IOException, javax.mail.MessagingException {

    // Create an attachment to store our part data
    Attachment attachment = new Attachment();

    // Set the filename
    attachment.setFilename(part.getFileName());

    // Set the size
    attachment.setSize(part.getSize());

    // Set the mime type
    attachment.setMimeType(part.getContentType());

    if (logger.isDebugEnabled()) {

      logger.debug("Added attachment: [" + attachment.getFilename() + ", " +
                   attachment.getMimeType() + ", " +
                   attachment.getSize() + "]");
    }

    // record the details of the file
    bean.addPart(attachment);
  }

  /**
   * Make a quoll email address from a javax.mail.Address.
   *
   * @param address The original address.
   * @return The quoll email address.
   */
  private QuollEmailAddress makeQuollEmailAddress(Address address) {

    // Set address and personal name seperately if possible
    if (address instanceof InternetAddress) {
      InternetAddress internetAddress = (InternetAddress) address;
      QuollEmailAddress result = new QuollEmailAddress(internetAddress.
          getAddress());
      result.setPersonalName(internetAddress.getPersonal());
      return result;
    } else {
      return new QuollEmailAddress(address.toString());
    }
  }

  /**
   * Caches the content of the input stream locally and returns the URI pointer
   * to the cached file.
   *
   * @param contentStream The stream that contains the file to cache
   * @param originalUri The original uri of the file
   *
   * @return The URI to the cached file
   *
   * @throws IOProcessingException
   */
  private URI cacheFile(InputStream contentStream, URI originalUri) throws
      IOProcessingException {

    // Create a cache directory in the temporary directory
    File cacheDir = new File(TempDir.getTempDir(), "mbox-cache");

    // Container for the original file name
    String filename = null;

    try {

      // Obtain the original file name
      filename = originalUri.toURL().getFile();
    } catch (MalformedURLException malformedURLException) {

      throw new IOProcessingException("Failed to obtain filename from URI: " +
                                      originalUri.toString(),
                                      malformedURLException);
    }

    // Remove the leading slash
    filename = filename.substring(1, filename.length());

    // Create the directory
    cacheDir.mkdirs();

    // Create a cached version of the message as a local file
    File cacheFile = new File(cacheDir, "mbox-message-" +
                              filename + ".mbox");

    // Container for our print writer
    PrintWriter writer = null;

    try {
      // Open a writer to the cached file
      writer = new PrintWriter(new FileWriter(cacheFile));
    } catch (IOException ioException) {

      throw new IOProcessingException("Failed to open writer to " +
                                      "temporary cache file: " +
                                      cacheFile.getAbsolutePath(), ioException);
    }

    // Create a reader for the original stream
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        contentStream));

    // Container for our current line
    String line = null;

    try {

      // Get the first line of the stream
      line = reader.readLine();
    } catch (IOException ioException) {

      throw new IOProcessingException("Could not read from source stream: " +
                                      originalUri.toString(), ioException);
    }

    // Read the stream line by line until we encounter its end
    while (line != null) {

      // Write out the next line of the stream
      writer.println(line);

      try {

        // Get the next line of the stream
        line = reader.readLine();
      } catch (IOException ioException) {

        throw new IOProcessingException("Could not read from source stream: " +
                                        originalUri.toString(), ioException);
      }
    }

    // Close the writer
    writer.flush();
    writer.close();

    try {

      // Close the stream
      reader.close();
    } catch (IOException ioException) {

      logger.warn("Ignoring failure to close input stream from mbox: " +
                  originalUri.toString(), ioException);

      // We can't do much about streams that refuse to close, so as long as we
      // have our data we can safely ignore the problem.
    }

    return cacheFile.toURI();
  }

  /**
   * Removes a cached file from the given URI.
   *
   * @param uri The URI of the cached file to remove
   */
  public void tidyCachedFile(URI uri) {

    // Create a file from the URI
    File cacheFile = new File(uri);

    // Assume we can delete the file
    boolean couldDelete = true;

    try {

      // Remove the file
      couldDelete = cacheFile.delete();
    } catch (SecurityException securityException) {

      // Log the problem but ignore it as we can't do anything about it
      logger.warn("Unable to gain access for deletion of " +
                  cacheFile.getAbsolutePath(), securityException);
    }

    if (!couldDelete) {

      // Log the problem but ignore it as we can't do anything about it
      logger.warn("Failed to delete [" + cacheFile.getAbsolutePath() +
                  "] for an unknown reason.");
    }

  }
}
