/*
 * $Header: /home/pgearon/cvs/mulgara/mulgara-1.1/src/jar/client-jrdf/java/org/mulgara/client/jrdf/util/ItqlQueryUtil.java,v 1.8 2005/01/05 04:57:37 newmana Exp $
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

//Java 2 packages
import java.io.*;
import java.net.*;
import java.util.Iterator;

//JRDF packages
import org.jrdf.graph.*;

//Mulgara packages
import org.mulgara.itql.*;
import org.mulgara.parser.MulgaraLexerException;
import org.mulgara.parser.MulgaraParserException;
import org.mulgara.query.*;
import org.mulgara.query.rdf.*;
import org.mulgara.server.Session;


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
public abstract class ItqlQueryUtil {

  /** Special <mulgara:is> predicate */
  private static final String MULGARA_IS = "<" + Mulgara.NAMESPACE + "is> ";

  /**
   * Returns an Answer for the executed query.
   *
   * @param query String
   * @param interpreter ItqlInterpreter
   * @param session Session
   * @throws GraphException
   * @return Answer
   */
  public static Answer executeQuery(String query, TqlInterpreter interpreter,
                                    Session session) throws GraphException {

    try {

      //parse and execute
      Query parsedQuery = interpreter.parseQuery(query);
      return session.query(parsedQuery);
    }
    catch (QueryException queryException) {

      throw new GraphException("Could not execute query: " + query,
                               queryException);
    }
    catch (MulgaraLexerException lexerException) {

      throw new GraphException("Could not parse query: " + query,
                               lexerException);
    }
    catch (MulgaraParserException parserException) {

      throw new GraphException("Could not parse query: " + query,
                               parserException);
    }
    catch (IOException ioException) {

      throw new GraphException("Could not parse query: " + query, ioException);
    }
  }

  /**
   * Executes an iTQL command/query and closes the Answer.
   *
   * @param command String
   * @param interpreter ItqlInterpreter
   * @param session Session
   * @throws GraphException
   */
  public static void executeCommand(String command, TqlInterpreter interpreter,
                                    Session session) throws GraphException {

    Answer answer = null;

    try {

      answer = executeQuery(command, interpreter, session);
    }
    finally {

      //close the answer
      if (answer != null) {

        try {

          answer.close();
        }
        catch (TuplesException tuplesException) {

          throw new GraphException("Could not close Answer.", tuplesException);
        }
      }
    }
  }

  /**
   * Creates a query to find all triple using the triple as a constraint.
   *
   * @param triple Triple
   * @param modelURI String
   * @return String
   * @throws GraphException
   */
  public static String getSelectQuery(Triple triple, String modelURI)
      throws GraphException {

    StringBuffer query = new StringBuffer("");

    //get the select, from, where part
    query.append(getSelectQueryBase(modelURI));

    //add constraints
    query.append(getConstraint("s", triple.getSubject()));
    query.append(getConstraint("p", triple.getPredicate()));
    query.append(getConstraint("o", triple.getObject()));

    //end with a semi-colon
    query.append("; ");

    return query.toString();
  }

  /**
   * Returns the query part: <pre>
   *    select $s $p $o
   *    from <modelURI>
   *    where $s $p $o
   *  </pre>
   *
   * @return String
   * @param modelURI String
   */
  public static String getSelectQueryBase(String modelURI) {

    return "select $s $p $o from <" + modelURI + "> where $s $p $o ";
  }

  /**
   * Returns the query part: <pre>
   *    delete
   *      <subjectURI> <predicateURI> <objectURI>
   *      <subjectURI> <predicateURI> 'object literal'^^<datatypeURI>
   *    from <modelURI> ;
   *  </pre>
   *
   * @return String
   * @param triples Iterator
   * @param modelURI String
   * @throws GraphException
   */
  public static String getDeleteQuery(Iterator<Triple> triples, String modelURI) throws GraphException {

    //value to be returned
    StringBuffer query = new StringBuffer("");

    //there must be triples to insert
    if ((triples != null) && (triples.hasNext())) {

      query.append("delete ");

      //add each triple in the iterator
      query.append(getTripleSetQuery(triples));

      //add the model expression
      query.append("from <");
      query.append(modelURI);
      query.append("> ; ");
    }

    return query.toString();
  }

  /**
   * Returns the query part: <pre>
   *    insert
   *      <subjectURI> <predicateURI> <objectURI>
   *      <subjectURI> <predicateURI> 'object literal'^^<datatypeURI>
   *    into <modelURI> ;
   *  </pre>
   *
   * @return String
   * @param triples Iterator
   * @param modelURI String
   * @throws GraphException
   */
  public static String getInsertQuery(Iterator<Triple> triples, String modelURI) throws
      GraphException {

    //value to be returned
    StringBuffer query = new StringBuffer("");

    //there must be triples to insert
    if ((triples != null) && (triples.hasNext())) {

      query.append("insert ");

      //add each triple in the iterator
      query.append(getTripleSetQuery(triples));

      //add the model expression
      query.append("into <");
      query.append(modelURI);
      query.append("> ; ");
    }

    return query.toString();
  }

  /**
   * Returns a query part representing a set of triples.
   *
   * eg.
   * <pre>
   *   <subjectURI> <predicateURI> <objectURI>
   *   <subjectURI> <predicateURI> 'object literal'
   * </pre>
   *
   * @param triples ClosableIterator
   * @throws GraphException
   * @return String
   */
  private static String getTripleSetQuery(Iterator<Triple> triples) throws GraphException {

    //value to be returned
    StringBuffer tripleSet = new StringBuffer();

    if (triples != null) {

      //append each
      while (triples.hasNext()) {

        tripleSet.append(getTripleString(triples.next()));
      }
    }

    return tripleSet.toString();
  }

  /**
   * Returns a string representation of the Triple for use in a query.
   *
   * @return String
   * @param triple Triple
   * @throws GraphException
   */
  public static String getTripleString(Triple triple) throws GraphException {

    //validate
    if (triple == null) {

      throw new GraphException("Triple is null.");
    }

    //eg. <BlankNode> <URIReference> 'literal'@language
    return getEscapedForm(triple.getSubject()) + " " +
        getEscapedForm(triple.getPredicate()) + " " +
        getEscapedForm(triple.getObject()) + " ";
  }

  /**
   * Returns the query part: <pre>
   *    and $var <mulgara:is> <Node>
   *  </pre>
   *
   * @param var String
   * @param node Node
   * @return String
   * @throws GraphException
   */
  private static String getConstraint(String var, Node node) throws GraphException {

    StringBuffer constraint = new StringBuffer("");

    //validate
    if (node instanceof BlankNode) {

      throw new GraphException("Cannot create constraint for a BlankNode: " +
                               node);
    }

    //nulls are unconstrained (return empty constraint)
    if (node != null) {

      constraint.append("and $");
      constraint.append(var);
      constraint.append(" ");
      constraint.append(MULGARA_IS);
      constraint.append(getEscapedForm(node));
    }

    return constraint.toString();
  }

  /**
   * Returns a String representation for the Node. eg. <pre>
   *    (URIReference):  <nodeURI>
   *    (Literal):       'literal'^^<datatypeURI>
   *    (BlankNode):     node_123
   *  </pre>
   *
   * @param node Node
   * @return String
   * @throws GraphException
   */
  public static String getEscapedForm(Node node) throws GraphException {

    String escaped = "";

    //validate
    if (node == null) {

      throw new GraphException("Node is null.");
    }

    //determine type and call method
    if (node instanceof URIReference) {

      escaped = getEscapedForm( (URIReference) node);
    }
    else if (node instanceof Literal) {

      escaped = getEscapedForm( (Literal) node);
    }
    else if (node instanceof BlankNode) {

      escaped = getEscapedForm( (BlankNode) node);
    }
    else {

      throw new GraphException("Unknown node type: " + node.getClass().getName() +
                               ". Node must be either: URIReference, Literal " +
                               "or BlankNode.");
    }

    return escaped;
  }

  /**
   * Returns a String representation for the URIReference.
   *
   * @param node Node
   * @return String
   */
  private static String getEscapedForm(URIReference node) {

    return "<" + node.getURI() + "> ";
  }

  /**
   * Returns a String representation for the BlankNode.
   *
   * @param node Node
   * @return String
   */
  private static String getEscapedForm(BlankNode node) {

    return node.toString();
  }

  /**
   * Returns a String representation for the Literal.
   *
   * @param node Node
   * @return String
   */
  private static String getEscapedForm(Literal node) {

    StringBuffer escaped = new StringBuffer("");

    String literal = node.getLexicalForm();
    String language = node.getLanguage();
    URI datatype = node.getDatatypeURI();

    //start with basic 'literal'
    escaped.append('\'');
    escaped.append(literal);
    escaped.append('\'');

    //add language or datatype (if specified)
    if ( (language != null)
        && (!"".equals(language))) {

      //add language
      escaped.append('@');
      escaped.append(language);
    }
    else if (datatype != null) {

      //add datatype
      escaped.append("^^<");
      escaped.append(datatype.toString());
      escaped.append(">");
    }

    //add a space to the end
    escaped.append(' ');

    return escaped.toString();
  }

}
