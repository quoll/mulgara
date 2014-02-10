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
 * Contributor(s): Andrew Newman.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.client.jrdf;

// Java 2 standard packages
import java.net.*;

// Log4J
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.*;

//Mulgara
import org.mulgara.client.jrdf.answer.*;
import org.mulgara.client.jrdf.itql.*;
import org.mulgara.query.*;
import org.mulgara.server.*;
import org.mulgara.server.driver.*;

/**
 * Abstract factory that contains static methods for creating client-side
 * JRDF Graphs.
 *
 * @created 2004-07-29
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 * @author Andrew Newman
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/26 21:01:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class AbstractGraphFactory {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(AbstractGraphFactory.class.getName());

  /**
   * Returns a ClientGraph backed by an Answer.
   *
   * @param dataSource Answer where the Graph gets it's data from
   * @param session the session that was used to get the answer.
   * @throws GraphException
   * @return ClientGraph
   */
  public static ClientGraph createGraph(Answer dataSource, Session session)
      throws GraphException {

    //validate answer
    if (dataSource == null) {
      throw new IllegalArgumentException("Cannot create Graph from a null " +
          "Answer.");
    }

    return new ClientGraph(createGraphProxy(dataSource, session));
  }

  /**
   * Returns a ClientGraph representing the model URI, backed by a newly
   * created Session.
   *
   * @param serverURI the server to connect to.
   * @param modelURI the URI of the model to use.
   * @throws GraphException
   * @return ClientGraph
   */
  public static ClientGraph createGraph(URI serverURI, URI modelURI)
      throws GraphException {

    //validate
    if (serverURI == null) {
      throw new IllegalArgumentException("Cannot create Graph from a null " +
          "Session.");
    }
    if (modelURI == null) {
      throw new IllegalArgumentException("Cannot create Graph from a null " +
          "model URI.");
    }

    // Create new session and then new graph.
    JRDFSession session = createServerSession(serverURI);
    return new ClientGraph(createGraphProxy(modelURI, session));
  }

  //------------------ RemoteGraphProxy Factory Methods ------------------------

  /*------------------------------ NOTE --------------------------------------
   *                                                                         *
   *  RemoteGraphProxy is used by ClientGraph as a Data-Access Object (DAO). *
   *  They are the Graph's data source. Therfore their implementation is     *
   *  dependant on the data-source (eg. HTTPServlet, Answer).                *
   *                                                                         *
    -------------------------------------------------------------------------*/

  /**
   * Instantiates the correct implementation of RemoteGraphProxy for an Answer.
   *
   * @param dataSource Answer
   * @param session Session
   * @throws GraphException
   * @return RemoteGraphProxy
   */
  private static RemoteGraphProxy createGraphProxy(Answer dataSource,
      Session session) throws GraphException {

    return new RemoteAnswerGraphProxy(dataSource, session);
  }

  /**
   * Instantiates the correct implementation of RemoteGraphProxy for a model
   * URI and Session.
   *
   * @param modelURI Answer
   * @param session Session
   * @throws GraphException
   * @return RemoteGraphProxy
   */
  private static RemoteGraphProxy createGraphProxy(URI modelURI,
      JRDFSession session) throws GraphException {

    return new SessionGraphProxy(session, modelURI);
  }

  /**
   * Creates a new JRDFSession.
   *
   * @param serverURI the server to connect to.
   * @throws GraphException any exceptions that occur when trying to make the
   *   connection.
   * @return JRDFSession the newly created session.
   */
  private static JRDFSession createServerSession(URI serverURI)
      throws GraphException{
    try {
      SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(
          serverURI, true);
      JRDFSession session = (JRDFSession) sessionFactory.newJRDFSession();
      return session;
    }
    catch (SessionFactoryFinderException sffe) {
      throw new GraphException("Failed to connect to " + serverURI, sffe);
    }
    catch (NonRemoteSessionException nrse) {
      throw new GraphException("Failed to create a remote session", nrse);
    }
    catch (QueryException qe) {
      throw new GraphException("Failed to get new JRDF session", qe);
    }
  }
}
