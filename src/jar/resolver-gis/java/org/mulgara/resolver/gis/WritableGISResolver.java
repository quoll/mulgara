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

// Log4J
import org.apache.log4j.Logger;

//local packages
import java.net.*;

import org.mulgara.content.*;
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.statement.*;
import org.mulgara.store.tuples.*;
import org.mulgara.util.*;

import java.util.*;

/**
 * Used for estimating the difference between two points. Infers statements that
 * are inserted.
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
public class WritableGISResolver extends ReadOnlyGISResolver {

  /** Logger. */
  private final static Logger log = Logger.getLogger(WritableGISResolver.class.getName());

  /** Model type */
  private static final URI MODEL_TYPE = URI.create(Mulgara.NAMESPACE + "Model");
  
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
  WritableGISResolver(ResolverSession resolverSession, Resolver resolver,
      Resolver systemResolver,
      ContentHandlerManager contentManager) throws ResolverFactoryException {
    super(resolverSession, resolver, systemResolver, contentManager);
  }

  /**
   * {@inheritDoc}
   *
   * @param model long
   * @param statements Statements
   * @param occurs boolean Whether to insert (true) or delete (false)
   * @throws ResolverException
   */
  public void modifyModel(long model, Statements statements,
      boolean occurs) throws ResolverException {

    //insert/delete statements and inferred statements
    if (occurs) {
      try {
        insertStatements(model, statements);
      } catch (Exception exception) {
        throw new ResolverException("Failed to insert statements.", exception);
      }
    } else {
      throw new ResolverException("Delete not implemented");
    }
  }

  /**
   * Inserts the statements into the model. Also generates and inserts inferred
   * statements into the model.
   *
   * @param model long
   * @param modifyStatements Statements
   * @throws ResolverException
   * @throws TuplesException
   */
  private void insertStatements(long model,
      Statements modifyStatements) throws ResolverException, TuplesException {

    long tempModel = -1;
    Resolution xPoints = null;
    Resolution xLats = null;
    Resolution xLongs = null;
    Resolution yPoints = null;
    Resolution yLats = null;
    Resolution yLongs = null;
    Statements inferredStatements = null;

    try {

      //insert modify statements into a temp model and get all points
      tempModel = createTemporaryModel();
      super.resolver.modifyModel(tempModel, modifyStatements, true);
      Variable x = GISDistanceStatements.X_VAR;
      Variable xlat = GISDistanceStatements.XLAT_VAR;
      Variable xlong = GISDistanceStatements.XLONG_VAR;
      xPoints = getPointTypes(x, tempModel);
      xLats = getPointLatitudes(x, xlat, tempModel);
      xLongs = getPointLongitudes(x, xlong, tempModel);

      // insert modify statements into the store and get all points
      super.resolver.modifyModel(model, modifyStatements, true);
      Variable y = GISDistanceStatements.Y_VAR;
      Variable ylat = GISDistanceStatements.YLAT_VAR;
      Variable ylong = GISDistanceStatements.YLONG_VAR;
      yPoints = getPointTypes(y, model);
      yLats = getPointLatitudes(y, ylat, model);
      yLongs = getPointLongitudes(y, ylong, model);

      //join all points (generate inferred statements)
      List<Resolution> tupleList = new ArrayList<Resolution>();
      tupleList.add(xPoints);
      tupleList.add(xLats);
      tupleList.add(xLongs);
      tupleList.add(yPoints);
      tupleList.add(yLats);
      tupleList.add(yLongs);
      Tuples points = TuplesOperations.join(tupleList);
      inferredStatements = getInferredStatements(points);

      //delete all "duplicate" inferred statements before inserting
      deleteDuplicates(model, modifyStatements);

      //insert the inferred statements
      super.resolver.modifyModel(model, inferredStatements, true);
    } finally {
      //clean up
      if (tempModel != -1) {
        dropTemporaryModel(tempModel);
      }
      close(xPoints);
      close(xLats);
      close(xLongs);
      close(yPoints);
      close(yLats);
      close(yLongs);
    }
  }

  /**
   * Deletes any duplicate inferred statements from the model.
   *
   * @param model long
   * @param inferred Statements
   * @throws ResolverException
   * @throws TuplesException
   * @throws ResolverException
   */
  private void deleteDuplicates(long model,
      Statements modified) throws ResolverException, TuplesException,
      ResolverException {

    long tempModel = -1;
    Resolution xPoints = null;
    Resolution yPoints = null;
    Tuples points = null;
    Tuples distTypes = null;
    Tuples distanceTypes = null;
    Tuples projectedDistTypes = null;
    Tuples dist = null;
    Tuples distances = null;
    Tuples all = null;
    Statements distTypeStatements = null;
    Statements distStatements = null;
    Statements distStatements2 = null;

    try {

      //insert modify statements into a temp model
      tempModel = createTemporaryModel();
      super.resolver.modifyModel(tempModel, modified, true);

      //get all point combinations - cartesian product ($x $y)
      Variable x = GISDistanceStatements.X_VAR;
      Variable y = GISDistanceStatements.Y_VAR;
      xPoints = getPointTypes(x, tempModel);
      yPoints = getPointTypes(y, model);
      points = TuplesOperations.join(xPoints, yPoints);

      //get all distance statements from the existing model ($x $d $y)
      Variable d = new Variable("d");
      dist = getDistancesStatements(d, model);
      all = getAllStatements(x, d, y, model);
      distances = TuplesOperations.join(Arrays.asList(new Tuples []{
        all, dist, points
      }));
      if (!isEmpty(distances)) {
        //must also delete inverted statements
        distStatements = new TuplesWrapperStatements((Tuples) distances.clone(), x, d, y);
        distStatements2 = new TuplesWrapperStatements((Tuples) distances.clone(), y, d, x);
      }

      //get all statements for each of the distances ($d $p $o)
      distTypes = getDistances(d, model);
      distanceTypes = TuplesOperations.join(distances, distTypes);
      //only want $d $p $o
      Variable[] vars = new Variable[] {
          d,
          StatementStore.VARIABLES[1],
          StatementStore.VARIABLES[2]
      };
      projectedDistTypes = TuplesOperations.project(distanceTypes, Arrays.asList(vars), true);
      if (!isEmpty(distanceTypes)) {
        distTypeStatements = new TuplesWrapperStatements((Tuples)distanceTypes.clone(), d, vars[1], vars[2]);
      }

      //delete the statements
      if (!isEmpty(distStatements)) {
        super.resolver.modifyModel(model, distStatements, false);
      }
      if (!isEmpty(distStatements)) {
        super.resolver.modifyModel(model, distStatements2, false);
      }
      if (!isEmpty(distTypeStatements)) {
        super.resolver.modifyModel(model, distTypeStatements, false);
      }
    } finally {
      //clean up
      if (tempModel != -1) dropTemporaryModel(tempModel);
      close(xPoints);
      close(yPoints);
      close(points);
      close(distTypes);
      close(distanceTypes);
      close(projectedDistTypes);
      close(dist);
      close(distances);
      close(all);
    }
  }

  /**
   * Returns true if the Cursor are empty or null.
   *
   * @param cursor Cursor
   * @return boolean
   * @throws TuplesException
   */
  private boolean isEmpty(Cursor cursor) throws TuplesException {
    return (cursor == null) || cursor.isEmpty() || (cursor.getRowCardinality() == Cursor.ZERO);
  }

  /**
   * Closes the cursor if it is not null.
   * @param cursor Cursor
   * @throws TuplesException
   */
  private void close(Cursor cursor) throws TuplesException {
    if (cursor != null) {
      cursor.close();
    }
  }

  /**
   * Returns all Statements of the form: $x $d $y ($d is type Distance)
   *
   * @param model long
   * @throws ResolverException
   * @throws TuplesException
   * @return Tuples
   */
  private Tuples getDistancesStatements(Variable distVar,
      long model) throws ResolverException, TuplesException {

    Resolution xPoints = null;
    Resolution dist = null;
    Resolution yPoints = null;

    try {

      //delete all statements: $x $d $y
      Variable x = GISDistanceStatements.X_VAR;
      Variable y = GISDistanceStatements.Y_VAR;

      xPoints = getPointTypes(x, model);
      yPoints = getPointTypes(y, model);
      dist = getDistanceTypes(distVar, model);

      //join all points (generate all point combinations)
      List<Resolution> tupleList = new ArrayList<Resolution>();
      tupleList.add(xPoints);
      tupleList.add(dist);
      tupleList.add(yPoints);
      return TuplesOperations.join(tupleList);
    } finally {
      //clean up
      if (xPoints != null) xPoints.close();
      if (dist != null) dist.close();
      if (yPoints != null) yPoints.close();
    }
  }

  /**
   * Returns all Statements for all Distances
   *
   * @param model long
   * @throws ResolverException
   * @throws TuplesException
   * @return Tuples
   */
  private Tuples getDistances(Variable distVar,
      long model) throws ResolverException, TuplesException {

    Resolution all = null;
    Resolution dist = null;

    try {

      //delete all statements: $d $p $o
      Variable p = StatementStore.VARIABLES[1];
      Variable o = StatementStore.VARIABLES[2];

      dist = getDistanceTypes(distVar, model);
      all = getAllStatements(distVar, p, o, model);

      //join all statements for each Distance
      List<Resolution> tupleList = new ArrayList<Resolution>();
      tupleList.add(all);
      tupleList.add(dist);
      return TuplesOperations.join(tupleList);
    } finally {
      //clean up
      if (all != null) all.close();
      if (dist != null) dist.close();
    }
  }

  /**
   * Returns all statements from the model using the given variables.
   *
   * @param s Variable
   * @param p Variable
   * @param o Variable
   * @param model long
   * @throws ResolverException
   * @return Resolution
   */
  private Resolution getAllStatements(Variable s, Variable p, Variable o,
      long model) throws ResolverException {

    try {
      LocalNode modelNode = new LocalNode(model);
      Constraint constraint = new ConstraintImpl(s, p, o, modelNode);
      return super.resolver.resolve(constraint);
    }
    catch (QueryException queryException) {
      throw new ResolverException("Could not get all statements.", queryException);
    }
  }

  /**
   * Resolves the constraint: $var &lt;rdf:type&gt; &lt;geo:Point&gt; <model>
   *
   * @param var Variable
   * @param model long
   * @throws ResolverException
   * @return Resolution
   */
  private Resolution getPointTypes(Variable var,
      long model) throws ResolverException {

    try {
      LocalNode rdfType = new LocalNode(RDF_TYPE);
      LocalNode geoPoint = new LocalNode(GEO_POINT);
      LocalNode modelNode = new LocalNode(model);
      Constraint constraint = new ConstraintImpl(var, rdfType, geoPoint, modelNode);
      return super.resolver.resolve(constraint);
    } catch (QueryException queryException) {
      throw new ResolverException("Could not get Points.", queryException);
    }
  }

  /**
   * Returns all Statements of the form: $d :type Distance
   *
   * @param var Variable
   * @param model long
   * @throws ResolverException
   * @return Resolution
   */
  private Resolution getDistanceTypes(Variable var,
      long model) throws ResolverException {

    try {
      LocalNode rdfType = new LocalNode(RDF_TYPE);
      LocalNode distance = new LocalNode(DIS_DISTANCE);
      LocalNode modelNode = new LocalNode(model);
      Constraint constraint = new ConstraintImpl(var, rdfType, distance, modelNode);
      return super.resolver.resolve(constraint);
    } catch (QueryException queryException) {
      throw new ResolverException("Could not get Distances.", queryException);
    }
  }

  /**
   * Resolves the constraint: $var &lt;geo:lat&gt; $lat <model>
   *
   * @param var Variable
   * @param lat Variable
   * @param model long
   * @throws ResolverException
   * @return Resolution
   */
  private Resolution getPointLatitudes(Variable var, Variable lat,
      long model) throws ResolverException {

    try {
      LocalNode geoLat = new LocalNode(GEO_LAT);
      LocalNode modelNode = new LocalNode(model);
      Constraint constraint = new ConstraintImpl(var, geoLat, lat, modelNode);
      return super.resolver.resolve(constraint);
    } catch (QueryException queryException) {
      throw new ResolverException("Could not get Point Latitudes.", queryException);
    }
  }

  /**
   * Resolves the constraint: $var &lt;geo:long&gt; $lon <model>
   *
   * @param var Variable
   * @param lon Variable
   * @param model long
   * @throws ResolverException
   * @return Resolution
   */
  private Resolution getPointLongitudes(Variable var, Variable lon,
      long model) throws ResolverException {

    try {
      LocalNode geoLong = new LocalNode(GEO_LONG);
      LocalNode modelNode = new LocalNode(model);
      Constraint constraint = new ConstraintImpl(var, geoLong, lon, modelNode);
      return super.resolver.resolve(constraint);
    } catch (QueryException queryException) {
      throw new ResolverException("Could not get Point Longitudes.", queryException);
    }
  }

  /**
   * Creates co-ordinates from the tuples and generates inferred distance
   * statements from the co-ordinates 'x' and 'y'.
   *
   * Tuples will have the variables:
   * [[$x] [$xlong] [$xlat] [$y] [$ylat] [$ylong]]
   *
   * @param points Tuples
   * @return Statements
   * @throws ResolverException
   */
  private Statements getInferredStatements(Tuples points) throws ResolverException {
    return new GISDistanceStatements(points, super.gisCalculator, resolverSession);
  }

  /**
   * Creates a Model for storing temporary statements and returns it's Node ID.
   *
   * @throws ResolverException
   * @return long
   */
  private long createTemporaryModel() throws ResolverException {

    long tempModel = -1;

    try {
      String modelUID = UIDGenerator.generateUID();
      URIReferenceImpl model = new URIReferenceImpl(new URI("temp:" + modelUID));
      tempModel = resolverSession.localizePersistent(model);

      if (log.isDebugEnabled()) {
        log.debug("Creating temp Model: " + model + " with ID: " + tempModel);
      }
      super.resolver.createModel(tempModel, MODEL_TYPE);
      return tempModel;
    } catch (Exception exception) {
      throw new ResolverException("Failed to create temporary Model.", exception);
    }
  }

  /**
   * Drops the model.
   *
   * @param model long
   * @throws ResolverException
   */
  private void dropTemporaryModel(long model) throws ResolverException {
    if (log.isDebugEnabled()) log.debug("Dropping temp Model with ID: " + model);
    super.resolver.removeModel(model);
  }

  /**
   * {@inheritDoc}
   *
   * @param constraint Constraint
   * @return Resolution
   * @throws QueryException
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    return super.resolver.resolve(constraint);
  }
}
