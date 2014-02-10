Mulgara Tests

The contents of this directory come from:
http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfs-subPropertyOf-semantics/

The nt files are equivalent to the rdf files and would normally be used for
parser testing.

Tests are performed by loading the initial data, performing RDFS entailment,
and querying for expected results.  While the expected results are found in
the test002.rdf files in this directory, the tests are based in iTQL, and
the resulting data is explicitly searched for in the test script.

The test002.rdf, test002.nt and the manifest files are all left in this
directory for completeness and to help explain the machanics of the tests.

