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
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): 
 *   The copywrite in this file is held by:
 *     The Australian Commonwealth Government
 *     Department of Defense
 *   Developed by Netymon Pty Ltd
 *   under contract 4500430665
 *   contributed to the Mulgara Project under the
 *     Mozilla Public License version 1.1
 *   per clause 4.1.3 of the above contract.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.store.tuples;

// JDK packages
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// Third party packages
// Locally written packages
import org.mulgara.query.Variable;

/**
 * Indicates that the specified Tuples needs to 
 *
 * @created 2005-05-04
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.2 $
 * @modified $Date: 2005/06/03 13:57:47 $ by $Author: amuys $
 * @maintenanceAuthor $Author: amuys $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class MandatoryBindingAnnotation implements Annotation {
  private Set<Variable> variables;

  public MandatoryBindingAnnotation(Collection<Variable> variables) {
    this.variables = new HashSet<Variable>(variables);
  }

  public MandatoryBindingAnnotation(Variable[] varArray) {
    this(Arrays.asList(varArray));
  }

  public Set<Variable> requiredVariables() {
    return variables;
  }

  public boolean meetsRequirement(Set<Variable> boundVars) {
    Iterator<Variable> i = variables.iterator();
    while (i.hasNext()) {
      if (!boundVars.contains(i.next())) {
        return false;
      }
    }

    return true;
  }
}
