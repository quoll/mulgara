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

package org.mulgara.demo.mp3.id3;

// Java 2 standard packages

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.URIReference;
import org.jrdf.util.ClosableIterator;

// Local Packages
import org.jrdf.graph.Triple;
import java.net.URI;
import org.mulgara.demo.mp3.Mp3Context;
import org.mulgara.demo.mp3.Mp3File;
import org.mulgara.demo.mp3.Mp3Model;
import org.mulgara.demo.mp3.SchemaModel;
import org.mulgara.itql.*;
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.jrdf.graph.*;
import java.util.*;

/**
 * Default Id3Tag base class.
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
public abstract class AbstractId3Tag implements Id3Tag {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(AbstractId3Tag.class.
      getName());

  /** Used to perform queries */
  private ItqlInterpreterBean bean = null;

  /** Graph containing the Mp3 instance data */
  private Mp3Model model = null;

  /** Graph containing Schema information */
  private SchemaModel schemaModel = null;

  /** Mp3File that this Tag represents. */
  private Mp3File mp3 = null;

  /**
   * Initializes the Id3Tag.
   * @param context Mp3Context
   */
  public void init(Mp3Context context) {

    if (context == null) {
      throw new IllegalArgumentException("Mp3Context is null.");
    }

    bean = context.getBean();
    model = context.getMp3Model();
    schemaModel = context.getSchemaModel();

    //validate
    if (bean == null) {
      throw new IllegalArgumentException("Mp3Context returned a null " +
          "ItqlInterpreterBean.");
    }
    if (model == null) {
      throw new IllegalArgumentException("Mp3Context returned a null Mp3Model.");
    }
    if (schemaModel == null) {
      throw new IllegalArgumentException("Mp3Context returned a null " +
          "SchemaModel.");
    }
  }

  /**
   * Returns the Resource URI for the ID3 tag's version (eg. Id3Tag.ID3V1).
   * @return URIReference never <code>null</code>
   */
  public abstract URIReference getVersion();

  /**
   * Sets the Mp3 that the Id3Tag represents
   * @param mp3 Mp3File
   */
  public void setMp3File(Mp3File mp3) {

    if (mp3 == null) {
      throw new IllegalArgumentException("Mp3File is null.");
    }
    this.mp3 = mp3;
  }

  /**
   * Returns the Mp3 that the Id3Tag represents
   * @return Mp3File
   */
  public Mp3File getMp3File() {
    return mp3;
  }

  /**
   * Returns all Id3 property-values for the Mp3 as an Iterator of Triples.
   *
   * @return ClosableIterator
   * @throws QueryException
   */
  public ClosableIterator getSchemaProperties() throws QueryException {
    //the id3 version is the RDFS domain
    return schemaModel.getDomainProperties(getVersion());
  }

  /**
   * Returns all Id3 property-values for the Mp3 as an Iterator of Triples.
   *
   * @return ClosableIterator
   * @throws QueryException
   */
  public ClosableIterator getStatements() throws QueryException {

    ClosableIterator schemaProperties = null;
    try {
      schemaProperties = getSchemaProperties();
      String query = getStatementsQuery(mp3.getResource(), schemaProperties);
      return model.query(query);
    }
    catch (Exception exception) {
      throw new QueryException("Failed to getStatements.", exception);
    }
    finally {
      if (schemaProperties != null) {
        schemaProperties.close();
      }
    }
  }

  /**
   * Returns a queryString in the following format:
   *
   * <p> <pre>
   *  select $s $p $o
   *  from <model.getResource()>
   *  where $s $p $o
   *  and $s <id3:uri> <mp3>
   *  and ($p <mulgara:is> <schemaProperty-1>
   *    or $p <mulgara:is> <schemaProperty-2>
   *              ...
   *    or $p <mulgara:is> <schemaProperty-n>) ;
   *  </pre> where <schemaProperty> is a property of the ID3Tags schema.
   *
   * @return String
   * @param mp3 URIReference
   * @param properties Iterator of Triples where the SubjectNode's represent the
   * schemaProperties.
   */
  private String getStatementsQuery(URIReference mp3, Iterator properties) {

    //if there are no properties, there should be no statements.
    if ((properties == null)
        || !(properties.hasNext())) {
      return ";";
    }

    Node currentProperty = ((Triple) properties.next()).getPredicate();
    StringBuffer query = new StringBuffer("select $s $p $o" + NEWLINE);
    query.append("from <" + model.getResource().getURI() + ">" + NEWLINE);
    query.append("where $s $p $o" + NEWLINE);

    query.append("and $s" + ID3_URI + asString(mp3) + " " + NEWLINE);
    query.append("and ( $p" + MULGARA_IS + asString((URIReference) currentProperty) +
        " " + NEWLINE);
    while (properties.hasNext()) {
      currentProperty = ((Triple) properties.next()).getSubject();
      query.append("  or $p" + MULGARA_IS + asString((URIReference) currentProperty) +
          " " + NEWLINE);
    }
    //results must be ordered
    query.append(") order by $s $p $o ; ");
    return query.toString();
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
