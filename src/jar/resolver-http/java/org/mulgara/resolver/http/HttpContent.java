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

package org.mulgara.resolver.http;

// Java 2 standard packages
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

import org.apache.commons.httpclient.ConnectionPoolTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;
import org.mulgara.content.Content;
import org.mulgara.content.NotModifiedException;

/**
 * Wrapper around a {@link URL}to make it satisfy the {@link Content}
 * interface.
 * 
 * @created 2004-09-23
 * @author Mark Ludlow
 * @version $Revision: 1.8 $
 * @modified $Date: 2005/01/05 04:58:45 $
 * @maintenanceAuthor $Author: newmana $
 * @company <a href="mailto:info@tucanatech.com">Tucana Technology </a>
 * @copyright &copy; 2004 <a href="http://www.tucanatech.com/">Tucana Technology
 *            Inc </a>
 * @licence <a href=" {@docRoot}/../../LICENCE">Mozilla Public License v1.1
 *          </a>
 */
public class HttpContent implements Content {

  /** Logger. */
  private final static Logger logger = Logger.getLogger(HttpContent.class.getName());

  /** The URI version of the URL */
  private URI httpUri;

  /** The MIME type of this data */
  private MimeType contentType = null;

  /** Connection host <code>host</code> */
  private String host;

  /** <code>port</code> to make connection to */
  private int port;

  /** Schema for connection <code>schema</code> */
  private String schema;

  /** A container for HTTP attributes that may persist from request to request */
  private HttpState state = new HttpState();

  /** Http connection */
  private HttpConnection connection = null;

  /** Http connection manager. For setting up and cleaning after connections. */
  HttpConnectionManager connectionManager = new SimpleHttpConnectionManager();

  /** To obtain the http headers only */
  private static final int HEAD = 1;

  /** To obtain the response body */
  private static final int GET = 2;

  /** Max. number of redirects */
  private static final int MAX_NO_REDIRECTS = 10;

  public HttpContent(URI uri) throws URISyntaxException, MalformedURLException {
    this(uri.toURL());
  }


  /**
   * Constructor.
   * 
   * @param url The URL this object will be representing 
   * the content of
   */
  public HttpContent(URL url) throws URISyntaxException {
    // Validate "url" parameter
    if (url == null)  throw new IllegalArgumentException("Null \"url\" parameter");
    initialiseSettings(url);
  }

  /**
   * Initialise the basic settings for a connection
   * 
   * @param url location of source
   * @throws URISyntaxException invalid URI
   */
  private void initialiseSettings(URL url) throws URISyntaxException {
    // Convert the URL to a Uri
    httpUri = new URI(url.toExternalForm());

    // obtain basic details for connections
    host = httpUri.getHost();
    port = httpUri.getPort();
    schema = httpUri.getScheme();
  }

  /**
   * Obtain the approrpriate connection method
   * 
   * @param methodType can be HEAD or GET
   * @return HttpMethodBase method
   */
  private HttpMethod getConnectionMethod(int methodType) {
    if (methodType != GET && methodType != HEAD) {
      throw new IllegalArgumentException("Invalid method base supplied for connection");
    }

    HostConfiguration config = new HostConfiguration();
    config.setHost(host, port, Protocol.getProtocol(schema));
    if (connection != null) {
      connection.releaseConnection();
      connection.close();
      connection = null;
    }
    try {
      connection = connectionManager.getConnectionWithTimeout(config, 0L);
    } catch (ConnectionPoolTimeoutException te) {
      // NOOP: SimpleHttpConnectionManager does not use timeouts
    }

    String proxyHost = System.getProperty("mulgara.httpcontent.proxyHost");

    if (proxyHost != null && proxyHost.length() > 0) {
      connection.setProxyHost(proxyHost);
    }

    String proxyPort = System.getProperty("mulgara.httpcontent.proxyPort");
    if (proxyPort != null && proxyPort.length() > 0) {
      connection.setProxyPort(Integer.parseInt(proxyPort));
    }

    // default timeout to 30 seconds
    connection.getParams().setConnectionTimeout(Integer.parseInt(System.getProperty(
        "mulgara.httpcontent.timeout", "30000")));

    String proxyUserName = System.getProperty("mulgara.httpcontent.proxyUserName");
    if (proxyUserName != null) {
      state.setCredentials(
          new AuthScope(
              System.getProperty("mulgara.httpcontent.proxyRealmHost"), AuthScope.ANY_PORT,
              System.getProperty("mulgara.httpcontent.proxyRealm"), AuthScope.ANY_SCHEME
          ),
          new UsernamePasswordCredentials(proxyUserName, System.getProperty("mulgara.httpcontent.proxyPassword"))
      );
    }

    HttpMethod method = null;
    if (methodType == HEAD) {
      method = new HeadMethod(httpUri.toString());
    } else {
      method = new GetMethod(httpUri.toString());
    }

    // manually follow redirects due to the
    // strictness of http client implementation

    method.setFollowRedirects(false);

    return method;
  }


