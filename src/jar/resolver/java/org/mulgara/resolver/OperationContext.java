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

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;
import java.util.List;

import org.mulgara.query.Answer;
import org.mulgara.query.Constraint;
import org.mulgara.query.Query;
import org.mulgara.query.QueryException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.SecurityAdapter;
import org.mulgara.resolver.spi.SystemResolver;
import org.mulgara.store.tuples.Tuples;

/**
 * Services provided to invocations of the {@link Operation#execute} method.
 *
 * @created 2004-11-08
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:24 $ by $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.tucanatech.com/">Tucana
 *   Technology, Inc</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public interface OperationContext {

  /**
   * Find a {@link ResolverFactory} capable of generating a {#link Resolver} to
   * manipulate a specified graph.
   *
   * @param graph  any graph
   * @return  a {@link ResolverFactory} capable of generating
   *   {@link Resolver}s which can manipulate the <var>graph</var>, never
   *   <code>null</code>
   * @throws QueryException if the <var>model</var> isn't a supported type or
   *   protocol
   */
  public ResolverFactory findModelResolverFactory(long graph)
    throws QueryException;

  /**
   * Find a {@link ResolverFactory} capable of generating a {@link Resolver}
   * that manipulates models of a specific type.
   *
   * @param modelTypeURI  a URI identifying a model type
   * @return a {@link ResolverFactory} capable of generating {@link Resolver}s
   *   which can manipulate models of the specificed <var>modelTypeURI</var>,
   *   or <code>null</code> if the type isn't supported
   * @throws QueryException if the <var>modelTypeURI</var> does identify a
   *   supported model type but the factory can't be obtained
   */
  public ResolverFactory findModelTypeResolverFactory(URI modelTypeURI)
    throws QueryException;

  /**
   * Obtain the list of installed {@link SecurityAdapter}s.
   *
   * @return an unmodifiable list of {@link SecurityAdapter}s
   */
  public List<SecurityAdapter> getSecurityAdapterList();

  /**
   * This method should be used to obtain a {@link Resolver} instance, rather
   * than directly calling {@link ResolverFactory#newResolver}.
   *
   * It makes sure that the new {@link Resolver} is enlisted with the current
   * transaction and bound to <code>this</code> session.
   *
   * @param resolverFactory  a factory from which to obtain a {@link Resolver}
   * @return a resolver from the <var>resolverFactory</var>, enlisted into the
   *   current transaction and bound to <code>this</code> session
   * @throws QueryException if the {@link Resolver} couldn't be obtained, bound
   *   and enlisted
   */
  public Resolver obtainResolver(ResolverFactory resolverFactory)
    throws QueryException;


  /**
   * Returns the canonical form of the model, leaving the model alone if it is recognised or unknown.
   *
   * @param model The model to check.
   * @return The new model node, or the current model if it is already canonical or unknown.
   */
  public long getCanonicalModel(long model);
  
  /**
   * Convenience method for evaluating a constraint in the context of a transactional
   * operation.  This method may be used to evaluate constraints against both internal and
   * external graphs; the context will find and use the appropriate Resolver.  The method will
   * also perform security checks on the graph specified by the constraint before resolving it.
   * 
   * @param constraint The constraint to resolve.
   * @return The constraint resolution.
   * @throws QueryException if there was an error obtaining the resolver, or resolving the consraint,
   *         or if security permissions were not satisfied.
   */
  public Tuples resolve(Constraint constraint) throws QueryException;

  /**
   * Here for the moment while we fix transactions.
   * Oct 2006 - if it's still here after Dec 2006 let someone know it's been
   * forgotten.
   */
  public Answer doQuery(Query query) throws Exception;

  public SystemResolver getSystemResolver(); // FIXME: Scaffolding for transactions.
}
