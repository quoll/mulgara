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
package org.mulgara.store.stringpool.xa;

// Java 2 standard packages
import java.nio.ByteBuffer;
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.store.stringpool.*;


/**
 * An SPObject that represents an RDF resource.
 *
 * @created 2002-03-07
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPURIImpl extends AbstractSPObject implements SPURI {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPURIImpl.class);

  private URI uri;


  SPURIImpl(URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("Null \"uri\" parameter");
    }

    this.uri = uri;
  }


  SPURIImpl(ByteBuffer data) {
    this(CHARSET.decode(data).toString());
  }


  private SPURIImpl(String str) {
    this(URI.create(str));
  }


  static SPObject newSPObject(String uriStr) {
    return new SPURIImpl(uriStr);
  }


  public String getLexicalForm() {
    return uri.toString();
  }


  /* from SPURI interface. */

  public URI getURI() {
    return uri;
  }


  /* from SPObject interface. */

  public TypeCategory getTypeCategory() {
    return TypeCategory.URI;
  }


  public ByteBuffer getData() {
    return CHARSET.encode(uri.toString());
  }


  public SPComparator getSPComparator() {
    return SPCaseSensitiveStringComparator.getInstance();
  }


  public String getEncodedString() {
    return '<' + uri.toASCIIString() + '>';
  }


  public org.jrdf.graph.Node getRDFNode() {
    return new URIReferenceImpl(uri);
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the Strings.
    return uri.compareTo(((SPURIImpl)o).uri);
  }


  /* from Object. */

  public int hashCode() {
    return uri.hashCode();
  }


  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      return uri.equals(((SPURIImpl)obj).uri);
    } catch (ClassCastException ex) {
      // obj was not an SPURIImpl.
      return false;
    }
  }

}
