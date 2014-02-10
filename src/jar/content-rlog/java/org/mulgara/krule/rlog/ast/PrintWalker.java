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
 * Visitor pattern class for traversing the AST and printing what it finds.
 * 
 * @created May 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 */
public class PrintWalker implements TreeWalker {
  // Axiom BPredicateLiteral Rule TypeLiteral Variable StringLiteral IntegerLiteral

  public void visit(Axiom node) {
    System.out.println("Local treewalker: Axiom");
    node.predicate.accept(this);
  }

  public void visit(Rule node) {
    System.out.println("Local treewalker: Rule");
    node.head.accept(this);
    for (Predicate p: node.body) p.accept(this);
  }

  public void visit(TypeStatement node) {
    System.out.println("Local treewalker: TypeStatement");
    node.typeLabel.accept(this);
    node.param.accept(this);
  }

  public void visit(InvertedPredicate node) {
    System.out.println("Local treewalker: InvertedPredicate");
    node.invertPredicate.accept(this);
  }

  public void visit(BPredicate node) {
    System.out.println("Local treewalker: BPredicate");
    node.label.accept(this);
    node.left.accept(this);
    node.right.accept(this);
  }

  public void visit(BPredicateLiteral node) {
    // leaf
  }

  public void visit(TypeLiteral node) {
    // leaf
  }

  public void visit(Variable node) {
    // leaf
  }

  public void visit(StringLiteral node) {
    // leaf
  }

  public void visit(IntegerLiteral node) {
    // leaf
  }

}

