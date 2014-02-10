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
 * Represents a disjunction of patterns.
 *
 * @created Feb 19, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class GraphPatternDisjunction extends BasicGraphPattern {

  /** The patterns to union */
  List<GroupGraphPattern> patterns = new ArrayList<GroupGraphPattern>();

  /**
   * Build a disjunction out of two GroupGraphPatterns.
   * @param lhs The first GroupGraphPattern
   * @param rhs The second GroupGraphPattern
   */
  public GraphPatternDisjunction(GroupGraphPattern lhs, GroupGraphPattern rhs) {
    patterns.add(lhs);
    patterns.add(rhs);
  }

  /**
   * Build a disjunction by expanding on an existing disjunction.
   * @param lhs The existing disjunction
   * @param rhs The pattern to be appended to the lhs
   */
  public GraphPatternDisjunction(GraphPatternDisjunction lhs, GroupGraphPattern rhs) {
    patterns.addAll(lhs.patterns);
    patterns.add(rhs);
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
    StringBuffer result = new StringBuffer(" {");
    result.append(patterns.get(0).getImage()).append(" }");
    for (int i = 1; i < patterns.size(); i++) result.append(" UNION { ").append(patterns.get(i).getImage()).append(" }");
    return addPatterns(result.toString());
  }

}
