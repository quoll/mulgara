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
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.resolver.spi;

import org.mulgara.store.statement.StatementStoreException;


/**
 * A {@link Resolver} that is currently hosting the System Graph.
 *
 * @created 2004-03-28
 * @author <a href="http://staff.pisoftware.com/andrae">Andrae Muys</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $
 * @maintenanceAuthor $Author: newmana $
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface SystemResolver extends Resolver, ResolverSession
{
  /**
   * Create the system model.  Used for bootstrapping the Database.
   */
  public void createSystemModel(long model, long modelTypeURI) throws ResolverException, LocalizeException;

  public boolean modelExists(long model) throws ResolverException, LocalizeException;

  /**
   * Informs the system resolver of the metadata it uses for managing data.
   * @param systemGraphNode The node for the system graph.
   * @param rdfTypeNode The node representing rdf:type.
   * @param systemGraphTypeNode The node used for the SystemResolver graph type.
   */
  public void initializeSystemNodes(long systemGraphNode, long rdfTypeNode, long systemGraphTypeNode) throws StatementStoreException;
}
