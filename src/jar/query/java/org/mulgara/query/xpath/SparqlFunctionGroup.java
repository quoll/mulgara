/*
 * Copyright 2009 DuraSpace.
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

package org.mulgara.query.xpath;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathFunctionException;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.SimpleLiteral;
import org.mulgara.query.functions.MulgaraFunction;
import org.mulgara.query.functions.MulgaraFunctionGroup;

/**
 * Container for functions in the SPARQL domain.
 *
 * @created Oct 5, 2009
 * @author Paul Gearon
 * @copyright &copy; 2009 <a href="http://www.duraspace.org/">DuraSpace</a>
 */
public class SparqlFunctionGroup implements MulgaraFunctionGroup {

  /** The prefix for the sparql: namespace */
  static final String PREFIX = "sparql";

  /** The sparql: namespace */
  static final String NAMESPACE = "http://www.w3.org/2006/sparql-functions#";

  /**
   * Get the prefix used for the namespace of these operations.
   * @return The short string used for a prefix in a QName.
   */
  public String getPrefix() {
    return PREFIX;
  }

  /**
   * Get the namespace of these operations.
   * @return The string of the namespace URI.
   */
  public String getNamespace() {
    return NAMESPACE;
  }

  /**
   * Get the set of SPARQL functions.
   * @return A set of MulgaraFunction for this entire group.
   */
  public Set<MulgaraFunction> getAllFunctions() {
    Set<MulgaraFunction> functions = new HashSet<MulgaraFunction>();
    functions.add(new Bound());
    functions.add(new IsIRI());
    functions.add(new IsURI());
    functions.add(new IsBlank());
    functions.add(new IsLiteral());
    functions.add(new Str());
    functions.add(new Lang());
    functions.add(new Datatype());
    functions.add(new LangMatches());
    functions.add(new LogicalOr());
    functions.add(new LogicalAnd());
    return functions;
  }

  /** Function to evaluate if a parameter is bound. */
  static private class Bound extends MulgaraFunction {
    public Object eval(List<?> args) { return args.get(0) != null; }
  }

  /** Function to evaluate if a parameter is an IRI. */
  static private class IsIRI extends MulgaraFunction {
    public Object eval(List<?> args) { return args.get(0) instanceof URI; }
  }

  /** Function to evaluate if a parameter is a URI. */
  static private class IsURI extends MulgaraFunction {
    public Object eval(List<?> args) { return args.get(0) instanceof URI; }
  }

  /** Function to evaluate if a parameter is a blank node. */
  static private class IsBlank extends MulgaraFunction {
    public Object eval(List<?> args) { return args.get(0) instanceof BlankNode; }
  }

  /** Function to evaluate if a parameter is a literal. */
  static private class IsLiteral extends MulgaraFunction {
    public Object eval(List<?> args) {
      Object o = args.get(0);
      return o instanceof Literal || o instanceof String || o instanceof Number;
    }
  }

  /** Function to convert a value to a string. */
  static private class Str extends MulgaraFunction {
    public Object eval(List<?> args) { return args.get(0).toString(); }
  }

  /** Function to get the language of a literal. This information is probably not available for this implementation. */
  static private class Lang extends MulgaraFunction {
    public Object eval(List<?> args) {
      Object o = args.get(0);
      return o instanceof Literal ? ((Literal)o).getLanguage() : "";
    }
  }
    
  /** Function to get the datatype of a literal. This information is probably not available for this implementation. */
  static private class Datatype extends MulgaraFunction {
    public Object eval(List<?> args) {
      Object o = args.get(0);
      return o instanceof Literal ? ((Literal)o).getDatatype() : null;
    }
  }
  
  /**
   * Function to test if a language code matches a pattern.
   * This information is probably not available for this implementation.
   */
  static private class LangMatches extends MulgaraFunction {
    protected int getArity() { return 2; }
    public Object eval(List<?> args) throws XPathFunctionException {
      Object o = args.get(0);
      if (o instanceof Literal) {
        Literal l = (Literal)o;
        if (l.getLanguage() == null || l.getLanguage().length() == 0) return false;
        org.mulgara.query.filter.LangMatches match = new org.mulgara.query.filter.LangMatches(new SimpleLiteral(l.getLexicalForm(), l.getLanguage()), new SimpleLiteral((String)args.get(1)));
        try {
          return match.getValue();
        } catch (QueryException e) {
          throw new XPathFunctionException("Unable to get data from lang matching test: " + e.getMessage());
        }
      }
      return false;
    }
  }

  /** Common operations required for a logical operation. */
  static private abstract class LogicOp extends MulgaraFunction {
    protected int getArity() { return 2; }
    protected abstract boolean op(Object left, Object right) throws XPathFunctionException;
    public Object eval(List<?> args) throws XPathFunctionException {
      return op(args.get(0), args.get(1));
    }
  }

  /** Function to perform a logical OR between 2 operands. */
  static private class LogicalOr extends LogicOp {
    public String getName() { return "logical-or"; }
    public boolean op(Object left, Object right) throws XPathFunctionException {
      return toBool(left) || toBool(right);
    }
  }

  /** Function to perform a logical AND between 2 operands. */
  static private class LogicalAnd extends LogicOp {
    public String getName() { return "logical-and"; }
    public boolean op(Object left, Object right) throws XPathFunctionException {
      return toBool(left) && toBool(right);
    }
  }

}
