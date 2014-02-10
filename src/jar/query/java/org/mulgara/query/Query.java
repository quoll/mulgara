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
 *   Move to java-generics copyright Netymon Pty Ltd
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.query;

// Java 2 standard packages;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mulgara.connection.Connection;
import org.mulgara.connection.Connection.SessionOp;
import org.mulgara.query.operation.Command;
import org.mulgara.server.Session;

/**
 * An ITQL query. This is a data structure used as an argument to the
 * {@link org.mulgara.server.Session#query} method.
 *
 * @created 2001-08-12
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class Query implements Cloneable, Serializable, Command {

 /**
  * Allow newer compiled version of the stub to operate when changes
  * have not occurred with the class.
  * NOTE : update this serialVersionUID when a method or a public member is
  * deleted.
  */
  private static final long serialVersionUID = 5165341858790210479L;

  /** Logger. */
  private static final Logger logger = Logger.getLogger(Query.class.getName());

  /**
   * The variable list. This may only contain {@link Variable}s. It corresponds
   * to the <code>select</code> clause. If it is <code>null</code>, it indicates
   * that there is no <code>select</code> clause and that no projection will be
   * performed.
   */
  private List<SelectElement> variableList;

  /**
   * Mutable version of the variable list. This isn't exposed via {@link
   * #getVariableList} the way {@link #variableList} is.
   */
  private List<SelectElement> mutableVariableList;

  /** The model expression. It corresponds to the <code>from</code> clause. */
  private GraphExpression graphExpression;

  /** The constraint expression.  It corresponds to the <code>where</code> clause. */
  private ConstraintExpression constraintExpression;

  /** The having expression.  It corresponds to the <code>having</code> clause. */
  private ConstraintHaving havingConstraint;

  /**
   * The sort ordering. The elements of this list should be {@link Order}s, with
   * major orderings preceding minor orderings. It's only sensible for this to
   * contain orders on variables in the {@link #variableList}.
   */
  private List<Order> orderList;

  /**
   * The limit on rows in the result. If this is <code>null</code>, it indicates
   * that there is no limit.
   */
  private Integer limit;

  /** The offset on rows in the result. This value is never negative. */
  private int offset;

  /** Indicates that the results must be without duplicates. */
  private boolean distinct = true;

  /** The accumulated solutions. This can be <code>null</code>, indicating no solutions. */
  private Answer answer;

  /** A UI message describing the result of this message. */
  private String resultMessage = "";

  /** A lexical form for the query, set manually rather than constructed in {@link #toString()}. */
  private String textualForm = "";

  //
  // Constructors
  //

  /**
   * Construct a query.
   *
   * @param variableList {@link Variable}s or node values to appear as bindings
   *     in the solution (i.e. columns of the result {@link Answer});
   *     <code>null</code> indicates that all columns are to be retained.
   *     This is a list of: Variable; ConstantValue; Count; Subquery.
   * @param graphExpression an expression defining the model to query, never
   *     <code>null</code>
   * @param constraintExpression an expression defining the constraints to
   *     satisfy, never <code>null</code>
   * @param havingExpression an expression defining the conditions to apply to
   *     aggregate functions or null if not given.
   * @param orderList sort order column names. This is a list of {@link Order}s
   *     which is a wrapper around Variable, with an "ascending" flag.
   * @param limit the maximum number of rows to return, which must be
   *     non-negative; <code>null</code> indicates no limit
   * @param offset the number of rows to skip from the beginning of the result,
   *     never negative
   * @param answer an existing solution set to which results must belong, or
   *     {@link UnconstrainedAnswer} for no constraints; never
   *     <code>null</code> is
   * @throws IllegalArgumentException if <var>limit</var> or <var>offset</var>
   *     are negative, or if <var>graphExpression</var>,
   *     <var>constraintExpression</var>, <var>orderList<var> or
   *     <var>answer</var> are <code>null</code>
   */
  public Query(List<? extends SelectElement> variableList, GraphExpression graphExpression,
      ConstraintExpression constraintExpression,
      ConstraintHaving havingExpression, List<Order> orderList, Integer limit,
      int offset, boolean distinct, Answer answer) {

    // Validate parameters
    if (graphExpression == null) {
      throw new IllegalArgumentException("Null \"graphExpression\" parameter");
    } else if (constraintExpression == null) {
      throw new IllegalArgumentException("Null \"constraintExpression\" parameter");
    } else if ((limit != null) && (limit.intValue() < 0)) {
      throw new IllegalArgumentException("Negative \"limit\" parameter");
    } else if (orderList == null) {
      throw new IllegalArgumentException("Null \"orderList\" parameter");
    } else if (offset < 0) {
      throw new IllegalArgumentException("Negative \"offset\" parameter");
    } else if (answer == null) {
      throw new IllegalArgumentException("Null \"answer\" parameter");
    } else if (variableList != null) {
      Set<Variable> variableSet = new HashSet<Variable>(constraintExpression.getVariables());
      variableSet.addAll(Arrays.asList(answer.getVariables()));

      for (Object o: variableList) {
        if (o instanceof Variable) {
          Variable var = (Variable)o;
          if (!var.isBnodeVar() && !variableSet.contains(var)) {
            if (logger.isDebugEnabled()) logger.debug("Failed to find " + var + " in " + variableSet);
            throw new IllegalArgumentException("Failed to constrain all variables: " + var +
                " not constrained in WHERE or GIVEN clauses");
          }
          if (var.isBnodeVar() && !(this instanceof ConstructQuery)) {
            if (logger.isDebugEnabled()) logger.debug("BNode variable in non-CONSTRUCT query: " + var);
            throw new IllegalArgumentException("BNode variable in non-CONSTRUCT query: " + var);
          }
        }
      }
    }

    // Initialize fields
    this.mutableVariableList = (variableList == null) ? null : new ArrayList<SelectElement>(variableList);
    this.variableList = (variableList == null) ? null : Collections.unmodifiableList(mutableVariableList);
    this.graphExpression = graphExpression;
    this.constraintExpression = constraintExpression;
    this.havingConstraint = havingExpression;
    this.orderList = Collections.unmodifiableList(new ArrayList<Order>(orderList));
    this.limit = limit;
    this.offset = offset;
    this.distinct = distinct;
    this.answer = answer;
  }

 
  /**
   * Construct a new query equivalent to substituing 'where' for the
   * where-clause in the original query.
   */
  public Query(Query query, ConstraintExpression where) {
    this.mutableVariableList = query.mutableVariableList;
    this.variableList = query.variableList;
    this.graphExpression = query.graphExpression;
    this.constraintExpression = where;
    this.havingConstraint = query.havingConstraint;
    this.orderList = query.orderList;
    this.limit = query.limit;
    this.offset = query.offset;
    this.distinct = query.distinct;
    this.answer = (query.answer != null) ?(Answer)query.answer.clone() : new UnconstrainedAnswer();
    /*
      this(query.getVariableList(),
          query.getModelExpression(),
          where,
          query.getHavingExpression(),
          query.getOrderList(),
          query.getLimit(),
          query.getOffset(),
          (Answer)query.getGiven().clone());
    */
  }

  /**
   * Cloning must always be supported.
   */
  public Object clone() {

    Query cloned;
    try {
      cloned = (Query) super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException("Query subclass "+getClass()+" not cloneable");
    }

    // Copy mutable fields by value
    if (variableList == null) {
      cloned.mutableVariableList = null;
      cloned.variableList = null;
    } else {
      cloned.variableList = new ArrayList<SelectElement>();
      for (SelectElement o : variableList) {
        if (o instanceof Subquery) {
          Subquery s = (Subquery)o;
          cloned.variableList.add(new Subquery(s.getVariable(), (Query)s.getQuery().clone()));
        } else if (o instanceof Count) {
          Count a = (Count)o;
          cloned.variableList.add(new Count(a.getVariable(), (Query)a.getQuery().clone()));
        } else {
          cloned.variableList.add(o);
        }
      }
      cloned.mutableVariableList = Collections.unmodifiableList(cloned.variableList);
    }
    cloned.graphExpression = graphExpression;  // FIXME: should be cloned
    cloned.answer = (Answer)answer.clone();

    // Copy immutable fields by reference
    cloned.orderList = orderList;
    cloned.limit = limit;
    cloned.offset = offset;

    return cloned;
  }

  //
  // API methods
  //

  /**
   * Accessor for the <code>variableList</code> property.
   *
   * @return a {@link List} containing one or more {@link Variable}s, {@link ConstantValue}s,
   * {@link Count}s or {@link Subquery}
   */
  public List<SelectElement> getVariableList() {
    return variableList;
  }


  /**
   * Accessor for the <code>constraintExpression</code> property.
   * @return a {@link ConstraintExpression}
   */
  public ConstraintExpression getConstraintExpression() {
    return constraintExpression;
  }

  /**
   * Accessor for the <code>havingExpression</code> property.
   * @return a {@link ConstraintExpression} containing only
   *   {@link ConstraintHaving} or <code>null</code> to indicate an empty
   *   having clause.
   */
  public ConstraintHaving getHavingExpression() {
    return havingConstraint;
  }


  /**
   * Accessor for the <code>graphExpression</code> property.
   * @return a {@link GraphExpression}, or <code>null</code> to indicate the empty model
   */
  public GraphExpression getModelExpression() {
    return graphExpression;
  }


  /**
   * Accessor for the <code>orderList</code> property.
   * @return a {@link List} containing one or more {@link Order}s
   *         (which wrap {@link Variable}s)
   */
  public List<Order> getOrderList() {
    return orderList;
  }


  /**
   * Accessor for the <code>limit</code> property.
   * @return the limit for this query, or <code>null</code> if unlimited
   */
  public Integer getLimit() {
    return limit;
  }


  /**
   * Accessor for the <code>offset</code> property.
   * @return the offset for this query, a non-negative integer
   */
  public int getOffset() {
    return offset;
  }


  /**
   * Accessor for the <code>answer</code> property. If the <var>
   * constraintExpression</var> property is <code>null</code>, this is the
   * answer to the entire query.
   * @return an {@link Answer}, or <code>null</code> to indicate the set of all statements
   */
  public Answer getGiven() {
    return answer;
  }

  /**
   * Accessor for the DISTINCT property on this query.
   * @return <code>true</code> if results of the query should not contain duplicates.
   */
  public boolean isDistinct() {
    return distinct;
  }

  //
  // Methods overriding Object
  //

  /**
   * Equality is by value.
   * @param object The object to compare to
   * @return <code>true</code> if object is functionaly equivalent to this object.
   */
  public boolean equals(Object object) {

    if (object == this) return true;
    if (object == null) return false;
    if (!(object instanceof Query)) return false;

    Query query = (Query)object;

    // Check the variableList field
    if (!variableList.equals(query.variableList)) return false;

    // Check the graphExpression field
    if ((graphExpression == null) ?
        (query.graphExpression != null) :
        (!graphExpression.equals(query.graphExpression))) {
      return false;
    }

    // Check the constraintExpression field
    if ((constraintExpression == null) ?
        (query.constraintExpression != null) :
        (!constraintExpression.equals(query.constraintExpression))) {
      return false;
    }

    if ((havingConstraint == null) ?
        (query.havingConstraint != null) :
        (!havingConstraint.equals(query.havingConstraint))) {
      return false;
    }

    // Check the orderList field
    if ((orderList == null) ^ (query.orderList == null)) return false;

    if ((orderList != null) && !orderList.equals(query.orderList)) return false;

    // Check the limit field
    if ((limit == null) ^ (query.limit == null)) return false;
    if ((limit != null) && !limit.equals(query.limit)) return false;

    // Check the offset field
    if (offset != query.offset) return false;

    // Finally, it comes down to the answer field
    return answer.equals(query.answer);
  }

  public int hashCode() {
    return variableList.hashCode() + 3 * graphExpression.hashCode() +
           5 * constraintExpression.hashCode() + 7 * havingConstraint.hashCode() +
           11 * ((orderList == null) ? 0 : orderList.hashCode()) +
           13 * ((limit == null) ? 0 : limit.hashCode()) +
           17 * offset + answer.hashCode();
  }

  /**
   * Close this {@link Query}, and the underlying {@link Answer} objects.
   */
  public void close() throws TuplesException {
    answer.close();
    answer = null;

    if (mutableVariableList != null) {
      for (SelectElement v: mutableVariableList) {
        if (v instanceof AggregateFunction) {
          ((AggregateFunction)v).getQuery().close();
        }
      }
    }
  }


  /** @see org.mulgara.query.operation.Command#setText(java.lang.String) */
  public void setText(String text) {
    textualForm = text;
  }


  /**
   * Returns the textual representation of this Command.
   * @return The text of the command. This comes from the lexer, rather than reconstruction
   *         from the structure of the query.
   */
  public String getText() {
    return textualForm;
  }


  /**
   * Generate a legible representation of the query.
   *
   * @return A string representing all the elements of a query.
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer();

    // SELECT
    if (variableList != null) {
      buffer.append("SELECT");
      for (Object i: variableList) buffer.append(" ").append(i);
      buffer.append(" ");
    }

    // FROM
    buffer.append("FROM ").append(graphExpression);

    // WHERE
    buffer.append(" WHERE ").append(constraintExpression);

    // HAVING
    if (havingConstraint != null) buffer.append(" HAVING ").append(havingConstraint);

    // ORDER BY
    if (!orderList.isEmpty()) {
      buffer.append(" ORDER BY");
      for (Order o: orderList) buffer.append(" ").append(o);
    }

    // LIMIT
    if (limit != null) buffer.append(" LIMIT ").append(limit.intValue());

    // OFFSET
    if (offset != 0) buffer.append(" OFFSET ").append(offset);

    // GIVEN
    if (answer != null) buffer.append(" GIVEN ").append(answer);

    return buffer.toString();
  }


  /**
   * Serializes the current object to a stream.
   * @param out The stream to write to.
   * @throws IOException If an I/O error occurs while writing.
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    // convert answer to be serializable if needed
    if (!(answer instanceof Serializable)) {
      // TODO: use a remote answer object if the given is too large
      try {
        Answer tmpAnswer = answer;
        answer = new ArrayAnswer(answer);
        tmpAnswer.close();
      } catch (TuplesException e) {
        throw new IOException("Unable to serialize GIVEN clause");
      }
    }
    out.defaultWriteObject();
  }


  //
  // Command interface methods
  //
  
  /**
   * Operation can only be run by a server.
   * @return <code>false</code> as this is AST for a server.
   */
  public boolean isLocalOperation() {
    return false;
  }


  /**
   * Operation is not restricted to a user interface.
   * @return <code>false</code> as this operation has no effect on a UI.
   */
  public boolean isUICommand() {
    return false;
  }


  /**
   * Indicates that this command returns an Answer. Saves the overhead of checking
   * the return type of execute.
   * @return <code>true</code>.
   */
  public boolean isAnswerable() {
    return true;
  }


  /**
   * Indicates that the command modifies the state in a transaction.
   * @return <code>true</code> If the transaction state is to be modified.
   */
  public boolean isTxCommitRollback() {
    return false;
  }
  
  /**
   * Gets the associated server for a non-local operation.
   * @return the server URI, or <code>null</code> if the data should be found locally.
   */
  public URI getServerURI() {
    Set<URI> dbURIs = getModelExpression().getDatabaseURIs();
    return dbURIs.isEmpty() ? null : dbURIs.iterator().next();
  }


  /**
   * Gets a message text relevant to the operation.  Useful for the UI.
   * Consider changing this to a serialization of the result.
   * @return A text message associated with the result of this operation. Usually empty.
   */
  public String getResultMessage() {
    return resultMessage;
  }


  /**
   * Sets message text relevant to the operation.  Useful for the UI.
   * @return The set text.
   */
  public String setResultMessage(String resultMessage) {
    return this.resultMessage = resultMessage;
  }


  /**
   * Executes this query on a connection.
   * @param conn The connection to a database session to execute the query against.
   * @return The answer to this query.  This must be closed by the calling code.
   */
  public Answer execute(Connection conn) throws QueryException, TuplesException {
    if (logger.isDebugEnabled()) logger.debug("Executing query " + toString());
    Answer answer = conn.execute(new SessionOp<Answer,QueryException>() {
      public Answer fn(Session session) throws QueryException {
        return session.query(Query.this);
      }
    });
    if (answer == null) throw new QueryException("Invalid answer received");
    if (logger.isDebugEnabled()) logger.debug("Successfully executed query");
    // move to the first row
    answer.beforeFirst();
    return answer;
  }

}
