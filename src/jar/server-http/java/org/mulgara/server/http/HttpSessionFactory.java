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

package org.mulgara.server.http;

import java.io.*;
import java.lang.reflect.*;

// Java 2 standard packages
import java.net.*;

// Log4J
import org.apache.log4j.*;

// Locally written packages
import org.mulgara.query.QueryException;
import org.mulgara.server.Session;
import org.mulgara.server.SessionFactory;

/**
 * {@link SessionFactory} for downloading and querying static RDF web pages.
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @author <a href="http://staff.pisoftware.com/david">David Makepeace</a>
 *
 * @created 2002-01-21
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:01 $
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
public class HttpSessionFactory implements SessionFactory {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(HttpSessionFactory.class.getName());

  /**
   * Description of the Field
   */
  private URI serverURI;

  //
  // Constructor
  //

  /**
   * Create an HTTP proxy.
   *
   * @param serverURI the URI of the HTTP bridge WAR
   * @throws QueryException EXCEPTION TO DO
   */
  public HttpSessionFactory(URI serverURI) throws QueryException {

    // Validate "serverURI" parameter
    if (serverURI == null) {

      throw new IllegalArgumentException("Null \"serverURI\" parameter");
    }

    if (!"http".equals(serverURI.getScheme())) {

      throw new IllegalArgumentException(serverURI +
          " doesn't use the http: protocol");
    }

    this.serverURI = serverURI;
  }

  //
  // Methods implementing SessionFactory
  //

  /**
   * @return the security domain of the proxied session factory
   * @throws QueryException EXCEPTION TO DO
   */
  public URI getSecurityDomain() throws QueryException {

    return null;
  }

  /**
   * Creates a session that can be used for a JRDF Graph.
   *
   * @throws QueryException
   * @return Session
   */
  public Session newJRDFSession() throws QueryException {

    // Default to a new Session
    return newSession();
  }

  /**
   * Creates a session that can be used for a Jena Graph.
   *
   * @throws QueryException
   * @return Session
   */
  public Session newJenaSession() throws QueryException {

    // Default to a new Session
    return newSession();
  }

  /**
   * Factory method. The session generated will be an unauthenticated (&quot;guest
   * &quot;) session.
   *
   * @return an unauthenticated session
   * @throws QueryException if a session couldn't be generated
   */
  public Session newSession() throws QueryException {

    try {

      return (Session) java.lang.reflect.Proxy.newProxyInstance(Session.class.getClassLoader(),
          new Class[] {
        Session.class
      }

      , new InvocationHandlerImpl(serverURI));
    }
    catch (Exception ex) {

      throw new QueryException(ex.toString());
    }
  }

  /**
   * METHOD TO DO
   *
   */
  public void close() {

  }

  /**
   * METHOD TO DO
   *
   */
  public void delete() {

  }

  static class InvocationHandlerImpl implements InvocationHandler {

    /**
     * Logger.
     *
     */
    final static Logger logger = Logger.getLogger(InvocationHandlerImpl.class);

    private URL serverURL;

    /**
     * CONSTRUCTOR InvocationHandlerImpl TO DO
     *
     * @param serverURI PARAMETER TO DO
     * @throws MalformedURLException EXCEPTION TO DO
     */
    InvocationHandlerImpl(URI serverURI) throws MalformedURLException {

      serverURL = serverURI.toURL();
    }

    public Object invoke(Object proxy, Method method,
        Object[] args) throws QueryException {

      String methodName = method.getName();

      if (logger.isDebugEnabled()) {

        logger.debug("Call to method: " + methodName + " (" +
            ( (args != null) ? args.length : 0) + ")");
      }

      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;

      try {

        HttpURLConnection urlConnection =
            (HttpURLConnection) serverURL.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.connect();

        // Check for failure.
        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
          throw new QueryException(urlConnection.getResponseMessage());
        }

        if (logger.isDebugEnabled()) logger.debug("Connected to URL: " + serverURL);

        // Hopefully this will follow redirects.
        serverURL = urlConnection.getURL();

        if (logger.isDebugEnabled()) logger.debug("URL is now: " + serverURL);

        // Send the request.
        oos = new ObjectOutputStream(urlConnection.getOutputStream());
        oos.writeObject(methodName);
        oos.writeObject(args);
        oos.flush();
        oos.close();
        oos = null;

        // Get the reply.
        ois = new ObjectInputStream(urlConnection.getInputStream());

        // Check if an exception was thrown.
        boolean exceptionOccurred = ois.readBoolean();

        if (exceptionOccurred) {
          // Exception thrown.  Get the exception.
          Object exception = ois.readObject();
          if (!(exception instanceof Throwable)) throw new RuntimeException(exception.toString());
          throw (Throwable)exception;
        }

        if (method.getReturnType() == Void.TYPE) return null;

        return ois.readObject();

      } catch (QueryException ex) {
        throw ex; // rethrow
      } catch (RuntimeException ex) {
        throw ex; // rethrow
      } catch (Throwable ex) {
        throw new QueryException(ex.toString());
      } finally {
        try {
          if (oos != null) oos.close();
        } catch (IOException ex) {
          logger.error("Exception while trying to close ObjectOutputStream.");
          logger.error(ex);
        } finally {
          try {
            if (ois != null) ois.close();
          } catch (IOException ex) {
            logger.error("Exception while trying to close ObjectInputStream.");
            logger.error(ex);
          }
        }
      }
    }
  }
}
