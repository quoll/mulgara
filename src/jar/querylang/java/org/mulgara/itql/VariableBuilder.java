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

package org.mulgara.itql;

// Java 2 standard packages
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

// Third party packages
import org.apache.log4j.Logger;

// Automatically generated packages (SableCC)
import org.mulgara.itql.analysis.AnalysisAdapter;
import org.mulgara.itql.node.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.URIReferenceImpl;


/**
 * A implementation of SableCC's Adapter which creates list of variables
 * depending on the type of object given (visitor pattern).  Tied heavily to
 * the ItqlInterpreter.
 *
 * @created 2004-06-18
 *
 * @author Andrew Newman
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class VariableBuilder extends AnalysisAdapter {

  /** The logger */
  private final static Logger logger = Logger.getLogger(VariableBuilder.class.getName());

  /** Variable name generator for anonymous columns */
  private VariableFactory variableFactory;

  /** URI Syntax Exception - not null if exception occurred since last get. */
  private URISyntaxException uriException = null;

  /** Query Exception - not null if exception occurred since last get. */
  private QueryException queryException = null;

  /**
   * The list of variables - these are mixed object types:
   * ConstantValue, Variable, Count, SubQuery
   */
  private List<SelectElement> variableList;

  /** The TQL interpreter */
  private SableCCInterpreter interpreter;


  /**
   * Create a new builder.  Requires methods on the interpreter in order to
   * function correctly.
   * @param newInterpreter the interpreter to use.
   */
  public VariableBuilder(SableCCInterpreter newInterpreter, VariableFactory newVariableFactory) {
    variableList = new ArrayList<SelectElement>();
    interpreter = newInterpreter;
    variableFactory = newVariableFactory;
  }


  /**
   * Converts a literal element to a constant and adds it to the variable list.
   * This method is called back from SableCC.
   * @param element the literal element to add to the variable list.
   */
  public void caseALiteralElement(ALiteralElement element) {
    variableList.add(new ConstantValue(
      variableFactory.newVariable(),
      interpreter.toLiteralImpl(((ALiteralElement)element).getLiteral())
    ));
  }


  /**
   * Converts a resource element to a constant and adds it to the variable list.
   * This method is called back from SableCC.
   * @param element the resource element to add to the variable list.
   */
  public void caseAResourceElement(AResourceElement element) {
    // add a new resource to the list
    variableList.add(new ConstantValue(
      variableFactory.newVariable(),
      new URIReferenceImpl(interpreter.toURI(((AResourceElement) element).getResource()))
    ));
  }


  /**
   * Gets the string values of a variable element and adds it to the variable list.
   * This method is called back from SableCC.
   * @param element the variable element to add to the variable list.
   */
  public void caseAVariableElement(AVariableElement element) {
    // get the name of this variable
    String variableName = ((AVariable)((AVariableElement)element).getVariable()).getIdentifier().getText();

    if (logger.isDebugEnabled()) logger.debug("Found variable $" + variableName);

    // add a new variable to the list
    variableList.add(new Variable(variableName));

    if (logger.isDebugEnabled()) logger.debug("Variable list: " + variableList);
  }


  /**
   * Handle an aggregate element.  At the moment it handles both count and
   * sub-queries.  Any further aggregates will be instantiated here.
   * Will set URIException or QueryException if an exception occurs.
   * This method is called back from SableCC.
   * @param element the aggregate element to add to the variable list.
   */
  public void caseAAggregateElement(AAggregateElement element) {
    try {
      AAggregateElement aggregateElement = (AAggregateElement)element;

      // build the elements of the aggregate
      Variable aggregateVariable = variableFactory.newVariable();
      Query aggregateQuery = interpreter.buildQuery(aggregateElement.getQuery());
      
      // create the correct aggregate type - count or subquery
      PAggregate aggregate = aggregateElement.getAggregate();
      if (aggregate instanceof ACountAggregate) {
        variableList.add(new Count(aggregateVariable, aggregateQuery));
      } else if (aggregate instanceof ASubqueryAggregate) {
        variableList.add(new Subquery(aggregateVariable, aggregateQuery));
      } else {
        throw new Error("Unsupported aggregate type: " + aggregate.getClass());
      }
    } catch (QueryException qe) {
      queryException = qe;
    } catch (URISyntaxException use) {
      uriException = use;
    }
  }


  /**
   * Returns the latest variable list or throws an exception if there
   * was an error creating it.  Once called the variable list is cleared
   * and exceptions are nulled.
   * @return A list of: Variable, ConstantValue, Count, Subquery.
   * @throws QueryException if the variable does not is invalid
   * @throws URISyntaxException if the variable contains a resource whose
   *   text violates <a href="http://www.isi.edu/in-notes/rfc2396.txt">RFC?2396</a>
   */
  public List<SelectElement> getVariableList() throws QueryException, URISyntaxException {
    try {
      List<SelectElement> tmpVariableList = new ArrayList<SelectElement>(variableList);

      if (uriException != null) throw uriException;
      else if (queryException != null) throw queryException;
      else return tmpVariableList;

    } finally {
      uriException = null;
      queryException = null;
      variableList.clear();
    }
  }
}
