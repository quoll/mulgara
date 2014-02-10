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

// Java 2 standard packages

// Log4J
import org.apache.log4j.Logger;

//JRDF
import org.jrdf.graph.*;
import org.jrdf.vocabulary.*;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.resolver.gis.tools.*;
import org.mulgara.resolver.spi.*;
import org.mulgara.store.statement.*;
import org.mulgara.store.tuples.*;

/**
 * Wraps a Tuples with the Variables: [$x, $xlat, $xlong, $y, $ylat, $ylong] and
 * generates the following distance statements for each row:
 * $x   $z           $y
 * $y   $z           $x
 * $z   &lt;:type&gt;      &lt;:distance&gt;
 * $z   &lt;:magnitude&gt; "f($xlat,$xlong,$ylat,$ylong)"^^xsd:double
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
public class GISDistanceStatements implements Statements, Cloneable {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(GISDistanceStatements.class.getName());

  /** Number of statements generated per row of the Tuples */
  private static final int STATEMENTS_PER_ROW = 4;

  /** Data source for calculations */
  private Tuples points = null;

  /** Used to calculate GeoSpatial distances */
  private DistanceCalculator calculator = null;

  /** Used to allocate nodes */
  private ResolverSession resolverSession = null;

  /** Local Node Id's used to describe a distance object */
  private long rdfType = -1;
  private long distType = -1;
  private long distMagnitude = -1;

  /** Variables that the Tuples contain */
  public static final Variable X_VAR;
  public static final Variable XLAT_VAR;
  public static final Variable XLONG_VAR;
  public static final Variable Y_VAR;
  public static final Variable YLAT_VAR;
  public static final Variable YLONG_VAR;

  //initialize Variables
  static {
    X_VAR = new Variable("x");
    XLAT_VAR = new Variable("xlat");
    XLONG_VAR = new Variable("xlong");
    Y_VAR = new Variable("y");
    YLAT_VAR = new Variable("ylat");
    YLONG_VAR = new Variable("ylong");
  }

  /** Variable indexes */
  private int xIndex = -1;
  private int xlatIndex = -1;
  private int xlongIndex = -1;
  private int yIndex = -1;
  private int ylatIndex = -1;
  private int ylongIndex = -1;

  /** Current Subject */
  private long subject = -1;

  /** Current Predicate */
  private long predicate = -1;

  /** Current Object */
  private long object = -1;

  /** Current 'x' coordinate */
  private LocalGISCoordinate xCoord = null;

  /** Current 'y' coordinate */
  private LocalGISCoordinate yCoord = null;

  /** Current Distance between 'X' and 'Y' Coordinates */
  private LocalGISDistance distance = null;

  /** Current Row in the statements */
  private long currentRow = -1;

  /** Number of rows Expected */
  private long rowCount = -1;

  /**
   * Constructor.
   *
   * @param points Tuples
   * @param calculator DistanceCalculator
   * @param resolverSession ResolverSession
   * @throws ResolverException
   */
  public GISDistanceStatements(Tuples points, DistanceCalculator calculator,
      ResolverSession resolverSession) throws ResolverException {

    //validate
    if (calculator == null) {
      throw new IllegalArgumentException("DistanceCalculator is null.");
    }
    if (resolverSession == null) {
      throw new IllegalArgumentException("ResolverSession is null.");
    }
    if (points == null) {
      throw new IllegalArgumentException("Tuples is null.");
    }

    //find the Variables
    try {
      //check for emtpy Tuples
      if (!(points.isEmpty())) {
        xIndex = points.getColumnIndex(X_VAR);
        xlatIndex = points.getColumnIndex(XLAT_VAR);
        xlongIndex = points.getColumnIndex(XLONG_VAR);
        yIndex = points.getColumnIndex(Y_VAR);
        ylatIndex = points.getColumnIndex(YLAT_VAR);
        ylongIndex = points.getColumnIndex(YLONG_VAR);
      }
    }
    catch (TuplesException tuplesException) {
      String tuplesVars = "Tuples.Variables: ";
      Variable [] vars = points.getVariables();
      if (vars != null) {
        for (int i = 0; i < vars.length; i++) {
          tuplesVars += vars[i] + ", ";
        }
      } else {
        tuplesVars += "null";
      }
      String tuples = "Tuples: " + points + "(" + points.getClass().getName() + ")";
      throw new ResolverException("Tuples must contain the Variables: " +
          X_VAR + ", " + XLAT_VAR + ", " + XLONG_VAR + ", " + Y_VAR + ", " +
          YLAT_VAR + ", " + YLONG_VAR + ".\n" + tuplesVars + ".\n" + tuples, tuplesException);
    }

    //pre-localize nodes
    try {
      rdfType = resolverSession.localizePersistent(new URIReferenceImpl(RDF.TYPE));
      distType = resolverSession.localizePersistent(LocalGISDistance.RDF_TYPE);
      distMagnitude = resolverSession.localizePersistent(LocalGISDistance.MAGNITUDE);
    }
    catch (LocalizeException localizeException) {
      throw new ResolverException("Failed to pre-localize Nodes.",
          localizeException);
    }

    //initialize members
    this.points = points;
    this.calculator = calculator;
    this.resolverSession = resolverSession;
  }

  /**
   * getSubject
   *
   * @return long
   */
  public long getSubject() throws TuplesException {

    if (subject == -1) {
      throw new TuplesException("beforeFirst() not called or end already reached.");
    }

    return subject;
  }

  /**
   * getPredicate
   *
   * @return long
   */
  public long getPredicate() throws TuplesException {

    if (predicate == -1) {
      throw new TuplesException("beforeFirst() not called or end already reached.");
    }

    return predicate;
  }

  /**
   * getObject
   *
   * @return long
   */
  public long getObject() throws TuplesException {

    if (object == -1) {
      throw new TuplesException("beforeFirst() not called or end already reached.");
    }

    return object;
  }

  /**
   * beforeFirst
   *
   * @throws TuplesException
   */
  public void beforeFirst() throws TuplesException {
    currentRow = -1;
    subject = -1;
    predicate = -1;
    object = -1;
    xCoord = null;
    yCoord = null;
    distance = null;
    points.beforeFirst();
  }

  /**
   * Statements have 3 columns: $subject, $predicate, $object
   *
   * @param column Variable
   * @return int
   * @throws TuplesException
   */
  public int getColumnIndex(Variable column) throws TuplesException {

    if (column.equals(StatementStore.VARIABLES[0])) {
      return 0;
    }
    else if (column.equals(StatementStore.VARIABLES[1])) {
      return 1;
    }
    else if (column.equals(StatementStore.VARIABLES[2])) {
      return 2;
    }
    else {
      throw new TuplesException("Statements does not contain Variable: " +
          column);
    }
  }

  /**
   * Statements have 3 columns: $subject, $predicate, $object
   *
   * @return int
   */
  public int getNumberOfVariables() {
    return 3;
  }

  /**
   * Statements have 3 columns: $subject, $predicate, $object
   *
   * @return Variable[]
   */
  public Variable[] getVariables() {
    return new Variable[] {
        StatementStore.VARIABLES[0],
        StatementStore.VARIABLES[1],
        StatementStore.VARIABLES[2]
    };
  }

  /**
   * isUnconstrained
   *
   * @return boolean
   */
  public boolean isUnconstrained() {
    return false;
  }

  /**
   * Returns the number of rows in this Statements.
   *
   * @return long
   * @throws TuplesException
   */
  public long getRowCount() throws TuplesException {

    //lazily evaluate
    if (rowCount == -1) {
      rowCount = points.getRowCount() * STATEMENTS_PER_ROW;
    }
    return rowCount;
  }

  /**
   * getRowUpperBound
   *
   * @return long
   * @throws TuplesException
   */
  public long getRowUpperBound() throws TuplesException {
    return points.getRowUpperBound() * STATEMENTS_PER_ROW;
  }

  /**
   * getRowExpectedCount
   *
   * @return The expected wize of this result
   * @throws TuplesException when accessing data
   */
  public long getRowExpectedCount() throws TuplesException {
    return points.getRowExpectedCount() * STATEMENTS_PER_ROW;
  }

  /**
   * getRowCardinality
   *
   * @return int
   * @throws TuplesException
   */
  public int getRowCardinality() throws TuplesException {
    return points.getRowCardinality();
  }

  /* (non-Javadoc)
   * @see org.mulgara.query.Cursor#isEmpty()
   */
  public boolean isEmpty() throws TuplesException {
    return points.isEmpty();
  }

  /**
   * Determines which 'sub row' to return from the tuples.
   *
   * @return boolean
   * @throws TuplesException
   */
  public boolean next() throws TuplesException {

    //are there any statements?
    if (points.isEmpty()) {
      return false;
    }

    //reset statement
    subject = -1;
    predicate = -1;
    object = -1;

    //has the end been reached already?
    if (currentRow >= getRowCount()) {
      return false;
    }
    currentRow++;

    //what 'sub statement' is next?
    int subRow = (int) currentRow % STATEMENTS_PER_ROW;
    switch (subRow) {

      case (0):

        //are there any more Tuples
        if (!nextInTuples()
            || (xCoord == null) || (yCoord == null) || (distance == null)) {
          return false;
        }
        //$x $z $y
        subject = xCoord.getNodeId();
        predicate = distance.getNodeId();
        object = yCoord.getNodeId();
        break;

      case (1):

        //$y $z $x
        subject = yCoord.getNodeId();
        predicate = distance.getNodeId();
        object = xCoord.getNodeId();
        break;

      case (2):

        //$z rdf:type :distance
        subject = distance.getNodeId();
        predicate = rdfType;
        object = distType;
        break;

      case (3):

        //$z :magnitude "size"
        subject = distance.getNodeId();
        predicate = distMagnitude;
        object = localizeMagnitude(distance.getMagnitude());
        break;

      default:
        throw new TuplesException(
            "Failed to determine current statement position.");
    }

    return true;
  }

  /**
   * Creates a Literal node and localizes it for the given magnitude.
   *
   * @param mag double
   * @throws TuplesException
   * @return long
   */
  public long localizeMagnitude(double mag) throws TuplesException {
    try {
      Literal literal = new LiteralImpl(mag);
      return resolverSession.localizePersistent(literal);
    }
    catch (LocalizeException localException) {
      throw new TuplesException("Failed to localize magnitude.",
          localException);
    }
  }

  /**
   * calls next on the Tuples and loads the next X & Y coordinates
   *
   * @return boolean
   * @throws TuplesException
   */
  public boolean nextInTuples() throws TuplesException {
    //reset 'old' coords
    xCoord = null;
    yCoord = null;
    distance = null;
    //are there any more?
    boolean next = points.next();
    if (next) {
      //get 'X'
      long x = points.getColumnValue(xIndex);
      long xlat = points.getColumnValue(xlatIndex);
      long xlong = points.getColumnValue(xlongIndex);
      xCoord = new LocalGISCoordinate(x, xlat, xlong, resolverSession);
      //get 'Y'
      long y = points.getColumnValue(yIndex);
      long ylat = points.getColumnValue(ylatIndex);
      long ylong = points.getColumnValue(ylongIndex);
      yCoord = new LocalGISCoordinate(y, ylat, ylong, resolverSession);
      //determine distance
      distance = new LocalGISDistance(xCoord, yCoord, resolverSession,
          calculator);
    }
    return next;
  }

  /**
   * close
   *
   * @throws TuplesException
   */
  public void close() throws TuplesException {
    points.close();
  }

  /**
   * Returns a new GISDistanceStatements using a copy of this Statements Tuples.
   * @return Object
   */
  public Object clone() {
    try {
      GISDistanceStatements gds = (GISDistanceStatements)super.clone();
      gds.points = (Tuples)points.clone();
      return gds;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Failed to clone GISDistanceStatements.", e);
    }
  }

}
