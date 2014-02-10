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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Describes operations expected by a Jena client.
 *
 * @created Jun 22, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public interface JenaConnection {

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.  
   * Does not create the remote model.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Model connectModel(String graphURI);

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model,
   * creating the model if it does not already exist.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Model createModel(String graphURI);

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  public Model connectModel(String graphURI, boolean createIfDoesNotExist);

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.
   * @param graphURI The URI of the Mulgara model.
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  public Model connectModel(URI graphURI, boolean createIfDoesNotExist);


  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model. Does not create the remote model.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Graph connectGraph(String graphURI);

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Model.  
   * Creates the remote graph if it does not already exist.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @return A Jena Model
   */
  public Graph createGraph(String graphURI);

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Graph.
   * @param graphURI The URI,as a string, of the Mulgara model in the server 
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Model
   */
  public Graph connectGraph(String graphURI, boolean createIfDoesNotExist);

  /**
   * Connect to RDF data stored in a Mulgara server as a Jena Graph.
   * @param graphURI The URI of the Mulgara model.
   * @param createIfDoesNotExist Create the Mulgara model if it does not already exist.
   * @return A Jena Graph
   */
  public Graph connectGraph(URI graphURI, boolean createIfDoesNotExist);


  /**
   * Drop the Mulgara graph/model.
   * @param graphURI The URI of the graph
   */
  public void dropGraph(String graphURI);

  /**
   * Drop the Mulgara graph/model.
   * @param graphURI The URI of the graph
   */
  public void dropGraph(URI graphURI);

}
