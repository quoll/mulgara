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

package org.mulgara.demo.mp3;

// Java 2 standard packages
import java.net.URL;
import java.net.URI;

// JRDF
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.demo.mp3.id3.Id3Tag;
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.*;


/**
 * Represents an Mp3File (read-only).
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
public interface Mp3File extends Comparable<URIReference> {

  /** Used in queries to represent an Mp3 */
  public final static String MULGARA_MP3 = " <" + Mulgara.NAMESPACE + "mp3>";

  /** Used to represent the Mp3 file domain */
  public final static URIReference MULGARA_MP3_DOMAIN = new URIReferenceImpl(URI.
      create(Mulgara.NAMESPACE + "MP3"));

  /**
   * Initializes the Mp3File.
   * @param context Mp3Context
   */
  public void init(Mp3Context context);

  /**
   * Returns the Mp3File's metadata (Id3)
   *
   * @return Id3Tag
   * @throws QueryException
   * @param version URIReference (eg. Id3Tag.ID3V2)
   */
  public Id3Tag getId3Tag(URIReference version) throws QueryException;

  /**
   * Returns the Resource that the Mp3 represents
   * @return URIReference
   */
  public URIReference getResource();

  /**
   * Sets the Resource that the Mp3 represents
   *
   * @param mp3 URIReference
   */
  public void setResource(URIReference mp3);

  /**
   * Returns the Location of the Mp3File.
   *
   * @return URL
   * @throws IllegalStateException
   */
  public URL getURL() throws IllegalStateException;

}
