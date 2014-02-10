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
import java.net.*;

// Logging
import org.apache.log4j.Logger;

// Third party packages

//JRDF
import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.ObjectNode;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.URIReferenceImpl;

/**
 * Methods for querying/managing metadata for files in the filesystem.
 *
 * @created 2004-12-03
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:06 $
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
public class FileSystemModelImpl extends AbstractModel implements
    FileSystemModel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(SchemaModelImpl.class.
      getName());

  /**
   * Adds the directory to the list of directories to search.
   *
   * @param directory URL
   * @throws QueryException
   */
  public void includeDirectory(URL directory) throws QueryException {
    if (directory == null) {
      throw new IllegalArgumentException("'directory' is null");
    }
    try {
      //insert the inclusion and delete the exclusion (if it exists)
      String includeQuery = getInsertQuery(getIncludeStatement(directory));
      String excludeQuery = getDeleteQuery(getExcludeStatement(directory));
      getBean().executeUpdate(includeQuery);
      getBean().execute(excludeQuery);
    }
    catch (Exception exception) {
      throw new QueryException("Failed to include directory: " + directory,
          exception);
    }
  }

  /**
   * Removes the directory from the list of directories to search.
   *
   * @param directory URL
   * @throws QueryException
   */
  public void excludeDirectory(URL directory) throws QueryException {
    if (directory == null) {
      throw new IllegalArgumentException("'directory' is null");
    }
    try {
      //insert the exclusion and delete the inclusion (if it exists)
      String excludeQuery = getInsertQuery(getExcludeStatement(directory));
      String includeQuery = getDeleteQuery(getIncludeStatement(directory));
      getBean().executeUpdate(includeQuery);
      getBean().execute(excludeQuery);
    }
    catch (Exception exception) {
      throw new QueryException("Failed to include directory: " + directory,
          exception);
    }
  }

  /**
   * Returns an Iterator containing all the Files (as URIReferenece's) that have
   * the specified metadata property/value.
   *
   * @param property PredicateNode metadata predicate (eg. "mulgara:extension")
   * @param value ObjectNode metadata object (eg. "mp3")
   * @return ClosableIterator
   * @throws QueryException
   */
  public ClosableIterator<Triple> findFiles(PredicateNode property,
      ObjectNode value) throws QueryException {

    if (property == null) {
      throw new IllegalArgumentException("PredicateNode is null");
    }
    if (value == null) {
      throw new IllegalArgumentException("ObjectNode is null");
    }

    try {

      String query = getFindFilesQuery((URIReference) property, value);
      return query(query);
    }
    catch (Exception exception) {
      throw new QueryException("Failed to determine domain properties.",
          exception);
    }
  }

  /**
   * Returns a query in the following format:
   * <p><pre>
   *   select $s $p $o
   *   from <getResource()>
   *   where $s $p $o
   *   and $p <mulgara:is> <property>
   *   and $o <mulgara:is> 'object' ;
   * </pre>
   *  ('object' will be replaced with <object> for resource objects)
   *
   *
   * @param predicate URIReference
   * @param object ObjectNode
   * @return String
   */
  private String getFindFilesQuery(URIReference predicate, ObjectNode object) {
    StringBuffer query = new StringBuffer();
    query.append("select $s $p $o " + NEWLINE);
    query.append("from <" + getResource() + "> " + NEWLINE);
    query.append("where $s $p $o " + NEWLINE);
    query.append("and $p " + MULGARA_IS + " <" + predicate.getURI() + "> " +
        NEWLINE);
    query.append("and $o " + MULGARA_IS + asString(object) + " ;" + NEWLINE);
    return query.toString();
  }

  /**
   * Returns a statement in the following format:
   * <p><pre>
   *   <getResource()> <mulgara:include> <directory>
   * </pre>
   *
   * @param directory URL
   * @return String
   */
  private String getIncludeStatement(URL directory) {

    return "<" + getResource().getURI() + ">" + MULGARA_INCLUDE +
        " <" + directory + "> ";
  }

  /**
   * Returns a statement in the following format:
   * <p><pre>
   *   <getResource()> <mulgara:exclude> <directory>
   * </pre>
   *
   * @param directory URL
   * @return String
   */
  private String getExcludeStatement(URL directory) {

    return "<" + getResource().getURI() + ">" + MULGARA_EXCLUDE +
        " <" + directory + "> ";
  }

  /**
   * Returns a query in the following format:
   *
   * <p><pre>
   *    insert *statement*
   *    into <getResource()> ;
   *  </pre>
   *
   * @param statement String
   * @return String
   * @throws URISyntaxException
   */
  private String getInsertQuery(String statement) throws URISyntaxException {
    StringBuffer query = new StringBuffer();
    query.append("insert " + statement + NEWLINE);
    query.append("into <" + getDefinition().getURI() + "> ;");
    return query.toString();
  }

  /**
   * Returns a query in the following format:
   *
   * <p><pre>
   *    delete *statement*
   *    from <getResource()> ;
   *  </pre>
   *
   * @param statement String
   * @return String
   * @throws URISyntaxException
   */
  private String getDeleteQuery(String statement) throws URISyntaxException {
    StringBuffer query = new StringBuffer();
    query.append("delete " + statement + NEWLINE);
    query.append("from <" + getDefinition().getURI() + "> ;");
    return query.toString();
  }

  /**
   * Returns a Resource representing the Definition Graph Resource
   *
   * @return URIReference
   * @throws URISyntaxException
   */
  private URIReference getDefinition() throws URISyntaxException {
    URIReference resource = getResource();
    URI resourceURI = resource.getURI();
    URI definitionURI = new URI(resourceURI.getScheme(),
        resourceURI.getAuthority(), resourceURI.getPath(), "def",
        resourceURI.getFragment());
    if (log.isDebugEnabled()) {
      log.debug("Returning definition: " + definitionURI + " for " +
          resourceURI);
    }
    return new URIReferenceImpl(definitionURI);
  }

}
