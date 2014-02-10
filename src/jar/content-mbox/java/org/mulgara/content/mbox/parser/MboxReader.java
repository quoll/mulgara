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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.mail.*;

// logging
import org.apache.log4j.*;

/*
 * MboxFolder.java
 * Copyright (C) 1999 dog <dog@dog.net.uk>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * You also have permission to link it with the Sun Microsystems, Inc.
 * JavaMail(tm) extension and run that combination.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * You may retrieve the latest version of this library from
 * http://www.dog.net.uk/knife/
 *
 * Contributor(s): Daniel Thor Kristjan <danielk@cat.nyu.edu> close and expunge
 *                 clarification.
 *                 Sverre Huseby <sverrehu@online.no> gzipped mailboxes
 */

/**
 * This is a refactoring of the original MBoxFolderImpl.  Basically, taking out
 * the private methods and putting them into a class of their own.  This is so
 * that reading of the folder and indexing it is in its own separate class.  The
 * global access to files and messages are now away from the implementation of
 * the Java Mail API.
 * <P/>
 *
 * This code is derived from the 'knife' mail and news client
 * at http://www.dog.net.uk/knife.
 * <P/>
 *
 * @created 2001-8-21
 *
 * @author dog@dog.net.uk
 * @author Andrew Newman
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
 * @copyright &copy; 2001
 *   <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 *
 * @licence <A href="{@docRoot}/../../LICENCE_LGPL.txt">Licence description</A>
 */
public class MboxReader {

  private static final String INDEX_EXTENSION = ".idx";

  /** Log category */
  private static final Logger log = Logger.getLogger(MboxReader.class);

  private File file;
  private File indexFile;
  private boolean indexExists = false;
  private boolean readOnly = false;
  private MboxFolderImpl myFolder;
  private int messageCount = -1;

  /**
   * Constructor.
   */
  protected MboxReader(MboxFolderImpl newFolder, String filename) {
    file = new File(filename);
    indexFile = new File(filename + INDEX_EXTENSION);
    if ((indexFile.canRead()) && (indexFile.exists())) {
      indexed();
    }
    readOnly = !this.canWrite();
    myFolder = newFolder;
  }

  /**
   * Returns the file for the mail box.
   *
   * @return the file for the mail box.
   */
  public File getFile() {
    return file;
  }

  /**
   * Returns if the mail box has been indexed.
   *
   * @return if the mail box has been indexed.
   */
  public boolean isIndexed() {
    return indexExists;
  }

  /**
   * Indicates that the mail box has been indexed.
   */
  public void indexed() {
    indexExists = true;
  }

  /**
   * Return the mail box files name.
   *
   * @return the mail box files name.
   */
  public String getName() {
    return file.getName();
  }

  /**
   * Return the mail box absolute path.
   *
   * @return the mail box file name.
   */
  public String getFullName() {
    return file.getAbsolutePath();
  }

  /**
   * Indicates whether this folder exists.
   *
   * @exception MessagingException if a messaging error occurred
   */
  public boolean exists() throws MessagingException {
    return file.exists();
  }

  /**
   * Return whether the file can be written to or not.
   *
   * @return whether the file can be written to or not.
   */
  public boolean canWrite() {
    return file.canWrite();
  }

  /**
   * Return whether the file can be read or not.
   *
   * @return whether the file can be read or not.
   */
  public boolean canRead() {
    return file.canRead();
  }

  /**
   * Sets the readOnly flag of this mail box to true.  No writes will be
   * allowed.
   */
  public void setReadOnly() {
    readOnly = true;
  }

