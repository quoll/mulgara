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

// JRDF
import org.jrdf.graph.URIReference;
import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.*;

// Local packages
import org.mulgara.demo.mp3.*;
import org.mulgara.demo.mp3.id3.Id3Tag;
import org.mulgara.demo.mp3.swing.widgets.*;

/**
 * Panel for Id3 Properties and Values
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
public class Id3Panel extends JPanel {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(Id3Panel.class.getName());

  /** Mp3 to display the details for */
  private Id3Tag tag = null;

  /** Version of Id3Tag to use (eg. Id3Tag.ID3V2) Default is ID3V2 */
  private URIReference version = Id3Tag.ID3V2;

  /**
   * Default constructor
   *
   * @throws Exception
   */
  public Id3Panel() throws Exception {

    setup();
  }

  /**
   * Initializes and sets up components.
   *
   * @throws Exception
   */
  private void setup () throws Exception {

    //instantiate

    //initialize
    setLayout(new BorderLayout());

    //add
    add(getPropertiesPanel(), BorderLayout.CENTER);
  }

  /**
   * Returns the panel containing all the Id3Tag info.
   *
   * @return JPanel
   * @throws Exception
   */
  private JPanel getPropertiesPanel() throws Exception {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    if (tag != null) {
      //create table and add
      ClosableIterator properties = tag.getStatements();
      PropertiesTable table = new PropertiesTable();
      table.display(properties);
      properties.close();
      panel.add(table.getTableHeader(), BorderLayout.NORTH);
      panel.add(table, BorderLayout.CENTER);
    }
    return panel;
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
  public void clear() throws Exception {
    //can get away with rendering a null file for now
    tag = null;
    redraw();
  }

  /**
   * Sets the Id3Tag to be displayed. A <code>null</code> will clear the display.
   *
   * @param tag Id3Tag
   * @throws Exception
   */
  public void setId3Tag(Id3Tag tag) throws Exception {
    if (tag == null) {
      clear();
    } else {
      this.tag = tag;
      redraw();
    }
  }

  /**
   * Sets the Mp3File whose Id3Tag is to be displayed. A <code>null</code>
   * will clear the display.
   *
   * @param mp3 Id3Tag
   * @throws Exception
   */
  public void setId3Tag(Mp3File mp3) throws Exception {
    if (mp3 == null) {
      clear();
    } else {
      tag = mp3.getId3Tag(getVersion());
      redraw();
    }
  }

  /**
   * Sets the Id3Tag version to use.
   * @param version URIReference
   * @throws Exception
   */
  public void setVersion(URIReference version) throws Exception {
    if (version == null) {
      throw new IllegalArgumentException("'version' is null");
    }
    this.version = version;
  }

  /**
   * Returns the Version of Id3Tag to display
   * @return URIReference
   */
  public URIReference getVersion() {
    assert (version != null) : "Version should never be null.";
    return version;
  }

}
