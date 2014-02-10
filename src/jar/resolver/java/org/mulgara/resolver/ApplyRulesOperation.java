/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.rmi.RemoteException;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.DatabaseMetadata;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.rules.Rules;
import org.mulgara.rules.RulesRef;

/**
 * An {@link Operation} that applies a set of rules to a graph. These rules are built with
 * the corresponding {@link BuildRulesOperation}.
 *
 * @created Mar 25, 2008
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
class ApplyRulesOperation implements Operation {

  @SuppressWarnings("unused")
  /** Logger. */
  private static final Logger logger = Logger.getLogger(ApplyRulesOperation.class.getName());

  /** The rules to run in this operation. */
  Rules rules;

  /**
   * Create and set up the operation.
   *
   * @param rulesRef A reference to the rules that this operation will execute.
   * @throws IllegalArgumentException if the rules are null.
   * @throws QueryException if the rules reference could not be accessed over a network.
   */
  public ApplyRulesOperation(RulesRef rulesRef) throws QueryException {
    if (rulesRef == null) throw new IllegalArgumentException("Illegal to use a null set of rules");
    try {
      rules = rulesRef.getRules();
    } catch (RemoteException e) {
      throw new QueryException("Unable to read rules from server", e);
    }
  }

  /**
   * Runs the rules over the configured data.
   * @see org.mulgara.resolver.Operation#execute(OperationContext, SystemResolver, DatabaseMetadata)
   */
  public void execute(OperationContext         operationContext,
                      SystemResolver           systemResolver,
                      DatabaseMetadata         metadata) throws Exception {
    rules.run(packageParams(operationContext, systemResolver));
  }

  /**
   * @see org.mulgara.resolver.Operation#isWriteOperation()
   * @return <code>true</code>
   */
  public boolean isWriteOperation() {
    return true;
  }
  
  /**
   * Packages up the parameters for the rules to run with.
   * This was a Session, and will become a Session, but for the moment it is the
   * OperationContext and SystemResolver.
   * TODO: change this back to Session
   */
  private Object[] packageParams(OperationContext context, SystemResolver resolver) {
    return new Object[] { context, resolver };
  }
}
