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
 * This class contains some simple common functionality for data that appears
 * in graph patterns.
 *
 * @created Feb 19, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public abstract class BasicGraphPattern implements GroupGraphPattern {
  
  /** The graph that this pattern is to match on */
  Expression graph;

  /** The filter to apply to this pattern */
  Expression filter;

  /**
   * Creates the basic pattern, with no specific graph.
   */
  BasicGraphPattern() {
    graph = null;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#getElements()
   */
  public abstract List<? extends GroupGraphPattern> getElements();

  /**
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#getAllTriples()
   */
  public List<GroupGraphPattern> getAllTriples() {
    // accumulate all triples into a single list
    List<GroupGraphPattern> result = new ArrayList<GroupGraphPattern>();
    for (GroupGraphPattern p: getElements()) result.addAll(p.getAllTriples());
    return result;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#setGraph(org.mulgara.sparql.parser.cst.Expression)
   */
  public void setGraph(Expression g) {
    graph = g;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#getGraph()
   */
  public Expression getGraph() {
    return graph;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#setFilter(org.mulgara.sparql.parser.cst.Expression)
   */
  public void setFilter(Expression f) {
    if (filter == null) filter = f;
    else {
      filter = new AndExpression(filter, f);
    }
  }

  /**
   * @see org.mulgara.sparql.parser.cst.GroupGraphPattern#getFilter()
   */
  public Expression getFilter() {
    return filter;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public abstract String getImage();
  
  /**
   * Update the pattern with any GRAPH or FILTER patterns
   * @param image The original image of this graph pattern
   * @return An updated string for this pattern
   */
  String addPatterns(String image) {
    if (graph != null) image = " GRAPH " + graph.getImage() + " {" + image + " }";
    if (filter != null) image += " FILTER (" + filter.getImage() + ")";
    return image;
  }

}
