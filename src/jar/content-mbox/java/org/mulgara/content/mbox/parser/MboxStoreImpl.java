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
import javax.mail.*;
import java.util.Hashtable;

import org.apache.log4j.*;

/*
 * MboxStore.java
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
 */

/**
 * The storage class implementing the Mbox mailbox file format.
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
public class MboxStoreImpl extends Store implements MboxStore {

  /** Logger */
  private static final Logger log = Logger.getLogger(MboxStoreImpl.class);

  static boolean attemptFallback = false;
  Hashtable<String,Folder> folders = new Hashtable<String,Folder>();
  public static final int fetchsize = 1024;

  /**
   * Constructor.
   */
  public MboxStoreImpl(Session session, URLName urlname) {
    super(session, urlname);
/**    String ccs = session.getProperty("mail.mbox.fetchsize");
    if (ccs!=null)
    {
      try
      {
        fetchsize = Math.max(Integer.parseInt(ccs), 1024);
      }
      catch (NumberFormatException e)
      {}
    }
    String af = session.getProperty("mail.mbox.attemptFallback");
    if (af!=null)
    {
      attemptFallback = Boolean.valueOf(af).booleanValue();
    }*/
  }

  /**
   * There isn't a protocol to implement, so this method just returns.
   */
  protected boolean protocolConnect(String host, int port, String username, String password)
        throws MessagingException {
    return true;
  }

  public Folder getDefaultFolder() throws MessagingException {
    if (url != null) {
      String file = url.getFile();
      if (file != null && file.length() > 0) {
        String name = File.separator+file.replace('/', File.separatorChar);

        if (log.isDebugEnabled()) {

          log.debug("Store will use folder name of " + name);
        }

        Folder folder = getFolder(name);

        return folder;
      }
    }

    try {
      String defaultDir = session.getProperty("mail.mbox.userhome");
      if (defaultDir == null) {
        defaultDir = System.getProperty("user.home");
      }

      if (log.isDebugEnabled()) {

          log.debug("Store will use default directory: " + defaultDir);
      }

      return new MboxDefaultFolder(this, defaultDir);
    } catch (SecurityException e) {
      throw new MessagingException("Access denied", e);
    }
  }

  public Folder getFolder(String filename) throws MessagingException {

    Folder folder = folders.get(filename);
    if (folder==null) {

      if ("inbox".equals(filename.toLowerCase())) {

        // First try the session property mail.mbox.inbox.
        String m = session.getProperty("mail.mbox.inbox");
        
        if (m!=null && new File(m).exists()) {
          filename = m;

        } else if (attemptFallback) { // If that fails try some common (UNIX) locations.

          try {
            m = File.separator+"var"+File.separator+"spool"+File.separator+"mail"+File.separator+System.getProperty("user.name");
            if (new File(m).exists()) {
              filename = m;
            } else {
              m = System.getProperty("user.home")+File.separator+"mbox";
              if (new File(m).exists())
                filename = m;
            }
          } catch (SecurityException e) { // not allowed to read system properties
          }
        }
      }
      folders.put(filename, folder = new MboxFolderImpl(this, filename));
    }
    return folder;
  }

  public Folder getFolder(URLName urlname) throws MessagingException {
    String fileName = File.separator+urlname.getFile().replace('/', File.separatorChar);
    return getFolder(fileName);
  }

  public Session getSession() {
    return session;
  }

  public static int getFetchSize() {
    return fetchsize;
  }
}
