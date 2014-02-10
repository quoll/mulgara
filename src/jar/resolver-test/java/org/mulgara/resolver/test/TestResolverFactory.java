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

package org.mulgara.resolver.test;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;
import org.jrdf.vocabulary.RDF;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.*;

/**
 * @created 2005-05-03
 * @author <a href="http://netymon.com/people/staff/andrae">Andrae Muys</a>
 * @version $Revision: 1.4 $
 * @modified $Date: 2005/05/16 11:07:09 $ by $Author: amuys $
 * @maintenanceAuthor $Author: amuys $
 * @copyright &copy;2005 <a href="http://www.defence.gov.au/">
 *      Australian Commonwealth Government, Department of Defence</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class TestResolverFactory implements ResolverFactory {
  /** Logger.  */
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(TestResolverFactory.class.getName());

  private static URI modelTypeURI = URI.create(Mulgara.NAMESPACE + "TestModel");

  /**
   * Construct a local query.
   *
   * @param resolverFactoryInitializer  {@inheritDoc}
   * @throws IllegalArgumentException {@inheritDoc}
   * @throws ResolverException {@inheritDoc}
   */
  private TestResolverFactory(ResolverFactoryInitializer
                             resolverFactoryInitializer) throws
      ResolverException, InitializerException {

    // Validate "resolverFactoryInitializer" parameter
    if (resolverFactoryInitializer == null) {

      throw new IllegalArgumentException(
          "Null \"resolverInitializer\" parameter");
    }

    // Initialize fields
    resolverFactoryInitializer.preallocate(new URIReferenceImpl(RDF.TYPE));

    resolverFactoryInitializer.preallocate(new URIReferenceImpl(modelTypeURI));

    // Claim mulgara:TestModel
    resolverFactoryInitializer.addModelType(modelTypeURI, this);

    resolverFactoryInitializer.registerNewConstraint(new TestConstraintDescriptor());

    TestResolution.initializeResults(resolverFactoryInitializer);
  }

  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because the only persistent resources
   * are outside the database.
   */
  public void close() {
    // null implementation
  }

  /**
   * {@inheritDoc ResolverFactory}
   *
   * This is actually a non-operation, because the only persistent resources
   * are outside the database.
   */
  public void delete() {
    // null implementation
  }

  /**
   * {@inheritDoc}
   * @return <code>null</code> - no default graphs for this resolver
   */
  public Graph[] getDefaultGraphs() { return null; }
  
  /**
   * {@inheritDoc}
   * @return <code>false</code> - this graph is for custom constraint testing only.
   */
  public boolean supportsExport() {
    return false;
  }

  /**
   * Register this resolver upon database startup.
   *
   * @param resolverFactoryInitializer  the database within which to find or
   *                                    create the various Test resources
   * @throws ResolverException If the Test resources can't be found or created
   */
  public static ResolverFactory newInstance(
      ResolverFactoryInitializer resolverFactoryInitializer) throws
      ResolverException, InitializerException {

    return new TestResolverFactory(resolverFactoryInitializer);
  }

  /**
   * Obtaining an Test Resolver
   *
   * @param resolverSession  the session which this query is local to
   * @throws IllegalArgumentException if <var>resolverSession</var> is
   *   <code>null</code>
   */
  public Resolver newResolver(boolean canWrite,
                              ResolverSession resolverSession,
                              Resolver systemResolver) throws
                              ResolverFactoryException{
    throw new ResolverFactoryException("Resolution should have been short circuited by descriptor");
  }
}
