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

package org.mulgara.krule.rlog.rdf;

import java.net.URI;

/**
 * An RDF literal.
 *
 * @created May 2, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="http://www.opensource.org/licenses/apache2.0.php">Apache License, Version 2.0</a>
 */
public class Literal implements RDFNode {

  private final String lexical;
  
  private final String language;
  
  private final boolean isSimple;
  
  private final URI type;

  public Literal(String lexical) {
    this.lexical = lexical;
    language = "";
    isSimple = true;
    type = null;
  }

  public Literal(String lexical, String language) {
    this.lexical = lexical;
    this.language = language;
    isSimple = true;
    type = null;
  }

  public Literal(String lexical, URIReference type) {
    this.lexical = lexical;
    this.language = "";
    isSimple = false;
    this.type = type.getURI();
  }

  /** @see org.mulgara.krule.rlog.rdf.RDFNode#isVariable() */
  public boolean isVariable() {
    return false;
  }

  /** @see org.mulgara.krule.rlog.rdf.RDFNode#isReference() */
  public boolean isReference() {
    return false;
  }

  /** Get the lexical reference. */
  public String getLexical() {
    return lexical;
  }

  /** {@inheritDoc} */ // TODO: consider changing rule system to handle this in the head.
  public String getRdfLabel() {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Literal)) return false;
    Literal l = (Literal)o;
    if (isSimple) return lexical.equals(l.lexical) && language.equals(l.language);
    else return lexical.equals(l.lexical) && type.equals(l.type);
  }

  /** {@inheritDoc} */
  public int hashCode() {
    return lexical.hashCode() * 7 + language.hashCode() * 13 + type.hashCode() * 17 + (isSimple ? 1 : 0);
  }
}
