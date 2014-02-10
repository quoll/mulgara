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
package org.mulgara.content.rdfxml.writer;

// Java 2 standard packages
import java.io.PrintWriter;
import java.net.URI;

import org.apache.log4j.Logger;
import org.jrdf.graph.Literal;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.URIReference;
import org.mulgara.query.QueryException;
import org.mulgara.util.StringUtil;


/**
 * Statement that can be written to a PrintWriter. Statement is in the form:
 * SubjectNode, URIReference, Literal.
 *
 * @created 2004-02-20
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:03 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LiteralWritableStatement extends AbstractWritableStatement {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(LiteralWritableStatement.class.getName());

  /** Literal being writen */
  private Literal literal = null;

  /**
   * Constructor.
   *
   * @param subject SubjectNode
   * @param predicate URIReference
   * @param literal Literal
   */
  public LiteralWritableStatement(SubjectNode subject, URIReference predicate, Literal literal) {

    super (subject, predicate);

    //validate
    if (literal == null) {

      throw new IllegalArgumentException("Literal is null");
    }
    this.literal = literal;
  }

  /**
   * Writes the Literal statement to the writer.
   *
   * <p>If there is no datatype or language:
   * <predicateURI>"Literal value"</predicateURI>
   *
   * <p>With a datatype:
   * <predicateURI rdf:datatype="datatype">"Literal value"</predicateURI>
   *
   * <p>With a language:
   * <predicateURI xml:lang="language">"Literal value"</predicateURI>
   *
   * @param namespaces NamespaceMap
   * @param writer PrintWriter
   * @throws QueryException
   */
  public void write(NamespaceMap namespaces, PrintWriter writer) throws QueryException {

    //validate
    if (namespaces == null) {

      throw new IllegalArgumentException("NamespaceMap is null");
    }
    if (writer  == null) {

      throw new IllegalArgumentException("PrintWriter is null");
    }

    //determine if the Literal has a datatype or language
    URI datatype = literal.getDatatypeURI();
    String lang = literal.getLanguage();

    // Get the lexical form of the literal and encode
    String literalObject = literal.getLexicalForm();
    StringBuffer buffer = new StringBuffer();
    StringUtil.quoteAV(literalObject, buffer);

    //get the predicate
    String predicateString = getPredicateString(namespaces);

    if (datatype != null) {

      //write as:  <predicateURI rdf:datatype="datatype">"Literal value"
      //           </predicateURI>
      writer.println("    <" + predicateString + " " + namespaces.getRDFPrefix() +
                     ":datatype=\"" + datatype + "\">" +
                     buffer.toString() +
                     "</" + predicateString + ">");
    }
    else if (lang != null) {

      //write as:  <predicateURI xml:lang="language">"Literal value"
      //           </predicateURI>
      writer.println("    <" + predicateString + " " + "xml:lang=\"" + lang +
                     "\">" + buffer.toString() + "</" + predicateString + ">");
    }
    else {

      //write as:  <predicateURI>"Literal value"</predicateURI>
      writer.println("    <" + predicateString + ">" + buffer.toString() +
                     "</" + predicateString + ">");
    }
  }

}
