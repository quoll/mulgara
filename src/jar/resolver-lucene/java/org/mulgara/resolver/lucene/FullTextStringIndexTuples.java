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

package org.mulgara.resolver.lucene;

// Java 2 standard packages
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Third party packages
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;

// JRDf
import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.URIReference;

// local packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintElement;
import org.mulgara.query.LocalNode;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.tuples.AbstractTuples;
import org.mulgara.store.tuples.Annotation;
import org.mulgara.store.tuples.DefinablePrefixAnnotation;
import org.mulgara.store.tuples.MandatoryBindingAnnotation;
import org.mulgara.store.tuples.RowComparator;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.tuples.TuplesOperations;

/**
 * A {@link Tuples} backed by a {@link FullTextStringIndex}. This is split into two parts, an
 * upper layer which mostly delegates all methods to another tuples, and a lower layer which is
 * a direct tuples around the lucene search hits. This allows us to conditionally materialize
 * the results by materializing the lower-layer tuples.
 *
 * <p>The observed performance of lucene is such that running one larger query is faster than
 * running, say, 100 smaller queries; at the same time, however, retrieving all the documents
 * for the results usually takes several times longer than the actual query took. We therefore
 * run a single query for the given constraint (rather than, say, running a query on each
 * <code>beforeFirst</code> with the currently bound values) which gathers the document id's
 * and scores. The documents are then retrieved on-demand by the lower-layer tuples; for most
 * queries we then just materialize that tuples rather than creating our own document-cache
 * (lucene does not explicitly cache documents) so repeated iterations over the results are
 * fast.
 *
 * @created 2002-03-27
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2002-2004 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
class FullTextStringIndexTuples extends AbstractTuples implements Resolution, Cloneable {
  /** Logger.  */
  private final static Logger logger = Logger.getLogger(FullTextStringIndexTuples.class);

  /** Session used to localize Lucene text into string pool nodes. */
  private final ResolverSession session;

  /** The upper bound on the number of items in tuples */
  private long rowUpperBound = -1;

  /** The real results. */
  private Tuples results = null;

  /** The list of variables as found in the constraint */
  private final List<Variable> constrVariableList = new ArrayList<Variable>(4);
  /** The list of lucene keys corresponding to the variables found in the constraint */
  private final List<String> constrLuceneKeyList = new ArrayList<String>(3);

  private final FullTextStringIndex fullTextStringIndex;
  private final LuceneConstraint constraint;

  private final ConstraintElement subjectElement;
  private final ConstraintElement predicateElement;
  private final ConstraintElement objectElement;

  //
  // Constructor
  //

  /**
   * Find the answer to a single constraint.
   *
   * The {@link org.mulgara.query.Answer}'s columns will
   * match the variables in the <var>constraint</var>, except that a magical
   * column called <code>$score</code> is added at the end, containing Lucene's
   * reckoning of how close the matches are.
   *
   * @param fullTextStringIndex PARAMETER TO DO
   * @param constraint the single constraint
   * @param session a session context for globalization, etc
   * @throws TuplesException if the set of triples couldn't be determined
   */
  FullTextStringIndexTuples(FullTextStringIndex fullTextStringIndex,
      LuceneConstraint constraint, ResolverSession session) throws TuplesException {
    this.fullTextStringIndex = fullTextStringIndex;
    this.session = session;
    this.constraint = constraint;

    // process subject
    subjectElement = constraint.getSubject();

    if (subjectElement instanceof Variable) {
      constrVariableList.add((Variable)subjectElement);
      constrLuceneKeyList.add(FullTextStringIndex.SUBJECT_KEY);
    }

    // process predicate
    predicateElement = constraint.getPredicate();

    if (predicateElement instanceof Variable) {
      constrVariableList.add((Variable)predicateElement);
      constrLuceneKeyList.add(FullTextStringIndex.PREDICATE_KEY);
    }

    // process object
    objectElement = constraint.getObject();

    if (objectElement instanceof Variable) {
      constrVariableList.add((Variable)objectElement);
      constrLuceneKeyList.add(FullTextStringIndex.LITERAL_KEY);
    }

    // Get the score variable
    Variable score = constraint.getScoreVar();
    if (score != null) {
      constrVariableList.add(score);
    }

    setVariables(constrVariableList);

    /* run the query now and materialize the result; it is often much faster to run a large query
     * and grab all resulting lucene documents than it is to run many smaller queries. Ideally we
     * would try and figure out which approach is better on a query-by-query basis.
     *
     * One special case is the all-variable query, which retrieves the whole db - this one is not
     * materialized because it's usually only used by export().
     */
    String subject = getString(subjectElement);
    String predicate = getString(predicateElement);
    String object = getString(objectElement);

    results = new SearchHitsTuples(subject, predicate, object);
    if (subject != null && predicate != null && object != null) {
      Tuples old = results;
      results = old.next() ? TuplesOperations.unconstrained() : TuplesOperations.empty();
      old.close();
    } else if (subject != null || predicate != null || object != null) {
      Tuples old = results;
      long t0 = System.currentTimeMillis();
      results = TuplesOperations.materialize(results);
      logger.debug("materialized " + results.getRowCount() + " lucene results() in " + (System.currentTimeMillis() - t0));
      old.close();
    }
  }

  //
  // Implementation of AbstractTuples methods
  //

  public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
    assert (constraint.getScoreVar() == null ||
            getString(objectElement, prefix, constrVariableList) != null) :
           "Internal error: lucene-query string not bound even though a score is requested";

    results.beforeFirst(prefix, suffixTruncation);
  }

  private String getString(ConstraintElement ce) throws TuplesException {
    return getString(ce, NO_PREFIX, constrVariableList);
  }

  private String getString(ConstraintElement ce, long[] prefix, List<Variable> vars) throws TuplesException {
    long boundVal = 0;

    if (ce instanceof LocalNode) {
      boundVal = ((LocalNode)ce).getValue();
    } else if (ce instanceof Variable) {
      int idx = vars.indexOf(ce);
      boundVal = (idx < prefix.length) ? prefix[idx] : 0;
    }

    if (boundVal == 0) return null;

    try {
      Object val = session.globalize(boundVal);
      if (val instanceof URIReference) return ((URIReference)val).getURI().toString();
      if (val instanceof Literal) return ((Literal)val).getLexicalForm();
      if (val instanceof BlankNode) return "";

      throw new TuplesException("Unknown node-type for Lucene constraint '" + ce + "': local-value=" + boundVal + ", global-value=" + val + ", class=" + val.getClass());
    } catch (GlobalizeException e) {
      throw new TuplesException("Couldn't globalize value " + boundVal, e);
    }
  }

  public void close() throws TuplesException {
    if (results != null)
      results.close();
  }

  public FullTextStringIndexTuples clone() {
    FullTextStringIndexTuples clone = (FullTextStringIndexTuples)super.clone();
    if (results != null) clone.results = (Tuples)results.clone();
    return clone;
  }

  public long getColumnValue(int column) throws TuplesException {
    return results.getColumnValue(column);
  }

  public long getRowCount() throws TuplesException {
    if (results == null)
      beforeFirst();

    return results.getRowCount();
  }

  public long getRowUpperBound() throws TuplesException {
    return (results != null) ? results.getRowUpperBound() : getRowUpperBoundEstimate();
  }

  public long getRowExpectedCount() throws TuplesException {
    return (results != null) ? results.getRowExpectedCount() : getRowUpperBoundEstimate();
  }

  private long getRowUpperBoundEstimate() throws TuplesException {
    if (rowUpperBound == -1) {
      try {
        rowUpperBound = (results != null) ? results.getRowCount() :
            fullTextStringIndex.getMaxDocs(getString(subjectElement),
                                           getString(predicateElement),
                                           getString(objectElement));
      } catch (FullTextStringIndexException e) {
        throw new TuplesException("Couldn't row upper-bound from text index: subject='" +
                                  getString(subjectElement) + "', predicate='" +
                                  getString(predicateElement) + "', object='" +
                                  getString(objectElement) + "'", e);
      }
    }

    return rowUpperBound;
  }

  public int getRowCardinality() throws TuplesException {
    long bound = getRowUpperBound();

    if (bound == 0) return Tuples.ZERO;
    if (bound == 1) return Tuples.ONE;
    return Tuples.MANY;
  }

  /**
   * Lucene never generates unbound columns.
   *
   * @return <code>false</code>
   */
  public boolean isColumnEverUnbound(int column) throws TuplesException {
    return false;
  }

  public boolean hasNoDuplicates() throws TuplesException {
    if (results == null)
      beforeFirst();
    return results.hasNoDuplicates();
  }

  public boolean isMaterialized() {
    return (results != null) && results.isMaterialized();
  }

  public RowComparator getComparator() {
    if (results != null) {
      return results.getComparator();
    } else {
      return null;
    }
  }

  public boolean next() throws TuplesException {
    assert results != null : "next() called without beforeFirst()";
    return results.next();
  }

  public Constraint getConstraint() {
    return constraint;
  }

  //!!FIXME: I have no idea if this is correct.
  public boolean isComplete() {
    return false;
  }

  public List<Tuples> getOperands() {
    return Collections.singletonList(results);
  }

  public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
    // the object (lucene query string) is required when a score is requested
    if (annotationClass == MandatoryBindingAnnotation.class &&
        objectElement instanceof Variable && constraint.getScoreVar() != null) {
      return new MandatoryBindingAnnotation(new Variable[] { (Variable)objectElement });
    }

    return (results != null) ? results.getAnnotation(annotationClass) : null;
  }

  private class SearchHitsTuples extends AbstractTuples {
    /** The current list of variables (possibly re-ordered from definePrefix()) */
    private final List<Variable> variableList = new ArrayList<Variable>(4);
    /** The list of lucene keys corresponding to the (re-ordered) variable-list */
    private final List<String> luceneKeyList = new ArrayList<String>(3);

    /** The native Lucene query result to represent as a {@link Tuples}. */
    private FullTextStringIndex.Hits hits;

    /** Which fields to load from the documents. */
    private final FieldSelector fieldSelector;

    /**
     * The current document within the {@link #hits}.
     *
     * A Lucene document hit corresponds to a {@link Tuples} row.
     */
    private Document document;

    /** The index of the next {@link #document} within the {@link #hits}. */
    private int nextDocumentIndex = 0;

    /** the currently bound value for the subject, or null if not bound */
    private String subject;
    /** the currently bound value for the predicate, or null if not bound */
    private String predicate;
    /** the currently bound value for the object, or null if not bound */
    private String object;

    public SearchHitsTuples(final String subject, final String predicate, final String object) throws TuplesException {
      if (logger.isDebugEnabled()) {
        logger.debug("Searching for " + subject + " : " + predicate + " : " + object);
      }

      // run the query
      try {
        hits = fullTextStringIndex.find(subject, predicate, object);
      } catch (FullTextStringIndexException e) {
        throw new TuplesException("Couldn't generate answer from text index: subject='" + subject +
                                  "', predicate='" + predicate + "', object='" + object + "'", e);
      }

      // sort the result in doc-id order for faster document retrieval
      hits.sort();

      // make sure we only load those fields we need (=> faster document retrieval)
      List<String> load = new ArrayList<String>(3);
      if (subject == null) load.add(FullTextStringIndex.SUBJECT_KEY);
      if (predicate == null) load.add(FullTextStringIndex.PREDICATE_KEY);
      if (object == null) load.add(FullTextStringIndex.LITERAL_KEY);
      fieldSelector = new MapFieldSelector(load);

      // prepare for iterating
      document = null;
      nextDocumentIndex = 0;

      variableList.addAll(constrVariableList);
      luceneKeyList.addAll(constrLuceneKeyList);

      setVariables(variableList);
    }

    public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
      subject = getString(subjectElement, prefix, variableList);
      predicate = getString(predicateElement, prefix, variableList);
      object = getString(objectElement, prefix, variableList);

      document = null;
      nextDocumentIndex = 0;
    }

    public boolean next() throws TuplesException {
      try {
        while (nextDocumentIndex < hits.length()) {
          document = hits.doc(nextDocumentIndex++, fieldSelector);
          if (matches(subject, document.get(FullTextStringIndex.SUBJECT_KEY)) &&
              matches(predicate, document.get(FullTextStringIndex.PREDICATE_KEY)) &&
              matches(object, document.get(FullTextStringIndex.LITERAL_KEY))) {
            return true;
          }
        }

        document = null;
        return false;
      } catch (IOException e) {
        throw new TuplesException("Couldn't obtain next Lucene hit", e);
      }
    }

    private boolean matches(String bound, String found) {
      return bound == null || found == null || bound.equals(found);
    }

    public long getRowCount() throws TuplesException {
      return hits.length();
    }

    public long getRowUpperBound() throws TuplesException {
      return hits.length();
    }

    public long getRowExpectedCount() throws TuplesException {
      return hits.length();
    }

    public int getRowCardinality() throws TuplesException {
      switch (hits.length()) {
        case 0: return Tuples.ZERO;
        case 1: return Tuples.ONE;
        default: return Tuples.MANY;
      }
    }

    public List<Tuples> getOperands() {
      return Collections.<Tuples>emptyList();
    }

    public long getColumnValue(int column) throws TuplesException {
      try {
        if (column >= 0 && column < luceneKeyList.size()) {
          String luceneKey = luceneKeyList.get(column);
          if (luceneKey == FullTextStringIndex.LITERAL_KEY)
            return session.localize(new LiteralImpl(document.get(luceneKey)));
          else
            return session.localize(new URIReferenceImpl(new URI(document.get(luceneKey))));
        } else if (column == luceneKeyList.size()) {
          // Generate the score column
          return session.localize(new LiteralImpl(hits.score(nextDocumentIndex - 1)));
        } else {
          throw new TuplesException("Column " + column + " does not exist");
        }
      } catch (IOException e) {
        throw new TuplesException("Couldn't get column " + column + " value", e);
      } catch (LocalizeException e) {
        throw new TuplesException("Couldn't localize column " + column + " value", e);
      } catch (URISyntaxException e) {
        throw new TuplesException("Couldn't get column " + column + " value", e);
      }
    }

    public boolean isColumnEverUnbound(int column) throws TuplesException {
      return false;
    }

    public boolean hasNoDuplicates() throws TuplesException {
      return false;
    }

    public void close() throws TuplesException {
      try {
        if (hits != null) hits.close();
      } catch (IOException ioe) {
        throw new TuplesException("Error closing fulltext index hits", ioe);
      }
    }

    public SearchHitsTuples clone() {
      SearchHitsTuples clone = (SearchHitsTuples)super.clone();
      if (hits != null) clone.hits = hits.clone();
      return clone;
    }

    public Annotation getAnnotation(Class<? extends Annotation> annotationClass) throws TuplesException {
      // support re-ordering the variables so any variables can be bound in the prefix
      if (annotationClass == DefinablePrefixAnnotation.class) {
        return new DefinablePrefixAnnotation() {
          public void definePrefix(Set<Variable> boundVars) throws TuplesException {
            if (boundVars.contains(constraint.getScoreVar()))
              throw new TuplesException("Score variable may not be bound");

            variableList.clear();
            luceneKeyList.clear();

            for (boolean useBound : new boolean[] { true, false }) {
              for (int idx = 0; idx < constrLuceneKeyList.size(); idx++) {
                Variable var = constrVariableList.get(idx);

                if (boundVars.contains(var) == useBound) {
                  variableList.add(var);
                  luceneKeyList.add(constrLuceneKeyList.get(idx));
                }
              }
            }

            if (constraint.getScoreVar() != null) variableList.add(constraint.getScoreVar());
            setVariables(variableList);
          }
        };
      }

      return null;
    }
  }
}
