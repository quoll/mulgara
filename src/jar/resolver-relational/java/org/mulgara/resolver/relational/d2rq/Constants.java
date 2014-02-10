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
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */
package org.mulgara.resolver.relational.d2rq;

import java.net.URI;
import java.net.URISyntaxException;

import org.mulgara.query.rdf.URIReferenceImpl;


public class Constants {
  public static final String prefix = "http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#";

  // d2rq:Database properties
  public static final URIReferenceImpl database = ref("Database");
  public static final URIReferenceImpl jdbcDSN = ref("jdbcDSN");
  public static final URIReferenceImpl jdbcDriver = ref("jdbcDriver");
  public static final URIReferenceImpl username = ref("username");
  public static final URIReferenceImpl password = ref("password");
  public static final URIReferenceImpl textColumn = ref("textColumn");
  public static final URIReferenceImpl numericColumn = ref("numericColumn");
  public static final URIReferenceImpl dateColumn = ref("dateColumn");
  public static final URIReferenceImpl allowDistinct = ref("allowDistinct");

  // d2rq:classMap properties
  public static final URIReferenceImpl TypeClassMap = ref("ClassMap");

  public static final URIReferenceImpl klass = ref("class");
  public static final URIReferenceImpl uriPattern = ref("uriPattern");
  public static final URIReferenceImpl uriColumn = ref("uriColumn");
  public static final URIReferenceImpl bNodeIdColumns = ref("bNodeIdColumns");
  public static final URIReferenceImpl translateWith = ref("translateWith");
  public static final URIReferenceImpl dataStorage = ref("dataStorage");
  public static final URIReferenceImpl containsDuplicates = ref("containsDuplicates");
  public static final URIReferenceImpl additionalProperty = ref("additionalProperty");
  public static final URIReferenceImpl condition = ref("condition");
  public static final URIReferenceImpl classMap = ref("classMap");

  // d2rq:propertyBridge properties
  public static final URIReferenceImpl TypeObjectPropertyBridge = ref("ObjectPropertyBridge");
  public static final URIReferenceImpl TypeDatatypePropertyBridge = ref("DatatypePropertyBridge");
  public static final URIReferenceImpl belongsToClassMap = ref("belongsToClassMap");
  public static final URIReferenceImpl property = ref("property");
  public static final URIReferenceImpl join = ref("join");
  public static final URIReferenceImpl alias = ref("alias");
  public static final URIReferenceImpl valueMaxLength = ref("valueMaxLength");
  public static final URIReferenceImpl valueContains = ref("valueContains");
  public static final URIReferenceImpl valueRegex = ref("valueRegex");

  public static final URIReferenceImpl propertyBridge = ref("propertyBridge");
  public static final URIReferenceImpl column = ref("column");
  public static final URIReferenceImpl pattern = ref("pattern");
  public static final URIReferenceImpl refersToClassMap = ref("refersToClassMap");
  public static final URIReferenceImpl datatype = ref("datatype");
  public static final URIReferenceImpl lang = ref("lang");

  public static final URIReferenceImpl propertyName = ref("propertyName");
  public static final URIReferenceImpl propertyValue = ref("propertyValue");

  // d2rq:translation properties
  public static final URIReferenceImpl translation = ref("translation");
  public static final URIReferenceImpl href = ref("href");
  public static final URIReferenceImpl javaClass = ref("javaClass");

  public static final URIReferenceImpl dbValue = ref("dbValue");
  public static final URIReferenceImpl rdfValue = ref("rdfValue");


  /**
   * Utility method to simplify initialising constants.
   */
  static URIReferenceImpl ref(String fragment) {
    try {
      return new URIReferenceImpl(new URI(prefix + fragment));
    } catch (URISyntaxException eu) {
      throw new IllegalStateException("Unable to initialise D2RQ Constants");
    }
  }
}
