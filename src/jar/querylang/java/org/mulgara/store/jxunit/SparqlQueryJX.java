/*
 * Copyright 2009 DuraSpace.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mulgara.store.jxunit;

// 3rd party package
import net.sourceforge.jxunit.JXTestCase;
import net.sourceforge.jxunit.JXTestStep;
import net.sourceforge.jxunit.JXProperties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;


/**
 * Test frame for SPARQL queries.
 *
 * @created 2009-09-17
 * @author Paul Gearon
 */
public class SparqlQueryJX implements JXTestStep {

  /** Line separator. */
  private static final String EOL = System.getProperty("line.separator");

  /** Name of the query command parameter */
  public final static String QUERY = "query";

  /** Parameter name of the results of the command */
  public final static String RESULT = "queryResult";

  /** Parameter name of the time flag */
  public final static String TIME_NAME = "time";

  /** Parameter name of the endpoint parameter */
  public final static String ENDPOINT_NAME = "endpoint";

  /** Parameter name of the default graph */
  public final static String DEFAULT_GRAPH_NAME = "default-graph-uri";

  /** The default endpoint to connect to */
  private final static String DEFAULT_ENDPOINT = "http://localhost:8080/sparql/";

  /** The character encoding to use for the request URL */
  private static final String UTF8 = "UTF-8";

  /** Internal record of the URL used */
  private String requestUrl = "-";

  /** Size for internal read buffer */
  private final static int BUFFER_SIZE = 1024;

  /**
   * Execute this object. Retrieves a string from the testCase properties and
   * executes it as a SPARQL query, returning the results in the testCase object.
   *
   * @param testCase The map object containing the properties.
   * @throws Exception propagated exception from the query framework.
   */
  public void eval(JXTestCase testCase) throws Exception {

    JXProperties props = testCase.getProperties();

    // get the query to execute
    String query = (String)props.get(QUERY);

    // get the default graph URI
    String defGraphStr = props.getString(DEFAULT_GRAPH_NAME);
    URI defGraph = (defGraphStr != null) ? new URI(defGraphStr) : null;

    // retrieve the endpoint to use
    String confEndpoint =  props.getString(ENDPOINT_NAME);
    String endpoint = (confEndpoint != null && !confEndpoint.equals("")) ? confEndpoint : DEFAULT_ENDPOINT;

    // retrieve flag asking if the query should be timed
    String timeStr = props.getString(TIME_NAME);
    boolean time = (timeStr != null) && Boolean.valueOf(timeStr).booleanValue();

    long start = System.currentTimeMillis();
    String queryResult = "";

    try {
      queryResult = executeQuery(endpoint, query, defGraph);
    } catch (Exception ex) {
      queryResult = "URL = <" + requestUrl + ">";
      queryResult += "Exception: " + org.mulgara.util.StackTrace.throwableToString(ex);
    } finally {
    }

    if (time) {
      System.out.println(EOL + "query: \"" + query + "\" query has taken " +
        (System.currentTimeMillis() - start) + " milliseconds to execute." + EOL);
    }

    JXUtil.putStringProp(props, RESULT, queryResult + EOL);
  }


  /**
   * Execute a query against a SPARQL endpoint.
   * @param endpoint The URL of the endpoint.
   * @param query The query to execute.
   * @param defGraph The default graph to execute the query against,
   *        or <code>null</code> if not set.
   * @return A string representation of the result from the server.
   * @throws IOException If there was an error communicating with the server.
   * @throws UnsupportedEncodingException The SPARQL endpoint used an encoding not understood by this system.
   * @throws HttpClientException An unexpected response was returned from the SPARQL endpoint.
   */
  String executeQuery(String endpoint, String query, URI defGraph)
        throws IOException, UnsupportedEncodingException, HttpClientException {
    String request = endpoint + "?";
    if (defGraph != null && (0 != defGraph.toString().length())) request += "default-graph-uri=" + defGraph.toString() + "&";
    request += "query=" + URLEncoder.encode(query, UTF8);
    requestUrl = request;

    HttpClient client = new DefaultHttpClient();
    client.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

    HttpGet get = new HttpGet(request);
    get.setHeader("Accept", "application/rdf+xml");

    StringBuilder result = new StringBuilder();
    try {
      HttpResponse response = client.execute(get);
      StatusLine status = response.getStatusLine();
      if (status.getStatusCode() != HttpStatus.SC_OK) {
        String msg = "Bad result from SPARQL endpoint: " + status;
        System.err.println(msg);
        throw new HttpClientException(msg);
      }

      HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStreamReader resultStream = new InputStreamReader(entity.getContent());
        char[] buffer = new char[BUFFER_SIZE];
        int len;
        while ((len = resultStream.read(buffer)) >= 0) result.append(buffer, 0, len);
        resultStream.close();
      } else {
        String msg = "No data in response from SPARQL endpoint";
        System.out.println(msg);
        throw new HttpClientException(msg);
      }
    } catch (UnsupportedEncodingException e) {
      System.err.println("Unsupported encoding returned from SPARQL endpoint: " + e.getMessage());
      throw e;
    } catch (IOException ioe) {
      System.err.println("Error communicating with SPARQL endpoint: " + ioe.getMessage());
      throw ioe;
    }
    return result.toString();
  }


  public static class HttpClientException extends Exception {
    /** Serialization ID */
    private static final long serialVersionUID = -7563267824587372995L;
    public HttpClientException() {}
    public HttpClientException(String msg) {
      super(msg);
    }
    public HttpClientException(String msg, Throwable cause) {
      super(msg, cause);
    }
    public HttpClientException(Throwable cause) {
      super(cause);
    }
  }
}
