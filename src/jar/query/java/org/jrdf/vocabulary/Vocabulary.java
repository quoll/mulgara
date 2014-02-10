/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.vocabulary;

// Java 2 standard
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A representation of a known vocabulary.
 *
 * @author Andrew Newman
 */
public abstract class Vocabulary implements Serializable {

  /**
   * Allow newer compiled version of the stub to operate when changes
   * have not occurred with the class.
   * NOTE : update this serialVersionUID when a method or a public member is
   * deleted.
   */
  private static final long serialVersionUID = 1299792941457444378L;

  /** The set of URI references. */
  protected static Set<URI> resources = new HashSet<URI>();

  /** The set of properties in this vocabulary. */
  protected static Set<URI> properties = new HashSet<URI>();

  /** The set of classes in this vocabulary. */
  protected static Set<URI> classes = new HashSet<URI>();

  protected Vocabulary() {
  }

  /**
   * Returns the resources in this vocabulary.
   * @return the resource in this vocabulary.
   */
  public static Set<URI> getResources() {
    return Collections.unmodifiableSet(resources);
  }

  /**
   * Returns the property resources in this vocabulary.
   * @return the property resources in this vocabulary.
   */
  public static Set<URI> getProperties() {
    return Collections.unmodifiableSet(properties);
  }

  /**
   * Returns the class resources in this vocabulary.
   * @return the class resources in this vocabulary.
   */
  public static Set<URI> getClasses() {
    return Collections.unmodifiableSet(classes);
  }

  /**
   * Calculate the intersection of 2 sets.
   * @param <T> The objects contained in the sets.
   * @param first The first set.
   * @param second The second set.
   * @return A new set containing those elements that appear in both first and second.
   */
  protected static <T> Set<T> intersectionOf(Set<T> first, Set<T> second) {
    Set <T> copyFirst = new HashSet<T>(first);
    copyFirst.retainAll(second);
    return copyFirst;
  }
}
