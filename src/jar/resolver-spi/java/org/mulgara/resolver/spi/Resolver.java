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

// Java 2 standard packages
import java.net.URI;

// Local packages
import org.mulgara.query.Constraint;
import org.mulgara.query.QueryException;
import org.mulgara.store.tuples.Tuples;

/**
 * A plugable graph-set containing graphs of RDF triples.
 *
 * A Resolver allows models/graphs to be created, modified, queried and deleted.
 *
 * @created 2003-12-01
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2003-2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface Resolver extends EnlistableResource {
  /**
   * Create a model of a specified type.
   *
   * The database won't ask for a <var>modelType</var> which wasn't registered
   * using the {@link ResolverFactoryInitializer#addModelType} method when this
   * instance was created.  The <var>modelType</var> parameter is passed to
   * support {@link ResolverFactory} instances which are capable of generating
   * more than one type of model.  Even if a particular implementation is only
   * registered for a single type, it would be good practice to validate the
   * <var>modelType</var> passed anyway.
   *
   * @param model  a local node identifying the new model to create
   * @param modelType  the preallocated node of the resource representing the
   *   type of the new <var>model</var>
   * @throws IllegalArgumentException if <var>model</var> or
   *   <var>modelType</var> are <code>null</code>
   * @throws ResolverException if the <var>model</var> can't be created
   */
  public void createModel(long model, URI modelType) throws ResolverException, LocalizeException;
                                                                                
  /**
   * Insert or delete RDF statements in an existing model.
   *
   * @param model  the local node identifying an existing model
   * @param statements  the {@link Statements} to insert into the
   *   <var>model</var>
   * @param occurs  whether to assert the <var>tripleSet</var>, or (if
   *   <code>false</code>) to deny it
   * @throws ResolverException if the <var>tripleSet</var> can't be
   *   added to the <var>model</var>
   */
  public void modifyModel(long model, Statements statements, boolean occurs)
    throws ResolverException;
  
  /**
   * Remove a model.
   *
   * @param model  the local node identifying the existing model to remove
   * @throws IllegalArgumentException if <var>model</var> is <code>null</code>
   * @throws ResolverException if the <var>model</var> can't be removed
   */
  public void removeModel(long model) throws ResolverException;

  /**
   * Resolve a {@link Constraint} into its solution {@link Tuples}.
   *
   * @param constraint  the constraint to resolve, never <code>null</code>
   * @return the {@link Statements} satisfying the <var>constraint</var>
   * @throws IllegalArgumentException if <var>constraint</var> is
   *   <code>null</code>
   * @throws QueryException if the <var>constraint</var> can't be resolved
   */
  public Resolution resolve(Constraint constraint) throws QueryException;
}
