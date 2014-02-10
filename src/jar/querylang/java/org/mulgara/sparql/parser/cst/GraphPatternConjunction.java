/*
 * Copyright 2008 Fedora Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser.cst;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a conjunction of patterns.
 *
 * @created Feb 19, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class GraphPatternConjunction extends BasicGraphPattern {

  /** The patterns to join */
  List<GroupGraphPattern> patterns = new ArrayList<GroupGraphPattern>();

  /**
   * Build a conjunction out of two GroupGraphPatterns.
   * @param lhs The first GroupGraphPattern
   * @param rhs The second GroupGraphPattern
   */
  public GraphPatternConjunction(GroupGraphPattern lhs, GroupGraphPattern rhs) {
    if (lhs instanceof GraphPatternConjunction && lhs.getFilter() == null && lhs.getGraph() == null) {
      patterns.addAll(((GraphPatternConjunction)lhs).patterns);
    } else patterns.add(lhs);

    if (rhs instanceof GraphPatternConjunction && rhs.getFilter() == null && rhs.getGraph() == null) {
      patterns.addAll(((GraphPatternConjunction)rhs).patterns);
    } else patterns.add(rhs);
  }

  /**
   * Build a conjunction by expanding on an existing conjunction.
   * @param lhs The existing conjunction
   * @param rhs The pattern to be appended to the lhs
   */
  public GraphPatternConjunction(GraphPatternConjunction lhs, GroupGraphPattern rhs) {
    patterns.addAll(lhs.patterns);
    patterns.add(rhs);
  }

  /**
   * Build a conjunction by prepending on an existing conjunction.
   * @param lhs The existing conjunction
   * @param rhs The pattern to be appended to the lhs
   */
  public GraphPatternConjunction(GroupGraphPattern lhs, GraphPatternConjunction rhs) {
    patterns.add(lhs);
    patterns.addAll(rhs.patterns);
  }

  /**
   * Build a conjunction from two conjunctions.  Slightly better than
   *   @see #GraphPatternConjunction(GroupGraphPattern, GroupGraphPattern)
   * @param lhs The first conjunction
   * @param rhs The pattern to be appended to the lhs
   */
  public GraphPatternConjunction(GraphPatternConjunction lhs, GraphPatternConjunction rhs) {
    patterns.addAll(lhs.patterns);
    patterns.addAll(rhs.patterns);
  }

  /**
   * Build a conjunction from a triples list.
   * @param triples The list to turn into a conjunction
   */
  public GraphPatternConjunction(TripleList triples) {
    patterns.addAll(triples.getElements());
  }

  /**
   * Build a conjunction by appending a list of triples to an existing conjunction
   * @param lhs The first conjunction
   * @param triples The list to be appended
   */
  public GraphPatternConjunction(GraphPatternConjunction lhs, TripleList triples) {
    patterns.addAll(lhs.patterns);
    patterns.addAll(triples.getElements());
  }

  /**
   * @see org.mulgara.sparql.parser.cst.BasicGraphPattern#getElements()
   */
  @Override
  public List<GroupGraphPattern> getElements() {
    return patterns;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.BasicGraphPattern#getImage()
   */
  @Override
  public String getImage() {
    StringBuffer result = new StringBuffer();
    result.append(patterns.get(0).getImage());
    for (int i = 1; i < patterns.size(); i++) result.append(" . ").append(patterns.get(i).getImage());
    return addPatterns(result.toString());
  }

}
