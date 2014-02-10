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

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.jrdf.graph.URIReference;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.QueryException;
import org.mulgara.query.LocalNode;


public class DatabaseElem extends D2RQDefn {
  public final String jdbcDSN;
  public final String jdbcDriver;
  public final String username;
  public final String password;
  public final List<String> numericColumns;
  public final List<String> textColumns;
  public final List<String> dateColumns;
  public final DBType dbType;

  public enum DBType { oracle, other };

  public DatabaseElem(Resolver resolver, ResolverSession session, long rdftype, long defModel) throws LocalizeException, QueryException, TuplesException, GlobalizeException {
    super(resolver, session);
    long[] dbs = getSubjects(rdftype, session.lookup(Constants.database), defModel);
    if (dbs.length == 0) {
      throw new TuplesException("No database definition found");
    } else if (dbs.length != 1) {
      throw new TuplesException("Multiple database definitions not currently supported");
    }

    LocalNode database = new LocalNode(dbs[0]);
    LocalNode model = new LocalNode(defModel);

    jdbcDSN = getStringObject(database, Constants.jdbcDSN, model, false);
    jdbcDriver = getStringObject(database, Constants.jdbcDriver, model, false);
    username = getStringObject(database, Constants.username, model, true);
    password = getStringObject(database, Constants.password, model, true);
    numericColumns = getStringObjects(database, Constants.numericColumn, model);
    textColumns = getStringObjects(database, Constants.textColumn, model);
    dateColumns = getStringObjects(database, Constants.dateColumn, model);
    dbType = jdbcDriver.toLowerCase().contains("oracle") ? DBType.oracle : DBType.other;

    Map<String,URIReference> typeMap = new HashMap<String,URIReference>();
    // "_" is a dummy anonymous column to describe the types of literals within a query (always text)
    typeMap.put("_", Constants.textColumn);
    for (String nc: numericColumns) typeMap.put(nc, Constants.numericColumn);
    for (String nc: textColumns) typeMap.put(nc, Constants.textColumn);
    for (String nc: dateColumns) typeMap.put(nc, Constants.dateColumn);
    initColumnTypeMap(typeMap);
  }
}
