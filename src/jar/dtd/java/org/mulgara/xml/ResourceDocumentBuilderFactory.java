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

package org.mulgara.xml;


// Java 2 standard packages
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A {@link DocumentBuilderFactory} that sets the entity resolver of all the
 * {@link DocumentBuilder}s it creates to use the {@link
 * ResourceEntityResolver}.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ResourceDocumentBuilderFactory
  extends DocumentBuilderFactoryWrapper {

  //
  // Methods overriding the wrapped DocumentBuilderFactory
  //

  /**
   * This sets the entity resolver to a {@link ResourceEntityResolver}.
   *
   * @return RETURNED VALUE TO DO
   * @throws ParserConfigurationException if the wrapped document builder can't
   *      create an instance that satisfies the configuration requested
   */
  public DocumentBuilder newDocumentBuilder()
    throws ParserConfigurationException {

    // Get a parser from the wrapped instance
    DocumentBuilder documentBuilder = wrapped.newDocumentBuilder();

    // Wrap the parser's entity resolver
    documentBuilder.setEntityResolver(new ResourceEntityResolver());

    // Pass on the modified parser
    return documentBuilder;
  }
}
