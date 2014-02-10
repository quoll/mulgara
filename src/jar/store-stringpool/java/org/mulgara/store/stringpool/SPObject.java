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
import java.nio.ByteBuffer;

// Locally written packages

/**
 * @created 2001-10-05
 *
 * @author <a href="http://staff.pisoftware.com/david">David Makepeace</a>
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/02/20 10:26:19 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface SPObject extends Comparable<SPObject> {

  public TypeCategory getTypeCategory();

  public ByteBuffer getData();
  public SPComparator getSPComparator();
  public String getLexicalForm();
  public String getEncodedString();
  public org.jrdf.graph.Node getRDFNode();
  public boolean compareOverride();
  public boolean isSmallest();


  /**
   * Tests if this object represents a number.
   * @return <code>true</code> if this object is a number.
   */
  public boolean isNumber();


  /**
   * Compare objects numerically. If one object is not a number, then use the standard comparison.
   * @param o The object to compare to.
   * @return -1 if this object is smaller, +1 if larger, 0 if equal.
   */
  public int numericalCompare(SPObject o);


  public static class TypeCategory {

    // Indicates an unused G2N record.
    public final static int TCID_FREE = 0;
    // An RDF Resource URI
    public final static int TCID_URI = 1;
    // An untyped RDF literal
    public final static int TCID_UNTYPED_LITERAL = 2;
    // An RDF literal with a type URI
    public final static int TCID_TYPED_LITERAL = 3;
    // An RDF literal with a language code
    //public final static int TCID_LANG_LITERAL = 4;

    public final static TypeCategory URI = new TypeCategory(
        "URI", TCID_URI
    );
    public final static TypeCategory UNTYPED_LITERAL = new TypeCategory(
        "Untyped literal", TCID_UNTYPED_LITERAL
    );
    public final static TypeCategory TYPED_LITERAL = new TypeCategory(
        "Typed literal", TCID_TYPED_LITERAL
    );
    //public final static TypeCategory LANG_LITERAL = new TypeCategory(
    //    "Literal with language code", TCID_LANG_LITERAL
    //);

    private final String name;
    public final int ID;


    private TypeCategory(String name, int id) {
      this.name = name;
      this.ID = id;
    }

    public static TypeCategory forId(int id) {
      switch (id) {
      case TCID_URI:              return URI;
      case TCID_UNTYPED_LITERAL:  return UNTYPED_LITERAL;
      case TCID_TYPED_LITERAL:    return TYPED_LITERAL;
      //case TCID_LANG_LITERAL:     return LANG_LITERAL;
      default: throw new IllegalArgumentException("Bad TypeCategory id: " + id);
      }
    }

    public String toString() {
      return name;
    }

  }

}
