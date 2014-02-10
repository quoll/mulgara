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
package org.mulgara.resolver.relational;

import org.apache.log4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.jrdf.graph.URIReference;

import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintOperation;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.AbstractSymbolicTransformer;
import org.mulgara.resolver.spi.SymbolicTransformationContext;
import org.mulgara.resolver.spi.SymbolicTransformationException;

public class RelationalTransformer extends AbstractSymbolicTransformer {
  /** Logger */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RelationalTransformer.class);

  private URI modelTypeURI;

  public RelationalTransformer(URI modelTypeURI) {
    this.modelTypeURI = modelTypeURI;
  }

  @Override
  protected ConstraintExpression transformOperation(SymbolicTransformationContext context,
                                                    ConstraintOperation expr)
        throws SymbolicTransformationException {
    if (expr instanceof ConstraintConjunction)
      return transformConj(context, (ConstraintConjunction)expr);
    return super.transformOperation(context, expr);
  }

  @Override
  protected ConstraintExpression transformConstraint(SymbolicTransformationContext context, Constraint c) throws SymbolicTransformationException {
    if (c instanceof RelationalConstraint) return c;

    try {
      ConstraintElement ce = c.getModel();
      if (ce instanceof URIReference) {
        URIReference cu = (URIReference)ce;
        URI constraintModelType = context.mapToModelTypeURI(cu.getURI());
        if (constraintModelType != null && constraintModelType.equals(modelTypeURI)) {
          return new RelationalConstraint(c);
        }
      }
      return c;
    } catch (QueryException eq) {
      throw new SymbolicTransformationException("Failed to map model to model-type", eq);
    }
  }

  public ConstraintExpression transformConj(SymbolicTransformationContext context, ConstraintConjunction cc) throws SymbolicTransformationException {
    List<ConstraintExpression> retainedArgs = new ArrayList<ConstraintExpression>();
    Map<ConstraintElement,List<RelationalConstraint>> relationalArgs = new HashMap<ConstraintElement,List<RelationalConstraint>>();

    boolean transformed = false;

    for (ConstraintExpression arg: cc.getElements()) {
      if (arg instanceof RelationalConstraint) {
        RelationalConstraint rc = (RelationalConstraint)arg;
        List<RelationalConstraint> rcArgs = relationalArgs.get(rc.getModel());
        if (rcArgs == null) {
          rcArgs = new ArrayList<RelationalConstraint>();
          relationalArgs.put(rc.getModel(), rcArgs);
        }
        rcArgs.add(rc);
      } else {
        ConstraintExpression trans = transformExpression(context, arg);
        retainedArgs.add(trans);
        if (arg != trans) {
          transformed = true;
        }
      }
    }

    for (List<RelationalConstraint> rargl: relationalArgs.values()) {
      Iterator<RelationalConstraint> rarg = rargl.iterator();
      RelationalConstraint rc = null;
      if (rarg.hasNext()) rc = rarg.next();
      while (rarg.hasNext()) {
        transformed = true;
        rc.conjoinWith(rarg.next());
      }

      if (rc != null) retainedArgs.add(rc);
    }


    if (transformed) {
      return new ConstraintConjunction(retainedArgs);
    } else {
      return cc;
    }
  }
}