  /**
   * Tests if the flag of this mail box is set to read-only.
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Does a simple check to see whether the file is of mbox format.
   *
   * @return if the file is of mbox format.
   * @throws MessagingException if the file could not be opened.
   */
  public boolean isMboxFormat() throws MessagingException {

    boolean isMboxFormat = false;

    try {
      InputStream is = getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          new CRLFInputStream(is)));
      String line = reader.readLine();
      if ((line != null) && ((line.toLowerCase().startsWith("from ")))) {
        isMboxFormat = true;
      }
      reader.close();
      is.close();
      is = null;
    } catch (IOException e) {
      throw new MessagingException("Unable to open folder: " +
                                   file.getAbsolutePath(), e);
    }
    return isMboxFormat;
  }

  /**
   * Create an index of the file.  This simply holds the offset of each message
   * in the raw mbox file.  The index contains the number of messages and then
   * the offsets to the file.  Eventually, it should contain the last time
   * the mail box file was modified so that it can be reindexed as required.
   */
  public void indexMbox() throws MessagingException {
    synchronized (this) {
      if (!isIndexed()) {
        int noMails = 0;
        long offset = 0;
        ArrayList<Long> offsets = new ArrayList<Long>();

        // Read in the offsets to a array list.
        try {
          // replace because it stopped reading when it encountered a null
          // character in a file - which is NOT the EOF
          //BufferedRandomAccessFile mailBox = new BufferedRandomAccessFile(file,
          RandomAccessFile mailBox = new RandomAccessFile(file, "r");

          String line;

          // Container for the from line in order to calculate the message start
          String fromLine = "";

          line = mailBox.readLine();

          // Process the initial line for the message
          if (line != null && line.toLowerCase().startsWith("from ")) {

            if (log.isDebugEnabled()) {

              log.debug("Found initial from");
            }

            // Check if we have the proper message starter sequence

            // Since we need to know the beginning of the message, we start
            // to store how far along we are progressing for resetting the
            // pointer's origin
            fromLine = line;

            // Get the next line
            line = mailBox.readLine();

            if (log.isDebugEnabled()) {

              log.debug("Checking for header line in " + line);
            }

            if (line.length() > 0 && line.split(" ")[0].endsWith(":")) {

              // Check that the next line is a header of some
              // sort.  There is a 99.99% certainty this is always the case as
              // there should be at least a "From:" header

              // Concatenate the new line onto our marker line
              fromLine += "\n" + line;

              // The offset to the from entry in the file.
              // Assumes Unix file for now.
              offset = mailBox.getFilePointer() - fromLine.length() - 1;
              offsets.add(new Long(offset));
              noMails++;
            }
          }

          while (line != null) {

            if (line.trim().equals("")) {

              // There is a specific sequence to testing mail messages and the
              // first check is a blank line, then progress to the next
              line = mailBox.readLine();

              if (log.isDebugEnabled()) {

                                log.debug("Checking for message in " + line);
                              }

              if (line != null && line.toLowerCase().startsWith("from ")) {

                // The second check is the next line starts with 'from'

                // Since we need to know the beginning of the message, we start
                // to store how far along we are progressing for resetting the
                // pointer's origin
                fromLine = line;

                // Get the next line
                line = mailBox.readLine();

                if (log.isDebugEnabled()) {

                  log.debug("Checking for header line in " + line);
                }

                if (line != null && line.length() > 0 &&
                    line.split(" ")[0].indexOf(":") >= 0) {

                  // The third check is that the next line is a header of some
                  // sort.  There is a 99.99% certainty this is always the case as
                  // there should be at least a "From:" header

                  // Concatenate the new line onto our marker line
                  fromLine += "\n" + line;

                  // The offset to the from entry in the file.
                  // Assumes Unix file for now.
                  offset = mailBox.getFilePointer() - fromLine.length() - 1;
                  offsets.add(new Long(offset));
                  noMails++;
                }
              }
            } else {

              // Get the next line
              line = mailBox.readLine();
            }
          }
          mailBox.close();

          log.info("Indexed " + noMails + " messages in MBOX file " +
                   file.getCanonicalPath());

        } catch (IOException e) {
          throw new MessagingException("I/O error reading mailbox", e);
        }

        //Create the index file.
        try {
          if (!indexFile.exists()) {
            indexFile.createNewFile();
          }
          if (indexFile.canWrite()) {
            FileOutputStream fos = new FileOutputStream(indexFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream index = new ObjectOutputStream(bos);

            // Write number of message/offsets.
            index.writeInt(noMails);

            // Write offsets
            for (Long offsetl: offsets) {
              index.writeLong(offsetl);
            }
            index.close();
            index = null;
            bos.close();
            bos = null;
            fos.close();
            fos = null;

            indexed();
          } else {
            throw new MessagingException("Cannot write index file: " +
                                         indexFile.getName());
          }
        } catch (IOException ioe) {
          throw new MessagingException("Error indexing file: " +
                                       indexFile.getName() + ", " +
                                       ioe.getMessage());
        }
      }
    }
  }

  /**
   * Returns the number of messages in this folder.
   *
   * @return the number of messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getMessageCount() throws MessagingException {

    // Open the index file and read the first long.  This is the number of
    // messages.
    if (messageCount == -1) {

      this.indexMbox();

      try {
        ObjectInputStream index = new ObjectInputStream(
            new FileInputStream(indexFile));
        messageCount = index.readInt();
        index.close();
      } catch (IOException ioe) {
        throw new MessagingException("Error indexing file: " +
                                     indexFile.getName() + ", " +
                                     ioe.getMessage());
      }
    }
    return messageCount;
  }

  /**
   * Returns the specified message from this folder.  Calls
   * getMessages(msgnum, msgnum).
   *
   *
   * @param msgnum the index to the message in the store.
   * @exception MessagingException if a messaging error occurred
   */
  public Message getMessage(int msgnum) throws MessagingException {
    return (getMessages(msgnum, msgnum)[0]);
  }

  /**
   * Returns all the messages in this folder.  Calls
   * getMessages(0, getMessageCount).
   *
   * @return all the messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public Message[] getMessages() throws MessagingException {
    return getMessages(0, getMessageCount());
  }

  /**
   * Returns all the messages within the give range.
   */
  public Message[] getMessages(int msgStart, int msgEnd) throws
      MessagingException {
    return collectionToMessageArray(getMessagesAsArrayList(msgStart, msgEnd));
  }

  /**
   * Returns the specified messages in a given range.
   *
   * @param msgStart the starting index of the messages.
   * @param msgEnd the finishing index of the messages.
   * @return the specified messages in a given range.
   * @exception MessagingException if a messaging error occurred.
   */
  public ArrayList<Message> getMessagesAsArrayList(int msgStart, int msgEnd) throws
      MessagingException {
    //Test to see if it above the max number of messages
    long numberOfMessages = getMessageCount();

    if ((numberOfMessages < msgStart) || (numberOfMessages < msgEnd)) {
      throw new MessagingException(
          "Message number higher than maximum number" +
          "of messages.");
    }

    ArrayList<Message> messages = new ArrayList<Message>();
//    messages.add(getMessage(0, 1));

    // Read the location of the message using the index
    FileInputStream fis = null;
    ObjectInputStream index = null;
    try {
      fis = new FileInputStream(indexFile);
      index = new ObjectInputStream(fis);

      // Skip the first entry which is the number of messages

      index.readInt();
      long offset = 0;

      // Skip the following entries
      for (int count = 0; count < msgStart; count++) {
        offset = index.readLong();
      }

      // Start reading the files
      for (int count = msgStart; count < msgEnd + 1; count++) {
        offset = index.readLong();
        messages.add(getMessage(offset, count));
      }

      // close file handles
      index.close();
      fis.close();
      index = null;
      fis = null;

      // Use a reader to get that file.
      return messages;
    } catch (IOException ioe) {
      throw new MessagingException("I/O error reading index ", ioe);
    } finally {

      try {

        if (index != null) {
          index.close();
          index = null;
        }

        if (fis != null) {
          fis.close();
          fis = null;
        }
      } catch (IOException ioe2) {
        throw new MessagingException("I/O error reading index ", ioe2);
      }
    }
  }

  /**
   * Reads the message file beginning at the current offset.
   *
   * @param fileOffset the number of bytes to skip in the message file.
   * @param msgnum the number of the message.
   * @return the full formed message.
   * @throws MessagingException if the message was of the incorrect format.
   */
  private Message getMessage(long fileOffset, int msgnum) throws
      MessagingException {
    try {
      //BufferedRandomAccessFile raf = new BufferedRandomAccessFile(file, "r", 4096);
      RandomAccessFile raf = new RandomAccessFile(file, "r");
      raf.seek(fileOffset);
      Message message = new MboxMessage(myFolder, raf, msgnum);
      raf.close();
      return message;
    } catch (IOException e) {
      throw new MessagingException("I/O error reading mailbox", e);
    }
  }

  /**
   * Checks if the current file is or is supposed to be
   * compressed. Uses the filename to figure it out.
   */
  private boolean isGzip() {
    return file.getName().toLowerCase().endsWith(".gz");
  }

  /**
   * Creates an output stream that possibly will compress
   * whatever is sent to it, based on the current filename.
   */
  public OutputStream getOutputStream() throws IOException {
    OutputStream out;

    out = new FileOutputStream(file);
    if (isGzip())
      out = new GZIPOutputStream(out);
    return out;
  }

  /**
   * Creates an input stream that possibly will decompress the
   * file contents.
   */
  public InputStream getInputStream() throws IOException {
    InputStream in;

    in = new FileInputStream(file);
    if (isGzip()) {
      in = new GZIPInputStream(in);
    }
    return in;
  }

  /**
   * A help method that converts the collection to a message array.
   *
   * @param source the source collection (array list usually), which is assumed
   *               to contain Message objects.
   * @return the list of messages contained in the collection.
   */
  private Message[] collectionToMessageArray(Collection<Message> source) {
    return source.toArray(new Message[source.size()]);
  }
}
