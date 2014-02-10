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

package org.mulgara.content.mp3.parser;

// Log4j
import org.apache.log4j.*;

// Mp3 Library
import org.blinkenlights.id3.v2.*;

// JRDF
import org.jrdf.graph.*;

// Internal Packages
import org.mulgara.content.mp3.parser.api.*;
import org.mulgara.content.mp3.parser.exception.*;

/**
 * Parser to take in an ID3v2 file and convert it into multiple formats.  This is
 * an implementation of the original interface.
 *
 * @created 2004-07-29
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.9 $
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
public final class ID3v2ParserImpl implements ID3v2Parser {

  /** The category to log to. */
  private static final Logger log = Logger.getLogger(ID3v2ParserImpl.class);

  /**
   * Constructor.
   */
  public ID3v2ParserImpl() {

  }

  /**
   * Converts the given ID3v2 tag to rdf and stores the data in a given JRDF graph.
   *
   * @param id3v2 The ID3v2 tag for the file
   * @param mp3Resource The parent resource of the tag
   * @param graph The graph to store the parsed conversion in
   *
   * @throws ParserException
   */
  public void parseRDF(ID3V2Tags id3v2, SubjectNode mp3Resource,
      Graph graph) throws ParserException {

    // Retrieve a field iterator for the ID3 tag
    ID3V2Frame[] frames = id3v2.getSingleFrames();

    // Container for our processor instance
    IdentifierProcessor processor = new IdentifierProcessor();

    try {

      // Populate the processor
      processor.createMappings(graph);
    }
    catch (IdentifierException identifierException) {

      throw new ParserException(
          "Unable to initialise the identifier processor.",
          identifierException);
    }

    // Get the element factory for the graph
    GraphElementFactory factory = graph.getElementFactory();

    for (int index = 0; index < frames.length; index++) {

      // Retrieve the next tag item
      ID3V2Frame frame = frames[index];

      try {

        // Get the property that this identifier represents
        graph.add(mp3Resource,
            processor.resolveIdentifier(frame.getClass().toString()),
            processor.resolveLiteral(frame, factory));
      }
      catch (GraphException graphException) {

        throw new ParserException("Unable to add " + frame +
            " to id3v2 resource.", graphException);
      }
      catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ParserException("Unable to create a literal for" + frame,
            graphElementFactoryException);
      }

      if (log.isDebugEnabled()) {
        try {
          log.debug("Added triple: <" + mp3Resource + ", " +
              processor.resolveIdentifier(frame.getClass().toString()) + ", " +
              processor.resolveLiteral(frame, factory) + ">");
        }
        catch (GraphElementFactoryException graphElementFactoryException) {

          throw new ParserException("Unable to create a literal for" + frame,
              graphElementFactoryException);
        }
      }
    }
  }
}
