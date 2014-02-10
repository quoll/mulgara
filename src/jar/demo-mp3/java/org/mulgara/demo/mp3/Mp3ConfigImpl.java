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

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.URIReference;

// Local packages



/**
 * Mp3Config implementation.
 *
 * @created 2004-12-08
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
public class Mp3ConfigImpl implements Mp3Config {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(Mp3ConfigImpl.class.
      getName());

  /** Returned by getMp3Model() */
  private URIReference mp3Model = null;

  /** Returned by getMp3ModelType() */
  private URIReference mp3ModelType = null;

  /** Returned by getSchemaModel() */
  private URIReference schemaModel = null;

  /** Returned by getSchemaModelType() */
  private URIReference schemaModelType = null;

  /** Returned by getFileSystemModel() */
  private URIReference fileSystemModel = null;

  /** Returned by getFileSystemModelType() */
  private URIReference fileSystemModelType = null;

  /**
   * Returns the Mp3Model Resource.
   * @throws IllegalStateException
   * @return URIReference
   */
  public URIReference getMp3Model() throws IllegalStateException {
    if (mp3Model == null) {
      throw new IllegalStateException("Mp3Model Resource has not been set.");
    }
    return mp3Model;
  }

  /**
   * Returns the SchemaModel Resource.
   * @throws IllegalStateException
   * @return URIReference
   */
  public URIReference getSchemaModel() throws IllegalStateException {
    if (schemaModel == null) {
      throw new IllegalStateException("SchemaModel Resource has not been set.");
    }
    return schemaModel;
  }

  /**
   * Returns the FileSystemModel Resource.
   * @throws IllegalStateException
   * @return URIReference
   */
  public URIReference getFileSystemModel() throws IllegalStateException {
    if (fileSystemModel == null) {
      throw new IllegalStateException(
          "FileSystemModel Resource has not been set.");
    }
    return fileSystemModel;
  }

  /**
   * Returns the Mp3Model type. Can be <code>null</code>.
   * @return URIReference
   */
  public URIReference getMp3ModelType() {
    return mp3ModelType;
  }

  /**
   * Returns the SchemaModel type. Can be <code>null</code>.
   * @return URIReference
   */
  public URIReference getSchemaModelType() {
    return schemaModelType;
  }

  /**
   * Returns the FileSystemModel type. Can be <code>null</code>.
   * @return URIReference
   */
  public URIReference getFileSystemModelType() {
    return fileSystemModelType;
  }


  /**
   * Sets the Mp3Model Resource returned by getMp3Model().
   *
   * @param resource Mp3Model
   * @param type URIReference
   * @throws IllegalArgumentException
   */
  public void setMp3Model(URIReference resource, URIReference type) throws IllegalArgumentException {
    if (resource == null) {
      throw new IllegalArgumentException("'resource' is null.");
    }
    mp3Model = resource;
    mp3ModelType = type;
  }

  /**
   * Sets the SchemaModel Resource returned by getSchemaModel().
   *
   * @param resource URIReference
   * @param type URIReference
   * @throws IllegalArgumentException
   */
  public void setSchemaModel(URIReference resource, URIReference type) throws IllegalArgumentException {
    if (resource == null) {
      throw new IllegalArgumentException("'resource' is null.");
    }
    schemaModel = resource;
    schemaModelType = type;
  }

  /**
   * Sets the FileSystemModel Resource returned by getFileSystemModel().
   *
   * @param resource URIReference
   * @param type URIReference
   * @throws IllegalArgumentException
   */
  public void setFileSystemModel(URIReference resource, URIReference type) throws IllegalArgumentException {
    if (resource == null) {
      throw new IllegalArgumentException("'resource' is null.");
    }
    fileSystemModel = resource;
    fileSystemModelType = type;
  }

}
