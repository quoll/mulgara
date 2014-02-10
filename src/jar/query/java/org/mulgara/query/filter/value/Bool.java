/**
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
package org.mulgara.query.filter.value;

import java.net.URI;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.Context;
import static org.mulgara.query.rdf.XSD.BOOLEAN_URI;

/**
 * A boolean value.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class Bool extends TypedLiteral {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -1139308134744630943L;

  /** xsd:boolean */
  public static final URI TYPE = BOOLEAN_URI;

  /** xsd:boolean as an IRI */
  public static final IRI IRI_TYPE = new IRI(BOOLEAN_URI);

  /** A single boolean value for true */
  public static final Bool TRUE = new Bool(Boolean.TRUE);

  /** A single boolean value for false */
  public static final Bool FALSE = new Bool(Boolean.FALSE);

  /**
   * Creates the value to wrap the boolean
   * @param b The boolean to wrap
   */
  public Bool(Boolean b) {
    super(b, TYPE);
  }

  /** {@inheritDoc} */
  public boolean test(Context context) throws QueryException {
    return (Boolean)value;
  }
}
