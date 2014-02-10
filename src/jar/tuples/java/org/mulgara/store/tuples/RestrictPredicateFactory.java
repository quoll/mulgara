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
import java.util.*;

// Local packages
import org.mulgara.query.ConstraintHaving;
import org.mulgara.query.ConstraintNotOccurs;
import org.mulgara.query.ConstraintOccurs;
import org.mulgara.query.ConstraintOccursLessThan;
import org.mulgara.query.ConstraintOccursMoreThan;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Factory of RestrictPredicates for use with implementing Having clauses.
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
public class RestrictPredicateFactory {
  protected static Map prototypes;

  static {
    prototypes = new HashMap();
  }

  static {
    registerPredicate(ConstraintOccurs.class, new HavingPredicates.PredicateOccurs());
    registerPredicate(ConstraintNotOccurs.class, new HavingPredicates.PredicateNotOccurs());
    registerPredicate(ConstraintOccursMoreThan.class, new HavingPredicates.PredicateOccursMoreThan());
    registerPredicate(ConstraintOccursLessThan.class, new HavingPredicates.PredicateOccursLessThan());
  }


  public static boolean registerPredicate(Class constraintClass, RestrictPredicatePrototype prototype) {
    if (prototypes.containsKey(constraintClass)) {
      return false;
    } else {
      prototypes.put(constraintClass, prototype);
      return true;
    }
  }


  public static RestrictPredicate getPredicate(ConstraintHaving constraint, ResolverSession session) throws TuplesException {
    RestrictPredicatePrototype predicate = (RestrictPredicatePrototype)prototypes.get(constraint.getClass());
    if (predicate == null) {
      throw new TuplesException("Unable to obtain predicate matching restrict constraint: " + constraint);
    }

    return predicate.duplicate(constraint, session);
  }
}
