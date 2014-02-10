/*
 * Copyright 2008 The Topaz PRoject
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

package org.mulgara.krule.rlog.ast;

import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.krule.rlog.rdf.RDFNode;

/**
 * A parameter for a predicate.
 *
 * @created 2007
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Topaz Project</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public interface PredicateParam extends Comparable<PredicateParam> {
  public void accept(TreeWalker walker);
  public void print(int indent);
  public RDFNode getRDFNode() throws URIParseException;
  public int orderId();

  // Identifiers for ordering subtypes
  static final int PREDICATE_LITERAL_ID = 1;
  static final int STRING_LITERAL_ID = 2;
  static final int INTEGER_LITERAL_ID = 3;
  static final int VARIABLE_ID = 4;

}

