/*
 * Copyright 2008 Fedora Commons, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.krule.rlog;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mulgara.krule.rlog.ast.*;
import org.mulgara.krule.rlog.parser.TypeException;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.MulgaraGraphs;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;

/**
 * This class is used to interpret a string into an AST.  The AST is then transformed into a krule encoding in XML.
 * 
 * @created Feb 22, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class Rlog implements Interpreter {

  /** Logger. */
  private static final Logger logger = Logger.getLogger(Rlog.class.getName());

  /** The label used on all generated rules. */
  private static String ruleLabel = "rule";

  /** All the parsed statements to be processed. */
  private List<Statement> statements;

  /** All the parsed Axioms. */
  private List<Axiom> axioms;

  /** All the parsed Rules. */
  private List<Rule> rules;

  /**
   * Sets the label to use for each rule. The rule names are made up of this label
   * plus an incremening number.
   * @param label The text label to use on rules.
   */
  public static void setRuleLabel(String label) {
    ruleLabel = label;
  }

  /**
   * Create an rlog interpreter for building an AST from a stream Reader object.
   * @param input The stream Reader.
   * @param inputLocation The location the input comes from.
   * @throws IOException There was an IO Exception on the input.
   * @throws beaver.Parser.Exception There was a parser exception in the input data.
   * @throws URIParseException If the rules contain illegal URIs.
   */
  public Rlog(Reader input, URI inputLocation) throws IOException, ParseException, TypeException, URIParseException {
    Program program = loadProgram(input, inputLocation);
    statements = program.getStatements();

    // separate out the rules from the axioms
    int ruleCount = 0;
    rules = new ArrayList<Rule>();
    axioms = new ArrayList<Axiom>();
    for (Statement s: statements) {
      if (s instanceof Axiom) axioms.add((Axiom)s);
      else if (s instanceof Rule) {
        rules.add((Rule)s);
        ((Rule)s).setName(ruleLabel + ++ruleCount);
      } else throw new IllegalStateException("Unknown statement type found: " + s.getClass().getName());
    }

    // calculate dependencies between the rules
    calculateRuleDependencies();
  }


  /**
   * Loads a program from an input Reader.
   * @param input The Reader to get the program from.
   * @param baseLocation The location context to load the program in. This is used for relative
   *        import declartations.
   * @return The fully loaded program, including imports.
   * @throws IOException Due to an IO error while loading the program.
   * @throws ParseException If the program contains an error.
   */
  private Program loadProgram(Reader input, URI baseLocation) throws IOException, ParseException {
    // parse the rlog into statements
    RlogParser parser = new RlogParser(input);

    try {
      Program program = parser.getProgram();
      loadImports(program, baseLocation);
      return program;
    } catch (ParseException e) {
      logger.error("Error parsing program: " + e.getMessage());
      throw e;
    } catch (IOException e) {
      logger.error("IO Error reading program: " + e.getMessage());
      throw e;
    }
  }


  /**
   * Find all the variables in every rule.
   * @return A complete collection of all the variables that were parsed.
   */
  public Collection<Var> getVariables() {
    LinkedHashSet<Var> vars = new LinkedHashSet<Var>();
    for (Rule r: rules) vars.addAll(r.getVariables());
    return vars;
  }


  /**
   * Gets all the URIs referenced in the rules.
   * @return All URIs in order of appearance within axioms, then rules.
   * @throws URIParseException The referenced URIs had bad syntax.
   */
  public Set<URIReference> getReferences() throws URIParseException {
    Set<URIReference> refs = new LinkedHashSet<URIReference>();
    for (Axiom a: axioms) refs.addAll(a.getReferences());
    for (Rule r: rules) refs.addAll(r.getReferences());
    refs.addAll(MulgaraGraphs.getSpecialUriRefs());
    return refs;
  }


  /**
   * Get all the axioms appearing in the rule set.
   * @return A list of axioms.
   */
  public List<Axiom> getAxioms() {
    return Collections.unmodifiableList(axioms);
  }


  /**
   * Get all the rules appearing in the rule set.
   * @return A list of rules.
   */
  public List<Rule> getRules() {
    return Collections.unmodifiableList(rules);
  }


  /**
   * Determine which rules are dependent on the result of which other rules,
   * and set the rule objects accordingly.
   * @throws URIParseException If the rules contain illegal URIs.
   */
  private void calculateRuleDependencies() throws TypeException, URIParseException {
    for (Rule trigger: rules) {
      for (Rule potentialTarget: rules) {
        if (potentialTarget.triggeredBy(trigger)) trigger.addTrigger(potentialTarget);
      }
    }
  }


  /**
   * Load all imports into the given program.
   * @param prog The program to load its imports into.
   * @param currentLocation A URI for the location of the program. Used for relative loads.
   * @throws IOException If the import files cannot be read.
   * @throws ParseException If an imported program has an error.
   */
  private void loadImports(Program prog, URI currentLocation) throws IOException, ParseException {
    List<URI> imports = prog.getImports();
    for (URI imp: imports) {
      URL importLocation = makeAbsolute(currentLocation, imp);
      if (logger.isDebugEnabled()) logger.debug("Importing " + importLocation);
      Reader input = new InputStreamReader(importLocation.openStream());
      Program impProgram;
      try {
        impProgram = loadProgram(input, currentLocation);
      } catch (ParseException e) {
        logger.error("Error in imported program <" + importLocation + ">");
        throw e;
      } finally {
        input.close();
      }
      prog.merge(impProgram);
    }
  }


  /**
   * Calculate a URL from a given URI, relative to the current base location.
   * @param base The base to calculate the final URL from. Must be a URL if <var>rel</var> is relative.
   * @param rel The URL that is relative to the base. If absolute, then <var>base</var> is ignored.
   * @return A new URL, which is <var>rel</var> if it is absolute, or a merge of <var>rel</var> and
   *         <var>base</var> if <var>rel</var> is relative.
   * @throws IllegalArgumentException If <var>rel</var> is relative and <var>base</var> is not a URL,
   *         or if <var>rel</var> (or <var>base</var>+<var>rel</var>) is not a valid URL.
   */
  private URL makeAbsolute(URI base, URI rel) {
    try {
      if (!rel.isAbsolute()) {
        if (base == null) throw new IllegalArgumentException("Relative URL used in import with no current location set");
        return new URL(base.toURL(), rel.toString());
      } else {
        return rel.toURL();
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL for import of (" + rel + ")" + ": "+ e.getMessage());
    }
  }
}
