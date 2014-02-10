/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Northrop Grumman Corporation. All Rights Reserved.
 *
 * This file is an original work and contains no Original Code.  It was
 * developed by Netymon Pty Ltd under contract to the Australian 
 * Commonwealth Government, Defense Science and Technology Organisation
 * under contract #4500507038 and is contributed back to the Kowari/Mulgara
 * Project as per clauses 4.1.3 and 4.1.4 of the above contract.
 *
 * Contributor(s): N/A.
 *
 * Copyright:
 *   The copyright on this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 * Copyright (C) 2006
 * The Australian Commonwealth Government
 * Department of Defense
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.itql;

// Java 2 standard packages
import java.util.ArrayList;
import java.util.List;

// Third party packages
import org.apache.log4j.Logger; 

// Generated packages
import org.mulgara.itql.analysis.*;
import org.mulgara.itql.node.*;

/**
 * Create a list of predicates and their associated object lists from a compound constraint.
 *
 * @created 2006-04-07
 *
 * @author Andrae Muys
 *
 * @company <a href="mailto:mail@netymon.com">Netymon Pty Ltd</a>
 *
 * @copyright &copy;2006 Australian Commonwealth Government.
 *      All rights reserved.
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class CompoundPredListBuilder extends AnalysisAdapter {

  private final static Logger logger = Logger.getLogger(CompoundPredListBuilder.class.getName());

  private List<CompoundPredicate> predLists;

  /**
   * Constructor.
   */
  public CompoundPredListBuilder() {
    predLists = new ArrayList<CompoundPredicate>();
  }

  public List<CompoundPredicate> getPredLists() {
    return predLists;
  }

  /**
   * Used to accumulate predicate lists.
   */
  private void addPredicateList(CompoundPredicate plist) {
    predLists.add(plist);
  }

  public void caseAColonExistsExpression(AColonExistsExpression expr) {
    if (logger.isDebugEnabled()) logger.debug("Found a colon exists expression: " + expr);
    expr.getExistsExpression().apply(this);
    expr.getExistsPterm().apply(this);
  }

  public void caseAEtermExistsExpression(AEtermExistsExpression expr) {
    if (logger.isDebugEnabled()) logger.debug("Found a colon exists expression: " + expr);
    expr.getExistsPterm().apply(this);
  }

  public void caseAPtermExistsPterm(APtermExistsPterm rawTerm) {
    if (logger.isDebugEnabled()) logger.debug("Found an Existential Predicate Term: " + rawTerm);

    ObjectListBuilder builder = new ObjectListBuilder();
    rawTerm.getExistsOterm().apply(builder);

    this.addPredicateList(new CompoundPredicate(rawTerm.getPredicate(), builder.getObjectList()));
  }

  public void defaultCase(Node node) {
    if (logger.isDebugEnabled()) logger.debug("Found node in pred builder: " + node + "::" + node.getClass());
  }
}

