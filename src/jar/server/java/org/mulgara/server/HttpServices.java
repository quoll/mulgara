/*
 * Copyright 2011 Revelytix, Inc.
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

package org.mulgara.server;

/**
 * Manages all the HTTP services provided by a Mulgara server.
 *
 * @created Dec 12, 2011
 * @author Paul Gearon
 */
public interface HttpServices {

  /** The name of the primary implementation class. */
  public static String IMPL_CLASS_NAME = "org.mulgara.server.HttpServicesImpl";

  /**
   * Starts the web server and all services.
   * @throws ExceptionList Caused by a MultiException in the HTTP Server.
   * @throws Exception Both the server and the services are able to throw exceptions.
   */
  public void start() throws ExceptionList, Exception;

  /**
   * Stops the web server and all services.
   * @throws Exception Both the server and the services are able to throw exceptions.
   */
  public void stop() throws Exception;

}