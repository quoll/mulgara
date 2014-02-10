/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.rosenlaw.com/OSL3.0.htm
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * This file is an original work developed by Netymon Pty Ltd
 * (http://www.netymon.com, mailto:mail@netymon.com). Portions created
 * by Netymon Pty Ltd are Copyright (c) 2006 Netymon Pty Ltd.
 * All Rights Reserved.
 */

package org.mulgara.resolver;

// Local packages
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Wraps the system resolver as an internal resolver without obtaining a new one.
 *
 * Duplicate transactional resources cause problems with prepare/commit, so ensure
 * that only one is created.
 *
 * @created 2006-10-06
 *
 * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
 *
 * @version $Revision: $
 *
 * @modified $Date: $
 *
 * @maintenanceAuthor $Author: $
 *
 * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
 *
 * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
 *
 * @licence Open Software License v3.0</a>
 */


class InternalSystemResolverFactory extends InternalResolverFactory
{
  InternalSystemResolverFactory(ResolverFactory resolverFactory,
                                long            rdfType,
                                long            systemModel)
  {
    super(resolverFactory, rdfType, systemModel);
  }

  public Resolver newResolver(boolean         canWrite,
                              ResolverSession resolverSession,
                              Resolver        systemResolver)
    throws ResolverFactoryException
  {
    return new InternalResolver(
        systemResolver,
        rdfType,
        resolverSession,
        systemModel,
        systemResolver
    );
  }
}
