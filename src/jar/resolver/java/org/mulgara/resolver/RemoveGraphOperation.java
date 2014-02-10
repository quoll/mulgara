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

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.nodepool.NodePool;

/**
 * An {@link Operation} that implements the {@link org.mulgara.server.Session#removeModel(URI)} method.
 *
 * @created 2004-11-24
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/05/02 20:07:56 $ by $Author: raboczi $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class RemoveGraphOperation implements Operation {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RemoveGraphOperation.class.getName());

  /**
   * The URI of the model to be removed.
   */
  private final URI modelURI;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   *
   * @param modelURI  the {@link URI} of the model to be removed, never
   *   <code>null</code>
   * @throws IllegalArgumentException if <var>modelURI</var> is
   *   <code>null</code>
   */
  RemoveGraphOperation(URI modelURI)
  {
    // Validate "modelURI" parameter
    if (modelURI == null) {
      throw new IllegalArgumentException("Null \"modelURI\" parameter");
    }

    // Initialize fields
    this.modelURI = modelURI;
  }

  //
  // Methods implementing Operation
  //

  public void execute(OperationContext       operationContext,
                      SystemResolver         systemResolver,
                      DatabaseMetadata       metadata) throws Exception {

    long model = systemResolver.localize(new URIReferenceImpl(modelURI));
    model = operationContext.getCanonicalModel(model);

    for (SecurityAdapter securityAdapter: operationContext.getSecurityAdapterList()) {

      // Lie to the user
      if (model == NodePool.NONE || !securityAdapter.canSeeModel(model, systemResolver)) {
        throw new QueryException("No such model " + modelURI);
      }

      // Tell the truth to the user
      if (!securityAdapter.canRemoveModel(model, systemResolver)) {
        throw new QueryException("You aren't allowed to remove " + modelURI);
      }
    }

    // Look up the resolver factory for the model
    ResolverFactory resolverFactory =
      operationContext.findModelResolverFactory(model);

    if (resolverFactory == null) {
      throw new QueryException(
        "Could not obtain a resolver factory for " + modelURI
      );
    }

    // Obtain an appropriate resolver bound to this session
    Resolver resolver = operationContext.obtainResolver(resolverFactory);
    assert resolver != null;

    // Use the resolver to remove the model
    resolver.removeModel(model);
  }

  /**
   * @return <code>true</code>
   */
  public boolean isWriteOperation()
  {
    return true;
  }
}
