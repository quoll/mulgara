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
package org.mulgara.util;


/**
 * Class used to launch a WebBrowser. <p>
 *
 * Extracted from method: EmbeddedMulgaraServer.LaunchBrowser()</p>
 *
 * @created 2004-07-23
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @modified $Date: 2005/01/05 04:59:29 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <a href="http://www.tucanatech.com/">Tucana Technologies</a>
 *
 * @copyright &copy;2002-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 *
 */
public class WebBrowser {

  // Windows config
  private static final String win_id = "Windows";
  private static final String win_path = "rundll32";
  private static final String win_flag = "url.dll,FileProtocolHandler";

  // MAC config
  private static final String macosx_id = "Mac OS X";

  //UNIX Config
  private static final String unix_path = "netscape";
  private static final String unix_flag = "-remote openURL";
  private static final String mozilla_path = "mozilla";


  /**
   * Constructor. This class uses static methods.
   */
  private WebBrowser() {
  }

  /**
   * Launch a browser to display the specified URL.
   */
  public static void launchBrowser(String url) throws Exception {

    // validate URL
    if ((url != null)
        && (!"".equals(url))) {

      //determine OS
      String os = System.getProperty("os.name");
      boolean windows = ( (os != null) && os.startsWith(win_id));
      boolean macosx = ( (os != null) && os.startsWith(macosx_id));

      //execute launch command depending on the OS
      if (windows) {

        WebBrowser.launchBrowserWindows(url);
      }
      else if (macosx) {

        WebBrowser.launchBrowserMac(url);
      }
      else {

        //default OS is Unix (eg. Linux, BSD, Solaris)
        WebBrowser.launchBrowserUnix(url);
      }
    }
  }

  /**
   * Executes a Windows command to launch the Browser.
   *
   * @param url String
   * @throws Exception
   */
  private static void launchBrowserWindows(String url) throws Exception {

    // command = 'rundll32 url.dll,FileProtocolHandler http://...'
    Runtime.getRuntime().exec(win_path + " " + win_flag + " " + url);
  }

  /**
   * Executes a Mac command to launch the Browser.
   *
   * @param url String
   * @throws Exception
   */
  private static void launchBrowserMac(String url) throws Exception {

    // command = 'open http://...'
    Runtime.getRuntime().exec("open " + url);
  }

  /**
   * Executes an Unix command to launch the Browser.
   *
   * @param url String
   * @throws Exception
   */
  private static void launchBrowserUnix(String url) throws Exception {

    try {

      //try the Netcape Browser first
      WebBrowser.launchNetscapeBrowserUnix(url);
    }
    catch (Exception ex) {

      // try mozilla
      WebBrowser.launchMozillaBrowserUnix(url);
    }
  }

  /**
   * Executes an Unix command to launch the Mozilla Browser.
   *
   * @param url String
   * @throws Exception
   */
  private static void launchMozillaBrowserUnix(String url) throws Exception {

    WebBrowser.launchGeckoBrowserUnix(WebBrowser.mozilla_path, url);
  }

  /**
   * Executes an Unix command to launch the Netscape Browser.
   *
   * @param url String
   * @throws Exception
   */
  private static void launchNetscapeBrowserUnix(String url) throws Exception {

    WebBrowser.launchGeckoBrowserUnix(WebBrowser.unix_path, url);
  }

  /**
   * Executes an Unix command to launch the specified Gecko-based Browser.
   *
   * @param url String
   * @throws Exception
   */
  private static void launchGeckoBrowserUnix(String browser, String url) throws
      Exception {

    // command = '*browser* -remote openURL(http://www.javaworld.com)'
    String command = browser + " " + unix_flag + "(" + url + ")";

    //Under Unix, Browser has to be running for the "-remote" flag to work.
    Process process = Runtime.getRuntime().exec(command);

    // exit code 0, indicates command worked,
    boolean success = (process.waitFor()) == 0;

    // Command failed, start up the browser
    if (!success) {

      // command = '*browser* http://www.javaworld.com'
      command = browser + " " + url;
      Runtime.getRuntime().exec(command);
    }
  }

}
