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
 * Contributor(s):
 *   DefinablePrefixAnnotation contributed by Netymon Pty Ltd on behalf of
 *   The Australian Commonwealth Government under contract 4500507038.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.tuples;

// Java 2 standard packages
import java.util.*;

// Log4j
import org.apache.log4j.*;

// Local packages
import org.mulgara.query.*;
import org.mulgara.query.filter.Filter;
import org.mulgara.query.filter.Inverse;
import org.mulgara.query.filter.RDFTerm;
import org.mulgara.resolver.spi.*;

/**
 * TQL answer. An answer is a set of solutions, where a solution is a mapping of
 * {@link Variable}s to {@link Value}s.
 *
 * @created 2003-01-30
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class TuplesOperations {

  /** Logger. This is named after the class. */
  private final static Logger logger = Logger.getLogger(TuplesOperations.class.getName());

  /** The factory used to generate new {@link Tuples} instances. */
  private static TuplesFactory tuplesFactory = TuplesFactory.newInstance();

  /**
   * Create a proposition which is always false. This is the additive identity
   * of the relational algebra: appending the empty value to a tuples leaves it
   * unchanged. By duality, it's also the multiplicative zero: joining the empty
   * value to a tuples generates an empty result.
   *
   * @return the expression which is never satisfied, no matter what value any
   *         variable takes
   */
  public static StoreTuples empty() {
    return EmptyTuples.getInstance();
  }

  /**
   * Create a proposition which is always true.
   *
   * This is the multiplicative
   * identity of the relational algebra: joining the unconstrained value to a
   * tuples leaves it unchanged. By duality, it's also the additive zero:
   * appending the unconstrained value to a tuples generates an unconstrained
   * result.
   *
   * @return the expression which is always true, for any value of any variables
   */
  public static StoreTuples unconstrained() {
    return UnconstrainedTuples.getInstance();
  }

  /**
   * Assign a value to a variable, representing the binding as a tuples with one
   * row and one column.
   *
   * @param variable The variable to bind
   * @param value The value in local space to bind the variable to
   * @return A Tuples with the variable bound to the given value.
   */
  public static Tuples assign(Variable variable, long value) {
    return (value == Tuples.UNBOUND) ? (Tuples)unconstrained()
                                     : new Assignment(variable, value);
  }

  /**
   * This is approximately a disjunction.
   *
   * @param lhs The first Tuples to be used in the result.
   * @param rhs The second Tuples to be used in the result.
   * @return A new Tuples containing all of the bindings from the lhs and rhs parameters.
   * @throws TuplesException if the append fails
   */
  public static Tuples append(Tuples lhs, Tuples rhs) throws TuplesException {
    return append(Arrays.asList(new Tuples[] { lhs, rhs }));
  }


  /**
   * Creates a new Tuples which contains all of the bindings of the Tuples in the argument list.
   * If any tuples contains variables not found in the other Tuples, then those values will
   * remain unbound for the bindings of those Tuples missing those variables.
   * @param args A list of the Tuples to be used in the result.
   * @return A Tuples containing all of the bindings from the args list.
   * @throws TuplesException If the data could not be appended.
   */
  public static Tuples append(List<? extends Tuples> args) throws TuplesException {
    if (logger.isDebugEnabled()) logger.debug("Appending " + args);

    HashSet<Variable> variableSet = new HashSet<Variable>();
    List<Variable> variables = new ArrayList<Variable>();
    boolean unionCompat = true;
    Variable[] leftVars = null;
    List<Tuples> operands = new ArrayList<Tuples>();
    Iterator<? extends Tuples> i = args.iterator();
    while (i.hasNext()) {
      Tuples operand = i.next();
      if (operand.isUnconstrained()) {
        closeOperands(operands);
        if (logger.isDebugEnabled()) logger.debug("Returning unconstrained from append.");
        return unconstrained();
      } else if (operand.isEmpty()) {
        if (logger.isDebugEnabled()) logger.debug("Ignoring empty append operand " + operand);
        continue;
      }

      operands.add((Tuples)operand.clone());

      Variable[] vars = operand.getVariables();
      if (leftVars == null) leftVars = vars;
      else unionCompat = unionCompat && Arrays.equals(leftVars, vars);
      for (int j = 0; j < vars.length; j++) {
        if (!variableSet.contains(vars[j])) {
          variableSet.add(vars[j]);
          variables.add(vars[j]);
        }
      }
    }

    if (logger.isDebugEnabled()) logger.debug("Operands after append-unification: " + operands);

    if (operands.isEmpty()) {
      if (logger.isDebugEnabled()) logger.debug("Returning empty from append.");
      return empty();
    }

    if (operands.size() == 1) {
      if (logger.isDebugEnabled()) logger.debug("Returning singleton from append.");
      return operands.get(0);
    }

    if (unionCompat) {
      if (logger.isDebugEnabled()) {
        logger.debug("Columns are union-compatible");
        logger.debug("Returning OrderedAppend from Union compatible append.");
      }
      Tuples result = new OrderedAppend(operands.toArray(new Tuples[operands.size()]));
      closeOperands(operands);
      return result;
    } else {
      List<Tuples> projected = new ArrayList<Tuples>();
      for (Tuples operand: operands) {
        Tuples proj = project(operand, variables, true);
        Tuples sorted = sort(proj);
        projected.add(sorted);
        proj.close();
        operand.close();
      }

      if (logger.isDebugEnabled()) logger.debug("Returning OrderedAppend from Non-Union compatible append.");
      Tuples result = new OrderedAppend(projected.toArray(new Tuples[projected.size()]));
      closeOperands(projected);
      return result;
    }
  }


  /**
   * Creates a new Tuples which contains all of the bindings of the Tuples in the argument list.
   * If any tuples contains variables not found in the other Tuples, then those values will
   * remain unbound for the bindings of those Tuples missing those variables.
   * @param args A list of the Tuples to be used in the result.
   * @return A Tuples containing all of the bindings from the args list.
   * @throws TuplesException If the data could not be appended.
   */
  public static Tuples unorderedAppend(List<? extends Tuples> args) throws TuplesException {
    if (logger.isDebugEnabled()) logger.debug("Appending " + args);

    HashSet<Variable> variableSet = new HashSet<Variable>();
    List<Variable> variables = new ArrayList<Variable>();
    boolean unionCompat = true;
    Variable[] leftVars = null;
    List<Tuples> operands = new ArrayList<Tuples>();
    Iterator<? extends Tuples> i = args.iterator();
    while (i.hasNext()) {
      Tuples operand = i.next();
      if (operand.isUnconstrained()) {
        closeOperands(operands);
        if (logger.isDebugEnabled()) logger.debug("Returning unconstrained from append.");
        return unconstrained();
      } else if (operand.isEmpty()) {
        if (logger.isDebugEnabled()) logger.debug("Ignoring append operand " + operand + " with rowcount = " + operand.getRowCount());
        continue;
      }

      operands.add((Tuples)operand.clone());

      Variable[] vars = operand.getVariables();
      if (leftVars == null) leftVars = vars;
      else unionCompat = unionCompat && Arrays.equals(leftVars, vars);
      for (int j = 0; j < vars.length; j++) {
        if (!variableSet.contains(vars[j])) {
          variableSet.add(vars[j]);
          variables.add(vars[j]);
        }
      }
    }

    if (logger.isDebugEnabled()) logger.debug("Operands after unordered-append-unification: " + operands);

    if (operands.isEmpty()) {
      if (logger.isDebugEnabled()) logger.debug("Returning empty from unorderedAppend.");
      return empty();
    }

    if (operands.size() == 1) {
      if (logger.isDebugEnabled()) logger.debug("Returning singleton from unorderedAppend.");
      return operands.get(0);
    }

    if (unionCompat) {
      if (logger.isDebugEnabled()) {
        logger.debug("Columns are union-compatible");
        logger.debug("Returning OrderedAppend from Union compatible append.");
      }
      Tuples result = new UnorderedAppend(operands.toArray(new Tuples[operands.size()]));
      closeOperands(operands);
      return result;
    } else {
      List<Tuples> projected = new ArrayList<Tuples>();
      for (Tuples operand: operands) {
        Tuples proj = project(operand, variables, false);
        projected.add(proj);
        operand.close();
      }

      if (logger.isDebugEnabled()) logger.debug("Returning OrderedAppend from Non-Union compatible unorderedAppend.");
      Tuples result = new UnorderedAppend(projected.toArray(new Tuples[projected.size()]));
      closeOperands(projected);
      return result;
    }
  }


  /**
   * Creates a new Tuples which contains all of the bindings of the Tuples in the argument list.
   * All tuples must have an identical pattern, and come directly from the store.
   * @param args A list of the Tuples directly backed by the store to be used in the result.
   * @return A StoreTuples containing all of the bindings from the args list.
   * @throws TuplesException If the data could not be appended.
   */
  public static StoreTuples appendCompatible(List<StoreTuples> args) throws TuplesException {
    if (logger.isDebugEnabled()) logger.debug("Compatible append of " + args);

    Variable[] vars = null;
    List<StoreTuples> operands = new ArrayList<StoreTuples>();
    for (StoreTuples arg: args) {

      // test for empty or unconstrained data
      if (arg.isUnconstrained()) {
        closeOperands(operands);
        if (logger.isDebugEnabled()) logger.debug("Returning unconstrained from append.");
        return unconstrained();
      } else if (arg.isEmpty()) {
        if (logger.isDebugEnabled()) logger.debug("Ignoring empty append operand " + arg);
        continue;
      }

      operands.add((StoreTuples)arg.clone());

      // test for tuples compatibility
      if (vars == null) vars = arg.getVariables();
      else if (!Arrays.equals(vars, arg.getVariables())) {
        throw new IllegalArgumentException("Incompatible arguments to appendCompatible");
      }

    }

    if (logger.isDebugEnabled()) logger.debug("Operands after compatible-append-unification: " + operands);

    if (operands.isEmpty()) {
      if (logger.isDebugEnabled()) logger.debug("Returning empty from append.");
      return empty();
    }

    if (operands.size() == 1) {
      if (logger.isDebugEnabled()) logger.debug("Returning singleton from append.");
      return operands.get(0);
    }

    StoreTuples result = new OrderedStoreAppend(operands.toArray(new StoreTuples[operands.size()]));
    closeOperands(operands);
    return result;
  }


  /**
   * Convenience method for doing a binary {@link #join(List)}.
   * @param lhs The first argument to be joined.
   * @param rhs The second argument to be joined.
   * @return A Tuples containing the conjunction of lhs and rhs.
   */
  public static Tuples join(Tuples lhs, Tuples rhs) throws TuplesException {
    return join(Arrays.asList(new Tuples[] { lhs, rhs }));
  }

  /**
   * This is approximately a conjunction. Returns a set of bindings containing all the variables
   * from both parameters. The only bindings returned are those where all the matching variables
   * in each argument are bound to the same values.
   * @param args The Tuples to be joined together.
   * @return A Tuples containing the conjunction of all the arguments.
   */
  public static Tuples join(List<? extends Tuples> args) throws TuplesException {
    try {
      if (logger.isDebugEnabled()) logger.debug(printArgs("Flattening args:", args));
      List<Tuples> operands = flattenOperands(args);

      if (logger.isDebugEnabled()) logger.debug(printArgs("Unifying args: ", operands));
      List<Tuples> unified = unifyOperands(operands);

      if (logger.isDebugEnabled()) logger.debug(printArgs("Sorting args:", unified));
      List<Tuples> sorted = sortOperands(unified);

      if (logger.isDebugEnabled()) logger.debug(printArgs("Preparing result: ", sorted));
      switch (sorted.size()) {
        case 0:
          if (logger.isDebugEnabled()) logger.debug("Short-circuit empty");
          return empty();

        case 1:
          if (logger.isDebugEnabled()) logger.debug("Short-circuit singleton");
          return sorted.get(0);

        default:
          if (logger.isDebugEnabled()) logger.debug("return UnboundJoin");
          Tuples result = new UnboundJoin(sorted.toArray(new Tuples[sorted.size()]));
          closeOperands(sorted);
          return result;
      }
    } catch (RuntimeException re) {
      logger.warn("RuntimeException thrown in join", re);
      throw re;
    } catch (TuplesException te) {
      logger.warn("TuplesException thrown in join", te);
      throw te;
    }
  }


  /**
   * This is approximately a subtraction.  The subtrahend is matched against the minuend in the same
   * way as a conjunction, and the matching lines removed from the minuend.  The remaining lines in
   * the minuend are the result. Parameters are not closed during this operation.
   * @param minuend The tuples to subtract from.
   * @param subtrahend The tuples to match against the minuend for removal.
   * @return The contents from the minuend, excluding those rows which match against the subtrahend.
   * @throws TuplesException If there are no matching variables between the minuend and subtrahend.
   */
  public static Tuples subtract(Tuples minuend, Tuples subtrahend) throws TuplesException {
    try {
      if (logger.isDebugEnabled()) logger.debug("subtracting " + subtrahend + " from " + minuend);
      // get the matching columns
      Set<Variable> matchingVars = getMatchingVars(minuend, subtrahend);
      if (matchingVars.isEmpty()) {
        // check to see if the subtrahend is empty
        if (subtrahend.getVariables().length == 0 || minuend.getVariables().length == 0) {
          return (Tuples)minuend.clone();
        }
        throw new TuplesException("Unable to subtract: no common variables.");
      }
      // double check that the variables are not equal
      if (subtrahend.isEmpty() || minuend.isEmpty()) {
        logger.debug("Found an empty Tuples with bound variables");
        return (Tuples)minuend.clone();
      }
      // reorder the subtrahend as necessary
      Tuples sortedSubtrahend;
      // check if there are variables which should not be considered when sorting
      if (checkForExtraVariables(subtrahend, matchingVars)) {
        // yes, there are extra variables
        logger.debug("removing extra variables not needed in subtraction");
        // project out the extra variables (sorting happens in projection)
        sortedSubtrahend = project(subtrahend, new ArrayList<Variable>(matchingVars), true);
      } else {
        // there were no extra variables in the subtrahend
        logger.debug("All variables needed");
        // check if the data is already sorted
        sortedSubtrahend = (null == subtrahend.getComparator()) ? sort(subtrahend) : subtrahend;
      }
      // return the difference
      try {
        return new Difference(minuend, sortedSubtrahend);
      } finally {
        if (sortedSubtrahend != subtrahend) sortedSubtrahend.close();
      }

    } catch (RuntimeException re) {
      logger.warn("RuntimeException thrown in subtraction", re);
      throw re;
    } catch (TuplesException te) {
      logger.warn("TuplesException thrown in subtraction", te);
      throw te;
    }
  }


  /**
   * Does a left-outer-join between two tuples. Parameters are not closed during this
   * operation.
   * @param standard The standard pattern that appears in all results.
   * @param optional The optional pattern that may or may not be bound in each result
   * @param context The query evaluation context to evaluate any nested fitlers in.
   * @return A Tuples containing variables from both parameters. All variables from
   *   <em>standard</em> will be bound at all times. Variables from <em>optional</em>
   *   may or may not be bound.
   */
  public static Tuples optionalJoin(Tuples standard, Tuples optional, Filter filter, QueryEvaluationContext context) throws TuplesException {
    try {

      if (logger.isDebugEnabled()) logger.debug("optional join of " + standard + " optional { " + optional + " }");

      // get the matching columns
      Set<Variable> matchingVars = getMatchingVars(standard, optional);
      // check for empty parameters
      if (logger.isDebugEnabled() && standard.getRowCardinality() == Cursor.ZERO) logger.debug("Nothing to the left of an optional");

      // Checks if there is nothing on the RHS of the optional join
      if (optional.isEmpty() || optional.getRowCardinality() == 0) {
        // need to return standard, projected out to the extra variables
        if (optional.getNumberOfVariables() == 0) {
          // This may be empty due to having zero rows (since the columns are truncated in this case)
          return (Tuples)standard.clone();
        } else {
          return project(standard, optional.getVariables());
        }
      }

      // If the Optional clause does not have matching variables with the LHS
      // then this is the equivalent to a normal join as a cartesian product
      if (matchingVars.isEmpty()) {

        // get the cartesian product
        Tuples filteredProduct = filter(join(standard, optional), filter, context);
        Tuples invertedStd;
        if (intersects(optional.getVariables(), filter.getVariables())) {
          invertedStd = new LeftFiltered(standard, optional, filter, context);
        } else {
          // this is correct, since 'optional' is independent of the filter
          invertedStd = filter(standard, new Inverse(filter), context);
        }
        return append(filteredProduct, invertedStd);
      }

      // check if there are variables which should not be considered when sorting
      if (!checkForExtraVariables(optional, matchingVars)) {
        // there were no extra variables in the optional
        logger.debug("All variables needed");
        // if all variables match, then the result is the same as the LHS
        return (Tuples)standard.clone();
      }

      // yes, there are extra variables
      if (logger.isDebugEnabled()) logger.debug("sorting on the common variables: " + matchingVars);
      // re-sort the optional according to the matching variables
      // reorder the optional as necessary
      Tuples sortedOptional = reSort(optional, new ArrayList<Variable>(matchingVars));

      // return the difference
      try {
        return new LeftJoin(standard, sortedOptional, filter, context);
      } finally {
        sortedOptional.close();
      }

    } catch (RuntimeException re) {
      logger.warn("RuntimeException thrown in optional", re);
      throw re;
    } catch (TuplesException te) {
      logger.warn("TuplesException thrown in optional", te);
      throw te;
    }
  }


  /**
   * Convenience method to see if an array and a collection share any elements in common.
   * @param <T> The type of elements in both containers.
   * @param lhs An array of elements.
   * @param rhs A collection of elements.
   * @return <code>true</code> iff there is 1 or more elements present in both containers. 
   */
  private static final <T> boolean intersects(T[] lhs, Collection<T> rhs) {
    for (T elt: lhs) if (rhs.contains(elt)) return true;
    return false;
  }

  /**
   * Flattens any nested joins to allow polyadic join operations.
   * @param operands A list of Tuples which may in turn be nested operations.
   * @return A flattened list of flattened Tuples. 
   */
  private static List<Tuples> flattenOperands(List<? extends Tuples> operands) throws TuplesException {
    List<Tuples> result = new ArrayList<Tuples>();
    for (Tuples operand: operands) result.addAll(flattenOperand(operand));
    return result;
  }


  /**
   * Flattens a Tuples into a list of Tuples. This means that joins will be expanded into their components.
   * @param operand The Tuples to flatten
   * @return A flattened list.
   * @throws TuplesException If the Tuples could not be accessed.
   */
  private static List<Tuples> flattenOperand(Tuples operand) throws TuplesException {
    List<Tuples> operands = new ArrayList<Tuples>();
    if (operand instanceof UnboundJoin) {
      for (Tuples op: operand.getOperands()) operands.add((Tuples)op.clone());
    } else {
      operands.add((Tuples)operand.clone());
    }
    return operands;
  }


  /**
   * Unifies bound variables in operands.
   * Prepends a LiteralTuples containing constrained variable bindings.
   * If any operand returns 0-rows returns EmptyTuples.
   * @param operands List of Tuples to unify.  Consumed by this function.
   * @return List of operands remaining after full unification.
   */
  private static List<Tuples> unifyOperands(List<Tuples> operands) throws TuplesException {
    Map<Variable,Long> bindings = new HashMap<Variable,Long>();

    if (!bindSingleRowOperands(bindings, operands)) {
      closeOperands(operands);
      logger.debug("Returning empty due to shortcircuiting initial bindSingleRowOperands");
      return new ArrayList<Tuples>(Collections.singletonList(empty()));
    }

    List<Tuples> result = extractNonReresolvableTuples(operands);
    // operands is now effectively a List<ReresolvableResolution>

    List<ReresolvableResolution> reresolved;
    do {
      reresolved = resolveNewlyBoundFreeNames(operands, bindings);
      if (!bindSingleRowOperands(bindings, reresolved)) {
        closeOperands(operands);
        closeOperands(result);
        closeOperands(reresolved);
        logger.debug("Returning empty due to shortcircuiting progressive bindSingleRowOperands");
        // wrap in an Array list to convert the generic type
        return new ArrayList<Tuples>(Collections.singletonList(empty()));
      }
      operands.addAll(reresolved);
    } while (reresolved.size() != 0);

    result.addAll(operands);
    result.add(createTuplesFromBindings(bindings));

    return result;
  }


  /**
   * Extracts all bound names from workingSet into bindings.
   */
  private static boolean bindSingleRowOperands(Map<Variable,Long> bindings, List<? extends Tuples> workingSet) throws TuplesException {
    Iterator<? extends Tuples> iter = workingSet.iterator();
    while (iter.hasNext()) {
      Tuples tuples = iter.next();
      
      if (tuples.isEmpty()) return false;

      switch ((int)tuples.getRowCardinality()) {
        case Cursor.ZERO:
          return false;

        case Cursor.ONE:
          Variable[] vars = tuples.getVariables();
          tuples.beforeFirst();
          if (tuples.next()) {
            for (int i = 0; i < vars.length; i++) {
              Long value = new Long(tuples.getColumnValue(tuples.getColumnIndex(vars[i])));
              Long oldValue = (Long)bindings.put(vars[i], value);
              if (oldValue != null && !value.equals(oldValue)) return false;
            }
          } else {
            // This should not happen.
            // If the call to getRowCardinality returns > 0 then beforeFirst,
            // and then next should return true too.
            logger.error("No rows but getRowCardinality returned Cursor.ONE: (class=" +
                    tuples.getClass().getName() + ") " + tuples.toString());
            throw new AssertionError("No rows but getRowCardinality returned Cursor.ONE");
          }
          iter.remove();
          tuples.close();
          break;

        case Cursor.MANY:
          continue;

        default:
          throw new TuplesException("getRowCardinality() returned other than ZERO, ONE, or MANY");
      }
    }

    return true;
  }


  private static List<Tuples> extractNonReresolvableTuples(List<Tuples> workingSet) throws TuplesException {
    List<Tuples> nonReresolvable = new ArrayList<Tuples>(workingSet.size());

    Iterator<Tuples> iter = workingSet.iterator();
    while (iter.hasNext()) {
      Tuples operand = iter.next();
      if (!(operand instanceof ReresolvableResolution)) {
        nonReresolvable.add(operand);
        iter.remove();
      }
    }

    return nonReresolvable;
  }


  /**
   * Compares the free names in the working-set against the current bindings
   * and resolves any constraints found with bindings.
   * @param workingSet A set of ReresolvableResolution, though it will be represented as a set of Tuples
   * @return List of ConstrainedTuples resulting from any resolutions required.
   */
  private static List<ReresolvableResolution> resolveNewlyBoundFreeNames(List<Tuples> workingSet, Map<Variable,Long> bindings) throws TuplesException {
    List<ReresolvableResolution> reresolved = new ArrayList<ReresolvableResolution>();
    Iterator<Tuples> iter = workingSet.iterator();
    while (iter.hasNext()) {
      ReresolvableResolution tuples = (ReresolvableResolution)iter.next();
      ReresolvableResolution updated = tuples.reresolve(bindings);
      if (updated != null) {
        reresolved.add(updated);
        tuples.close();
        iter.remove();
      }
    }

    return reresolved;
  }


  private static Tuples createTuplesFromBindings(Map<Variable,Long> bindings) throws TuplesException {
    if (bindings.isEmpty()) return unconstrained();

    Set<Variable> keys = bindings.keySet();
    Variable[] vars = keys.toArray(new Variable[keys.size()]);

    long[] values = new long[vars.length];
    for (int i = 0; i < values.length; i++) values[i] = bindings.get(vars[i]);

    LiteralTuples tuples = new LiteralTuples(vars);
    tuples.appendTuple(values);

    return tuples;
  }


  /**
   * Calls close on all tuples in operands list.
   */
  private static void closeOperands(List<? extends Tuples> operands) throws TuplesException {
    for (Tuples op: operands) op.close();
  }


  /**
   * Sorts operands by weighted row count in-place.
   * Each row count is discounted by the number of free-names bound to its left.
   * Weighted-row-count = row-count ^ (free-after-binding / free-before-binding)
   */
  private static List<Tuples> sortOperands(List<Tuples> operands) throws TuplesException {
    Set<Variable> boundVars = new HashSet<Variable>();
    List<Tuples> result = new ArrayList<Tuples>();

    while (!operands.isEmpty()) {
      Tuples bestTuples = removeBestTuples(operands, boundVars);

      DefinablePrefixAnnotation definable =
          (DefinablePrefixAnnotation)bestTuples.getAnnotation(DefinablePrefixAnnotation.class);
      if (definable != null) definable.definePrefix(boundVars);

      // Add all variables that don't contain UNBOUND to boundVars set.
      // Note: the inefficiency this introduces for distributed results
      // can only be eliminated by propagating isColumnEverUnbound through Answer.
      // Note: this is required to ensure that a subsequent operand will not
      // rely on this variable when selecting an index as if it is UNBOUND in a
      // left-operand it becomes unprefixed.
      Variable[] vars = bestTuples.getVariables();
      for (int i = 0; i < vars.length; i++) {
        if (!bestTuples.isColumnEverUnbound(i)) boundVars.add(vars[i]);
      }

      result.add(bestTuples);
    }

    return result;
  }


  // FIXME: Method too long.  Refactor.
  private static Tuples removeBestTuples(List<Tuples> operands, Set<Variable> boundVars) throws TuplesException {
    ListIterator<Tuples> iter = operands.listIterator();
    Tuples minTuples = null;
    double minRowCount = Double.MAX_VALUE;
    int minIndex = -1;

    assert(iter.hasNext());

    logger.debug("removeBestTuples");
    while (iter.hasNext()) {
      Tuples tuples = (Tuples)iter.next();
      if (logger.isDebugEnabled()) logger.debug("tuples: " + tuplesSummary(tuples));

      // Check tuples meets any mandatory left bindings.
      MandatoryBindingAnnotation bindingRequirements =
          (MandatoryBindingAnnotation)tuples.getAnnotation(MandatoryBindingAnnotation.class);

      if (bindingRequirements != null && !bindingRequirements.meetsRequirement(boundVars)) continue;

      Variable[] vars = tuples.getVariables();
      int numLeftBindings = calculateNumberOfLeftBindings(tuples, boundVars);
      if (logger.isDebugEnabled()) logger.debug("numLeftBindings: " + numLeftBindings);

      // Basic formula assumes uniform distribution.  So number of rows is the
      // product of the length of each variable taken seperately, hence expected
      // row count for n from m bindings is expected(0 from m)**((m - n) / m).
      // This fails to consider the effect on performance of worst case so we
      // incorporate weighted terms to allow for possible skew on each column.
      // We assume a reducing probability of compounded failure so weight each
      // term by 100**term (0-indexed), this is a fudge factor that needs proper
      // analysis.
      double weightedRowCount = 0.0;
      for (int weight = 0; weight < numLeftBindings + 1; weight++) {
        double term = vars.length > 0
                        ? Math.pow(tuples.getRowExpectedCount(), (double)(vars.length - (numLeftBindings - weight)) / vars.length)
                        : tuples.getRowExpectedCount();
        weightedRowCount += term / Math.pow(100.0, weight);
      }

      if (logger.isDebugEnabled()) {
        logger.debug("weightedRowCount: " + weightedRowCount);
        logger.debug("minRowCount: " + minRowCount);
      }

      if (weightedRowCount < minRowCount) {
        minRowCount = weightedRowCount;
        minTuples = tuples;
        minIndex = iter.nextIndex() - 1;
      }
    }

    if (minTuples == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Unable to meet ordering constraints with bindings: " + boundVars);
        for (Tuples op: operands) logger.debug("    Operand: " + tuplesSummary(op));
      }
      throw new TuplesException("Unable to meet ordering constraints");
    }

    if (logger.isDebugEnabled()) logger.debug("Selected: " + tuplesSummary(minTuples) + " with weightedRowCount: " + minRowCount);
    operands.remove(minIndex);
    return minTuples;
  }


  private static int calculateNumberOfLeftBindings(Tuples tuples, Set<Variable> boundVars) throws TuplesException {
    int numLeftBindings = 0;
    Variable[] vars = tuples.getVariables();
    // If the tuples supports defining a prefix then
    if (tuples.getAnnotation(DefinablePrefixAnnotation.class) != null) {
      for (int i = 0; i < vars.length; i++) {
        if (boundVars.contains(vars[i])) numLeftBindings++;
      }
    } else {
      for (int i = 0; i < vars.length; i++) {
        if (boundVars.contains(vars[i])) numLeftBindings++;
        else break;
      }
    }

    return numLeftBindings;
  }


  /**
   * Relational projection. This eliminates any columns not in the specified
   * list, and eliminates any duplicate rows that result.
   *
   * @param tuples The original tuples to project
   * @param variableList the list of {@link Variable}s to project on
   * @param distinct indicates that duplicate rows should be removed
   * @return The tuples, with only the required columns, and possibly with duplicates removed
   * @throws TuplesException if the projection operation fails
   */
  public static Tuples project(Tuples tuples, List<Variable> variableList, boolean distinct) throws TuplesException {
    try {

      boolean noVariables = (variableList == null) || (variableList.size() == 0);
      if (tuples.isUnconstrained() || (noVariables && tuples.getRowCardinality() != Cursor.ZERO)) {
        return unconstrained();
      } else if (tuples.isEmpty()) {
        return empty();
      }

      if (logger.isDebugEnabled()) logger.debug("Projecting to " + variableList);

      // Perform the actual projection
      Tuples originalTuples = tuples;
      tuples = new UnorderedProjection(tuples, variableList);
      assert tuples != originalTuples;

      // Test whether creating an unordered projects has removed variables.
      if (tuples.isUnconstrained()) {
        tuples.close();
        return TuplesOperations.unconstrained();
      }

      // Eliminate any duplicates
      if (distinct) {
        Tuples oldTuples = tuples;
        tuples = removeDuplicates(tuples);
        assert tuples != oldTuples;

        if (tuples == oldTuples) {
          logger.warn("removeDuplicates does not change the underlying tuples");
        } else {
          oldTuples.close();
        }
        assert tuples.hasNoDuplicates();
      }

      return tuples;
    } catch (TuplesException e) {
      throw new TuplesException("Couldn't perform projection", e);
    }
  }


  /**
   * Project a tuples out to extra columns that will always be unbound.
   * @param tuples The original tuples to expand.
   * @param expansionVars The new set of variables to expand to.
   *        These may intersect the existing variables, but this is unexpected.
   * @return A Tuples with the original bindings, plus any specified new columns that will be unbound.
   */
  public static Tuples project(Tuples tuples, Variable[] expansionVars) {
    if (tuples == null) throw new IllegalArgumentException("Projection on Null \"tuples\"");
    if (expansionVars == null) throw new IllegalArgumentException("Projection with Null expansion variables");

    // test if no expansion, and short circuit if there isn't one
    if (expansionVars.length == 0) return (Tuples)tuples.clone();

    // test for overlapping variables
    Variable[] opVars = tuples.getVariables();
    List<Variable> newVars = new ArrayList<Variable>();
    for (Variable v: expansionVars) newVars.add(v);
    for (Variable v: opVars) newVars.remove(v);

    // test again for no expansion, and short circuit if there isn't one
    if (newVars.isEmpty()) return (Tuples)tuples.clone();

    return new ExpandedProjection(tuples, newVars);
  }


  /**
   * Creates a new restriction tuples, based on a normal Tuples and a restriction predicate.
   * @param tuples The tuples to restrict.
   * @param pred The predicate describing the restriction.
   * @return A new Tuples whose bindings only match the restriction.
   * @throws TuplesException If the Tuples could not be accessed.
   */
  public static Tuples restrict(Tuples tuples, RestrictPredicate pred) throws TuplesException {
    return new RestrictionTuples(tuples, pred);
  }


  /**
   * Filter a Tuples according to a {@link org.mulgara.query.filter.Filter} test.
   * @param tuples The Tuples to be filtered.
   * @param filter The Filter to apply to the tuples.
   * @param context The context in which the Filter is to be resolved. This can go beyond
   *        what has already been determined for the tuples parameter.
   * @return A new Tuples which is a subset of the provided Tuples.
   * @throws IllegalArgumentException If tuples is <code>null</code>
   */
  public static Tuples filter(Tuples tuples, Filter filter, QueryEvaluationContext context) {
    // The incoming context needs to be updated for the tuples, so that clones are not inadvertantly used
    return new FilteredTuples(tuples, filter, context);
  }


  /**
   * Assign a variable to an expression, with variables coming from a provided tuples.
   * @param tuples The Tuples to provide the variable context.
   * @param var The variable to be bound.
   * @param expr The expression to bind the variable to.
   * @param context The context in which the expression is to be resolved. This can go beyond
   *        what has already been determined for the tuples parameter.
   * @return A new Tuples which expands the provided Tuples to include the new variable.
   * @throws IllegalArgumentException If tuples is <code>null</code>
   */
  public static Tuples assign(Tuples tuples, Variable var, RDFTerm expr, QueryEvaluationContext context) {
    return new LetTuples(tuples, var, expr, context);
  }


  /**
   * Sort into default order, based on the columns and local node numbers.
   * @param tuples the tuples to sort
   * @return A new Tuples with the bindings sorted.
   * @throws TuplesException if the sorting can't be accomplished
   */
  public static Tuples sort(Tuples tuples) throws TuplesException {
    if (tuples.getComparator() == null) {
      if (tuples.isUnconstrained()) {
        return TuplesOperations.unconstrained();
      } else if (tuples.isEmpty()) {
        tuples = empty();
      } else {
        if (logger.isDebugEnabled()) logger.debug("Sorting " + tuples.getRowCount() + " rows");

        tuples = tuplesFactory.newTuples(tuples);
        assert tuples.getComparator() != null;
      }

      if (logger.isDebugEnabled()) logger.debug("Sorted " + tuples.getRowCount() + " rows");

      return tuples;
    } else {
      return (Tuples) tuples.clone();
    }
  }

  /**
   * Sort into a specified order.
   *
   * @param tuples the tuples to sort
   * @param rowComparator the ordering
   * @return A Tuples with bindings sorted according to the rowComparator.
   * @throws TuplesException if the sorting can't be accomplished
   */
  public static Tuples sort(Tuples tuples, RowComparator rowComparator) throws TuplesException {
    if (!rowComparator.equals(tuples.getComparator())) {
      tuples = tuplesFactory.newTuples(tuples, rowComparator);
      if (logger.isDebugEnabled()) logger.debug("Sorted: " + tuples + " (using supplied row comparator)");
      return tuples;
    } else {
      return (Tuples) tuples.clone();
    }
  }

  /**
   * Sort into an order given by the list of variables.  The parameter is not closed, and this
   * method will create and return a new tuples.
   *
   * @param tuples The parameter to sort. This will be not be closed.
   * @param variableList the list of {@link Variable}s to sort by
   * @return A {@link Tuples} that meets the sort criteria. This may be the original tuples parameter.
   * @throws TuplesException if the sort operation fails
   */
  public static Tuples reSort(Tuples tuples, List<Variable> variableList) throws TuplesException {
    try {
      // if there is nothing to sort on, then tuples meets the criteria
      if ((variableList == null) || (variableList.size() == 0)) return (Tuples)tuples.clone();

      // if there is nothing to sort, then just return that nothing
      if (tuples.isUnconstrained()) {
        if (logger.isDebugEnabled()) logger.debug("Returning Unconstrained Tuples.");
        return TuplesOperations.unconstrained();
      } else if (tuples.isEmpty()) {
        return empty();
      }

      // initialise the mapping of column names to tuples columns.
      int[] varMap = new int[variableList.size()];

      boolean sortNeeded = false;
      // iterate over the variables to do the mapping
      for (int varCol = 0; varCol < variableList.size(); varCol++) {
        Variable var = variableList.get(varCol);
        // get the index of the variable in the tuples
        int ti = tuples.getColumnIndex(var);
        // check that it is within the prefix columns. If not, then sorting is needed
        if (ti >= varMap.length) sortNeeded = true;
        // map the tuples index of the variable to the column index
        varMap[varCol] = ti;
      }

      if (!sortNeeded) {
        if (logger.isDebugEnabled()) logger.debug("No sort needed on tuples.");
        return (Tuples)tuples.clone();
      }
      
      if (logger.isDebugEnabled()) logger.debug("Sorting on " + variableList);

      // append the remaining variables to the list of variables to sort on
      List<Variable> fullVarList = new ArrayList<Variable>(variableList);
      for (Variable v: tuples.getVariables()) {
        if (!variableList.contains(v)) fullVarList.add(v);
      }
      assert fullVarList.containsAll(Arrays.asList(tuples.getVariables()));

      // Reorder the columns - the projection here does not remove any columns
      Tuples projectedTuples = new UnorderedProjection(tuples, fullVarList);
      assert projectedTuples != tuples;

      // Perform the actual sort
      Tuples sortedTuples = tuplesFactory.newTuples(projectedTuples);
      assert sortedTuples != projectedTuples;
      projectedTuples.close();

      return sortedTuples;
    } catch (TuplesException e) {
      throw new TuplesException("Couldn't perform projection", e);
    }
  }


  /**
   * Truncate a tuples to have no more than a specified number of rows. This
   * method removes rows from the end of the tuples; to remove rows from the
   * start of the tuples, the {@link #offset} method can be used. If the limit
   * is larger than number of rows, the result is unchanged.
   *
   * @param tuples  the instance to limit
   * @param rowCount the number of leading rows to retain
   * @return the truncated tuples
   * @throws TuplesException If there was an error accessing the Tuples.
   */
  public static Tuples limit(Tuples tuples, long rowCount) throws TuplesException {
    return new LimitedTuples((Tuples) tuples.clone(), rowCount);
  }

  /**
   * If a tuples is virtual, evaluate and store it.
   *
   * @param tuples the instance to materialize
   * @return A set of Tuples with any virtual bindings converted into actual bindings.
   * @throws TuplesException If there was an error evaluating the virtual bindings
   */
  public static Tuples materialize(Tuples tuples) throws TuplesException {
    if (tuples.isMaterialized()) {
      return (Tuples)tuples.clone();
    } else {
      return tuplesFactory.newTuples(tuples);
    }
  }

  /**
   * Skip a specified number of rows from the beginning of a tuples. This method
   * removes rows from the beginning of the tuples; to remove rows from the end
   * of the tuples, the {@link #limit} method can be used. If more rows are
   * removed than are present, an empty tuples is produced.
   *
   * @param tuples  the instance to offset
   * @param rowCount the number of leading rows to remove
   * @return the remaining rows, if any
   * @throws TuplesException If there was an error accessing the tuples.
   */
  public static Tuples offset(Tuples tuples, long rowCount) throws TuplesException {
    return new OffsetTuples((Tuples)tuples.clone(), rowCount);
  }

  /**
   * Filter out duplicate rows.
   *
   * @param tuples The tuples to filter.
   * @return An equivalent Tuples, but with duplicate bindings removed.
   * @throws TuplesException If there was an error accessing the tuples.
   */
  public static Tuples removeDuplicates(Tuples tuples) throws TuplesException {

    if (tuples.hasNoDuplicates()) {
      if (logger.isDebugEnabled()) logger.debug("Didn't need to remove duplicates");
      return (Tuples)tuples.clone();
    }

    if (logger.isDebugEnabled()) logger.debug("Removing duplicates");

    if (tuples.getComparator() == null) {
      Tuples oldTuples = tuples;
      tuples = sort(tuples);
      assert tuples != oldTuples;
      // leave the original tuples.  We may not touch it.

      if (!tuples.hasNoDuplicates()) {
        oldTuples = tuples;
        tuples = new DistinctTuples(tuples);
        assert tuples != oldTuples;
        oldTuples.close();
      }

      return tuples;
    } else {
      if (logger.isDebugEnabled()) logger.debug("Already sorted: " + tuples);
      Tuples result = new DistinctTuples(tuples);
      return result;
    }
  }


  public static String formatTuplesTree(Tuples tuples) {
    return indentedTuplesTree(tuples, "").toString();
  }


  public static StringBuilder tuplesSummary(Tuples tuples) {
    StringBuilder buff = new StringBuilder();

    buff.append(tuples.getClass().toString());

    buff.append("<" + System.identityHashCode(tuples) + ">");
    buff.append("[");
    if (!tuples.isMaterialized()) buff.append("~");
    else buff.append("=");

    try {
      buff.append(tuples.getRowUpperBound());
      buff.append(" (~").append(tuples.getRowExpectedCount());
      buff.append(")]");
    } catch (TuplesException et) {
      buff.append(et.toString()).append("]");
    }

    buff.append(" {");
    Variable[] vars = tuples.getVariables();
    if (vars.length > 0) {
      buff.append(vars[0].toString());
      for (int i = 1; i < vars.length; i++) buff.append(", " + vars[i].toString());
    }
    buff.append("}");

    try {
      MandatoryBindingAnnotation mba = (MandatoryBindingAnnotation)tuples.getAnnotation(MandatoryBindingAnnotation.class);
      if (mba != null) buff.append(" :: MBA{ " + mba.requiredVariables() + " }");
    } catch (TuplesException et) {
      logger.error("Failed to obtain annotation", et);
    }

    return buff;
  }

  /**
   * Calculates a consistent hash code for a tuples.
   * @param t The tuples to get the hash code for.
   * @return The hash code value.
   */
  public static int hashCode(Tuples t) {
    t = (Tuples)t.clone();
    int result = t.getVariables().hashCode();
    try {
      t.beforeFirst();
      int cols = t.getNumberOfVariables();
      while (t.next()) {
        for (int i = 0; i < cols; i++) {
          long val = t.getColumnValue(i);
          result ^= (int)(val ^ (val >>> 32));
        }
      }
    } catch (TuplesException e) {
      throw new RuntimeException(e.toString(), e);
    } finally {
      try {
        if (t != null) t.close();
      } catch (TuplesException ex) {
        throw new RuntimeException(ex.toString(), ex);
      }
    }
    return result;
  }

  /**
   * Find the list of variables which appear in both the lhs and rhs tuples.
   *
   * @param lhs The first tuples to check the variables of.
   * @param rhs The second tuples to check the variables of.
   * @return A set containing all of the shared variables from lhs and rhs.
   */
  static Set<Variable> getMatchingVars(Tuples lhs, Tuples rhs) {
    // get all the variables from the lhs
    Set<Variable> commonVarSet = new HashSet<Variable>(Arrays.asList(lhs.getVariables()));
    // get all the variables from the rhs
    Set<Variable> rhsVars = new HashSet<Variable>(Arrays.asList(rhs.getVariables()));

    // find the intersecting set of variables
    commonVarSet.retainAll(rhsVars);
    return commonVarSet;
  }


  /**
   * Compares a tuples' variables to a set of variables.
   *
   * @param tuples The tuples to check the variables of.
   * @param vars The variables to check for.
   * @return <code>true</code> when all of the tuples' variables are in <code>vars</code>.
   */
  private static boolean checkForExtraVariables(Tuples tuples, Collection<Variable> vars) {
    // get the variable list
    Variable[] sv = tuples.getVariables();
    for (int i = 0; i < sv.length; i++) {
      if (!vars.contains(sv[i])) return true;  // extra variable
    }
    return false;
  }


  /**
   * Convert a list of Tuples into a string.
   * @param header The header for the returned string.
   * @param args The tuples to print.
   * @return The string containing the full list of tuples.
   */
  private static String printArgs(String header, List<? extends Tuples> args) {
    StringBuilder buff = new StringBuilder(header);
    buff.append("[");
    boolean first = true;
    for (Tuples arg: args) {
      if (!first) {
        buff.append(", ");
        first = false;
      }
      buff.append(tuplesSummary(arg));
    }
    buff.append("]");
    return buff.toString();
  }


  private static StringBuilder indentedTuplesTree(Tuples tuples, String indent) {
    StringBuilder buff = new StringBuilder();
    buff.append("\n").append(indent).append("(").append(tuplesSummary(tuples));
    for (Tuples t: tuples.getOperands()) buff.append(" ").append(indentedTuplesTree(t, indent + ".   "));
    buff.append(")");
    return buff;
  }

}