  /**
   * Obtain a valid connection and follow redirects if necessary.
   * 
   * @param methodType request the headders (HEAD) or body (GET)
   * @return valid connection method. Can be null.
   * @throws NotModifiedException  if the content validates against the cache
   * @throws IOException  if there's difficulty communicating with the web site
   */
  private HttpMethod establishConnection(int methodType) throws IOException, NotModifiedException {
    if (logger.isDebugEnabled()) logger.debug("Establishing connection");

    HttpMethod method = getConnectionMethod(methodType);
    assert method != null;
    Header header = null;

    /*
      // Add cache validation headers to the request
      if (lastModifiedMap.containsKey(httpUri)) {
        String lastModified = (String) lastModifiedMap.get(httpUri);
        assert lastModified != null;
        method.addRequestHeader("If-Modified-Since", lastModified);
      }

      if (eTagMap.containsKey(httpUri)) {
        String eTag = (String) eTagMap.get(httpUri);
        assert eTag != null;
        method.addRequestHeader("If-None-Match", eTag);
      }
     */

    // Make the request
    if (logger.isDebugEnabled()) logger.debug("Executing HTTP request");
    connection.open();
    method.execute(state, connection);
    if (logger.isDebugEnabled()) {
      logger.debug("Executed HTTP request, response code " + method.getStatusCode());
    }

    // Interpret the response header
    if (method.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
      // cache has been validated
      throw new NotModifiedException(httpUri);
    } else if (!isValidStatusCode(method.getStatusCode())) {
      throw new UnknownHostException("Unable to obtain connection to "
          + httpUri + ". Returned status code " + method.getStatusCode());
    } else {
      // has a redirection been issued
      int numberOfRedirection = 0;
      while (isRedirected(method.getStatusCode()) && numberOfRedirection <= MAX_NO_REDIRECTS) {

        // release the existing connection
        method.releaseConnection();

        //attempt to follow the redirects
        numberOfRedirection++;

        // obtain the new location
        header = method.getResponseHeader("location");
        if (header != null) {
          try {
            initialiseSettings(new URL(header.getValue()));
            if (logger.isInfoEnabled()) {
              logger.info("Redirecting to " + header.getValue());
            }

            // attempt a new connection to this location
            method = getConnectionMethod(methodType);
            connection.open();
            method.execute(state, connection);
            if (!isValidStatusCode(method.getStatusCode())) {
              throw new UnknownHostException(
                  "Unable to obtain connection to " + " the redirected site "
                  + httpUri + ". Returned status code "
                  + method.getStatusCode());
            }
          } catch (URISyntaxException ex) {
            throw new IOException("Unable to follow redirection to "
                + header.getValue() + " Not a valid URI");
          }
        } else {
          throw new IOException("Unable to obtain redirecting detaild from " + httpUri);
        }
      }
    }

    // Update metadata about the cached document
    Header lastModifiedHeader = method.getResponseHeader("Last-Modified");
    if (lastModifiedHeader != null) {
      logger.debug(lastModifiedHeader.toString());
      assert lastModifiedHeader.getElements().length >= 1;
      assert lastModifiedHeader.getElements()[0].getName() != null;
      assert lastModifiedHeader.getElements()[0].getName() instanceof String;
      // previous code: added to cache
    }

    Header eTagHeader = method.getResponseHeader("Etag");
    if (eTagHeader != null) {
      logger.debug(eTagHeader.toString());
      assert eTagHeader.getElements().length >= 1;
      assert eTagHeader.getElements()[0].getName() != null;
      assert eTagHeader.getElements()[0].getName() instanceof String;
      // previous code: added to cache
    }

    return method;
  }

