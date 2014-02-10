package org.mulgara.jena;

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

import java.net.URI;
import java.net.URISyntaxException;

import org.mulgara.query.QueryException;
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.server.driver.SessionFactoryFinderException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Factory for graphs and model that provide the Jena operations over Mulgara-stored RDF data.
 * Documentation: {@link http://jena.hpl.hp.com/wiki/JenaMulgara}
 * @author Andy Seaborne
 * @deprecated These methods should be accessed through {@link org.mulgara.connection.JenaConnection}
 */
public class JenaMulgara {

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.  
   * Does not create the remote model.
   * @param serverURI The server URI as a string
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  static public Model connectModel(String serverURI, String graphURI) {
    return connectModel(serverURI, graphURI, false);
  }
    
  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model,
   * creating the model if it does not already exist.
   * @param serverURI The server URI as a string
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  static public Model createModel(String serverURI, String graphURI) {
    return connectModel(serverURI, graphURI, true);
  }
    
    
  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.
   * @param serverURI The server URI as a string
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  static public Model connectModel(String serverURI, String graphURI, boolean createIfDoesNotExist) {
    try {
      return connectModel(createSession(serverURI), new URI(graphURI), createIfDoesNotExist);
    } catch (URISyntaxException ex) {
      throw new JenaException("JenaMulgara.createModel", ex);
    }
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.
   * @param session The Mulgara Session object
   * @param graphURI The URI of the Mulgara model.
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  static public Model connectModel(Session session, URI graphURI, boolean createIfDoesNotExist) {
    Graph g = connectGraph(session, graphURI, createIfDoesNotExist);
    return ModelFactory.createModelForGraph(g);
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.  Does not create the remote model.
   * @param serverURI The server URI as a string
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  static public Graph connectGraph(String serverURI, String graphURI) {
    return connectGraph(serverURI, graphURI, false);
  }
    
  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.  
   * Creates the remote graph if it does not already exist.
   * @param serverURI The server URI as a string
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  static public Graph createGraph(String serverURI, String graphURI) {
    return connectGraph(serverURI, graphURI, true);
  }
    
  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Graph.
   * @param serverURI The server URI as a string
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  static public Graph connectGraph(String serverURI, String graphURI, boolean createIfDoesNotExist) {
    try {
      return connectGraph(createSession(serverURI), new URI(graphURI), createIfDoesNotExist) ;
    } catch (URISyntaxException ex) {
      throw new JenaException("JenaMulgara.createGraph", ex);
    }
  }
    
  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Graph.
   * @param session The Mulgara Session object
   * @param graphURI The URI of the Mulgara model.
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Graph
   */
  static public Graph connectGraph(Session session, URI graphURI, boolean createIfDoesNotExist) {
    if (createIfDoesNotExist) {
      try {
        boolean definitelyExists = false ;
        definitelyExists = session.modelExists(graphURI);
        if (!definitelyExists) session.createModel(graphURI, null);
      } catch (QueryException ex) {
        throw new JenaException(ex);
      }
    }
    Graph g = new GraphMulgara(session, graphURI) ;
    return g;
  }


  /**
   * Drop the Mulgara graph/model.
   * @param serverURI The server URI
   * @param graphURI The URI of the graph
   */
  public static void dropGraph(String serverURI, String graphURI) {
    Session session = createSession(serverURI) ;
    try {
      dropGraph(session, new URI(graphURI)) ;
    } catch (URISyntaxException ex) {
      throw new JenaException("JenaMulgara.dropGraph", ex);
    }
  }

  /**
   * Drop the Mulgara graph/model.
   * @param session The session to use for dropping the graph.
   * @param graphURI The URI of the graph
   */
  public static void dropGraph(Session session, URI graphURI) {
    try {
      session.removeModel(graphURI);
    } catch (Exception ex) {
      throw new JenaException("JenaMulgara.dropGraph", ex);
    }
  }

  /**
   * Create a Mulgara session to a server, by URI
   * @param serverURI The URI of the server to get the session for.
   * @return A new session.
   */
  public static Session createSession(String serverURI) {
    try {
      // Create the URI of the server
      URI server = new URI(serverURI);
      // Create a new session factory
      SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(server, true);
      Session session = sessionFactory.newSession();
      return session;
    } catch (URISyntaxException ex) {
      throw new JenaException("JenaMulgara.createSession", ex);
    } catch (SessionFactoryFinderException ex) {
      throw new JenaException("JenaMulgara.createSession", ex);
    } catch (NonRemoteSessionException ex) {
      throw new JenaException("JenaMulgara.createSession", ex);
    } catch (QueryException ex) {
      throw new JenaException("JenaMulgara.createSession", ex);
    }
  }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
