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
 *   The copyright on this file is held by:
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

package org.mulgara.resolver.test;

// Java 2 standard packages;
import java.net.*;

// Third party packages
import org.apache.log4j.Logger;
import javax.transaction.xa.XAResource;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.resolver.spi.*;

/**
 * @created 2005-05-03
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.4 $
 * @modified $Date: 2005/05/19 08:44:00 $ by $Author: raboczi $
 * @maintenanceAuthor $Author: raboczi $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class TestResolver implements Resolver {

  /** Logger */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(TestResolver.class);

  /** The session that this resolver is associated with */
  @SuppressWarnings("unused")
  private final ResolverSession resolverSession;

  /**
   * Construct a local query.
   *
   * @param resolverSession  the session which this query is local to
   *
   * @throws IllegalArgumentException if <var>variableList</var> is
   *   <code>null</code> or empty
   */
  TestResolver(ResolverSession resolverSession) throws ResolverFactoryException {
    // Validate "resolverSession" parameter
    if (resolverSession == null) {
      throw new IllegalArgumentException("Null \"resolverSession\" parameter");
    }
    this.resolverSession = resolverSession;
  }

  /**
   * Resolver does not support model creation.
   */
  public void createModel(long model, URI modelTypeURI) throws
                          ResolverException, LocalizeException {
      throw new ResolverException("TestResolver does not support model creation");
  }

  /**
   * @return a {@link DummyXAResource} with a 10 second transaction timeout
   */
  public XAResource getXAResource() {
    return new DummyXAResource(10);
  }

  /**
   * Insert or delete RDF statements from a model stored in a file.
   */
  public void modifyModel(long model, Statements statements, boolean occurs)
                          throws ResolverException {
    throw new ResolverException("Modification not supported in TestResolver");
  }

  /**
   * Remove the file containing the model.
   */
  public void removeModel(long model) throws ResolverException {
    throw new ResolverException("Graph removal not supported in TestResolver");
  }

  /**
   * Resolve a constraint based on special knowledge of a datatype.
   *
   * @param constraint  the constraint to resolve; this has the side effect of
   *                    setting the constraint's row count estimate
   * @return the statements which are true of the datatype, but only of
   *         instances of the datatype present in the string pool
   */
  public Resolution resolve(Constraint constraint) throws QueryException {
    if (!(constraint instanceof TestConstraint)) {
      throw new QueryException("TestResolver unable to resolve non-TestConstraint: " + constraint);
    }

    try {
      return new TestResolution((TestConstraint)constraint);
    } catch (TuplesException et) {
      throw new QueryException("Failed to resolve constraint", et);
    }
  }

  public void abort() {}
}
