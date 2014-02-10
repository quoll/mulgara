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
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.query.LocalNode;
import org.mulgara.query.TuplesException;
import org.mulgara.query.QueryException;

public class PropertyBridgeElem extends D2RQDefn {
  public final String property;
  public final List<String> join;
  public final String alias;
  public final String valueMaxLength;
  public final String valueContains;
  public final List<String> condition;
  public final TranslationTableElem translateWith;

  public PropertyBridgeElem(Resolver resolver, ResolverSession session, long propMap, long defModel, D2RQDefn parent) throws LocalizeException, QueryException, TuplesException, GlobalizeException {
    super(resolver, session, parent);
    LocalNode map = new LocalNode(propMap);
    LocalNode model = new LocalNode(defModel);

    property = getStringObject(map, Constants.property, model, false);
    join = getStringObjects(map, Constants.join, model);
    alias = getStringObject(map, Constants.alias, model, true);
    valueMaxLength = getStringObject(map, Constants.valueMaxLength, model, true);
    valueContains = getStringObject(map, Constants.valueContains, model, true);
    condition = getStringObjects(map, Constants.condition, model);

    LocalNode ttable = getLocalNodeObject(map, Constants.translateWith, model, true);
    translateWith = (ttable != null) ? new TranslationTableElem(resolver, session, ttable, defModel) : null;
  }

  public String toString() {
    return "(property=" + property +
           ", join=" + join +
           ", alias=" + alias +
           ", valueMaxLength=" + valueMaxLength +
           ", valueContains=" + valueContains +
           ", condition=" + condition +")";
  }
}
