/*
 * Copyright 2009 Fedora Commons, Inc.
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


package org.jrdf.vocabulary;

// Java 2 standard
import java.net.URI;

/**
 * A set of constants for the standard OWL vocabulary.
 *
 * @created 2009-1-28
 * @author <a href="mailto:gearon@ieee.org">Paul Gearon</a>
 */
public abstract class OWL extends Vocabulary {

  /** Generated serialization ID */
  private static final long serialVersionUID = 5397714222277720635L;

  /** The OWL namespace. */
  public static final URI BASE_URI = URI.create("http://www.w3.org/2002/07/owl#");

  /** Top. */
  public static final URI THING = URI.create(BASE_URI + "Thing");

  /** Bottom. */
  public static final URI NOTHING = URI.create(BASE_URI + "Nothing");

  /** The class of classes. */
  public static final URI CLASS = URI.create(BASE_URI + "Class");

  /** The class of things that are all different. */
  public static final URI ALL_DIFFERENT = URI.create(BASE_URI + "AllDifferent");

  /** The class of things that are restricted. */
  public static final URI RESTRICTION = URI.create(BASE_URI + "Restriction");

  /** The class of object properties. */
  public static final URI OBJECT_PROPERTY = URI.create(BASE_URI + "ObjectProperty");

  /** The class of datatype properties (where the value is a literal). */
  public static final URI DATATYPE_PROPERTY = URI.create(BASE_URI + "DatatypeProperty");

  /** The class of transitive object properties. */
  public static final URI TRANSITIVE_PROPERTY = URI.create(BASE_URI + "TransitiveProperty");

  /** The class of symmetric object properties. */
  public static final URI SYMMETRIC_PROPERTY = URI.create(BASE_URI + "SymmetricProperty");

  /** The class of functional object properties. */
  public static final URI FUNCTIONAL_PROPERTY = URI.create(BASE_URI + "FunctionalProperty");

  /** The class of inverse functional object properties. */
  public static final URI INVERSE_FUNCTIONAL_PROPERTY = URI.create(BASE_URI + "InverseFunctionalProperty");

  /** The class of annotation properties. */
  public static final URI ANNOTATION_PROPERTY = URI.create(BASE_URI + "AnnotationProperty");

  /** The class of ontologies. */
  public static final URI ONTOLOGY = URI.create(BASE_URI + "Ontology");

  /** The class properties of an ontology. */
  public static final URI ONTOLOGY_PROPERTY = URI.create(BASE_URI + "OntologyProperty");

  /** The class for classes that should no longer be used. */
  public static final URI DEPRECATED_CLASS = URI.create(BASE_URI + "DeprecatedClass");

  /** The class for properties that should no longer be used. */
  public static final URI DEPRECATED_PROPERTY = URI.create(BASE_URI + "DeprecatedProperty");

  /** The class for a range of data that a property can refer to. */
  public static final URI DATA_RANGE = URI.create(BASE_URI + "DataRange");

  ////////////////////////////////////
  // Properties
  ////////////////////////////////////

  /** Equivalent class property. */
  public static final URI EQUIVALENT_CLASS = URI.create(BASE_URI + "equivalentClass");

  /** Disjoint With property. */
  public static final URI DISJOINT_WITH = URI.create(BASE_URI + "disjointWith");

  /** Equivalent property property. */
  public static final URI EQUIVALENT_PROPERTY = URI.create(BASE_URI + "equivalentProperty");

  /** Same As property. */
  public static final URI SAME_AS = URI.create(BASE_URI + "sameAs");

  /** Different From property. */
  public static final URI DIFFERENT_FROM = URI.create(BASE_URI + "differentFrom");

  /** Distinct Members property. */
  public static final URI DISTINCT_MEMBERS = URI.create(BASE_URI + "distinctMembers");

  /** A property for a union of other classes. */
  public static final URI UNION_OF = URI.create(BASE_URI + "unionOf");

  /** A property for an intersection of other classes. */
  public static final URI INTERSECTION_OF = URI.create(BASE_URI + "intersectionOf");

