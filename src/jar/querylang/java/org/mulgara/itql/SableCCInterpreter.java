/**
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */
package org.mulgara.itql;

import java.net.URI;
import java.net.URISyntaxException;

import org.mulgara.itql.node.PLiteral;
import org.mulgara.itql.node.TResource;
import org.mulgara.itql.node.Token;
import org.mulgara.parser.Interpreter;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.query.Variable;
import org.mulgara.query.rdf.LiteralImpl;

/**
 * Interpreter methods specific to sableCC structures.
 * 
 * @created Aug 24, 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface SableCCInterpreter extends Interpreter {

  /**
   * Construct a {@link LiteralImpl} from a {@link PLiteral}.
   *
   * @param p  the instance to convert
   */
  public LiteralImpl toLiteralImpl(PLiteral p);

  /**
   * Returns an anonymous variable unique for this interpreter.
   * Note: We really should introduce a new subclass of Variable
   * that is explicitly anonymous, but for now this will do.
   */
  public Variable nextAnonVariable();

  /**
   * Executes a query and returns its results.
   *
   * @param rawQuery a select query, represented as either a {@link
   *      org.mulgara.itql.node.ASelectCommand} or a {@link
   *      org.mulgara.itql.node.ASelectSetOfTriples}
   * @return the answer to the query
   * @throws QueryException if the query cannot be executed
   * @throws URISyntaxException if the <code>query</code> contains a resource
   *      whose text violates <a href="http://www.isi.edu/in-notes/rfc2396.txt">
   *      RFC\uFFFD2396</a>
   */
  public Query buildQuery(org.mulgara.itql.node.Node rawQuery) throws QueryException,
      URISyntaxException;

  /**
   * Convert SableCC-generated {@link TResource} tokens into {@link URI}s.
   *
   * Resolution will treat the token as an XML
   * <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified
   * names</a> if the <code>aliasMap</code> contains a key
   * for the URI scheme part that can be treated as an XML namespace prefix.
   * For example, <kbd>dc:title</kbd> is treated as a qname and mapped to the
   * to the URI <kbd>http://purl.org/dc/elements/1.1/title</kbd>, assuming the
   * aliasMap had an entry mapping <code>"dc"</code> to the Dublin
   * Core namespace.
   *
   * @param token  the token to be converted, which should actually be a
   *   {@link TResource}
   * @throws RuntimeException if the <var>token</var> text isn't syntactically
   *   a {@link URI}; this shouldn't ever occur, assuming the <var>token</var>
   *   is a {@link TResource}
   */
  URI toURI(Token token);

}
