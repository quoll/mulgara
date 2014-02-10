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
import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.*;
import org.mulgara.util.ObjectUtil;

/*
 * MboxMessage.java
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
 */

/**
 * The message class implementing the Mbox mail protocol.
 * <P/>
 *
 * This code is derived from the 'knife' mail and user news client
 * at http://www.dog.net.uk/knife.
 * <P/>
 *
 * @created 2001-8-21
 *
 * @author dog@dog.net.uk
 * @author Andrew Newman
 * @author Ben Warren
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
public class MboxMessage extends MimeMessage {

  /** The category to log to. */
  private static final Logger logger = Logger.getLogger(MboxMessage.class);

  /**
   * The offset of the start of this message from the beginning of the file.
   */
  protected long startOffset = -1;

  /**
   * The offset of the start of this message's content from the beginning of the file.
   */
  protected long contentOffset = -1;

  protected MboxMessage(MboxFolderImpl folder, BufferedInputStream in,
                        int msgnum) throws MessagingException {
    super(folder, msgnum);
    headers = new InternetHeaders(in);
    try {
      int fetchsize = MboxStoreImpl.fetchsize;
      byte bytes[];
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      bytes = new byte[fetchsize];
      int len;
      while ((len = in.read(bytes, 0, fetchsize)) != -1) {
        out.write(bytes, 0, len);
      }
      bytes = out.toByteArray();
      content = bytes;
    } catch (IOException e) {
      throw new MessagingException("I/O error", e);
    }
    readStatusHeader();
  }

  /**
   * Creates a Mbox message.
   * This is called by the MboxStore.
   */
  protected MboxMessage(MboxFolderImpl folder, InputStream in, int msgnum) throws
      MessagingException {
    super(folder, msgnum);
    if (!(in instanceof ByteArrayInputStream) &&
        !(in instanceof BufferedInputStream)) {
      in = new BufferedInputStream(in);
    }
    headers = new InternetHeaders(in);
    try {
      int fetchsize = MboxStoreImpl.fetchsize;
      byte bytes[];
      if (in instanceof ByteArrayInputStream) {
        fetchsize = in.available();
        bytes = new byte[fetchsize];
        in.read(bytes, 0, fetchsize);
      } else {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bytes = new byte[fetchsize];
        int len;
        while ((len = in.read(bytes, 0, fetchsize)) != -1)
          out.write(bytes, 0, len);
        bytes = out.toByteArray();
      }
      content = bytes;
    } catch (IOException e) {
      throw new MessagingException("I/O error", e);
    }
    readStatusHeader();
  }

  /**
   * Creates a Mbox message.
   * This is called by the MboxStore.
   */
  protected MboxMessage(MboxFolderImpl folder, RandomAccessFile file,
                        int msgnum) throws MessagingException {
    super(folder, msgnum);
    // just create the headers for now
    headers = new InternetHeaders();
    try {
      startOffset = file.getFilePointer();
      String line;
      while ((line = file.readLine()) != null) {

        if (logger.isDebugEnabled()) {

          logger.debug("Checking header for MBox Message: " + line);
        }

        int len = line.length();
        if (len == 0 || (len == 1 && line.charAt(0) == '\r'))
          break;

        headers.addHeaderLine(line);
      }
/*
      // Retrieve the Content-Type header from the message
      String contentType = headers.getHeader("Content-Type", null);

      if (logger.isDebugEnabled()) {

        logger.debug("Content-Type is: " + contentType);
      }

      if (contentType.trim().endsWith(";")) {

        // If the content type ends with a semi-colon then remove it
        headers.setHeader("Content-Type",
                          contentType.trim().substring(0,
            contentType.length() - 1));

        if (logger.isDebugEnabled()) {

          logger.debug("Changed Content-Type to: " +
                       headers.getHeader("Content-Type", null));
        }

      }*/

      contentOffset = file.getFilePointer();
    } catch (IOException e) {
      throw new MessagingException("I/O error", e);
    }
    readStatusHeader();
  }

  /**
   * Creates a Mbox message.
   * This is called by the MboxFolder when appending.
   * It creates a copy of the specified message for the new folder.
   */
  @SuppressWarnings("unchecked")
  protected MboxMessage(MboxFolderImpl folder, MimeMessage message, int msgnum) throws
      MessagingException {
    super(folder, msgnum);
    headers = new InternetHeaders();
    for (Enumeration<String> enumeration = (Enumeration<String>)message.getAllHeaderLines(); enumeration.hasMoreElements();) {
      headers.addHeaderLine((String) enumeration.nextElement());
    }
    try {
      InputStream in = message.getInputStream();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] bytes = new byte[1024];
      for (int len = in.read(bytes); len > -1; len = in.read(bytes))
        out.write(bytes, 0, len);
      content = out.toByteArray();
    } catch (IOException e) {
      throw new MessagingException("I/O error", e);
    }
    readStatusHeader();
  }

  /**
   * Returns the content of this message as a Java object.
   *
   * @throws MessagingException if retrieve content fails.
   * @throws IOException if the file read fails.
   */
  public Object getContent() throws MessagingException, IOException {
    if (content == null) {
      retrieveContent();
    }

    return super.getContent();
  }

  /**
   * Returns the content of this message as a byte stream.
   *
   * @throws MessagingException if the retrieve content fails.
   */
  public InputStream getContentStream() throws MessagingException {
    if (content == null) {
      try {
        retrieveContent();
      } catch (IOException e) {
        throw new MessagingException("I/O error", e);
      }
    }
    return super.getContentStream();
  }

  /**
   * Returns the content of this message as a decoded stream.
   */
  public InputStream getInputStream() throws MessagingException, IOException {
    if (content == null)
      retrieveContent();
    return super.getInputStream();
  }

  /**
   * Returns the number of lines in the content of this message.
   */
  public int getLineCount() throws MessagingException {
    if (content == null) {
      try {
        retrieveContent();
      } catch (IOException e) {
        throw new MessagingException("I/O error", e);
      }
    }
    return super.getLineCount();
  }

  protected void retrieveContent() throws IOException {
    if (contentOffset < 0 || content != null) {
      return;
    }
    RandomAccessFile file =
        new RandomAccessFile(((MboxFolderImpl) folder).getFile(), "r");
    file.seek(contentOffset);
    String line;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (line = file.readLine(); line != null; line = file.readLine()) {
      int fromIndex = line.toLowerCase().indexOf("from ");
      if (fromIndex == 0) { // line begins with From_, end of message
        content = out.toByteArray();
        break;
      } else {
        // strip quoting if necessary
        if (fromIndex > 0) {
          String prefix = line.substring(0, fromIndex);
          boolean quoted = true;
          for (int i = 0; i < prefix.length(); i++)
            if (prefix.charAt(i) != '>') {
              quoted = false;
              break;
            }
          if (quoted) {
            String suffix = line.substring(fromIndex);
            line = prefix.substring(1) + suffix;
          }
        }
        if (line.endsWith("\r"))
          line = line.substring(0, line.length() - 1);
        out.write(line.getBytes());
        out.write('\n');
      }
    }
    // KEITH - close the reader!
    file.close();

    if (line == null) // end of file
      content = out.toByteArray();
  }

  /**
   * Returns the from address.
   */
  public Address[] getFrom() throws MessagingException {
    Address[] a = getAddressHeader("From");
    if (a == null)
      a = getAddressHeader("Sender");
    return a;
  }

  /**
   * Returns the recipients' addresses.
   */
  public Address[] getRecipients(RecipientType type) throws MessagingException {
    if (type == RecipientType.NEWSGROUPS) {
      String key = getHeader("Newsgroups", ",");
      if (key == null)
        return null;
      return NewsAddress.parse(key);
    } else {
      return getAddressHeader(getHeaderKey(type));
    }
  }

  /**
   * Returns the reply-to address.
   */
  public Address[] getReplyTo() throws MessagingException {
    Address[] a = getAddressHeader("Reply-To");
    if (a == null)
      a = getFrom();
    return a;
  }

  /**
   * Returns an array of addresses for the specified header key.
   */
  protected Address[] getAddressHeader(String key) throws MessagingException {
    String header = getHeader(key, ",");
    if (header == null)
      return null;
    try {
      return InternetAddress.parse(header);
    } catch (AddressException e) {
      String message = e.getMessage();
      if (message != null && message.indexOf("@domain") > -1) {
        try {
          return parseAddress(header, "localhost");
        } catch (AddressException e2) {
          throw new MessagingException("Invalid address: " + header, e);
        }
      }
      throw e;
    }
  }

  /**
   * Makes a pass at parsing internet addresses.
   */
  protected Address[] parseAddress(String in, String defhost) throws
      AddressException {
    //Vector v = new Vector();
    ArrayList<Address> v = new ArrayList<Address>();
    for (StringTokenizer st = new StringTokenizer(in, ","); st.hasMoreTokens(); ) {
      String s = st.nextToken().trim();
      try {
        //v.addElement(new InternetAddress(s));
        v.add(new InternetAddress(s));
      } catch (AddressException e) {
        int index = s.indexOf('>');
        if (index > -1) { // name <address>
          StringBuffer buffer = new StringBuffer();
          buffer.append(s.substring(0, index));
          buffer.append('@');
          buffer.append(defhost);
          buffer.append(s.substring(index));
          //v.addElement(new InternetAddress(buffer.toString()));
          v.add(new InternetAddress(buffer.toString()));
        } else {
          index = s.indexOf(" (");
          if (index > -1) {
            // address (name)
            StringBuffer buffer = new StringBuffer();
            buffer.append(s.substring(0, index));
            buffer.append('@');
            buffer.append(defhost);
            buffer.append(s.substring(index));
            //v.addElement(new InternetAddress(buffer.toString()));
            v.add(new InternetAddress(buffer.toString()));
          } else { // address
            //v.addElement(new InternetAddress(s+"@"+defhost));
            v.add(new InternetAddress(s + "@" + defhost));
          }
        }

      }
    }
    //Address[] a = new Address[v.size()];
    //v.copyInto(a);
    return (Address[]) v.toArray();
  }

  /**
   * Returns the header key for the specified RecipientType.
   */
  protected String getHeaderKey(RecipientType type) throws MessagingException {
    if (type == RecipientType.TO) {
      return "To";
    }
    if (type == RecipientType.CC) {
      return "Cc";
    }
    if (type == RecipientType.BCC) {
      return "Bcc";
    }
    if (type == RecipientType.NEWSGROUPS) {
      return "Newsgroups";
    }
    throw new MessagingException("Invalid recipient type: " + type);
  }

  // -- Need to override these since we are read-only --

  /**
   * Mbox messages are read-only.
   */
  public void setFrom(Address address) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void addFrom(Address aaddress[]) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setRecipients(javax.mail.Message.RecipientType recipienttype,
                            Address aaddress[]) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void addRecipients(javax.mail.Message.RecipientType recipienttype,
                            Address aaddress[]) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setReplyTo(Address aaddress[]) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setSubject(String s, String s1) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setSentDate(Date date) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setDisposition(String s) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setContentID(String s) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setContentMD5(String s) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setDescription(String s, String s1) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Mbox messages are read-only.
   */
  public void setDataHandler(DataHandler datahandler) throws MessagingException {
    throw new IllegalWriteException("MboxMessage is read-only");
  }

  /**
   * Ok, Mbox messages aren't entirely read-only.
   */
  public synchronized void setFlags(Flags flag, boolean set) throws
      MessagingException {
    if (set) {
      flags.add(flag);
    } else {
      flags.remove(flag);
    }
    updateStatusHeader();
  }

  /**
   * Updates the status header from the current flags.
   */
  private void updateStatusHeader() throws MessagingException {
    if (!flags.contains(Flags.Flag.SEEN))
      this.setHeader("Status", "O");
    else
      this.setHeader("Status", "RO");
  }

  /**
   * Updates the status header from the current flags.
   */
  private void readStatusHeader() throws MessagingException {
    String[] currentStatus = this.getHeader("Status");
    if (currentStatus != null && currentStatus.length > 0) {
      if (currentStatus[0].indexOf('R') >= 0)
        flags.add(Flags.Flag.SEEN);
      if (currentStatus[0].indexOf('O') < 0)
        flags.add(Flags.Flag.RECENT);
    }

    updateStatusHeader();
  }

  // -- Utility methods --

  /**
   * Checks to see whether the passed object is the same type and that the
   * values are all equivalent.  This has been improved on the previous
   * version which only checked that the folder and the message number were
   * the same.  Checks flags, folder, from, linecount, messageID, messageNumber,
   * receivedDate, Receipients (to, cc, bcc), Reply To, Sent Date, Size and
   * Subject.
   *
   * @param other the object to check for equality with the object.
   */
  public boolean equals(Object other) {
    boolean isEqual = false;
    if (other instanceof MboxMessage) {
      MboxMessage message = (MboxMessage) other;

      try {
        isEqual = (
            flagsEqual(message.getFlags(), getFlags()) &&
            foldersEqual(message.getFolder(), getFolder()) &&
            addressesEqual(message.getFrom(), getFrom()) &&
            message.getLineCount() == getLineCount() &&
            messageIdsEqual(message.getMessageID(), getMessageID()) &&
            message.getMessageNumber() == getMessageNumber() &&
            datesEqual(message.getReceivedDate(), getReceivedDate()) &&
            addressesEqual(message.getRecipients(Message.RecipientType.TO),
                           getRecipients(Message.RecipientType.TO)) &&
            addressesEqual(message.getRecipients(Message.RecipientType.CC),
                           getRecipients(Message.RecipientType.CC)) &&
            addressesEqual(message.getRecipients(Message.RecipientType.BCC),
                           getRecipients(Message.RecipientType.BCC)) &&
            addressesEqual(message.getReplyTo(), getReplyTo()) &&
            datesEqual(message.getSentDate(), getSentDate()) &&
            message.getSize() == getSize() &&
            subjectsEqual(message.getSubject(), getSubject()));
      } catch (javax.mail.MessagingException me) {
        logger.warn("Exception comparing messages", me);
      }
    }
    return isEqual;
  }

  /**
   * Test whether two flags are equal.  This is true if they are both are null
   * or .equals.
   *
   * @param flag1 the first flag to test against.
   * @param flag2 the second flag to test against.
   * @return the flags are equal.
   */
  public static boolean flagsEqual(Flags flag1, Flags flag2) {
    return ObjectUtil.eq(flag1, flag2);
  }

  /**
   * Test whether two folders are equal.  This is true if they are both are null
   * or .equals.
   *
   * @param folder1 the first folder to test against.
   * @param folder2 the second folder to test against.
   * @return the folders are equal.
   */
  public static boolean foldersEqual(Folder folder1, Folder folder2) {
    return ObjectUtil.eq(folder1, folder2);
  }

  /**
   * Test whether two addresses are equal.  This is true if Array.equals is
   * true.
   *
   * @param address1 the first address to test against.
   * @param address2 the second address to test against.
   * @return the addresses are equal.
   */
  public static boolean addressesEqual(Address[] address1, Address[] address2) {
    return (((address1 == null) && (address2 == null)) ||
            (Arrays.equals(address1, address2))
            );
  }

  /**
   * Test whether two message ids are equal.  This is true if they are both are
   * null or .equals.
   *
   * @param messageId1 the first message to test against.
   * @param messageId2 the second message to test against.
   * @return the messages are equal.
   */
  public static boolean messageIdsEqual(String messageId1, String messageId2) {
    return ObjectUtil.eq(messageId1, messageId2);
  }

  /**
   * Test whether two dates are equal.  This is true if they are both are null
   * or .equals.
   *
   * @param date1 the first date to test against.
   * @param date2 the second date to test against.
   * @return the dates are equal.
   */
  public static boolean datesEqual(Date date1, Date date2) {
    return ObjectUtil.eq(date1, date2);
  }

  /**
   * Test whether two subjects are equal.  This is true if they are both are
   * null or .equals.
   *
   * @param subject1 the first flag to test against.
   * @param subject2 the second flag to test against.
   * @return the subjects are equal.
   */
  public static boolean subjectsEqual(String subject1, String subject2) {
    return ObjectUtil.eq(subject1, subject2);
  }
}
