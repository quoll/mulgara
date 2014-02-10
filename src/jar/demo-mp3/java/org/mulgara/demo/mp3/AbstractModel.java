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

package org.mulgara.demo.mp3;

// Java 2 standard packages

// Logging
import org.apache.log4j.Logger;

//JRDF
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Node;
import org.jrdf.graph.Literal;
import org.jrdf.util.ClosableIterator;

// Locally written packages
import java.util.*;
import org.jrdf.graph.*;
import java.net.URLDecoder;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.Answer;
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.Mulgara;

import java.net.URLEncoder;

/**
 * Base class for Graph.
 *
 * @created 2004-12-03
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:05 $
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
public abstract class AbstractModel implements Model {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(AbstractModel.class.
      getName());

  /** Used in queries */
  protected final static String MULGARA_IS = " <" + Mulgara.NAMESPACE + "is>";

  /** Newline character ('\n' on Unix) */
  protected final static String NEWLINE = System.getProperty("line.separator");

  /** Encoding used to encode/decode query nodes */
  protected final static String URL_ENCODING = "UTF-8";

  /** Used to perform queries */
  private ItqlInterpreterBean bean = null;

  /** The Resource that the Graph represents */
  private URIReference resource = null;

  /** Used to determine if the Graph has been initialized */
  private boolean initialized = false;

  /** Type of the Graph */
  private URIReference type = null;

  /**
   * init
   *
   * @param context ModelContext
   * @throws Exception
   */
  public void init(ModelContext context) throws Exception {

    if (context == null) {
      throw new IllegalArgumentException("ModelContext is null.");
    }
    bean = context.getBean();
    if (bean == null) {
      throw new IllegalArgumentException("ModelContext returned a null " +
          "ItqlInterpreterBean.");
    }
  }

  /**
   * setResource
   *
   * @param model URIReference
   */
  public void setResource(URIReference model) {
    this.resource = model;
  }

  /**
   * getResource
   *
   * @return URIReference
   */
  public URIReference getResource() {
    checkInitialized();
    return resource;
  }

  /**
   * Sets the Graph's type.
   * @param model URIReference
   */
  public void setType(URIReference type) {
    this.type = type;
  }

  /**
   * Returns the Resource URI that represents the Graph's type.
   * @return URIReference
   */
  public URIReference getType() {
    return type;
  }


  /**
   * Returns a ClosableIterator of Triples that are returned from the model
   * using the property-value pairs as constraints. Any null values are
   * replaced by the relevant variable name.
   *
   * @param properties Iterator containing PredicateNodes
   * @param values Iterator containing ObjectNodes (non-Blank)
   * @throws QueryException
   * @return ClosableIterator
   */
  public ClosableIterator<Triple> find(Iterator<PredicateNode> properties,
      Iterator<ObjectNode> values) throws QueryException {
    try {
      String query = getFindQuery(properties, values);
      return query(query);
    }
    catch (Exception exception) {
      throw new QueryException("Failed to find Triples.", exception);
    }
  }

  /**
   * Creates the Graph with the specified type, or the default Graph type if
   * 'type' null.
   *
   * @param type URIReference
   * @throws QueryException
   */
  public void create() throws QueryException {
    checkInitialized();
    try {
      String typeURI = (type == null) ? Mulgara.NAMESPACE + "Graph" :
          type.getURI().toString();
      getBean().executeUpdate("create <" + getResource().getURI() + "> " +
          "<" + typeURI + "> ;");
    }
    catch (Exception exception) {
      throw new QueryException("Failed to create: " + getResource() +
          " as type: " + type, exception);
    }
  }

  /**
   * delete
   *
   * @throws QueryException
   */
  public void delete() throws QueryException {
    checkInitialized();
    try {
      getBean().executeUpdate("drop <" + getResource().getURI() + "> ;");
    }
    catch (Exception exception) {
      throw new QueryException("Failed to drop: " + getResource(), exception);
    }
  }

  /**
   * Drops the Graph and then re-creates it.
   *
   * @throws QueryException
   */
  public void clear() throws QueryException {
    delete();
    create();
  }

  /**
   * close
   */
  public void close() {
    checkInitialized();
  }

  /**
   * hasInitialized
   *
   * @return boolean
   */
  public boolean hasInitialized() {
    return (bean != null) && (resource != null);
  }

  /**
   * Used by subclasses to obtain an ItqlInterpreterBean reference.
   * @return ItqlInterpreterBean
   */
  protected ItqlInterpreterBean getBean() {
    if (bean == null) {
      throw new IllegalStateException("ItqlInterpreterBean has not been set");
    }
    return bean;
  }

  /**
   * Returns a query in the following format:
   *
   * <p><pre>
   *    select $s $p $o
   *    from <getResource()>
   *    where $s $p $o
   *    and $s <property-1> <value-1>
   *    and $s <property-2> <value-2>
   *               ...
   *    and $s <property-n> <value-n> ;
   *  </pre> Where n is the number of statements in the shortest iterator and
   * null nodes are replaced with the corresponding variable ($p or $o).
   *
   * @param predicates Iterator containing PredicateNodes
   * @param objects Iterator containing ObjectNodes
   * @return String
   */
  protected String getFindQuery(Iterator<PredicateNode> predicates, Iterator<ObjectNode> objects) {
    StringBuffer query = new StringBuffer();
    query.append("select $s $p $o " + NEWLINE);
    query.append("from <" + getResource() + "> " + NEWLINE);
    query.append("where $s $p $o " + NEWLINE);

    //Only constrain if constraints are supplied
    if ((predicates != null)
        && (objects != null)) {
      String currentProperty = null;
      String currentValue = null;
      while (predicates.hasNext()
          && objects.hasNext()) {
        currentProperty = asString(" $p", predicates.next());
        currentValue = asString(" $o", objects.next());
        query.append("and $s" + currentProperty + currentValue + NEWLINE);
      }
    }
    query.append("order by $s $p $o ;");
    return query.toString();
  }

  /**
   * Throw an Exception if the Graph has not been initialized.
   * @throws IllegalStateException
   */
  protected void checkInitialized() throws IllegalStateException {
    if (!initialized) {
      if (hasInitialized()) {
        initialized = true;
      }
      else {
        throw new IllegalStateException("Graph has not been Initialized.");
      }
    }
  }

  /**
   * If the node is null, the var argument is returned, otherwise the value of
   * asString(Node) is returned.
   *
   * @param var String
   * @param node ObjectNode
   * @return String
   */
  protected String asString(String var, Node node) {
    if (node == null) {
      return var;
    }
    return asString(node);
  }

  /**
   * Determines if the object is a Literal or Resource and returns an iTQL
   * representation.
   *
   * <p>Throws an Exception if the ObjectNode is a BlankNode
   *
   * @param node ObjectNode
   * @return String
   */
  protected String asString(Node node) {
    if (node == null) {
      throw new IllegalArgumentException("Node is null");
    }
    //determine type and convert to String
    if (node instanceof URIReference) {
      return " <" + ((URIReference) node).getURI() + ">";
    }
    else if (node instanceof Literal) {
      return " '" + escape(((Literal) node).getLexicalForm()) + "'";
    }
    else {
      //probably a BlankNode
      throw new IllegalArgumentException("Unsupported Node type: " +
          node.getClass().getName());
    }
  }

  /**
   * Executes the query and returns the results as a ClosableIterator. Query
   * must return three columns that represent subject, predicate and object.
   *
   * @param query String
   * @throws Exception
   * @return ClosableIterator
   */
  public ClosableIterator<Triple> query(String query) throws Exception {
    try {
      checkInitialized();
      Answer answer = getBean().executeQuery(query);
      //return as ClosableIterator
      return new AnswerClosableIteratorImpl(answer);
    }
    catch (Exception exception) {
      throw new QueryException("Failed to execute Query: " + NEWLINE + query,
          exception);
    }
  }

  /**
   * Decodes the String using an URLDecoder.
   * @param url String
   * @return String
   */
  protected String decodeURL(String url) {
    try {
      return URLDecoder.decode(url, URL_ENCODING);
    }
    catch (Exception exception) {
      log.warn("URL decoding failed for: " + url);
      return url;
    }
  }

  /**
   * Encodes the String using an URLEncoder.
   * @param url String
   * @return String
   */
  protected String encodeURL(String url) {
    try {
      return URLEncoder.encode(url, URL_ENCODING);
    }
    catch (Exception exception) {
      log.warn("URL encoding failed for: " + url);
      return url;
    }
  }

  /**
   * replace all quotes ', " with \', \"
   *
   * @param literal String
   * @return String
   */
  protected String escape(String literal) {
    String escaped = literal;
    if (literal != null) {
      escaped = escaped.replaceAll("'", "\\\\'");
//      escaped = escaped.replaceAll("\"", "\\\"");
    }
    return escaped;
  }
}
