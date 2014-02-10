/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

/**
 * This class takes a {@link java.lang.Class} or an instance of a class, and describes it
 * in XML, with pretty-print indenting.
 *
 * @created Mar 28, 2008
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ClassDescriberXML implements ClassDescriber {

  /** The default number of spaces for each new level of indenting. */
  private static final int DEFAULT_INDENT_SPACES = 2;

  /** The number of spaces to use for each new level of indenting. */
  private int indentSpaces = DEFAULT_INDENT_SPACES;

  /** The class to be described. */
  private Class<?> cls;

  /** A string of spaces to use for indenting. The number of spaces should be {@link #indentLevel}*{@link #indentSpaces}. */
  private String indent;

  /**
   * Holds the level of indenting to use at a particular moment during XML generation.
   * Each change only increments or decrements by 1.
   */
  private int indentLevel;

  /** A string builder to accumulate the XML as it gets built. */
  private StringBuilder buffer;

  /**
   * Creates a describer for the class of an object instance.
   * @param obj The object instance to get the class description for.
   */
  public ClassDescriberXML(Object obj) {
    this(obj.getClass());
  }

  /**
   * Creates a describer for a given class.
   * @param cls The class tobe described.
   */
  public ClassDescriberXML(Class<?> cls) {
    this.cls = cls;
  }

  /** @see org.mulgara.util.ClassDescriber#getDescribedClass() */
  public Class<?> getDescribedClass() {
    return cls;
  }

  /**
   * @see org.mulgara.util.ClassDescriber#getDescription()
   * @return a String containing XML describing the provided class.
   */
  public String getDescription() {
    setIndent(0);
    buffer = new StringBuilder();
    describeClass();
    return buffer.toString();
  }

  /**
   * @see org.mulgara.util.ClassDescriber#getDescription(int)
   * @param indentLevel The level of indenting to start with.
   * @return a String containing XML describing the provided class.
   */
  public String getDescription(int indentLevel) {
    setIndent(indentLevel);
    buffer = new StringBuilder();
    describeClass();
    return buffer.toString();
  }

  /** @see org.mulgara.util.ClassDescriber#setSpacesPerIndent(int) */
  public void setSpacesPerIndent(int spaces) {
    indentSpaces = spaces;
  }

  /**
   * This method manages the work of describing a class. It calls the desciption methods for
   * all the parts of a class in order, and wraps the result in a &lt;class&gt; element. The
   * entire description is appended to {@link #buffer}.
   * @return The {@link #buffer} containing a full description of the configured class.
   */
  private StringBuilder describeClass() {
    buffer.append(indent).append("<class name=\"").append(cls.getName()).append(">\n");
    pushIndent();
    describeSuperClass();
    describeInterfaces();
    describeAnnotations();
    describeFields();
    describeConstructors();
    describeMethods();
    popIndent();
    buffer.append(indent).append("</class>\n");
    return buffer;
  }

  /**
   * Appends the superclass description of the configured class to {@link #buffer}.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder describeSuperClass() {
    Class<?> s = cls.getSuperclass();
    if (s != null) {
      buffer.append(indent).append("<superclass name=\"").append(s.getName()).append("\"/>\n");
    }
    return buffer;
  }

  /**
   * Appends the interface descriptions of the current class to {@link #buffer}.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder describeInterfaces() {
    Class<?>[] classes = cls.getInterfaces();
    if (classes.length != 0) {
      buffer.append(indent).append("<interfaces>\n");
      for (Class<?> c: classes) {
        buffer.append(indent).append("  <interface name=\"").append(c.getName()).append("\"/>\n");
      }
      buffer.append(indent).append("</interfaces>\n");
    }
    return buffer;
  }

  /**
   * Appends the annotation descriptions of the current class to {@link #buffer}.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder describeAnnotations() {
    Annotation[] annotations = cls.getAnnotations();
    addAnnotations(annotations);
    return buffer;
  }

  /**
   * Appends the full field descriptions of the current class to {@link #buffer}.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder describeFields() {
    Field[] fields = cls.getFields();
    if (fields.length != 0) {
      buffer.append(indent).append("<fields>\n");
      pushIndent();
      for (Field f: fields) {
        buffer.append(indent).append("<field name=\"").append(f.getName());
        buffer.append("\" type=\"").append(f.getType().getName());
        Annotation[] annotations = f.getDeclaredAnnotations();
        if (annotations.length == 0) buffer.append("\"/>\n");
        else {
          buffer.append("\">\n");
          pushIndent();
          addAnnotations(annotations);
          popIndent();
          buffer.append(indent).append("</field>\n");
        }
      }
      popIndent();
      buffer.append(indent).append("</fields>\n");
    }
    return buffer;
  }

  /**
   * Appends the constructor descriptions of the current class to {@link #buffer}.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder describeConstructors() {
    Constructor<?>[] constructors = cls.getConstructors();
    if (constructors.length != 0) {
      buffer.append(indent).append("<constructors>\n");
      pushIndent();
      for (Constructor<?> c: constructors) {
        buffer.append(indent).append("<constructor");
        if (c.isVarArgs()) buffer.append(" varargs=\"true\"");
        if (c.isSynthetic()) buffer.append(" synthethic=\"true\"");

        Class<?>[] exceptions = c.getExceptionTypes();
        Class<?>[] params = c.getParameterTypes();
        Annotation[] annotations = c.getDeclaredAnnotations();

        if (params.length == 0 && exceptions.length == 0 && annotations.length == 0) buffer.append("/>\n");
        else {
          buffer.append(">\n");
          pushIndent();
          addExceptions(exceptions);
          addAnnotations(annotations);
          addParameters(params, c.getParameterAnnotations());
          popIndent();
        }
        buffer.append(indent).append("</constructor>\n");
      }
      popIndent();
      buffer.append(indent).append("</constructors>\n");
    }
    return buffer;
  }

  /**
   * Appends the method descriptions of the current class to {@link #buffer}.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder describeMethods() {
    Method[] methods = cls.getMethods();
    if (methods.length != 0) {
      buffer.append(indent).append("<methods>\n");
      pushIndent();
      for (Method m: methods) {
        buffer.append(indent).append("<method name=\"").append(m.getName()).append("\"");
        if (m.isVarArgs()) buffer.append(" varargs=\"true\"");
        if (m.isSynthetic()) buffer.append(" synthethic=\"true\"");
        if (m.isBridge()) buffer.append(" bridge=\"true\"");
        buffer.append(">\n");
        pushIndent();

        Class<?>[] exceptions = m.getExceptionTypes();
        Class<?>[] params = m.getParameterTypes();
        Annotation[] annotations = m.getDeclaredAnnotations();

        addExceptions(exceptions);
        addAnnotations(annotations);
        addParameters(params, m.getParameterAnnotations());
        Class<?> ret = m.getReturnType();
        buffer.append(indent).append("<return ");

        if (ret == null) buffer.append("void=\"true\"/>\n");
        else buffer.append("type=\"").append(ret.getName()).append("\"/>\n");

        popIndent();
        buffer.append(indent).append("</method>\n");
      }
      popIndent();
      buffer.append(indent).append("</methods>\n");
    }
    return buffer;
  }

  /**
   * Appends the annotation descriptions for a given set of annotations to {@link #buffer}.
   * @param annotations The annotations to obtain descriptions for.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder addAnnotations(Annotation[] annotations) {
    if (annotations.length == 0) return buffer;
    buffer.append(indent).append("<annotations>\n");
    pushIndent();
    for (Annotation a: annotations) {
      buffer.append(indent).append("<annotation type=\"").append(a.annotationType().getName()).append("\">");
      buffer.append(xmlEscape(a.toString())).append("</annotation>\n");
    }
    popIndent();
    buffer.append(indent).append("</annotations>\n");
    return buffer;
  }

  /**
   * Appends the exception descriptions for a given set of exceptions to {@link #buffer}.
   * @param exceptions The exceptions to obtain descriptions for.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder addExceptions(Class<?>[] exceptions) {
    if (exceptions.length == 0) return buffer;
    buffer.append(indent).append("<throws>\n");
    pushIndent();
    for (int n = 0; n < exceptions.length; n++) {
      Class<?> e = exceptions[n];
      buffer.append(indent).append("<exception type=\"").append(e.getName()).append("\"/>\n");
    }
    popIndent();
    buffer.append(indent).append("</throws>\n");
    return buffer;
  }

  /**
   * Appends the parameter descriptions for a given set of method parameters to {@link #buffer}.
   * @param params The classes for each parameter.
   * @param paramAnnotations An array of parameter annotations for each parameter.
   * @return The updated {@link #buffer}.
   */
  private StringBuilder addParameters(Class<?>[] params, Annotation[][] paramAnnotations) {
    if (params.length == 0) return buffer;
    for (int nParam = 0; nParam < params.length; nParam++) {
      Class<?> param = params[nParam];
      Annotation[] pAnn = paramAnnotations[nParam];
      buffer.append(indent).append("<parameter type=\"").append(param.getName());
      if (pAnn.length == 0) buffer.append("\"/>\n");
      else {
        buffer.append("\">\n");
        pushIndent();
        addAnnotations(pAnn);
        popIndent();
        buffer.append(indent).append("</parameter>\n");
      }
    }
    return buffer;
  }

  /**
   * Changes the indent level to a new value. The new value is multiplied by the
   * {@link #indentSpaces} value to get the number of spaces to indent each line by.
   * @param level The new level to set the indent to. Should be +1 or -1 from the previous value.
   */
  private void setIndent(int level) {
    indentLevel = level;
    indent = indent(indentLevel);
  }

  /** Increment the current level of indenting. */
  private void pushIndent() {
    indent = indent(++indentLevel);
  }

  /** Decrement the current level of indenting. */
  private void popIndent() {
    indent = indent(--indentLevel);
  }

  /**
   * Build a new string containing the number of spaces needed for the current level of indenting.
   * @param i The indent level to calculate the new indent string for.
   * @return A new string containing i * {@link #indentSpaces} spaces.
   */
  private String indent(int i) {
    char[] arr = new char[indentSpaces * i];
    Arrays.fill(arr, ' ');
    return new String(arr);
  }

  /** An internal structure for mapping characters disallowed in XML to their escape codes. */
  private static final Map<Character,String> escapes = new HashMap<Character,String>();
  static {
    escapes.put('&', "&amp;");
    escapes.put('<', "&lt;");
    escapes.put('\r', "&#13;");
    escapes.put('>', "&gt;");
    escapes.put('"', "&quot;");
    escapes.put('\'', "&apos;");
  }

  /**
   * Search for disallowed XML characters in a string, and replace them with their escape codes.
   * @param s The string to escape.
   * @return A new version of the input string, with XML escaping done.
   */
  private static String xmlEscape(String s) {
    StringBuilder result = null;
    for(int i = 0, max = s.length(), delta = 0; i < max; i++) {
      char c = s.charAt(i);
      String escCode = escapes.get(c);

      if (escCode != null) {
        if (result == null) result = new StringBuilder(s);
        result.replace(i + delta, i + delta + 1, escCode);
        delta += (escCode.length() - 1);
      }
    }
    return (result == null) ? s : result.toString();
  }

}
