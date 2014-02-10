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

/**
 * This class takes a {@link java.lang.Class} or an instance of a class, and describes it
 * in a format that is serialized as a string.
 *
 * @created Mar 28, 2008
 * @author Paul Gearon
 * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface ClassDescriber {

  /**
   * Gets a description of the current class. This is equivalent to calling getDescription(0)
   * @return A string encoding the description.
   */
  public String getDescription();

  /**
   * Gets a description of the current class, increasing any indenting of the string format if
   * an indented format is used.
   * @param indentLevel The number of indents to add to the formatting of the string. Ignored
   *        if the output string is not in an indented format.
   * @return A string encoding the description, with indenting increased by indentLevel if relevant.
   */
  public String getDescription(int indentLevel);

  /**
   * Gets the class that is described by this object.
   * @return The class that this object is describing.
   */
  public Class<?> getDescribedClass();

  /**
   * Sets the size of indenting in the output, if this is relevant to the describer.
   * @param spaces The size of the indent, in number of spaces.
   */
  public void setSpacesPerIndent(int spaces);
}