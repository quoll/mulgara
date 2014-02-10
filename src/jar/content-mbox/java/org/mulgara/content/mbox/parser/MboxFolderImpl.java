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
import java.net.*;
import java.text.*;
import java.util.*;
import javax.mail.*;
import javax.mail.event.*;
import javax.mail.internet.*;
import javax.mail.search.*;

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
 * Contributor(s): Daniel Thor Kristjan <danielk@cat.nyu.edu> close and expunge clarification.
 *                 Sverre Huseby <sverrehu@online.no> gzipped mailboxes
 */

/**
 * The folder class implementing a UNIX mbox-format mailbox.
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
public class MboxFolderImpl	extends Folder implements MboxFolder
{

  /** The category to log to. */
  private static final Logger logger = Logger.getLogger(MboxFolderImpl.class);

  static final DateFormat df = new SimpleDateFormat("EEE MMM d H:m:s yyyy");
  public Flags permanentFlags = null;

  private MboxReader reader;
  private ArrayList<Message> messages;
  private boolean open = false;
  private boolean readOnly = true;
  private int type = HOLDS_MESSAGES;
  private boolean inbox = false;
  private long fileLastModified = 0;

  /**
   * Constructor.
   */
  protected MboxFolderImpl(Store store, String filename, boolean isInbox)
  {
    super(store);
    reader = new MboxReader(this, filename);
    if (reader.getFile().exists() && reader.getFile().isDirectory())
    {
      type = HOLDS_FOLDERS;
    }
    inbox = isInbox;
  }

  /**
   * Constructor.
   */
  protected MboxFolderImpl(Store store, String filename)
  {
    this(store, filename, false);
  }

  /**
   * Return the file used to access/store the mail box.
   *
   * @return the file used to access/store the mail box.
   */
  public File getFile()
  {
    return reader.getFile();
  }

  /**
   * Return the name of the mail box.
   *
   * @return the name of the mail box.
   */
  public String getName()
  {
    if (!inbox)
    {
      return getFile().getName();
    }
    else
    {
      return ("INBOX");
    }
  }

  /**
   * Return the full path of the mail box.
   *
   * @return the full path of the mail box.
   */
  public String getFullName()
  {
    if (!inbox)
    {
      return getFile().getAbsolutePath();
    }
    else
    {
      return ("INBOX");
    }
  }

  /**
   * Return the mail folder type (HOLDS_MESSAGES).
   *
   * @return the mail folder type (HOLDS_MESSAGES).
   */
  public int getType() throws MessagingException
  {
    return type;
  }

  /**
   * Indicates whether this folder exists.
   *
   * @exception MessagingException if a messaging error occurred
   */
  public boolean exists()	throws MessagingException
  {
    return getFile().exists();
  }

  /**
   * Indicates whether this folder contains new messages.
   *
   * @exception MessagingException if a messaging error occurred
   */
  public boolean hasNewMessages() throws MessagingException
  {
    return getNewMessageCount() > 0;
  }

  /**s
   * Opens this folder.
   *
   * @exception MessagingException if a messaging error occurred
   */
  public void open(int mode) throws MessagingException
  {
    switch (mode)
    {
      case READ_WRITE:
      {
        if (!reader.canWrite())
        {
          throw new MessagingException("Folder is read-only");
        }
        readOnly = false;
        break;
      }
      case READ_ONLY:
      {
        //Call the index method which will index the file if there hasn't
        //been one created.
        reader.indexMbox();
      }
    }
    if (!reader.canRead())
    {
      throw new MessagingException("Can't read folder: " +
        getFile().getAbsolutePath());
    }

    if (getFile().length() == 0) {
      // empty file
      //log.info("Mail file is empty!" + getFile().getAbsolutePath());
    } else {
      if (!reader.isMboxFormat()) {

        throw new MessagingException("Mailbox format error",
            new ProtocolException());
      }
    }

    open = true;
    notifyConnectionListeners(ConnectionEvent.OPENED);
  }

  /**
   * Closes this folder.
   * @param expunge if the folder is to be expunged before it is closed
   * @exception MessagingException if a messaging error occurred
   */
  public void close(boolean expunge)
    throws MessagingException
  {
    if (open)
    {
      if (expunge)
      {
        expunge();
      }
      open = false;
      notifyConnectionListeners(ConnectionEvent.CLOSED);
      if (!readOnly) {
        synchronizeMessages();
        saveMessages();
      }
    }
    if (((MboxStore)store).getSession().getDebug())
      logger.debug("mbox: closing "+getFile().getAbsolutePath());
  }

  /**
   * Expunges this folder.
   * This deletes all the messages marked as deleted.
   * @exception MessagingException if a messaging error occurred
   */
  public synchronized Message[] expunge() throws MessagingException {

    ArrayList<Message> ve = new ArrayList<Message>();
    if (open && this.messages != null) {
      ArrayList<Message> vm = new ArrayList<Message>();
      Iterator<Message> iter = messages.iterator();
      while (iter.hasNext()) {

        Message message = iter.next();
        Flags flags = message.getFlags();
        if (flags.contains(Flags.Flag.DELETED)) {
          ve.add(message);
        } else {
          vm.add(message);
        }
      }
      messages = vm;
    }

    Message[] expunged = collectionToMessageArray(ve);
    if (expunged.length > 0) {
      notifyMessageRemovedListeners(true, expunged);
    }
    return expunged;
  }

  /**
   * Indicates whether this folder is open.
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Returns the permanent flags for this folder.
   */
  public Flags getPermanentFlags() {
    if (permanentFlags == null) {
      Flags tmpFlags = new Flags();
      tmpFlags.add(Flags.Flag.DELETED);
      tmpFlags.add(Flags.Flag.SEEN);
      tmpFlags.add(Flags.Flag.RECENT);
      permanentFlags = tmpFlags;
    }

    return permanentFlags;
  }

  /**
   * Returns the number of messages in this folder.  Calls the getMessageCount.
   *
   * @return the number of messages in this folder.
   * @exception MessagingException if a messaging error occurred
   */
  public int getMessageCount() throws MessagingException {
    return reader.getMessageCount();
  }

  /**
   * Returns the specified message number from this folder.
   *
   * @exception MessagingException if a messaging error occurred
   */
  public Message getMessage(int msgnum) throws MessagingException {

    if (logger.isDebugEnabled()) {

      logger.debug("Retrieving message number " + msgnum);
    }

    // Use a reader to get that getFile().
    try {

      return reader.getMessage(msgnum - 1);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new MessagingException("No such message", e);
    }
  }

  /**
   * Returns the messages in this folder.
   *
   * @exception MessagingException if a messaging error occurred
   */
  public synchronized Message[] getMessages()	throws MessagingException {
    this.synchronizeMessages();
    this.saveMessages();

    return this.collectionToMessageArray(messages);
  }

  /**
   * Appends messages to this folder.
   */
  public synchronized void appendMessages(Message[] messages) throws MessagingException {
    synchronizeMessages();

    ArrayList<Message> added = new ArrayList<Message>();
    for (int i = 0; i < messages.length; i++) {
      if (messages[i] instanceof MimeMessage) {
        MboxMessage message = new MboxMessage(this, (MimeMessage)messages[i], i);
        added.add(message);
        this.messages.add(message);
      }
    }
    if (added.size() > 0) {
      Message[] n = this.collectionToMessageArray(added);
      notifyMessageAddedListeners(n);
    }
    saveMessages();
  }

  /**
   * Does nothing.
   * The messages <i>must</i> be fetched in their entirety by getMessages() -
   * this is the nature of the Mbox protocol.
   *
   * @exception MessagingException ignore
   */
  public void fetch(Message amessage[], FetchProfile fetchprofile) throws MessagingException {
  }

  /**
   * Returns the parent folder.
   *
   * @return the parent folder.
   */
  public Folder getParent() throws MessagingException {
    return store.getFolder(getFile().getParent());
  }

  /**
   * Returns the subfolders of this folder.
   *
   * @return the subfolders of this folder.
   */
  public Folder[] list() throws MessagingException {
    if (type != HOLDS_FOLDERS) {
      throw new MessagingException("This folder can't contain subfolders");
    }

    try
    {
      String[] files = getFile().list();
      Folder[] folders = new Folder[files.length];
      for (int i = 0; i < files.length; i++) {
        folders[i] = store.getFolder(getFile().getAbsolutePath() + File.separator + files[i]);
      }
      return folders;
    } catch (SecurityException e) {
      throw new MessagingException("Access denied", e);
    }
  }

  /**
   * Returns the subfolders of this folder matching the specified pattern.
   */
  public Folder[] list(String pattern) throws MessagingException {
    if (type!=HOLDS_FOLDERS) {
      throw new MessagingException("This folder can't contain subfolders");
    }
    try {
      String[] files = getFile().list(new MboxFilenameFilter(pattern));
      Folder[] folders = new Folder[files.length];
      for (int i=0; i<files.length; i++) {
        folders[i] = store.getFolder(getFile().getAbsolutePath() + File.separator + files[i]);
      }
      return folders;

    } catch (SecurityException e) {
      throw new MessagingException("Access denied", e);
    }
  }

  /**
   * Returns the separator character.
   *
   * @return the separator character.
   */
  public char getSeparator() throws MessagingException {
    return File.separatorChar;
  }

  /**
   * Creates this folder in the store.
   */
  public boolean create(int type) throws MessagingException {

    if (getFile().exists()) throw new MessagingException("Folder already exists");
    switch (type) {

    case HOLDS_FOLDERS:
      try {
        if (getFile().mkdirs()) {
        this.type = type;
        notifyFolderListeners(FolderEvent.CREATED);
        return true;
        }
        throw new MessagingException("Could not create folder: " + getFile());
      } catch (SecurityException e) {
        throw new MessagingException("Access denied", e);
      }
      case HOLDS_MESSAGES:
      try {
        // save the changes
        synchronized (this) {
          if (messages == null) messages = new ArrayList<Message>();
          OutputStream os = new BufferedOutputStream(getOutputStream());
          try {
            Message[] m = this.collectionToMessageArray(this.messages);
            for (int i = 0; i < m.length; i++) {
              Address[] f = m[i].getFrom();
              String top = "From " + ((f.length>0) ? f[0].toString() : "-") + " "+
              df.format(m[i].getSentDate()) + "\n";
              os.write(top.getBytes());
              m[i].writeTo(os);
            }
          } finally {
            os.close();
          }
        }
        this.type = type;
        notifyFolderListeners(FolderEvent.CREATED);
        return true;
      } catch (IOException e) {
        throw new MessagingException("I/O error writing mailbox", e);
      } catch (SecurityException e) {
        throw new MessagingException("Access denied", e);
      }
    }
    return false;
  }

  /**
   * Deletes this folder.
   */
  public boolean delete(boolean recurse) throws MessagingException {
    if (recurse) {
      try {
        if (type == HOLDS_FOLDERS) {
          Folder[] folders = list();
          for (int i = 0; i < folders.length; i++) {
            if (!folders[i].delete(recurse)) return false;
          }
        }
        getFile().delete();
        notifyFolderListeners(FolderEvent.DELETED);
        return true;
      } catch (SecurityException e) {
        throw new MessagingException("Access denied", e);
      }
    } else {
      try {
        if (type == HOLDS_FOLDERS) {
          Folder[] folders = list();
          if (folders.length > 0) return false;
        }
        getFile().delete();
        notifyFolderListeners(FolderEvent.DELETED);
        return true;

      } catch (SecurityException e) {
        throw new MessagingException("Access denied", e);
      }
    }
  }

  /**
   * Mbox folders cannot be created, deleted, or renamed.
   */
  public boolean renameTo(Folder folder) throws MessagingException {
    try {
      String filename = folder.getFullName();
      if (filename != null) {
        getFile().renameTo(new File(filename));
        ((MboxStoreImpl)store).folders.clear();
        notifyFolderListeners(FolderEvent.RENAMED);
        return true;
      } else {
        throw new MessagingException("Illegal filename: null");
      }
    } catch (SecurityException e) {
      throw new MessagingException("Access denied", e);
    }
  }

  /**
   * Mbox folders cannot contain subfolders.
   */
  public Folder getFolder(String filename) throws MessagingException {
    return store.getFolder(getFile().getAbsolutePath() + File.separator + filename);
  }

  public Message[] search(SearchTerm term) throws MessagingException {
    return super.search(term);
  }

  public Message[] search(SearchTerm term, Message[] messages) throws MessagingException {
    return super.search(term, messages);
  }

  /**
   * Locks this mailbox.  Not implementented yet.
   */
  public synchronized boolean acquireLock() {
    return true;
  }

  /**
   * Unlocks this mailbox.  Not implemented yet.
   */
  public synchronized boolean releaseLock() {
    return true;
  }

  // Reads messages from the disk getFile().
  private ArrayList<Message> readMessages() throws MessagingException {
    synchronized (this) {
      return reader.getMessagesAsArrayList(0, reader.getMessageCount() - 1);
    }
  }

  /**
   * Synchronizes the source file with the current message list.
   */
  private void synchronizeMessages() throws MessagingException {
    // Modified this to speed it up.
    if (getFile().lastModified() != fileLastModified) {
      fileLastModified = getFile().lastModified();

      // we should never be in the position where we've removed messages
      // that haven't been removed from the file itself.  at least, let's
      // hope so.  :)
      //
      // it should also be the case that messages are only appended to
      // the file, so if we find a message that doesn't correspond to a
      // current message, then it should be both a new message, and have
      // no old messages after it.
      //
      // FIXME:  these are both really, really, really bad assumptions.
      //

      ArrayList<Message> tmpMessages = readMessages();

      if (messages == null) {
        messages = tmpMessages;
      } else {
        ArrayList<Message> messagesAdded = new ArrayList<Message>();
        ArrayList<Message> finalMessages = new ArrayList<Message>();
        int j = -1;
        MboxMessage tmpMessage;

        for (int index =0; index < tmpMessages.size(); index++) {

          tmpMessage = (MboxMessage) tmpMessages.get(index);
          String tmpUniqueId = tmpMessage.getMessageID();
          String uniqueId = null;

          while (uniqueId != tmpUniqueId &&
                 (!tmpUniqueId.equals(uniqueId)) && j < messages.size() - 1) {
            uniqueId = ((MboxMessage)messages.get(++j)).getMessageID();
          }

          if (j < messages.size() -1) {
            finalMessages.add(messages.get(j));
          } else {
            Message newMessage = tmpMessages.get(index);
            finalMessages.add(newMessage);
            messagesAdded.add(newMessage);
          }

        }

        messages = finalMessages;
        if (messagesAdded.size() > 0) {
          Message[] n = collectionToMessageArray(messagesAdded);
          notifyMessageAddedListeners(n);
          saveMessages();
        }
      }
    }
  }

  // Saves messages to the disk getFile().
  private void saveMessages() throws MessagingException {
    if (readOnly) {
      return;
    }
    synchronized (this) {
      if (messages != null) {
        try {
          Message[] m = this.collectionToMessageArray(messages);

          // make sure content has been retrieved for all messages
          for (int i = 0; i < m.length; i++) {
            if (m[i] instanceof MboxMessage) {
              ((MboxMessage)m[i]).retrieveContent();
            }
          }

          OutputStream os = new BufferedOutputStream(getOutputStream());
          try {
            MboxOutputStream mos = new MboxOutputStream(os);

            for (int i = 0; i < m.length; i++) {
              Address[] f = m[i].getFrom();
              String from = "-";
              if (f.length > 0) {
                if (f[0] instanceof InternetAddress) {
                  from = ((InternetAddress)f[0]).getAddress();
                } else {
                  from = f[0].toString();
                }
              }
              Date date = m[i].getSentDate();
              if (date == null) date = m[i].getReceivedDate();
              if (date == null) date = new Date();

              String top = "From " + from + " " + df.format(date) + "\n";
              os.write(top.getBytes());
              m[i].writeTo(mos);
              mos.flush();
            }
          } finally {
            os.close();
          }
          fileLastModified = getFile().lastModified();
        } catch (IOException e) {
          throw new MessagingException("I/O error writing mailbox", e);
        }
      }
    }
  }

  /**
   * Creates an output stream that possibly will compress
   * whatever is sent to it, based on the current filename.
   */
  private OutputStream getOutputStream() throws IOException {
    return reader.getOutputStream();
  }

  /**
   * A helper method that converts the collection to a message array.
   *
   * @param source the source collection (array list usually), which is assumed
   *               to contain Message objects.
   * @return the list of messages contained in the collection.
   */
  private Message[] collectionToMessageArray(Collection<Message> source) {
    return source.toArray(new Message[source.size()]);
  }

  /**
   * A filter to return a list of files based on patters on * and %.
   */
  class MboxFilenameFilter implements FilenameFilter {

     String pattern;
     int asteriskIndex, percentIndex;

     MboxFilenameFilter(String pattern) {
       this.pattern = pattern;
       asteriskIndex = pattern.indexOf('*');
       percentIndex = pattern.indexOf('%');
     }

     public boolean accept(File directory, String name) {

       if (asteriskIndex > -1) {
         String start = pattern.substring(0, asteriskIndex),
                end = pattern.substring(asteriskIndex+1, pattern.length());
         return (name.startsWith(start) && name.endsWith(end));

       } else if (percentIndex > -1) {

         String start = pattern.substring(0, percentIndex),
                end = pattern.substring(percentIndex+1, pattern.length());
         return (directory.equals(getFile()) && name.startsWith(start) && name.endsWith(end));
       }

       return name.equals(pattern);
     }
  }
}
