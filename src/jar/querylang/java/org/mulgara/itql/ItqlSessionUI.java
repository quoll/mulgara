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
 */

package org.mulgara.itql;

/**
 * Swing based iTQL session command line shell.
 *
 * @created 2004-01-15
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $ by $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2004 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.apache.log4j.*;
import org.jrdf.graph.Node;

import org.mulgara.query.Answer;

public class ItqlSessionUI extends JScrollPane implements Runnable,
    KeyListener, java.awt.event.MouseListener, ActionListener {

  /** Serialization ID */
  private static final long serialVersionUID = 6713691768040570333L;

  /**
   * The logging category to log to
   */
  private final static Logger log = Logger.getLogger(ItqlSessionUI.class);

  /**
   * Used to pipe input.
   */
  private InputStream inPipe;

  /**
   * Used to pipe output.
   */
  private OutputStream outPipe;

  /**
   * The iTQL session to send queries and used to send results.
   */
  private ItqlSession itqlSession;

  /**
   * The list of history items.
   */
  private ArrayList<String> history = new ArrayList<String>();

  /**
   * Current index into the history.
   */
  private int historyIndex = 0;

  /**
   * Current cursor position.
   */
  private int cursorPosition = 0;

  /**
   * The UI widget for displaying all text.
   */
  private JTextPane text;

  /**
   * The default styled document.
   */
  private DefaultStyledDocument doc;

  /**
   * Popup menu for Windows users.
   */
  private JPopupMenu popupMenu = new JPopupMenu();

  /**
   * Whether we are running a command still.
   */
  private volatile boolean runningCommand = false;

  /**
   * Create a new UI representation.
   *
   * @param newItqlSession the itql session to call when we receive commands and
   *   when we want to display them.
   */
  public ItqlSessionUI(ItqlSession newItqlSession) {

    super();

    itqlSession = newItqlSession;
    doc = new DefaultStyledDocument();
    text = new PasteablePane(doc);
    text.setFont(new Font("Monospaced", Font.PLAIN, 12));
    text.setMargin(new Insets(5, 5, 5, 5));
    text.addKeyListener(this);
    text.addMouseListener(this);
    setViewportView(text);

    // Consume middle click to handle properly for Unix/Linux
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    toolkit.addAWTEventListener(new MouseListener(), AWTEvent.MOUSE_EVENT_MASK);

    // Add popup menu for Windows users.
    JMenuItem copyItem = new JMenuItem("Copy");
    JMenuItem pasteItem = new JMenuItem("Paste");
    popupMenu.add(copyItem);
    popupMenu.add(pasteItem);
    copyItem.addActionListener(this);
    pasteItem.addActionListener(this);

    outPipe = new PipedOutputStream();
    try {

      new PipedInputStream((PipedOutputStream) outPipe);
    } catch (IOException e) {

      log.error("Error creating input stream", e);
    }

    PipedOutputStream pout = new PipedOutputStream();
    new PrintStream(pout);
    try {

      inPipe = new PipedInputStream(pout);
    } catch (IOException e) {

      log.error("Error creating input pipe", e);
    }

    // Start the inpipe watcher
    new Thread(this).start();
    requestFocus();
  }

  public void requestFocus() {

    super.requestFocus();
    text.requestFocus();
  }

  /**
   * Handle key pressed event.
   *
   * @param e the key that was pressed.
   */
  public void keyPressed(KeyEvent e) {

    switch (e.getKeyCode()) {

      // Enter pressed
      case (KeyEvent.VK_ENTER):

        if (e.getID() == KeyEvent.KEY_PRESSED) {

          if (!runningCommand) {

            enterPressed();
            cursorPosition = textLength();
            text.setCaretPosition(cursorPosition);
          }
        }
        e.consume();
        text.repaint();
      break;

      // Up history
      case (KeyEvent.VK_UP):

        if (e.getID() == KeyEvent.KEY_PRESSED) {

          historyUp();
        }
        e.consume();
      break;

      // Down history
      case (KeyEvent.VK_DOWN):
        if (e.getID() == KeyEvent.KEY_PRESSED) {

          historyDown();
        }
        e.consume();
      break;

      // Left or delete.
      case (KeyEvent.VK_LEFT):
      case (KeyEvent.VK_DELETE):

        if (text.getCaretPosition() <= cursorPosition) {

          e.consume();
        }
      break;

      // Go right.
      case (KeyEvent.VK_RIGHT):

        if (text.getCaretPosition() < cursorPosition) {

          // move caret first!
        }
        text.repaint();
      break;

      // Control-A go to start of line.
      case (KeyEvent.VK_A):

        if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          text.setCaretPosition(cursorPosition);
          e.consume();
        }
      break;

      // Control-C copy the text.
      case (KeyEvent.VK_C):

        if (text.getSelectedText() == null) {

          text.copy();
          e.consume();
        }
      break;


      // Control-E go to end of line.
      case (KeyEvent.VK_E):

        if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          text.setCaretPosition(textLength());
          e.consume();
        }
      break;

      // Control-U remove line
      case (KeyEvent.VK_U):

        if ((e.getModifiers() & InputEvent.CTRL_MASK) > 0) {

          replaceText("", cursorPosition, textLength());
          historyIndex = 0;
          e.consume();
        }
      break;

      // Home go to start of line
      case (KeyEvent.VK_HOME):

        text.setCaretPosition(cursorPosition);
        e.consume();
      break;

      // Go to end of line
      case (KeyEvent.VK_END):

        text.setCaretPosition(textLength());
        e.consume();
      break;

      // Ignore modifiers
      case (KeyEvent.VK_ALT):
      case (KeyEvent.VK_CAPS_LOCK):
      case (KeyEvent.VK_CONTROL):
      case (KeyEvent.VK_ESCAPE):
      case (KeyEvent.VK_F1):
      case (KeyEvent.VK_F2):
      case (KeyEvent.VK_F3):
      case (KeyEvent.VK_F4):
      case (KeyEvent.VK_F5):
      case (KeyEvent.VK_F6):
      case (KeyEvent.VK_F7):
      case (KeyEvent.VK_F8):
      case (KeyEvent.VK_F9):
      case (KeyEvent.VK_F10):
      case (KeyEvent.VK_F11):
      case (KeyEvent.VK_F12):
      case (KeyEvent.VK_INSERT):
      case (KeyEvent.VK_META):
      case (KeyEvent.VK_PAUSE):
      case (KeyEvent.VK_PRINTSCREEN):
      case (KeyEvent.VK_SHIFT):
      case (KeyEvent.VK_SCROLL_LOCK):

        // Do nothing.

      break;

      // Handle normal characters
      default:

        if ( (e.getModifiers() & (InputEvent.ALT_MASK | InputEvent.CTRL_MASK |
            InputEvent.META_MASK)) == 0) {

          if (text.getCaretPosition() < cursorPosition) {

            text.setCaretPosition(textLength());
          }
          text.repaint();
        }

        // Handle back space - don't let it go too far back.
        if (e.paramString().indexOf("Backspace") != -1) {

          if (text.getCaretPosition() <= cursorPosition) {

            e.consume();
            break;
          }
        }
      break;
    }
  }

  public void keyTyped(KeyEvent e) {

    if (e.paramString().indexOf("Backspace") != -1) {

      if (text.getCaretPosition() <= cursorPosition) {

        e.consume();
      }
    }
  }

  public void keyReleased(KeyEvent e) {

    // Do nothing.
  }

  public void mouseClicked(MouseEvent e) {

    // Do nothing.
  }

  public void mouseEntered(MouseEvent e) {

    // Do nothing.
  }

  public void mouseExited(MouseEvent e) {

    // Do nothing.
  }

  public void mousePressed(MouseEvent e) {

    if (e.isPopupTrigger()) {

      popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  public void mouseReleased(MouseEvent e) {

    if (e.isPopupTrigger()) {

      popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  public void actionPerformed(ActionEvent event) {

    String eventOccurred = event.getActionCommand();

    if (eventOccurred.equals("Copy")) {

      text.copy();
    } else if (eventOccurred.equals("Paste")) {

      text.paste();
    }
  }

  /**
   * Returns the length of the current text buffer.
   *
   * @return length of the current text buffer.
   */
  private int textLength() {

    return text.getDocument().getLength();
  }

  /**
   * Replaces the given string to a position in the currently displayed line.
   *
   * @param newString the string to add.
   * @param start the starting position.
   * @param end the end position.
   */
  private void replaceText(String newString, int start, int end) {

    text.select(start, end);
    text.replaceSelection(newString);
  }

  /**
   * When the enter key has been pressed process the current command.
   */
  private void enterPressed() {

    String command = getCommand();

    // Create null command.
    if (command.length() != 0) {

      // Put the command at the end of the array.
      history.add(command);
      command = command + System.getProperty("line.separator");

      // If the array gets too large remove the last entry.
      if (history.size() > 30) {

        history.remove(0);
      }

      // Indicate that we are running a command.
      runningCommand = true;

      // Create a new thread and start it.
      ExecutionThread execThread = new ExecutionThread("execThread", command);
      execThread.start();
    } else {

      // We've just hit enter so print the prompt.
      printPrompt();
    }
  }

  /**
   * Prints out the prompt.
   */
  public void printPrompt() {

    println();
    print(ItqlSession.PROMPT);
    historyIndex = 0;
    text.repaint();
  }

  /**
   * Returns the current command.
   *
   * @return the current command.
   */
  private String getCommand() {

    String command = "";
    try {

      command = text.getText(cursorPosition, textLength() - cursorPosition);
    } catch (BadLocationException e) {

      log.error("Failed to get text command at position: " + cursorPosition,
          e);
    }

    return command;
  }

  /**
   * Display the next command in the history buffer.
   */
  private void historyUp() {

    // Ensure there's a history and that the index never goes above the array
    // size.
    if ((history.size() != 0) && (historyIndex != history.size())) {

      historyIndex++;
      displayHistoryLine();
    }
  }

  /**
   * Display the previous command in the history buffer.
   */
  private void historyDown() {

    // Ensure there's a history and that the index is initially above 1.
    if ((history.size() != 0) && (historyIndex > 1)) {

      historyIndex--;
      displayHistoryLine();
    }
  }

  /**
   * Displays the history line to the screen.
   */
  private void displayHistoryLine() {

    String showline = (String) history.get(history.size() - historyIndex);
    replaceText(showline, cursorPosition, textLength());
    text.setCaretPosition(textLength());
    text.repaint();
  }

  /**
   * Prints a message to the UI with a line separator.
   *
   * @param message the message to display.
   */
  public void println(String message) {

    print(message + System.getProperty("line.separator"));
    text.repaint();
  }

  /**
   * Prints empty line.
   */
  public void println() {

    print(System.getProperty("line.separator"));
    text.repaint();
  }

  /**
   * Prints a message to the UI.
   *
   * @param message the message to display.
   */
  public void print(final String message) {

    invokeAndWait(new Runnable() {

      public void run() {


        append(message);
        cursorPosition = textLength();
        text.setCaretPosition(cursorPosition);
      }
    });
  }

  /**
   * Print out an error message to the UI.
   *
   * @param errorMessage the error message to display.
   */
  public void error(String errorMessage) {

    print(errorMessage, Color.red);
  }

  /**
   * Print out the message with the given color using the current font.
   *
   * @param message the message to display.
   * @param color the color to use.
   */
  public void print(String message, Color color) {

    print(message, null, color);
  }

  /**
   * Print out the message with the given font and colour.  Uses invoke and
   * wait.
   *
   * @param message the message to display.
   * @param font the font to use.
   * @param color the color to use.
   */
  public void print(final String message, final Font font, final Color color) {

    invokeAndWait(new Runnable() {

      public void run() {

        try {

          AttributeSet oldStyle = text.getCharacterAttributes();
          setStyle(font, color);
          append(message);
          cursorPosition = textLength();
          text.setCaretPosition(cursorPosition);
          text.setCharacterAttributes(oldStyle, true);
        } catch (Exception e) {

          log.error("Error when printing: " + message, e);
        }
      }
    });
  }

  /**
   * Sets the new style of a font and color to the text.
   *
   * @param font the new font.
   * @param color the new color.
   * @return the attributes of the given font and color.
   */
  private AttributeSet setStyle(Font font, Color color) {

    MutableAttributeSet attr = new SimpleAttributeSet();
    StyleConstants.setForeground(attr, color);

    // Don't set if null
    if (font != null) {

      if (font.isBold()) {

        StyleConstants.setBold(attr, true);
      } else {

        StyleConstants.setBold(attr, false);
      }

      StyleConstants.setFontFamily(attr, font.getFamily());
      StyleConstants.setFontSize(attr, font.getSize());
    }
    text.setCharacterAttributes(attr, false);
    return text.getCharacterAttributes();
  }

  /**
   * Append the given string to the existing string.
   *
   * @param newString the string to append to.
   */
  private void append(String newString) {

    int length = textLength();
    text.select(length, length);
    text.replaceSelection(newString);
  }

  /**
   * Thread that runs while waiting for input.
   */
  public void run() {

    try {

      byte[] buffer = new byte[255];
      int read;
      while ((read = inPipe.read(buffer)) != -1) {

        print(new String(buffer, 0, read));
      }
    } catch (IOException e) {

      log.error("Error reading input", e);
    }
  }

  /**
   * If not in the event thread run via SwingUtilities.invokeAndWait()
   */
  private void invokeAndWait(Runnable runnable) {

    if (!SwingUtilities.isEventDispatchThread()) {

      try {

        SwingUtilities.invokeAndWait(runnable);
      } catch (Exception e) {

        log.error("Error while executing invoke and wait", e);
      }
    } else {

      runnable.run();
    }
  }

  /**
   * Extension to JTextPane to put all pastes at the end of the command line.
   */
  @SuppressWarnings("serial")
  class PasteablePane extends JTextPane {

    public PasteablePane(StyledDocument doc) {
      super(doc);
    }

    public void paste() {
      super.paste();
    }
  }

  class MouseListener implements AWTEventListener {

    public void eventDispatched(AWTEvent event) {

      MouseEvent me = (MouseEvent) event;
      if (me.getButton() == MouseEvent.BUTTON2) {

        me.consume();
      }
    }
  }

  /**
   * Executes the command in a separate thread and display the results.
   */
  class ExecutionThread extends Thread {

    /**
     * The command to execute.
     */
    private String command;

    /**
     * Create a new execution thread.
     *
     * @param threadName the name of the thread.
     * @param newCommand the iTQL command to execute.
     */
    public ExecutionThread(String threadName, String newCommand) {

      super(threadName);
      command = newCommand;
    }

    /**
     * Run the command and display answer results.
     */
    public void run() {

      itqlSession.executeCommand(command);
      println();

      java.util.List<Answer> answers = itqlSession.getLastAnswers();
      java.util.List<String> messages = itqlSession.getLastMessages();

      int answerIndex = 0;
      String lastMessage;

      while (answerIndex < answers.size()) {

        lastMessage = messages.get(answerIndex);

        try {

          // Assume the same number of answers and messages
          Answer answer = answers.get(answerIndex);

          // If there's more than one answer print a heading.
          if (answers.size() > 1) {

            println();
            // If there's more than one answer add an extra line before the
            // heading.
            print("Executing Query " + (answerIndex+1),
                new Font("Monospaced", Font.BOLD, 12), Color.BLACK);
            println();
          }

          // print the results
          if (answer != null) {

            boolean hasAnswers = true;

            answer.beforeFirst();
            long rowCount = 0;

            if (answer.isUnconstrained()) {
              println("[ true ]");
              rowCount = 1;
            } else {
              if (!answer.next()) {
                print("No results returned.",
                    new Font("Monospaced", Font.BOLD, 12), Color.BLACK);
                hasAnswers = false;
              } else {
                do {
                  rowCount++;
                  print("[ ");
                  for (int index = 0; index < answer.getNumberOfVariables();
                      index++) {
                    Object object = answer.getObject(index);

                    assert(object instanceof Answer) ||
                          (object instanceof Node  ) ||
                          (object == null);

                    print(String.valueOf(object));
                    if (index < (answer.getNumberOfVariables() - 1)) {
                      print(", ");
                    }
                  }
                  println(" ]");
                }
                while (answer.next());
              }
            }
            if (hasAnswers) {
              print(rowCount + " rows returned.",
                  new Font("Monospaced", Font.BOLD, 12), Color.BLACK);
            }

            answer.close();
          }
        } catch (Exception te) {

          // Failed to iterate over or retrieve the answer.
          log.fatal("Failed to retrieve or iterate over answer", te);
          error("Failed to get answer");
        }

        if ((lastMessage != null) && (!lastMessage.equals(""))) {

          print(lastMessage, new Font("Monospaced", Font.BOLD, 12),
              Color.BLACK);
        }

        // If there's more than one answer add a new line.
        if (answers.size() > 1) {

          println();
        }

        // Increment index
        answerIndex++;
      }

      // Signal that the command has finished and display prompt
      runningCommand = false;
      printPrompt();
    }
  };
}
