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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mulgara.sparql.parser.cst.AndExpression;
import org.mulgara.sparql.parser.cst.BicBound;
import org.mulgara.sparql.parser.cst.BicDatatype;
import org.mulgara.sparql.parser.cst.BicIsBlank;
import org.mulgara.sparql.parser.cst.BicIsIri;
import org.mulgara.sparql.parser.cst.BicIsLiteral;
import org.mulgara.sparql.parser.cst.BicIsUri;
import org.mulgara.sparql.parser.cst.BicLang;
import org.mulgara.sparql.parser.cst.BicLangMatches;
import org.mulgara.sparql.parser.cst.BicRegEx;
import org.mulgara.sparql.parser.cst.BicSameTerm;
import org.mulgara.sparql.parser.cst.BicStr;
import org.mulgara.sparql.parser.cst.BooleanLiteral;
import org.mulgara.sparql.parser.cst.DecimalLiteral;
import org.mulgara.sparql.parser.cst.DoubleLiteral;
import org.mulgara.sparql.parser.cst.Equals;
import org.mulgara.sparql.parser.cst.Expression;
import org.mulgara.sparql.parser.cst.FunctionCall;
import org.mulgara.sparql.parser.cst.GreaterThan;
import org.mulgara.sparql.parser.cst.GreaterThanEqual;
import org.mulgara.sparql.parser.cst.IRIReference;
import org.mulgara.sparql.parser.cst.IntegerLiteral;
import org.mulgara.sparql.parser.cst.Divide;
import org.mulgara.sparql.parser.cst.LessThan;
import org.mulgara.sparql.parser.cst.LessThanEqual;
import org.mulgara.sparql.parser.cst.LogicExpression;
import org.mulgara.sparql.parser.cst.Minus;
import org.mulgara.sparql.parser.cst.Multiply;
import org.mulgara.sparql.parser.cst.Not;
import org.mulgara.sparql.parser.cst.NotEquals;
import org.mulgara.sparql.parser.cst.OrExpression;
import org.mulgara.sparql.parser.cst.Plus;
import org.mulgara.sparql.parser.cst.RDFLiteral;
import org.mulgara.sparql.parser.cst.UnaryMinus;
import org.mulgara.sparql.parser.cst.UnaryPlus;
import org.mulgara.sparql.parser.cst.Variable;
import org.mulgara.parser.MulgaraParserException;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.And;
import org.mulgara.query.filter.BoundFn;
import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.GreaterThanEqualTo;
import org.mulgara.query.filter.IsBlankFn;
import org.mulgara.query.filter.IsIriFn;
import org.mulgara.query.filter.IsLiteralFn;
import org.mulgara.query.filter.IsUriFn;
import org.mulgara.query.filter.LangMatches;
import org.mulgara.query.filter.LessThanEqualTo;
import org.mulgara.query.filter.Or;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.query.filter.RegexFn;
import org.mulgara.query.filter.SameTerm;
import org.mulgara.query.filter.arithmetic.AddOperation;
import org.mulgara.query.filter.arithmetic.DivideOperation;
import org.mulgara.query.filter.arithmetic.MinusOperation;
import org.mulgara.query.filter.arithmetic.MultiplyOperation;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.ComparableExpression;
import org.mulgara.query.filter.value.DataTypeFn;
import org.mulgara.query.filter.value.ExternalFn;
import org.mulgara.query.filter.value.IRI;
import org.mulgara.query.filter.value.LangFn;
import org.mulgara.query.filter.value.NumericExpression;
import org.mulgara.query.filter.value.NumericLiteral;
import org.mulgara.query.filter.value.SimpleLiteral;
import org.mulgara.query.filter.value.StrFn;
import org.mulgara.query.filter.value.TypedLiteral;
import org.mulgara.query.filter.value.ValueLiteral;
import org.mulgara.query.filter.value.Var;


