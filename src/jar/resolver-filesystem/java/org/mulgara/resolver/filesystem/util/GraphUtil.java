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

package org.mulgara.resolver.filesystem.util;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.jrdf.graph.*;
import org.jrdf.vocabulary.RDF;

import org.mulgara.resolver.filesystem.exception.MetaDataException;

/**
 * A set of methods that can be statically invoked to allow for operations on
 * JRDF graphs.
 *
 * @created 2004-11-26
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:28 $ @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class GraphUtil {

  /**
   * Creates a predicate node using the given string as a lookup in the
   * dictionary.
   *
   * @param key The key into the dictionary
   * @param elementFactory The factory for creating nodes
   *
   * @return The predicate as a node
   *
   * @throws MetaDataException
   */
  public static PredicateNode createPredicate(String key,
                                              GraphElementFactory
                                              elementFactory) throws
      MetaDataException {

    try {

      // Create the type node
      return (PredicateNode) elementFactory.createResource(
          RDF.TYPE);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new MetaDataException("Failed to create predicate node for key: " +
                                  key,
                                  graphElementFactoryException);
    }
  }

  /**
   * Creates a predicate node using the given given uri.
   *
   * @param reference The URI reference to use as the predicate
   * @param elementFactory The factory for creating nodes
   *
   * @return The predicate as a node
   *
   * @throws MetaDataException
   */
  public static PredicateNode createPredicateFromURI(URI reference,
      GraphElementFactory
      elementFactory) throws
      MetaDataException {

    try {

      // Create the type node
      return (PredicateNode) elementFactory.createResource(reference);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new MetaDataException("Failed to create predicate node for URI " +
                                  reference.toString(),
                                  graphElementFactoryException);
    }
  }

  /**
   * Creates an object node using a resource URI.
   *
   * @param value The resource URI
   * @param elementFactory The factory for creating nodes
   *
   * @return The object node
   *
   * @throws MetaDataException
   */
  public static ObjectNode createObjectResource(URI value,
                                                GraphElementFactory
                                                elementFactory) throws
      MetaDataException {

    try {

      // The default type is file
      return (ObjectNode) elementFactory.createResource(value);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new MetaDataException("Failed to create object node for " +
                                  value.toString(),
                                  graphElementFactoryException);
    }
  }

  /**
   * Creates an object node using a literal value.
   *
   * @param value The value of the literal
   * @param elementFactory The factory for creating nodes
   *
   * @return The object node representing the literal
   *
   * @throws MetaDataException
   */
  public static ObjectNode createObjectLiteral(String value,
                                               GraphElementFactory
                                               elementFactory) throws
      MetaDataException {

    try {

      // The default type is file
      return (ObjectNode) elementFactory.createLiteral(value);
    } catch (GraphElementFactoryException graphElementFactoryException) {

      throw new MetaDataException("Failed to create object node for " +
                                  value, graphElementFactoryException);
    }
  }

  /**
   * Adds the given triple to the graph.
   *
   * @param subject The subject node
   * @param predicate The predicate node
   * @param object The object node
   * @param graph The graph we are adding the triple to
   *
   * @throws MetaDataException
   */
  public static void addTriple(Node subject, Node predicate, Node object,
                               Graph graph) throws MetaDataException {

    try {

      // Add the type triple
      graph.add((SubjectNode) subject, (PredicateNode) predicate,
                (ObjectNode) object);
    } catch (GraphException graphException) {

      throw new MetaDataException("Failed to add triple for predicate: " +
                                  predicate, graphException);
    }
  }
}
