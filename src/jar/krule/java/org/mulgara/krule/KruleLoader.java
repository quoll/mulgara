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

package org.mulgara.krule;

// Java 2 standard packages
import java.net.*;
import java.util.*;
import java.rmi.RemoteException;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.*;
import org.jrdf.vocabulary.RDF;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.TripleImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.OperationContext;
import org.mulgara.rules.*;
import org.mulgara.util.functional.Pair;

import static org.mulgara.query.rdf.Mulgara.PREFIX_GRAPH;
import static org.mulgara.query.rdf.Krule.*;

/**
 * This object is used for parsing an RDF graph and building a rules structure
 * from it, according to the krule.owl ontology.
 *
 * @created 2005-5-17
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.3 $
 * @modified $Date: 2005/07/03 12:57:44 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class KruleLoader implements RuleLoader {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(KruleLoader.class.getName());

  /** The database session for querying. */
  private OperationContext operationContext;

  /** The rules. */
  private RuleStructure rules;

  /** The Graph resource represented by ruleGraphUri. */
  private final GraphResource ruleGraph;

  /** The graph expression containing the base data. */
  private GraphExpression baseGraphExpr;

  /** The URI of the graph to receive the entailed data. */
  private URI destGraphUri;

  /** Map of krule:URIReference nodes to their associated URIs. */
  private Map<URIReference,URIReference> uriReferences;

  /** Map of krule:Variable nodes to their associated nodes. */
  private Map<URIReference,Variable> varReferences;

  /** Map of krule:Literal nodes to their associated strings. */
  private Map<Node,Literal> literalReferences;

  /** Map of Constraint nodes to the associated constraint object. */
  private Map<Node,ConstraintExpression> constraintMap;

  /** RDF reference for rdf:type. */
  public static final URIReferenceImpl RDF_TYPE = new URIReferenceImpl(RDF.TYPE);

  /** RDF reference for rdf:value. */
  public static final URIReferenceImpl RDF_VALUE = new URIReferenceImpl(RDF.VALUE);

  /** A field used in queries to indicate no prior constraints on the answer. */
  private static final UnconstrainedAnswer UNCONSTRAINED = new UnconstrainedAnswer();

  /**
   * Principle constructor.
   *
   * @param ruleGraphUri The name of the graph with the rules to run.
   * @param baseGraphExpr The graph expression with the base data.
   * @param destGraphUri The name of the graph which will receive the entailed data.
   */
  KruleLoader(URI ruleGraphUri, GraphExpression baseGraphExpr, URI destGraphUri) {
    this.baseGraphExpr = baseGraphExpr;
    this.destGraphUri = destGraphUri;

    ruleGraph = new GraphResource(ruleGraphUri);

    // set the query objects to null
    operationContext = null;

    // initialize the constriant map
    constraintMap = new HashMap<Node,ConstraintExpression>();
  }


  /**
   * Factory method.
   *
   * @param ruleModel The name of the model with the rules to run.
   * @param baseGraph The graph expression with the base data.
   * @param destModel The name of the model which will receive the entailed data.
   * @return A new KruleLoader instance.
   */
  public static RuleLoader newInstance(URI ruleModel, GraphExpression baseGraph, URI destModel) {
    return new KruleLoader(ruleModel, baseGraph, destModel);
  }


  /**
   * Reads the ruleModel in the database and constructs the rules from it.
   *
   * @param opContextParam The operationContext for querying on.
   * @return A new rule structure, or <code>null</code> if the rules are not a Krule structure.
   * @throws InitializerException There was a problem reading and creating the rules.
   */
  public Rules readRules(Object opContextParam) throws InitializerException, RemoteException {
    this.operationContext = (OperationContext)opContextParam;

    rules = null;
    try {
      if (logger.isDebugEnabled()) logger.debug("Initializing for rule queries.");
      // load the objects
      loadRdfObjects();

      // if there is not Krule data, then return null to indicate this loader cannot read these rules
      if (uriReferences.isEmpty()) return null;

      if (logger.isDebugEnabled()) logger.debug("Querying for rules");
      rules = findRules();
      // set the target model
      rules.setTargetModel(destGraphUri);

      // find the triggers
      loadTriggers();

      // find the queries for each rule
      loadQueries();

      // load the axioms
      rules.setAxioms(findAxioms());

      if (rules.getRuleCount() == 0 && rules.getAxiomCount() == 0) {
        throw new InitializerException("No valid rules found");
      }

    } catch (TuplesException te) {
      logger.error("Exception while accessing rule data. " + te.getMessage());
      throw new InitializerException("Problem accessing rule data", te);
    } catch (QueryException qe) {
      logger.error("Exception while reading rules. " + qe.getMessage());
      throw new InitializerException("Problem reading rules", qe);
    } catch (KruleStructureException ke) {
      logger.error("Error in rule RDF data:" + ke.getMessage());
      throw new InitializerException("Problem in rules RDF", ke);
    } catch (Throwable t) {
      logger.error("Unexpected error during loading: " + t.getMessage(), t);
      throw new InitializerException("Unexpected error loading rules", t);
    }

    return rules;
  }


  /**
   * Loads all objects desribed in the RDF graph.
   */
  private void loadRdfObjects() throws QueryException, TuplesException, InitializerException, KruleStructureException {
    // get all the URIReferences
    findUriReferences();
    if (logger.isDebugEnabled()) logger.debug("Got URI References");
    if (uriReferences.isEmpty()) {
      if (logger.isDebugEnabled()) logger.debug("No Krule data");
      return;
    }
    findVarReferences();
    if (logger.isDebugEnabled()) logger.debug("Got Variable references");
    findLiteralReferences();
    if (logger.isDebugEnabled()) logger.debug("Got Literal references");

    // pre-load all constraints
    loadSimpleConstraints();
    if (logger.isDebugEnabled()) logger.debug("Got simple constraints");
    loadTransitiveConstraints();
    if (logger.isDebugEnabled()) logger.debug("Got transitive constraints");
    loadJoinConstraints();
    if (logger.isDebugEnabled()) logger.debug("Got join constraints");
    loadHavingConstraints();
    if (logger.isDebugEnabled()) logger.debug("Got having constraints");
  }


  /**
   * Finds all the rules, and creates empty Rule objects to represent each one.
   *
   * @return A Rules structure containing all found rules.
   * @throws TuplesException There was an error retrieving data from the model.
   * @throws QueryException When there is an exception finding the rules.
   */
  private RuleStructure findRules() throws QueryException, TuplesException {
    // select $rule from <ruleGraph> where $rule <rdf:type> <krule:Rule>
    Variable ruleV = new Variable("rule");
    Variable ruletypeV = new Variable("ruletype");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(ruleV, RDF_TYPE, ruletypeV),
        new ConstraintDisjunction(
            new ConstraintIs(ruletypeV, RULE),
            new ConstraintIs(ruletypeV, CHECK)
        )
    );
    Query query = createQuery(where, ruleV, ruletypeV);

    Answer ruleAnswer = query(query);
    if (logger.isDebugEnabled()) logger.debug("Got response for rule query");

    // create the rule structure for all the rules
    RuleStructure rules = new RuleStructure();

    try {
      // create all the rules
      ruleAnswer.beforeFirst();
      while (ruleAnswer.next()) {
        // create the rule and add it to the set
        URIReference type = (URIReference)ruleAnswer.getObject(1);
        String name = ruleAnswer.getObject(0).toString();
        if (type.equals(RULE)) rules.add(new Rule(name));
        else if (type.equals(CHECK)) rules.add(new ConsistencyCheck(name));
        else throw new QueryException("Unexpected type for rule: " + name + "(" + type + ")");
      }
    } finally {
      ruleAnswer.close();
    }
    if (logger.isDebugEnabled()) logger.debug("Created rules" + rules.toString());
    return rules;
  }


  /**
   * Finds all the rule triggers, and links the rule objects accordingly.
   *
   * @throws TuplesException When there is an exception accessing the data.
   * @throws QueryException When there is an exception finding the triggers.
   * @throws InitializerException Data structures did not meet preconditions.
   */
  private void loadTriggers() throws QueryException, TuplesException, InitializerException {
    // select $src $dest from <ruleGraph> where $src <krule:triggers> $dest
    Variable srcV = new Variable("src");
    Variable destV = new Variable("dest");
    Query query = createQuery(new ConstraintImpl(srcV, TRIGGERS, destV), srcV, destV);

    Answer answer = query(query);

    try {
      // link all the rules together
      answer.beforeFirst();
      while (answer.next()) {
        String src = answer.getObject(0).toString();
        String dest = answer.getObject(1).toString();
        if (logger.isDebugEnabled()) logger.debug("Linking <" + src + "> -> <" + dest + ">");
        rules.setTrigger(src, dest);
      }
    } finally {
      answer.close();
    }
  }


  /**
   * Finds all the rule queries, and attach them to the rules.
   * Does not yet consider a HAVING clause.
   *
   * @throws TuplesException When there is an exception accessing the data.
   * @throws QueryException When there is an exception finding the queries.
   * @throws KruleStructureException When there is an error in the RDF data structure.
   * @throws InitializerException When there is an intialization error.
   */
  private void loadQueries() throws TuplesException, QueryException, KruleStructureException, InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Loading Queries");

    // create some of the loop-invariant resources
    final URIReferenceImpl sysPrefix = new URIReferenceImpl(URI.create(PREFIX_GRAPH));
    final URIReferenceImpl mulgaraPrefix = new URIReferenceImpl(Mulgara.PREFIX_URI);
    final URIReferenceImpl seqPrefix = new URIReferenceImpl(URI.create(RDF.BASE_URI + "_"));

    // go through the rules to set their queries
    Iterator<Rule> ri = rules.getRuleIterator();
    while (ri.hasNext()) {
      Rule rule = ri.next();

      // create a resource for this rule
      URIReferenceImpl ruleRef = new URIReferenceImpl(URI.create(rule.getName()));
      if (logger.isDebugEnabled()) logger.debug("Reading query for rule: " + rule.getName());
      // select $pre $v $t from <ruleGraph>
      // where <#ruleRef> <krule:hasQuery> $q and $q <krule:selectionVariables> $vs
      // and $vs $pre $v and $pre <mulgara:prefix> <rdf:_> in <sys:prefix> and $v <rdf:type> $t
      Variable qV = new Variable("q");
      Variable vsV = new Variable("vs");
      Variable preV = new Variable("pre");
      Variable vV = new Variable("v");
      Variable tV = new Variable("t");
      ConstraintExpression where = new ConstraintConjunction(
          new ConstraintImpl(ruleRef, HAS_QUERY, qV),
          new ConstraintImpl(qV, SELECTION_VARS, vsV),
          new ConstraintImpl(vsV, preV, vV),
          new ConstraintImpl(preV, mulgaraPrefix, seqPrefix, sysPrefix),
          new ConstraintImpl(vV, RDF_TYPE, tV)
      );
      Query query = createQuery(where, preV, vV, tV);
      Answer answer = query(query);

      // get the length of the sequence prefix
      int prefixLength = RDF.BASE_URI.toString().length() + 1;
      // get the variables and values as elements with the appropriate type
      List<URIReference> elements = new ArrayList<URIReference>();
      List<URIReference> types = new ArrayList<URIReference>();
      try {
        answer.beforeFirst();
        while (answer.next()) {
          if (logger.isDebugEnabled()) logger.debug("Getting element from " + answer.getObject(0));
          // work out the position of the element.  Subject=0 Predicate=1 Object=2
          int seqNr = Integer.parseInt(answer.getObject(0).toString().substring(prefixLength)) - 1;
          if (logger.isDebugEnabled()) logger.debug("parsed: " + seqNr);
          // get the selection element and its type
          setList(elements, seqNr, (URIReference)answer.getObject(1));
          setList(types, seqNr, (URIReference)answer.getObject(2));
          if (logger.isDebugEnabled()) logger.debug("Nr: " + seqNr + ", v: " + elements.get(seqNr) + ", type: " + types.get(seqNr));
        }
      } finally {
        answer.close();
      }
      for (int select = 0; select < elements.size(); select++) {
        if (elements.get(select) == null || types.get(select) == null) {
          // one element was set. Get a descriptive error message
          StringBuffer errorMsg = new StringBuffer();
          for (int s = 0; s < elements.size(); s++) {
            if (elements.get(s) == null) errorMsg.append(" <null>");
            else errorMsg.append(" ").append(elements.get(s));
            if (types.get(s) == null) errorMsg.append("^^<null>");
            else errorMsg.append("^^<").append(types.get(s)).append(">");
          }
          throw new KruleStructureException("Rule " + rule.getName() + " does not have enough insertion elements. Got: " + errorMsg);
        }
      }
      // convert these elements into ConstraintElements for the query
      QueryStruct queryStruct = new QueryStruct(elements, types, uriReferences, varReferences, literalReferences);

      // read in the WHERE reference

      // select $w from <ruleGraph>
      // where <#rule.getName()> <krule:hasQuery> $q and $q <krule:hasWhereClause> $w
      Variable wV = new Variable("w");
      where = new ConstraintConjunction(
          new ConstraintImpl(ruleRef, HAS_QUERY, qV),
          new ConstraintImpl(qV, HAS_WHERE_CLAUSE, wV)
      );
      query = createQuery(where, wV);
      answer = query(query);

      try {
        // attach the correct constraint tree to the query structure
        answer.beforeFirst();
        if (answer.next()) {
          if (logger.isDebugEnabled()) logger.debug("Setting where clause for rule: " + rule.getName() + "");
          Node whereClauseNode = (Node)answer.getObject(0);
          if (logger.isDebugEnabled()) logger.debug("Where clause is: " + whereClauseNode);
          ConstraintExpression ce = (ConstraintExpression)constraintMap.get(whereClauseNode);
          if (logger.isDebugEnabled()) logger.debug("where clause expression: " + ce);
          if (ce == null) throw new KruleStructureException("Rule " + rule.getName() + " has no where clause");
          queryStruct.setWhereClause(ce);
        }

        if (answer.next()) throw new KruleStructureException("Rule " + rule.getName() + " has more than one query");
      } finally {
        answer.close();
      }

      if (logger.isDebugEnabled()) logger.debug("Setting models for the query");
      // set the models
      queryStruct.setGraphExpression(baseGraphExpr, destGraphUri);

      if (logger.isDebugEnabled()) logger.debug("Setting query structure for the rule");
      // create a new query and set it for the rule
      rule.setQueryStruct(queryStruct);
    }
  }


  /**
   * Finds all the axioms, and builds up the statements.
   *
   * @return A set of Triples which form the axiom statements for the rules.
   * @throws TuplesException When there is an exception accessing the data.
   * @throws QueryException When there is an exception finding the queries.
   * @throws KruleStructureException When there is an error in the RDF data structure.
   * @throws InitializerException When there is an intialization error.
   */
  private Set<org.jrdf.graph.Triple> findAxioms() throws TuplesException, QueryException, KruleStructureException, InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Loading Axioms");

    // select $s $p $o from <ruleGraph>
    // where $axiom <rdf:type> <krule:Axiom> and $axiom <krule:subject> $s
    // and $axiom <krule:predicate> $p and $axiom <krule:object> $o
    Variable sV = new Variable("s");
    Variable pV = new Variable("p");
    Variable oV = new Variable("o");
    Variable axiomV = new Variable("axiom");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(axiomV, RDF_TYPE, AXIOM),
        new ConstraintImpl(axiomV, AXIOM_SUBJECT, sV),
        new ConstraintImpl(axiomV, AXIOM_PREDICATE, pV),
        new ConstraintImpl(axiomV, AXIOM_OBJECT, oV)
    );
    Query query = createQuery(where, sV, pV, oV);
    Answer answer = query(query);

    // prepare the set of axioms
    Set<org.jrdf.graph.Triple> axioms = new HashSet<org.jrdf.graph.Triple>();

    try {
      Node sn = null;
      Node pn = null;
      Node on = null;
      try {
        answer.beforeFirst();
        while (answer.next()) {
          // use general nodes first to get the data from the answer
          sn = (Node)answer.getObject(0);
          pn = (Node)answer.getObject(1);
          on = (Node)answer.getObject(2);
          // convert to URIReference, catch any problems in this structure
          URIReference subjectRef = (URIReference)sn;
          URIReference predicateRef = (URIReference)pn;
          URIReference objectRef = (URIReference)on;
          // get the referred nodes
          ConstraintElement subject = convertToElement(subjectRef);
          ConstraintElement predicate = convertToElement(predicateRef);
          ConstraintElement object = convertToElement(objectRef);
          // convert these to a triple
          org.jrdf.graph.Triple jrdfTriple = new TripleImpl(
                        (SubjectNode) subject, (PredicateNode) predicate, (ObjectNode) object);
          // add to the set of axiom statements
          axioms.add(jrdfTriple);

        }
      } catch (ClassCastException cce) {
        throw new KruleStructureException("Axioms must be built using references to Nodes.  Faulty axiom: {" +
            sn + "," + pn + "," + on + "}");
      }
    } finally {
      // make sure the answer is cleanly closed
      answer.close();
    }
    return axioms;
  }


  /**
   * Queries for all URI references and loads their string representations.
   *
   * @throws TuplesException There was an error acessing the data.
   * @throws QueryException There was an error querying the model.
   * @throws InitializerException There was an error in the method preconditions.
   */
  private void findUriReferences() throws TuplesException, QueryException, InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Querying for URI reference objects.");

    // select $ref $uri from <ruleGraph>
    // where $ref <rdf:type> <krule:URIReference> and $ref <rdf:value> $uri
    Variable refV = new Variable("ref");
    Variable uriV = new Variable("uri");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(refV, RDF_TYPE, URI_REF),
        new ConstraintImpl(refV, RDF_VALUE, uriV)
    );
    Query query = createQuery(where, refV, uriV);
    Answer answer = query(query);
    if (logger.isDebugEnabled()) logger.debug("Found all URI references.");

    // create the mapping
    uriReferences = new HashMap<URIReference,URIReference>();
    // map each reference to the associated URI
    try {
      answer.beforeFirst();
      while (answer.next()) {
        URIReference ref = (URIReference)answer.getObject(0);
        URIReference uri = (URIReference)answer.getObject(1);
        if (logger.isDebugEnabled()) logger.debug("Mapping <" + ref + "> to <" + uri + ">");
        uriReferences.put(ref, uri);
      }
    } finally {
      answer.close();
    }
    if (logger.isDebugEnabled()) logger.debug("Mapped all URI references.");
  }


  /**
   * Queries for all variable references and loads their names.
   *
   * @throws TuplesException There was an error acessing the data.
   * @throws QueryException There was an error querying the model.
   * @throws InitializerException There was an error in the method preconditions.
   */
  private void findVarReferences() throws TuplesException, QueryException, InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Querying for variable reference objects.");

    // find the URI references and the referred URIs.

    // select $ref $name from <ruleGraph>
    // where $ref <rdf:type> <krule:Variable> and $ref <krule:name> $name
    Variable refV = new Variable("ref");
    Variable nameV = new Variable("name");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(refV, RDF_TYPE, VARIABLE),
        new ConstraintImpl(refV, NAME, nameV)
    );
    Query query = createQuery(where, refV, nameV);
    Answer answer = query(query);
    if (logger.isDebugEnabled()) logger.debug("Found all variable references.");

    // create the mapping
    varReferences = new HashMap<URIReference,Variable>();
    try {
      // map each reference to the associated variable
      answer.beforeFirst();
      while (answer.next()) {
        URIReference ref = (URIReference)answer.getObject(0);
        Literal name = (Literal)answer.getObject(1);
        if (logger.isDebugEnabled()) logger.debug("Mapping <" + ref + "> to <" + name + ">");
        varReferences.put(ref, new Variable(name.getLexicalForm()));
      }
    } finally {
      answer.close();
    }
    if (logger.isDebugEnabled()) logger.debug("Mapped all Variable references.");
  }


  /**
   * Queries for all Literal references and loads their string representations.
   *
   * @throws TuplesException There was an error acessing the data.
   * @throws QueryException There was an error querying the model.
   * @throws InitializerException There was an error in the method preconditions.
   */
  private void findLiteralReferences() throws TuplesException, QueryException, InitializerException {
    if (logger.isDebugEnabled()) logger.debug("Querying for Literal objects.");

    // select $lit $str from <ruleGraph>
    // where $lit <rdf:type> <krule:Literal> and $lit <rdf:value> $str
    Variable litV = new Variable("lit");
    Variable strV = new Variable("str");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(litV, RDF_TYPE, LITERAL),
        new ConstraintImpl(litV, RDF_VALUE, strV)
    );
    Query query = createQuery(where, litV, strV);
    Answer answer = query(query);
    if (logger.isDebugEnabled()) logger.debug("Found all Literals.");

    // create the mapping
    literalReferences = new HashMap<Node,Literal>();
    try {
      // map each reference to the associated String
      answer.beforeFirst();
      while (answer.next()) {
        Node litRef = (Node)answer.getObject(0);
        Literal lit = (Literal)answer.getObject(1);
        if (logger.isDebugEnabled()) logger.debug("Mapping <" + litRef + "> to <" + lit + ">");
        literalReferences.put(litRef, lit);
      }
    } finally {
      answer.close();
    }
    if (logger.isDebugEnabled()) logger.debug("Mapped all Literals.");
  }


  /**
   * Finds all Simple constraints, and stores them by node.
   *
   * @throws TuplesException There was an error retrieving data from the model.
   * @throws QueryException There was an error querying the model.
   * @throws KruleStructureException There was an error in the krule model.
   */
  private void loadSimpleConstraints() throws KruleStructureException, TuplesException, QueryException {
    if (logger.isDebugEnabled()) logger.debug("Querying for Simple constraints.");

    // select $c $p $o from <ruleGraph>
    // where $c <rdf:type> <krule:SimpleConstraint> and $c $p $o
    // and ($p <mulgara:is> <krule:hasSubject>
    //   or $p <mulgara:is> <krule:hasPredicate>
    //   or $p <mulgara:is> <krule:hasObject>
    //   or $p <mulgara:is> <krule:hasModel>)
    Variable cV = new Variable("c");
    Variable pV = new Variable("p");
    Variable oV = new Variable("o");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(cV, RDF_TYPE, SIMPLE_CONSTRAINT),
        new ConstraintImpl(cV, pV, oV),
        new ConstraintDisjunction(
            new ConstraintIs(pV, HAS_SUBJECT),
            new ConstraintIs(pV, HAS_PREDICATE),
            new ConstraintIs(pV, HAS_OBJECT),
            new ConstraintIs(pV, HAS_GRAPH)
        )
    );
    Query query = createQuery(where, cV, pV, oV);

    Answer answer = query(query);
    if (logger.isDebugEnabled()) logger.debug("Found all simple constraints.");

    // create a mapping of URIs to simple constraint structures
    Map<Node,Map<Node,Node>> simpleConstraints = new HashMap<Node,Map<Node,Node>>();
    try {
      // map each reference to the associated property/values
      answer.beforeFirst();
      while (answer.next()) {
        Node constraintNode = (Node)answer.getObject(0);
        URIReference predicate = (URIReference)answer.getObject(1);
        Node object = (Node)answer.getObject(2);
        if (logger.isDebugEnabled()) logger.debug("setting <" + constraintNode + ">.<" + predicate + "> = " + object);
        addProperty(simpleConstraints, constraintNode, predicate, object);
      }
    } finally {
      answer.close();
    }

    if (logger.isDebugEnabled()) logger.debug("Mapped all constraints to their property/values");

    // collect all property/values together into constraints
    for (Map.Entry<Node,Map<Node,Node>> entry: simpleConstraints.entrySet()) {
      // get the node in question
      Node constraintNode = entry.getKey();
      // get its properties
      Map<Node,Node> pv = entry.getValue();
      // get the individual properties
      ConstraintElement s = convertToElement(pv.get(HAS_SUBJECT));
      ConstraintElement p = convertToElement(pv.get(HAS_PREDICATE));
      ConstraintElement o = convertToElement(pv.get(HAS_OBJECT));
      // check if there is a "from" property
      Node from = pv.get(HAS_GRAPH);
      // build the appropriate constraint
      // add it to the map
      if (from == null) {
        if (logger.isDebugEnabled()) logger.debug("Creating <" + constraintNode + "> as (<" + s + "> <" + p + "> <" + o +">)");
        constraintMap.put(constraintNode, ConstraintFactory.newConstraint(s, p, o));
      } else {
        if (logger.isDebugEnabled()) logger.debug("Creating <" + constraintNode + "> as (<" + s + "> <" + p + "> <" + o +">) in <" + from + ">");
        constraintMap.put(constraintNode, ConstraintFactory.newConstraint(s, p, o, convertToElement(from)));
      }
    }
  }


  /**
   * Finds all join constraints which contain other constraints, and stores them by node.
   *
   * @throws TuplesException There was an error retrieving data from the model.
   * @throws QueryException There was an error querying the model.
   * @throws KruleStructureException There was an error querying the model.
   */
  private void loadJoinConstraints() throws KruleStructureException, TuplesException, QueryException {
    // build constraints in place, recursively constructing child constraints until all are found
    if (logger.isDebugEnabled()) logger.debug("Querying for Join constraints.");

    // don't look for the type of the child constraints. krule:argument has range of Constraint.

    // select $constraint $arg $constraint2 $type from <ruleGraph>
    // where $constraint $arg $constraint2
    //   and $constraint <rdf:type> $type
    //   and ($type <mulgara:is> <krule:ConstraintConjunction>
    //     or $type <mulgara:is> <krule:ConstraintDisjunction>)
    //   and ($arg <mulgara:is> <krule:argument>
    //     or $arg <mulgara:is> <krule:minuend>
    //     or $arg <mulgara:is> <krule:subtrahend>)
    Variable constraintV = new Variable("constraint");
    Variable argV = new Variable("arg");
    Variable constraint2V = new Variable("constraint2");
    Variable typeV = new Variable("type");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(constraintV, argV, constraint2V),
        new ConstraintImpl(constraintV, RDF_TYPE, typeV),
        new ConstraintDisjunction(
            new ConstraintIs(typeV, CONSTRAINT_CONJUNCTION),
            new ConstraintIs(typeV, CONSTRAINT_DISJUNCTION),
            new ConstraintIs(typeV, DIFFERENCE)
        ),
        new ConstraintDisjunction(
            new ConstraintIs(argV, ARGUMENT),
            new ConstraintIs(argV, MINUEND),
            new ConstraintIs(argV, SUBTRAHEND)
        )
    );
    Query query = createQuery(where, constraintV, argV, constraint2V, typeV);
    Answer answer = query(query);
    if (logger.isDebugEnabled()) logger.debug("Found all join constraints.");

    // accumulate all the constraint links and types

    // create a map of join constraints to the constraints that they join
    Map<Node,Set<Pair<Node,Node>>> constraintLinks = new HashMap<Node,Set<Pair<Node,Node>>>();

    // map the join constraints to the type of join
    Map<Node,URIReference> joinTypes = new HashMap<Node,URIReference>();

    try {
      // map each reference to the associated argument and type
      answer.beforeFirst();
      while (answer.next()) {
        Node constraintNode = (Node)answer.getObject(0);
        URIReference arg = (URIReference)answer.getObject(1);
        Node constraintNode2 = (Node)answer.getObject(2);
        URIReference type = (URIReference)answer.getObject(3);
        if (logger.isDebugEnabled()) logger.debug("constraint (" + type + ")<" + constraintNode + ">  <" + arg + "><" + constraintNode2 + ">");
        // map the constraint to its operand: constraintNode2
        addLink(constraintLinks, constraintNode, new Pair<Node,Node>(arg, constraintNode2));
        // map the type
        URIReference storedType = joinTypes.get(constraintNode);
        if (storedType == null) joinTypes.put(constraintNode, type);
        else if (!storedType.equals(type)) throw new KruleStructureException("Varying types in constraint operations in the rule structure");
      }
    } finally {
      answer.close();
    }

    if (logger.isDebugEnabled()) logger.debug("mapping join constraint RDF nodes to join constraint objects");
    // collect all arguments together into constraints and map the node to the constraint
    for (Map.Entry<Node,Set<Pair<Node,Node>>> entry: constraintLinks.entrySet()) {
      // get the constraint node in question
      Node constraintNode = entry.getKey();
      // see if it maps to a constraint
      if (constraintMap.get(constraintNode) == null) {
        // the constraint does not exist
        // get the argument nodes
        Set<Pair<Node,Node>> operands = entry.getValue();
        // get the constraint's type
        Node type = joinTypes.get(constraintNode);
        if (type == null) throw new KruleStructureException("No type (AND/OR/Minus) available on join constraint: " + constraintNode);
        // convert the RDF nodes to constraints
        List<ConstraintExpression> constraintArgs = getConstraints(operands, constraintLinks, joinTypes);
        ConstraintExpression joinConstraint = newJoinConstraint(type, constraintArgs);
        if (logger.isDebugEnabled()) logger.debug("mapped " + constraintNode + " -> " + joinConstraint);
        // build the join constraint, and map the node to it
        constraintMap.put(constraintNode, joinConstraint);
      } else {
        if (logger.isDebugEnabled()) logger.debug("constraint <" + constraintNode + "> already exists");
      }
    }
    // every key should now be mapped to a constraint object
    if (logger.isDebugEnabled()) logger.debug("mapped all constraint nodes to constraints");
  }


  /**
   * Finds all having constraints. This is included for completeness, but we don't do it yet.
   *
   * @throws TuplesException There was an error retrieving data from the model.
   * @throws QueryException There was an error querying the model.
   * @throws KruleStructureException There was an error querying the model.
   */
  private void loadHavingConstraints() throws KruleStructureException, TuplesException, QueryException {
    if (logger.isDebugEnabled()) logger.debug("Querying for Having constraints.");

    // select $constraint from <ruleGraph> where $rule <krule:hasHavingClause> $constraint
    Variable ruleV = new Variable("rule");
    Variable constraintV = new Variable("constraint");
    Query query = createQuery(new ConstraintImpl(ruleV, HAS_HAVING_CLAUSE, constraintV), constraintV);
    Answer answer = query(query);

    if (logger.isDebugEnabled()) logger.debug("Found all having constraints.");

    try {
      answer.beforeFirst();
      if (answer.next()) throw new KruleStructureException("Having structures not yet implemented");
    } finally {
      answer.close();
    }
  }


  /**
   * Finds all Transitive constraints, and stores them by node.
   *
   * @throws TuplesException There was an error retrieving data from the model.
   * @throws QueryException There was an error querying the model.
   * @throws KruleStructureException There was an error in the krule model.
   */
  private void loadTransitiveConstraints() throws KruleStructureException, TuplesException, QueryException {
    if (logger.isDebugEnabled()) logger.debug("Querying for Transitive constraints.");

    // select $c $p $arg from <ruleGraph>
    // where $c <rdf:type> <krule:TransitiveConstraint> and $c $p $arg
    // and ($p <mulgara:is> <krule:transitiveArgument>
    //   or $p <mulgara:is> <krule:anchorArgument>)
    Variable cV = new Variable("c");
    Variable pV = new Variable("p");
    Variable argV = new Variable("arg");
    ConstraintExpression where = new ConstraintConjunction(
        new ConstraintImpl(cV, RDF_TYPE, TRANSITIVE_CONSTRAINT),
        new ConstraintImpl(cV, pV, argV),
        new ConstraintDisjunction(
            new ConstraintIs(pV, TRANSITIVE_ARGUMENT),
            new ConstraintIs(pV, ANCHOR_ARGUMENT)
        )
    );
    Query query = createQuery(where, cV, pV, argV);
    Answer answer = query(query);

    if (logger.isDebugEnabled()) logger.debug("Retrieved all transitive constraints.");

    // set up a mapping of constraints to predicate/SimpleConstraint pairs
    Map<Node,Map<Node,Node>> transMap = new HashMap<Node,Map<Node,Node>>();

    try {
      // accumulate the transitive arguments
      answer.beforeFirst();
      while (answer.next()) {
        Node transConstraint = (Node)answer.getObject(0);
        URIReference predicate = (URIReference)answer.getObject(1);
        Node argument = (Node)answer.getObject(2);
        addProperty(transMap, transConstraint, predicate, argument);
        if (logger.isDebugEnabled()) logger.debug("mapping <" + transConstraint + "> to <" + predicate + ">.<" + argument +">");
      }
    } finally {
      answer.close();
    }
    if (logger.isDebugEnabled()) logger.debug("Mapped all transitive properties");

    // build a new transconstraint for each transitive constraint node
    for (Map.Entry<Node,Map<Node,Node>> tEntry: transMap.entrySet()) {
      Node constraintNode = tEntry.getKey();
      Map<Node,Node> arguments = tEntry.getValue();
      Constraint constraint;
      // build the constraint based on the arguments
      if (arguments.size() == 1) {
        Node sc = arguments.get(TRANSITIVE_ARGUMENT);
        if (sc == null) throw new KruleStructureException("Transitive argument not correct in: " + constraintNode + " " + arguments);
        if (logger.isDebugEnabled()) logger.debug("Mapping transitive constraint <" + constraintNode +"> to <" + sc +">");
        // get the simple constraint and build the transitive constraint around it
        constraint = new SingleTransitiveConstraint((Constraint)constraintMap.get(sc));
      } else if (arguments.size() == 2) {
        Node sc = arguments.get(TRANSITIVE_ARGUMENT);
        Node anchor = arguments.get(ANCHOR_ARGUMENT);
        if (sc == null || anchor == null) {
          throw new KruleStructureException("Transitive arguments not correct for: " + constraintNode + " " + arguments);
        }
        if (logger.isDebugEnabled()) logger.debug("Mapping transitive constraint <" + constraintNode +"> to <" + sc +">,<" + anchor + ">");
        // get the simple constraint and build the transitive constraint around it
        constraint = new TransitiveConstraint((Constraint)constraintMap.get(anchor), (Constraint)constraintMap.get(sc));
      } else {
        throw new KruleStructureException("Expected 1 or 2 arguments for Transitive constraint (" + constraintNode + "), got: " + arguments.size());
      }
      // map the transitive constraint node to the transitive constraint
      constraintMap.put(constraintNode, constraint);
    }
    if (logger.isDebugEnabled()) logger.debug("Mapped all transitive constraints");
  }


  /**
   * Converts a set of constraint Nodes from an RDF graph into a List of Constraint objects.
   * If the object for a Node already exists, then this is returned, otherwise create a new
   * Constraint object.  All simple constraints should exist, only leaving join constraints
   * to be created.  The constraintLinks and typeMap arguments are for constructing new
   * constraint objects.
   *
   * @param constraints The set of constraint nodes and their usage to get the constraints for.
   *             Whenever possible, the constraints come from constraintMap.
   * @param constraintLinks Linkage of join constraints to their arguments (and argument usage).  Used to create a new constraint.
   * @param typeMap Maps constraint nodes to their type.  Used to create a new constraint.
   * @throws KruleStructureException There was an error in the RDF data structure.
   */
  private List<ConstraintExpression> getConstraints(Set<Pair<Node,Node>> constraints, Map<Node,Set<Pair<Node,Node>>> constraintLinks, Map<Node,URIReference> typeMap) throws KruleStructureException {
    if (logger.isDebugEnabled()) logger.debug("converting nodes to constraint list: " + constraints);

    // build the return list
    List<ConstraintExpression> cList = new ArrayList<ConstraintExpression>();
    // check argument validity
    if (constraints == null) {
      logger.warn("Empty constraint found in data. Ignored.");
      return cList;
    }
    // go through the arguments
    for (Pair<Node,Node> constraintUsage: constraints) {
      Node usage = constraintUsage.first();  // one of: argument/minuend/subtrahend
      Node cNode = constraintUsage.second();
      if (logger.isDebugEnabled()) logger.debug("converting: " + cNode);
      // get the constraint expression object
      ConstraintExpression constraintExpr = (ConstraintExpression)constraintMap.get(cNode);
      if (constraintExpr == null) {
        if (logger.isDebugEnabled()) logger.debug(cNode.toString() + " not yet mapped to constraint");
        // constraint expression object does not yet exist, get its arguments
        Set<Pair<Node,Node>> constraintArgNodes = constraintLinks.get(cNode);
        // build the constraint expression - get the arguments as a list of constraints
        List<ConstraintExpression> constraintArgs = getConstraints(constraintArgNodes, constraintLinks, typeMap);
        constraintExpr = newJoinConstraint((Node)typeMap.get(cNode), constraintArgs);
      }
      // add the constraint argument to the list
      if (constraintExpr != null) {
        if (usage.equals(MINUEND)) setList(cList, 0, constraintExpr);
        else if (usage.equals(SUBTRAHEND)) setList(cList, 1, constraintExpr);
        else if (usage.equals(ARGUMENT)) cList.add(constraintExpr);
        else throw new KruleStructureException("Unknown argument type for " + cNode + ": " + usage);
      } else logger.warn("Missing constraint expression. Ignoring.");
    }
    return cList;
  }


  /**
   * Create a new join constraint.
   *
   * @param type The URI for the type to create. <code>null</code> is a handled error.
   * @param args The list of arguments for the constraint.
   * @return a new join constraint of the correct type, or <code>null</code> if the type is null.
   */
  private ConstraintExpression newJoinConstraint(Node type, List<ConstraintExpression> args) throws KruleStructureException {
    logger.debug("Building join constraint of type <" + type + ">: " + args);
    // confirm arguments
    if (type == null) return null;

    if (type.equals(CONSTRAINT_CONJUNCTION)) {
      return new ConstraintConjunction(args);
    } else if (type.equals(CONSTRAINT_DISJUNCTION)) {
      return new ConstraintDisjunction(args);
    } else if (type.equals(DIFFERENCE)) {
      if (args.size() != 2) throw new KruleStructureException("Difference constraints require 2 arguments: args=" + args);
      return new ConstraintDifference(args.get(0), args.get(1));
    }
    throw new KruleStructureException("Unknown join constraint type (not AND/OR/Minus): " + type);
  }


  /**
   * Converts an RDF Node to a constraint element.
   *
   * @param node The node to convert.
   * @throws KruleStructureException If node cannot be converted.
   */
  private ConstraintElement convertToElement(Node node) throws KruleStructureException {
    if (logger.isDebugEnabled()) logger.debug("converting " + node + " to ConstraintElement");
    // check that this is a named node
    if (node instanceof URIReference) {
      // get the referred node
      URIReferenceImpl ref = (URIReferenceImpl)uriReferences.get(node);
      if (ref != null) return ref;
      // not referred, so look in the variables
      Variable var = varReferences.get(node);
      if (var != null) return var;
      throw new KruleStructureException("Unrecognized URI (" + node + ") in constraint. Was not declared to reference a URI nor a variable.");
    } else {
      // This could be an anonymous Literal
      LiteralImpl lit = (LiteralImpl)literalReferences.get(node);
      if (lit != null) return lit;
      throw new KruleStructureException("Unrecognized literal (" + lit + ") in constraint. Was not declared to reference a literal.");
    }
  }


  /**
   * Sets a property for a node, creating the entry if it does not exist yet.
   *
   * @param map The mapping of nodes to property/values.
   * @param node The node to set the property for.
   * @param predicate The property to set.
   * @param object The value to set the property to.
   */
  private static void addProperty(Map<Node,Map<Node,Node>> map, Node node, URIReference predicate, Node object) {
    // get the current set of properties
    Map<Node,Node> pv = map.get(node);
    // check that the map exists
    if (pv == null) {
      // no, so create
      pv = new HashMap<Node,Node>();
      pv.put(predicate, object);
      // add to the map
      map.put(node, pv);
    } else {
      // update the map to hold the new value
      pv.put(predicate, object);
    }
  }


  /**
   * Maps a node to data about a nodee, creating the entry if it does not exist yet.
   *
   * @param map The mapping of nodes to tuples.
   * @param node1 The node to map.
   * @param nodeData The node data to map it to.
   */
  private static void addLink(Map<Node,Set<Pair<Node,Node>>> map, Node node1, Pair<Node,Node> nodeData) {
    // get the current set of properties
    Set<Pair<Node,Node>> links = map.get(node1);
    // check that the set exists
    if (links == null) {
      // no, so create
      links = new HashSet<Pair<Node,Node>>();
      links.add(nodeData);
      // add to the map
      map.put(node1, links);
    } else {
      // update the map to hold the new value
      links.add(nodeData);
    }
  }


  /**
   * Sets an element in a list, expanding the list if necessary.
   * @param list The list to update.
   * @param offset The offset to write to in the list.
   * @param value The value to write to the list.
   */
  private static <T> void setList(List<T> list, int offset, T value) {
    while (offset >= list.size()) list.add(null);
    list.set(offset, value);
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
  private Answer query(Query q) throws QueryException {
    try {
      if (operationContext != null) return operationContext.doQuery(q);
      throw new IllegalStateException("No environment to query the database in");
    } catch (Exception e) {
      if (e instanceof QueryException) throw (QueryException)e;
      throw new QueryException("Unable to execute query", e);
    }
  }
}
