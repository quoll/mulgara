/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
5B
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
import javax.transaction.xa.XAResource;
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.SingletonStatements;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.resolver.view.SessionView;
import org.mulgara.resolver.view.ViewMarker;
import org.mulgara.store.nodepool.NodePool;

/**
 * Access to models that appear in the system model (<code>#</code>).
 *
 * @created 2004-11-17
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/02/22 08:16:09 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

class InternalResolver implements Resolver, ViewMarker
{
  /**
   * Logger.
   *
   * This is named after the class.
   */
  private static final Logger logger =
    Logger.getLogger(InternalResolver.class.getName());

  /**
   * The preallocated node for the <code>rdf:type</code> property.
   *
   * If this is equal to {@link NodePool#NONE} then creation and removal of
   * models won't be possible.
   */
  private final long rdfType;

  /**
   * The wrapped instance.
   */
  private final Resolver resolver;

  private final ResolverSession resolverSession;

  /**
   * The preallocated node for the system model (<code>#</code>).
   */
  private final long systemModel;

  /**
   * The resolver used to manipulate the system model (<code>#</code>).
   */
  private final Resolver systemResolver;

  //
  // Constructor
  //

  /**
   * Sole constructor.
   *
   * @param resolver  the wrapped instance, never <code>null</code>
   * @param rdfType  the preallocated node for <code>rdf:type</code>; if
   *   {@link NodePool#NONE} then creation and removal will be disabled
   * @param systemModel  the preallocated node for the system model
   *   (<code>#</code>)
   * @param systemResolver  used to manipulate the system model
   *   (<code>#</code>), never <code>null</code>
   */
  InternalResolver(Resolver        resolver,
                   long            rdfType,
                   ResolverSession resolverSession,
                   long            systemModel,
                   Resolver        systemResolver)
    throws ResolverFactoryException
  {
    if (resolver == null) {
      throw new IllegalArgumentException("Resolver 'null'");
    } else if (resolverSession == null) {
      throw new IllegalArgumentException("ResolverSession 'null'");
    } else if (systemResolver == null) {
      throw new IllegalArgumentException("SystemResolver 'null'");
    }

    // Initialize fields
    this.resolver        = resolver;
    this.rdfType         = rdfType;
    this.resolverSession = resolverSession;
    this.systemModel     = systemModel;
    this.systemResolver  = systemResolver;
  }

  //
  // Methods implementing Resolver
  //

  public void createModel(long model, URI modelType)
    throws ResolverException, LocalizeException
  {
    resolver.createModel(model, modelType);

    // Record the new model's existence in the system model (#), unless it IS
    // the system model
    if (systemModel != NodePool.NONE) {
      setModelPresenceInSystemModel(model, modelType, true);
    }
  }

  public XAResource getXAResource()
  {
    return resolver.getXAResource();
  }

  public void modifyModel(long model, Statements statements, boolean occurs)
    throws ResolverException
  {
    resolver.modifyModel(model, statements, occurs);
  }

  public void removeModel(long model) throws ResolverException
  {
    resolver.removeModel(model);
    setModelPresenceInSystemModel(model, findModelType(model), false);
  }

  public Resolution resolve(Constraint constraint) throws QueryException
  {
    // Validate "constraint" parameter
    if (constraint == null) {
      throw new IllegalArgumentException("Null \"constraint\" parameter");
    }

    return resolver.resolve(constraint);
  }

  //
  // Methods implementing ViewMarker
  //

  public void setSession(SessionView session)
  {
    if (resolver instanceof ViewMarker) {
      ((ViewMarker) resolver).setSession(session);
    }
  }

  //
  // Internal methods
  //

  /**
   * Find the type of a model.
   *
   * @param model  the local node of a model
   * @return the local node representing the type of the <var>model</var>, or
   *   {@link NodePool#NONE} if the <var>model</var> isn't stored within the
   *   system
   * @throws IllegalStateException if this session wasn't constructed with a
   *   {@link #systemModel}
   * @throws ResolverException if the model type can't be determined
   */
  private long findModelType(long model) throws ResolverException
  {
    if (rdfType == NodePool.NONE) {
      throw new IllegalStateException("No rdf:type for this resolver");
    }
    if (systemModel == NodePool.NONE) {
      throw new IllegalStateException("No system model for this resolver");
    }
    if (systemResolver == null) {
      throw new ResolverException("Resolver created without a system model");
    }

    try {
      // Query the system model for the type of the model
      Variable modelTypeVariable = new Variable("modelType");
      Resolution resolution = systemResolver.resolve(
          new ConstraintImpl(new LocalNode(model),
                         new LocalNode(rdfType),
                         modelTypeVariable,
                         new LocalNode(systemModel)));
      assert resolution != null;

      // Check the solution and extract the model type (if any) from it
      try {
        resolution.beforeFirst();
        if (resolution.next()) {
          long modelType = resolution.getColumnValue(
                             resolution.getColumnIndex(modelTypeVariable));
          if (resolution.next()) {           throw new ResolverException("Graph " + model + " has more than one type!");
          }

          return modelType;
        }
        else {
          return NodePool.NONE;
        }
      } finally {
        resolution.close();
      }
    }
    catch (QueryException e) {
      throw new ResolverException("Unable to determine model type of "+model, e);
    }
    catch (TuplesException e) {
      throw new ResolverException("Unable to determine model type of "+model, e);
    }
  }

  /**
   * Change a model's presence in the system model (<code>#</code>).
   *
   * @param model  the model to add or remove
   * @param present  whether to add or remove the model from the system model
   * @throws ResolverException if the system model wasn't updated
   */
  private void setModelPresenceInSystemModel(long    model,
                                             URI    modelTypeURI,
                                             boolean present)
    throws ResolverException, LocalizeException
  {
    // Check whether we're capable of system model operations
    if (systemResolver == null) {
      throw new ResolverException("Resolver created without a system model");
    }

    setModelPresenceInSystemModel(model,
        resolverSession.localizePersistent(new URIReferenceImpl(modelTypeURI)), present);
  }

  private void setModelPresenceInSystemModel(long    model,
                                             long    modelType,
                                             boolean present)
    throws ResolverException
  {
    // Check whether we're capable of system model operations
    if (rdfType == NodePool.NONE) {
      throw new ResolverException("Resolver created without rdf:type");
    }
    if (systemResolver == null) {
      throw new ResolverException("Resolver created without a system model");
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Inserting statement to create model: " + model + ":" + rdfType + ":" + modelType);
    }
    // Add the statement that the model is of a type to the system model
    systemResolver.modifyModel(systemModel,
        new SingletonStatements(model, rdfType, modelType),
        present);
  }

  public void abort() {
    resolver.abort();
  }
}
