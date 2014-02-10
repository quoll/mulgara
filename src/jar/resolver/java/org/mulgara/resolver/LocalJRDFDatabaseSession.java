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
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.content.ContentHandlerManager;
import org.mulgara.jrdf.*;
import org.mulgara.query.*;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;
import org.mulgara.server.*;
import org.mulgara.store.statement.StatementStore;
import org.mulgara.transaction.TransactionManagerFactory;

/**
 * A JRDF database session.
 *
 * @created 2004-10-26
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.11 $
 *
 * @modified $Date: 2005/05/19 08:43:59 $ by $Author: raboczi $
 *
 * @maintenanceAuthor $Author: raboczi $
 *
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class LocalJRDFDatabaseSession extends DatabaseSession
    implements LocalJRDFSession {

  /**
   * Logger.
   */
  private static final Logger logger =
      Logger.getLogger(LocalJRDFDatabaseSession.class.getName());

  /**
   * JRDF Graph
   */
  protected JRDFFactory jrdfFactory;

  /**
   * Construct a database session.
   *
   * @param transactionManager  the source of transactions for this session,
   *   never <code>null</code>
   * @param securityAdapterList  {@link List} of {@link SecurityAdapter}s to be
   *   consulted before permitting operations, never <code>null</code>
   * @param symbolicTransformationList  {@link List} of
   *   {@link SymbolicTransformation}s to apply
   * @param resolverSessionFactory  source of {@link ResolverSessionFactory}s,
   *   never <code>null</code>
   * @param systemResolverFactory  Source of {@link SystemResolver}s to manage
   *   persistent models, for instance the system model (<code>#</code>); never
   *   <code>null</code>
   * @param temporaryResolverFactory  Source of {@link Resolver}s to manage
   *   models which only last the duration of a transaction, for instance the
   *   contents of external RDF/XML documents; never <code>null</code>
   * @param resolverFactoryList  the list of registered {@link ResolverFactory}
   *   instances to use for constraint resolution, never <code>null</code>
   * @param externalResolverFactoryMap  map from URL protocol {@link String}s
   *   to {@link ResolverFactory} instances for models accessed via that
   *   protocol, never <code>null</code>
   * @param internalResolverFactoryMap  map from model type {@link LocalNode}s
   *   to {@link ResolverFactory} instances for that model type, never
   *   <code>null</code>
   * @param metadata  even more parameters from the parent {@link Database},
   *   never <code>null</code>
   * @param contentHandlers contains the list of valid registered content
   *   handlers, never <code>null</code>
   * @param cachedResolverFactorySet  contains the resolver factories that are
   *   to have access to their models cached, never <code>null</code>
   *   never <code>null</code>
   * @param temporaryModelTypeURI  the type of model to use as a cache for
   *   external models
   * @throws IllegalArgumentException if any argument is <code>null</code>
   */
  LocalJRDFDatabaseSession(MulgaraTransactionManager transactionManager, 
      TransactionManagerFactory transactionManagerFactory,
      List<SecurityAdapter> securityAdapterList,
      List<SymbolicTransformation> symbolicTransformationList,
      ResolverSessionFactory resolverSessionFactory,
      SystemResolverFactory systemResolverFactory,
      ResolverFactory temporaryResolverFactory,
      List<ResolverFactory> resolverFactoryList,
      Map<String,ResolverFactory> externalResolverFactoryMap,
      Map<URI,InternalResolverFactory> internalResolverFactoryMap,
      DatabaseMetadata metadata,
      ContentHandlerManager contentHandlers,
      Set<ResolverFactory> cachedResolverFactorySet,
      URI temporaryModelTypeURI)
      throws ResolverFactoryException {
    super(transactionManager, transactionManagerFactory, securityAdapterList,
        symbolicTransformationList, resolverSessionFactory,
        systemResolverFactory, temporaryResolverFactory, resolverFactoryList,
        externalResolverFactoryMap, internalResolverFactoryMap, metadata,
        contentHandlers, cachedResolverFactorySet, temporaryModelTypeURI);

    jrdfFactory = new JRDFFactoryImpl();
  }

  public boolean contains(URI modelURI, SubjectNode subject,
      PredicateNode predicate, ObjectNode object) throws GraphException {

    Answer answer = find(modelURI, subject, predicate, object);

    try {

      return answer.getRowCardinality() > 0;
    }
    catch (TuplesException tuplesException) {

      throw new GraphException("Failed to determine if Graph contains Triple.",
          tuplesException);
    }
    finally {

      try {

        answer.close();
      }
      catch (TuplesException tuplesException) {

        //cant do anything...
        logger.warn("Failed to close Answer.", tuplesException);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public Answer find(URI modelURI, SubjectNode subject, PredicateNode predicate,
      ObjectNode object) throws GraphException {

    try {
      //select $Subject $Predicate $Object
      Variable[] vars = new Variable[3];
      vars[0] = StatementStore.VARIABLES[0];
      vars[1] = StatementStore.VARIABLES[1];
      vars[2] = StatementStore.VARIABLES[2];
      ConstraintImpl varConstraint = new ConstraintImpl(vars[0], vars[1],
          vars[2]);
      //where $s $p $o
      ConstraintExpression constraint = varConstraint;
      //and $s <is> <s>
      constraint = appendMulgaraIsConstraint(vars[0], toValue(subject), constraint);
      //and $p <is> <p>
      constraint = appendMulgaraIsConstraint(vars[1], toValue(predicate),
          constraint);
      //and $o <is> <o>
      constraint = appendMulgaraIsConstraint(vars[2], toValue(object), constraint);

      Query query = new Query(
          Arrays.asList(vars),             // variable list
          new GraphResource(modelURI),     // model expression
          constraint,                      // constraint expr
          null,                            // no having
          (List<Order>)Collections.EMPTY_LIST,          // no ordering
          null,                            // no limit
          0,                               // zero offset
          true,                            // distinct results
          new UnconstrainedAnswer()        // nothing given
      );

      //Execute!
      Answer answer = query(query);
      return answer;
    }
    catch (QueryException qe) {
      throw new GraphException("Failed to find the given triple pattern in " +
          " the model.", qe);
    }
  }

  /**
   * If the node is not null, a "and [var] <MULGARA:is> [node]" is appended to the
   * ConstraintExpression.
   *
   * If the node is null, the ConstraintExpression is returned unchanged.
   *
   * @param var Variable
   * @param node Value
   * @param expression ConstraintExpression
   * @throws QueryException
   * @return ConstraintExpression
   */
  private ConstraintExpression appendMulgaraIsConstraint(Variable var, Value node,
      ConstraintExpression expression) throws QueryException {

    if (var == null) {
      throw new IllegalArgumentException("Variable is null.");
    }

    if (node != null) {
      ConstraintExpression constraint = new ConstraintIs(var, node);
      return new ConstraintConjunction(expression, constraint);
    }
    else {
      return expression;
    }
  }

  /**
   * Returns the number of triples in the graph.
   *
   * @return the number of triples in the graph.
   */
  public long getNumberOfTriples(URI graphURI) {

    Answer allTriples = null;
    try {

      allTriples = find(graphURI, null, null, null);
      return allTriples.getRowCount();
    }
    catch (GraphException ge) {
      logger.error("Failed to find statements", ge);
      return 0l;
    }
    catch (TuplesException tuplesException) {
      logger.error("Failed to determine number of statements", tuplesException);
      return 0l;
    }
    finally {

      //try to close the Answer
      if (allTriples != null) {
        try {
          allTriples.close();
        }
        catch (TuplesException tuplesException) {
          logger.warn("Failed to close Answer.", tuplesException);
        }
      }
    }
  }

  /**
   * Returns the JRDF node factory.
   *
   * @return JRDF graph factory.
   */
  public JRDFFactory getJRDFFactory() {
    return jrdfFactory;
  }

  /**
   * Convert a {@link Node} into a {@link Value}.
   *
   * @param node  the JRDF {@link Node} to convert
   * @return an equivalent {@link Node} which is also a {@link Value}
   */
  private Value toValue(Node node) throws QueryException {

    //short circuit if the node is null
    if (node == null) {
      return null;
    }

    // Short circuit if the "node" is already a Value
    if (node instanceof Value) {
      return (Value) node;
    }

    //convert URIReference or Literal Nodes only
    if (node instanceof URIReference) {

      return new URIReferenceImpl(((URIReference) node).getURI());
    }
    else if (node instanceof Literal) {

      Literal literal = (Literal) node;
      String lexicalForm = literal.getLexicalForm();
      String lang = literal.getLanguage();
      URI type = literal.getDatatypeURI();

      if (type != null) {

        return new LiteralImpl(lexicalForm, type);
      }
      else if (lang != null) {

        return new LiteralImpl(lexicalForm, lang);
      }
      else {

        return new LiteralImpl(lexicalForm);
      }
    }

    //node must be external BlankNode
    throw new QueryException("Conversion of alien BlankNodes not implemented: " +
        node);
  }
}
