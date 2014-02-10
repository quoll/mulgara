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

package org.mulgara.krule.rlog.ast;

/**
 * Created by IntelliJ IDEA.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public interface TreeWalker {

  void visit(Axiom node);

  void visit(Rule node);

  void visit(TypeStatement node);

  void visit(InvertedPredicate node);

  void visit(BPredicate node);

  void visit(BPredicateLiteral node);

  void visit(TypeLiteral node);

  void visit(Variable node);

  void visit(StringLiteral node);

  void visit(IntegerLiteral node);
}
