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

package org.mulgara.resolver.lucene;

// Java 2 standard packages
import java.net.URI;

// Third party packages
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Resolves constraints in models defined by static RDF documents.
 *
 * @created 2004-04-01
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/02/22 08:16:13 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ReadOnlyLuceneResolver extends LuceneResolver {
  /** Logger.  */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(ReadOnlyLuceneResolver.class);

  //
  // Constructors
  //

  /**
   * Construct a resolver.
   *
   * @param modelTypeURI     the URI of the lucene model type
   * @param resolverSession  the session this resolver is associated with
   * @param resolverFactory  the resolver-factory that created us
   * @throws IllegalArgumentException  if <var>directory</var> is <code>null</code>
   */
  ReadOnlyLuceneResolver(URI modelTypeURI, ResolverSession resolverSession,
                         LuceneResolverFactory resolverFactory)
      throws ResolverFactoryException {
    super(modelTypeURI, resolverSession, resolverFactory, false);
  }

  //
  // Methods implementing Resolver
  //

  /**
   * Graph creation is not allowed the via this read-only view.
   *
   * @throws ResolverException always
   */
  public void createModel(long model, URI modelTypeURI)
      throws ResolverException, LocalizeException {
    throw new ResolverException("Attempt to create model in ReadOnlyLuceneResolver");
  }

  /**
   * Graph modification is not supported via this read-only view.
   *
   * @throws ResolverException always
   */
  public void modifyModel(long model, Statements statements,
      boolean occurs) throws ResolverException {
    throw new ResolverException("Attempt to modify model in ReadOnlyLuceneResolver");
  }

  /**
   * Graph deletion is not supported via this read-only view.
   *
   * @throws ResolverException always
   */
  public void removeModel(long model) throws ResolverException {
    throw new ResolverException("Attempt to remove model in ReadOnlyLuceneResolver");
  }
}
