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
import java.util.Iterator;

// Third party packages

//JRDF
import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.Literal;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.query.rdf.LiteralImpl;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.ObjectNode;

/**
 * Methods for querying/managing mp3 metadata.
 *
 * @created 2004-12-03
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.4 $
 *
 * @modified $Date: 2005/04/20 19:02:50 $
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
public interface Mp3Model extends Model {

  /** mp3 file extension used to identify a mp3 file */
  public final static Literal MP3_EXT = new LiteralImpl("mp3");

  /**
   * Discover mp3's from the FileSystemModel and loads them.
   *
   * @param fsModel FileSystemModel
   * @throws QueryException
   * @throws IllegalArgumentException
   */
  public void loadMp3s(FileSystemModel fsModel) throws
      QueryException, IllegalArgumentException;

  /**
   * Sets the Graph used to store Schema Information.
   * @param schemaModel SchemaModel
   * @throws IllegalArgumentException
   */
  public void setSchemaModel(SchemaModel schemaModel) throws
      IllegalArgumentException;

  /**
   * Returns the Graph used to store Schema Information.
   * @return SchemaModel
   */
  public SchemaModel getSchemaModel();

  /**
   * Returns all Mp3Files from the filesystem (FileSystemModel)
   *
   * @return Mp3Iterator
   * @throws QueryException
   */
  public Mp3Iterator getMp3s() throws QueryException;

  /**
   * Returns an Iterator (of Triples) that match any of the properties for the
   * specified mp3.
   *
   * @param mp3 URIReference
   * @param properties Iterator
   * @return ClosableIterator
   * @throws QueryException
   */
  public ClosableIterator<Triple> getPropertyStatements(URIReference mp3,
      Iterator<Triple> properties) throws QueryException;

  /**
   * Returns an Iterator containing all the Mp3s (as URIReferenece's) that have
   * the specified metadata properties and values. Empty/null constraints will
   * return all mp3s in the model.
   *
   * @param properties Iterator metadata predicates (eg. "mulgara:title")
   * @param values Iterator metadata objects (eg. "Vertigo")
   * @return Mp3Iterator
   * @throws QueryException
   */
  public Mp3Iterator findMp3s(Iterator<PredicateNode> properties,
      Iterator<ObjectNode> values) throws QueryException;
  
  /**
   * Sets the listener that is notified of Mp3Model events.
   * 
   * @param listener
   */
  public void setMp3ModelListener(Mp3ModelListener listener);  

}

