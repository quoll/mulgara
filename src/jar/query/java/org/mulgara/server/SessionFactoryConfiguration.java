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

package org.mulgara.server;

// Java 2 Standard Packages
import java.net.*;
import java.io.*;
import java.util.*;

// Locally written packages
import org.mulgara.server.SessionFactory;

/**
 * Stores configuration properties used to instantiate a
 * {@link SessionFactory}.
 *
 * @created 2004-09-06
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:21 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class SessionFactoryConfiguration {

  /** The class name of the {@link SessionFactory} implementation. */
  private String className = null;

  /** Location of the server being created (default is local hostname) */
  private String serverURI = null;

  /**
   * Directory that the {@link SessionFactory} can write files to
   * (if applicable).
   */
  private String directory = null;

  /**
   * Default Constructor.
   *
   */
  public SessionFactoryConfiguration () {

    super();
  }

  /**
   * Sets the implementation of SessionFactory to be returned
   *
   * @param className String
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * Sets the hostname of the server
   *
   * @param serverURI String
   */
  public void setServerURI(String serverURI) {
    this.serverURI = serverURI;
  }

  /**
   * Sets the directory used to store session files (if applicable)
   *
   * @param directory String
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Returns the name of the Session Factory implementation to be used.
   *
   * @return String
   */
  public String getClassName() {

    return this.className;
  }

  /**
   * Returns the types of the arguments used to instantiate a SessionFactory.
   * (class name not included)
   *
   * @throws SessionFactoryException
   * @return Class[]
   */
  public Class<?>[] getConfigurationTypes() throws SessionFactoryException {

    try {

      List<Class<?>> classes = new ArrayList<Class<?>>();

      //serverURI
      if ( (this.serverURI != null)
          && (!"".equals(this.serverURI))) {

        URI uri = new URI(this.serverURI);

        classes.add(uri.getClass());
      }

      //directory
      if ( (this.directory != null)
          && (!"".equals(this.directory))) {

        File file = new File(this.directory);

        classes.add(file.getClass());
      }

      //return as an array
      return (Class[]) classes.toArray(new Class[classes.size()]);
    } catch (URISyntaxException uriException) {

      throw new SessionFactoryException("Could not get configuration types.",
                                        uriException);
    }
  }

  /**
   * Returns the arguments used to instantiate a SessionFactory.
   * (class name not included)
   *
   * @throws SessionFactoryException
   * @return Object[]
   */
  public Object[] getConfigurationObjects() throws SessionFactoryException {

    try {

      List<Object> objects = new ArrayList<Object>();

      //serverURI
      if ( (this.serverURI != null)
          && (!"".equals(this.serverURI))) {

        URI uri = new URI(this.serverURI);

        objects.add(uri);
      }

      //directory
      if ( (this.directory != null)
          && (!"".equals(this.directory))) {

        File file = new File(this.directory);

        objects.add(file);
      }

      //return as an array
      return (Object[]) objects.toArray(new Object[objects.size()]);

    } catch (URISyntaxException uriException) {

      throw new SessionFactoryException("Could not get configuration types.",
                                        uriException);
    }
  }
}
