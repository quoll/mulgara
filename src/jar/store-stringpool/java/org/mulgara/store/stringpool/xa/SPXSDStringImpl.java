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
import java.net.URI;
import java.nio.ByteBuffer;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.store.stringpool.AbstractSPTypedLiteral;
import org.mulgara.store.stringpool.SPComparator;
import org.mulgara.store.stringpool.SPObject;


/**
 * An SPTypedLiteral that represents xsd:string literals.
 * These objects are distinct from SPStringImpl objects and always compare
 * unequal to them even when they have the same lexical form.
 *
 * @created 2002-10-05
 *
 * @author David Makepeace
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/03/12 02:53:28 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPXSDStringImpl extends AbstractSPTypedLiteral {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPXSDStringImpl.class);

  private String str;

  static final int TYPE_ID = 1; // Unique ID

  static final URI TYPE_URI = XSD.STRING_URI;


  SPXSDStringImpl(String str) {
    super(TYPE_ID, TYPE_URI);

    if (str == null) {
      throw new IllegalArgumentException("Null \"str\" parameter");
    }
    this.str = str;
  }


  SPXSDStringImpl(ByteBuffer data) {
    this(CHARSET.decode(data).toString());
  }


  /* from SPObject interface. */

  public ByteBuffer getData() {
    return CHARSET.encode(str);
  }


  public SPComparator getSPComparator() {
    return SPCaseInsensitiveStringComparator.getInstance();
  }


  public String getLexicalForm() {
    return str;
  }


  /* from Comparable interface. */

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the Strings.
    return str.compareToIgnoreCase(((SPXSDStringImpl)o).str);
  }


  /* from Object. */

  public int hashCode() {
    return str.hashCode();
  }


  public boolean equals(Object obj) {
    // Check for null.
    if (obj == null) return false;

    try {
      return str.equals(((SPXSDStringImpl)obj).str);
    } catch (ClassCastException ex) {
      // obj was not an SPXSDStringImpl.
      return false;
    }
  }

}
