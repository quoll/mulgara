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

package org.mulgara.server.rmi;

// Java 2 standard packages
import java.rmi.RemoteException;

// Log4j
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;

/**
 * {@link RemoteSession} which generates unpaged answers.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @created 2004-03-18
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/27 11:58:45 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 *
 * @see <a href="http://developer.java.sun.com/developer/JDCTechTips/2001/tt0327.html#jndi"/>
 *      <cite>JNDI lookup in distributed systems</cite> </a>
 */
class RemoteSessionImpl extends SessionWrapperRemoteSession
    implements RemoteSession {

  /**
   * Logger. This is named after the classname.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(RemoteSessionImpl.class.getName());

  /**
   * Reference to the RemoteSessionFactory
   */
  protected RemoteSessionFactory remoteSessionFactory;

  //
  // Constructor
  //

  /**
   * @param session {@inheritDoc}
   * @throws IllegalArgumentException {@inheritDoc}
   */
  RemoteSessionImpl(Session session, RemoteSessionFactory remoteSessionFactory) {
    super(session);
    this.remoteSessionFactory = remoteSessionFactory;
  }

  /**
   * Close and de-register this object
   *
   * @throws RemoteException EXCEPTION TO DO
   */
  public void close() throws QueryException, RemoteException {

    super.close();

    // remove the instance from the factory
    remoteSessionFactory.removeSession(this);

  }
}
