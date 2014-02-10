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

package org.mulgara.sparql;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mulgara.sparql.parser.cst.BlankNode;
import org.mulgara.sparql.parser.cst.BooleanLiteral;
import org.mulgara.sparql.parser.cst.DecimalLiteral;
import org.mulgara.sparql.parser.cst.DoubleLiteral;
import org.mulgara.sparql.parser.cst.EmptyGraphPattern;
import org.mulgara.sparql.parser.cst.Expression;
import org.mulgara.sparql.parser.cst.GraphPatternConjunction;
import org.mulgara.sparql.parser.cst.GraphPatternDisjunction;
import org.mulgara.sparql.parser.cst.GraphPatternOptional;
import org.mulgara.sparql.parser.cst.GroupGraphPattern;
import org.mulgara.sparql.parser.cst.IRIReference;
import org.mulgara.sparql.parser.cst.IntegerLiteral;
import org.mulgara.sparql.parser.cst.Modifier;
import org.mulgara.sparql.parser.cst.Node;
import org.mulgara.sparql.parser.cst.RDFLiteral;
import org.mulgara.sparql.parser.cst.Triple;
import org.mulgara.sparql.parser.cst.TripleList;
import org.mulgara.sparql.parser.cst.VarAssign;
import org.mulgara.parser.MulgaraParserException;
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintAssignment;
import org.mulgara.query.ConstraintConjunction;
import org.mulgara.query.ConstraintDisjunction;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.ConstraintExpression;
import org.mulgara.query.ConstraintFalse;
import org.mulgara.query.ConstraintFilter;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.ConstraintIn;
import org.mulgara.query.ConstraintOptionalJoin;
import org.mulgara.query.SingleTransitiveConstraint;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.query.rdf.XSD;

