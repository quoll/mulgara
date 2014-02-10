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

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

// Java 2 enterprise packages
import javax.servlet.*;
import javax.servlet.http.*;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.server.*;

/**
 * Receive HTTP requests, pass them to a {@link Session}, and return any
 * reponses as HTTP.
 *
 * @created 2002-01-21
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.3 $
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class HttpServerServlet extends HttpServlet {

  /**
   * The {@link SessionFactory} this bridge connects HTTP to.
   */
  private HttpServer httpServer;

  /**
   * Initialize the session factory.
   */
  public void init() throws ServletException {

    try {

      httpServer =
        new HttpServer(new URI(getServletConfig().getInitParameter("uri")));
    }
     catch (URISyntaxException e) {

      throw new ServletException("Bad uri init parameter", e);
    }

    //assert httpServer == null;
    try {

      httpServer.setDir(new File(getServletConfig().getInitParameter("dir")));
      httpServer.setProviderClassName(getServletConfig().getInitParameter("providerClassName"));
      httpServer.init();
      httpServer.start();
    }
     catch (Exception e) {

      e.printStackTrace();
      throw new ServletException("Couldn't start HTTP service", e);
    }
  }

  /**
   * Free the session factory reference.
   */
  public void destroy() {

    try {

      httpServer.stop();
      httpServer.destroy();
    }
     catch (Exception e) {

      log("Couldn't stop HTTP service", e);
    }

    httpServer = null;
  }

  /**
   * Process user requests.
   *
   * @param request PARAMETER TO DO
   * @param response PARAMETER TO DO
   * @throws IOException EXCEPTION TO DO
   * @throws ServletException EXCEPTION TO DO
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {

    final String MULGARA_SESSION_PROPERTY = "mulgaraSession";

    // Obtain the Mulgara session
    HttpSession httpSession = request.getSession();
    Session mulgaraSession =
      (Session) httpSession.getAttribute(MULGARA_SESSION_PROPERTY);

    if (mulgaraSession == null) {

      try {

        SessionFactory sessionFactory = httpServer.getSessionFactory();

        synchronized (sessionFactory) {

          mulgaraSession = sessionFactory.newSession();
        }
      }
       catch (QueryException e) {

        throw new ServletException("Couldn't generate session", e);
      }

      httpSession.setAttribute(MULGARA_SESSION_PROPERTY, mulgaraSession);
    }

    //assert mulgaraSession != null;
    // Extract the Kowai query from the HTTP request
    ObjectInputStream in = new ObjectInputStream(request.getInputStream());
    Object result;
    Method method;

    try {

      String methodName = (String) in.readObject();
      Object[] args = (Object[]) in.readObject();

      Class[] argClasses;

      if (args != null) {

        argClasses = new Class[args.length];

        for (int i = 0; i < args.length; i++) {

          argClasses[i] = args[i].getClass();
        }
      }
      else {

        argClasses = null;
      }

      method = mulgaraSession.getClass().getMethod(methodName, argClasses);
      result = method.invoke(mulgaraSession, args);
    }
     catch (InvocationTargetException e) {

      // Return the Mulgara answer in the HTTP response
      ObjectOutputStream out =
        new ObjectOutputStream(response.getOutputStream());
      out.writeBoolean(true);

      // exception occurred
      Throwable tex = e.getTargetException();

      if (tex instanceof Serializable) {

        out.writeObject(tex);
      }
      else {

        out.writeObject(tex.toString());
      }

      out.close();

      return;
    }
     catch (Exception e) {

      e.printStackTrace();
      throw new ServletException("Reflection failure", e);
    }

    // Return the Mulgara answer in the HTTP response
    ObjectOutputStream out = new ObjectOutputStream(response.getOutputStream());
    out.writeBoolean(false);

    // no exception occurred
    if (method.getReturnType() != Void.TYPE) {

      out.writeObject(result);
    }

    out.close();
  }
}
