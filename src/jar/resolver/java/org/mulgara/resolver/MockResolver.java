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

// Java 2 standard packages;
import java.net.URI;

// Third party packages
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.LocalNode;
import org.mulgara.query.QueryException;
import org.mulgara.query.TuplesException;
import org.mulgara.resolver.spi.DummyXAResource;
import org.mulgara.resolver.spi.EmptyResolution;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.Resolution;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.Statements;

/**
 * Simple mock resolver. All operations are dummys: model creation, removing, and modifying are
 * no-ops, and queries return an empty resolution. The model name is parsed for commands to
 * execute: the model must have a query string consisting of an '&amp;' separated list of commands
 * where each command is of the form "name=value" (the value may be empty) (i.e. follow http
 * query string syntax for parameters).
 *
 * <p>Currently the only commands supported: are 'active', 'wait', and 'hardWait':
 * <dl>
 *   <dt>active</dt>
 *   <dd>the value must be any combination of the characters 'c', 'd', 'm', 'r', 'f', 'n', or
 *       'l', which stand for 'Resolver.createModel', 'Resolver.dropModel', 'Resolver.modifyModel',
 *       'Resolver.resolve', 'Answer.beforeFirst', 'Answer.next', and 'Answer.cLose', respectively.
 *       The commands after this one are only executed for these operations. If not specified, the
 *       default is 'cdmrfnl' (i.e. all operations)</dd>
 *   <dt>wait</dt>
 *   <dd>the value is the number of milliseconds to sleep before continuing.</dd>
 *   <dt>hardWait</dt>
 *   <dd>like <var>wait</var>, but ignore interrupts.</dd>
 * </dl>
 *
 * @created 2009-07-05
 * @author Ronald Tschalär
 * @copyright &copy;2008 <a href="http://www.topazproject.org/">Topaz Foundation</a>
 * @licence Apache License v2.0
 */
public class MockResolver implements Resolver {
  /** Logger */
  private static final Logger logger = Logger.getLogger(MockResolver.class);

  /** the next XAResource to return */
  private static XAResource nextXARes = null;

  /** The session that this resolver is associated with */
  private final ResolverSession resolverSession;

  public static synchronized void setNextXAResource(XAResource xaRes) {
    nextXARes = xaRes;
  }

  MockResolver(ResolverSession resolverSession) {
    this.resolverSession = resolverSession;
  }

  public void createModel(long model, URI modelTypeURI) throws ResolverException {
    processCommands(model, 'c', ResolverException.class);
  }

  public XAResource getXAResource() {
    synchronized (MockResolver.class) {
      return (nextXARes != null) ? nextXARes : new DummyXAResource(10);
    }
  }

  public void modifyModel(long model, Statements statements, boolean occurs) throws ResolverException {
    processCommands(model, 'm', ResolverException.class);
  }

  public void removeModel(long model) throws ResolverException {
    processCommands(model, 'd', ResolverException.class);
  }

  public Resolution resolve(Constraint constraint) throws QueryException {
    long model = ((LocalNode) constraint.getModel()).getValue();
    processCommands(model, 'r', QueryException.class);
    return new MockEmptyResolution(constraint, model);
  }

  public void abort() {}

  private <T extends Throwable> void processCommands(long model, char op, Class<T> exc) throws T {
    URI modelUri = toURI(model, exc);
    logger.debug("model-uri='" + modelUri + "', op='" + op + "'");

    String query = modelUri.getQuery();
    if (query == null) {
      logger.debug("no query found, no commands");
      return;
    }

    for (String param : query.split("&")) {
      String name  = param.substring(0, param.indexOf('='));
      String value = param.substring(param.indexOf('=') + 1);

      logger.debug("processing command '" + name + "' with value '" + value + "'");

      if (name.equals("active")) {
        if (value.indexOf(op) < 0) {
          break;
        }
      } else if (name.equals("wait")) {
        logger.debug("sleeping '" + value + "' milliseconds");
        try {
          Thread.sleep(Long.parseLong(value));
        } catch (InterruptedException ie) {
          throw MulgaraTransactionFactory.newExceptionOrCause(exc, "sleep interrupted", ie);
        }
      } else if (name.equals("hardWait")) {
        logger.debug("sleeping '" + value + "' milliseconds");
        long targetDate = System.currentTimeMillis() + Long.parseLong(value);
        while (true) {
          long wait = targetDate - System.currentTimeMillis();
          if (wait <= 0)
            break;
          try {
            Thread.sleep(wait);
          } catch (InterruptedException ie) {
          }
        }
      } else {
        logger.info("Unknown command '" + name + "' - ignoring");
      }
    }
  }

  private <T extends Throwable> URI toURI(long model, Class<T> exc) throws T {
    try {
      Node globalModel = resolverSession.globalize(model);
      return ((URIReference) globalModel).getURI();
    } catch (GlobalizeException ge) {
      throw MulgaraTransactionFactory.newExceptionOrCause(exc, "Couldn't globalize model", ge);
    }
  }

  private class MockEmptyResolution extends EmptyResolution {
    private final long model;

    public MockEmptyResolution(Constraint constraint, long model) {
      super(constraint, true);
      this.model = model;
    }

    public void beforeFirst(long[] prefix, int suffixTruncation) throws TuplesException {
      processCommands(model, 'f', TuplesException.class);
      super.beforeFirst(prefix, suffixTruncation);
    }

    public boolean next() throws TuplesException {
      processCommands(model, 'n', TuplesException.class);
      return super.next();
    }

    public void close() {
      processCommands(model, 'l', RuntimeException.class);
      super.close();
    }
  }
}
