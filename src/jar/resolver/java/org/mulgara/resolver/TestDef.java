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
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;  // Log4J

// Locally written packages
import org.mulgara.query.*;

/**
* Test case for {@link DatabaseSession}.
*
* @created 2004-06-15
* @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
* @version $Revision: 1.8 $
* @modified $Date: 2005/01/05 04:58:24 $Author: newmana $
* @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
* @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
*      Software Pty Ltd</a>
* @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
*/
public class TestDef {
  /** Logger.  */
  private static Logger logger = Logger.getLogger(TestDef.class.getName());

  public String name;
  public String[] resolvers;
  public List<Variable> selectList;
  public GraphExpression model;
  public ConstraintExpression query;
  public List<List<Object>> results;
  public String errorString;

  @SuppressWarnings("unchecked")
  public TestDef(String name, String[] resolvers, TestQuery query,
                 GraphExpression model, List results, String errorString) {
    this.name = name;
    this.resolvers = resolvers;
    this.model = model;
    this.selectList = query.selectList;
    this.query = query.query;
    this.results = results;
    this.errorString = errorString;
  }


  static private class TestQuery {
    public List<Variable> selectList;
    public ConstraintExpression query;
    public TestQuery(List<Variable> selectList, ConstraintExpression query) {
      this.selectList = selectList;
      this.query = query;
    }
  }


  static public class Parser {
    private ConstraintElement[] elements;
    private GraphResource[] models;

    public Parser(ConstraintElement[] elements, GraphResource[] models) {
      this.elements = elements;
      this.models = models;
    }

    public TestDef parse(String name, String[] resolvers, String query,
                         String model, String resultDefs, String errorString) {
      return new TestDef(name, resolvers,
          query == null ? null : parseQuery(new StringTokenizer(query, "() ", true)),
          model == null ? null : parseModel(new StringTokenizer(model, "() ", true)),
          resultDefs == null ? null : parseResultDefinition(new StringTokenizer(resultDefs, "() ", true)),
          errorString);
    }


    private String getToken(StringTokenizer tokenizer) {
      String token;
      do {
        token = tokenizer.nextToken();
      } while (token.equals(" "));

      logger.debug("Returning token '" + token + "'");
      return token;
    }


    private GraphExpression parseModel(StringTokenizer tokenizer) {
      String token = getToken(tokenizer);

      if ("(".equals(token)) {
        return parseModelOperation(tokenizer);
      }
      if (token.startsWith("M")) {
        return parseModelResource(token);
      }
      throw new IllegalArgumentException("Unrecognized token in modelExpression " + token);
    }


    private GraphOperation parseModelOperation(StringTokenizer tokenizer) {
      String token = getToken(tokenizer);
      GraphExpression lhs = parseModel(tokenizer);
      GraphExpression rhs = parseModel(tokenizer);
      String terminator = getToken(tokenizer);
      if (!")".equals(terminator)) {
        throw new IllegalArgumentException("Unterminated GraphOperation " + terminator);
      }
      if ("union".equals(token)) {
        return new GraphUnion(lhs, rhs);
      }
      if ("intersect".equals(token)) {
        return new GraphIntersection(lhs, rhs);
      }
      throw new IllegalArgumentException("Unknown GraphOperation " + token);
    }


    private GraphResource parseModelResource(String token) {
      try {
        int index = Integer.parseInt(token.substring(1));
        if (index > models.length) {
          throw new IllegalArgumentException("Invalid GraphResource index " + index);
        }

        return models[index];
      } catch (NumberFormatException en) {
        throw new IllegalArgumentException("Invalid GraphResource descriptor" + token);
      }
    }


    private TestQuery parseQuery(StringTokenizer tokenizer) {
      String token = getToken(tokenizer);
      if ("(".equals(token)) {
        return parseQueryExpression(tokenizer);
      } else {
        throw new IllegalArgumentException("Invalid initial token in modelExpression " + token);
      }
    }


    private TestQuery parseQueryExpression(StringTokenizer tokenizer) {
      String token = getToken(tokenizer);
      if ("query".equals(token)) {
        List<Variable> selectList = parseSelectList(tokenizer);

        token = getToken(tokenizer);
        if (!"(".equals(token)) {
          throw new IllegalArgumentException("Query's ConstraintExpression must be an s-expr " + token);
        }
        ConstraintExpression expr = parseConstraintExpression(tokenizer);

        return new TestQuery(selectList, expr);
      } else {
        throw new IllegalArgumentException("Expected query-expression to start with query " + token);
      }
    }


    private List<Variable> parseSelectList(StringTokenizer tokenizer) {
      String token = getToken(tokenizer);
      if ("(".equals(token)) {
        return parseVariableList(tokenizer);
      } else {
        return Collections.singletonList(parseVariable(token));
      }
    }


