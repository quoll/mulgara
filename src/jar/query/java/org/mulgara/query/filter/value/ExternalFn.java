/**
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
package org.mulgara.query.filter.value;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.jrdf.vocabulary.RDF;
import org.mulgara.parser.MulgaraParserException;
import org.mulgara.query.FunctionResolverRegistry;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.query.rdf.XSD;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

/**
 * Executes a function that isn't defined in these packages. If the function is external
 * then the answer is presumed to be scalar. URIs are automatically treated as IRIs, and anything else
 * is treated as a literal.
 *
 * @created Apr 22, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ExternalFn extends AbstractAccessorFn implements NumericExpression {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 5748124115023875223L;

  /** The logger */
  private final static Logger logger = Logger.getLogger(ExternalFn.class.getName());
  
  /** A URI containing the namespace for XSD */
  private final static URI XSD_URI = URI.create(XSD.NAMESPACE);

  /** The scheme for XSD */
  private final static String XSD_SCHEME = XSD_URI.getScheme();

  /** The scheme-specific part for XSD */
  private final static String XSD_PART = XSD_URI.getSchemeSpecificPart();

  /** The function to be run. This will be mapped to a functor or reflection code. */
  private URI fnUri;

  /** This is a constructor function. */
  private boolean isConstructor = false;

  /** The external function to execute. */
  XPathFunction extFn = null;

  /** Caches functions by their label of iri/#args. */
  private static Map<String,XPathFunction> fnCache = new WeakHashMap<String,XPathFunction>();

  /** Indicates that an error will always be returned from this method. */
  private boolean unrecoverableError = false;

  /**
   * Create a new function instance.
   * @param fn The function to run.
   * @param operands The arguments of the function.
   */
  public ExternalFn(IRI fn, RDFTerm... operands) throws MulgaraParserException {
    super(operands);
    fnUri = fn.getValue();
    if (isCast(fnUri)) {
      if (operands.length != 1) throw new MulgaraParserException("Cast operation can only take a single parameter");
      isConstructor = true;
    } else {
      extFn = findFunction(fn, operands.length);
      if (extFn == null) logger.debug("Unknown function: " + fn);
    }
  }

  /**
   * Tests if the URI is used for casting a literal. Anything that is in the XSD namespace,
   * or the rdf:XMLLiteral type, is considered to be a construction operation.
   * @param u The URI to test.
   * @return <code>true</code> If the URI is a known type for casting.
   */
  private boolean isCast(URI u) {
    if (XSD_SCHEME.equals(fnUri.getScheme()) && XSD_PART.equals(fnUri.getSchemeSpecificPart())) return true;
    if (XSD.DOM.equals(fnUri.getScheme())) return true;
    if (RDF.XML_LITERAL.equals(u) || RDF.XML_LITERAL_ABBR.equals(u)) return true;
    return false;
  }

  // The ValueLiteral interface

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getLexical()
   * @throws QueryException if this function does not resolve to a literal.
   */
  public String getLexical() throws QueryException {
    RDFTerm result = resolve();
    if (result.isLiteral()) return ((ValueLiteral)result).getLexical();
    throw new QueryException("Type Error: Not valid to ask the lexical form of a: " + result.getClass().getSimpleName());
  }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getLang()
   * @throws QueryException if this function does not resolve to a literal.
   */
  public SimpleLiteral getLang() throws QueryException {
    RDFTerm result = resolve();
    if (result.isLiteral()) return ((ValueLiteral)result).getLang();
    throw new QueryException("Type Error: Not valid to ask the language of a: " + result.getClass().getSimpleName());
  }

  /**
   * @see org.mulgara.query.filter.value.ValueLiteral#getType()
   * @throws QueryException if this function does not resolve to a literal.
   */
  public IRI getType() throws QueryException {
    RDFTerm result = resolve();
    if (result.isLiteral()) return ((ValueLiteral)result).getType();
    throw new QueryException("Type Error: Not valid to ask the type of a: " + result.getClass().getSimpleName());
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#isSimple() */
  public boolean isSimple() throws QueryException {
    RDFTerm result = resolve();
    if (result.isLiteral()) return ((ValueLiteral)result).isSimple();
    throw new QueryException("Type Error: Not valid to check if a non-literal is a simple literal: " + result.getClass().getSimpleName());
  }

  // The RDFTerm interface

  /** @see org.mulgara.query.filter.RDFTerm#isBlank() */
  public boolean isBlank() throws QueryException { return resolve().isBlank(); }

  /** @see org.mulgara.query.filter.RDFTerm#isIRI() */
  public boolean isIRI() throws QueryException { return resolve().isIRI(); }

  /**
   * {@inheritDoc}
   * The operation of this method is depended on the context in which it was called.
   * If it is called without a context owner, then this means it was called during
   * Filter construction, and we want to indicate that it is valid to treat this as a literal.
   * @return <code>true</code> if there is no context, or else it calls isLiteral on the resolved value.
   */
  public boolean isLiteral() throws QueryException {
    return getContextOwner() == null ? true : resolve().isLiteral();
  }

  /** @see java.lang.Object#toString() */
  public String toString() {
    String result = "function[" + fnUri + "]";
    if (extFn != null) result += " -> " + extFn.getClass().getName();
    return result;
  }

  /**
   * Resolve the value of the function.
   * @return The resolution of the function
   * @throws QueryException if the function does not resolve
   */
  protected RDFTerm resolve() throws QueryException {
    if (isConstructor) {
      List<Object> args = resolveArgs();
      assert args.size() == 1;
      Object value = args.get(0);
      // being a cast we'll have to resort to the lowest common denominator of "string"
      // and let the TypedLiteral work it out for us
      if (XSD.isNumericType(fnUri) && value instanceof Number) return new NumericLiteral(NumericLiteral.getValueFor((Number)value, fnUri), fnUri);
      return TypedLiteral.newLiteral(value.toString(), fnUri, null);
    }
    if (extFn == null) {
      if (logger.isDebugEnabled()) logger.debug("Attempting to execute an unsupported function: " + fnUri + "(" + resolveArgs() + ")");
      return Bool.FALSE;
    }
    if (unrecoverableError) return Bool.FALSE;
    Object result;
    try {
      result = extFn.evaluate(resolveArgs());
    } catch (XPathFunctionException e) {
      if (invalidFunctionException(e)) {
        unrecoverableError = true;
        logger.error("Error executing XPathFunction", e);
      } else {
        if (logger.isDebugEnabled()) logger.debug("Error executing XPathFunction", e);
      }
      throw new QueryException("Error executing external function", e);
    }
    return (result.getClass() == URI.class) ? new IRI((URI)result) : TypedLiteral.newLiteral(result);
  }

  /**
   * A utility function to create a list of arguments to be passed to the external function.
   * @return A {@link List} of arbitrary objects to be passed as arguments to the external function.
   * @throws QueryException If any of the arguments could not be resolved.
   */
  private List<Object> resolveArgs() throws QueryException {
    List<Object> result = new ArrayList<Object>(operands.length);
    for (int i = 0; i < operands.length; i++) {
      RDFTerm op = operands[i];
      if (isConstructor && op.isBlank()) throw new QueryException("Type Error: Cannot cast a blank node.");
      result.add(op.getValue());
    }
    return result;
  }

  /**
   * Extract a numeric value from this expression, if legal. This may result in a type exception.
   * @see org.mulgara.query.filter.value.NumericExpression#getNumber()
   * @return A numeric value for the resolved expression.
   * @throws QueryException If the resolved expression is not a numeric type.
   */
  public Number getNumber() throws QueryException {
    RDFTerm result = resolve();
    if (!result.isLiteral() && !(result instanceof NumericExpression)) throw new QueryException("Type Error: Not valid to ask the numeric form of a: " + result.getClass().getSimpleName());
    return ((NumericExpression)result).getNumber();
  }

  /**
   * Look for a function in the registered XPathFunctionResolvers.
   * @param iri The URI of the function to find.
   * @return The requested XPathFunction, or <code>null</code> if not found.
   */
  private XPathFunction findFunction(IRI iri, int argCount) {
    String label = iri.toString() + "/" + argCount;
    XPathFunction result = fnCache.get(label);
    if (result == null) {
      QName fnName = iri.getQName();
      if (fnName == null) return null;
      for (XPathFunctionResolver resolver: FunctionResolverRegistry.getFunctionResolverRegistry()) {
        try {
          result = resolver.resolveFunction(fnName, argCount);
          if (result != null) {
            fnCache.put(label, result);
            break;
          }
        } catch (Exception e) {
          // this resolver is unable to handle the given QName
          result = null;
        }
      }
    }
    return result;
  }

  /**
   * Test if an exception indicates that a method is unavailable.
   * @param e An exception thrown when an external method is invoked.
   * @return <code>true</code> if the exception indicates that the method can never be successful.
   */
  private static boolean invalidFunctionException(Exception e) {
    Throwable t = e;
    do {
      if (t instanceof NoSuchMethodException) return true;
    } while ((t = t.getCause()) != null);
    return false;
  }
}
