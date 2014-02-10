/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.resolver.store;

import java.io.IOException;

import org.mulgara.resolver.spi.FactoryInitializer;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.store.statement.xa11.XA11StatementStoreImpl;
import org.mulgara.store.xa.XAResolverSessionFactory;
import org.mulgara.store.xa.XAStatementStore;

/**
 * An extension of StatementStoreResolverFactory which instantiates an XA 1.1 statement store.
 *
 * @created Oct 1, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class XA11StatementStoreResolverFactory extends StatementStoreResolverFactory {

  /**
   * Internal construction of a resolver factory.
   * @throws InitializerException Unable to initialize.
   */
  protected XA11StatementStoreResolverFactory(FactoryInitializer initializer,
      XAResolverSessionFactory resolverSessionFactory) throws InitializerException {
    super(initializer, resolverSessionFactory);
  }

  /**
   * Factory method for instances of this factory
   * @param initializer Configuration object for this factory.
   * @param resolverSessionFactory A factory that this factory can proxy for getting sessions. 
   * @return A new factory for resolver sessions.
   * @throws InitializerException The factory could not be initialized.
   */
  public static ResolverFactory newInstance(FactoryInitializer initializer,
        XAResolverSessionFactory resolverSessionFactory) throws InitializerException {
    return new XA11StatementStoreResolverFactory(initializer, resolverSessionFactory);
  }

  /**
   * Creates the required type of store
   * @param filePrefix The base for the files being used for storage.
   * @return a new instance of an XAStatementStore
   * @throws IOException Error accessing the filesystem
   */
  protected XAStatementStore createStore(String filePrefix) throws IOException {
    return new XA11StatementStoreImpl(filePrefix.toString());
  }
}
