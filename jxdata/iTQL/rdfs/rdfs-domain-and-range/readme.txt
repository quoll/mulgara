Issue rdfs-domain-and-range:
---------------------------
Should a property be allowed more than one rdfs:range property?
What should the semantics of multiple domain and range properties be?

-> Multiple domain and range constraints are permissable
   and will have conjunctive semantics.

test001
  describes a property with rdfs:domain the intersection of 2 domains
test002
  describes a property with rdfs:range the intersection of 2 ranges
test003
  sample statement
test004
  entailed description using test001, test002, test003 and
  the rules for RDF and RDFS entailment
  (see http://www.w3.org/2000/10/rdf-tests/rdfcore/entailment/ )

# Suppose we have
#   ex:baz1 ex:bar ex:baz2 .        #test003
# and
#   ex:bar rdfs:domain ex:Domain1 . #test001
# and
#   ex:bar rdfs:domain ex:Domain2 . #test001
#
# then we can RDFS entail
#   ex:baz1  rdf:type ex:Domain1 .  #test004
# and
#   ex:baz1  rdf:type ex:Domain2 .  #test004
#
# Now, it is that last 'and' that expresses
# those 'conjunctive semantics' i.e.
#   ex:baz1 in in the intersection of ex:Domain1 and ex:Domain2
#
# The same story for rdfs:range
#
