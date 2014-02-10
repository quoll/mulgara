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

package org.mulgara.resolver.view;

// Standard java packages
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Third Party packages
import gnu.trove.TLongArrayList;
import gnu.trove.TLongProcedure;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.view.SessionView;
import org.mulgara.store.tuples.Tuples;

/**
 * A component of a View definition.
 *
 * @created 2004-09-21
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:36 $ @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class ViewDefinition
{
  TLongArrayList models;
  List<ViewDefinition> exprs;

  public ViewDefinition(TLongArrayList models, List<ViewDefinition> exprs) {
    this.models = models;
    this.exprs = exprs;
  }


  public abstract Tuples consolidate(List<Tuples> args) throws TuplesException;


  public Resolution resolve(Constraint constraint, final SessionView session) throws QueryException {
    final ConstraintElement subject = constraint.getElement(0);
    final ConstraintElement predicate = constraint.getElement(1);
    final ConstraintElement object = constraint.getElement(2);

    final List<Tuples> args = new ArrayList<Tuples>();

    try {
      models.forEach(new TLongProcedure() {
          public boolean execute(long value) {
            Constraint con = new ConstraintImpl(subject, predicate, object, new LocalNode(value));
            try {
              args.add(session.resolve(con));
            } catch (Throwable th) {
              throw new RuntimeException("Query resolution failed", th);
            }
            return true;
          }
        });
    } catch (RuntimeException er) {
      throw new QueryException("Failed to resolve view constraint", er.getCause());
    }

    Iterator<ViewDefinition> i = exprs.iterator();
    while (i.hasNext()) {
      args.add(i.next().resolve(constraint, session));
    }

    try {
      Tuples result = consolidate(args);

      for (Tuples t: args) t.close();

      return new ViewResolution(constraint, result);
    } catch (TuplesException et) {
      throw new QueryException("Error closing tuples in view expansion", et);
    }

  }
}
