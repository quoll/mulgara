/*
 * Copyright 2010 Paul Gearon
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

package org.mulgara.content.rdfa;

import org.apache.log4j.Logger;      // Apache Log4J

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

import net.rootdev.javardfa.Resolver;

/**
 *
 * @created 2010-08-09
 * @author Paul Gearon
 */
class BasedResolver implements Resolver {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(BasedResolver.class.getName());

  /** The file: scheme which we have to work around. */
  private static final String FILE_SCHEME = "file:";

  /** The factory for creating new IRIs. */
  private final IRIFactory iriFactory;

  /** The main IRI for determining relative IRIs in relation to. */
  private IRI base;

  /** The string if the IRI that is the base. */
  private String baseStr;

  /**
   * Constructs a resolver with just the base.
   * @param baseStr The string for the IRI to use as the base of relative IRIs.
   */
  public BasedResolver(String baseStr) {
    this(baseStr, IRIFactory.semanticWebImplementation());
  }

  /**
   * Constructs a resolver.
   * @param baseStr The string for the IRI to use as the base of relative IRIs.
   * @param iriFactory The factory for creating IRIs from.
   */
  public BasedResolver(String baseStr, IRIFactory iriFactory) {
    this.iriFactory = iriFactory;
    setBase(baseStr);
  }

  /**
   * Changes the base to use.
   * @param baseStr The new base. <code>null</code> will be ignored.
   */
  public void setBase(String baseStr) {
    if (baseStr != null) {
      // this is a hack to avoid a problem with IRIs
      if (baseStr.startsWith(FILE_SCHEME)) baseStr = baseStr.replaceFirst("file:", "http://localhost");
      this.baseStr = baseStr;
      base = iriFactory.construct(baseStr);
    }
  }


  /**
   * Resolves a IRI relative to a given base.
   * @param baseStr A string form of the base IRI. Expected to be the same as the current base.
   * @param rel An IRI that may be relative to the base.
   * @return a string containing the lexical form of the calculated IRI.
   */
  public String resolve(String baseStr, String rel) {
    IRI bIri = base;
    // test if a different base string to the one we expect is being used
    if (baseStr != null && !baseStr.equals(this.baseStr)) {
      bIri = iriFactory.construct(baseStr);
    }
    IRI resolved = bIri.resolve(rel);
    return resolved.toString();
  }

}
