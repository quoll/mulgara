/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
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

package org.jrdf.graph.mem;

import org.jrdf.graph.*;

/**
 * A trivial implementation of an RDF {@link Triple}.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 *
 * @version $Revision: 624 $
 */
public class TripleImpl extends AbstractTriple {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 7468341140195609635L;

  /**
   * Constructor for this Triple, only to be used by the NodeFactory.
   *
   * @param subject The subject node of this triple.
   * @param predicate The predicate node of this triple.
   * @param object The object node of this triple.
   */
  TripleImpl(SubjectNode subject, PredicateNode predicate, ObjectNode object) {
    subjectNode = subject;
    predicateNode = predicate;
    objectNode = object;
  }


  /**
   * Constructor for this Triple, only to be used by the iterators.
   *
   * @param factory the factory to use to get the nodes that make up this
   *   triple.
   * @param subject The subject node id of this triple.
   * @param predicate The predicate node id of this triple.
   * @param object The object node id of this triple.
   */
  TripleImpl(GraphElementFactory factory, Long subject, Long predicate,
      Long object) {
    GraphElementFactoryImpl graphFactory = (GraphElementFactoryImpl) factory;
    subjectNode = (SubjectNode) graphFactory.getNodeById(subject);
    predicateNode = (PredicateNode) graphFactory.getNodeById(predicate);
    objectNode = (ObjectNode) graphFactory.getNodeById(object);
  }


  /**
   * Constructor for this Triple, only to be used by the variable iterators.
   *
   * @param factory the factory to use to get the nodes that make up this
   *   triple.
   * @param first The first node id of this triple.
   * @param second The second node id of this triple.
   * @param third The third node id of this triple.
   */
  TripleImpl(GraphElementFactoryImpl factory, int var, Long first,
      Long second, Long third) {
    Long[] nodes = new Long[]{first, second, third};
    subjectNode = (SubjectNode) factory.getNodeById(nodes[var]);
    predicateNode = (PredicateNode) factory.getNodeById(
        nodes[(var + 1) % 3]);
    objectNode = (ObjectNode) factory.getNodeById(nodes[(var + 2) % 3]);
  }
}
