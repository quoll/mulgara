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

package org.mulgara.demo.mp3.swing.id3;

// Java 2 standard packages
import javax.swing.*;
import java.awt.*;

// Logging
import org.apache.log4j.Logger;

// Local packages
import org.jrdf.graph.*;
import java.net.*;
import java.beans.PropertyChangeListener;
import java.awt.event.*;

import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.id3.*;
import org.mulgara.demo.mp3.swing.*;
import org.mulgara.demo.mp3.swing.widgets.*;
import org.mulgara.itql.*;
import org.mulgara.query.rdf.*;

/**
 * Panel for displaying Id3Tag information.
 *
 * @created 2004-12-07
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:08 $
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
public class TagPanel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(TagPanel.class.getName());

  /** Encoding used to encode/decode URLs */
  private static final String URL_ENCODING = "UTF-8";

  /** Mp3 to display the details for */
  private Mp3File mp3 = null;

  /** Displays the Id3Tag info */
  private Id3Panel id3Panel = null;

  /**
   * Default constructor
   *
   * @throws Exception
   */
  public TagPanel() throws Exception {

    setup();
  }

  /**
   * Initializes and sets up components.
   *
   * @throws Exception
   */
  private void setup() throws Exception {

    //instantiate
    id3Panel = (id3Panel == null) ? new Id3Panel() : getId3Panel();

    //initialize
    setLayout(new BorderLayout());
    getId3Panel().setId3Tag(mp3);

    //add
    add(getNorthPanel(), BorderLayout.NORTH);
    add(getCenterPanel(), BorderLayout.CENTER);
//    add(getSouthPanel(), BorderLayout.SOUTH);
  }

  /**
   * Clears the panel and sets up.
   *
   * @throws Exception
   */
  private void redraw() throws Exception {
    invalidate();
    removeAll();
    setup();
    repaint();
    revalidate();
  }

  /**
   * Clears the Mp3 Panel.
   *
   * @throws Exception
   */
  private void clear() throws Exception {
    //can get away with rendering a null file for now
    mp3 = null;
    redraw();
  }

  /**
   * Returns the Top panel.
   * @return JPanel
   */
  private JPanel getNorthPanel() {
    Icon icon = IconLoader.getIcon(IconLoader.MUSIC_FILE_24);
    return new HeadingPanel(getTitle(), icon, HeadingPanel.SMALL);
  }

  /**
   * Returns the Middle panel.
   * @return JPanel
   */
  private JPanel getCenterPanel() {
    return getId3Panel();
  }

  /**
   * Returns the Bottom panel.
   *
   * @return JPanel
   * @throws Exception
   */
  private JPanel getSouthPanel() throws Exception {
    JPanel panel = new JPanel();
    final JCheckBox id3v1 = new JCheckBox("ID3v1");
    final JCheckBox id3v2 = new JCheckBox("ID3v2");
    //initialize
    if (getId3Panel().getVersion().equals(Id3Tag.ID3V1)) {
      id3v1.setSelected(true);
      id3v2.setSelected(false);
    } else {
      id3v2.setSelected(true);
      id3v1.setSelected(false);
    }
    id3v1.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          if (id3v1.isSelected()) {
            id3v2.setSelected(false);
            getId3Panel().setVersion(Id3Tag.ID3V1);
            getId3Panel().setId3Tag(mp3);
          }
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    id3v2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          if (id3v2.isSelected()) {
            id3v1.setSelected(false);
            getId3Panel().setVersion(Id3Tag.ID3V2);
            getId3Panel().setId3Tag(mp3);
          }
        }
        catch (Exception exception) {
          ExceptionHandler.handleException(exception);
        }
      }
    });
    //add
    panel.add(id3v1);
    panel.add(id3v2);
    return panel;
  }

  /**
   * Returns the URI for the Mp3File being displayed.
   * @return String
   */
  private String getTitle() {
    String uri = (mp3 == null) ? "" : "" + mp3.getResource().getURI();
    String text = uri;
    int lastChar = 0;
    int lastHash = uri.lastIndexOf("#");
    int lastSlash = uri.lastIndexOf("/");
    if ((lastHash > 0)
        || (lastSlash > 0)) {
      lastChar = Math.max(lastHash, lastSlash);
      text = text.substring(lastChar);
    }
    return decodeURL(text);
  }

  /**
   * Decodes the String using an URLDecoder.
   * @param url String
   * @return String
   */
  private String decodeURL(String url) {
    try {
      return URLDecoder.decode(url, URL_ENCODING);
    }
    catch (Exception exception) {
      ExceptionHandler.handleException(exception);
      return url;
    }
  }

  /**
   * Sets the Mp3File to be displayed. A <code>null</code> file will clear the
   * display.
   *
   * @param mp3File Mp3File
   * @throws Exception
   */
  public void setMp3File(Mp3File mp3File) throws Exception {
    if (mp3File == null) {
      clear();
    }
    else {
      mp3 = mp3File;
      redraw();
    }
  }

  /**
   * Returns the Panel used to display Id3 Information
   * @throws IllegalStateException
   * @return Id3Panel
   */
  private Id3Panel getId3Panel() throws IllegalStateException {
    if (id3Panel == null) {
      throw new IllegalStateException("Id3Panel does not exist.");
    }
    return id3Panel;
  }

}
