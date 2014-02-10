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

package org.mulgara.store.tuples;

// Java 2 standard packages

// Third party packages
import org.jrdf.graph.Node;

// Local packages
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintHaving;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.stringpool.StringPoolException;


/**
 * Interface for RestrictPredicates for use with TuplesOperations.restrict()
 *
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 *
 * @created 2003-02-21
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2003-2004 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class HavingPredicates {
  public static abstract class AbstractHavingPredicate implements RestrictPredicatePrototype {
    protected ResolverSession session;
    protected long rhsValue;
    protected Variable rhsVar;
    protected Variable lhsVar;

    public abstract boolean pass(Tuples tuples) throws TuplesException;

    public RestrictPredicate duplicate(ConstraintHaving constraint, ResolverSession session) throws TuplesException {
      AbstractHavingPredicate pred = (AbstractHavingPredicate)clone();
      pred.session = session;

      ConstraintElement rhs = constraint.getElement(2);
      if (rhs instanceof Variable) {
        pred.rhsVar = (Variable)rhs;
      } else {
        try {
          pred.rhsValue = session.localize((Node)rhs);
        } catch (LocalizeException el) {
          throw new TuplesException("Failed to localize rhs of restriction predicate: " + constraint);
        }

      }

      ConstraintElement lhs = constraint.getElement(0);
      if (!(lhs instanceof Variable)) {
        throw new TuplesException("Left-hand-side of restriction predicate is not variable");
      }
      pred.lhsVar = (Variable)lhs;

      return pred;
    }

    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException ec) {
        throw new IllegalStateException("Object threw clone not supported exception!");
      }
    }

    protected long getRhsValue(Tuples tuples) throws TuplesException {
      if (rhsVar != null) {
        return tuples.getColumnValue(tuples.getColumnIndex(rhsVar));
      } else {
        return rhsValue;
      }
    }


    protected long getLhsValue(Tuples tuples) throws TuplesException {
      if (lhsVar != null) {
        return tuples.getColumnValue(tuples.getColumnIndex(lhsVar));
      } else {
        throw new TuplesException("lhsVar not initialised");
      }
    }
  }


  public static class PredicateOccurs extends AbstractHavingPredicate {
    public boolean pass(Tuples tuples) throws TuplesException {
      return getLhsValue(tuples) == getRhsValue(tuples);
    }
  }


  public static class PredicateNotOccurs extends AbstractHavingPredicate {
    public boolean pass(Tuples tuples) throws TuplesException {
      return getLhsValue(tuples) != getRhsValue(tuples);
    }
  }


  public static class PredicateOccursMoreThan extends AbstractHavingPredicate {
    public boolean pass(Tuples tuples) throws TuplesException {
      try {
        return session.findStringPoolObject(getLhsValue(tuples)).numericalCompare(
                  session.findStringPoolObject(getRhsValue(tuples))) > 0;
      } catch (StringPoolException es) {
        throw new TuplesException("Failure to extract SPObject from stringpool", es);
      }
    }
  }


  public static class PredicateOccursLessThan extends AbstractHavingPredicate {
    public boolean pass(Tuples tuples) throws TuplesException {
      try {
        return session.findStringPoolObject(getLhsValue(tuples)).numericalCompare(
                  session.findStringPoolObject(getRhsValue(tuples))) < 0;
      } catch (StringPoolException es) {
        throw new TuplesException("Failure to extract SPObject from stringpool", es);
      }
    }
  }
}
