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

package org.mulgara.rules;

import java.net.URI;

/**
 * A dummy implementation of the rule loader.
 *
 * @created 2005-7-1
 * @author <a href="mailto:gearon@users.sourceforge.net">Paul Gearon</a>
 * @version $Revision: 1.1 $
 * @modified $Date: 2005/07/01 23:21:33 $
 * @maintenanceAuthor $Author: pgearon $
 * @copyright &copy; 2005 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class DummyRuleLoader implements RuleLoader {

  /**
   * Reads the ruleModel in the database and constructs the rules from it.
   *
   * @return Nothing.
   */
  public Rules readRules(Object session) throws InitializerException {
    throw new InitializerException("No rule loader available.");
  }

  /**
   * Placeholder implementation.
   *
   * @param ruleModel Ignored.
   * @param baseModel Ignored.
   * @param destModel Ignored.
   */
  public DummyRuleLoader(URI ruleModel, URI baseModel, URI destModel) {
    // No-op
  }

  /**
   * Factory method.
   *
   * @param ruleModel The name of the model with the rules to run.
   * @param baseModel The name of the model with the base data.
   * @param destModel The name of the model which will receive the entailed data.
   * @return A new DummyRuleLoader instance.
   */
  public static RuleLoader newInstance(URI ruleModel, URI baseModel, URI destModel) {
    return new DummyRuleLoader(ruleModel, baseModel, destModel);
  }

}
