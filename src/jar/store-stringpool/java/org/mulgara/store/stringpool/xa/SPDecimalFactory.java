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
import java.util.*;
import java.net.URI;
import java.nio.ByteBuffer;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.rdf.XSD;
import org.mulgara.query.rdf.XSDAbbrev;
import org.mulgara.store.stringpool.*;


/**
 * A factory for SPDecimalImpl objects.
 *
 * @created 2004-10-05
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
public final class SPDecimalFactory implements SPTypedLiteralFactory {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPDecimalFactory.class);

  /** The label for the base XSD type. */
  private final static String DECIMAL = "decimal";

  /** The subtype ID for the base XSD type. */
  private final static int XSD_DEC_ID = 0;

  /**
   * The labels for each of the XSD types.
   * The element at position {@link #XSD_DEC_ID} must be {@link #DECIMAL}.
   */
  private final static String[] TYPE_LABELS = {
    DECIMAL,    // must be at position 0 to match XSD_DEC_ID
    "integer",
    "nonPositiveInteger",
    "negativeInteger",
    "long",
    "int",
    "short",
    "byte",
    "nonNegativeInteger",
    "unsignedLong",
    "unsignedInt",
    "unsignedShort",
    "unsignedByte",
    "positiveInteger",
  };

  /** The type URIs, indexed by subtype ID. */
  private final static URI[] TYPE_URIS = new URI[TYPE_LABELS.length];

  /** A map of the URIs to the subtypes. This includes abbreviated URIs. */
  private final static Map<URI,Integer> uriToSubtypeIdMap;

  static {
    // check that no one changed the structure
    assert TYPE_LABELS[XSD_DEC_ID].equals(DECIMAL);
    // Populate the uriToSubtypeIdMap.
    uriToSubtypeIdMap = new HashMap<URI,Integer>();
    for (int i = 0; i < TYPE_LABELS.length; ++i) {
      URI type = URI.create(XSD.NAMESPACE + TYPE_LABELS[i]);
      uriToSubtypeIdMap.put(type, i);
      uriToSubtypeIdMap.put(URI.create(XSDAbbrev.NAMESPACE + TYPE_LABELS[i]), i);
      TYPE_URIS[i] = type;
    }
  }


  /** @see org.mulgara.store.stringpool.SPTypedLiteralFactory#getTypeId() */
  public int getTypeId() {
    return SPDecimalImpl.TYPE_ID;
  }


  /**
   * Returns the type URIs for the objects created by this factory.
   * @return All the type URIs this factory handles.
   */
  public Set<URI> getTypeURIs() {
    return Collections.unmodifiableSet(uriToSubtypeIdMap.keySet());
  }


  /** @see org.mulgara.store.stringpool.SPTypedLiteralFactory#newSPTypedLiteral(java.net.URI, java.lang.String) */
  public SPTypedLiteral newSPTypedLiteral(URI typeURI, String lexicalForm) {
    Integer subtypeIdI = (Integer)uriToSubtypeIdMap.get(typeURI);
    if (subtypeIdI == null) {
      throw new IllegalArgumentException("Invalid type URI: " + typeURI);
    }
    if (subtypeIdI == XSD_DEC_ID) {
      return new SPDecimalBaseImpl(subtypeIdI, typeURI, lexicalForm);
    } else {
      return new SPDecimalExtImpl(subtypeIdI, typeURI, lexicalForm);
    }
  }


  /** @see org.mulgara.store.stringpool.SPTypedLiteralFactory#newSPTypedLiteral(int, java.nio.ByteBuffer) */
  public SPTypedLiteral newSPTypedLiteral(int subtypeId, ByteBuffer data) {
    if (subtypeId < 0 || subtypeId >= TYPE_URIS.length) {
      throw new IllegalArgumentException("Invalid subtype ID: " + subtypeId);
    }
    if (subtypeId == XSD_DEC_ID) {
      return new SPDecimalBaseImpl(subtypeId, TYPE_URIS[subtypeId], data);
    } else {
      return new SPDecimalExtImpl(subtypeId, TYPE_URIS[subtypeId], data);
    }
  }

}
