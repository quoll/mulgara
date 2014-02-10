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

package org.mulgara.resolver.gis;

//Java 2 standard packages
import java.net.URI;
import javax.transaction.xa.XAResource;

// Log4J
import org.apache.log4j.Logger;

//JRDF
import org.jrdf.vocabulary.*;

//local packages
import org.mulgara.content.*;
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.gis.tools.*;
import org.mulgara.resolver.spi.*;

/**
 * Used for estimating the difference between two points. Read-Only Resolver
 * that calculates distances from a contraint.
 *
 * @created 2004-11-17
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.6 $
 *
 * @modified $Date: 2005/01/05 04:58:29 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ReadOnlyGISResolver implements Resolver {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(ReadOnlyGISResolver.class.getName());

  /** Calculates distances */
  protected DistanceCalculator gisCalculator = null;

  /** "StringPool" */
  protected ResolverSession resolverSession = null;

  /** Used for delegation */
  protected Resolver resolver = null;

  /** Used to create/drop models and query the statement store */
  protected Resolver systemResolver = null;

  /** Used to handle content/manage content handlers */
  protected ContentHandlerManager contentManager;

  /** Graph type supported by this resolver */
  public static final URI MODEL_TYPE = URI.create(Mulgara.NAMESPACE + "GISModel");

  /** Node ID for rdf:type */
  protected long RDF_TYPE = -1;

  /** Node ID for geo:Point */
  protected long GEO_POINT = -1;

  /** Node ID for dis:Distance */
  protected long DIS_DISTANCE = -1;

  /** Node ID for geo:lat */
  protected long GEO_LAT = -1;

  /** Node ID for geo:long */
  protected long GEO_LONG = -1;

  /**
   * Constructor.
   *
   * @param resolverSession ResolverSession
   * @param systemResolver Resolver
   * @param contentManager ContentHandlerManager
   * @param rdfType long
   * @param systemModel long
   * @throws ResolverFactoryException
   */
  ReadOnlyGISResolver(ResolverSession resolverSession, Resolver resolver,
      Resolver systemResolver, ContentHandlerManager contentManager) throws ResolverFactoryException {

    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }
    if (resolver == null) {
      throw new IllegalArgumentException("Null \"resolver\" parameter");
    }
    if (systemResolver == null) {
      throw new IllegalArgumentException("Null \"systemResolver\" parameter");
    }
    if (contentManager == null) {
      throw new IllegalArgumentException("Null \"contentManager\" parameter");
    }

    // Initialize fields
    gisCalculator = DistanceCalculatorFactory.newGISDistanceEstimator();
    this.resolverSession = resolverSession;
    this.resolver = resolver;
    this.systemResolver = systemResolver;
    this.contentManager = contentManager;

    //pre-localize common Nodes
    try {
      GEO_POINT = resolverSession.localizePersistent(GEO.POINT);
      GEO_LAT = resolverSession.localizePersistent(GEO.LAT);
      GEO_LONG = resolverSession.localizePersistent(GEO.LONG);
      RDF_TYPE = resolverSession.localizePersistent(new URIReferenceImpl(RDF.TYPE));
      DIS_DISTANCE = resolverSession.localizePersistent(LocalGISDistance.RDF_TYPE);
    }
    catch (LocalizeException localizeException) {
      throw new ResolverFactoryException("Failed to pre-localize 'geo' RDF " +
          "Vocabulary.", localizeException);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param model long
   * @param modelType URI
   * @throws ResolverException
   * @throws LocalizeException
   */
  public void createModel(long model, URI modelType) throws ResolverException,
      LocalizeException {

    if (!MODEL_TYPE.equals(modelType)) {
      throw new ResolverException("Cannot create model of type: " + modelType);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @return XAResource
   */
  public XAResource getXAResource() {

    //should not be bound by transactions (this may change)
    // (10 seconds before transaction timeout)
    return new DummyXAResource(10);
  }

  /**
   * This Resolver is read-only. Throws Exception.
   *
   * @param model long
   * @param statements Statements
   * @param occurs boolean
   * @throws ResolverException
   */
  public void modifyModel(long model, Statements statements,
      boolean occurs) throws ResolverException {
    throw new ResolverException("Resolver is Read-Only.");
  }

  /**
   * {@inheritDoc}
   *
   * @param model long
   * @throws ResolverException
   */
  public void removeModel(long model) throws ResolverException {

    if (log.isDebugEnabled()) {
      log.debug("Dropping GISModel: " + model);
    }
    systemResolver.removeModel(model);
  }

  /**
   * {@inheritDoc}
   *
   * @param constraint Constraint
   * @return Resolution
   * @throws QueryException
   */
  public Resolution resolve(Constraint constraint) throws QueryException {

    /** @todo Implement backward-chaining.
     *  Just query the statement store for now
     */
    return resolver.resolve(constraint);
  }

  public void abort() {
    resolver.abort();
  }

}
