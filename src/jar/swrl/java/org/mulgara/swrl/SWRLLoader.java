/*
 * Copyright 2009 Revelytix.
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
package org.mulgara.swrl;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.URIReference;
import org.jrdf.vocabulary.OWL;
import org.jrdf.vocabulary.RDF;
import org.mulgara.itql.VariableFactoryImpl;
import org.mulgara.krule.ConsistencyCheck;
import org.mulgara.krule.KruleStructureException;
import org.mulgara.krule.QueryStruct;
import org.mulgara.krule.Rule;
import org.mulgara.krule.RuleStructure;
import org.mulgara.query.Answer;
import org.mulgara.query.ConstantValue;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintFilter;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.ConstraintIs;
import org.mulgara.query.ConstraintOperation;
import org.mulgara.query.GraphExpression;
import org.mulgara.query.GraphResource;
import org.mulgara.query.Order;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.SelectElement;
import org.mulgara.query.SingleTransitiveConstraint;
import org.mulgara.query.TuplesException;
import org.mulgara.query.UnconstrainedAnswer;
import org.mulgara.query.Value;
import org.mulgara.query.Variable;
import org.mulgara.query.VariableFactory;
import org.mulgara.query.filter.And;
import org.mulgara.query.filter.Equals;
import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.query.filter.value.DataTypeFn;
import org.mulgara.query.filter.value.IRI;
import org.mulgara.query.filter.value.TypedLiteral;
import org.mulgara.query.filter.value.Var;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.SWRL;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.VariableNodeImpl;
import org.mulgara.resolver.OperationContext;
import org.mulgara.rules.InitializerException;
import org.mulgara.rules.RuleLoader;
import org.mulgara.rules.Rules;
import org.mulgara.util.functional.Pair;

/**
 * Implementation of a rule loader which parses rule definitions from an RDF
 * graph according to the SWRL schema.
 * 
 * @created Jun 4, 2009
 * @author Alex Hall
 * @copyright &copy; 2009 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SWRLLoader implements RuleLoader {
  
  private static final Logger logger = Logger.getLogger(SWRLLoader.class.getName());
  
  /** A field used in queries to indicate no prior constraints on the answer. */
  private static final UnconstrainedAnswer UNCONSTRAINED = new UnconstrainedAnswer();

  /** RDF reference for rdf:type. */
  public static final URIReferenceImpl RDF_TYPE = new URIReferenceImpl(RDF.TYPE);
  
  /** The graph from which rule definitions are being read. */
  private final GraphResource ruleGraph;
  /** The graph expression containing the base statements. */
  private final GraphExpression baseGraph;
  /** The graph where entailed statements will be inserted. */
  private final URI destGraph;
  
  /** The database session for querying. */
  private OperationContext operationContext;
  
  /** Factory for creating unique variables in the rule queries. */
  private VariableFactory varFactory = new VariableFactoryImpl();

  /**
   * Factory method.
   * @param ruleGraph The graph URI that contains the SWRL rule definitions. 
   * @param baseGraph The graph expression that contains the base statements.
   * @param destGraph The graph URI that will receive the entailed statements.
   * @return The rule loader which will process the rule definitions.
   */
  public static RuleLoader newInstance(URI ruleGraph, GraphExpression baseGraph, URI destGraph) {
    return new SWRLLoader(ruleGraph, baseGraph, destGraph);
  }
  
  /**
   * Initialize the rule loader with the rule, base, and destination graphs.
   * @param ruleGraph The graph containing the rules to load.
   * @param baseGraph The graph expression containing the base statements.
   * @param destGraph The destination graph for entailed statements.
   */
  SWRLLoader(URI ruleGraph, GraphExpression baseGraph, URI destGraph) {
    this.ruleGraph = new GraphResource(ruleGraph);
    this.baseGraph = baseGraph;
    this.destGraph = destGraph;
  }
  
  /* (non-Javadoc)
   * @see org.mulgara.rules.RuleLoader#readRules(java.lang.Object)
   */
  public Rules readRules(Object session) throws InitializerException, RemoteException {
    this.operationContext = (OperationContext)session;
    
    RuleStructure rules = new RuleStructure();
    rules.setTargetModel(destGraph);
    
    try {
      List<Node> ruleNodes = findRules();
      if (ruleNodes.isEmpty()) {
        logger.debug("No SWRL data.");
        return null;
      }
      
      if (logger.isDebugEnabled()) logger.debug("Found rules: " + ruleNodes);
      
      Map<URIReference,Variable> vars = findVariables();
      if (logger.isDebugEnabled()) logger.debug("Found variables: " + vars);
      
      Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms = new HashMap<Node,Pair<URI,ConstraintImpl>>();
      Map<Node,Pair<URI,Filter>> filterAtoms = new HashMap<Node,Pair<URI,Filter>>();
      findAtoms(constraintAtoms, filterAtoms, vars);
      if (logger.isDebugEnabled()) {
        logger.debug("Found constraint atoms: " + constraintAtoms);
        logger.debug("Found filter atoms: " + filterAtoms);
      }
      
      for (Node ruleNode : ruleNodes) {
        buildRule(ruleNode, rules, constraintAtoms, filterAtoms);
      }
      
      processTriggers(rules);
    } catch (TuplesException te) {
      logger.error("Exception while accessing rule data.", te);
      throw new InitializerException("Problem accessing rule data", te);
    } catch (QueryException qe) {
      logger.error("Exception while reading rules.", qe);
      throw new InitializerException("Problem reading rules", qe);
    } catch (SWRLStructureException se) {
      logger.error("Error in rule RDF data:" + se.getMessage(), se);
      throw new InitializerException("Problem in rules RDF", se);
    }
    
    return rules;
  }

  /**
   * Find all variable declarations in the rule graph.  Variable references are
   * identified using an <tt>rdf:type</tt> of <tt>swrl:Variable</tt>.
   * @return A mapping of variable URI reference to a variable object which will
   *         represent that reference in all subsequent queries.
   */
  private Map<URIReference,Variable> findVariables() throws QueryException, TuplesException {
    // select $var from <ruleGraph> where $var <rdf:type> <swrl:Variable>
    Variable varV = new Variable("var");
    ConstraintExpression where = new ConstraintImpl(varV, RDF_TYPE, SWRL.VARIABLE);
    Query query = createQuery(where, varV);
    if (logger.isDebugEnabled()) logger.debug("Variable query: " + query);
    Answer answer = doQuery(query);
    
    Map<URIReference,Variable> variables = new HashMap<URIReference,Variable>();
    
    try {
      answer.beforeFirst();
      while (answer.next()) {
        Object obj = answer.getObject(varV.getName());
        if (logger.isDebugEnabled()) logger.debug("Found variable: " + obj);
        if (obj instanceof URIReference) {
          Variable var = varFactory.newVariable();
          variables.put((URIReference)obj, var);
        }
      }
    } finally {
      answer.close();
    }
    
    return variables;
  }
  
  /**
   * Find all resources in the rule graph that represent SWRL rules (a.k.a. implications).
   * A rule is identified by an <tt>rdf:type</tt> of <tt>swrl:Imp</tt>.
   * @return A list of RDF nodes that represent rule resources.
   */
  private List<Node> findRules() throws QueryException, TuplesException {
    // select $rule from <ruleGraph> where $rule <rdf:type> <swrl:Imp>
    Variable ruleVar = new Variable("rule");
    ConstraintExpression where = new ConstraintImpl(ruleVar, RDF_TYPE, SWRL.IMP);
    Query query = createQuery(where, ruleVar);
    if (logger.isDebugEnabled()) logger.debug("Rule query: " + query);
    Answer answer = doQuery(query);
    
    List<Node> rules = new ArrayList<Node>();
    try {
      answer.beforeFirst();
      while (answer.next()) {
        rules.add((Node)answer.getObject(ruleVar.getName()));
      }
    } finally {
      answer.close();
    }
    
    return rules;
  }
  
  /**
   * Finds all SWRL atoms in the rule graph, and convert them into query triple patterns
   * or filters as appropriate.  The triple patterns and filters are inserted into the supplied
   * maps, and variable mappings are performed when the patterns and filters are constructed.
   * @param constraintAtoms This object will be populated with SWRL atoms that convert to RDF
   *        triple patterns. The mapping is from the atom's RDF node to a [atom type URI, triple pattern] pair.
   * @param filterAtoms This object will be populated with SWRL atoms that convert to SPARQL
   *        filters. The mapping is from the atom's RDF node to a [atom type URI, filter] pair.
   * @param varMap A map of variable URI references to variable objects to use when constructing
   *        triple patterns and filters.
   */
  private void findAtoms(Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms, Map<Node,Pair<URI,Filter>> filterAtoms, Map<URIReference,Variable> varMap)
      throws QueryException, TuplesException, SWRLStructureException {
    findClassAtoms(constraintAtoms, varMap);
    findIndividualAtoms(constraintAtoms, varMap);
    findDataAtoms(constraintAtoms, varMap);
    findIdentityAtoms(constraintAtoms, varMap);
    findDataRangeAtoms(filterAtoms, varMap);
    findBuiltinAtoms(filterAtoms, varMap);
  }
  
  /**
   * Find all atoms in the rule graph of type <tt>swrl:ClassAtom</tt> and insert the resulting
   * triple patterns into the supplied map.  Note that only named classes are supported.
   * A class atom has the form:
   * <pre>
   * _:x rdf:type swrl:ClassAtom .
   * _:x swrl:classPredicate ex:MyClass .
   * _:x swrl:argument1 rule:varX .
   * </pre>
   * and will be translated into the triple pattern: <tt>[$varX rdf:type ex:MyClass]</tt>
   * @param constraintAtoms The map into which the triple patterns are inserted.
   * @param varMap The variable reference map which is used to construct the triple patterns.
   */
  private void findClassAtoms(Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms, Map<URIReference,Variable> varMap)
      throws QueryException, TuplesException, SWRLStructureException {
    // select $atom $class $arg from <ruleGraph>
    // where $atom <rdf:type> <swrl:ClassAtom> and $atom <swrl:classPredicate> $class and $atom <swrl:argument1> $arg
    Variable atomVar = new Variable("atom");
    Variable classVar = new Variable("class");
    Variable argVar = new Variable("arg");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(atomVar, RDF_TYPE, SWRL.CLASS_ATOM),
        new ConstraintImpl(atomVar, SWRL.CLASS_PREDICATE, classVar),
        new ConstraintImpl(atomVar, SWRL.ARG_1, argVar)
    );
    
    Query query = createQuery(where, atomVar, classVar, argVar);
    Answer answer = doQuery(query);
    
    try {
      answer.beforeFirst();
      while (answer.next()) {
        Node atom = (Node)answer.getObject(atomVar.getName());
        Object classObj = answer.getObject(classVar.getName());
        checkClass(classObj, URIReference.class, "Only named classes may be used with class atoms.");
        Object argObj = answer.getObject(argVar.getName());
        checkClass(argObj, URIReference.class, "Argument of a class atom may only be a URI or variable reference");
        constraintAtoms.put(atom, new Pair<URI,ConstraintImpl>(SWRL.CLASS_ATOM.getURI(),
            toConstraint((URIReference)argObj, RDF_TYPE, (URIReference)classObj, varMap)));
      }
    } finally {
      answer.close();
    }
  }
  
  /**
   * Find all atoms in the rule graph of type <tt>swrl:IndividualPropertyAtom</tt> and insert
   * the resulting triple patterns into the supplied map.  An individual property atom has the form:
   * <pre>
   * _:x rdf:type swrl:IndividualPropertyAtom .
   * _:x swrl:propertyPredicate ex:myObjectProperty .
   * _:x swrl:argument1 ex:myIndividual .
   * _:x swrl:argument2 rule:varX .
   * </pre>
   * and will be translated to the triple pattern: <tt>[ex:myIndividual ex:myObjectProperty $varX]</tt>.
   * @param constraintAtoms The map into which the triple patterns are inserted.
   * @param varMap The variable reference map which is used to construct the triple patterns.
   */
  private void findIndividualAtoms(Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms, Map<URIReference,Variable> varMap)
      throws QueryException, TuplesException, SWRLStructureException {
    // select $atom $property $arg1 $arg2 from <ruleGraph>
    // where $atom <rdf:type> <swrl:IndividualPropertyAtom> and $atom <swrl:propertyPredicate> $property
    //   and $atom <swrl:argument1> $arg1 and $atom <swrl:argument2> $arg2;
    Variable atomVar = new Variable("atom");
    Variable propertyVar = new Variable("property");
    Variable arg1Var = new Variable("arg1");
    Variable arg2Var = new Variable("arg2");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(atomVar, RDF_TYPE, SWRL.INDIVIDUAL_ATOM),
        new ConstraintImpl(atomVar, SWRL.PROPERTY_PREDICATE, propertyVar),
        new ConstraintImpl(atomVar, SWRL.ARG_1, arg1Var),
        new ConstraintImpl(atomVar, SWRL.ARG_2, arg2Var)
    );
    
    Query query = createQuery(where, atomVar, propertyVar, arg1Var, arg2Var);
    Answer answer = doQuery(query);
    
    try {
      answer.beforeFirst();
      while (answer.next()) {
        Node atom = (Node)answer.getObject(atomVar.getName());
        Object subjectObj = answer.getObject(arg1Var.getName());
        checkClass(subjectObj, URIReference.class, "Subject of an individual property atom must be a URI or variable reference");
        Object propertyObj = answer.getObject(propertyVar.getName());
        checkClass(propertyObj, URIReference.class, "Predicate of an individual property atom must be a URI");
        Node object = (Node)answer.getObject(arg2Var.getName());
        constraintAtoms.put(atom, new Pair<URI,ConstraintImpl>(SWRL.INDIVIDUAL_ATOM.getURI(),
            toConstraint((URIReference)subjectObj, (URIReference)propertyObj, object, varMap)));
      }
    } finally {
      answer.close();
    }
  }
  
  /**
   * Find all atoms in the rule graph of type <tt>swrl:DatavaluedPropertyAtom</tt> and insert
   * the resulting triple patterns into the supplied map.  An individual property atom has the form:
   * <pre>
   * _:x rdf:type swrl:DatavaluedPropertyAtom .
   * _:x swrl:propertyPredicate ex:myDataProperty .
   * _:x swrl:argument1 ex:myIndividual .
   * _:x swrl:argument2 rule:varX .
   * </pre>
   * and will be translated to the triple pattern: <tt>[ex:myIndividual ex:myDataProperty $varX]</tt>.
   * @param constraintAtoms The map into which the triple patterns are inserted.
   * @param varMap The variable reference map which is used to construct the triple patterns.
   */
  private void findDataAtoms(Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms, Map<URIReference,Variable> varMap)
      throws QueryException, TuplesException, SWRLStructureException {
    // select $atom $property $arg1 $arg2 from <ruleGraph>
    // where $atom <rdf:type> <swrl:DatavaluedPropertyAtom> and $atom <swrl:propertyPredicate> $property
    //   and $atom <swrl:argument1> $arg1 and $atom <swrl:argument2> $arg2;
    Variable atomVar = new Variable("atom");
    Variable propertyVar = new Variable("property");
    Variable arg1Var = new Variable("arg1");
    Variable arg2Var = new Variable("arg2");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(atomVar, RDF_TYPE, SWRL.DATA_ATOM),
        new ConstraintImpl(atomVar, SWRL.PROPERTY_PREDICATE, propertyVar),
        new ConstraintImpl(atomVar, SWRL.ARG_1, arg1Var),
        new ConstraintImpl(atomVar, SWRL.ARG_2, arg2Var)
    );
    
    Query query = createQuery(where, atomVar, propertyVar, arg1Var, arg2Var);
    Answer answer = doQuery(query);
    
    try {
      answer.beforeFirst();
      while (answer.next()) {
        Node atom = (Node)answer.getObject(atomVar.getName());
        Object subjectObj = answer.getObject(arg1Var.getName());
        checkClass(subjectObj, URIReference.class, "Subject of a data-valued property atom must be a URI or variable reference");
        Object propertyObj = answer.getObject(propertyVar.getName());
        checkClass(propertyObj, URIReference.class, "Predicate of a data-valued property atom must be a URI");
        Node object = (Node)answer.getObject(arg2Var.getName());
        constraintAtoms.put(atom, new Pair<URI,ConstraintImpl>(SWRL.DATA_ATOM.getURI(),
            toConstraint((URIReference)subjectObj, (URIReference)propertyObj, object, varMap)));
      }
    } finally {
      answer.close();
    }
  }
  
  /**
   * Find all identity atoms in the rule graph (i.e. atoms of type <tt>swrl:SameIndividualsAtom</tt>
   * and <tt>swrl:DifferentIndividualsAtom</tt>) an insert the resulting triple patterns into
   * the supplied map.  Identity atoms take the following form:
   * <pre>
   * _:x rdf:type swrl:SameIndividualsAtom .
   * _:x swrl:argument1 ex:individualA .
   * _:x swrl:argument2 rule:varX .
   * 
   * _:y rdf:type swrl:DifferentIndividualsAtom .
   * _:x swrl:argument1 ex:individualB .
   * _:x swrl:argument2 rule:varY .
   * </pre>
   * and are translated into the triple patterns: <tt>[ex:individualA owl:sameAs $varX]</tt>
   * and <tt>[ex:individualB owl:differentFrom $varY]</tt>.
   * @param constraintAtoms The map into which the triple patterns are inserted.
   * @param varMap The variable reference map which is used to construct the triple patterns.
   */
  private void findIdentityAtoms(Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms, Map<URIReference,Variable> varMap)
      throws QueryException, TuplesException, SWRLStructureException {
    // select $atom $type $arg1 $arg2 from <ruleGraph>
    // where $atom <rdf:type> $type and $atom <swrl:argument1> $arg1 and $atom <swrl:argument2> $arg2
    //   and ($type <mulgara:is> <swrl:SameIndividualsAtom> or $type <mulgara:is> <swrl:DifferentIndividualsAtom>)
    Variable atomVar = new Variable("atom");
    Variable typeVar = new Variable("type");
    Variable arg1Var = new Variable("arg1");
    Variable arg2Var = new Variable("arg2");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(atomVar, RDF_TYPE, typeVar),
        new ConstraintImpl(atomVar, SWRL.ARG_1, arg1Var),
        new ConstraintImpl(atomVar, SWRL.ARG_2, arg2Var),
        new ConstraintDisjunction(
            new ConstraintIs(typeVar, SWRL.SAME_INDIVIDUALS_ATOM),
            new ConstraintIs(typeVar, SWRL.DIFFERENT_INDIVIDUALS_ATOM))
    );
    
    Query query = createQuery(where, atomVar, typeVar, arg1Var, arg2Var);
    Answer answer = doQuery(query);
    
    try {
      answer.beforeFirst();
      while (answer.next()) {
        Node atom = (Node)answer.getObject(atomVar.getName());
        URIReference type = (URIReference)answer.getObject(typeVar.getName());
        Object arg1Obj = answer.getObject(arg1Var.getName());
        checkClass(arg1Obj, URIReference.class, "Arguments to identity atoms must be URI or variable references");
        Object arg2Obj = answer.getObject(arg2Var.getName());
        checkClass(arg2Obj, URIReference.class, "Arguments to identity atoms must be URI or variable references");
        
        URIReference pred = null;
        if (SWRL.SAME_INDIVIDUALS_ATOM.equals(type)) {
          pred = new URIReferenceImpl(OWL.SAME_AS);
        } else if (SWRL.DIFFERENT_INDIVIDUALS_ATOM.equals(type)) {
          pred = new URIReferenceImpl(OWL.DIFFERENT_FROM);
        } else {
          throw new IllegalStateException("Unexpected type result for identiy atom: " + type);
        }
        
        constraintAtoms.put(atom, new Pair<URI,ConstraintImpl>(type.getURI(),
            toConstraint((URIReference)arg1Obj, pred, (URIReference)arg2Obj, varMap)));
      }
    } finally {
      answer.close();
    }
  }
  
  /**
   * Find all atoms in the rule graph of type <tt>swrl:DataRangeAtom</tt> and insert the
   * resulting filters into the supplied map.  Note that only named datatypes are supported.
   * A data range atom has the form:
   * <pre>
   * _:x rdf:type swrl:DataRangeAtom .
   * _:x swrl:dataRange xsd:integer .
   * _:x swrl:argument1 rule:varX .
   * </pre>
   * and is mapped to the SPARQL filter clause: <tt>FILTER( datatype(?varX) = xsd:integer )</tt>.
   * @param filterAtoms The map into which the filters are inserted.
   * @param varMap The variable reference map which is used to construct the triple patterns.
   */
  private void findDataRangeAtoms(Map<Node,Pair<URI,Filter>> filterAtoms, Map<URIReference,Variable> varMap)
      throws QueryException, TuplesException, SWRLStructureException {
    // select $atom $range $arg from <ruleGraph>
    // where $atom <rdf:type> <swrl:DataRangeAtom> and $atom <swrl:dataRange> $range and $atom <swrl:argument1> $arg
    Variable atomVar = new Variable("atom");
    Variable rangeVar = new Variable("range");
    Variable argVar = new Variable("arg");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(atomVar, RDF_TYPE, SWRL.DATA_RANGE_ATOM),
        new ConstraintImpl(atomVar, SWRL.DATA_RANGE, rangeVar),
        new ConstraintImpl(atomVar, SWRL.ARG_1, argVar)
    );
    
    Query query = createQuery(where, atomVar, rangeVar, argVar);
    Answer answer = doQuery(query);
    
    try {
      answer.beforeFirst();
      while (answer.next()) {
        Node atom = (Node)answer.getObject(atomVar.getName());
        Object rangeObj = answer.getObject(rangeVar.getName());
        checkClass(rangeObj, URIReference.class, "Data range atom must specify a named datatype or variable reference");
        Node arg = (Node)answer.getObject(argVar.getName());
        Filter filter = new Equals(toRdfTerm((URIReference)rangeObj, varMap), new DataTypeFn(toRdfTerm(arg, varMap)));
        filterAtoms.put(atom, new Pair<URI,Filter>(SWRL.DATA_RANGE_ATOM.getURI(), filter));
      }
    } finally {
      answer.close();
    }
  }
  
  /**
   * Find all atoms in the rule graph of type <tt>swrl:BuiltinAtom</tt> and insert the
   * resulting filters into the supplied map.  Currently unimplemented.
   * @param filterAtoms The map into which the filters are inserted.
   * @param varMap The variable reference map which is used to construct the triple patterns.
   */
  private void findBuiltinAtoms(Map<Node,Pair<URI,Filter>> filterAtoms, Map<URIReference,Variable> varMap)
      throws QueryException, TuplesException, SWRLStructureException {
    // TODO Implement me.
  }
  
  /**
   * Builds the rule identified by the given RDF node, and inserts it into the supplied
   * rule structure.  Rules handled by this method may be ordinary rules, consistency checks,
   * or axioms, depending on the number of atoms in the body and head of the SWRL implication.
   * @param ruleNode The RDF node that identifies the rule description to build.
   * @param rules The rule structure to hold the new rule.
   * @param constraintAtoms A pre-loaded map containing all triple-pattern atoms in the rule graph.
   * @param filterAtoms A pre-loaded map containing all filter atoms in the rule graph.
   */
  private void buildRule(Node ruleNode, RuleStructure rules,
      Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms, Map<Node,Pair<URI,Filter>> filterAtoms)
      throws QueryException, TuplesException, SWRLStructureException {
    if (logger.isDebugEnabled()) logger.debug("Building structure for rule: " + ruleNode);
    
    Set<Node> bodyAtoms = getListMembers(ruleNode, SWRL.BODY);
    if (logger.isDebugEnabled()) logger.debug("Found body atoms: " + bodyAtoms);
    
    Set<Node> headAtoms = getListMembers(ruleNode, SWRL.HEAD);
    if (logger.isDebugEnabled()) logger.debug("Found head atoms: " + headAtoms);
    
    if (bodyAtoms.isEmpty() && headAtoms.isEmpty()) throw new SWRLStructureException("Rule must have at least one atom: " + ruleNode);
    
    if (bodyAtoms.isEmpty()) {
      // Empty body means it's an axiom
      for (Node headAtom : headAtoms) {
        rules.addAxiom(toAxiom(getHeadAtom(headAtom, constraintAtoms, filterAtoms)));
      }
    } else {
      // Either a regular rule or a consistency check; both have queries.
      ConstraintExpression whereClause = buildWhereClause(bodyAtoms, constraintAtoms, filterAtoms);
      Rule rule = null;
      List<SelectElement> selectVars = null;
      
      if (headAtoms.isEmpty()) {
        rule = new ConsistencyCheck(ruleNode.toString());
        selectVars = new ArrayList<SelectElement>(whereClause.getVariables());
      } else {
        rule = new Rule(ruleNode.toString());
        selectVars = getSelectElements(headAtoms, constraintAtoms, filterAtoms);
      }
      
      QueryStruct queryStruct = new QueryStruct(selectVars);
      queryStruct.setWhereClause(whereClause);
      queryStruct.setGraphExpression(baseGraph, destGraph);
      try {
        rule.setQueryStruct(queryStruct);
      } catch (KruleStructureException e) {
        throw new SWRLStructureException("Illegal query structure", e);
      }
      rules.add(rule);
    }
  }
  
  /**
   * Gets the triple pattern identified by the given atom node, or throws an exception if
   * the given atom does not map to a triple pattern.
   * @param node The RDF node representing an atom appearing in a rule head.
   * @param constraintAtoms All triple-pattern atoms in the rule graph.
   * @param filterAtoms All filter atoms in the rule graph.
   * @return The triple pattern mapped to the given RDF node.
   * @throws SWRLStructureException if the RDF node does not represent a triple pattern atom.
   */
  private ConstraintImpl getHeadAtom(Node node, Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms,
      Map<Node,Pair<URI,Filter>> filterAtoms) throws SWRLStructureException {
    if (constraintAtoms.containsKey(node)) {
      return constraintAtoms.get(node).second();
    } else if (filterAtoms.containsKey(node)) {
      throw new SWRLStructureException("Atoms in rule head must map to triple patterns; found: " + filterAtoms.get(node).first());
    } else {
      throw new SWRLStructureException("Unable to map entry in rule head to an atom: " + node);
    }
  }
  
  /**
   * Converts a query triple pattern to an axiomatic statement.
   * @param pattern A query triple pattern.
   * @return The equivalent axiomatic statement.
   * @throws SWRLStructureException if any position in the triple pattern contains a variable.
   */
  private Triple toAxiom(ConstraintImpl pattern) throws SWRLStructureException {
    ConstraintElement c = pattern.getElement(0);
    if (c instanceof Variable) throw new SWRLStructureException("Axioms may not contain variables.");
    SubjectNode s = (SubjectNode)c;
    c = pattern.getElement(1);
    if (c instanceof Variable) throw new SWRLStructureException("Axioms may not contain variables.");
    PredicateNode p = (PredicateNode)c;
    c = pattern.getElement(2);
    if (c instanceof Variable) throw new SWRLStructureException("Axioms may not contain variables.");
    ObjectNode o = (ObjectNode)c;
    return new TripleImpl(s, p, o);
  }
  
  /**
   * Builds the where clause for a query given a collection of atoms in a rule body.
   * The atoms from the rule body may map to triple patterns or filters, but at least one
   * atom must map to a triple pattern.
   * @param bodyAtoms A collection of RDF nodes that are atoms in the body of a SWRL implication.
   * @param constraintAtoms All triple-pattern atoms in the rule graph.
   * @param filterAtoms All filter atoms in the rule graph.
   * @return The where clause for a query that represents the rule body.
   */
  private ConstraintExpression buildWhereClause(Set<Node> bodyAtoms, Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms,
      Map<Node,Pair<URI,Filter>> filterAtoms) throws SWRLStructureException {
    List<ConstraintExpression> triplePatterns = new ArrayList<ConstraintExpression>();
    List<Filter> filters = new ArrayList<Filter>();
    
    for (Node bodyAtom : bodyAtoms) {
      if (constraintAtoms.containsKey(bodyAtom)) {
        triplePatterns.add(constraintAtoms.get(bodyAtom).second());
      } else if (filterAtoms.containsKey(bodyAtom)) {
        filters.add(filterAtoms.get(bodyAtom).second());
      } else {
        throw new SWRLStructureException("Unable to map entry in rule head to an atom: " + bodyAtom);
      }
    }
    
    if (triplePatterns.isEmpty()) {
      throw new SWRLStructureException("Rule body must contain at least one triple pattern.");
    }
    
    ConstraintExpression whereClause = new ConstraintConjunction(triplePatterns);
    if (!filters.isEmpty()) {
      Filter filter = (filters.size() == 1) ? filters.get(0) : new And(filters.toArray(new Filter[filters.size()]));
      whereClause = new ConstraintFilter(whereClause, filter);
    }
    return whereClause;
  }
  
  /**
   * Construct a list of select elements that are specified by triple patterns in
   * the head of a SWRL rule.  The select elements may be either variables or constant values.
   * @param headNodes A collection of RDF nodes that comprise the atoms in the head of a SWRL rule.
   * @param constraintAtoms All triple-pattern atoms in the rule graph.
   * @param filterAtoms All filter atoms in the rule graph.
   * @return The list of elements that represent statements to be entailed by the rule.
   */
  private List<SelectElement> getSelectElements(Set<Node> headNodes, Map<Node,Pair<URI,ConstraintImpl>> constraintAtoms,
      Map<Node,Pair<URI,Filter>> filterAtoms) throws SWRLStructureException {
    List<SelectElement> selectElements = new ArrayList<SelectElement>();
    for (Node headNode : headNodes) {
      ConstraintImpl pattern = getHeadAtom(headNode, constraintAtoms, filterAtoms);
      for (int i = 0; i < 3; i++) {
        ConstraintElement e = pattern.getElement(i);
        selectElements.add(e instanceof Variable ? (Variable)e : new ConstantValue(varFactory.newVariable(), (Value)e));
      }
    }
    return selectElements;
  }
  
  /**
   * Utility method to get all members of the RDF list whose head is the object of
   * an RDF statement with the given subject and predicate.
   * @param subject The subject of a statement which identifies the list.
   * @param predicate The predicate of a statement which identifies the list.
   * @return A collection of RDF nodes which comprise the given list.
   */
  private Set<Node> getListMembers(Node subject, URIReferenceImpl predicate) throws QueryException, TuplesException {
    // select $value from <ruleGraph>
    // where <parent> <predicate> $head
    //   and (trans($head <rdf:rest> $node) or $head <rdf:rest> $node)
    //   and ($head <rdf:first> $value or $node <rdf:first> $value)
    Variable valueVar = new Variable("value");
    Variable headVar = new Variable("head");
    Variable nodeVar = new Variable("node");
    URIReferenceImpl rdfRest = new URIReferenceImpl(RDF.REST);
    URIReferenceImpl rdfFirst = new URIReferenceImpl(RDF.FIRST);
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(toElement(subject), predicate, headVar),
        new ConstraintDisjunction(
            new SingleTransitiveConstraint(new ConstraintImpl(headVar, rdfRest, nodeVar)),
            new ConstraintImpl(headVar, rdfRest, nodeVar)),
        new ConstraintDisjunction(
            new ConstraintImpl(headVar, rdfFirst, valueVar),
            new ConstraintImpl(nodeVar, rdfFirst, valueVar))
    );
    
    Query query = createQuery(where, valueVar);
    Answer answer = doQuery(query);
    Set<Node> values = new HashSet<Node>();
    try {
      answer.beforeFirst();
      while (answer.next()) values.add((Node)answer.getObject(valueVar.getName()));
    } finally {
      answer.close();
    }
    return values;
  }
  
  /**
   * Detect and add all trigger dependencies between rules to the given rule structure.
   * @param rules The rule structure for which to process triggers.
   */
  private void processTriggers(RuleStructure rules) {
    for (Iterator<Rule> it1 = rules.getRuleIterator(); it1.hasNext(); ) {
      Rule trigger = it1.next();
      for (Iterator<Rule> it2 = rules.getRuleIterator(); it2.hasNext(); ) {
        Rule potentialTarget = it2.next();
        if (triggersRule(trigger, potentialTarget)) trigger.addTriggerTarget(potentialTarget);
      }
    }
  }
  
  /**
   * Determine if a rule triggers a potential terget.
   * @param trigger The rule which may potentially trigger another rule.
   * @param potentialTarget The potential target of the rule.
   * @return <tt>true</tt> iff the given rule has a head atom which matches one of
   *         the body atoms of the potential target.
   */
  private boolean triggersRule(Rule trigger, Rule potentialTarget) {
    // Consistency checks never trigger other rules (they throw exceptions instead).
    if (trigger instanceof ConsistencyCheck) return false;
    
    List<SelectElement> products = trigger.getQuery().getVariableList();
    if (products.size() % 3 != 0) throw new IllegalStateException("Invalid number of select elements for rule query: " + trigger.getQuery());
    
    for (int index = 0; index < products.size(); index += 3) {
      List<SelectElement> triggerProduct = products.subList(index, index + 3);
      if (matchesExpression(triggerProduct, potentialTarget.getQuery().getConstraintExpression())) return true;
    }
    
    return false;
  }
  
  /**
   * Determine whether the head atom represented by the product list matches any part of
   * the given constraint expression.
   * @param product A triple pattern appearing as the product of a rule.
   * @param expr The body of a potential target rule.
   * @return <tt>true</tt> iff the triple pattern matches any part of the potential target.
   */
  private boolean matchesExpression(List<SelectElement> product, ConstraintExpression expr) {
    assert product.size() == 3;
    // Only triple patterns, conjunctions, and filters are created by the rule loader; ignore other
    // constraint types.
    if (expr instanceof ConstraintImpl) return matchesPattern(product, (ConstraintImpl)expr);
    else if (expr instanceof ConstraintOperation) {
      // An operation matches if any of the operands matches.
      boolean matches = false;
      for (ConstraintExpression operand : ((ConstraintOperation)expr).getElements()) {
        matches = matches || matchesExpression(product, operand);
      }
      return matches;
    } else if (expr instanceof ConstraintFilter) {
      // Only the unfiltered part of a constraint can trigger a match (filters restrict the results from the unfiltered part).
      return matchesExpression(product, ((ConstraintFilter)expr).getUnfilteredConstraint());
    }
    return false;
  }
  
  /**
   * Determine whether a triple product in the head of one rule matches the triple pattern
   * in the body of another rule.
   * @param product A triple product.
   * @param pattern A triple pattern.
   * @return <tt>true</tt> if the product and pattern match at every position.  A match is
   *         defined as both constants with the same value, or both variables (regardless of
   *         variable name).
   */
  private boolean matchesPattern(List<SelectElement> product, ConstraintImpl pattern) {
    assert product.size() == 3;
    for (int i = 0; i < product.size(); i++) {
      SelectElement productTerm = product.get(i);
      if ((productTerm instanceof ConstantValue) && !(((ConstantValue)productTerm).getValue().equals(pattern.getElement(i)))) return false;
    }
    return true;
  }
    
  /**
   * Utility method to create a query.
   * @param constraintExpression The constraint expression making up the WHERE clause of the query.
   * @param selection The variables to select in the query.
   * @return The new query.
   */
  @SuppressWarnings("unchecked")
  private Query createQuery(ConstraintExpression constraintExpression, Variable... selection) {
    List<Variable> selectList = Arrays.asList(selection);
    return new Query(
        selectList,                                 // SELECT
        ruleGraph,                                  // FROM
        constraintExpression,                       // WHERE
        null,                                       // HAVING
        (List<Order>)Collections.EMPTY_LIST,        // ORDER BY
        null,                                       // LIMIT
        0,                                          // OFFSET
        true,                                       // DISTINCT
        UNCONSTRAINED                               // GIVEN
    );
  }
  
  /**
   * Local wrapper for querying on an OperationContext. Since {@link OperationContext#doQuery(Query)}
   * throws an {@link Exception}, this is captured and wrapped in or cast to a {@link QueryException}.
   * @param q The query to execute.
   * @return The Answer to the query.
   * @throws QueryException If the query fails.
   */
  private Answer doQuery(Query q) throws QueryException {
    try {
      if (operationContext != null) return operationContext.doQuery(q);
      throw new IllegalStateException("No environment to query the database in");
    } catch (Exception e) {
      if (e instanceof QueryException) throw (QueryException)e;
      throw new QueryException("Unable to execute query", e);
    }
  }
  
  /**
   * Converts an RDF node into an RDF term used in a filter.  Variable mappings from the
   * rule graph are applied as part of this conversion.
   * @param node A node from an RDF graph.
   * @param varMap The variable mappings defined by the rule graph.
   * @return The corresponding RDF term.
   */
  private RDFTerm toRdfTerm(Node node, Map<URIReference,Variable> varMap) throws QueryException, SWRLStructureException {
    if (varMap.containsKey(node)) return new Var(varMap.get(node).getName());
    if (node instanceof URIReference) return new IRI(((URIReference)node).getURI());
    if (node instanceof Literal) {
      Literal l = (Literal)node;
      return TypedLiteral.newLiteral(l.getLexicalForm(), l.getDatatypeURI(), l.getLanguage());
    }
    throw new SWRLStructureException("RDF term in an atom must be a URI, literal, or variable reference");
  }
  
  /**
   * Constructs a query triple pattern from a subject, predicate, and object RDF node.
   * Variable mappings from the rule graph are applied as part of this operation.
   * @param s The subject URI reference.
   * @param p The predicate URI reference.
   * @param o The object node.
   * @param varMap The variable mappings defined by the rule graph.
   * @return The corresponding triple pattern.
   */
  private ConstraintImpl toConstraint(URIReference s, URIReference p, Node o, Map<URIReference,Variable> varMap) throws SWRLStructureException {
    ConstraintElement subject = varMap.containsKey(s) ? varMap.get(s): (s instanceof URIReferenceImpl ? (URIReferenceImpl)s : new URIReferenceImpl(s.getURI()));
    ConstraintElement predicate = varMap.containsKey(p) ? varMap.get(p): (p instanceof URIReferenceImpl ? (URIReferenceImpl)p : new URIReferenceImpl(p.getURI()));
    ConstraintElement object = null;
    if (o instanceof URIReference) {
      URIReference oUri = (URIReference)o;
      object = varMap.containsKey(oUri) ? varMap.get(oUri) : (oUri instanceof URIReferenceImpl ? (URIReferenceImpl)oUri : new URIReferenceImpl(oUri.getURI()));
    } else if (o instanceof Literal) {
      object = toLiteralElement((Literal)o);
    } else {
      throw new SWRLStructureException("Object of a triple pattern must be a URI, literal, or variable reference; found: " + o);
    }
    return new ConstraintImpl(subject, predicate, object);
  }
  
  /**
   * Converts a JRDF node to a query constraint element.
   * @param node The JRDF node.
   * @return The query constraint element.
   */
  private ConstraintElement toElement(Node node) {
    if (node instanceof ConstraintElement) return (ConstraintElement)node;
    if (node instanceof URIReference) {
      return new URIReferenceImpl(((URIReference)node).getURI());
    } else if (node instanceof Literal) {
      return toLiteralElement((Literal)node);
    } else if (node instanceof BlankNode) {
      return new VariableNodeImpl(((BlankNode)node).getID());
    }
    throw new IllegalArgumentException("Unable to convert to constraint element: " + node);
  }
  
  /**
   * Converts a JRDF literal node to a query constraint element.
   * @param lit The JRDF literal node.
   * @return The query constraint element.
   */
  private LiteralImpl toLiteralElement(Literal lit) {
    if (lit instanceof LiteralImpl) return (LiteralImpl)lit;
    else if (lit.getDatatypeURI() != null) return new LiteralImpl(lit.getLexicalForm(), lit.getDatatypeURI());
    else if (lit.getLanguage() != null && lit.getLanguage().length() > 0) return new LiteralImpl(lit.getLexicalForm(), lit.getLanguage());
    else return new LiteralImpl(lit.getLexicalForm());
  }
  
  /**
   * Utility method to do type checking on an object extracted from query results.
   * @param <T> The expected class.
   * @param obj The object that was extracted from a query answer.
   * @param expected The expected class of the object.
   * @param msg The message to use when throwing an exception if the class doesn't match.
   * @throws SWRLStructureException if the actual class of the object doesn't match the expected class.
   */
  private <T> void checkClass(Object obj, Class<T> expected, String msg) throws SWRLStructureException {
    if (!expected.isAssignableFrom(obj.getClass())) {
      throw new SWRLStructureException(msg);
    }
  }
}
