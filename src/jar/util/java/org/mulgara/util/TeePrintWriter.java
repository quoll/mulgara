/*
 * Copyright 2010 Paula Gearon.
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
package org.mulgara.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * A Tee adapter between this print writer and another.
 */
public class TeePrintWriter extends PrintWriter {

  /** The other side of the Tee adapter */
  private final PrintWriter other;

  /**
   * @param out
   */
  public TeePrintWriter(Writer out, PrintWriter other) {
    super(out);
    this.other = other;
  }

  /**
   * @param out
   */
  public TeePrintWriter(OutputStream out, PrintWriter other) {
    super(out);
    this.other = other;
  }

  /**
   * @param fileName
   * @throws FileNotFoundException
   */
  public TeePrintWriter(String fileName, PrintWriter other) throws FileNotFoundException {
    super(fileName);
    this.other = other;
  }

  /**
   * @param file
   * @throws FileNotFoundException
   */
  public TeePrintWriter(File file, PrintWriter other) throws FileNotFoundException {
    super(file);
    this.other = other;
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(Writer out, PrintWriter other, boolean autoFlush) {
    super(out, autoFlush);
    this.other = other;
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(OutputStream out, PrintWriter other, boolean autoFlush) {
    super(out, autoFlush);
    this.other = other;
  }

  /**
   * @param fileName
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(String fileName, String csn, PrintWriter other)
      throws FileNotFoundException, UnsupportedEncodingException {
    super(fileName, csn);
    this.other = other;
  }

  /**
   * @param file
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(File file, String csn, PrintWriter other) throws FileNotFoundException,
      UnsupportedEncodingException {
    super(file, csn);
    this.other = other;
  }

  /**
   * @param out
   */
  public TeePrintWriter(Writer out, OutputStream o) {
    this(out, new PrintWriter(o));
  }

  /**
   * @param out
   */
  public TeePrintWriter(OutputStream out, OutputStream o) {
    this(out, new PrintWriter(o));
  }

  /**
   * @param fileName
   * @throws FileNotFoundException
   */
  public TeePrintWriter(String fileName, OutputStream o) throws FileNotFoundException {
    this(fileName, new PrintWriter(o));
  }

  /**
   * @param file
   * @throws FileNotFoundException
   */
  public TeePrintWriter(File file, OutputStream o) throws FileNotFoundException {
    this(file, new PrintWriter(o));
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(Writer out, OutputStream o, boolean autoFlush) {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(OutputStream out, OutputStream o, boolean autoFlush) {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param fileName
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(String fileName, String csn, OutputStream o)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(fileName, csn, new PrintWriter(o));
  }

  /**
   * @param file
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(File file, String csn, OutputStream o) throws FileNotFoundException,
      UnsupportedEncodingException {
    this(file, csn, new PrintWriter(o));
  }

  /**
   * @param out
   */
  public TeePrintWriter(Writer out, Writer o) {
    this(out, new PrintWriter(o));
  }

  /**
   * @param out
   */
  public TeePrintWriter(OutputStream out, Writer o) {
    this(out, new PrintWriter(o));
  }

  /**
   * @param fileName
   * @throws FileNotFoundException
   */
  public TeePrintWriter(String fileName, Writer o) throws FileNotFoundException {
    this(fileName, new PrintWriter(o));
  }

  /**
   * @param file
   * @throws FileNotFoundException
   */
  public TeePrintWriter(File file, Writer o) throws FileNotFoundException {
    this(file, new PrintWriter(o));
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(Writer out, Writer o, boolean autoFlush) {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(OutputStream out, Writer o, boolean autoFlush) {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param fileName
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(String fileName, String csn, Writer o)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(fileName, csn, new PrintWriter(o));
  }

  /**
   * @param file
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(File file, String csn, Writer o) throws FileNotFoundException,
      UnsupportedEncodingException {
    this(file, csn, new PrintWriter(o));
  }

  /**
   * @param out
   */
  public TeePrintWriter(Writer out, String o) throws FileNotFoundException {
    this(out, new PrintWriter(o));
  }

  /**
   * @param out
   */
  public TeePrintWriter(OutputStream out, String o) throws FileNotFoundException {
    this(out, new PrintWriter(o));
  }

  /**
   * @param fileName
   * @throws FileNotFoundException
   */
  public TeePrintWriter(String fileName, String o) throws FileNotFoundException {
    this(fileName, new PrintWriter(o));
  }

  /**
   * @param file
   * @throws FileNotFoundException
   */
  public TeePrintWriter(File file, String o) throws FileNotFoundException {
    this(file, new PrintWriter(o));
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(Writer out, String o, boolean autoFlush) throws FileNotFoundException {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(OutputStream out, String o, boolean autoFlush) throws FileNotFoundException {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param fileName
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(String fileName, String csn, String o)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(fileName, csn, new PrintWriter(o));
  }

  /**
   * @param file
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(File file, String csn, String o) throws FileNotFoundException,
      UnsupportedEncodingException {
    this(file, csn, new PrintWriter(o));
  }

  /**
   * @param out
   */
  public TeePrintWriter(Writer out, File o) throws FileNotFoundException {
    this(out, new PrintWriter(o));
  }

  /**
   * @param out
   */
  public TeePrintWriter(OutputStream out, File o) throws FileNotFoundException {
    this(out, new PrintWriter(o));
  }

  /**
   * @param fileName
   * @throws FileNotFoundException
   */
  public TeePrintWriter(String fileName, File o) throws FileNotFoundException {
    this(fileName, new PrintWriter(o));
  }

  /**
   * @param file
   * @throws FileNotFoundException
   */
  public TeePrintWriter(File file, File o) throws FileNotFoundException {
    this(file, new PrintWriter(o));
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(Writer out, File o, boolean autoFlush) throws FileNotFoundException {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param out
   * @param autoFlush
   */
  public TeePrintWriter(OutputStream out, File o, boolean autoFlush) throws FileNotFoundException {
    this(out, new PrintWriter(o), autoFlush);
  }

  /**
   * @param fileName
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(String fileName, String csn, File o)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(fileName, csn, new PrintWriter(o));
  }

  /**
   * @param file
   * @param csn
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   */
  public TeePrintWriter(File file, String csn, File o) throws FileNotFoundException,
      UnsupportedEncodingException {
    this(file, csn, new PrintWriter(o));
  }

  public void flush() {
    super.flush();
    other.flush();
  }

  public void close() {
    super.close();
    other.close();
  }

  public boolean checkError() {
    boolean first = super.checkError();
    return other.checkError() && first;
  }

  public void write(int c) {
    super.write(c);
    other.write(c);
  }

  public void write(char[] c, int offset, int len) {
    super.write(c, offset, len);
    other.write(c, offset, len);
  }

  public void write(String s, int off, int len) {
    super.write(s, off, len);
    other.write(s, off, len);
  }

  public void println() {
    super.println();
    other.println();
  }

}
