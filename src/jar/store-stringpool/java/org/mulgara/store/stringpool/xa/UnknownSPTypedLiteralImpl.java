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

//Java 2 standard packages
import java.nio.ByteBuffer;
import java.net.URI;
import java.net.URISyntaxException;

//apache packages
import org.apache.log4j.*;

//mulgara packages
import org.mulgara.store.stringpool.AbstractSPTypedLiteral;
import org.mulgara.store.stringpool.SPComparator;
import org.mulgara.store.stringpool.SPObject;
import org.mulgara.store.stringpool.SPTypedLiteral;

/**
 * A class that represents a datatyped literal of some type we don't natively
 * support.
 *
 * @created 2004-12-08
 *
 * @author Simon Raboczi
 *
 * @version $Revision: 1.2 $
 *
 * @modified $Date: 2005/03/12 02:53:28 $ by $Author: newmana $
 *
 * @company <a href="http://www.tucanatech.com/">Tucana Technologies</a>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class UnknownSPTypedLiteralImpl extends AbstractSPTypedLiteral
    implements SPTypedLiteral {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private static final Logger logger =
      Logger.getLogger(UnknownSPTypedLiteralImpl.class.getName());

  /**
   * Type code for this type.
   */
  static final int TYPE_ID = 127;

  /**
   * The lexical value of the literal.
   */
  private String lexicalForm;

  /**
   * @param lexicalForm  the lexical form of the literal, never
   *   <code>null</code>
   * @param datatypeURI  the URI of the literal's datatype, never
   *   <code>null</code>
   */
  UnknownSPTypedLiteralImpl(String lexicalForm, URI datatypeURI)
  {
    super(TYPE_ID, datatypeURI);

    // Validate parameters
    if (lexicalForm == null) {
      throw new IllegalArgumentException("Null \"lexicalForm\" parameter");
    }
    if (typeURI == null) {
      throw new IllegalArgumentException("Null \"typeURI\" parameter");
    }

    // Initialize fields
    this.lexicalForm = lexicalForm;
  }

  UnknownSPTypedLiteralImpl(ByteBuffer data)
  {
    super(TYPE_ID, null);

    // Split the buffer into the datatype and lexical form fields
    String string = CHARSET.decode(data).toString();
    int delimiter = string.indexOf('\t');
    if (delimiter == -1) {
      throw new RuntimeException("Corrupt string pool entry (no delimiter)");
    }
    
    try {
      // Initialize instance fields
      this.lexicalForm = string.substring(delimiter+1, string.length());
      this.typeURI     = new URI(string.substring(0, delimiter));
    }
    catch (URISyntaxException e) {
      throw new RuntimeException("Corrupt string pool entry", e);
    }
  }

  public ByteBuffer getData() {
    assert typeURI.toString().indexOf('\t') == -1:
           "Datatype URI contains tab: " + typeURI;
    return CHARSET.encode(typeURI + "\t" + lexicalForm);
  }

  public SPComparator getSPComparator() {
    return SPCaseInsensitiveStringComparator.getInstance();
  }

  public String getLexicalForm() {
    return lexicalForm;
  }

  public int compareTo(SPObject o) {
    // Compare types.
    int c = super.compareTo(o);
    if (c != 0) return c;

    // Compare the lexicalForms
    return lexicalForm.compareTo(((UnknownSPTypedLiteralImpl) o).lexicalForm);
  }

  public int hashCode() {
    return lexicalForm.hashCode() ^ typeURI.hashCode();
  }

  public boolean equals(Object obj) {

    // Check for null.
    if (obj == null || !(obj instanceof UnknownSPTypedLiteralImpl)) {
      return false;
    }
    assert obj instanceof UnknownSPTypedLiteralImpl;

    UnknownSPTypedLiteralImpl unknown = (UnknownSPTypedLiteralImpl) obj;

    return lexicalForm.equals(unknown.lexicalForm) &&
           typeURI.equals(unknown.typeURI);
  }
}
