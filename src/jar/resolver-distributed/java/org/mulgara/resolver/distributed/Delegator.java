/*
 * Copyright 2008 Fedora Commons, Inc.
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

package org.mulgara.resolver.distributed;

import org.mulgara.query.Constraint;
import org.mulgara.query.LocalNode;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.query.QueryException;


/**
 * Interface for an object capable of resolving a constraint
 *
 * @created 2007-03-20
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: $
 * @modified $Date: $
 * @maintenanceAuthor $Author: $
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface Delegator {

  /**
   * Resolve a given constraint down to the appropriate resolution.
   * @param constraint The constraint to resolve.
   * @param model The LocalNode containing the model
   * @throws QueryException A delegator specific problem occurred resolving the constraint.
   * @throws ResolverException A delegator specific problem occurred setting up the resolution.
   */
  public Resolution resolve(Constraint constraint, LocalNode model) throws QueryException, ResolverException;

  /**
   * Add a set of statements to a model.
   * @param model The <code>long</code> containing the model gNode.
   * @param statements The statements to add to the model.
   * @throws ResolverException A delegator specific problem occurred adding the data.
   * @throws QueryException There was an error adding data at the remote end.
   */
  public void add(long model, Statements statements) throws ResolverException, QueryException;

  /**
   * Remove a set of statements from a model.
   * @param model The <code>long</code> containing the model gNode.
   * @param statements The statements to remove from the model.
   * @throws ResolverException A delegator specific problem occurred removing the data.
   * @throws QueryException There was an error removing data at the remote end.
   */
  public void remove(long model, Statements statements) throws ResolverException, QueryException;

  /**
   * Close the delegator.
   */
  public void close();
}
