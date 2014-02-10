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
 * The Initial Developer of the Original Code is Andrew Newman. Portions
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
import java.net.URI;
import java.rmi.RemoteException;

// Log4j
import org.apache.log4j.Logger;

// Jena packages
import com.hp.hpl.jena.graph.Node_Variable;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.server.*;

/**
 * Wrapper around a {@link JenaSession} to make it look like a
 * {@link RemoteJenaSession}.
 *
 * @author Andrew Newman
 *
 * @created 2005-01-08
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/01/27 11:22:39 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy; 2005 Andrew Newman
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 *
 * @see <a href="http://developer.java.sun.com/developer/JDCTechTips/2001/tt0327.html#jndi"/>
 *      <cite>JNDI lookup in distributed systems</cite> </a>
 */
class JenaSessionWrapperRemoteJenaSession extends SessionWrapperRemoteSession
    implements RemoteJenaSession {

  /** Logger.  */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(JenaSessionWrapperRemoteJenaSession.class.getName());

  /**
   * The wrapped {@link JenaSession}
   */
  private final JenaSession jenaSession;

  //
  // Constructor
  //

  /**
   * @param session {@inheritDoc}
   * @throws IllegalArgumentException {@inheritDoc}
   */
  JenaSessionWrapperRemoteJenaSession(JenaSession session) {
    super(session);

    // Initialize fields
    this.jenaSession = session;
  }

  public void insert(URI modelURI,
      com.hp.hpl.jena.graph.Node subject, com.hp.hpl.jena.graph.Node predicate,
      com.hp.hpl.jena.graph.Node object) throws QueryException, RemoteException {
    jenaSession.insert(modelURI, subject, predicate, object);
  }

  public void delete(URI modelURI, com.hp.hpl.jena.graph.Node subject,
      com.hp.hpl.jena.graph.Node predicate, com.hp.hpl.jena.graph.Node object)
      throws QueryException, RemoteException {
    jenaSession.delete(modelURI, subject, predicate, object);
  }

  public long getNumberOfStatements(URI modelURI)
      throws QueryException, RemoteException {
    return jenaSession.getNumberOfStatements(modelURI);
  }

  public JenaFactory getJenaFactory() throws RemoteException {
    return jenaSession.getJenaFactory();
  }

  public com.hp.hpl.jena.util.iterator.ClosableIterator findUniqueValues(
    URI modelURI, Node_Variable column)
    throws QueryException, RemoteException {
    com.hp.hpl.jena.util.iterator.ClosableIterator iter =
        jenaSession.findUniqueValues(modelURI, column);
    return iter;
  }

  public void insert(URI modelURI, com.hp.hpl.jena.graph.Triple[] triples)
      throws QueryException, RemoteException {
    jenaSession.insert(modelURI, triples);
  }

  public void delete(URI modelURI, com.hp.hpl.jena.graph.Triple[] triples)
      throws QueryException, RemoteException {
    jenaSession.delete(modelURI, triples);
  }

  public boolean contains(URI modelURI, com.hp.hpl.jena.graph.Node subject,
    com.hp.hpl.jena.graph.Node predicate, com.hp.hpl.jena.graph.Node object)
    throws QueryException, RemoteException {

    boolean contains = jenaSession.contains(modelURI, subject, predicate,
        object);
    return contains;
  }

  public com.hp.hpl.jena.util.iterator.ClosableIterator find(URI modelURI,
      com.hp.hpl.jena.graph.Node subject, com.hp.hpl.jena.graph.Node predicate,
      com.hp.hpl.jena.graph.Node object)
      throws QueryException, RemoteException {

    com.hp.hpl.jena.util.iterator.ClosableIterator iter =
        jenaSession.find(modelURI, subject, predicate, object);
    return iter;
  }
}
