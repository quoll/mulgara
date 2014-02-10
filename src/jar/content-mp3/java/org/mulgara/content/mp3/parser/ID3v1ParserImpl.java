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

// MP3 Library
import org.blinkenlights.id3.v1.*;

// JRDF Library
import org.jrdf.graph.*;

// Internal package.
import org.mulgara.content.mp3.parser.api.*;
import org.mulgara.content.mp3.parser.exception.*;

/**
 * Parser to take in an ID3v1 file and convert it into an RDF document.
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
public final class ID3v1ParserImpl implements ID3v1Parser {

  /** The category to log to. */
  private static final Logger log = Logger.getLogger(ID3v1ParserImpl.class);

  /**
   * Constructor.
   */
  public ID3v1ParserImpl() {

  }

  /**
   * Converts the given ID3v1 tag to rdf and stores the data in a given JRDF graph.
   *
   * @param id3v1 The ID3v1 tag for the file
   * @param mp3Resource The parent resource of the tag
   * @param graph The graph to store the parsed conversion in
   *
   * @throws ParserException
   */
  public void parseRDF(ID3V1Tags id3v1, SubjectNode mp3Resource,
      Graph graph) throws ParserException {

    // Get the element factory for the graph
    GraphElementFactory factory = graph.getElementFactory();

    // Container for our processor instance
    IdentifierProcessor processor = new IdentifierProcessor();

    try {

      // Populate the processor
      processor.createMappings(graph);
    } catch (IdentifierException identifierException) {

      throw new ParserException(
          "Unable to initialise the identifier processor.",
          identifierException);
    }

    try {

      // Add the album property
      graph.add(mp3Resource, processor.resolveIdentifier(IdentifierProcessor.TALB),
          factory.createLiteral(id3v1.getAlbum()));
    } catch (GraphException graphException) {

      throw new ParserException("Unable to add album to id3v1 resource.",
          graphException);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a literal for album name.",
          graphElementFactoryException);
    }

    try {

      // Add the artist property
      graph.add(mp3Resource, processor.resolveIdentifier(IdentifierProcessor.TCOM),
          factory.createLiteral(id3v1.getArtist()));
    } catch (GraphException graphException) {

      throw new ParserException("Unable to add artist to id3v1 resource.",
          graphException);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a literal for artist name.",
          graphElementFactoryException);
    }

    try {

      // Add the comment property
      graph.add(mp3Resource, processor.resolveIdentifier(IdentifierProcessor.COMM),
          factory.createLiteral(id3v1.getComment()));
    } catch (GraphException graphException) {

      throw new ParserException("Unable to add comment to id3v1 resource.",
          graphException);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a literal for comment.",
          graphElementFactoryException);
    }

    try {

      // Add the genre property
      graph.add(mp3Resource, processor.resolveIdentifier(IdentifierProcessor.MCDI),
          factory.createLiteral("" + id3v1.getGenre()));
    }
    catch (GraphException graphException) {

      throw new ParserException("Unable to add genre to id3v1 resource.",
          graphException);
    }
    catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a literal for genre.",
          graphElementFactoryException);
    }

    try {

      // Add the title property
      graph.add(mp3Resource, processor.resolveIdentifier(IdentifierProcessor.TOAL),
          factory.createLiteral(id3v1.getTitle()));
    } catch (GraphException graphException) {

      throw new ParserException("Unable to add title to id3v1 resource.",
          graphException);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a literal for title.",
          graphElementFactoryException);
    }

    if (id3v1.getYear().equals("")) {

      // Set the year to be 0 if there is no year set
      id3v1.setYear("0");
    }

    try {

      // Add the year property
      graph.add(mp3Resource, processor.resolveIdentifier(IdentifierProcessor.TYER),
          factory.createLiteral(id3v1.getYear()));
    } catch (GraphException graphException) {

      throw new ParserException("Unable to add year to id3v1 resource.",
          graphException);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a literal for year.",
          graphElementFactoryException);
    }

    if (log.isDebugEnabled()) {

      log.debug("Added year " + id3v1.getYear());
    }

    if (id3v1 instanceof ID3V1_1Tags) {

      try {
        // Add the track property
        graph.add(mp3Resource, processor.resolveIdentifier(IdentifierProcessor.TRCK),
            factory.createLiteral("" + ((ID3V1_1Tags) id3v1).getAlbumTrack()));
      } catch (GraphException graphException) {

        throw new ParserException(
            "Unable to add track number to id3v1 resource.",
            graphException);
      } catch (GraphElementFactoryException graphElementFactoryException) {

        throw new ParserException(
            "Unable to create a literal for track number.",
            graphElementFactoryException);
      }
    }
  }
}
