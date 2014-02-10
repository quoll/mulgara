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

package org.mulgara.resolver.filesystem;

// Java 2 standard packages
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.mulgara.resolver.filesystem.exception.VocabularyException;

/**
 * Creates a library of URIs representing the file system schema.
 *
 * @created 2004-11-18
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:27 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileSystemVocab {

  /** Class keys */
  public static final String FILE_TYPE = "File";
  public static final String DIRECTORY_TYPE = "Directory";
  public static final String HIDDEN_TYPE = "Hidden";
  public static final String READ_ONLY_TYPE = "ReadOnly";

  /** Property keys */
  public static final String FILENAME = "filename";
  public static final String EXTENSION = "extension";
  public static final String PATH = "path";
  public static final String CREATED = "created";
  public static final String ACCESSED = "accessed";
  public static final String MODIFIED = "modified";
  public static final String PARENT = "parent";
  public static final String SIZE = "size";
  public static final String OWNER = "owner";
  public static final String CAN_READ = "canRead";
  public static final String CAN_WRITE = "canWrite";
  public static final String CAN_EXECUTE = "canExecute";

  private static HashMap<String,URI> dictionary;

  /**
   * Does a lookup of the key against the hash map to find the required
   * predicate.
   *
   * @param key The predicate key
   *
   * @return The predicate URI
   *
   * @throws VocabularyException
   */
  public static URI getPredicate(String key) throws VocabularyException {

    if (dictionary == null) {

      // If we haven't initialised the dictionary then initialise it
      initialise();
    }

    // Retrieve the predicate by key
    URI predicateURI = dictionary.get(key);

    if (predicateURI == null) {

      // A null predicate URI means that we have used an invalid key so report
      // it
      throw new VocabularyException("Invalid key used when retrieving file " +
                                    "system predicate.");
    }

    return predicateURI;
  }

  /**
   * Initialises the predicate map.
   *
   * @throws VocabularyException
   */
  private static void initialise() throws VocabularyException {

    // Initialise the has map
    dictionary = new HashMap<String,URI>();

    // Create a base URI
    String baseURI = "http://mulgara.org/mulgara#";

    try {

      // Store the class reference key/value pairs
      dictionary.put(FILE_TYPE, new URI(baseURI + "File"));
      dictionary.put(DIRECTORY_TYPE, new URI(baseURI + "Directory"));
      dictionary.put(HIDDEN_TYPE, new URI(baseURI + "Hidden"));
      dictionary.put(READ_ONLY_TYPE, new URI(baseURI + "ReadOnly"));

      // Store the property reference key/value pairs
      dictionary.put(FILENAME, new URI(baseURI + "filename"));
      dictionary.put(EXTENSION, new URI(baseURI + "extension"));
      dictionary.put(PATH, new URI(baseURI + "path"));
      dictionary.put(CREATED, new URI(baseURI + "created"));
      dictionary.put(ACCESSED, new URI(baseURI + "accessed"));
      dictionary.put(MODIFIED, new URI(baseURI + "modified"));
      dictionary.put(PARENT, new URI(baseURI + "parent"));
      dictionary.put(SIZE, new URI(baseURI + "size"));
      dictionary.put(OWNER, new URI(baseURI + "owner"));
      dictionary.put(CAN_READ, new URI(baseURI + "canRead"));
      dictionary.put(CAN_WRITE, new URI(baseURI + "canWrite"));
      dictionary.put(CAN_EXECUTE, new URI(baseURI + "canExecute"));
    } catch (URISyntaxException uriSyntaxException) {

      // This should not occur because the URIs are hardcoded, but we should
      // still throw an exception just to be safe
      throw new VocabularyException ("Failed to initialise predicate map for " +
                                     "file systems", uriSyntaxException);
    }
  }
}
