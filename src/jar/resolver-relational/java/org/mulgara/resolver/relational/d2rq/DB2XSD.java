/*
 * Copyright 2010 Paul Gearon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.resolver.relational.d2rq;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.mulgara.query.rdf.XSD;

import static java.sql.Types.*;

/**
 * Provides a mapping function between JDBC data types and XSD types.
 *
 * @created Jun 9, 2010
 * @author Paul Gearon
 */
public class DB2XSD {

  /** The mapping structure keyed on the integers found in java.sql.Types */
  private static final Map<Integer,URI> typeMap = new HashMap<Integer,URI>();

  static {
    typeMap.put(DECIMAL, XSD.DOUBLE_URI);
    typeMap.put(DOUBLE, XSD.DOUBLE_URI);
    typeMap.put(FLOAT, XSD.FLOAT_URI);
    typeMap.put(INTEGER, XSD.INT_URI);
    typeMap.put(SMALLINT, XSD.INT_URI);
    typeMap.put(TINYINT, XSD.INT_URI);
    typeMap.put(DATE, XSD.DATE_URI);
    typeMap.put(TIME, XSD.DATE_URI);
    typeMap.put(TIMESTAMP, XSD.DATE_URI);
  }

  /**
   * Retrieves a URI from the XSD namespace that can hold the data represented
   * in JDBC. The JDBC type is identified by an integer from {@link java.sql.Types}
   * @param dbt The database type identifier
   * @return The URI of the required XSD type or <code>null</code> if none is found. 
   */
  public static URI get(int dbt) {
    return typeMap.get(dbt);
  }
}
