/**
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
package org.mulgara.query.filter;


/**
 * Defines how to set and retrieve context
 *
 * @created Mar 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface ContextOwner {

  /**
   * Set the current context. This *must* be run at the start of every test else the underlying
   * values will not resolve correctly.
   * @param context The context for this test.
   */
  public void setCurrentContext(Context context);
  
  /**
   * Get the current context. This is a callback that is used during a test.
   * @return The context of the currently running test, or the most recent context if not in a test.
   */
  public Context getCurrentContext();

  /**
   * Sets the object that contains the current context to work in.
   * @param owner The object that owns this context.
   */
  public void setContextOwner(ContextOwner owner);

  /**
   * Gets the object that contains the current context to work in.
   * @return The object that owns this context.
   */
  public ContextOwner getContextOwner();

  /**
   * Adds a context owner as a listener so that it will be updated with its context
   * when this owner gets updated.
   * @param l The context owner to register.
   */
  public void addContextListener(ContextOwner l);

}
