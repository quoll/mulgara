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

package org.mulgara.store.stringpool;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.LiteralImpl;


/**
 *
 * @created 2004-08-05
 *
 * @author David Makepeace
 *
 * @version $Revision$
 *
 * @modified $Date$
 *
 * @maintenanceAuthor $Author$
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class AbstractSPTypedLiteral extends AbstractSPObject
      implements SPTypedLiteral {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(AbstractSPTypedLiteral.class);

  protected int typeId;
  protected int subtypeId;
  protected URI typeURI;


  /**
   * @param typeURI  the absolute URI of the datatype; if <code>null</code>,
   *   the subclass constructor is responsible for setting its value
   * @throws IllegalArgumentException if <var>typeURI</var> is relative
   */
  protected AbstractSPTypedLiteral(int typeId, int subtypeId, URI typeURI) {

    // Validate "typeURI" parameter
    if (typeURI != null && !typeURI.isAbsolute()) {
      throw new IllegalArgumentException(
        "Datayype URI " + typeURI + " is not absolute"
      );
    }

    this.typeId = typeId;
    this.subtypeId = subtypeId;
    this.typeURI = typeURI;
  }


  protected AbstractSPTypedLiteral(int typeId, URI typeURI) {
    this(typeId, 0, typeURI);
  }


  public TypeCategory getTypeCategory() {
    return TypeCategory.TYPED_LITERAL;
  }


  public String getEncodedString() {
    StringBuffer sb = new StringBuffer(getLexicalForm());
    escapeString(sb);
    sb.insert(0, '"');
    return sb.append("\"^^<").append(typeURI).append('>').toString();
  }


  public org.jrdf.graph.Node getRDFNode() {
    if (typeURI == null) {
      return new LiteralImpl(getLexicalForm(), "");
    }
    else {
      return new LiteralImpl(getLexicalForm(), typeURI);
    }
  }


  /* from SPTypedLiteral interface. */

  public int getTypeId() {
    return typeId;
  }

  public int getSubtypeId() {
    return subtypeId;
  }

  public URI getTypeURI() {
    return typeURI;
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare type categories.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the type URIs.
    return typeURI.compareTo(((AbstractSPTypedLiteral)o).typeURI);
  }

}
