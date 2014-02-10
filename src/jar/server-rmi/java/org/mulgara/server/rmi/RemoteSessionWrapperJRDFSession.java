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
import java.net.URI;
import java.rmi.RemoteException;

// JRDF
import org.jrdf.graph.*;

// Locally written packages
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.server.*;

/**
 * Wrapper around a {@link RemoteSession} to make it look like a {@link
 * Session}. The only real functionality this wrapper implements is to nest any
 * {@link RemoteException}s inside {@link QueryException}s.
 *
 * @author Andrew Newman
 *
 * @created 2004-10-02
 *
 * @version $Revision: 1.11 $
 *
 * @modified $Date: 2005/06/26 12:48:16 $
 *
 * @maintenanceAuthor $Author: pgearon $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2002-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class RemoteSessionWrapperJRDFSession extends RemoteSessionWrapperSession
    implements JRDFSession {

  /**
   * The wrapped {@link RemoteJRDFSession}
   */
  private RemoteJRDFSession remoteJRDFSession;

  //
  // Constructor
  //

  /**
   *
   * @param remoteSession the wrapped remote session
   * @throws IllegalArgumentException if <var>remoteSession</var> is <code>null</code>
   */
  RemoteSessionWrapperJRDFSession(RemoteJRDFSession remoteSession, URI serverURI) {
    super(remoteSession, serverURI);
    remoteJRDFSession = remoteSession;
  }

  public long getNumberOfTriples(URI uri) {
    try {
      long r = remoteJRDFSession.getNumberOfTriples(uri);
      resetRetries();
      return r;
    }
    catch (RemoteException e) {
      try {
        testRetry(e);
      }
      catch (QueryException qe) {

        // Does not handle failing on a retry.
        return -1;
      }
      return getNumberOfTriples(uri);
    }
  }

  public Answer find(URI modelURI,  SubjectNode subject,
      PredicateNode predicate, ObjectNode object) throws GraphException {
    try {
      RemoteAnswer ans = remoteJRDFSession.find(modelURI, subject, predicate,
          object);
      resetRetries();
      return new RemoteAnswerWrapperAnswer(ans);
    }
    catch (RemoteException e) {
      try {
        testRetry(e);
      }
      catch (QueryException qe) {
        throw new GraphException("Failed to retry", qe);
      }
      return find(modelURI, subject, predicate, object);
    }
  }

  public boolean contains(URI modelURI, SubjectNode subject,
      PredicateNode predicate, ObjectNode object) throws GraphException {
    try {
      boolean b = remoteJRDFSession.contains(modelURI, subject, predicate, object);
      resetRetries();
      return b;
    }
    catch (RemoteException e) {
      try {
        testRetry(e);
      }
      catch (QueryException qe) {
        throw new GraphException("Failed to retry", qe);
      }
      return contains(modelURI, subject, predicate, object);
    }
  }

}