    private List<Variable> parseVariableList(StringTokenizer tokenizer) {
      List<Variable> variableList = new ArrayList<Variable>();
      String token = getToken(tokenizer);
      while (!")".equals(token)) {
        variableList.add(parseVariable(token));
        token = getToken(tokenizer);
      }

      return variableList;
    }


    private Variable parseVariable(String token) {
      try {
        int index = Integer.parseInt(token.substring(1));
        if (index > elements.length) {
          throw new IllegalArgumentException("Invalid Variable index " + index);
        }
        if (elements[index] instanceof Variable) {
          return (Variable)elements[index];
        } else {
          throw new IllegalArgumentException("Variable reference not a variable " + token);
        }
      } catch (NumberFormatException en) {
        throw new IllegalArgumentException("Invalid Variable descriptor" + token);
      }
    }


    private ConstraintExpression parseConstraintExpression(StringTokenizer tokenizer) {
      String token = getToken(tokenizer);
      if ("and".equals(token)) {
        return new ConstraintConjunction(parseConstraintArguments(tokenizer));
      }
      if ("or".equals(token)) {
        return new ConstraintDisjunction(parseConstraintArguments(tokenizer));
      }
      if ("|".equals(token)) {
        return parseConstraint(tokenizer);
      }
      throw new IllegalArgumentException("Invalid ConstraintExpression " + token);
    }


    private List<ConstraintExpression> parseConstraintArguments(StringTokenizer tokenizer) {
      List<ConstraintExpression> arguments = new ArrayList<ConstraintExpression>();

      while (true) {
        String token = getToken(tokenizer);
        if ("(".equals(token)) {
          arguments.add(parseConstraintExpression(tokenizer));
        } else  if (")".equals(token)) {
          break;
        } else {
          throw new IllegalArgumentException("Arguments ConstraintExpression must be an s-expr " + token);
        }
      }

      return arguments;
    }


    private Constraint parseConstraint(StringTokenizer tokenizer) {
      Constraint constraint =
          new ConstraintImpl(parseConstraintElement(getToken(tokenizer)),
                         parseConstraintElement(getToken(tokenizer)),
                         parseConstraintElement(getToken(tokenizer)));
      String term = getToken(tokenizer);
      if (")".equals(term)) {
        return constraint;
      } else {
        throw new IllegalArgumentException("Too many constraint elements in constraint " + term);
      }
    }


    private ConstraintElement parseConstraintElement(String token) {
      try {
        int index = Integer.parseInt(token.substring(1));
        if (index > elements.length) {
          throw new IllegalArgumentException("Invalid ConstraintElement index " + index);
        }

        return elements[index];
      } catch (NumberFormatException en) {
        throw new IllegalArgumentException("Invalid ConstraintElement descriptor" + token);
      }
    }


    /**
     * @return A list of lists (to arbitrary depth) of result-strings.
     */
    @SuppressWarnings("unchecked")
    private List parseResultDefinition(StringTokenizer tokenizer) {
      logger.debug("Parsing Result Definition");
      String token = getToken(tokenizer);
      if ("(".equals(token)) {
        return parseResultExpression(tokenizer);
      } else {
        throw new IllegalArgumentException("Invalid initial token in modelExpression " + token);
      }
    }


    /**
     * @return A list of lists (to arbitrary depth) of result-strings.
     */
    @SuppressWarnings("unchecked")
    private List parseResultExpression(StringTokenizer tokenizer) {
      logger.debug("parseResultExpression");
      String token = getToken(tokenizer);
      if ("result".equals(token)) {
        List<List<String>> result = parseResultList(tokenizer);
        logger.debug("returning result-list-expression: " + result);
        return result;
      }
      if ("product".equals(token)) {
        LinkedList productTerm = parseProductTerms(tokenizer);
        List product = produceProduct(productTerm);
        logger.debug("returning result-product-expression: " + product);
        return product;
      }
      if ("divide".equals(token)) {
        LinkedList divideTerm = parseDivideTerms(tokenizer);
        List divisor = parseResultDefinition(tokenizer);
        List divand = produceDivide(divideTerm, divisor);
        logger.debug("returning result-divide-expression: " + divand);
        return divand;
      }

      throw new IllegalArgumentException("Only results and products supported in result expression " + token);
    }