  /**
   * {@inheritDoc}
   *
   * This particular implementation tries to read the content type directly
   * from the HTTP <code>Content-Type</code> header.
   */
  public MimeType getContentType() throws NotModifiedException {
    // if we don't have the type from the connection already, then establish one
    if (contentType == null) {
      HttpMethod method = null;
      try {
        method = establishConnection(HEAD);
        contentType = readMimeType(method);
      } catch (IOException e) {
        logger.info("Unable to obtain content type for " + httpUri);
      } finally {
        // we're the only one to have needed this connection, so drop it
        if (method != null) method.releaseConnection();
        if (connection != null) connection.close();
      }
    }
    return contentType;
  }

  /**
   * Retrieves the URI for the actual content.
   * 
   * @return The URI for the actual content
   */
  public URI getURI() {
    return httpUri;
  }

  /** The stream can be re-opened, so return false. */
  public boolean isStreaming() {
    return false;
  }


  /**
   * Creates an input stream to the resource whose content we are representing.
   * 
   * @return An input stream to the resource whose content we are representing
   * @throws IOException
   */
  public InputStream newInputStream() throws IOException, NotModifiedException {

    if (logger.isDebugEnabled()) logger.debug("Getting new input stream for " + httpUri);

    // Create an input stream by opening the URL's input stream
    GetMethod method = null;
    InputStream inputStream = null;

    // obtain connection and retrieve the headers
    method = (GetMethod) establishConnection(GET);
    contentType = readMimeType(method);
    inputStream = method.getResponseBodyAsStream();
    if (inputStream == null) throw new IOException("Unable to obtain inputstream from " + httpUri);
    if (logger.isDebugEnabled()) logger.debug("Got new input stream for " + httpUri);
    return inputStream;
  }

  /**
   * @throws IOException always (not implemented)
   */
  public OutputStream newOutputStream() throws IOException {
    throw new IOException("Output of HTTP content not implemented");
  }

  private boolean isValidStatusCode(int status) {
    return (status == HttpStatus.SC_OK || isRedirected(status));
  }

  private boolean isRedirected(int status) {
    return (status == HttpStatus.SC_TEMPORARY_REDIRECT
        || status == HttpStatus.SC_MOVED_TEMPORARILY
        || status == HttpStatus.SC_MOVED_PERMANENTLY
        || status == HttpStatus.SC_SEE_OTHER);
  }

  /** @see org.mulgara.content.Content#getURIString() */
  public String getURIString() {
    return httpUri.toString();
  }


  /**
   * Read the mime type. Should only be done if the Mime type is not already available
   * as this will close the connection.
   * @return The MimeType for the URL.
   * @throws NotModifiedException if the content validates against the cache
   */
  @SuppressWarnings("unchecked")
  private MimeType readMimeType(HttpMethod method) throws NotModifiedException {
    MimeType result = null;
    String contentType = null;

    try {
      // obtain connection and retrieve the headers
      Header header = method.getResponseHeader("Content-Type");
      if (header != null) {
        contentType = header.getValue();
        // find the parameter separator so we can protect against bad params
        int sep = contentType.indexOf(';');
        // no params, just create the MimeType
        if (sep < 0) result = new MimeType(contentType);
        else {
          // create the MimeType from the type/subtype
          result = new MimeType(contentType.substring(0, sep));
          // parse parameters separately and set the result accordingly
          try {
            MimeTypeParameterList params = new MimeTypeParameterList(contentType.substring(sep + 1));
            Enumeration<String> names = (Enumeration<String>)params.getNames();
            while (names.hasMoreElements()) {
              String name = names.nextElement();
              result.setParameter(name, params.get(name));
            }
          } catch (MimeTypeParseException e) {
            logger.warn("Ignoring bad parameters in '" + contentType.substring(sep + 1) + "' from the content type for " + httpUri);
          }
        }
        if (logger.isInfoEnabled()) {
          logger.info("Obtain content type " + result + "  from " + httpUri);
        }
      }
    } catch (java.lang.IllegalStateException e) {
      logger.info("Unable to obtain content type for " + httpUri);
    } catch (MimeTypeParseException e) {
      logger.warn("Unexpected parameters before ; in '" + contentType + "' as a content type for " + httpUri);
    }
    return result;
  }
}
