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

package org.mulgara.query.rdf;


// Java 2 standard packages
import java.io.Serializable;
import java.net.URI;

// JRDF
import org.jrdf.graph.*;

// Local packages
import org.mulgara.query.Value;

/**
 * RDF resource node.
 *
 * @created 2001-08-13
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class URIReferenceImpl extends AbstractURIReference
    implements Comparable<Node>, Serializable, Value {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = -3088866814215539356L;

  /**
   * Create an RDF resource
   *
   * @param uri the absolute URI of the resource
   * @throws IllegalArgumentException if <var>uri</var> is not absolute
   */
  public URIReferenceImpl(URI uri) {

    //validate the URI
    this(uri, true);
  }

  /**
   * Create an RDF resource
   *
   * @param uri the absolute URI of the resource
   * @param validate whether or not to validate the URI
   * @throws IllegalArgumentException if <var>uri</var> is not absolute
   */
  public URIReferenceImpl(URI uri, boolean validate) {

    super(uri, validate);
  }

  /**
   * METHOD TO DO
   *
   * @param object PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public int compareTo(Node object) {

    if (object instanceof BlankNode) {
      return 1;
    }

    if (object instanceof Literal) {
      return -1;
    }
    else if (object instanceof URIReference) {
      return getURI().compareTo(((URIReference) object).getURI());
    }
    else {
      throw new ClassCastException("Not an RDF node");
    }
  }
}
