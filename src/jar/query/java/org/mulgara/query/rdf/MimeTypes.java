/*
 * Copyright 2011 Revelytix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.query.rdf;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * Central location for defining MIME content type constants.
 * 
 * @author Alex Hall
 * @created Jul 19, 2011
 */
public class MimeTypes {
  
  /** MIME type for N3 */
  public static final MimeType APPLICATION_N3;
  
  /** The MIME type of RDF/XML. */
  public static final MimeType APPLICATION_RDF_XML;

  /** The MIME type for XHTML documents */
  public static final MimeType APPLICATION_XHTML;

  /** The MIME type of MP3. */
  public static final MimeType AUDIO_MPEG;

  /** The MIME type for HTML documents */
  public static final MimeType TEXT_HTML;

  /** MIME type for N3 */
  public static final MimeType TEXT_RDF_N3;
  
  /** The MIME type of RLog. */
  public static final MimeType TEXT_RLOG;

  /** MIME type for Turtle */
  public static final MimeType TEXT_TURTLE;

  static {
    try {
      APPLICATION_N3      = new MimeType("application", "n3");
      APPLICATION_RDF_XML = new MimeType("application", "rdf+xml");
      APPLICATION_XHTML = new MimeType("application", "xhtml+xml");
      AUDIO_MPEG = new MimeType("audio", "mpeg");
      TEXT_HTML = new MimeType("text", "html");
      TEXT_RDF_N3         = new MimeType("text", "rdf+n3");
      TEXT_RLOG    = new MimeType("text", "rlog");
      TEXT_TURTLE         = new MimeType("text", "turtle");
    }
    catch (MimeTypeParseException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

}