    /**
     * @return A list of lists of result-strings.
     */
    private List<List<String>> parseResultList(StringTokenizer tokenizer) {
      logger.debug("parseResultList");
      List<List<String>> result = new ArrayList<List<String>>();
      String token = getToken(tokenizer);
      while (!")".equals(token)) {

        if (token.equals("(")) {

          // If a token starts with a bracket then it is a multi part token so
          // we need to store the entire string (until we reach the close bracket)

          // Create a temporary token to store our tokenised string
          String tempToken = "";
          List<String> tempList = new ArrayList<String>();

          // Get the next token
          token = getToken(tokenizer);

          while (!token.equals(")")) {

            // While we haven't reached the end of a bracketed token, keep
            // adding to the temporary token
            tempToken += token + " ";
            tempList.add(token);

            // Get the next token
            token = getToken(tokenizer);
          }

          // Store the completed token
          token = tempToken.trim();

          result.addAll(parseResult(token));
          //result.add(tempList);
          token = getToken(tokenizer);
        } else {
          result.addAll(parseResult(token));
          token = getToken(tokenizer);
        }
      }
      logger.debug("returning result-list: " + result);
      return result;
    }

    /**
     * @return A list of result-strings.
     */
    private List<List<String>> parseResult(String token) {
      logger.debug("parsing result " + token);
      List<List<String>> result = new ArrayList<List<String>>();

      if (token.startsWith("p") || token.startsWith("o") || token.startsWith("s")) {
        if (token.endsWith("*")) {
          token = token.substring(0, token.length() - 1);
          result.add(Collections.singletonList("test:" + token + "01"));
          result.add(Collections.singletonList("test:" + token + "02"));
          result.add(Collections.singletonList("test:" + token + "03"));
        } else {
          result.add(Collections.singletonList("test:" + token));
        }
      } else {
        result.add(Collections.singletonList(token));
      }
      logger.debug("returning result: " + result);
      return result;
    }


    /**
     * @return A list of lists of lists of result-strings.
     *
     * Specifically a list of result-expressions.
     */
    @SuppressWarnings("unchecked")
    private LinkedList parseProductTerms(StringTokenizer tokenizer) {
      logger.debug("parseProductTerms");
      LinkedList result = new LinkedList();

      String token = getToken(tokenizer);
      while (!")".equals(token)) {
        if ("(".equals(token)) {
          result.add(parseResultExpression(tokenizer));
          token = getToken(tokenizer);
        } else {
          throw new IllegalArgumentException("Product Term must be an s-expr " + token);
        }
      }

      return result;
    }

    /**
     * @param productTerms A list of lists of lists of result-strings, from parseProductTerms.
     * @return A list of lists of result-strings.
     */
    @SuppressWarnings("unchecked")
    private List produceProduct(LinkedList productTerms) {
      logger.debug("produceProduct");
      if (productTerms.size() == 1) {
        return (List)productTerms.get(0);
      } else {
        List lhs = (List)productTerms.removeFirst();
        logger.debug("productTerms : " + productTerms);
        logger.debug("car : " + lhs);
        logger.debug("cdr : " + productTerms);

        List rhs = produceProduct(productTerms);

        List result = new ArrayList();

        Iterator i = lhs.iterator();
        while (i.hasNext()) {
          List leftResult = (List)i.next();

          Iterator j = rhs.iterator();
          while (j.hasNext()) {
            List rightResult = (List)j.next();

            List combined = new ArrayList(leftResult);
            combined.addAll(rightResult);

            result.add(combined);
          }
        }

        return result;
      }
    }

    @SuppressWarnings("unchecked")
    private LinkedList parseDivideTerms(StringTokenizer tokenizer) {
      String token = getToken(tokenizer);
      if (!"(".equals(token)) {
        throw new IllegalArgumentException("Divide requires s-expr for foreach " + token);
      }
      token = getToken(tokenizer);
      if (!"foreach".equals(token)) {
        throw new IllegalArgumentException("Divide requires foreach " + token);
      }
      return parseForeachResults(tokenizer);
    }

    @SuppressWarnings("unchecked")
    private LinkedList parseForeachResults(StringTokenizer tokenizer) {
      LinkedList result = new LinkedList();

      String token = getToken(tokenizer);
      while ("(".equals(token)) {
        result.add(parseResultExpression(tokenizer));
        token = getToken(tokenizer);
      }
      if (!")".equals(token)) {
        throw new IllegalArgumentException("Failed to properly terminate foreach result-list " + token);
      }

      return result;
    }

    @SuppressWarnings("unchecked")
    private List produceDivide(List divideTerm, List divisor) {
      List result = new ArrayList();
      Iterator i = divideTerm.iterator();
      while (i.hasNext()) {
        List divideResult = (List)i.next();
        result.addAll(produceDivideResult(divideResult, divisor));
      }

      return result;
    }

    @SuppressWarnings("unchecked")
    private List produceDivideResult(List divideResult, List divisor) {
      List result = new ArrayList();

      assert divisor.size() % divideResult.size() == 0;
      int multiples = divisor.size() / divideResult.size();

      Iterator iter = divisor.iterator();
      int c = 0;
      while (iter.hasNext()) {
        List row = new ArrayList();
        row.addAll((List)(divideResult.get(c++ / multiples)));
        row.addAll((List)iter.next());

        result.add(row);
      }

      return result;
    }
  }
}
