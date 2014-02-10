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

// std packages
import java.io.*;
import java.net.URI;

// Logging
import org.apache.log4j.*;

// Mp3 Library
import org.blinkenlights.id3.*;
import org.blinkenlights.id3.v1.*;
import org.blinkenlights.id3.v2.*;

// JRDF
import org.jrdf.graph.mem.*;
import org.jrdf.graph.*;

import org.mulgara.content.mp3.parser.exception.*;

/**
 * An object that allows access to the conversions of MP3 ID3 tags and the RDF
 * model containing its statements.
 *
 * @created 2004-08-02
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/11 07:02:25 $
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
public class MP3Conversion {

  /** The category to log to. */
  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(MP3Conversion.class);

  /** The RDF conversion path */
  private String rdfConversionPath;

  /** The text conversion path */
  private String textConversionPath;

  /** The html conversion path */
  private String htmlConversionPath;

  /** The graph that this conversion should parse into */
  private Graph graph;

  /** The MP3File this object represents */
  private MP3File mp3File;

  /** The identification that this file should use in cases like resource names */
  private URI resourceUri;

  /**
   * Constructor which creates a conversion object with no specific graph.
   *
   * @param mp3File The file we are converting
   * @param uri The uri of our source file
   */
  public MP3Conversion (MP3File mp3File, URI uri) throws ConversionException {

    // Store the given file
    this.mp3File = mp3File;

    try {

      // Create a new model
      graph = new GraphImpl();
    } catch (GraphException graphException) {

      throw new ConversionException("Failed to create a new graph for conversion.",
                                    graphException);
    }

    // Store the uri
    resourceUri = uri;
  }

  /**
   * Constructor that creates a conversion object using a specific graph.
   *
   * @param mp3File The file we are converting
   * @param graph The graph to store the conversion RDF in
   * @param uri The uri of our source file
   */
  public MP3Conversion (MP3File mp3File, Graph graph, URI uri) {

    // Store the given file
    this.mp3File = mp3File;

    // Create a new graph
    this.graph = graph;

    // Store the uri
    resourceUri = uri;
  }

  /**
   * Retrieves the uri of the original file.
   *
   * @return The uri of the original file
   */
  public URI getUri() {

    return resourceUri;
  }

  /**
   * Sets the path for the RDF conversion.
   *
   * @param conversionPath The conversion path for the RDF conversion
   */
  public void setRDFConversionPath (String conversionPath) {

    // Set the new conversion path
    rdfConversionPath = conversionPath;
  }

  /**
   * Retrieves the path of the RDF conversion file.
   *
   * @return The RDF conversion path
   */
  public String getRDFConversionPath () {

    return rdfConversionPath;
  }

  /**
   * Retrieves the rdf conversion as a file.
   *
   * @return The RDF conversion as a file
   */
  public File getRDFConversionFile () {

    return new File(rdfConversionPath);
  }

  /**
   * Sets the path for the text conversion.
   *
   * @param conversionPath The conversion path for the text conversion
   */
  public void setTextConversionPath (String conversionPath) {

    // Set the new conversion path
    textConversionPath = conversionPath;
  }

  /**
   * Retrieves the path of the text conversion file.
   *
   * @return The text conversion path
   */
  public String getTextConversionPath () {

    return textConversionPath;
  }

  /**
   * Retrieves the text conversion as a file.
   *
   * @return The text conversion as a file
   */
  public File getTextConversionFile () {

    return new File(textConversionPath);
  }

  /**
   * Sets the path for the html conversion.
   *
   * @param conversionPath The conversion path for the html conversion
   */
  public void setHtmlConversionPath (String conversionPath) {

    // Set the new conversion path
    htmlConversionPath = conversionPath;
  }

  /**
   * Retrieves the path of the html conversion file.
   *
   * @return The html conversion path
   */
  public String getHtmlConversionPath () {

    return htmlConversionPath;
  }

  /**
   * Retrieves the html conversion as a file.
   *
   * @return The html conversion as a file
   */
  public File getHtmlConversionFile () {

    return new File(htmlConversionPath);
  }

  /**
   * Sets the graph that should be used for this conversion.
   *
   * @param graph The graph to use for statements regarding the conversion
   */
  public void setGraph (Graph graph) {

    this.graph = graph;
  }

  /**
   * Retrieves the graph that stores the statements for this conversion.
   *
   * @return The graph that stores the statements for this conversion
   */
  public Graph getGraph () {

    return graph;
  }

  /**
   * Retrieves the mp3File that this class represents.
   *
   * @return MP3File
   */
  public MP3File getMP3File () {

    return mp3File;
  }

  /**
   * Retrieves the ID3v1 tag associated with this object.
   *
   * @return The ID3v1 tag associated with this object
   * @throws ID3Exception error reading the file.
   */
  public ID3V1Tags getV1Tag () throws ID3Exception {

    return mp3File.getID3V1Tags();
  }

  /**
   * Retrieves the ID3v2 tag associated with this object.
   *
   * @return The ID3v2 tag associated with this object
   * @throws ID3Exception error reading the file.
   */
  public ID3V2Tags getV2Tag () throws ID3Exception {

    return mp3File.getID3V2Tags();
  }
}
