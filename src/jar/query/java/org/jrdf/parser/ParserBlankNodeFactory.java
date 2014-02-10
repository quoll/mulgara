/*
 * $Header$
 * $Revision: 624 $
 * $Date: 2006-06-24 21:02:12 +1000 (Sat, 24 Jun 2006) $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003-2005 The JRDF Project.  All rights reserved.
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

package org.jrdf.parser;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactoryException;

/**
 * A factory for creating BlankNodes.  One possible implementation might create
 * BlankNodes that maintain their identity relative to the RDF file being
 * parsed, possibly by wrapping the nodeID in the case of
 * {@link #createBlankNode(String)} or by wrapping a sequence number in the
 * case of {@link #createBlankNode()}.  In that case the resulting BlankNodes
 * would only be useful as a temporary object that is used while an RDF file is
 * being parsed. <br>
 * Another implementation might keep a map from <code>nodeID</code> to
 * <code>BlankNode</code> so that {@link #createBlankNode(String)} would always
 * return the same BlankNode object when called with a particular
 * <code>nodeID</code>.
 *
 * @author David Makepeace
 *
 * @version $Revision: 624 $
 */
public interface ParserBlankNodeFactory {

  /**
   * Creates a BlankNode that is unique relative to this instance of the
   * ParserBlankNodeFactory.
   *
   * @return the BlankNode.
   * @throws GraphElementFactoryException if there is a failure to create the
   *   blank node.
   */
  BlankNode createBlankNode() throws GraphElementFactoryException;

  /**
   * Creates a BlankNode that is only distinguished by the nodeID value.
   *
   * @param nodeID the nodeID of the blank node - consistently returns the same
   *   blank node if it exists. 
   * @return the BlankNode.
   * @throws GraphElementFactoryException if there is a failure to create the
   *   blank node.
   */
  BlankNode createBlankNode(String nodeID) throws GraphElementFactoryException;

  /**
   * Resets this ParserBlankNodeFactory.  Subsequent calls to
   * {@link #createBlankNode()} may return BlankNodes that compare equal to
   * BlankNodes that were created before the call to <code>clear()</code>.
   */
  void clear();

}
