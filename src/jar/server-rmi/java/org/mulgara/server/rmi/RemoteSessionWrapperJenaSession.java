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

// Jena packages
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node_Variable;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.server.*;

/**
 * Wrapper around a {@link RemoteJenaSession} to make it look like a {@link
 * Session}. The only real functionality this wrapper implements is to nest any
 * {@link RemoteException}s inside {@link QueryException}s.
 *
 * @author Andrew Newman
 *
 * @created 2005-01-08
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/06/26 12:48:16 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @copyright &copy; 2005 Andrew Newman
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class RemoteSessionWrapperJenaSession extends RemoteSessionWrapperSession
    implements JenaSession {

  /**
   * The wrapped {@link RemoteJenaSession}
   */
  private RemoteJenaSession remoteJenaSession;

  //
  // Constructor
  //

  /**
   *
   * @param remoteSession the wrapped remote session
   * @throws IllegalArgumentException if <var>remoteSession</var> is <code>null</code>
   */
  RemoteSessionWrapperJenaSession(RemoteJenaSession remoteSession, URI serverURI) {
    super(remoteSession, serverURI);
    remoteJenaSession = remoteSession;
  }

  public void insert(URI modelURI,
      com.hp.hpl.jena.graph.Node subject, com.hp.hpl.jena.graph.Node predicate,
      com.hp.hpl.jena.graph.Node object) throws QueryException {
    try {
      remoteJenaSession.insert(modelURI, subject, predicate, object);
      resetRetries();
    }
    catch (RemoteException e) {
      testRetry(e);
      insert(modelURI, subject, predicate, object);
    }
  }

  public void delete(URI modelURI, com.hp.hpl.jena.graph.Node subject,
      com.hp.hpl.jena.graph.Node predicate, com.hp.hpl.jena.graph.Node object)
      throws QueryException {
    try {
      remoteJenaSession.delete(modelURI, subject, predicate, object);
      resetRetries();
    }
    catch (RemoteException e) {
      testRetry(e);
      delete(modelURI, subject, predicate, object);
    }
  }

  public long getNumberOfStatements(URI modelURI)
      throws QueryException {
    try {
      long r = remoteJenaSession.getNumberOfStatements(modelURI);
      resetRetries();
      return r;
    }
    catch (RemoteException e) {
      testRetry(e);
      return getNumberOfStatements(modelURI);
    }
  }

  public JenaFactory getJenaFactory() {
    try {
      JenaFactory factory = remoteJenaSession.getJenaFactory();
      resetRetries();
      return factory;
    }
    catch (RemoteException e) {
      try {
        testRetry(e);
      }
      catch (QueryException qe) {

        // Does not handle query exception
        return null;
      }
      return getJenaFactory();
    }
  }

  public com.hp.hpl.jena.util.iterator.ClosableIterator findUniqueValues(
      URI modelURI, Node_Variable column)
      throws QueryException {
    try {
      com.hp.hpl.jena.util.iterator.ClosableIterator iter =
          remoteJenaSession.findUniqueValues(modelURI, column);
      resetRetries();
      return iter;
    }
    catch (RemoteException e) {
      testRetry(e);
      return findUniqueValues(modelURI, column);
    }
  }

  public void insert(URI modelURI, com.hp.hpl.jena.graph.Triple[] triples)
      throws QueryException {
    try {
      remoteJenaSession.insert(modelURI, triples);
      resetRetries();
    }
    catch (RemoteException e) {
      testRetry(e);
      insert(modelURI, triples);
    }
  }

  public void delete(URI modelURI, com.hp.hpl.jena.graph.Triple[] triples)
      throws QueryException {
    try {
      remoteJenaSession.delete(modelURI, triples);
      resetRetries();
    }
    catch (RemoteException e) {
      testRetry(e);
      delete(modelURI, triples);
    }
  }

  public boolean contains(URI modelURI, com.hp.hpl.jena.graph.Node subject,
      com.hp.hpl.jena.graph.Node predicate, com.hp.hpl.jena.graph.Node object)
      throws QueryException {
    try {
      boolean contains = remoteJenaSession.contains(modelURI, subject,
          predicate, object);
      resetRetries();
      return contains;
    }
    catch (RemoteException e) {
      testRetry(e);
      return contains(modelURI, subject, predicate, object);
    }
  }

  public com.hp.hpl.jena.util.iterator.ClosableIterator find(URI modelURI,
      com.hp.hpl.jena.graph.Node subject, com.hp.hpl.jena.graph.Node predicate,
      com.hp.hpl.jena.graph.Node object)
      throws QueryException {
    try {
      com.hp.hpl.jena.util.iterator.ClosableIterator iter =
          remoteJenaSession.find(modelURI, subject, predicate,
          object);
      resetRetries();
      return iter;
    }
    catch (RemoteException e) {
      testRetry(e);
      return find(modelURI, subject, predicate, object);
    }
  }

}
