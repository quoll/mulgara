/*
 * Copyright 2008 The Topaz Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributions:
 */

package org.mulgara.resolver;

// Java 2 standard packages
import java.net.URI;

// Locally written packages
import org.mulgara.query.rdf.Mulgara;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactory.Graph;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;

/**
 * Factory for simple mock resolver.
 *
 * @created 2009-07-05
 * @author Ronald Tschalär
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">Topaz Foundation</a>
 * @licence Apache License v2.0
 */
public class MockResolverFactory implements ResolverFactory {
  private static URI modelTypeURI = URI.create(Mulgara.NAMESPACE + "MockModel");

  private MockResolverFactory(ResolverFactoryInitializer resolverFactoryInitializer)
      throws ResolverException, InitializerException {
    resolverFactoryInitializer.preallocate(new URIReferenceImpl(modelTypeURI));
    resolverFactoryInitializer.addModelType(modelTypeURI, this);
  }

  public void close() { }
  public void delete() { }
  public Graph[] getDefaultGraphs() { return null; }
  public boolean supportsExport() { return true; }

  public static ResolverFactory newInstance(ResolverFactoryInitializer resolverFactoryInitializer)
      throws ResolverException, InitializerException {
    return new MockResolverFactory(resolverFactoryInitializer);
  }

  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession, Resolver systemResolver)
      throws ResolverFactoryException {
    return new MockResolver(resolverSession);
  }
}
