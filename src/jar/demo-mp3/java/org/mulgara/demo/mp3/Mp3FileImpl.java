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
import java.net.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.demo.mp3.id3.*;
import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.QueryException;

/**
 * Mp3File implementation.
 *
 * @created 2004-12-06
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
public class Mp3FileImpl implements Mp3File {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(Mp3FileImpl.class.getName());

  /** Mp3File metadata */
  private Id3Tag id3 = null;

  /** Resource being represented */
  private URIReference resource = null;

  /** Used to execute iTQL queries */
  private ItqlInterpreterBean bean = null;

  /** Graph where the Mp3 metadata is stored */
  private Mp3Model model = null;

  /** Graph where schema information is stored */
  private SchemaModel schemaModel = null;

  /** Used to determine if this Mp3 is ready first use. */
  private boolean initialized = false;

  /**
   * Initializes the Mp3File.
   * @param context Mp3Context
   */
  public void init(Mp3Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Mp3Context is null.");
    }
    bean = context.getBean();
    model = context.getMp3Model();
    schemaModel = context.getSchemaModel();
    //validate
    if (bean == null) {
      throw new IllegalArgumentException("Mp3Context returned a null " +
          "ItqlInterpreterBean.");
    }
    if (model == null) {
      throw new IllegalArgumentException("Mp3Context returned a null Mp3Model.");
    }
    if (schemaModel == null) {
      throw new IllegalArgumentException("Mp3Context returned a null " +
          "SchemaModel.");
    }
  }

  /**
   * Returns the Mp3File's metadata (Id3)
   *
   * @return Id3Tag
   * @throws QueryException
   * @param version URIReference
   */
  public Id3Tag getId3Tag(URIReference version) throws QueryException {
    checkInitialized();
    //lazily generate Id3 tag
    try {
      if ((id3 == null)
          || !(id3.getVersion().equals(version))) {
        id3 = generateId3Tag(version);
      }
      return id3;
    }
    catch (Exception exception) {
      throw new QueryException("Failed to generate Id3 Tag.", exception);
    }
  }

  /**
   * Returns the Resource that the Mp3 represents
   * @return URIReference
   */
  public URIReference getResource() {
    checkInitialized();
    return resource;
  }

  /**
   * Sets the Resource that the Mp3 represents
   *
   * @param mp3 URIReference
   */
  public void setResource(URIReference mp3) {
    if (mp3 == null) {
      throw new IllegalArgumentException("'mp3' is null.");
    }
    resource = mp3;
  }

  /**
   * Returns the Location of the Mp3File.
   *
   * @return URL
   * @throws IllegalStateException
   */
  public URL getURL() throws IllegalStateException {
    checkInitialized();
    try {
      URI uri = resource.getURI();
      return uri.toURL();
    }
    catch (MalformedURLException urlException) {
      throw new IllegalStateException("Resource is not a valid URL.");
    }
  }

  /**
   * Returns the Id3Tag for this Mp3 file.
   *
   * @throws Exception
   * @return Id3Tag
   * @param version URIReference
   */
  private Id3Tag generateId3Tag(URIReference version) throws Exception {
    checkInitialized();
    //determine id3 tag version and instantiate
    Id3Tag id3Tag = null;
    if (Id3Tag.ID3V1.equals(version)) {
      id3Tag = new Id3v1Tag();
    }
    else if (Id3Tag.ID3V2.equals(version)) {
      id3Tag = new Id3v2Tag();
    }
    else {
      throw new IllegalArgumentException("Unknown Id3 version: " + version);
    }
    //init and return
    id3Tag.setMp3File(this);
    id3Tag.init(newMp3Context());
    return id3Tag;
  }

  /**
   * Creates and returns a new Mp3Context.
   * @return Mp3Context
   */
  private Mp3Context newMp3Context() {
    Mp3ContextImpl context = new Mp3ContextImpl();
    context.setBean(bean);
    context.setMp3Model(model);
    context.setSchemaModel(schemaModel);
    return context;
  }

  /**
   * Throws an exception if the Mp3File is not initialized.
   * @throws IllegalStateException
   */
  private void checkInitialized() throws IllegalStateException {
    if (!initialized) {
      if (hasInitialized()) {
        initialized = true;
      }
      else {
        throw new IllegalStateException("Mp3File has not been Initialized.");
      }
    }
  }

  /**
   * Returns true if the Mp3File has been initialized.
   * @return boolean
   */
  private boolean hasInitialized() {
    return (resource != null)
        && (bean != null)
        && (model != null)
        && (schemaModel != null);
  }

  /**
   * Returns a String describing the Resource.
   * @return String
   */
  public String toString() {
    return getResource().toString();
  }

  /**
   * Compares the Mp3File Resource to another object
   *
   * @param obj Object
   * @return int
   */
  @SuppressWarnings("unchecked")
  public int compareTo(URIReference obj) {
    URIReference resource = getResource();
    if ((obj == null)
        || !(obj instanceof Mp3File)
        || !(resource instanceof Comparable)) {
      return -1;
    }
    Mp3File mp3File = (Mp3File) obj;
    return ((Comparable<Node>)resource).compareTo(mp3File.getResource());
  }

}
