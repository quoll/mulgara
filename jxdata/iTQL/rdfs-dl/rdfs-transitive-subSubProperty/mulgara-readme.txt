This test is not relevant to RDFS because arbitrary properties cannot be
declared as transitive.  These tests cannot be applied to the existing
transitive properies (rdfs:subClassOf and rdfs:subPropertyOf) as these
properties should not have their types modified (by giving them a super
type) and all sub properties will be given the semantics and entailments
of the rdfs properties they inherit from.
