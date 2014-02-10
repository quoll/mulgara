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

package org.mulgara.server;

/**
 * This is an interface for gaining access to an existing SessionFactory.
 * If a SessionFactory needs to be created, then use {@link SessionFactoryFactory}.
 *
 * @created Sep 8, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public interface SessionFactoryProvider {

  /**
   * Accesses a SessionFactory that this object is associated with.
   * @return A SessionFactory associated with this object.
   */
  SessionFactory getSessionFactory();
}
