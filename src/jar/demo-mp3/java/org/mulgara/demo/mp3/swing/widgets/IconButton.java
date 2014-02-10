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

// Logging
import org.apache.log4j.Logger;
import javax.swing.border.Border;
import java.awt.Insets;

// Local packages


/**
 * Button representing an action. Displays an Icon and no text.
 *
 * @created 2004-12-15
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
public class IconButton extends JButton {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(IconButton.class.getName());

  /** Border used by every button */
  private static final Border border = BorderFactory.createEmptyBorder();

  /** Icon to be displayed */
  private Icon icon = null;


  /**
   * Constructor.
   * @param icon Icon
   */
  public IconButton(Icon icon) {
    setIcon(icon);
    setup();
  }

  /**
   * Sets up and configures the button.
   */
  private void setup () {
    setIcon(icon);
    setText("");
    setBorder(border);
    setBorderPainted(false);
    Insets internalMargin = new Insets(0, 0, 0, 0);
    setMargin(internalMargin);
  }

  /**
   * Validates and sets the buttons icon.
   * @param icon Icon
   * @throws IllegalArgumentException
   */
  public void setIcon(Icon icon) throws IllegalArgumentException {
    if (icon == null) {
      throw new IllegalArgumentException("Icon is null");
    }
    this.icon = icon;
    super.setIcon(icon);
  }

}
