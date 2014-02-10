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

package org.mulgara.store.stringpool.xa;

// Java 2 standard packages
import java.net.URI;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.store.stringpool.*;


/**
 * Manages mappings of type URIs to the factory that handles that type, and also typeIDs to factories
 * handling raw data with that ID.
 * 
 * @created 2004-09-30
 *
 * @author David Makepeace
 * @copyright &copy; 2004 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class SPTypedLiteralRegistry {

  private static final Logger logger = Logger.getLogger(SPTypedLiteralRegistry.class);

  // TODO the class names of these factories should be in a configuration file.
  private static final SPTypedLiteralFactory[] DATATYPE_HANDLERS = {
      new SPXSDStringFactory(),     // type ID: 1
      new SPDecimalFactory(),       // type ID: 2
      new SPFloatFactory(),         // type ID: 3
      new SPDoubleFactory(),        // type ID: 4
      new SPDateFactory(),          // type ID: 5
      new SPDateTimeFactory(),      // type ID: 6
      new SPGYearMonthFactory(),    // type ID: 7
      new SPGYearFactory(),         // type ID: 8
      new SPGMonthDayFactory(),     // type ID: 9
      new SPGDayFactory(),          // type ID: 10
      new SPGMonthFactory(),        // type ID: 11
      new SPBooleanFactory(),       // type ID: 12
      new SPBase64BinaryFactory(),  // type ID: 13
      new SPHexBinaryFactory(),     // type ID: 14
      new SPXMLLiteralFactory(),    // type ID: 15
  };
  // UnknownSPTypedLiteralImpl uses type ID: 127

  private static final int MAX_TYPE_ID = 127;

  private static final SPTypedLiteralFactory[] typeIdToFactory = new SPTypedLiteralFactory[MAX_TYPE_ID];

  // Maps URI objects to SPTypedLiteralFactory objects.
  private static final Map<URI,SPTypedLiteralFactory> typeURIToFactoryMap = new HashMap<URI,SPTypedLiteralFactory>();

  private static final SPTypedLiteralFactory unknownSPTypedLiteralFactory = new UnknownSPTypedLiteralFactory();


  static {
    initTypeURIToFactoryMap();
  }


  /**
   * Returns the Set of supported type URIs.
   */
  public static Set<URI> getTypeURIs() {
    return Collections.unmodifiableSet(typeURIToFactoryMap.keySet());
  }

  static SPTypedLiteralFactory getSPTypedLiteralFactory(URI typeURI) {
    if (typeURI == null)  throw new IllegalArgumentException("Parameter typeURI is null");

    // Look up the SPTypedLiteralFactory instance for this type URI.
    SPTypedLiteralFactory factory = (SPTypedLiteralFactory)typeURIToFactoryMap.get(typeURI);

    if (factory == null) factory = unknownSPTypedLiteralFactory;
    assert factory != null;
    return factory;
  }


  static SPTypedLiteralFactory getSPTypedLiteralFactory(int typeId) {
    if (typeId == SPObjectFactory.INVALID_TYPE_ID || typeId < 0 || typeId > MAX_TYPE_ID) {
      throw new IllegalArgumentException("Invalid typeId: " + typeId);
    }

    if (typeId == UnknownSPTypedLiteralImpl.TYPE_ID) return unknownSPTypedLiteralFactory;

    SPTypedLiteralFactory factory = typeIdToFactory[typeId];
    if (factory == null) factory = unknownSPTypedLiteralFactory;
    assert factory != null;
    return factory;
  }


  static void initTypeURIToFactoryMap() {
    // Iterate over the class names and construct each factory instance.
    for (SPTypedLiteralFactory factory: DATATYPE_HANDLERS) {
      assert factory != null;

      Set<URI> typeURIs = factory.getTypeURIs();
      assert typeURIs != null;
      assert !typeURIs.isEmpty();

      int typeId = factory.getTypeId();

      if (typeId < 0 || typeId == SPObjectFactory.INVALID_TYPE_ID) {
        logger.error("Invalid typeId for datatype handler: " + factory.getClass().getName() + ".  typeId:" + typeId);
        continue;
      }
      if (typeId >= MAX_TYPE_ID) {
        logger.error(
            "Invalid typeId for datatype handler: " +
            factory.getClass().getName() +
            ".  typeId (" + typeId + ") is greater than MAX_TYPE_ID (" +
            MAX_TYPE_ID + ").  Adjust the value of MAX_TYPE_ID and recompile."
        );
        continue;
      }

      // Check for duplicate typeIds.
      if (typeIdToFactory[typeId] != null) {
        logger.error("Duplicate typeId for datatype handler: " + factory.getClass().getName() + "  (typeId:" + typeId + ", typeURIs:" + typeURIs + ")");
        continue;
      }

      // Check for duplicate typeURIs.
      Set<URI> duplicates = new HashSet<URI>(typeURIs);
      duplicates.retainAll(typeURIToFactoryMap.keySet());
      if (!duplicates.isEmpty()) {
        logger.error(
            "Duplicate typeURIs (" + duplicates + ") for datatype handler: " +
            factory.getClass().getName() +
            "  (typeId:" + typeId + ", typeURIs:" + typeURIs + ")"
        );
        continue;
      }

      // Add the factory to the Maps.
      typeIdToFactory[typeId] = factory;
      for (URI uri: typeURIs) typeURIToFactoryMap.put(uri, factory);

      if (logger.isInfoEnabled()) {
        logger.info("Registered SPTypedLiteralFactory for: " + typeURIs + " (id:" + typeId + ")");
      }
    }
  }

}
