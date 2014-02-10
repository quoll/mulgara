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
import org.mulgara.query.rdf.XSDAbbrev;
import org.mulgara.store.stringpool.*;


/**
 * Factory class to create instances of SPGMonth objects.
 *
 * @created 2004-10-06
 *
 * @author Mark Ludlow
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
public final class SPGMonthFactory implements SPTypedLiteralFactory {

  /** Logging for the class */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPGMonthFactory.class);

  /**
   * Retrieves the type id for this data type.
   *
   * @return The type id for this data type
   */
  public int getTypeId() {

    return SPGMonthImpl.TYPE_ID;
  }


  /**
   * Returns the type URI for the objects created by this factory.
   *
   * @return The XSD type URI
   */
  public Set<URI> getTypeURIs() {
    Set<URI> types = new HashSet<URI>();
    types.add(SPGMonthImpl.TYPE_URI);
    types.add(XSDAbbrev.GMONTH_URI);
    return Collections.unmodifiableSet(types);
  }

  /**
   * Creates an instance of a gMonth typed literal based on the lexical form.
   *
   * @param typeURI The type URI of the lexical form
   * @param lexicalForm The lexical form of the literal
   *
   * @return The typed literal, gMonth instance
   */
  public SPTypedLiteral newSPTypedLiteral(URI typeURI, String lexicalForm) {

    assert SPGMonthImpl.TYPE_URI.equals(typeURI);
    return SPGMonthImpl.newInstance(lexicalForm);
  }

  /**
   * Creates an instance of a gMonth typed literal based on the content of a byte
   * buffer.
   *
   * @param subtypeId Whether this literal was a subtype
   * @param data The byte buffer containing our date information
   *
   * @return The typed literal, gMonth instance
   */
  public SPTypedLiteral newSPTypedLiteral(int subtypeId, ByteBuffer data) {

    assert subtypeId == 0;
    return new SPGMonthImpl(data);
  }

}
