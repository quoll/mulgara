/*
 * $Header: /home/pgearon/cvs/mulgara/mulgara-1.1/src/jar/client-jrdf/java/org/mulgara/client/jrdf/util/ClientGraphUtil.java,v 1.8 2005/01/05 04:57:37 newmana Exp $
 * $Revision: 1.8 $
 * $Date: 2005/01/05 04:57:37 $
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

package org.mulgara.client.jrdf.util;

//local packages
import org.jrdf.graph.*;
import org.jrdf.util.ClosableIterator;
import org.mulgara.client.jrdf.*;

/**
 * Utility class used with client-side JRDF Graphs.</p>
 *
 * <p>Data Access Object (DAO).</p>
 *
 * @created 2004-08-16
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:37 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class ClientGraphUtil {

  /**
   * Returns a ClosableIterator that orders the Triples by SubjectNode.
   * ClosableIterator returned will be memory bound.
   *
   * @return ClosableIterator
   * @throws GraphException
   */
  public static ClosableIterator<Triple> orderBySubject(ClosableIterator<Triple> iterator) throws
      GraphException {

    //validate
    if (iterator == null) {

      throw new IllegalArgumentException("ClosableIterator cannot be null.");
    }

    //Get all Triples
    VirtualClosableIteratorProxy<Triple> allTriplesProxy = new
        DefaultClosableIteratorProxy<Triple>(iterator);

    //Order the Triples
    return new OrderedClosableIteratorProxy<Triple>(allTriplesProxy,
                                            new SubjectNodeComparator());
  }

  /**
   * Returns a ClosableIterator that orders the Triples by SubjectNode,
   * PredicateNode and ObjectNode respectively. ClosableIterator returned will
   * be memory bound.
   *
   * @return ClosableIterator
   * @throws GraphException
   */
  public static ClosableIterator<Triple> orderBySPO(ClosableIterator<Triple> iterator) throws
      GraphException {

    //validate
    if (iterator == null) {

      throw new IllegalArgumentException("ClosableIterator cannot be null.");
    }

    //wrap the iterator
    VirtualClosableIteratorProxy<Triple> allTriplesProxy = new
        DefaultClosableIteratorProxy<Triple>(iterator);

    //Order the Triples
    return new OrderedClosableIteratorProxy<Triple>(allTriplesProxy, new SPOComparator());
  }

  /**
   * Returns a ClosableIterator that iterates over all unique subjects in the
   * Graph.
   *
   * @param graph Graph
   * @return ClosableIterator
   * @throws GraphException
   */
  public static ClosableIterator<Triple> getUniqueSubjects(Graph graph) throws
      GraphException {

    //validate
    if (graph == null) {

      throw new IllegalArgumentException("Graph cannot be null.");
    }

    //Get all Triples
    ClosableIterator<Triple> allTriples = graph.find(null, null, null);

    //Order the Triples
    OrderedClosableIteratorProxy<Triple> orderedTriples = (OrderedClosableIteratorProxy<Triple>)
        orderBySubject(allTriples);

    //return an UniqueSubjectIterator.
    return new UniqueSubjectIterator(orderedTriples);
  }
}
