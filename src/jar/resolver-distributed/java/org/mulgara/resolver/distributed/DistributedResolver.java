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

// Java 2 standard packages
import java.net.URI;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Resolves constraints accessible through a session.
 *
 * @created 2007-03-20
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @copyright &copy; 2007 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class DistributedResolver implements Resolver, TransactionCoordinator {
  /** Logger. */
  private static final Logger logger = Logger.getLogger(DistributedResolver.class);

  /** The delegator that resolves the constraint on another server.  */
  private final Delegator delegator;

  /** our xa-resource */
  private final DistributedXAResource xares;


  /**
   * Construct a Distributed Resolver.
   * @param resolverSession the session this resolver is associated with.
   * @param resolverFactory the factory this resolver is associated with.
   * @param canWrite        whether the current transaction is read-only or r/w
   * @param sessionCache    the session cache to use
   * @throws IllegalArgumentException if <var>resolverSession</var> is <code>null</code>
   * @throws ResolverFactoryException if the superclass is unable to handle its arguments
   */
  DistributedResolver(ResolverSession resolverSession, ResolverFactory resolverFactory,
                      boolean canWrite, SessionCache sessionCache) throws ResolverFactoryException {

    if (logger.isDebugEnabled()) logger.debug("Instantiating a distributed resolver");

    // Validate "resolverSession" parameter
    if (resolverSession == null) throw new IllegalArgumentException( "Null \"resolverSession\" parameter");

    delegator = new NetworkDelegator(resolverSession, canWrite, this, sessionCache);
    xares = new DistributedXAResource(10, resolverFactory, delegator);
  }


  /**
   * Graph creation method.  Not supported in this resolver.
   * @throws ResolverException The server should not ask this resolver to create a model.
   */
  public void createModel(long model, URI modelType) throws ResolverException {
    throw new ResolverException("Requesting model creation from a distributed resolver.");
  }


  /**
   * Expose a callback object for enlistment by a transaction manager.
   *
   * @return an {@link XAResource} that can be used by a transaction manager to
   *   coordinate this resolver's participation in a distributed transaction.
   * @see javax.resource.spi.ManagedConnection#getXAResource
   */
  public XAResource getXAResource() {
    return xares;
  }


  /**
   * Insert or delete RDF statements in an existing model.
   * This is illegal for this model type.
   *
   * @param model  the local node identifying an existing model
   * @param statements  the {@link Statements} to insert into the
   *   <var>model</var>
   * @param occurs  whether to assert the <var>statements</var>, or (if
   *   <code>false</code>) to deny it
   * @throws ResolverException The server should not ask this resolver to modify data.
   */
  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
    if (logger.isDebugEnabled()) logger.debug(occurs ? "Adding" : "Removing" + " statements from model");
    try {
      if (occurs) delegator.add(model, statements);
      else delegator.remove(model, statements);
    } catch (QueryException qe) {
      throw new ResolverException("Error modifying model", qe);
    }
  }


  /**
   * Remove the cached model containing the contents of a URL.
   * @throws ResolverException The server should not ask this resolver to modify data.
   */
  public void removeModel(long model) throws ResolverException {
    throw new ResolverException("Distributed models cannot be removed");
  }


  /**
   * Resolve a constraint against an RDF/XML document.
   * @param constraint The constraint pattern to be resolved.
   * @return A resolution for the constraint against a model.
   * @throws IllegalArgumentException The constraint is <code>null</code>, or not set to a non-local model.
   * @throws QueryException There was a problem resolving the constraint.
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (logger.isDebugEnabled()) logger.debug("Resolve " + constraint);

    // validate the parameter
    if (constraint == null) throw new IllegalArgumentException();
    ConstraintElement modelElement = constraint.getElement(3);
    if (!(modelElement instanceof LocalNode)) throw new QueryException("Constraint not set to a distributed model.");

    try {
      return delegator.resolve(constraint, (LocalNode)modelElement);
    } catch (ResolverException re) {
      throw new QueryException(re.getMessage(), re);
    }
  }


  public void abort() {
    delegator.close();
  }


  public void enlistResource(XAResource xares) throws XAException {
    this.xares.enlistResource(xares);
  }

  /**
   * An XAResource which encapsulates and delegates to the all the remote XAResource's.
   *
   * <p>Note that this can never be really correct because it's basically impossible to get
   * the <var>isSameRM</var> semantics correct when proxying multiple XAResource's.
   */
  private static class DistributedXAResource extends MultiXAResource {
    private final Delegator delegator;

    /**
     * Construct a {@link DistributedXAResource} with a specified transaction timeout.
     *
     * @param transactionTimeout transaction timeout period, in seconds
     * @param resolverFactory    the resolver-factory we belong to
     * @param delegator          the delegator being used
     */
    public DistributedXAResource(int transactionTimeout, ResolverFactory resolverFactory,
                                 Delegator delegator) {
      super(transactionTimeout, resolverFactory);
      this.delegator = delegator;
    }

    protected DistributedTxInfo newTransactionInfo() {
      DistributedTxInfo txInfo = new DistributedTxInfo();
      txInfo.delegator = delegator;
      return txInfo;
    }

    protected void transactionCompleted(MultiXAResource.MultiTxInfo tx) {
      try {
        ((DistributedTxInfo) tx).delegator.close();
      } finally {
        super.transactionCompleted(tx);
      }
    }

    static class DistributedTxInfo extends MultiXAResource.MultiTxInfo {
      public Delegator delegator;
    }
  }
}
