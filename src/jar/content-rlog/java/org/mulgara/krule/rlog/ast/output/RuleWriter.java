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

package org.mulgara.krule.rlog.ast.output;

import org.mulgara.krule.rlog.ast.CheckRule;
import org.mulgara.krule.rlog.ast.Predicate;
import org.mulgara.krule.rlog.ast.Rule;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.RDFNode;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Writes variables to an XML stream as a set of declarations.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class RuleWriter extends XMLFragmentWriter {

  /** The default starting point for indentation in the body of a statement. */
  private static final int DEFAULT_BODY_INDENT = 5;

  /** The collection of variables that this class emits. */
  public Collection<Rule> rules;

  /**
   * Creates a new writer for a collection of rules.
   * @param rules The rules to be written.
   */
  public RuleWriter(Collection<Rule> rules) {
    this.rules = rules;
  }

  /** {@inheritDoc} */
  public void emit(PrintStream out) throws URIParseException {
    for (Rule r: rules) emitRule(out, r);
  }

  /**
   * Prints an entire Rule to a PrintStream.
   * @param out The PrintStream to send the rule to.
   * @param r The rule to print.
   */
  private void emitRule(PrintStream out, Rule r) throws URIParseException {
    if (r instanceof CheckRule) {
      out.println("  <krule:ConsistencyCheck rdf:about=\"#" + r.getName() + "\">");
    } else {
      out.println("  <krule:Rule rdf:about=\"#" + r.getName() + "\">");
      emitTriggers(out, r.getTriggers());
    }
    out.println("    <hasQuery>\n" +
                "      <Query>");

    if (r instanceof CheckRule) emitSelection(out, r.getVariables()); 
    else emitSelection(out, r.getHead());

    emitWhereClause(out, r.getBody(), r.getBodySubtractions());
    out.println("      </Query>\n" +
                "    </hasQuery>\n" +
                "  </krule:Rule>\n");
  }

  /**
   * Prints the triggers for a rule to a PrintStream.
   * @param out The PrintStream to send the triggered rules to.
   * @param triggers The rules that get triggered by the currently printing rule.
   */
  private void emitTriggers(PrintStream out, Collection<Rule> triggers) {
    for (Rule r: triggers) {
      out.println("    <triggers rdf:resource=\"#" + r.getName() + "\"/>");
    }
  }

  /**
   * Prints the head for a rule to a PrintStream.
   * @param out The PrintStream to send the selection to.
   * @param selection The selection that makes up the head of a rule.
   */
  private void emitSelection(PrintStream out, Predicate selection) throws URIParseException {
    emitSelection(out, Collections.singletonList(selection));
  }


  /**
   * Prints the head for a rule to a PrintStream.
   * @param out The PrintStream to send the selection to.
   * @param selection The list of predicates that makes up the head of a rule.
   */
  private void emitSelection(PrintStream out, List<Predicate> selection) throws URIParseException {
    List<RDFNode> sel = new ArrayList<RDFNode>();
    for (Predicate p: selection) {
      sel.add(p.getSubject());
      sel.add(p.getPredicate());
      sel.add(p.getObject());
    }
    emitSelection(out, sel);
  }


  /**
   * Prints the selection values to a PrintStream.
   * @param out The PrintStream to send the selection to.
   * @param sel The elements to be selected.
   */
  private void emitSelection(PrintStream out, Collection<? extends RDFNode> sel) throws URIParseException {
    out.println("        <selectionVariables>\n" +
                "          <rdf:Seq>");
    for (RDFNode s: sel) {
      out.println("            <rdf:li rdf:resource=\"" + s.getRdfLabel() + "\"/>");
    }
    out.println("          </rdf:Seq>\n" +
                "        </selectionVariables>");
  }


  /**
   * Prints the body for a rule to a PrintStream.
   * @param out The PrintStream to send the where clause to.
   * @param body The where clause that makes up the body of the rule.
   * @param subs The part of a where clause to be removed from the body.
   * @throws URIParseException If one of the URIs in the constraint expression has an invalid syntax.
   */
  private void emitWhereClause(PrintStream out, List<Predicate> body, List<Predicate> subs) throws URIParseException {
    out.println("        <hasWhereClause>");
    if (subs.isEmpty()) emitConjunction(out, body, DEFAULT_BODY_INDENT);
    else emitSubtractions(out, body, subs, DEFAULT_BODY_INDENT);
    out.println("        </hasWhereClause>");
  }

  private void emitSubtractions(PrintStream out, List<Predicate> body, List<Predicate> subs, int indent) throws URIParseException {
    int lastElt = subs.size() - 1;
    out.println(sp(indent) + "<Difference>\n" +
                sp(indent + 1) + "<minuend>");
    if (lastElt == 0) emitConjunction(out, body, indent + 2);
    else emitSubtractions(out, body, subs.subList(0, lastElt), indent + 2);
    out.println(sp(indent + 1) + "</minuend>\n" +
                sp(indent + 1) + "<subtrahend>");
    emitSimpleConstraint(out, subs.get(lastElt), indent + 2);
    out.println(sp(indent + 1) + "</subtrahend>\n" +
                sp(indent) + "</Difference>");
  }

  /**
   * Prints a list of constraints as the arguments to a single ConstraintConjunction.
   * @param out The PrintStream to send the constraint to.
   * @param conjunction The list of constraints that form the conjunction to be written.
   * @param indent The number of indentations needed for the expression to be written correctly.
   * @throws URIParseException If one of the URIs in the constraint has an invalid syntax.
   */
  private void emitConjunction(PrintStream out, List<Predicate> conjunction, int indent) throws URIParseException {
    if (conjunction.size() == 1) emitSimpleConstraint(out, conjunction.get(0), indent);
    else {
      out.println(sp(indent) + "<ConstraintConjunction>");
      for (Predicate arg: conjunction) {
        out.println(sp(indent + 1) + "<argument>");
        emitSimpleConstraint(out, arg, indent + 2);
        out.println(sp(indent + 1) + "</argument>");
      }
      out.println(sp(indent) + "</ConstraintConjunction>");
    }
  }

  /**
   * Prints a single constraint to a PrintStream.
   * @param out The PrintStream to send the constraint to.
   * @param constraint The constraint to write.
   * @param indent The indentation needed for the constraint to be written correctly.
   * @throws URIParseException If one of the URIs in the constraint has an invalid syntax.
   */
  private void emitSimpleConstraint(PrintStream out, Predicate constraint, int indent) throws URIParseException {
    out.println(sp(indent) + "<SimpleConstraint>\n" +
                sp(indent + 1) + "<hasSubject>\n" +
                sp(indent + 2) + nodeString(constraint.getSubject()) + "\n" +
                sp(indent + 1) + "</hasSubject>\n" +
                sp(indent + 1) + "<hasPredicate>\n" +
                sp(indent + 2) + nodeString(constraint.getPredicate()) + "\n" +
                sp(indent + 1) + "</hasPredicate>\n" +
                sp(indent + 1) + "<hasObject>\n" +
                sp(indent + 2) + nodeString(constraint.getObject()) + "\n" +
                sp(indent + 1) + "</hasObject>");
    if (constraint.hasGraphAnnotation()) {
      out.println(sp(indent + 1) + "<hasModel>\n" +
                  sp(indent + 2) + nodeString(constraint.getGraphAnnotation()) + "\n" +
                  sp(indent + 1) + "</hasModel>");
    }
    out.println(sp(indent) + "</SimpleConstraint>");
  }
  
  private String sp(int nr) {
    StringBuffer sb = new StringBuffer();
    for (int n = 0; n < nr; n++) sb.append("  ");
    return sb.toString();
  }

}
