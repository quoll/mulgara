/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.connection;

import java.net.URI;
import java.net.URISyntaxException;

import org.mulgara.jena.GraphMulgara;
import org.mulgara.query.QueryException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Implements operations expected by a Jena client.
 *
 * @created Aug 11, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class JenaConnectionImpl implements JenaConnection {

  /** The internal connection to do the real work */
  private final SessionConnection internalConnection;


  public JenaConnectionImpl(SessionConnection connection) {
    internalConnection = connection;
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.  
   * Does not create the remote model.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Model connectModel(String graphURI) {
    return connectModel(graphURI, false);
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model,
   * creating the model if it does not already exist.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Model createModel(String graphURI) {
    return connectModel(graphURI, true);
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  public Model connectModel(String graphURI, boolean createIfDoesNotExist) {
    try {
      return connectModel(new URI(graphURI), createIfDoesNotExist);
    } catch (URISyntaxException ex) {
      throw new JenaException("JenaMulgara.createModel", ex);
    }
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.
   * @param graphURI The URI of the Mulgara model.
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  public Model connectModel(URI graphURI, boolean createIfDoesNotExist) {
    Graph g = connectGraph(graphURI, createIfDoesNotExist);
    return ModelFactory.createModelForGraph(g);
  }


  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model. Does not create the remote model.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Graph connectGraph(String graphURI) {
    return connectGraph(graphURI, false);
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.  
   * Creates the remote graph if it does not already exist.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Graph createGraph(String graphURI) {
    return connectGraph(graphURI, true);
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Graph.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  public Graph connectGraph(String graphURI, boolean createIfDoesNotExist) {
    try {
      return connectGraph(new URI(graphURI), createIfDoesNotExist);
    } catch (URISyntaxException ex) {
      throw new JenaException("JenaMulgara.connectGraph", ex);
    }
  }

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Graph.
   * @param graphURI The URI of the Mulgara model.
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Graph
   */
  public Graph connectGraph(URI graphURI, boolean createIfDoesNotExist) {
    if (createIfDoesNotExist) {
      try {
        if (!internalConnection.session.modelExists(graphURI)) internalConnection.session.createModel(graphURI, null);
      } catch (QueryException ex) {
        throw new JenaException(ex);
      }
    }
    return new GraphMulgara(internalConnection.session, graphURI) ;
  }


  /**
   * Drop the Mulgara graph/model.
   * @param graphURI The URI of the graph
   */
  public void dropGraph(String graphURI) {
    try {
      dropGraph(new URI(graphURI)) ;
    } catch (URISyntaxException ex) {
      throw new JenaException("JenaMulgara.dropGraph", ex);
    }
  }


  /**
   * Drop the Mulgara graph/model.
   * @param graphURI The URI of the graph
   */
  public void dropGraph(URI graphURI) {
    try {
      internalConnection.session.removeModel(graphURI);
    } catch (Exception ex) {
      throw new JenaException("JenaMulgara.dropGraph", ex);
    }
  }

}
