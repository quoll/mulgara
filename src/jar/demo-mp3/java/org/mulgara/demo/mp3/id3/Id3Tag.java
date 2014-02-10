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

package org.mulgara.demo.mp3.id3;

// Java 2 standard packages
import java.net.URI;

// JRDF
import org.jrdf.graph.URIReference;
import org.jrdf.util.ClosableIterator;

// Local Packages
import org.mulgara.demo.mp3.Mp3Context;
import org.mulgara.demo.mp3.Mp3File;
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;


/**
 * Represents an Mp3File's Id3Tag.
 *
 * @created 2004-12-03
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:06 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface Id3Tag {

  /** Newline character ('\n' on Unix) */
  public final static String NEWLINE = System.getProperty("line.separator");

  /** The Namespace used for id3 properties */
  public static final String NAMESPACE = "http://mulgara.org/mulgara/id3#";

  /** Used in queries to represent an Mp3's URI */
  public final static String ID3_URI = " <" + Id3Tag.NAMESPACE + "uri>";

  /** Used in queries */
  public final static String MULGARA_IS = " <" + Mulgara.NAMESPACE + "is>";

  /** Version URI for a version 1 ID3 tag */
  public static final URIReference ID3V1 = Mp3File.MULGARA_MP3_DOMAIN;

  /** Version URI for a version 2 ID3 tag */
  public static final URIReference ID3V2 = Mp3File.MULGARA_MP3_DOMAIN;


  /**
   * Returns the Resource URI for the ID3 tag's version (eg. Id3Tag.ID3V1).
   * @return URIReference never <code>null</code>
   */
  public URIReference getVersion();

  /**
   * Initializes the Id3Tag.
   * @param context Mp3Context
   */
  public void init(Mp3Context context);

  /**
   * Sets the Mp3 that the Id3Tag represents
   * @param mp3 Mp3File
   */
  public void setMp3File(Mp3File mp3);

  /**
   * Returns the Mp3 that the Id3Tag represents
   * @return Mp3File
   */
  public Mp3File getMp3File();

  /**
   * Returns all Id3 property-values for the Mp3 as an Iterator of Triples.
   *
   * @return ClosableIterator
   * @throws QueryException
   */
  public ClosableIterator getStatements() throws QueryException;

  /**
   * Returns all Id3 property-values for the Mp3 as an Iterator of Triples.
   *
   * @return ClosableIterator
   * @throws QueryException
   */
  public ClosableIterator getSchemaProperties() throws QueryException;

}
