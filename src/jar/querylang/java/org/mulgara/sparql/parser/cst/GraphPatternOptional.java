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

import java.util.LinkedList;
import java.util.List;


/**
 * Represents an optional join between patterns.
 *
 * @created Feb 20, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class GraphPatternOptional extends BasicGraphPattern {

  /** The main pattern */
  GroupGraphPattern main;

  /** The optional pattern */
  GroupGraphPattern optional;

  /**
   * Build an optional join out of two GroupGraphPatterns.
   * @param main The main pattern to be matched
   * @param optional The pattern to be optionally matched
   */
  public GraphPatternOptional(GroupGraphPattern main, GroupGraphPattern optional) {
    this.main = main;
    this.optional = optional;
  }

  /**
   * @return the main pattern
   */
  public GroupGraphPattern getMain() {
    return main;
  }

  /**
   * @return the optional pattern
   */
  public GroupGraphPattern getOptional() {
    return optional;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.BasicGraphPattern#getElements()
   */
  @Override
  public List<GroupGraphPattern> getElements() {
    List<GroupGraphPattern> result = new LinkedList<GroupGraphPattern>();
    result.add(main);
    result.add(optional);
    return result;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.BasicGraphPattern#getImage()
   */
  @Override
  public String getImage() {
    return addPatterns(main.getImage() + " OPTIONAL { " + optional.getImage() + " }");
  }

}
