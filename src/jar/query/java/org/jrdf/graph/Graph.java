/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2004 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.graph;

import org.jrdf.util.ClosableIterator;

import java.io.Serializable;
import java.util.Iterator;

/**
 * An RDF Graph. As defined by the
 * <a href="http://www.w3.org/TR/2003/WD-rdf-concepts-20031010"><cite>Resource
 * Description Framework (RDF): Concepts and Abstract Syntax</cite> </a>
 * specification.  An RDF graph is a set of RDF triples.  The set of nodes of
 * an RDF graph is the set of subjects and objects of triples in the graph.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 */
public interface Graph extends Serializable {

  /**
   * Test the graph for the occurrence of a statement.  A null value for any
   * of the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param subject The subject to find or null to indicate any subject.
   * @param predicate The predicate to find or null to indicate any predicate.
   * @param object The object to find or null to indicate any object.
   * @return True if the statement is found in the model, otherwise false.
   * @throws GraphException If there was an error accessing the graph.
   */
  boolean contains(SubjectNode subject, PredicateNode predicate, ObjectNode object)
      throws GraphException;

  /**
   * Test the graph for the occurrence of the triple.  A null value for any
   * of the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param triple The triple to find.
   * @return True if the triple is found in the graph, otherwise false.
   * @throws GraphException If there was an error accessing the graph.
   */
  boolean contains(Triple triple) throws GraphException;

  /**
   * Returns an iterator of {@link Triple}s to a set of statements that
   * match a given subject, predicate and object.  A null value for any of
   * the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param subject The subject to find or null to indicate any subject.
   * @param predicate The predicate to find or null to indicate any predicate.
   * @param object ObjectNode The object to find or null to indicate any object.
   * @return an iterator containing the matching statements.
   * @throws GraphException If there was an error accessing the graph.
   */
  ClosableIterator<Triple> find(SubjectNode subject, PredicateNode predicate, ObjectNode object)
      throws GraphException;

  /**
   * Returns an iterator of {@link Triple}s to a set of statements that
   * match a given subject, predicate and object.  A null value for any of
   * the parts of a triple are treated as unconstrained, any values will be
   * returned.
   *
   * @param triple The triple to find.
   * @return an iterator containing the matching statements.
   * @throws GraphException If there was an error accessing the graph.
   */
  ClosableIterator<Triple> find(Triple triple) throws GraphException;

  /**
   * Adds a triple to the graph.  The nodes must have already been created
   * using {@link GraphElementFactory}.
   *
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @throws GraphException If the statement can't be made.
   */
  void add(SubjectNode subject, PredicateNode predicate, ObjectNode object)
      throws GraphException;

  /**
   * Adds a triple to the graph.  The nodes must have already been created
   * using {@link GraphElementFactory}.
   *
   * @param triple The triple.
   * @throws GraphException If the statement can't be made.
   */
  void add(Triple triple) throws GraphException;

  /**
   * Adds all the triples contained in an iterator into the graph.  The nodes
   * must have already been created using {@link GraphElementFactory}.
   *
   * @param triples The triple iterator.
   * @throws GraphException If the statements can't be made.
   */
  void add(Iterator<Triple> triples) throws GraphException;

  /**
   * Closes any resources associated with this graph.
   */
  void close();

  /**
   * Removes a triple from the graph.  The nodes must have already been
   * created using {@link GraphElementFactory}.
   *
   * @param subject The subject.
   * @param predicate The predicate.
   * @param object The object.
   * @throws GraphException If there was an error revoking the statement, For
   *     example if it didn't exist.
   */
  void remove(SubjectNode subject, PredicateNode predicate, ObjectNode object)
      throws GraphException;

  /**
   * Removes a triple from the graph.  The nodes must have already been
   * created using {@link GraphElementFactory}.
   *
   * @param triple The triple.
   * @throws GraphException If there was an error revoking the statement, For
   *     example if it didn't exist.
   */
  void remove(Triple triple) throws GraphException;

  /**
   * Removes all the triples contained in an iterator from the graph.  The
   * nodes must have already been created using {@link GraphElementFactory}.
   *
   * @param triples The triple iterator.
   * @throws GraphException If the statements can't be revoked.
   */
  void remove(Iterator<Triple> triples) throws GraphException;

  /**
   * Returns the node factory for the graph, or creates one.
   *
   * @return the node factory for the graph, or creates one.
   */
  GraphElementFactory getElementFactory();

  /**
   * Returns the triple factory for the graph, or creates one.
   *
   * @return the triple factory for the graph, or creates one.
   */
  TripleFactory getTripleFactory();

  /**
   * Returns the number of triples in the graph.
   *
   * @return the number of triples in the graph.
   * @throws GraphException If the statements number of statements in the graph
   *   fails to be found.
   */
  long getNumberOfTriples() throws GraphException;

  /**
   * Returns true if the graph is empty i.e. the number of triples is 0.
   *
   * @return true if the graph is empty i.e. the number of triples is 0.
   * @throws GraphException If the statements number of statements in the graph
   *   fails to be found.
   */
  boolean isEmpty() throws GraphException;
}
