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

import java.util.List;
import java.util.Collection;
import java.util.Set;

import org.mulgara.krule.rlog.ast.*;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.URIReference;
import org.mulgara.krule.rlog.rdf.Var;

/**
 * This interface is used to interpret a string into an AST.  The AST is then transformed into a krule encoding in XML.
 *
 * @created Feb 22, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface Interpreter {


  /**
   * Find all the variables in every rule.
   * @return A complete collection of all the variables that were parsed.
   */
  public Collection<Var> getVariables();

  /**
   * Gets all the URIs referenced in the rules.
   * @return All URIs in order of appearance within axioms, then rules.
   * @throws URIParseException The referenced URIs had bad syntax.
   */
  public Set<URIReference> getReferences() throws URIParseException;

  /**
   * Get all the axioms appearing in the rule set.
   * @return A list of axioms.
   */
  public List<Axiom> getAxioms();

  /**
   * Get all the rules appearing in the rule set.
   * @return A list of rules.
   */
  public List<Rule> getRules();

}
