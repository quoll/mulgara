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

import javax.mail.*;
import javax.mail.search.*;

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
public interface MboxFolder
{
  /**
   * Returns the name of this folder.
   */
  public String getName();

  /**
   * Returns the full name of this folder.
   */
  public String getFullName();

  /**
   * Returns the type of this folder.
   *
   * @exception MessagingException if a messaging error occurred
   */
  public int getType() throws MessagingException;
  public boolean exists() throws MessagingException;
  public boolean hasNewMessages()	throws MessagingException;
  public void open(int mode) throws MessagingException;
  public void close(boolean expunge) throws MessagingException;
  public Message[] expunge() throws MessagingException;
  public boolean isOpen();
  public Flags getPermanentFlags();
  public int getMessageCount() throws MessagingException;
  public Message getMessage(int msgnum) throws MessagingException;
  public Message[] getMessages() throws MessagingException;
  public void appendMessages(Message[] messages) throws MessagingException;
  public void fetch(Message amessage[], FetchProfile fetchprofile)
    throws MessagingException;
  public Folder getParent() throws MessagingException;
  public Folder[] list() throws MessagingException;
  public Folder[] list(String pattern) throws MessagingException;
  public char getSeparator() throws MessagingException;
  public boolean create(int type)	throws MessagingException;
  public boolean delete(boolean recurse) throws MessagingException;
  public boolean renameTo(Folder folder) throws MessagingException;
  public Folder getFolder(String filename) throws MessagingException;
  public boolean acquireLock();
  public boolean releaseLock();
  public Message[] search(SearchTerm term)
    throws MessagingException;
  public Message[] search(SearchTerm term, Message[] msgs)
    throws MessagingException;
}
