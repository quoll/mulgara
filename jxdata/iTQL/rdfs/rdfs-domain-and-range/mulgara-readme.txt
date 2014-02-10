Mulgara Tests

The contents of this directory come from:
http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-domain-and-range/

These tests do not include the parser tests, as they are handled elsewhere.

The positive entailment tests are based on test001.rdf, test002.rdf and
test003.rdf resulting in the data in test004.rdf.  The n3 files are equivalent
to the rdf files and would normally be used for parser testing.

The negative entailment tests are based on premises005.rdf and
premises006.rdf.  Entailment on these graphs should not lead to statements
found in nonconclusions005.rdf and nonconclusions006.rdf.

Tests are performed by loading the initial data, performing RDFS entailment,
and querying for expected results.  While the expected results (or
non-results) are found in the rdf files in this directory, the tests are
based in iTQL, and the resulting data is explicitly searched for in the
test script.

The resulting data, the axioms, and the manifest files are all left in this
directory for completeness and to help explain the machanics of the tests.

