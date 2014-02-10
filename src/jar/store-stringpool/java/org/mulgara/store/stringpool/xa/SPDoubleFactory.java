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
 * A factory for SPDoubleImpl objects.
 *
 * @created 2004-09-24
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
public final class SPDoubleFactory implements SPTypedLiteralFactory {

  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(SPDoubleFactory.class);


  public int getTypeId() {
    return SPDoubleImpl.TYPE_ID;
  }


  /**
   * Returns the type URI for the objects created by this factory.
   */
  public Set<URI> getTypeURIs() {
    Set<URI> types = new HashSet<URI>();
    types.add(SPDoubleImpl.TYPE_URI);
    types.add(XSDAbbrev.DOUBLE_URI);
    return Collections.unmodifiableSet(types);
  }


  public SPTypedLiteral newSPTypedLiteral(URI typeURI, String lexicalForm) {
    assert SPDoubleImpl.TYPE_URI.equals(typeURI);
    return new SPDoubleImpl(lexicalForm);
  }


  public SPTypedLiteral newSPTypedLiteral(int subtypeId, ByteBuffer data) {
    assert subtypeId == 0;
    return new SPDoubleImpl(data);
  }

}
