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

import java.util.List;

import org.mulgara.krule.rlog.Interpreter;
import org.mulgara.krule.rlog.ParseException;
import org.mulgara.krule.rlog.parser.URIParseException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.store.nodepool.NodePoolException;

/**
 * Converts a set of rules to triples.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class KruleGenerator {

  /** The RLog interpreter that contains the data to be generated. */
  private Interpreter interpreter;

  /** The session for localizing data. */
  private ResolverSession resolverSession;

  /**
   * Construct a generator for writing Krule triples files.
   * @param interpreter The object that gets the data.
   * @throws LocalizeException If the required graph nodes could not be found.
   */
  public KruleGenerator(Interpreter interpreter, ResolverSession resolverSession) throws LocalizeException {
    this.interpreter = interpreter;
    this.resolverSession = resolverSession;
  }

  // inheritdoc
  public List<long[]> emit(List<long[]> triples) throws ParseException, NodePoolException {

    // pull in all the elements, and give them to generators
    TripleGenerator refWriter;
    try {
      refWriter = new ReferenceGenerator(interpreter.getReferences(), resolverSession);
      TripleGenerator axiomWriter = new AxiomGenerator(interpreter.getAxioms(), resolverSession);
      TripleGenerator ruleWriter = new RuleGenerator(interpreter.getRules(), resolverSession);
      
      refWriter.emit(triples);
      axiomWriter.emit(triples);
      ruleWriter.emit(triples);
    } catch (LocalizeException e) {
      throw new NodePoolException("Unable to access local nodes", e);
    } catch (URIParseException e) {
      throw new ParseException("Invalid URI. " + e.getMessage());
    }
    return triples;
  }

}
