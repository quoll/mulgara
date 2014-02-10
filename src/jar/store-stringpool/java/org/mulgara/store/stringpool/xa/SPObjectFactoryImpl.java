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

// JRDF
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

// Third party packages

// Locally written packages
import org.mulgara.store.stringpool.*;


/**
 * A factory for creating SPObjects.
 *
 * @created 2004-07-04
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
public class SPObjectFactoryImpl implements SPObjectFactory {

  private static final SPObjectFactoryImpl INSTANCE = new SPObjectFactoryImpl();


  public static SPObjectFactory getInstance() {
    return INSTANCE;
  }


  public SPObject createSPObjectFromEncodedString(String encodedString) {
    int len = encodedString.length();

    if (len < 2) {
      throw new IllegalArgumentException(
          "encodedString too short: \"" + encodedString + "\""
      );
    }

    switch (encodedString.charAt(0)) {
    case '"': // This is either a typed or an untyped literal.
      if (encodedString.charAt(len - 1) == '"') {
        // Last char is a double-quote so assume it is an untyped literal.
        return SPStringImpl.newSPObject(
            AbstractSPObject.unescapeString(encodedString.substring(1, len - 1))
        );
      }

      // This is either a typed literal, or it is untyped with a language code.
      // Check for the start of a type URI
      int index = encodedString.lastIndexOf("\"^^<");
      if (index == -1) {
        // must be a language-coded untyped literal
        index = encodedString.lastIndexOf("\"@");
        if (index == -1) throw new IllegalArgumentException("Could not parse encoded string (String?): \"" + encodedString + "\"");
        String lang = encodedString.substring(index + 2);  // 2 for the quote and @ characters
        return SPStringImpl.newSPObject(
            AbstractSPObject.unescapeString(encodedString.substring(1, index)), lang
        );
      }

      // must be a typed literal
      if (encodedString.charAt(len - 1) != '>') {
        throw new IllegalArgumentException("Bad encodedString format (Typed literal?): \"" + encodedString + "\"");
      }

      return newSPTypedLiteral(
          AbstractSPObject.unescapeString(encodedString.substring(1, index)),
          URI.create(encodedString.substring(index + 4, len - 1))
      );

    case '<': // A URI.
      if (encodedString.charAt(len - 1) != '>') {
        throw new IllegalArgumentException(
            "Bad encodedString format (URI?): \"" + encodedString + "\""
        );
      }

      return SPURIImpl.newSPObject(encodedString.substring(1, len - 1));

    default:
      throw new IllegalArgumentException(
          "Could not parse encoded string: \"" + encodedString + "\""
      );
    }
  }


  /**
   * Constructs an SPObject from the encoded representation used in the
   * backup file.
   */
  public SPObject createSPObjectFromBackupEncodedString(String encodedString) {
    if (encodedString.length() < 2) {
      throw new IllegalArgumentException(
        "encodedString too short: " + encodedString
      );
    }

    switch (encodedString.charAt(0)) {
    case '"':
    case '<':
      return createSPObjectFromEncodedString(encodedString);

    // Support old backup format for backward compatibility.
    case '@': // DateTime.
      return new SPDateTimeImpl(Long.parseLong(encodedString.substring(1)));

    // Support old backup format for backward compatibility.
    case '#': // Double.
      return new SPDoubleImpl(Double.parseDouble(encodedString.substring(1)));

    default:
      throw new IllegalArgumentException(
          "Could not parse backup encoded string: \"" + encodedString + "\""
      );
    }
  }


  /**
   * A convenience method for creating SPString objects.
   */
  public SPString newSPString(String str) {
    return new SPStringImpl(str);
  }


  /**
   * A convenience method for creating SPURI objects.
   */
  public SPURI newSPURI(URI uri) {
    return new SPURIImpl(uri);
  }


  public SPTypedLiteral newSPTypedLiteral(String lexicalForm, URI typeURI) {
    try {
      return SPTypedLiteralRegistry.getSPTypedLiteralFactory(typeURI).
          newSPTypedLiteral(typeURI, lexicalForm);
    } catch (Exception e) {
      // catch any parsing problems, and store in an "untyped" way
      return SPTypedLiteralRegistry.getSPTypedLiteralFactory(UnknownSPTypedLiteralImpl.TYPE_ID).
          newSPTypedLiteral(typeURI, lexicalForm);
    }
  }


  /**
   * A convenience method for creating SPDouble objects.
   */
  public SPDouble newSPDouble(double d) {
    return new SPDoubleImpl(d);
  }


  public SPObject newSPObject(org.jrdf.graph.Node rdfNode) {
    if (rdfNode == null) {
      throw new IllegalArgumentException("rdfNode parameter is null");
    }

    if (rdfNode instanceof Literal) {
      Literal literal = (Literal)rdfNode;

      URI typeURI = literal.getDatatypeURI();
      String lexicalForm = literal.getLexicalForm();

      if (typeURI != null) {
        // Create an SPObject representing a typed literal.
        return newSPTypedLiteral(lexicalForm, typeURI);
      }

      // Create an SPObject representing an untyped literal.
      return SPStringImpl.newSPObject(lexicalForm, literal.getLanguage());
    }

    if (rdfNode instanceof URIReference) {
      return new SPURIImpl(((URIReference)rdfNode).getURI());
    }

    throw new IllegalArgumentException(
        "Unsupported jrdf node type: " + rdfNode + " (" + rdfNode.getClass() + ")"
    );
  }


  public SPTypedLiteralFactory getSPTypedLiteralFactory(URI typeURI) {
    return SPTypedLiteralRegistry.getSPTypedLiteralFactory(typeURI);
  }


  public SPTypedLiteralFactory getSPTypedLiteralFactory(int typeId) {
    return SPTypedLiteralRegistry.getSPTypedLiteralFactory(typeId);
  }


  public int getTypeId(URI typeURI) {
    return getSPTypedLiteralFactory(typeURI).getTypeId();
  }


  /**
   * Constructs a new SPObject instance from the given binary representation.
   */
  public SPObject newSPObject(
      SPObject.TypeCategory typeCategory, int typeId, int subtypeId,
      ByteBuffer data
  ) {
    if (typeCategory == null) {
      throw new IllegalArgumentException("typeCategory is null");
    }

    if (
        typeCategory.ID != SPObject.TypeCategory.TCID_TYPED_LITERAL &&
        typeId != SPObjectFactory.INVALID_TYPE_ID
    ) {
      throw new IllegalArgumentException(
          "typeId is set to valid value when typeCategory is not TYPED_LITERAL"
      );
    }

    switch (typeCategory.ID) {
    case SPObject.TypeCategory.TCID_URI:
      return new SPURIImpl(data);

    case SPObject.TypeCategory.TCID_UNTYPED_LITERAL:
      return new SPStringImpl(data);

    case SPObject.TypeCategory.TCID_TYPED_LITERAL:
      return SPTypedLiteralRegistry.getSPTypedLiteralFactory(typeId).
          newSPTypedLiteral(subtypeId, data);

    //case SPObject.TypeCategory.TCID_LANG_LITERAL:
    //  throw new StringPoolException("LANG_LITERAL not supported.");

    default:
      throw new IllegalArgumentException(
          "Bad typeCategory parameter: " + typeCategory
      );
    }
  }

}
