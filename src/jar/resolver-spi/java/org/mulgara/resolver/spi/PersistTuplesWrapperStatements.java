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

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import gnu.trove.TLongLongHashMap;

// Local packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;
import org.mulgara.store.tuples.Tuples;

/**
 * A wrapper around {@link Tuples} to turn them into {@link Statements}.
 *
 * @created 2004-05-01
 * @author <a href="http://staff.tucanatech.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public class PersistTuplesWrapperStatements extends TuplesWrapperStatements
{
  /** Logger.  */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(PersistTuplesWrapperStatements.class.getName());

  ResolverSession resolverSession;
  
  TLongLongHashMap nodeMap;

  //
  // Constructors
  //

  public PersistTuplesWrapperStatements(Tuples   tuples,
                                 Variable subject,
                                 Variable predicate,
                                 Variable object,
                                 ResolverSession resolverSession) throws TuplesException
  {
    super(tuples, subject, predicate, object);

    this.resolverSession = resolverSession;
    this.nodeMap = new TLongLongHashMap();
  }

  public long getColumnValue(int column) throws TuplesException
  {
    try {
      long value = tuples.getColumnValue(column);
      if (value > 0) {
        return value;
      }
      if (nodeMap.containsKey(value)) {
        return nodeMap.get(value);
      }

      Node node = resolverSession.globalize(value);
      long newValue = resolverSession.localizePersistent(node);
      nodeMap.put(value, newValue);

      return newValue;
    } catch (LocalizeException el) {
      throw new TuplesException("Unable to localize value", el);
    } catch (GlobalizeException eg) {
      throw new TuplesException("Unable to globalize value", eg);
    }
  }
}
