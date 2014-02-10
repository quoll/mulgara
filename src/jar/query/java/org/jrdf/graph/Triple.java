/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The JRDF Project.  All rights reserved.
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

import java.io.Serializable;

/**
 * RDF Triple. An RDF triple as defined in "Resource Description Framework (RDF):
 * Concepts and Abstract Syntax"
 * <a href="http://www.w3c.org/TR/rdf-concepts/#section-triples"/>6.1 RDF Triples</a>.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author Andrew Newman
 *
 * @version $Revision: 624 $
 */
public interface Triple extends Serializable {

  /**
   * Obtains the subject of this statement.
   *
   * @return an {@link SubjectNode} which is either a {@link BlankNode} or
   *     {@link URIReference}
   */
  SubjectNode getSubject();

  /**
   * Obtains the predicate of this statement.
   *
   * @return a {@link PredicateNode} which is a {@link URIReference}
   */
  PredicateNode getPredicate();

  /**
   * Obtains the object of this statement.
   *
   * @return a {@link ObjectNode} which is either a {@link BlankNode},
   *     {@link URIReference} or {@link Literal}
   */
  ObjectNode getObject();

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * A triple is equal to another triple if the subject, predicate and object
   * are all equal.
   *
   * @param obj the reference object with which to compare.
   * @return true if this object is the same as the obj argument; false otherwise.
   */
  boolean equals(Object obj);

  /**
   * Returns a hash-code value for this triple. The hash code is based upon
   * XORing all of the components of a triple i.e. the subject, predicate and
   * object.
   *
   * @return a hash-code value for this triple.
   */
  int hashCode();
}
