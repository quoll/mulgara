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
import org.mulgara.query.rdf.*;
import org.mulgara.store.stringpool.*;


/**
 * Factory for creating SPbase64Binary objects.
 *
 * @created 2004-10-04
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.1 $
 *
 * @modified $Date: 2005/03/11 04:15:22 $ by $Author: raboczi $
 *
 * @company <a href="http://www.tucanatech.com/">Tucana Technologies</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public final class SPBase64BinaryFactory implements SPTypedLiteralFactory {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPBase64BinaryFactory.class);


  /**
   * Returns the SPBase64Binary type ID.
   * @return int
   */
  public int getTypeId() {
    return SPBase64BinaryImpl.TYPE_ID;
  }

  /**
   * Returns the type URI for the objects created by this factory.
   * @return URI
   */
  public Set<URI> getTypeURIs() {
    Set<URI> types = new HashSet<URI>();
    types.add(SPBase64BinaryImpl.TYPE_URI);
    types.add(XSDAbbrev.BASE64_BINARY_URI);
    return Collections.unmodifiableSet(types);
  }

  /**
   * Returns a SPBase64Binary by evaluating the lexicalForm.
   *
   * @param typeURI URI must be the xsd:base64Binary URI
   * @param lexicalForm String must contain the values [0-9a-zA-Z+/=]
   * @return SPTypedLiteral
   */
  public SPTypedLiteral newSPTypedLiteral(URI typeURI, String lexicalForm) {
    assert SPBase64BinaryImpl.TYPE_URI.equals(typeURI);
    return new SPBase64BinaryImpl(lexicalForm);
  }

  /**
   * Returns a SPBase64Binary by evaluating the data.
   *
   * @param subtypeId int must be the SPBase64Binary subtypeId.
   * @param data ByteBuffer
   * @return SPTypedLiteral
   */
  public SPTypedLiteral newSPTypedLiteral(int subtypeId, ByteBuffer data) {
    assert subtypeId == 0;
    return new SPBase64BinaryImpl(data);
  }

}
