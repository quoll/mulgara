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

package org.mulgara.store.tuples;

// Local packages
import org.mulgara.query.TuplesException;

/**
 * Centralizes the generation of {@link Tuples} instances.
 *
 *
 * @created 2003-02-03
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2003 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public abstract class TuplesFactory {

  /**
   * The system property key to set to override the default tuples factory
   * class.
   */
  public static final String FACTORY_CLASS = "TuplesFactoryClass";

  /**
   * Singleton factory instance.
   */
  private static TuplesFactory tuplesFactory = null;

  /**
   * @return RETURNED VALUE TO DO
   * @throws Error if the factory isn't correctly configured
   */
  public static TuplesFactory newInstance() throws Error {

    if (tuplesFactory == null) {

      try {

        String tuplesFactoryClass = System.getProperty(FACTORY_CLASS);
        if ((tuplesFactoryClass == null) || (tuplesFactoryClass.length() <= 0)) {
          tuplesFactoryClass = "org.mulgara.store.xa.HybridTuplesFactory";
        }

        //"org.mulgara.store.tuples.MemoryTuplesFactory";
        tuplesFactory =
          (TuplesFactory) Class.forName(tuplesFactoryClass).newInstance();
      }
       catch (Exception e) {

        throw new Error("Tuples factory misconfigured", e);
      }
    }

    assert tuplesFactory != null;

    return tuplesFactory;
  }

  /**
   * Copy constructor.
   *
   * @param tuples an existing instance, whose contents will be copied into the
   *      new instance
   * @return a new {@link Tuples} instance
   * @throws TuplesException EXCEPTION TO DO
   */
  public abstract Tuples newTuples(Tuples tuples) throws TuplesException;

  /**
   * Create a new tuples with a specified sort ordering.
   *
   * @param tuples an existing instance, whose contents will be copied into the
   *      new instance
   * @param rowComparator the desired sort ordering
   * @return a new {@link Tuples} instance
   * @throws TuplesException EXCEPTION TO DO
   */
  public abstract Tuples newTuples(Tuples tuples, RowComparator rowComparator)
    throws TuplesException;
}
