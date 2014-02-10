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

// JRDF
import org.jrdf.graph.*;
import org.jrdf.vocabulary.RDF;

// Log4J
import org.apache.log4j.*;

// Mp3 Library
import org.blinkenlights.id3.*;
import org.blinkenlights.id3.v1.*;
import org.blinkenlights.id3.v2.*;

// Internal packages
import org.mulgara.content.mp3.parser.api.*;
import org.mulgara.content.mp3.parser.exception.*;

/**
 * Parser to take in an ID3 file and convert it into multiple formats.  This is
 * an implementation of the api.
 *
 * @created 2004-07-27
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/11 07:02:23 $
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
public final class ID3ParserImpl implements ID3Parser {

  /** The category to log to. */
  private static final Logger log = Logger.getLogger(ID3ParserImpl.class);

  /**
   * Constructor.
   */
  public ID3ParserImpl() {

  }

  /**
   * Parses an mp3 conversion object.
   *
   * @param mp3File The conversion object to parse into a model
   *
   * @throws ParserException
   */
  public void parseTags(MP3Conversion mp3File) throws ParserException {

    ID3V1Tags id3v1 = null;
    ID3V2Tags id3v2 = null;

    try {
      // Get the v1 tag associated with the conversion
      id3v1 = mp3File.getV1Tag();

      // Get the v2 tag associated with the conversion
      id3v2 = mp3File.getV2Tag();
    }
    catch (ID3Exception ie) {
      // Exception thrown - ignore
    }

    // Get the graph of the conversion
    Graph model = mp3File.getGraph();

    // Container for the graphelement factory
    GraphElementFactory factory = null;

    // Get the factory to create elements
    factory = model.getElementFactory();

    // Container for our subject node
    SubjectNode resource = null;

    try {

      // Create a resource out of the actual file
      resource = factory.createResource();
    }
    catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create MP3 resource.",
          graphElementFactoryException);
    }

    if (log.isDebugEnabled()) {

      log.debug("!! Processing file with tags " + id3v1 + " and  " + id3v2);
    }

    if (id3v1 != null) {

      // Container for a v1 parser
      ID3v1Parser v1Parser = null;

      try {

        // If the ID3v1 tag exists, create a parser to parse the ID3v1 tag
        v1Parser = ParserFactory.getInstance().createID3v1Parser();
      }
      catch (FactoryException factoryException) {

        // Log the error
        log.error("Unable to instantiate ID3v1 Parser instance due to a bad " +
            "configuration for classes.", factoryException);

        // Pass the exception on as a ToolConfigException
        throw new ParserException(
            "Unable to instantiate ID3v1 Parser instance " +
            "due to a bad configuration for classes." +
            factoryException);
      }

      if (log.isDebugEnabled()) {

        log.debug("!! Processing v1 tag with type " + id3v1.getClass());
      }

      // Carry out the actual parsing
      v1Parser.parseRDF(id3v1, resource, model);
    }

    if (id3v2 != null) {

      // Container for the v2 parser
      ID3v2Parser v2Parser = null;

      try {

        // If the ID3v2 tag exists, create a parser to parse the ID3v2 tag
        v2Parser = ParserFactory.getInstance().createID3v2Parser();
      }
      catch (FactoryException factoryException) {

        // Log the error
        log.error("Unable to instantiate ID3v2 Parser instance due to a bad " +
            "configuration for classes.", factoryException);

        // Pass the exception on as a ToolConfigException
        throw new ParserException(
            "Unable to instantiate ID3v2 Parser instance " +
            "due to a bad configuration for classes." +
            factoryException);
      }

      // Carry out the actual parsing
      v2Parser.parseRDF(id3v2, resource, model);
    }

    // Container for our processor instance
    IdentifierProcessor processor = new IdentifierProcessor();

    try {

      // Populate the processor
      processor.createMappings(model);
    }
    catch (IdentifierException identifierException) {

      throw new ParserException(
          "Unable to initialise the identifier processor.",
          identifierException);
    }

    try {

      // Get the type identifier
      model.add(resource, factory.createResource(RDF.TYPE),
          (ObjectNode) processor.resolveIdentifier(IdentifierProcessor.MP3_TYPE));
    }
    catch (GraphException graphException) {

      throw new ParserException("Unable to add type to MP3 resource.",
          graphException);
    }
    catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a predicate for type.",
          graphElementFactoryException);
    }

    try {

      // Add the uri property
      model.add(resource,
          processor.resolveIdentifier(IdentifierProcessor.MP3_URI),
          factory.createResource(mp3File.getUri()));
    }
    catch (GraphException graphException) {

      throw new ParserException("Unable to add uri to id3v1 resource.",
          graphException);
    }
    catch (GraphElementFactoryException graphElementFactoryException) {

      throw new ParserException("Unable to create a literal for uri.",
          graphElementFactoryException);
    }

    if (log.isDebugEnabled()) {

      log.debug("-- Created uri triple with value: " + mp3File.getUri());
    }

  }
}
