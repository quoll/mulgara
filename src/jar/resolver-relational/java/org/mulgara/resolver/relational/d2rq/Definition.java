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

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

public class Definition {

  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(Definition.class);

  public DatabaseElem databaseDefn;
  public Map<String,ClassMapElem> classMaps;  // Map of class-type to class-defn.
  public Map<LocalNode,ClassMapElem> nodeClassMaps;  // Map of classMap node to class-defn.
  public Map<String,Map<String,ObjectPropertyBridgeElem>> objPropBridges;  // Map of class-type to list objectPropertyMaps
  public Map<String,Map<String,DatatypePropertyBridgeElem>> dataPropBridges;  // Map of class-type to list datatypePropertyMaps

  public Definition(Resolver resolver, ResolverSession session, long rdftype, long defModel) throws QueryException {
    try {
      databaseDefn = new DatabaseElem(resolver, session, rdftype, defModel);

      Variable subj = new Variable("cms");
      LocalNode type = new LocalNode(rdftype);
      LocalNode model = new LocalNode(defModel);
      LocalNode classMap = new LocalNode(session.localize(Constants.TypeClassMap));

      Resolution maps = resolver.resolve(new ConstraintImpl(subj, type, classMap, model));
      this.classMaps = new HashMap<String,ClassMapElem>();
      this.objPropBridges = new HashMap<String,Map<String,ObjectPropertyBridgeElem>>();
      this.dataPropBridges = new HashMap<String,Map<String,DatatypePropertyBridgeElem>>();
      this.nodeClassMaps = new HashMap<LocalNode,ClassMapElem>();
      maps.beforeFirst();
      while (maps.next()) {
        long map = maps.getColumnValue(0);
        ClassMapElem cmap = new ClassMapElem(resolver, session, map, defModel, databaseDefn);
        if (cmap.klass == null) {
          throw new QueryException("ClassMaps require a rdfs:Class");
        }
        classMaps.put(cmap.klass, cmap);
        nodeClassMaps.put(new LocalNode(map), cmap);
// FIXME: Maintain map of map-id's to maps, complete loop and then reloop over classmaps to populate bridges.
//        This is required to ensure refersToClassMap works.

      }
      maps.close();

      for (LocalNode map: nodeClassMaps.keySet()) {
        ClassMapElem cmap = (ClassMapElem)nodeClassMaps.get(map);

        populateObjPropBridges(resolver, session, subj, type, model, map, cmap.klass, nodeClassMaps);
        populateDataPropBridges(resolver, session, subj, type, model, map, cmap.klass);
      }
    } catch (LocalizeException el) {
      throw new QueryException("Error localizing constant", el);
    } catch (TuplesException et) {
      throw new QueryException("Error querying definition", et);
    } catch (GlobalizeException eg) {
      throw new QueryException("Error querying for definition", eg);
    }
  }

  void populateObjPropBridges(Resolver resolver, ResolverSession session,
      Variable subj, LocalNode type, LocalNode model, LocalNode map, String klass, Map<LocalNode,ClassMapElem> nodeClassMaps)
      throws LocalizeException, QueryException, TuplesException, GlobalizeException {
    Map<String,ObjectPropertyBridgeElem> pmap = objPropBridges.get(klass);
    if (pmap == null) {
      pmap = new HashMap<String,ObjectPropertyBridgeElem>();
      objPropBridges.put(klass, pmap);
    }

    LocalNode belongs = new LocalNode(session.localize(Constants.belongsToClassMap));
    LocalNode propBridge = new LocalNode(session.localize(Constants.TypeObjectPropertyBridge));
    Tuples lhs = resolver.resolve(new ConstraintImpl(subj, belongs, map, model));
    Tuples rhs = resolver.resolve(new ConstraintImpl(subj, type, propBridge, model));
    Tuples props;
    try {
      props = TuplesOperations.join(lhs, rhs);
    } finally {
      try {
        lhs.close();
      } finally {
        rhs.close();
      }
    }
    props.beforeFirst();
    while (props.next()) {
      long prop = props.getColumnValue(0);
      ObjectPropertyBridgeElem pbridge = new ObjectPropertyBridgeElem(resolver, session, prop, model.getValue(), nodeClassMaps, databaseDefn);
      pmap.put(pbridge.property, pbridge);
    }
    props.close();
  }

  void populateDataPropBridges(Resolver resolver, ResolverSession session,
      Variable subj, LocalNode type, LocalNode model, LocalNode map, String klass)
      throws LocalizeException, QueryException, TuplesException, GlobalizeException {
    Map<String,DatatypePropertyBridgeElem> pmap = dataPropBridges.get(klass);
    if (pmap == null) {
      pmap = new HashMap<String,DatatypePropertyBridgeElem>();
      dataPropBridges.put(klass, pmap);
    }

    LocalNode belongs = new LocalNode(session.localize(Constants.belongsToClassMap));
    LocalNode propBridge = new LocalNode(session.localize(Constants.TypeDatatypePropertyBridge));
    Tuples lhs = resolver.resolve(new ConstraintImpl(subj, belongs, map, model));
    Tuples rhs = resolver.resolve(new ConstraintImpl(subj, type, propBridge, model));
    Tuples props;
    try {
      props = TuplesOperations.join(lhs, rhs);
    } finally {
      lhs.close();
      rhs.close();
    }
    props.beforeFirst();
    while (props.next()) {
      long prop = props.getColumnValue(0);
      DatatypePropertyBridgeElem pbridge = new DatatypePropertyBridgeElem(resolver, session, prop, model.getValue(), databaseDefn);
      pmap.put(pbridge.property, pbridge);
    }
    props.close();
  }
    
}
