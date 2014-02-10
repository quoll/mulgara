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

package org.mulgara.content.mp3.parser.api;

// MP3 Package
import org.blinkenlights.id3.v2.*;

// JRDF
import org.jrdf.graph.*;

// Internal packages
import org.mulgara.content.mp3.parser.exception.ParserException;

/**
 * Parser to take in an ID3v2 tag and convert it into multiple formats.
 *
 * @created 2004-07-30
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/11 07:02:24 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company Tucana Technologies
 *
 * @copyright &copy; 2004
 *   <A href="http://www.tucanatech.com/">Tucana Technologies</A>
 *
 * @licence <A href="{@docRoot}/LICENCE">License description</A>
 */
public interface ID3v2Parser {

  /**
   * Converts the given ID3v2 tag to rdf and stores the data in a given JRDF graph.
   *
   * @param id3v2 The ID3v2 tag for the file
   * @param mp3Resource The parent resource of the tag
   * @param graph The graph to store the parsed conversion in
   *
   * @throws ParserException
   */
  public void parseRDF (ID3V2Tags id3v2, SubjectNode mp3Resource,
                        Graph graph) throws ParserException;
}