  /** A property for the complement of another class. */
  public static final URI COMPLEMENT_OF = URI.create(BASE_URI + "complementOf");

  /** A property for an enumeration class. */
  public static final URI ONE_OF = URI.create(BASE_URI + "oneOf");

  /** A property for associating a restriction with a property. */
  public static final URI ON_PROPERTY = URI.create(BASE_URI + "onProperty");

  /** A restriction property for the universal qualifier. */
  public static final URI ALL_VALUES_FROM = URI.create(BASE_URI + "allValuesFrom");

  /** A restriction property for specifying a property value. */
  public static final URI HAS_VALUE = URI.create(BASE_URI + "hasValue");

  /** A restriction property for the existential qualifier. */
  public static final URI SOME_VALUES_FROM = URI.create(BASE_URI + "someValuesFrom");

  /** A restriction property for minimal numeric cardinality. */
  public static final URI MIN_CARDINALITY = URI.create(BASE_URI + "minCardinality");

  /** A restriction property for maximum numeric cardinality. */
  public static final URI MAX_CARDINALITY = URI.create(BASE_URI + "maxCardinality");

  /** A restriction property synonymous with max and min cardinality set to the same value. */
  public static final URI CARDINALITY = URI.create(BASE_URI + "cardinality");

  /** A property for describing that two properties are the inverse of each other. */
  public static final URI INVERSE_OF = URI.create(BASE_URI + "inverseOf");

  /** An ontology property for importing another ontology. */
  public static final URI IMPORTS = URI.create(BASE_URI + "imports");

  /** An annotation property for versioning. */
  public static final URI VERSION_INFO = URI.create(BASE_URI + "versionInfo");

  /** An ontology property for describing a previous version. */
  public static final URI PRIOR_VERSION = URI.create(BASE_URI + "priorVersion");

  /** An ontology property for describing compatibility with a previous version. */
  public static final URI BACKWARD_COMPATIBLE_WITH = URI.create(BASE_URI + "backwardCompatibleWith");

  /** An ontology property for describing incompatibility with a previous version. */
  public static final URI INCOMPATIBLE_WITH = URI.create(BASE_URI + "incompatibleWith");

  static {
    // Add Classes
    classes.add(THING);
    classes.add(NOTHING);
    classes.add(CLASS);
    classes.add(ALL_DIFFERENT);
    classes.add(RESTRICTION);
    classes.add(OBJECT_PROPERTY);
    classes.add(DATATYPE_PROPERTY);
    classes.add(TRANSITIVE_PROPERTY);
    classes.add(SYMMETRIC_PROPERTY);
    classes.add(FUNCTIONAL_PROPERTY);
    classes.add(INVERSE_FUNCTIONAL_PROPERTY);
    classes.add(ANNOTATION_PROPERTY);
    classes.add(ONTOLOGY);
    classes.add(ONTOLOGY_PROPERTY);
    classes.add(DEPRECATED_CLASS);
    classes.add(DEPRECATED_PROPERTY);
    classes.add(DATA_RANGE);

    // Add Properties
    properties.add(EQUIVALENT_CLASS);
    properties.add(DISJOINT_WITH);
    properties.add(EQUIVALENT_PROPERTY);
    properties.add(SAME_AS);
    properties.add(DIFFERENT_FROM);
    properties.add(DISTINCT_MEMBERS);
    properties.add(UNION_OF);
    properties.add(INTERSECTION_OF);
    properties.add(COMPLEMENT_OF);
    properties.add(ONE_OF);
    properties.add(ON_PROPERTY);
    properties.add(ALL_VALUES_FROM);
    properties.add(HAS_VALUE);
    properties.add(SOME_VALUES_FROM);
    properties.add(MIN_CARDINALITY);
    properties.add(MAX_CARDINALITY);
    properties.add(CARDINALITY);
    properties.add(INVERSE_OF);
    properties.add(IMPORTS);
    properties.add(VERSION_INFO);
    properties.add(PRIOR_VERSION);
    properties.add(BACKWARD_COMPATIBLE_WITH);
    properties.add(INCOMPATIBLE_WITH);

    resources.addAll(classes);
    resources.addAll(properties);
  }
}
