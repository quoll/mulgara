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

// Java packages
import java.net.URI;

// Third party packages
import org.jrdf.graph.URIReference;

// Local packages
import org.mulgara.query.Constraint;
import org.mulgara.query.GraphResource;
import org.mulgara.query.QueryException;
import org.mulgara.store.tuples.Tuples;
import org.mulgara.store.xa.SimpleXARecoveryHandler;

/**
 * Generates {@link SystemResolver} instances.
 *
 * @created 2004-03-28
 * @author <a href="http://www.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:50 $ 
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */

public interface SystemResolverFactory extends ResolverFactory, SimpleXARecoveryHandler
{
  /**
   * Obtain a resolver for use as a SystemResolver.
   *
   * @param canWrite  whether the obtained resolver must support modification;
   *   if <code>false</code> is passed, it's permitted to return a read-only
   *   resolver.
   * @return a resolver instance for the <var>SessionResolver</var>
   * @throws ResolverFactoryException if the resolver couldn't be created
   */
  public abstract SystemResolver newResolver(boolean canWrite)
    throws ResolverFactoryException;

  public void setDatabaseMetadata(DatabaseMetadata metadata);

  public URI getSystemModelTypeURI();
}
