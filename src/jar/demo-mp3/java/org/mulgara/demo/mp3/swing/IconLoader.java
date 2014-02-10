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

package org.mulgara.demo.mp3.swing;

// Java 2 standard packages
import javax.swing.*;

// Logging
import org.apache.log4j.Logger;
import java.net.URL;

// Local packages


/**
 * Static List of Icon images and methods for creating Icons from them.
 *
 * @created 2004-12-15
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:07 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class IconLoader {

  /**
   * Logger. This is named after the class.
   */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(IconLoader.class.getName());

  //---------------------- List of Predefined Icon Images ----------------------

  /** Search Icon / Magnifying glass (24 x 24) */
  public static final String SEARCH_24 = "/ico_search.png";

  /** Music file Icon (24 x 24) */
  public static final String MUSIC_FILE_24 = "/ico_musicFile.png";

  /** Play Icon / Blue Right Arrow (16 x 16) */
  public static final String PLAY_16 = "/ico_play.png";

  /** Pause Icon / Blue Vertical Bars (16 x 16) */
  public static final String PAUSE_16 = "/ico_pause.png";

  /** Stop Icon / Blue Square (16 x 16) */
  public static final String STOP_16 = "/ico_stop.png";

  /** Skip Icon / Blue Bar and Arrow (16 x 16) */
  public static final String SKIP_16 = "/ico_skip.png";

  /** Add Icon / Plus (16 x 16) */
  public static final String ADD_16 = "/ico_add.png";

  /** Remove Icon / Cross (16 x 16) */
  public static final String REMOVE_16 = "/ico_remove.png";


  /** Singleton instance */
  public static IconLoader instance = null;

  /**
   * Default constructor
   */
  private IconLoader() {
  }

  /**
   * Returns the singleton instance of this class. Only used internally.
   * @return IconLoader
   */
  private static IconLoader getInstance() {
    synchronized (IconLoader.class) {
      if (instance == null) {
        instance = new IconLoader();
      }
    }
    return instance;
  }

  /**
   * Creates an Icon from the image/icon path.
   * @param path String
   * @return Icon
   */
  public static Icon getIcon(String path) {
    ImageIcon icon = null;
    URL url = getInstance().getClass().getResource(path);
    if (url != null) {
      icon =  new ImageIcon(url);
    }
    return icon;
  }

}
