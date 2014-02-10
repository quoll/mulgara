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
import javax.swing.table.*;

// Logging
import org.apache.log4j.Logger;

// JRDF
import org.jrdf.util.*;
import org.jrdf.graph.*;
import java.awt.Component;
import java.awt.*;

// Local packages


/**
 * Displays the Predicate-Object pairs of a ClosableIterator in a read-only
 * table.
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
public class PropertiesTable extends JTable {

  /**
   * Logger. This is named after the class.
   */
  private final static Logger log = Logger.getLogger(PropertiesTable.class.
      getName());

  /** May need replacing if more functionality required */
  private DefaultTableModel model = null;

  /**
   * Constructor.
   *
   * @throws Exception
   */
  public PropertiesTable() throws Exception {
    setup();
  }

  /**
   * Initializes and sets up.
   *
   * @throws Exception
   */
  private void setup() throws Exception {

    //instantiate
    model = new DefaultTableModel();

    //initialize
    setRenderer();
    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    getTableModel().addColumn("Property");
    getTableModel().addColumn("Value");
    getTableHeader().setVisible(true);
    getTableHeader().setResizingAllowed(true);

    //add
    setModel(model);
  }

  /**
   * Clears the Table and displays the iterator's contents.
   * @param iter ClosableIterator
   * @throws Exception
   */
  public void display(ClosableIterator iter) throws Exception {
    invalidate();
    removeAll();
    setup();
    populate(iter);
    repaint();
    revalidate();
  }

  /**
   * Populates Table with Predicates and Object from the iterator.
   * @param iter ClosableIterator
   * @throws Exception
   */
  private void populate (ClosableIterator iter) throws Exception {
    Triple triple = null;
    Node [] row = new Node [2];
    while (iter.hasNext()) {
      triple = (Triple) iter.next();
      row[0] = triple.getPredicate();
      row[1] = triple.getObject();
      getTableModel().addRow(row);
    }
  }

  /**
   * Returns the Graph to be used to modify Table.
   * @return DefaultTableModel
   */
  private DefaultTableModel getTableModel() {
    if (model == null) {
      model = (DefaultTableModel) getModel();
    }
    return model;
  }

  /**
   * Adds a renderer for renering Nodes.
   */
  private void setRenderer() {

    setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      /**
       * Renders Nodes.
       * @param table JTable
       * @param value Object
       * @param isSelected boolean
       * @param hasFocus boolean
       * @param row int
       * @param column int
       * @return Component
       */
      public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = new JLabel("", JLabel.LEFT);
        if (isSelected) {
          label.setOpaque(true);
          label.setBackground(Color.BLACK);
          label.setForeground(Color.WHITE);
        }
        if ((value != null)
            && (value instanceof Node)) {
          Node node = (Node) value;
          String text = "";
          if ((node instanceof URIReference)
              && (((URIReference) node).getURI().getFragment() != null)) {
            text = " :" + ((URIReference) node).getURI().getFragment() ;
          } else {
            text = "" + node;
          }
          label.setText(text);
        }
        return label;
      }
    });
  }

}
