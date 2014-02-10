/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.content.rlog;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mulgara.krule.rlog.parser.NSUtils;
import org.mulgara.query.TuplesException;
import org.mulgara.query.rdf.Krule;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;
import org.mulgara.util.functional.Pair;

import org.apache.log4j.Logger;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.jrdf.vocabulary.RDF;

/**
 * This class constructs an RLog structure out of a set of Statements.
 *
 * @created Mar 18, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RlogStructure {

  /** Logger.  */
  private static final Logger logger = Logger.getLogger(RlogStructure.class.getName());

  /** The rdf:type URI. */
  private static final URIReference TYPE = new URIReferenceImpl(RDF.TYPE);

  /** The rdf:value URI. */
  private static final URIReference RDF_VALUE = new URIReferenceImpl(RDF.VALUE);

  /** The rdf:Seq URI. */
  private static final URIReference RDF_SEQ = new URIReferenceImpl(RDF.SEQ);

  /** The domain for system graphs. */
  private static final String SYS = "sys";

  /** The session to use for localizing and globalizing. */
  ResolverSession session;

  /** The graph to store the statements in. */
  Map<Node,Map<URIReference,Set<Node>>> graph = new HashMap<Node,Map<URIReference,Set<Node>>>();

  /** Stores all the nodes of a particular type in a single set, one for each type. */
  Map<Node,Set<Node>> nodesByType = new HashMap<Node,Set<Node>>();

  /** Maps nodes to their type */
  Map<Node,Node> typeByNode = new HashMap<Node,Node>();

  /** Maps variables to their names */
  Map<Node,String> nameByVar = new HashMap<Node,String>();

  /** Maps namesto their variables */
  Map<String,Node> varByName = new HashMap<String,Node>();

  /** Maps all the in-use namespaces to their shorthand prefixes. */
  Map<String,String> namespaces;

  /** Default map of namespaces to their shorthand prefixes. */
  Map<String,String> defaultNamespaces;

  /** A generator for new namespace names. */
  NamespaceGenerator namespaceGen = new NamespaceGenerator();

  /** The current constraint context when writing a rule. */
  Node currentRootConstraint = null;

  /** A list of the rdf:_x IDs used for sequences. */
  List<URIReference> listIds = new ArrayList<URIReference>();

  /** The local node for the rdf:type URI. */
  final long rdfType;


  /**
   * Constructs the structure, along with the session used to help build it.
   * @param session The mechanism for convergins local nodes to global, and back.
   * @throws LocalizeException If a global node could not be localized to a long.
   */
  public RlogStructure(ResolverSession session) throws LocalizeException {
    this.session = session;
    rdfType = session.localize(TYPE);
    initNamespaces();
  }


  /**
   * Initialized the namespaces with the registered domains.
   */
  private void initNamespaces() {
    Map<String,String> ns = new HashMap<String,String>();
    for (Map.Entry<String,String> e: NSUtils.getRegisteredDomains()) {
      ns.put(e.getValue(), e.getKey());
    }
    defaultNamespaces = Collections.unmodifiableMap(ns);
    namespaces = new HashMap<String,String>();
    // add in sys: so it does not get mapped
    namespaces.put(SYS + ":", SYS);
  }


  /**
   * Build a structure in memory to represent the Krule data structure.
   * @param statements The statements to load.
   * @throws TuplesException If there was a problem accessing the statements.
   * @throws GlobalizeException If there was a problem converting the statements to references.
   */
  public void load(Statements statements) throws TuplesException, GlobalizeException {
    statements.beforeFirst();
    while (statements.next()) {
      long subject = statements.getSubject();
      long predicate = statements.getPredicate();
      long obj = statements.getObject();

      if (predicate == rdfType) {
        addType(subject, obj);
      } else {
        addToGraph(subject, predicate, obj);
      }
    }
    nameVariables();
  }


  /**
   * For every variable that was found, map it to its name.
   */
  void nameVariables() {
    Set<Node> vars = nodesByType.get(Krule.VARIABLE);
    if (vars == null) return;
    for (Node v: vars) {
      Node name = getPropertyValue(v, Krule.NAME);
      if (!(name instanceof Literal)) throw new IllegalArgumentException("Bad Krule structure. Variable name is not a string.");
      String sName = getValidVariableName((Literal)name);
      nameByVar.put(v, sName);
      varByName.put(sName, v);
    }
  }


  /**
   * Write a set of statements representing RLog to a given writer.
   * @param writer The output to send the RLog to.
   * @throws IOException If the data could not be converted or written out.
   */
  public void write(Writer writer) throws IOException {
    if (writer == null) throw new IllegalArgumentException("Writer cannot be null.");

    StringWriter out = new StringWriter();
    writeAxioms(out);
    writeChecks(out);
    writeRules(out);
    out.close();

    writePrefixes(writer);
    writer.append(out.getBuffer());
  }


  /**
   * Sets the type for an object.
   * @param s The subject to be typed.
   * @param t The type of the subject.
   * @throws GlobalizeException If the subject or type could not be globalized.
   */
  void addType(long s, long t) throws GlobalizeException {
    Node subj = session.globalize(s);
    Node type = session.globalize(t);
    // map the type to the node
    addValue(nodesByType, type, subj);
    // map the node to its type
    typeByNode.put(subj, type);
  }


  /**
   * Add a triple to the graph.
   * @param s The subject of the triple.
   * @param p The predicate of the triple.
   * @param o The object of the triple.
   * @throws GlobalizeException If one of the elements of the triple could not be
   *         converted to a global node.
   */
  void addToGraph(long s, long p, long o) throws GlobalizeException {
    Node subj = session.globalize(s);
    URIReference pred = (URIReference)session.globalize(p);
    Node obj = session.globalize(o);
    addPropertyValue(graph, subj, pred, obj);
  }


  /**
   * Writes all the axioms to the output as horn clauses.
   * @param out The writer to send the RLog to.
   * @throws IOException If there was an error writing to the output.
   */
  void writePrefixes(Writer out) throws IOException {
    for (Map.Entry<String,String> ns: namespaces.entrySet()) {
      String k = ns.getKey();
      String v = ns.getValue() + ":";
      if (v.equals(k)) continue;
      out.append("@prefix ");
      out.append(v);
      out.append(" <");
      out.append(k);
      out.append("> .\n");
    }
    out.append("\n");
  }


  /**
   * Writes all the axioms to the output as horn clauses.
   * @param out The writer to send the RLog to.
   * @throws IOException If there was an error writing to the output.
   */
  void writeAxioms(Writer out) throws IOException {
    Set<Node> axioms = nodesByType.get(Krule.AXIOM);
    if (axioms == null) return;
    for (Node axiom: axioms) {
      Map<URIReference,Set<Node>> properties = graph.get(axiom);
      Node s = getSingle(properties, Krule.AXIOM_SUBJECT);
      Node p = getSingle(properties, Krule.AXIOM_PREDICATE);
      Node o = getSingle(properties, Krule.AXIOM_OBJECT);
      out.append(toPredicate(s, p, o));
      out.append(".\n");
    }
    out.append("\n");
  }


  /**
   * Writes all the consistency checks to the output as horn clauses.
   * @param out The writer to send the RLog to.
   * @throws IOException If there was an error writing to the output.
   */
  void writeChecks(Writer out) throws IOException {
    Set<Node> checks = nodesByType.get(Krule.CHECK);
    if (checks == null) return;
    for (Node check: checks) {
      Map<URIReference,Set<Node>> properties = graph.get(check);
      Node q = getSingle(properties, Krule.HAS_QUERY);
      Node qType = typeByNode.get(q);
      if (!qType.equals(Krule.QUERY)) {
        throw new IllegalArgumentException("Bad Krule structure. Consistency check has a query that has a non-query type: " + qType);
      }
      out.append(":- ");
      writeBody(out, q);
      out.append(".\n");
    }
    out.append("\n");
  }


  /**
   * Writes all the rules to the output as horn clauses.
   * @param out The writer to send the RLog to.
   * @throws IOException If there was an error writing to the output.
   */
  void writeRules(Writer out) throws IOException {
    Set<Node> rules = nodesByType.get(Krule.RULE);
    if (rules == null) return;
    for (Node rule: rules) {
      Node q = getPropertyValue(rule, Krule.HAS_QUERY);
      Node qType = typeByNode.get(q);
      if (!qType.equals(Krule.QUERY)) {
        throw new IllegalArgumentException("Bad Krule structure. Consistency check has a query that has a non-query type: " + qType);
      }
      writeHead(out, q);
      out.append(" :- ");
      writeBody(out, q);
      out.append(".\n");
    }
    out.append("\n");
  }


  /**
   * Writes out the head of a query associated with a rule.
   * @param out The writer output.
   * @param query The node representing the query.
   */
  void writeHead(Writer out, Node query) throws IOException {
    Node seq = getPropertyValue(query, Krule.SELECTION_VARS);
    Node sType = typeByNode.get(seq);
    if (!sType.equals(RDF_SEQ)) {
      throw new IllegalArgumentException("Bad Krule structure. Query selection sequence is not a sequence type: " + sType);
    }

    // loop over multiple triples
    int seqNr = 1;
    Node s;
    while (null != (s = getPropertyValue(seq, getListId(seqNr++)))) {
      Node p = getPropertyValue(seq, getListId(seqNr++));
      Node o = getPropertyValue(seq, getListId(seqNr++));
      if (p == null || o == null) throw new IllegalArgumentException("Bad Krule structure. Query selection sequence is not set of triples. " + seqNr + " elements");
      // separate triples with commas
      if (seqNr > 4) out.append(", ");
      out.append(toPredicate(s, p, o));
    }
  }


  /**
   * Writes out the body (WHERE clause) of a query associated with a rule.
   * @param out The writer output.
   * @param query The node representing the query.
   */
  void writeBody(Writer out, Node query) throws IOException {
    Node constraint = getPropertyValue(query, Krule.HAS_WHERE_CLAUSE);
    // remember the current context in case it is needed
    currentRootConstraint = constraint;
    writeConstraint(out, constraint, false);
  }


  /**
   * Writes a general constraint type that may be inverted. This method is recursive.
   * @param out The writer output.
   * @param constraint The constraint to be written.
   *        Accepts conjunctions, differences, transitive and simple constraints.
   * @param inv Indicates that the constraint is inverted.
   * @throws IOException Due to a write error on the writer.
   */
  void writeConstraint(Writer out, Node constraint, boolean inv) throws IOException {
    Node cType = typeByNode.get(constraint);
    if (cType.equals(Krule.CONSTRAINT_CONJUNCTION)) {
      writeConstraintConjunction(out, constraint, inv);
    } else if (cType.equals(Krule.SIMPLE_CONSTRAINT)) {
      writeConstraintSimple(out, constraint, inv);
    } else if (cType.equals(Krule.DIFFERENCE)) {
      writeConstraintDifference(out, constraint, inv);
    } else if (cType.equals(Krule.TRANSITIVE_CONSTRAINT)) {
      writeConstraintTransitive(out, constraint, inv);
    } else {
      throw new IllegalArgumentException("Bad Krule structure. Unsupported Constraint type: " + cType);
    }
  }


  /**
   * Writes the simple constraint type. This is just a triple.
   * @param out The writer output.
   * @param constraint The constraint to be written.
   * @param inv Indicates that the constraint is inverted.
   * @throws IOException Due to a write error on the writer.
   */
  void writeConstraintSimple(Writer out, Node constraint, boolean inv) throws IOException {
    Node s = getPropertyValue(constraint, Krule.HAS_SUBJECT);
    Node p = getPropertyValue(constraint, Krule.HAS_PREDICATE);
    Node o = getPropertyValue(constraint, Krule.HAS_OBJECT);
    // throw away the graph, as this is autodetected from the predicate in RLog parsing
    if (s == null || p == null || o == null) {
      throw new IllegalArgumentException("Bad Krule structure. Incomplete constraint.");
    }
    if (inv) out.append("~");
    out.append(toPredicate(s, p, o));
  }


  /**
   * Writes a constraint conjunction.
   * @param out The writer output.
   * @param constraint The constraint to be written.
   * @param inv Indicates that the constraint is inverted.
   * @throws IOException Due to a write error on the writer.
   */
  void writeConstraintConjunction(Writer out, Node constraint, boolean inv) throws IOException {
    Map<URIReference,Set<Node>> constraintProps = graph.get(constraint);
    if (constraintProps == null) throw new IllegalArgumentException("Bad Krule structure. Missing arguments for a conjunction.");
    Set<Node> args = constraintProps.get(Krule.ARGUMENT);
    // go through all the constraints, separating them with commas
    boolean first = true;
    for (Node c: args) {
      if (first) first = false;
      else out.append(", ");
      writeConstraint(out, c, inv);
    }
  }


  /**
   * Writes a difference constraint.
   * @param out The writer output.
   * @param constraint The constraint to be written.
   * @param inv Indicates that the constraint is inverted.
   * @throws IOException Due to a write error on the writer.
   */
  void writeConstraintDifference(Writer out, Node constraint, boolean inv) throws IOException {
    Node minuend = getPropertyValue(constraint, Krule.MINUEND);
    Node subtrahend = getPropertyValue(constraint, Krule.SUBTRAHEND);
    if (minuend == null) throw new IllegalArgumentException("Bad Krule structure. Missing minuend on a Difference.");
    if (subtrahend == null) throw new IllegalArgumentException("Bad Krule structure. Missing subtrahend on a Difference.");
    writeConstraint(out, minuend, inv);
    out.append(", ");
    writeConstraint(out, subtrahend, !inv);
  }


  /**
   * Writes a transitive constraint.
   * @param out The writer output.
   * @param constraint The constraint to be written.
   * @param inv Indicates that the constraint is inverted.
   * @throws IOException Due to a write error on the writer.
   */
  void writeConstraintTransitive(Writer out, Node constraint, boolean inv) throws IOException {
    Node arg = getPropertyValue(constraint, Krule.TRANSITIVE_ARGUMENT);
    if (arg == null || !arg.equals(Krule.SIMPLE_CONSTRAINT)) {
      throw new IllegalArgumentException("Bad Krule structure. Transitive constraints must operate on simple arguments.");
    }
    Node s = getPropertyValue(arg, Krule.HAS_SUBJECT);
    Node p = getPropertyValue(arg, Krule.HAS_PREDICATE);
    Node o = getPropertyValue(arg, Krule.HAS_OBJECT);
    // throw away the graph, as this is autodetected from the predicate in RLog parsing
    if (s == null || p == null || o == null) {
      throw new IllegalArgumentException("Bad Krule structure. Incomplete constraint in transitive constraint.");
    }

    Node newVar = getUnusedVar();
    out.append(toPredicate(s, p, newVar));
    out.append(", ");
    out.append(toPredicate(newVar, p, o));
  }


  /**
   * Finds a variable that is not in use in the current constraint.
   * @return A variable this is not being used in the current rule.
   */
  Node getUnusedVar() {
    for (char v = 'A'; v <= 'Z'; v++) {
      Node var = varByName.get(Character.toString(v));
      if (var != null) return var;
    }
    // every variable is in use somewhere. Have to search the current rule.
    Set<Node> currentVariables = getVariables(currentRootConstraint);
    for (Node var: nameByVar.keySet()) {
      if (!currentVariables.contains(var)) return var;
    }
    throw new IllegalStateException("Rule is too complex. It contains too many variables.");
  }


  /**
   * Accumulates all of the variables under a given node.
   * @param constraint The constraint node to find variables under.
   * @return A Set of nodes which represent variables.
   */
  Set<Node> getVariables(Node constraint) {
    Node type = typeByNode.get(constraint);
    if (type.equals(Krule.SIMPLE_CONSTRAINT)) {
      return getVariablesSimple(constraint);
    } else if (type.equals(Krule.CONSTRAINT_CONJUNCTION)) {
      return getVariablesConjunction(constraint);
    } else if (type.equals(Krule.DIFFERENCE)) {
      return getVariablesDifference(constraint);
    } else if (type.equals(Krule.TRANSITIVE_CONSTRAINT)) {
      return getVariablesTransitive(constraint);
    } else {
      throw new IllegalArgumentException("Bad Krule structure. Unsupported Constraint type: " + type);
    }
  }


  /**
   * Accumulates all of the variables in a simple constraint.
   * @param constraint The constraint node to find variables under.
   * @return A Set of nodes which represent variables.
   */
  Set<Node> getVariablesSimple(Node constraint) {
    Node s = getPropertyValue(constraint, Krule.HAS_SUBJECT);
    Node p = getPropertyValue(constraint, Krule.HAS_PREDICATE);
    Node o = getPropertyValue(constraint, Krule.HAS_OBJECT);
    if (s == null || p == null || o == null) {
      throw new IllegalArgumentException("Bad Krule structure. Incomplete constraint.");
    }
    Set<Node> vars = new HashSet<Node>();
    if (nameByVar.containsKey(s)) vars.add(s);
    if (nameByVar.containsKey(p)) vars.add(p);
    if (nameByVar.containsKey(o)) vars.add(o);
    return vars;
  }


  /**
   * Accumulates all of the variables in a conjunction.
   * @param constraint The constraint node to find variables under.
   * @return A Set of nodes which represent variables.
   */
  Set<Node> getVariablesConjunction(Node constraint) {
    // Recursively get the variables from the arguments
    Map<URIReference,Set<Node>> constraintProps = graph.get(constraint);
    if (constraintProps == null) throw new IllegalArgumentException("Bad Krule structure. Missing arguments for a conjunction.");
    Set<Node> args = constraintProps.get(Krule.ARGUMENT);
    // accumulate the variables into the first result. This is not functional, but more efficient.
    Set<Node> vars = null;
    for (Node c: args) {
      Set<Node> tmp = getVariables(c);
      if (vars == null) vars = tmp;
      else vars.addAll(tmp);
    }
    return vars;
  }


  /**
   * Accumulates all of the variables in a difference.
   * @param constraint The constraint node to find variables under.
   * @return A Set of nodes which represent variables.
   */
  Set<Node> getVariablesDifference(Node constraint) {
    // Recursively get the variables from the arguments
    Node minuend = getPropertyValue(constraint, Krule.MINUEND);
    Node subtrahend = getPropertyValue(constraint, Krule.SUBTRAHEND);
    if (minuend == null) throw new IllegalArgumentException("Bad Krule structure. Missing minuend on a Difference.");
    if (subtrahend == null) throw new IllegalArgumentException("Bad Krule structure. Missing subtrahend on a Difference.");
    Set<Node> vars = getVariables(minuend);
    vars.addAll(getVariables(subtrahend));  // not quite functional, but more efficient
    return vars;
  }


  /**
   * Accumulates all of the variables in a transitive constraint.
   * @param constraint The constraint node to find variables under.
   * @return A Set of nodes which represent variables.
   */
  Set<Node> getVariablesTransitive(Node constraint) {
    // Recursively get the variables from the arguments
    Node arg = getPropertyValue(constraint, Krule.TRANSITIVE_ARGUMENT);
    if (arg == null || !arg.equals(Krule.SIMPLE_CONSTRAINT)) {
      throw new IllegalArgumentException("Bad Krule structure. Transitive constraints must operate on simple arguments.");
    }
    return getVariables(arg);
  }


  /**
   * Converts a triple to a string containing a DL predicate.
   * @param s The subject of the triple.
   * @param p The predicate of the triple.
   * @param o The object of the triple.
   * @return A string with the DL version of the predicate.
   */
  String toPredicate(Node s, Node p, Node o) {
    if (isType(p)) return toTypePredicate(s, o);
    return toBinaryPredicate(s, p, o);
  }


  /**
   * Convert a subject and type into a Type predicate.
   * @param s The subject to be typed.
   * @param type The type of the subject.
   * @return A string with the subject and type.
   */
  String toTypePredicate(Node s, Node type) {
    StringBuilder sb = new StringBuilder();
    sb.append(toString(type));
    sb.append("(");
    sb.append(toString(s));
    sb.append(")");
    return sb.toString();
  }


  /**
   * Convert a triple into a binary predicate.
   * @param s The subject of the triple.
   * @param p The predicate of the triple.
   * @param o The object of the triple.
   * @return A string with the DL version of the binary predicate.
   */
  String toBinaryPredicate(Node s, Node p, Node o) {
    StringBuilder sb = new StringBuilder();
    sb.append(toString(p));
    sb.append("(");
    sb.append(toString(s));
    sb.append(",");
    sb.append(toString(o));
    sb.append(")");
    return sb.toString();
  }


  /**
   * Gets the string representation of an object referenced by URIReference.
   * @param n The node to get the value of.
   * @return A string representation of the object.
   */
  String toString(Node n) {
    Node refType = typeByNode.get(n);  // Variable, URIReference or Literal
    if (refType == null) throw new IllegalArgumentException("Bad Krule structure. No type for node: " + n);
    if (!(refType instanceof URIReference)) throw new IllegalArgumentException("Bad Krule structure. Expected a reference for type, but got: " + refType);

    // Get the referenced data
    if (refType.equals(Krule.URI_REF)) {
      // writing a URI
      Node value = getPropertyValue(n, RDF_VALUE);
      if (value == null) throw new IllegalArgumentException("Bad Krule structure. null value for: " + n);
      if (!(value instanceof URIReference)) throw new IllegalArgumentException("Bad Krule structure. URIReference came back with the wrong type: " + value + "(" + value.getClass().getName() + ")");
      return namespaceString((URIReference)value);

    } else if (refType.equals(Krule.VARIABLE)) {
      return nameByVar.get(n);

    } else if (refType.equals(Krule.LITERAL)) {
      // writing a literal
      Node value = getPropertyValue(n, RDF_VALUE);
      if (!(value instanceof Literal)) throw new IllegalArgumentException("Bad Krule structure. Literal came back with the wrong type: " + value + "(" + value.getClass().getName() + ")");
      return value.toString();

    } else throw new IllegalArgumentException("Bad Krule structure. Output node is not a URI Reference, a variable, or a literal: " + refType);
  }


  /**
   * Tests if a node is a reference to the rdf:type URI.
   * @param n The node to check as a reference.
   * @return <code>true</code> only if the node is a reference and it refers to rdf:type
   */
  boolean isType(Node n) {
    Node refType = typeByNode.get(n);  // Variable, URIReference or Literal
    if (refType == null) throw new IllegalArgumentException("Bad Krule structure. No type for node: " + n);
    if (!(refType instanceof URIReference)) throw new IllegalArgumentException("Bad Krule structure. Expected a reference for type, but got: " + refType);

    // Get the referenced data
    if (refType.equals(Krule.URI_REF)) {
      // writing a URI
      Node value = getPropertyValue(n, RDF_VALUE);
      if (value == null) throw new IllegalArgumentException("Bad Krule structure. Null value for: " + n);
      if (!(value instanceof URIReference)) throw new IllegalArgumentException("Bad Krule structure. URIReference came back with the wrong type: " + value + "(" + value.getClass().getName() + ")");
      return value.equals(TYPE);
    }
    return false;
  }


  /**
   * Get the namespace version of a URI.
   * e.g. rdf:value instead of http://www.w3.org/1999/02/22-rdf-syntax-ns#value
   * @param r The URI reference to convert to namespace form.
   * @return The namespaced version of the URI.
   */
  String namespaceString(URIReference r) {
    Pair<String,String> nsPair = addToNamespace(r);
    String dom = nsPair.first();
    String val = nsPair.second();

    // Krule is the default namespace
    if (dom.equals(Krule.KRULE)) return val;
    return ((dom.endsWith(":")) ? dom : dom + ":") + val;
  }


  /**
   * Scans nodes that are URIReferences, and adds them to the list of namespaces if not already there.
   * @param n A node that may be a reference to be scanned.
   */
  void addToNamespace(Node n) {
    if (!(n instanceof URIReference)) return;
    addToNamespace((URIReference)n);
  }


  /**
   * Scans a URIReference, and adds it to the list of namespaces if not already there.
   * @param r The reference to be scanned.
   * @return The domain abbreviation to use for this reference, and the value in the domain as a Pair.
   */
  Pair<String,String> addToNamespace(URIReference r) {
    Pair<String,String> nsPair = splitUri(r.getURI());
    String ns = nsPair.first();
    String val = nsPair.second();
    if (val.length() == 0) throw new IllegalArgumentException("Bad RLog data. URI needs a value in a domain to be serialized in RLog: " + r);

    // determine the domain to use for this namespace
    String dom = defaultNamespaces.get(ns);
    if (dom == null) {
      dom = namespaces.get(ns);
      if (dom == null) {
        namespaces.put(ns, dom = namespaceGen.newNamespace());
      }
    } else {
      // domain is in the default list, add it to the in-use list
      if (!namespaces.containsKey(ns)) namespaces.put(ns, dom);
    }
    return new Pair<String,String>(dom, val);
  }


  /**
   * Gets the reference for a sequence member predicate.
   * @param n The sequence index.
   * @return The URI reference for <em>rdf:_n</em>.
   */
  URIReference getListId(int n) {
    if (listIds.size() < n) {
      for (int i = listIds.size() + 1; i <= n; i++) {
        URI u = URI.create(RDF.BASE_URI + "_" + i);
        listIds.add(new URIReferenceImpl(u));
      }
    }
    return listIds.get(n - 1);
  }


  /**
   * Convert a literal into a valid variable name. Variable names are a single upper case character.
   * @param value The literal to convert.
   * @return The validated, and converted name of the variable.
   */
  String getValidVariableName(Literal value) {
    String var = ((Literal)value).getLexicalForm();
    if (var.length() != 1) {
      String v = var.substring(0, 1).toUpperCase();
      logger.warn("Krule structure uses long variable names. Truncating: " + var + " -> " + v);
      var = v;
    }
    if (!Character.isLetter(var.charAt(0))) throw new IllegalArgumentException("Bad Krule structure. Variable must be a letter: " + var);
    if (!Character.isUpperCase(var.charAt(0))) {
      logger.warn("Variable names must have upper case letters: " + var);
      var = var.toUpperCase();
    }
    return var;
  }

  
  /**
   * Gets the value of a property on an object.
   * @param s The object in the graph.
   * @param p The property being looked for.
   * @return The value of the required property.
   */
  Node getPropertyValue(Node s, URIReference p) {
    Map<URIReference,Set<Node>> values = graph.get(s);
    if (values == null) throw new IllegalArgumentException("Bad Krule structure. Missing property <" + p + "> on object <" + s + ">");
    return getSingle(values, p);
  }


  /**
   * Map an object to a property value pair.
   * @param subj The subject to set a property and value for.
   * @param property The property for the subject.
   * @param value The value for the subject's property.
   */
  static final void addPropertyValue(Map<Node,Map<URIReference,Set<Node>>> graph, Node subj, URIReference property, Node value) {
    Map<URIReference,Set<Node>> propVal = graph.get(subj);
    if (propVal == null) {
      propVal = new HashMap<URIReference,Set<Node>>();
      graph.put(subj, propVal);
    }
    addValue(propVal, property, value);
  }


  /**
   * Map a property to a value.
   * @param propertyValues The full set of properties and values for the current object.
   * @param property The property to set on the current object.
   * @param value The value to set the property to.
   */
  static final <T> void addValue(Map<T,Set<Node>> propertyValues, T property, Node value) {
    Set<Node> values = propertyValues.get(property);
    if (values == null) {
      values = new HashSet<Node>();
      propertyValues.put(property, values);
    }
    values.add(value);
  }


  /**
   * Retrieves a single value from a multimap.
   * @param <K> The type of the keys in the multimap.
   * @param <V> The type of the values in the multimap.
   * @param map The map to get the value from.
   * @param key The key to find the value with.
   * @return The single value found associated with the key, or null if not found.
   * @throws IllegalArgumentException If the key maps to a set with more than one value.
   */
  static final <K,V> V getSingle(Map<K,Set<V>> map, K key) {
    Set<V> vals = map.get(key);
    if (vals == null || vals.size() == 0) return null;
    if (vals.size() != 1) throw new IllegalArgumentException("Expecting singleton from a set of size: " + vals.size());
    return vals.iterator().next();
  }


  /**
   * Splits a URI into two sections. The second section contains the trailing identifier
   * that includes only letters, digits and underscores. The first section is everything else. 
   * @param uri The URI to split.
   * @return A pair of strings containing the two parts of the original URI.
   */
  static final Pair<String,String> splitUri(URI uri) {
    String u = uri.toString();
    for (int p = u.length() - 1; p >= 0; p--) {
      char ch = u.charAt(p);
      if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '-') {
        p = p + 1;
        return new Pair<String,String>(u.substring(0, p), u.substring(p));
      }
    }
    return new Pair<String,String>("", u);
  }


  /**
   * This class is a utility to generate new namespace names.
   */
  class NamespaceGenerator {
    /** The namespace counter */
    int n = 1;

    /**
     * Create a new name for a namespace.
     * @return A new name.
     */
    String newNamespace() {
      return "ns" + n++;
    }
  }
}
