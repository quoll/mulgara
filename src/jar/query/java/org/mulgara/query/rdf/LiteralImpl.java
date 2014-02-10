
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
import java.io.*;
import java.net.URI;
import java.util.*;

// Third party packages
import org.jrdf.graph.*;  // JRDF


// Locally written packages
import org.mulgara.query.Value;

/**
 * An RDF literal node.
 * 
 * Strings for the constructor need to be in an unusual format. Each character in the
 * string is used to represent a byte in a UTF-8 encoding. This has no effect on
 * standard ASCII text, but once the characters get above 0x7F then this format no
 * longer represents the string, but rather the encoded data. This is the format
 * returned from the ARP parser.
 *
 * For instance, the character Ž (e acute) has a unicode value of 0xE9, and a
 * UTF-8 encoding of [0xC3, 0xA9]. The literal string used to represent this character
 * has two characters in it (representing the 2 bytes from the UTF-8 encoding),
 * specifically Ì (Latin capital letter A with tilde, unicode value 0xC3)
 * and © (copyright sign, unicode value 0xA9). This is despite the fact that a Java
 * string can represent e-acute with a single character.
 * 
 * Previously, this class accepted normal Java strings, and would escape them
 * correctly when presenting the lexical form. If lexical escaping fails, then it
 * will fall back to attempting this method. This means that many normal Java strings
 * will work with this class, but it is still possible to construct a standard string
 * that could potentially represent a UTF-encoding.
 * 
 *
 * @created 2001-08-13
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/03/02 11:21:26 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LiteralImpl extends AbstractLiteral implements Comparable<Node>,
    Serializable, Value {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  static final long serialVersionUID = 3123944034259700724L;

  //
  // Constructors
  //

  /**
   * Construct a literal.
   *
   * @param lexicalForm  the text part of the literal
   * @throws IllegalArgumentException if <var>lexicalForm</var> is <code>null</code>
   */
  public LiteralImpl(String lexicalForm) {
    super(lexicalForm);
  }

  /**
   * Construct a fully general literal.
   *
   * @param lexicalForm  the text part of the literal
   * @param language  the language code, possibly the empty string but not
   *    <code>null</code>
   * @throws IllegalArgumentException if <var>lexicalForm</var> or
   *    <var>lang</var> are <code>null</code>
   */
  public LiteralImpl(String lexicalForm, String language) {
    super(lexicalForm, language);
  }


  /**
   * Construct a fully general literal.
   *
   * @param lexicalForm  the text part of the literal
   * @param datatype  the URI for a datatyped literal, or <code>null</code> for
   *     an untyped literal
   * @throws IllegalArgumentException if <var>lexicalForm</var> or
   *     <var>lang</var> are <code>null</code>
   */
  public LiteralImpl(String lexicalForm, URI datatype) {
    super(lexicalForm, datatype);
  }

  /**
   * Construct an <code>xsd:dateTime</code> literal.
   *
   * Beware that although this is constructed from a Java {@link Date}
   * parameter, the datatype of the created literal is not
   * <code>xsd:date</code>.
   *
   * @param date  the date value of the literal
   */
  public LiteralImpl(Date date) {
    this(XSD.getLexicalForm(date), XSD.DATE_TIME_URI);
  }

  /**
   * Construct an <code>xsd:double</code> literal.
   *
   * @param value  the numerical value of the literal
   */
  public LiteralImpl(double value) {
    this(Double.toString(value), XSD.DOUBLE_URI);
  }

  /**
   * Construct an <code>xsd:integer</code> literal.
   * Choosing this over the more specific XSD.LONG
   * @param value  the numerical value of the literal
   */
  public LiteralImpl(long value) {
    this(Long.toString(value), XSD.INTEGER_URI);
  }

  //
  // Methods implementing the Comparable interface
  //

  /**
   * Comparison is by the <var>text</var> property.
   *
   * @param object PARAMETER TO DO
   * @return RETURNED VALUE TO DO
   */
  public int compareTo(Node object) {

    if (object instanceof BlankNode) {
      return 1;
    }

    if (object instanceof URIReference) {
      return 1;
    } else if (object instanceof Literal) {
      Literal literal = (Literal) object;
      return getLexicalForm().compareTo(literal.getLexicalForm());
    } else {
      throw new ClassCastException("Not an RDF node");
    }
  }
}
