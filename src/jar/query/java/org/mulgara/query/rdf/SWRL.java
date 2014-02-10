/*
 * Copyright 2009 Revelytix.
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
package org.mulgara.query.rdf;

import java.net.URI;

/**
 * URI constants for SWRL rules.
 * 
 * @created Jun 4, 2009
 * @author Alex Hall
 * @copyright &copy; 2009 <a href="http://www.revelytix.com">Revelytix, Inc.</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class SWRL {

  /** URI for the SWRL namespace. */
  public static final String SWRL = "http://www.w3.org/2003/11/swrl#";
  
  /** URI for a variable reference. */
  public static final String VARIABLE_STR = SWRL + "Variable";
  
  /** URI for an implication (i.e. rule) */
  public static final String IMP_STR = SWRL + "Imp";
  
  /** URI for a rule head. */
  public static final String HEAD_STR = SWRL + "head";
  
  /** URI for a rule body. */
  public static final String BODY_STR = SWRL + "body";
  
  /** URI for a class membership atom. */
  public static final String CLASS_ATOM_STR = SWRL + "ClassAtom";
  
  /** URI for an individual-valued property atom. */
  public static final String INDIVIDUAL_ATOM_STR = SWRL + "IndividualPropertyAtom";
  
  /** URI for a datatype-valued property atom. */
  public static final String DATA_ATOM_STR = SWRL + "DatavaluedPropertyAtom";
  
  /** URI for an individual identity atom. */
  public static final String SAME_INDIVIDUALS_ATOM_STR = SWRL + "SameIndividualsAtom";
  
  /** URI for an individual difference atom. */
  public static final String DIFFERENT_INDIVIDUALS_ATOM_STR = SWRL + "DifferentIndividualsAtom";
  
  /** URI for a builtin atom. */
  public static final String BUILTIN_ATOM_STR = SWRL + "BuiltinAtom";
  
  /** URI for a data range atom. */
  public static final String DATA_RANGE_ATOM_STR = SWRL + "DataRangeAtom";
  
  /** URI for a class atom predicate. */
  public static final String CLASS_PREDICATE_STR = SWRL + "classPredicate";
  
  /** URI for an individual or datatype property atom predicate. */
  public static final String PROPERTY_PREDICATE_STR = SWRL + "propertyPredicate";
  
  /** URI for a builtin atom operator. */
  public static final String BUILTIN_STR = SWRL + "builtin";
  
  /** URI for a data range atom predicate. */
  public static final String DATA_RANGE_STR = SWRL + "dataRange";
  
  /** URI to identify the first argument to an atom. */
  public static final String ARG_1_STR = SWRL + "argument1";
  
  /** URI to identify the second argument to an atom. */
  public static final String ARG_2_STR = SWRL + "argument2";
  
  /** URI to identify the argument list for a builtin atom. */
  public static final String ARGS_STR = SWRL + "arguments";
  
  /** URI reference for a variable reference. */
  public static final URIReferenceImpl VARIABLE = new URIReferenceImpl(URI.create(VARIABLE_STR));
  
  /** URI reference for an implication (i.e. rule) */
  public static final URIReferenceImpl IMP = new URIReferenceImpl(URI.create(IMP_STR));
  
  /** URI reference for a rule head. */
  public static final URIReferenceImpl HEAD = new URIReferenceImpl(URI.create(HEAD_STR));
  
  /** URI reference for a rule body. */
  public static final URIReferenceImpl BODY = new URIReferenceImpl(URI.create(BODY_STR));
  
  /** URI reference for a class membership atom. */
  public static final URIReferenceImpl CLASS_ATOM = new URIReferenceImpl(URI.create(CLASS_ATOM_STR));
  
  /** URI reference for an individual-valued property atom. */
  public static final URIReferenceImpl INDIVIDUAL_ATOM = new URIReferenceImpl(URI.create(INDIVIDUAL_ATOM_STR));
  
  /** URI reference for a datatype-valued property atom. */
  public static final URIReferenceImpl DATA_ATOM = new URIReferenceImpl(URI.create(DATA_ATOM_STR));
  
  /** URI reference for an individual identity atom. */
  public static final URIReferenceImpl SAME_INDIVIDUALS_ATOM = new URIReferenceImpl(URI.create(SAME_INDIVIDUALS_ATOM_STR));
  
  /** URI reference for an individual difference atom. */
  public static final URIReferenceImpl DIFFERENT_INDIVIDUALS_ATOM = new URIReferenceImpl(URI.create(DIFFERENT_INDIVIDUALS_ATOM_STR));
  
  /** URI reference for a builtin atom. */
  public static final URIReferenceImpl BUILTIN_ATOM = new URIReferenceImpl(URI.create(BUILTIN_ATOM_STR));
  
  /** URI reference for a data range atom. */
  public static final URIReferenceImpl DATA_RANGE_ATOM = new URIReferenceImpl(URI.create(DATA_RANGE_ATOM_STR));
  
  /** URI reference for a class atom predicate. */
  public static final URIReferenceImpl CLASS_PREDICATE = new URIReferenceImpl(URI.create(CLASS_PREDICATE_STR));
  
  /** URI reference for an individual or datatype property atom predicate. */
  public static final URIReferenceImpl PROPERTY_PREDICATE = new URIReferenceImpl(URI.create(PROPERTY_PREDICATE_STR));
  
  /** URI reference for a builtin atom operator. */
  public static final URIReferenceImpl BUILTIN = new URIReferenceImpl(URI.create(BUILTIN_STR));
  
  /** URI reference for a data range atom predicate. */
  public static final URIReferenceImpl DATA_RANGE = new URIReferenceImpl(URI.create(DATA_RANGE_STR));
  
  /** URI reference to identify the first argument to an atom. */
  public static final URIReferenceImpl ARG_1 = new URIReferenceImpl(URI.create(ARG_1_STR));
  
  /** URI reference to identify the second argument to an atom. */
  public static final URIReferenceImpl ARG_2 = new URIReferenceImpl(URI.create(ARG_2_STR));
  
  /** URI reference to identify the argument list for a builtin atom. */
  public static final URIReferenceImpl ARGS = new URIReferenceImpl(URI.create(ARGS_STR));
  
}
