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

package org.mulgara.demo.mp3.swing.widgets;

// Java 2 standard packages
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

// Logging
import org.apache.log4j.Logger;

// Local packages


/**
 * Panel for displaying the results of a search
 *
 * @created 2004-12-07
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:10 $
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
public class HeadingPanel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(HeadingPanel.class.getName());

  /** Used to construct a large heading */
  public static final int LARGE = 0;

  /** Used to construct a medium heading */
  public static final int MEDIUM = 1;

  /** Used to construct a small heading */
  public static final int SMALL = 2;

  /** Used to calculate the LARGE heading font size */
  private static final float LARGE_SCALE = 2.5F;

  /** Used to calculate the MEDIUM heading font size */
  private static final float MEDIUM_SCALE = 1.75F;

  /** Used to calculate the SMALL heading font size */
  private static final float SMALL_SCALE = 1.15F;

  /** Color of the Font */
  public static final Color TEXT_COLOR = Color.WHITE;

  /** Color of the Background */
  public static final Color BACKGROUND_COLOR = Color.GRAY;


  /**
   * Creates a Panel to be used as a heading.
   *
   * @param title String
   * @param icon Icon null indicates no Icon
   * @param size int LARGE, MEDIUM or SMALL (default is SMALL)
   */
  public HeadingPanel(String title, Icon icon, int size) {
    setLayout(new BorderLayout());
    add(getLabel(title, icon, size), BorderLayout.CENTER);
    setBackground(BACKGROUND_COLOR);
    setBorder(getDefaultBorder());
  }

  /**
   * Returns a JLabel with the specified properties.
   *
   * @param title String
   * @param icon Icon
   * @param size int
   * @return JLabel
   */
  private JLabel getLabel(String title, Icon icon, int size) {

    JLabel label = new JLabel(title, JLabel.LEFT);
    label.setFont(getLabelFont(size));
    label.setForeground(TEXT_COLOR);
    if (icon != null) {
      label.setIcon(icon);
    }
    return label;
  }

  /**
   * Returns the standard border.
   * @return Border
   */
  private Border getDefaultBorder() {
    return new EtchedBorder(EtchedBorder.LOWERED);
  }

  /**
   * Return a size-relative font.
   * @param size int
   * @return Font
   */
  private Font getLabelFont(int size) {
    Font font = new Font("Default", Font.PLAIN, 10);
    //derive scale from size
    float scale = 1.0F;
    switch (size) {
      case (LARGE):
        scale = LARGE_SCALE;
        break;
      case (MEDIUM):
        scale = MEDIUM_SCALE;
        break;
      case (SMALL):
        scale = SMALL_SCALE;
        break;
      default:
        break;
    }
    font = font.deriveFont(font.getSize() * scale);
    return font;
  }

}