/**
 * This object maps a {@link GroupGraphPattern} into a {@link ConstraintExpression}.
 *
 * @created Apr 21, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class PatternMapper {

  /** The pattern to start the mapping on. */
  GroupGraphPattern startPattern;

  /** An accumulating list of variables that are used in GRAPH patterns. */
  Set<Variable> graphVars = new HashSet<Variable>();

  /** An accumulating list of URIs that are used in GRAPH patterns. */
  Set<URI> graphUris = new HashSet<URI>();

  /**
   * Create a new mapper for a given graph pattern.
   * @param pattern The graph pattern to be mapped.
   */
  PatternMapper(GroupGraphPattern pattern) {
    startPattern = pattern;
  }

  /**
   * Get the set of variables that were employed as graphs in GRAPH patterns.
   * @return The set of variables used in GRAPH expressions.
   */
  Set<Variable> getGraphVars() {
    return graphVars;
  }

  /**
   * Perform the mapping of the graph pattern and return the results as a {@link ConstraintExpression}.
   * @return The mapped constraint expression.
   */
  ConstraintExpression mapToConstraints() throws MulgaraParserException {
    return mapPattern(startPattern);
  }

  /**
   * Converts a pattern to the matching constraint type.
   * @param pattern The pattern to convert.
   * @return The new ConstraintExpression which matches the pattern.
   */
  private ConstraintExpression mapPattern(GroupGraphPattern pattern) throws MulgaraParserException {
    PatternToConstraintMapper<? extends GroupGraphPattern> cons = constructors.get(pattern.getClass());
    if (cons == null) throw new UnsupportedOperationException("Unknown SPARQL pattern: " + pattern.getClass().getSimpleName());
    ConstraintExpression result = cons.map(pattern, this);
    result = applyFilter(result, pattern.getFilter());
    result = applyGraph(result, pattern.getGraph());
    return result;
  }

  /**
   * Apply a FILTER to a constraint expression.
   * @param constraint The expression to be filtered.
   * @param filter The filter to be wrapped around this constraint.
   * @return The filtered version of the constraint.
   */
  private ConstraintExpression applyFilter(ConstraintExpression constraint, Expression filter) throws MulgaraParserException {
    if (filter == null) return constraint;
    FilterMapper filterMapper = new FilterMapper(filter);
    return new ConstraintFilter(constraint, filterMapper.getFilter());
  }

  /**
   * Apply the parameter of a GRAPH modifier to a constraint expression.
   * @param constraint The expression to be updated.
   * @param graph The parameter of the GRAPH expression that is to be propagated through the constraint.
   * @return The modified version of the constraint.
   */
  private ConstraintExpression applyGraph(ConstraintExpression constraint, Expression graph) {
    if (graph == null) return constraint;
    // graph is a Variable or IRIReference
    if (graph instanceof org.mulgara.sparql.parser.cst.Variable) {
      org.mulgara.sparql.parser.cst.Variable v = (org.mulgara.sparql.parser.cst.Variable)graph;
      if (v.getName().equals("_from"));
      Variable var = new Variable(v.getName());
      // remember this is variable to be bound to the FROM NAMED values
      graphVars.add(var);
      constraint = new ConstraintIn(constraint, var);
    } else if (graph instanceof IRIReference) {
      // store this reference as a value that should be in the FROM NAMED list
      URI ref = ((IRIReference)graph).getUri();
      graphUris.add(ref);
      constraint = new ConstraintIn(constraint, new URIReferenceImpl(ref, false));
    } else {
      throw new IllegalArgumentException("Illegal argument in a GRAPH expression: " + graph.getClass().getSimpleName());
    }
    return constraint;
  }

  /**
   * A case analysis to convert simple types into {@link ConstraintElement}s.
   * @param n The {@link Node} to convert to a ConstraintElement.
   * @return A new constraint element that matches the node n.
   */
  private static ConstraintElement convertElement(Node n) {
    if (n instanceof org.mulgara.sparql.parser.cst.Variable) {
      return new Variable(((org.mulgara.sparql.parser.cst.Variable)n).getName());
    }
    if (n instanceof IRIReference) return new URIReferenceImpl(((IRIReference)n).getUri(), false);
    if (n instanceof BlankNode) return new Variable(((BlankNode)n).getLabel());
    if (n instanceof RDFLiteral) {
      RDFLiteral lit = (RDFLiteral)n;
      if (lit.isTyped()) return new LiteralImpl(lit.getValue(), lit.getDatatype().getUri());
      if (lit.isLanguageCoded()) return new LiteralImpl(lit.getValue(), lit.getLanguage());
      return new LiteralImpl(lit.getValue());
    }
    // decimal, double, boolean
    if (n instanceof DecimalLiteral) return new LiteralImpl(((IntegerLiteral)n).getValue().toString(), XSD.DECIMAL_URI);
    if (n instanceof DoubleLiteral) return new LiteralImpl(((IntegerLiteral)n).getValue().toString(), XSD.DOUBLE_URI);
    if (n instanceof BooleanLiteral) return new LiteralImpl(((IntegerLiteral)n).getValue().toString(), XSD.BOOLEAN_URI);
    // don't know what to make of this
    throw new UnsupportedOperationException("Unhandled data type in triple: " + n.getClass().getSimpleName());
  }

  /**
   * Converts a Triple from the CST into a ConstraintImpl in the AST.
   * @param t The triple to convert.
   * @return The new constraint.
   */
  private static ConstraintExpression newConstraintImpl(Triple t) {
    ConstraintElement s = convertElement(t.getSubject());
    ConstraintElement p = convertElement(t.getPredicate());
    Modifier m = t.getPredicateModifier();
    // if the object it a literal, then it may need to be expanded into various equivalent types
    Node n = t.getObject();
    if (n instanceof IntegerLiteral) {
      ConstraintElement[] numbers = createIntLiterals((IntegerLiteral)n);
      List<ConstraintExpression> options = new ArrayList<ConstraintExpression>();
      for (ConstraintElement obj: numbers) {
        options.add(newConstraint(s, p, obj, m));
      }
      return new ConstraintDisjunction(options);
    }

    ConstraintElement o = convertElement(n);
    return newConstraint(s, p, o, m);
  }

  private static ConstraintExpression newConstraint(ConstraintElement s, ConstraintElement p, ConstraintElement o, Modifier m) {
    Constraint c = new ConstraintImpl(s, p, o);
    if (m == Modifier.none) return c;
    boolean zeroStep = m == Modifier.star;
    return new ConstraintDisjunction(c, new SingleTransitiveConstraint(c, zeroStep));
  }

  /**
   * Create an array of the literals represented by a number.
   * @param nrText The text of a number.
   * @return All the literals that this number can represent.
   */
  private static ConstraintElement[] createIntLiterals(IntegerLiteral n) {
    List<ConstraintElement> elts = new ArrayList<ConstraintElement>();
    String nrText = n.getValue().toString();
    long nr = n.getValue().longValue();

    elts.add(new LiteralImpl(nrText, XSD.DECIMAL_URI));
    elts.add(new LiteralImpl(nrText, XSD.INTEGER_URI));
    elts.add(new LiteralImpl(nrText, XSD.LONG_URI));
    if (nr <= Integer.MAX_VALUE && nr >= Integer.MIN_VALUE) {
      elts.add(new LiteralImpl(nrText, XSD.INT_URI));
      if (nr <= Short.MAX_VALUE && nr >= Short.MIN_VALUE) {
        elts.add(new LiteralImpl(nrText, XSD.SHORT_URI));
        if (nr <= Byte.MAX_VALUE && nr >= Byte.MIN_VALUE) {
          elts.add(new LiteralImpl(nrText, XSD.BYTE_URI));
        }
      }
    }
    if (nr < 0) {
      elts.add(new LiteralImpl(nrText, XSD.NON_POSITIVE_INTEGER_URI));
      elts.add(new LiteralImpl(nrText, XSD.NEGATIVE_INTEGER_URI));
    } else if (nr > 0) {
      elts.add(new LiteralImpl(nrText, XSD.NON_NEGATIVE_INTEGER_URI));
      elts.add(new LiteralImpl(nrText, XSD.POSITIVE_INTEGER_URI));
      elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_LONG_URI));
      if (nr <= UInt.MAX_VALUE) {
        elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_INT_URI));
        if (nr <= UShort.MAX_VALUE) {
          elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_SHORT_URI));
          if (nr <= UByte.MAX_VALUE) {
            elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_BYTE_URI));
          }
        }
      }
    } else { // nr == 0
      elts.add(new LiteralImpl(nrText, XSD.NON_POSITIVE_INTEGER_URI));
      elts.add(new LiteralImpl(nrText, XSD.NON_NEGATIVE_INTEGER_URI));
      elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_LONG_URI));
      elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_INT_URI));
      elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_SHORT_URI));
      elts.add(new LiteralImpl(nrText, XSD.UNSIGNED_BYTE_URI));
    }
    return elts.toArray(new ConstraintElement[elts.size()]);
  }

  /** A mapping of pattern types to constructors for the objects they map to. */
  private static Map<Class<? extends GroupGraphPattern>,PatternToConstraintMapper<? extends GroupGraphPattern>> constructors = new HashMap<Class<? extends GroupGraphPattern>,PatternToConstraintMapper<? extends GroupGraphPattern>>();

  /**
   * The class for the mapping of {@link GroupGraphPattern}s to {@link ConstraintExpression}s.
   * This class and extending classes are not static, as the classes will be call back into the
   * outer class to recurse down the CST.
   * The reason this is an abstract class instead of an interface is so map(T) can be set
   * to accept the general GroupGraphPattern and do the cast. This was not possible outside
   * of the context of the generic type T.
   */
  private static abstract class PatternToConstraintMapper<T extends GroupGraphPattern> {
    /** An entry point for the map operation. This method handles casting to be compatible with the generic template. */
    @SuppressWarnings("unchecked")
    public ConstraintExpression map(GroupGraphPattern pattern, PatternMapper mapper) throws MulgaraParserException { return typedMap((T)pattern, mapper); }
    /**
     * Convert a GroupGraphPattern to a ConstraintExpression for Mulgara.
     * @param pattern The pattern to convert. Should be specific to the mapper.
     * @return The constraint expression relevant to the mapper.
     */
    abstract ConstraintExpression typedMap(T pattern, PatternMapper mapper) throws MulgaraParserException;
    /** Identify the class to be mapped by the extension. */
    public abstract Class<T> getMapType();
  }

  /**
   * Utility method to add a pattern mapper to the map, keyed on the class it maps.
   * @param mapper The mapper to add to the map.
   */
  static void addToMap(PatternToConstraintMapper<? extends GroupGraphPattern> mapper) {
    constructors.put(mapper.getMapType(), mapper);
  }

  /**
   * Initialize the mapping of patterns to the constraint builders.
   * This is not static in order to avoid passing "this" through to the methods
   * on each of the implementing mapper classes.
   */
  static {
    addToMap(new EmptyGraphPatternToConstraint());
    addToMap(new GraphPatternConjunctionToConstraint());
    addToMap(new GraphPatternDisjunctionToConstraint());
    addToMap(new GraphPatternOptionalToConstraint());
    addToMap(new VarAssignToConstraint());
    addToMap(new TripleToConstraint());
    addToMap(new TripleListToConstraint());
  }

  /** Map the empty graph pattern to a constraint that always resolves to nothing. */
  private static class EmptyGraphPatternToConstraint extends PatternToConstraintMapper<EmptyGraphPattern> {
    public Class<EmptyGraphPattern> getMapType() { return EmptyGraphPattern.class; }
    ConstraintExpression typedMap(EmptyGraphPattern pattern, PatternMapper mapper) {
      return ConstraintFalse.INSTANCE;
    }
  }

  /** Map the conjunctions to ConstraintConjunction. */
  private static class GraphPatternConjunctionToConstraint extends PatternToConstraintMapper<GraphPatternConjunction> {
    public Class<GraphPatternConjunction> getMapType() { return GraphPatternConjunction.class; }
    ConstraintExpression typedMap(GraphPatternConjunction pattern, PatternMapper mapper) throws MulgaraParserException {
      List<GroupGraphPattern> list = pattern.getElements();
      List<ConstraintExpression> newList = new ArrayList<ConstraintExpression>(list.size());
      for (GroupGraphPattern p: list) {
        if (p instanceof EmptyGraphPattern) {
          if (p.getFilter() != null) throw new MulgaraParserException("Unexpected filter on empty pattern");
        } else newList.add(mapper.mapPattern(p));
      }
      if (newList.size() == 1) return newList.get(0);
      return new ConstraintConjunction(newList);
    }
  }

  /** Map the disjunctions to ConstraintConjunction. */
  private static class GraphPatternDisjunctionToConstraint extends PatternToConstraintMapper<GraphPatternDisjunction> {
    public Class<GraphPatternDisjunction> getMapType() { return GraphPatternDisjunction.class; }
    ConstraintExpression typedMap(GraphPatternDisjunction pattern, PatternMapper mapper) throws MulgaraParserException {
      List<GroupGraphPattern> list = pattern.getElements();
      List<ConstraintExpression> newList = new ArrayList<ConstraintExpression>(list.size());
      for (GroupGraphPattern p: list) {
        if (p instanceof EmptyGraphPattern) {
          // if (p.getFilter() != null) throw new MulgaraParserException("Unexpected filter on empty pattern");
        } else newList.add(mapper.mapPattern(p));
      }
      if (newList.size() == 1) return newList.get(0);
      return new ConstraintDisjunction(newList);
    }
  }

  /** Map the optional patterns to ConstraintOptional. */
  private static class GraphPatternOptionalToConstraint extends PatternToConstraintMapper<GraphPatternOptional> {
    public Class<GraphPatternOptional> getMapType() { return GraphPatternOptional.class; }
    ConstraintExpression typedMap(GraphPatternOptional pattern, PatternMapper mapper) throws MulgaraParserException {
      ConstraintExpression opt = mapper.mapPattern(pattern.getOptional());
      if (opt instanceof ConstraintFilter) {
        ConstraintFilter f = (ConstraintFilter)opt;
        return new ConstraintOptionalJoin(mapper.mapPattern(pattern.getMain()), f.getUnfilteredConstraint(), f.getFilter());
      } else {
        return new ConstraintOptionalJoin(mapper.mapPattern(pattern.getMain()), opt);
      }
    }
  }

  /** Map the assignment patterns to ConstraintAssignment. */
  private static class VarAssignToConstraint extends PatternToConstraintMapper<VarAssign> {
    public Class<VarAssign> getMapType() { return VarAssign.class; }
    ConstraintExpression typedMap(VarAssign pattern, PatternMapper mapper) throws MulgaraParserException {
      ConstraintExpression main = mapper.mapPattern(pattern.getMain());
      org.mulgara.query.Variable v = new org.mulgara.query.Variable(pattern.getVar().getName());
      FilterMapper filterMapper = new FilterMapper(pattern.getExpression());
      return new ConstraintAssignment(main, v, filterMapper.getFilter());
    }
  }

  /** Map the triple patterns to ConstraintImpl. */
  private static class TripleToConstraint extends PatternToConstraintMapper<Triple> {
    public Class<Triple> getMapType() { return Triple.class; }
    ConstraintExpression typedMap(Triple pattern, PatternMapper mapper) {
      return newConstraintImpl(pattern);
    }
  }

  /** Map the lists of triple patterns to ConstraintConjunctions on ConstraintImpl. */
  private static class TripleListToConstraint extends PatternToConstraintMapper<TripleList> {
    public Class<TripleList> getMapType() { return TripleList.class; }
    @SuppressWarnings("unchecked")
    ConstraintExpression typedMap(TripleList pattern, PatternMapper mapper) {
      List<Triple> triples = (List<Triple>)pattern.getElements();
      List<ConstraintExpression> constraints = new ArrayList<ConstraintExpression>(triples.size());
      for (Triple t: triples) constraints.add(newConstraintImpl(t));
      return new ConstraintConjunction(constraints);
    }
  }

  /** Describes the range of unsigned ints */
  private static class UInt { public static final long MAX_VALUE = 0xFFFFFFFFL; }

  /** Describes the range of unsigned shorts */
  private static class UShort { public static final long MAX_VALUE = 0xFFFFL; }

  /** Describes the range of unsigned bytes */
  private static class UByte { public static final long MAX_VALUE = 0xFFL; }
}
