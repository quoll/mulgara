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

import org.apache.log4j.Logger;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.URIReference;
import org.mulgara.query.QueryException;

// Local packages


/**
 * Statement that can be written to a PrintWriter. Statement is in the form:
 * SubjectNode, URIReference, URIReference.
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
public class URIReferenceWritableStatement extends AbstractWritableStatement {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(
      URIReferenceWritableStatement.class.getName());

  /** URIReference being writen */
  private URIReference uriReference = null;

  /**
   * Constructor.
   *
   * @param subject SubjectNode
   * @param predicate URIReference
   * @param uriReference URIReference
   */
  public URIReferenceWritableStatement(SubjectNode subject, URIReference predicate, URIReference uriReference) {

    super(subject, predicate);

    //validate
    if (uriReference == null) {

      throw new IllegalArgumentException("URIReference is null");
    }
    this.uriReference = uriReference;
  }

  /**
   * Writes the URIReference statement to the writer in the foramt:
   * <predicateURI rdf:resource="resourceURI"/>
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
    if (writer == null) {

      throw new IllegalArgumentException("PrintWriter is null");
    }

    assert uriReference != null : "URIReference (object) should not be null";
    assert uriReference.getURI() != null : "URIReference should have an URI";

    //get the namespace for the URI
    String uriString = uriReference.getURI().toString();

    //write as:  <predicateURI rdf:resource="resourceURI"/>
    writer.println("    <" + getPredicateString(namespaces) + " " +
                   namespaces.getRDFPrefix() + ":resource=\"" +
                   uriString + "\"/>");
  }

}
