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
import java.io.*;
import java.net.*;
import java.util.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.query.rdf.*;
import org.mulgara.demo.mp3.config.FileSystemModel;
import org.mulgara.demo.mp3.config.Mp3ConfigXML;
import org.mulgara.demo.mp3.config.Mp3Model;
import org.mulgara.demo.mp3.config.SchemaFile;
import org.mulgara.demo.mp3.config.SchemaModel;

/**
 * Mp3Config implementation that can be loaded and persisted as an XML File.
 *
 * @created 2004-12-15
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
public class Mp3ConfigFile extends Mp3ConfigImpl {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(Mp3ConfigFile.class.
      getName());

  /** Generated XML Config reader */
  private Mp3ConfigXML config = null;

  /** List of schema Files to be returned by getSchemaFiles */
  private List<URIReference> schemaFiles = null;

  /**
   * Deafult Constructor.
   *
   * @throws Exception
   */
  public Mp3ConfigFile() throws Exception {
    schemaFiles = new ArrayList<URIReference>();
  }

  /**
   * Constructor. If the supplied URL does not exist, it is created when the
   * config is saved. Otherwise the config is loaded from it.
   *
   * @param url URL where to load/save the config.
   * @throws Exception
   */
  public Mp3ConfigFile(URL url) throws Exception {
    this();
    if (url == null) {
      throw new IllegalArgumentException("File is null");
    }
    load(url);
  }

  /**
   * If the file exists. The configuration is loaded from the file.
   *
   * @throws Exception
   * @param url URL
   */
  private void load(URL url) throws Exception {
    if (url == null) {
      throw new IllegalArgumentException("URL is null.");
    }
    InputStream stream = url.openStream();
    try {
      config = loadXML(new InputStreamReader(stream));
      loadSchemaFiles();
      // read properties
      String fsModel = config.getFileSystemModel().getResource();
      String fsModelType = config.getFileSystemModel().getType();
      String schemaModel = config.getSchemaModel().getResource();
      String schemaModelType = config.getSchemaModel().getType();
      String mp3Model = config.getMp3Model().getResource();
      String mp3ModelType = config.getMp3Model().getType();
      //set properties
      super.setFileSystemModel(toResource(fsModel), toResource(fsModelType));
      super.setSchemaModel(toResource(schemaModel), toResource(schemaModelType));
      super.setMp3Model(toResource(mp3Model), toResource(mp3ModelType));
    }
    finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Gets all declared SchemaFiles and adds them to the List.
   *
   * @throws Exception
   */
  private void loadSchemaFiles() throws Exception {
    if (config == null) {
      throw new IllegalStateException("Config File has not been loaded.");
    }
    SchemaFile[] schemas = config.getSchemaModel().getSchemaFile();
    if ((schemas != null)
        && (schemas.length > 0)) {
      for (int i = 0; i < schemas.length; i++) {
        schemaFiles.add(toResource(schemas[i].getResource()));
      }
    }
  }

  /**
   * Converts a String to an URIReference.
   * @param uriString String
   * @throws Exception
   * @return URIReference
   */
  private URIReference toResource(String uriString) throws Exception {
    //Resource may be embedded in the jar  - try to get the resource first
    URL url = getClass().getResource(uriString);
    URI resource = null;
    try {
      if (url != null) {
        resource = new URI(url.toExternalForm());
      }
      else {
        resource = new URI(uriString);
      }
    }
    catch (Exception exception) {
      //last resort
      resource = new File(uriString).toURI();
    }
    assert(resource != null):"URI resource should never be null";
    return new URIReferenceImpl(resource);
  }

  /**
   * Reads the File and loads it's XML.
   *
   * @throws Exception
   * @return Mp3ConfigXML
   * @param reader Reader
   */
  private Mp3ConfigXML loadXML(Reader reader) throws Exception {
    Mp3ConfigXML config = Mp3ConfigXML.unmarshal(reader);
    return config;
  }

  /**
   * Saves the config to the file. Overwrites any existing file.
   *
   * @throws Exception
   * @param file File
   */
  public void save(File file) throws Exception {
    if (file.exists()) {
      file.delete();
    }
    file.createNewFile();
    updateConfig();
    Writer writer = new FileWriter(file);
    config.marshal(writer);
  }

  /**
   * Saves setting to the config object (overwrites any existing values).
   * @throws Exception
   */
  private void updateConfig() throws Exception {
    config = new Mp3ConfigXML();
    //models and types
    FileSystemModel fsModel = new FileSystemModel();
    SchemaModel schemaModel = new SchemaModel();
    Mp3Model mp3Model = new Mp3Model();
    fsModel.setResource(super.getFileSystemModel().getURI().toString());
    fsModel.setType(super.getFileSystemModelType().getURI().toString());
    mp3Model.setResource(super.getMp3Model().getURI().toString());
    mp3Model.setType(super.getMp3ModelType().getURI().toString());
    schemaModel.setResource(super.getSchemaModel().getURI().toString());
    schemaModel.setType(super.getSchemaModelType().getURI().toString());
    //add schema Files
    URIReference current = null;
    SchemaFile newSchema = null;
    for (int i = 0; i < schemaFiles.size(); i++) {
      current = (URIReference) schemaFiles.get(i);
      newSchema = new SchemaFile();
      newSchema.setResource(current.getURI().toString());
      schemaModel.addSchemaFile(newSchema);
    }
    config.setFileSystemModel(fsModel);
    config.setMp3Model(mp3Model);
    config.setSchemaModel(schemaModel);
  }

  /**
   * Returns the schema Files that belong to the config.
   * @throws IllegalStateException
   * @return List
   */
  public List<URIReference> getSchemaFiles() throws IllegalStateException {
    if (schemaFiles == null) {
      throw new IllegalStateException("Schema Files do not exist.");
    }
    return Collections.unmodifiableList(schemaFiles);
  }

  /**
   * Adds the schema file to the list of schema files.
   * @param resource URIReference
   * @throws IllegalArgumentException
   */
  public void addSchemaFile(URIReference resource) throws
      IllegalArgumentException {
    if (resource == null) {
      throw new IllegalArgumentException("'resource' is null");
    }
    if (schemaFiles == null) {
      schemaFiles = new ArrayList<URIReference>();
    }
    schemaFiles.add(resource);
  }

  /**
   * Returns the configured Application name.
   * @throws IllegalStateException
   * @return String
   */
  public String getApplicationName() throws IllegalStateException {
    return config.getApplicationName();
  }

}
