/*
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

package org.mulgara.krule.rlog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mulgara.krule.rlog.ast.CanonicalStatement;
import org.mulgara.krule.rlog.ast.Statement;

/**
 * Represents a complete program, including annotations.
 * A program is mostly a list of statements, but can also include other elements
 * such as &quot;imports&quot; of other programs.
 *
 * @created Mar 3, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Program {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(Program.class.getName());

  /** All the statement that make up the program */
  private List<Statement> statements;

  /** The canonical forms of the statements that make up the program */
  private List<CanonicalStatement> canonicalStatements;

  /** The list of imports */
  private List<URI> imports;

  /**
   * Constructs an empty program.
   */
  public Program() {
    statements = new ArrayList<Statement>();
    canonicalStatements = new ArrayList<CanonicalStatement>();
    imports = new ArrayList<URI>();
  }


  /**
   * Retrieve the statements from this program.
   * @return A list of statements in the program.
   */
  public List<Statement> getStatements() {
    return statements;
  }


  /**
   * Merges the statements from another program into this one.
   * Note that the parsing context of the foreign statements is maintained.
   * @param p The foreign program to merge.
   */
  public void merge(Program p) {
    logger.debug("Merging programs");
    for (Statement s: p.getStatements()) add(s);
  }


  /**
   * Adds a statement to this program. If the statement is already present it is skipped.
   * @param s The statement to add.
   */
  public void add(Statement s) {
    CanonicalStatement canonical = s.getCanonical();
    if (!statementPresent(s, canonical)) {
      statements.add(s);
      canonicalStatements.add(canonical);
    } else {
      logger.debug("Not adding: " + s);
    }
  }


  /**
   * Adds an import directive to the program.
   */
  public void addImport(String imp) throws ParseException {
    try {
      URI u = new URI(imp);
      imports.add(u);
    } catch (URISyntaxException e) {
      throw new ParseException("Bad URL in import");
    }
  }


  /**
   * Get all the import URIs for the document
   * @return A List of URIs. These can be absolute or relative.
   */
  public List<URI> getImports() {
    return imports;
  }


  /**
   * Get an iterator for the statements in this program.
   * @return A new iterator that returns statements.
   */
  public Iterator<Statement> stmtIterator() {
    return statements.iterator();
  }

  /** @see java.lang.Object#toString() */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Statement s: statements) {
      sb.append(s).append("\n");
    }
    return sb.toString();
  }

  /**
   * Tests if a statement is already present in the program.
   * Attempts to canonicalize statements for comparison.
   * @param stmt The statement to look for. Included for logging purposes.
   * @param canonical The canonicalized form of the statement to look for.
   * @return <code>true</code> if an equivalent statement is found, <code>false</code> otherwise.
   */
  private boolean statementPresent(Statement stmt, CanonicalStatement canonical) {
    if (logger.isDebugEnabled()) logger.debug("Testing for presence of: " + stmt);
    for (CanonicalStatement s: canonicalStatements) {
      if (canonical.equals(s)) {
        if (logger.isDebugEnabled()) logger.debug(canonical.toString() + " == " + s);
        return true;
      }
      if (logger.isDebugEnabled()) logger.debug(canonical.toString() + " != " + s);
    }
    if (logger.isDebugEnabled()) logger.debug("New statement: " + stmt);
    return false;
  }
}
