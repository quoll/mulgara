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

package org.mulgara.server.driver;

// Java 2 standard packages
import java.net.URI;
import java.lang.reflect.Constructor;

// Third party packages
import org.jrdf.graph.*;

// Local packages
import org.mulgara.server.*;

/**
 * A Factory for creating client and server-side JRDF Graphs.
 *
 * @created 2004-11-30
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/13 12:05:15 $
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
public abstract class JRDFGraphFactory {

  /** Server-side Graph class */
  private static final String SERVER_GRAPH_CLASS = "org.mulgara.jrdf.JRDFGraph";

  /** Client-side Graph proxy (used to create the Graph) */
  private static final String CLIENT_PROXY_CLASS =
      "org.mulgara.client.jrdf.itql.ItqlGraphProxy";

  /** Client-side Graph class  */
  private static final String CLIENT_GRAPH_CLASS =
      "org.mulgara.client.jrdf.ClientGraph";

  /** Argument types for the Client-side Graph  */
  private static final String [] CLIENT_GRAPH_ARG_TYPES = new String [] {
      "org.mulgara.client.jrdf.RemoteGraphProxy"
  };

  /**
   * Creates a new Server JRDF Graph (local) representing an existing model.
   *
   * @param serverURI URI
   * @param modelURI URI
   * @throws GraphException
   * @return Graph
   */
  public static Graph newServerGraph(URI serverURI, URI modelURI) throws GraphException {
    try {
      Graph graph = null;
      JRDFSession session = newServerJRDFSession(serverURI);
      graph = newServerGraph(session, modelURI);
      return graph;
    } catch (Exception exception) {
      throw new GraphException("Failed to create Server JRDF Graph.", exception);
    }
  }

  /**
   * Obtains a session from the SessionFactory and creates a Server JRDF Graph
   * (local) from it.
   *
   * @param database SessionFactory
   * @param modelURI URI
   * @throws GraphException
   * @return Graph
   */
  public static Graph newServerGraph(SessionFactory database, URI modelURI) throws GraphException {
    try {
      Graph graph = null;
      JRDFSession session = (JRDFSession) database.newJRDFSession();
      graph = newServerGraph(session, modelURI);
      return graph;
    } catch (Exception exception) {
      throw new GraphException("Failed to create Server JRDF Graph.", exception);
    }
  }

  /**
   * Creates a Server JRDF Graph (local) using the session to represent an
   * existing model.
   *
   * @param session JRDFSession
   * @param modelURI URI
   * @throws GraphException
   * @return Graph
   */
  public static Graph newServerGraph(JRDFSession session, URI modelURI) throws GraphException {
    try {
      Graph graph = null;
      //find server graph class and instantiate
      Class [] argTypes = new Class[] {
        JRDFSession.class,
        URI.class
      };
      Object [] args = new Object [] {
        session,
        modelURI
      };
      graph = (Graph) instantiateClass(SERVER_GRAPH_CLASS, argTypes, args);
      return graph;
    } catch (Exception exception) {
      throw new GraphException("Failed to create Server JRDF Graph.", exception);
    }
  }

  /**
   * Creates a new Client JRDF Graph (remote) representing an existing model.
   *
   * @param serverURI URI
   * @param modelURI URI
   * @throws GraphException
   * @return Graph
   */
  public static Graph newClientGraph(URI serverURI,
      URI modelURI) throws GraphException {
    try {
      Graph graph = null;
      JRDFSession session = newClientJRDFSession(serverURI);
      graph = newClientGraph(session, modelURI);
      return graph;
    } catch (Exception exception) {
      throw new GraphException("Failed to create Client JRDF Graph.", exception);
    }
  }

  /**
   * Creates a new Client JRDF Graph (remote) using the session to represent
   * an existing model.
   *
   * @param session JRDFSession
   * @param modelURI URI
   * @throws GraphException
   * @return Graph
   */
  public static Graph newClientGraph(JRDFSession session,
      URI modelURI) throws GraphException {
    try {
      Graph graph = null;
      //find client graph class (via it's proxy) and instantiate
      Class [] argTypes = getClassTypes(CLIENT_GRAPH_ARG_TYPES);
      Object [] args = new Object [] {
          getClientProxy(CLIENT_PROXY_CLASS, session, modelURI)
      };
      graph = (Graph) instantiateClass(CLIENT_GRAPH_CLASS, argTypes, args);
      return graph;
    } catch (Exception exception) {
      throw new GraphException("Failed to create Client JRDF Graph.", exception);
    }
  }

  /**
   * Obtains a new JRDFSession (remote) for the client-side use.
   *
   * @param serverURI URI
   * @throws GraphException
   * @return JRDFSession
   */
  public static JRDFSession newClientJRDFSession(URI serverURI) throws GraphException {
    try {
      JRDFSession session = null;
      SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(
          serverURI, true);
      session = (JRDFSession) sessionFactory.newJRDFSession();
      return session;
    }
    catch (Exception exception) {
      throw new GraphException("Failed to obtain Client JRDF Session.",
          exception);
    }
  }

  /**
   * Obtains a new JRDFSession (local) for server-side use.
   *
   * @param serverURI URI
   * @throws GraphException
   * @return JRDFSession
   */
  public static JRDFSession newServerJRDFSession(URI serverURI) throws GraphException {
    try {
      JRDFSession session = null;
      SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(
          serverURI, false);
      session = (JRDFSession) sessionFactory.newJRDFSession();
      return session;
    }
    catch (Exception exception) {
      throw new GraphException("Failed to obtain Server JRDF Session.",
          exception);
    }
  }

  /**
   * Instantiates the Class using the provided constructor argument via
   * reflection.
   *
   * @param className String
   * @param argTypes Class[]
   * @param args Object[]
   * @throws Exception
   * @return Object
   */
  private static Object instantiateClass(String className, Class[] argTypes,
      Object[] args) throws Exception {

    //find constructor and instantiate
    Class graphClass = Class.forName(className);
    Constructor constructor = graphClass.getConstructor(argTypes);
    return constructor.newInstance(args);
  }

  /**
   * Loads each class and add's it to the array.
   *
   * @param classNames String[]
   * @throws Exception
   * @return Class[]
   */
  private static Class [] getClassTypes(String [] classNames) throws Exception {

    //validate
    if (classNames == null) {
      throw new IllegalArgumentException("'classNames' is null.");
    }
    Class [] types = new Class [classNames.length];
    for (int i = 0; i < types.length; i++) {
      types[i] = Class.forName(classNames[i]);
    }
    return types;
  }

  /**
   * Instantiates the proxy class using the session and model URI as constructors.
   *
   * @param proxyClass String
   * @param session JRDFSession
   * @param modelURI URI
   * @throws Exception
   * @return Object
   */
  private static Object getClientProxy(String proxyClass, JRDFSession session,
      URI modelURI) throws Exception {

    //find server graph class and instantiate
    Class [] argTypes = new Class[] {
      JRDFSession.class,
      URI.class
    };
    Object [] args = new Object [] {
      session,
      modelURI
    };
    return instantiateClass(proxyClass, argTypes, args);
  }

}
