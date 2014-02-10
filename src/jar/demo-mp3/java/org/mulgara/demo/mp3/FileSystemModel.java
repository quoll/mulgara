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
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.Triple;

// Third party packages

//JRDF
import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.URIReference;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;

/**
 * Methods for querying/managing metadata for files in the filesystem.
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
public interface FileSystemModel extends Model {

  /** Predicate used to identify a File extension */
  public final static URIReference FILE_EXT = new URIReferenceImpl(URI.create(
      Mulgara.NAMESPACE + "extension"));

  /** Namespace used in MULGARA filesystem domain */
  public final static String MULGARA_FS = "http://mulgara.org/mulgara/filesystem#";

  /** Used to include directories */
  public final static String MULGARA_INCLUDE = " <" + MULGARA_FS + "Include>";

  /** Used to exclude directories */
  public final static String MULGARA_EXCLUDE = " <" + MULGARA_FS + "Exclude>";


  /**
   * Adds the directory to the list of directories to search.
   *
   * @param directory URL
   * @throws QueryException
   */
  public void includeDirectory(URL directory) throws QueryException;

  /**
   * Removes the directory from the list of directories to search.
   *
   * @param directory URL
   * @throws QueryException
   */
  public void excludeDirectory(URL directory) throws QueryException;

  /**
   * Returns an Iterator containing all the Files (as URIReferenece's) that have
   * the specified metadata property/value.
   *
   * @param property PredicateNode metadata predicate (eg. "mulgara:extension")
   * @param value ObjectNode metadata object (eg. "mp3")
   * @return ClosableIterator
   * @throws QueryException
   */
  public ClosableIterator<Triple> findFiles(PredicateNode property,
      ObjectNode value) throws QueryException;

}