/**
 * This object maps a constraint from an {@link org.mulgara.sparql.parser.cst.Expression} into a
 * {@link org.mulgara.query.filter.Filter}.
 *
 * @created Apr 22, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class FilterMapper {

  /** Represents the CST Filter expression that is to be mapped. */
  private Expression cstFilter;

  public FilterMapper(Expression cstFilter) {
    this.cstFilter = cstFilter;
  }

  /**
   * Get the Filter object described by the expression.
   * @return A new Filter for use in a query.
   * @throws MulgaraParserException If the mapping could not be performed due to structural errors in the CST.
   */
  public Filter getFilter() throws MulgaraParserException {
    RDFTerm term = mapExpression(cstFilter);
    if (!(term instanceof Filter)) throw new MulgaraParserException("Bad structure in the FILTER");
    return (Filter)term;
  }

  /**
   * The internal method for mapping a CST expression into an AST term.
   * @param expr The expression to be mapped.
   * @return A new AST term.
   * @throws MulgaraParserException If the mapping could not be performed due to structural errors in the CST.
   */
  static private RDFTerm mapExpression(Expression expr) throws MulgaraParserException {
    ExpressionToTerm<? extends Expression> mapper = mappers.get(expr.getClass());
    if (mapper == null) throw new UnsupportedOperationException("Unable to handle expression: \"" + expr.getImage() + "\" (" + expr.getClass().getSimpleName() + ")");
    return mapper.map(expr);
  }

  /**
   * Converts a CST NumericExpression into an AST NumericExpression.
   * @param operand The CST numeric expression.
   * @return An AST numeric expression.
   * @throws MulgaraParserException If the value was not mapped to an AST numeric expression.
   */
  static private NumericExpression mapNumber(Expression operand) throws MulgaraParserException {
    RDFTerm op = mapExpression(operand);
    if (!(op instanceof NumericExpression)) throw new MulgaraParserException("Non-numeric value in arithmetic operation: " + op.getClass().getSimpleName());
    return (NumericExpression)op;
  }

  /**
   * Converts a list of CST NumericExpressions into a list of AST NumericExpressions.
   * @param operands The list of CST numeric expressions.
   * @return A list of AST numeric expressions.
   * @throws MulgaraParserException If one of the values was not mapped to an AST numeric expression.
   */
  static private List<NumericExpression> mapNumbers(List<org.mulgara.sparql.parser.cst.NumericExpression> operands) throws MulgaraParserException {
    List<NumericExpression> numbers = new ArrayList<NumericExpression>(operands.size());
    for (org.mulgara.sparql.parser.cst.NumericExpression e: operands) numbers.add(mapNumber(e));
    return numbers;
  }

  /**
   * Converts a CST NumericExpression into an AST NumericExpression.
   * @param operand The CST numeric expression.
   * @return An AST numeric expression.
   * @throws MulgaraParserException If the value was not mapped to an AST numeric expression.
   */
  static private Filter mapLogic(Expression operand) throws MulgaraParserException {
    RDFTerm op = mapExpression(operand);
    if (!(op instanceof Filter)) throw new MulgaraParserException("Value without an Effective Boolean Value in a logic expression: " + op.getClass().getSimpleName());
    return (Filter)op;
  }

  /**
   * Converts a list of CST LogicExpressions into a list of AST Filters.
   * @param operands The list of CST logic expressions.
   * @return A list of AST filter expressions.
   * @throws MulgaraParserException If one of the values was not mapped to an AST expression with an EBV.
   */
  static private Filter[] mapLogicListArr(List<LogicExpression> operands) throws MulgaraParserException {
    List<Filter> logicOps = new ArrayList<Filter>(operands.size());
    for (LogicExpression e: operands) logicOps.add(mapLogic(e));
    return logicOps.toArray(new Filter[logicOps.size()]);
  }

  /**
   * Converts a CST ComparableExpression into an AST ComparableExpression.
   * @param operand The CST comparable expression.
   * @return An AST comparable expression.
   * @throws MulgaraParserException If the value was not mapped to an AST numeric expression.
   */
  static private ComparableExpression mapComparable(Expression operand) throws MulgaraParserException {
    RDFTerm op = mapExpression(operand);
    if (!(op instanceof ComparableExpression)) throw new MulgaraParserException("Non-comparable value in comparison operation: " + op.getClass().getSimpleName());
    return (ComparableExpression)op;
  }

  /**
   * Converts a CST Expression into an AST value that resolves to a literal ({@link ValueLiteral}).
   * @param operand The CST expression.
   * @return An AST value expression.
   * @throws MulgaraParserException If the value was not mapped to an AST ValueLiteral.
   */
  static private ValueLiteral mapValue(Expression operand) throws MulgaraParserException {
    if (operand == null) return null;
    RDFTerm op = mapExpression(operand);
    try {
      if (!op.isLiteral()) throw new MulgaraParserException("Non-literal resolution when a value is required from: " + op.getClass().getSimpleName());
    } catch (Exception qe) {  // this is a QueryException
      throw new MulgaraParserException("Unexpected error getting value type from: " + op.getClass().getSimpleName() + ". " + qe.getMessage());
    }
    return (ValueLiteral)op;
  }

  //////////////////////////////////
  // The internal mapping structures
  //////////////////////////////////

  /** Defines an interface for mapping expressions into an equivalent Filter operation. */
  private static interface ExpressionToTerm<T extends Expression> {
    /**
     * A main entry point for the mapper, which will do the casting into {@link #typedMap(Expression)}.
     * @param expr The expression to map to an RDFTerm.
     * @return A {@link RDFTerm} that is the equivalent to the given expression.
     */
    public RDFTerm map(Expression expr) throws MulgaraParserException;
    /**
     * Convert an Expression to a RDFTerm for Mulgara.
     * @param pattern The Expression to convert. Should be specific to the mapper.
     * @return The RDFTerm relevant to the mapper.
     */
    RDFTerm typedMap(T pattern) throws MulgaraParserException;
    /** Identify the class to be mapped by the extension. */
    public Class<T> getMapType();
  }

  /**
   * An abstract root for all the mappers, which provides a {@link #map(Expression)} implementation
   * that handles the casting to the generics type.
   */
  private static abstract class AbstractExprToFilter<T extends Expression> implements ExpressionToTerm<T> {
    /** Call the {@link ExpressionToTerm#typedMap(Expression)} method with an appropriate cast for the parameter. */
    @SuppressWarnings("unchecked")
    public RDFTerm map(Expression expr) throws MulgaraParserException { return typedMap((T)expr); }
  }

  /** A mapping of Expression types to constructors for the Filters they map to. */
  private static Map<Class<? extends Expression>,ExpressionToTerm<? extends Expression>> mappers = new HashMap<Class<? extends Expression>,ExpressionToTerm<? extends Expression>>();

  /**
   * A utility method to add ExpressionToFilter mappers to the {@link #mappers} map.
   * @param mapper The mapper to add.
   */
  private static void addToMap(ExpressionToTerm<? extends Expression> mapper) {
    mappers.put(mapper.getMapType(), mapper);
  }

  // initialize the mappers
  static {
    addToMap(new BooleanMap());
    addToMap(new IntegerMap());
    addToMap(new DecimalMap());
    addToMap(new DoubleMap());
    addToMap(new VariableMap());
    addToMap(new RDFLiteralMap());
    addToMap(new IRIReferenceMap());
    addToMap(new FunctionCallMap());
    addToMap(new UnaryPlusMap());
    addToMap(new UnaryMinusMap());
    addToMap(new PlusMap());
    addToMap(new MinusMap());
    addToMap(new MultiplyMap());
    addToMap(new DivideMap());
    addToMap(new EqualsMap());
    addToMap(new NotEqualsMap());
    addToMap(new GreaterThanMap());
    addToMap(new GreaterThanEqualMap());
    addToMap(new LessThanMap());
    addToMap(new LessThanEqualMap());
    addToMap(new NotMap());
    addToMap(new AndMap());
    addToMap(new OrMap());
    addToMap(new BoundFnMap());
    addToMap(new DataTypeFnMap());
    addToMap(new IsBlankFnMap());
    addToMap(new IsIriFnMap());
    addToMap(new IsLiteralFnMap());
    addToMap(new IsUriFnMap());
    addToMap(new LangFnMap());
    addToMap(new LangMatchesFnMap());
    addToMap(new RegexFnMap());
    addToMap(new SameTermMap());
    addToMap(new StrFnMap());
  }

  private static class BooleanMap extends AbstractExprToFilter<BooleanLiteral> {
    public Class<BooleanLiteral> getMapType() { return BooleanLiteral.class; }
    public RDFTerm typedMap(BooleanLiteral expr) {
      return expr == BooleanLiteral.TRUE ? Bool.TRUE : Bool.FALSE;
    }
  }

  private static class IntegerMap extends AbstractExprToFilter<IntegerLiteral> {
    public Class<IntegerLiteral> getMapType() { return IntegerLiteral.class; }
    public RDFTerm typedMap(IntegerLiteral expr) {
      return new NumericLiteral(expr.getInteger());
    }
  }

  private static class DecimalMap extends AbstractExprToFilter<DecimalLiteral> {
    public Class<DecimalLiteral> getMapType() { return DecimalLiteral.class; }
    public RDFTerm typedMap(DecimalLiteral expr) {
      return new NumericLiteral(expr.getValue());
    }
  }

  private static class DoubleMap extends AbstractExprToFilter<DoubleLiteral> {
    public Class<DoubleLiteral> getMapType() { return DoubleLiteral.class; }
    public RDFTerm typedMap(DoubleLiteral expr) {
      return new NumericLiteral(expr.getDouble());
    }
  }

  private static class VariableMap extends AbstractExprToFilter<Variable> {
    public Class<Variable> getMapType() { return Variable.class; }
    public RDFTerm typedMap(Variable expr) {
      return new Var(expr.getName());
    }
  }

  private static class RDFLiteralMap extends AbstractExprToFilter<RDFLiteral> {
    public Class<RDFLiteral> getMapType() { return RDFLiteral.class; }
    public RDFTerm typedMap(RDFLiteral expr) throws MulgaraParserException {
      try {
        if (expr.isTyped()) return TypedLiteral.newLiteral(expr.getValue(), expr.getDatatype().getUri(), null);
      } catch (QueryException qe) {
        throw new MulgaraParserException(qe.getMessage());
      }
      if (expr.isLanguageCoded()) return new SimpleLiteral(expr.getValue(), expr.getLanguage());
      return new SimpleLiteral(expr.getValue());
    }
  }

  private static class IRIReferenceMap extends AbstractExprToFilter<IRIReference> {
    public Class<IRIReference> getMapType() { return IRIReference.class; }
    public RDFTerm typedMap(IRIReference expr) {
      return new IRI(expr.getUri());
    }
  }

  private static class FunctionCallMap extends AbstractExprToFilter<FunctionCall> {
    public Class<FunctionCall> getMapType() { return FunctionCall.class; }
    public RDFTerm typedMap(FunctionCall expr) throws MulgaraParserException {
      List<Expression> exprArgs = expr.getArgs();
      RDFTerm[] operands = new RDFTerm[exprArgs.size()];
      for (int i = 0; i < operands.length; i++) operands[i] = mapExpression(exprArgs.get(i));
      IRIReference fnName = expr.getName();
      return new ExternalFn(new IRI(fnName.getUri(), fnName.getQName()), operands);
    }
  }

  private static class UnaryPlusMap extends AbstractExprToFilter<UnaryPlus> {
    public Class<UnaryPlus> getMapType() { return UnaryPlus.class; }
    public RDFTerm typedMap(UnaryPlus expr) throws MulgaraParserException {
      return mapExpression(expr.getOperand());
    }
  }

  private static class UnaryMinusMap extends AbstractExprToFilter<UnaryMinus> {
    public Class<UnaryMinus> getMapType() { return UnaryMinus.class; }
    public RDFTerm typedMap(UnaryMinus expr) throws MulgaraParserException {
      RDFTerm param = mapExpression(expr.getOperand());
      if (!(param instanceof NumericLiteral)) throw new MulgaraParserException("Cannot negate a non-number: " + param.getClass().getSimpleName());
      return new org.mulgara.query.filter.arithmetic.UnaryMinus((NumericLiteral)param);
    }
  }

  private static class PlusMap extends AbstractExprToFilter<Plus> {
    public Class<Plus> getMapType() { return Plus.class; }
    public RDFTerm typedMap(Plus expr) throws MulgaraParserException {
      return AddOperation.newAddOperation(mapNumbers(expr.getOperands()));
    }
  }

  private static class MinusMap extends AbstractExprToFilter<Minus> {
    public Class<Minus> getMapType() { return Minus.class; }
    public RDFTerm typedMap(Minus expr) throws MulgaraParserException {
      return MinusOperation.newMinusOperation(mapNumbers(expr.getOperands()));
    }
  }

  private static class MultiplyMap extends AbstractExprToFilter<Multiply> {
    public Class<Multiply> getMapType() { return Multiply.class; }
    public RDFTerm typedMap(Multiply expr) throws MulgaraParserException {
      return MultiplyOperation.newMultiplyOperation(mapNumbers(expr.getOperands()));
    }
  }

  private static class DivideMap extends AbstractExprToFilter<Divide> {
    public Class<Divide> getMapType() { return Divide.class; }
    public RDFTerm typedMap(Divide expr) throws MulgaraParserException {
      return DivideOperation.newDivideOperation(mapNumbers(expr.getOperands()));
    }
  }

  private static class EqualsMap extends AbstractExprToFilter<Equals> {
    public Class<Equals> getMapType() { return Equals.class; }
    public RDFTerm typedMap(Equals expr) throws MulgaraParserException {
      return new org.mulgara.query.filter.Equals(mapExpression(expr.getLhs()), mapExpression(expr.getRhs()));
    }
  }

  private static class NotEqualsMap extends AbstractExprToFilter<NotEquals> {
    public Class<NotEquals> getMapType() { return NotEquals.class; }
    public RDFTerm typedMap(NotEquals expr) throws MulgaraParserException {
      return new org.mulgara.query.filter.NotEquals(mapExpression(expr.getLhs()), mapExpression(expr.getRhs()));
    }
  }

  private static class GreaterThanMap extends AbstractExprToFilter<GreaterThan> {
    public Class<GreaterThan> getMapType() { return GreaterThan.class; }
    public RDFTerm typedMap(GreaterThan expr) throws MulgaraParserException {
      return new org.mulgara.query.filter.GreaterThan(mapComparable(expr.getLhs()), mapComparable(expr.getRhs()));
    }
  }

  private static class GreaterThanEqualMap extends AbstractExprToFilter<GreaterThanEqual> {
    public Class<GreaterThanEqual> getMapType() { return GreaterThanEqual.class; }
    public RDFTerm typedMap(GreaterThanEqual expr) throws MulgaraParserException {
      return new GreaterThanEqualTo(mapComparable(expr.getLhs()), mapComparable(expr.getRhs()));
    }
  }

  private static class LessThanMap extends AbstractExprToFilter<LessThan> {
    public Class<LessThan> getMapType() { return LessThan.class; }
    public RDFTerm typedMap(LessThan expr) throws MulgaraParserException {
      return new org.mulgara.query.filter.LessThan(mapComparable(expr.getLhs()), mapComparable(expr.getRhs()));
    }
  }

  private static class LessThanEqualMap extends AbstractExprToFilter<LessThanEqual> {
    public Class<LessThanEqual> getMapType() { return LessThanEqual.class; }
    public RDFTerm typedMap(LessThanEqual expr) throws MulgaraParserException {
      return new LessThanEqualTo(mapComparable(expr.getLhs()), mapComparable(expr.getRhs()));
    }
  }

  private static class NotMap extends AbstractExprToFilter<Not> {
    public Class<Not> getMapType() { return Not.class; }
    public RDFTerm typedMap(Not expr) throws MulgaraParserException {
      return new org.mulgara.query.filter.Not(mapLogic(expr.getOperand()));
    }
  }

  private static class AndMap extends AbstractExprToFilter<AndExpression> {
    public Class<AndExpression> getMapType() { return AndExpression.class; }
    public RDFTerm typedMap(AndExpression expr) throws MulgaraParserException {
      return new And(mapLogicListArr(expr.getOperands()));
    }
  }

  private static class OrMap extends AbstractExprToFilter<OrExpression> {
    public Class<OrExpression> getMapType() { return OrExpression.class; }
    public RDFTerm typedMap(OrExpression expr) throws MulgaraParserException {
      return new Or(mapLogicListArr(expr.getOperands()));
    }
  }

  private static class BoundFnMap extends AbstractExprToFilter<BicBound> {
    public Class<BicBound> getMapType() { return BicBound.class; }
    public RDFTerm typedMap(BicBound expr) throws MulgaraParserException {
      return new BoundFn((Var)mapExpression(expr.getOperand()));
    }
  }

  private static class DataTypeFnMap extends AbstractExprToFilter<BicDatatype> {
    public Class<BicDatatype> getMapType() { return BicDatatype.class; }
    public RDFTerm typedMap(BicDatatype expr) throws MulgaraParserException {
      return new DataTypeFn(mapExpression(expr.getOperand()));
    }
  }

  private static class IsBlankFnMap extends AbstractExprToFilter<BicIsBlank> {
    public Class<BicIsBlank> getMapType() { return BicIsBlank.class; }
    public RDFTerm typedMap(BicIsBlank expr) throws MulgaraParserException {
      return new IsBlankFn(mapExpression(expr.getOperand()));
    }
  }

  private static class IsIriFnMap extends AbstractExprToFilter<BicIsIri> {
    public Class<BicIsIri> getMapType() { return BicIsIri.class; }
    public RDFTerm typedMap(BicIsIri expr) throws MulgaraParserException {
      return new IsIriFn(mapExpression(expr.getOperand()));
    }
  }

  private static class IsLiteralFnMap extends AbstractExprToFilter<BicIsLiteral> {
    public Class<BicIsLiteral> getMapType() { return BicIsLiteral.class; }
    public RDFTerm typedMap(BicIsLiteral expr) throws MulgaraParserException {
      return new IsLiteralFn(mapExpression(expr.getOperand()));
    }
  }

  private static class IsUriFnMap extends AbstractExprToFilter<BicIsUri> {
    public Class<BicIsUri> getMapType() { return BicIsUri.class; }
    public RDFTerm typedMap(BicIsUri expr) throws MulgaraParserException {
      return new IsUriFn(mapExpression(expr.getOperand()));
    }
  }

  private static class LangFnMap extends AbstractExprToFilter<BicLang> {
    public Class<BicLang> getMapType() { return BicLang.class; }
    public RDFTerm typedMap(BicLang expr) throws MulgaraParserException {
      return new LangFn(mapExpression(expr.getOperand()));
    }
  }

  private static class LangMatchesFnMap extends AbstractExprToFilter<BicLangMatches> {
    public Class<BicLangMatches> getMapType() { return BicLangMatches.class; }
    public RDFTerm typedMap(BicLangMatches expr) throws MulgaraParserException {
      return new LangMatches(mapValue(expr.getFirstOperand()), mapValue(expr.getSecondOperand()));
    }
  }

  private static class RegexFnMap extends AbstractExprToFilter<BicRegEx> {
    public Class<BicRegEx> getMapType() { return BicRegEx.class; }
    public RDFTerm typedMap(BicRegEx expr) throws MulgaraParserException {
      return new RegexFn(mapValue(expr.getExpr()), mapValue(expr.getPattern()), mapValue(expr.getFlags()));
    }
  }

  private static class SameTermMap extends AbstractExprToFilter<BicSameTerm> {
    public Class<BicSameTerm> getMapType() { return BicSameTerm.class; }
    public RDFTerm typedMap(BicSameTerm expr) throws MulgaraParserException {
      return new SameTerm(mapExpression(expr.getFirstOperand()), mapExpression(expr.getSecondOperand()));
    }
  }

  private static class StrFnMap extends AbstractExprToFilter<BicStr> {
    public Class<BicStr> getMapType() { return BicStr.class; }
    public RDFTerm typedMap(BicStr expr) throws MulgaraParserException {
      return new StrFn(mapExpression(expr.getOperand()));
    }
  }

}
