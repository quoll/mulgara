#!/usr/bin/env python

# The contents of this file are subject to the Mozilla Public License
# Version 1.1 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
# http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS"
# basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
# the License for the specific language governing rights and limitations
# under the License.
#
# The Original Code is the Kowari Metadata Store.
#
# The Initial Developer of the Original Code is Plugged In Software Pty
# Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
# created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
# Northrop Grumman Corporation. All Rights Reserved.
#
# This file is an original work and contains no Original Code.  It was
# developed by Netymon Pty Ltd under contract to the Australian 
# Commonwealth Government, Defense Science and Technology Organisation
# under contract #4500507038 and is contributed back to the Kowari/Mulgara
# Project as per clauses 4.1.3 and 4.1.4 of the above contract.
#
# Contributor(s): N/A.
#
# Copyright:
#   The copyright on this file is held by:
#     The Australian Commonwealth Government
#     Department of Defense
#   Developed by Netymon Pty Ltd
# Copyright (C) 2006
# The Australian Commonwealth Government
# Department of Defense
#
# [NOTE: The text of this Exhibit A may differ slightly from the text
# of the notices in the Source Code files of the Original Code. You
# should use the text of this Exhibit A rather than the text found in the
# Original Code Source Code for Your Modifications.]


# Generates an RDFXML file that contains a model of integer arithmetic.
# Skolemises both numbers and operations as well as the various arithmetic
# relations.

print \
"""<?xml version="1.0"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
         xmlns:math="math:">

  <rdf:Description rdf:nodeID="_add">
    <math:type rdf:resource="math:operator"/>
    <math:symbol>+</math:symbol>
  </rdf:Description>

  <rdf:Description rdf:nodeID="_mul">
    <math:type rdf:resource="math:operator"/>
    <math:symbol>*</math:symbol>
  </rdf:Description>
"""

MAX = 20

for i in range(0,MAX+1):
    print ''
    print '  <rdf:Description rdf:nodeID="_%d">' % (i,)
    print '     <math:type rdf:resource="math:number"/>'
    print '     <math:symbol>%d</math:symbol>' % (i,)
    print '  </rdf:Description>'

for i in range(0,MAX+1):
    for j in range(0,MAX+1):
        if i + j > MAX:
            break
        else:
            print ''
            print '  <rdf:Description>'
            print '    <math:type rdf:resource="math:equiv"/>'
            print '    <math:leftoperand rdf:nodeID="_%d"/>' % (i,)
            print '    <math:rightoperand rdf:nodeID="_%d"/>' % (j,)
            print '    <math:operator rdf:nodeID="_add"/>'
            print '    <math:result rdf:nodeID="_%d"/>' % (i+j,)
            print '  </rdf:Description>'
        if i * j > MAX:
            break
        else:
            print ''
            print '  <rdf:Description>'
            print '    <math:type rdf:resource="math:equiv"/>'
            print '    <math:leftoperand rdf:nodeID="_%d"/>' % (i,)
            print '    <math:rightoperand rdf:nodeID="_%d"/>' % (j,)
            print '    <math:operator rdf:nodeID="_mul"/>'
            print '    <math:result rdf:nodeID="_%d"/>' % (i*j,)
            print '  </rdf:Description>'

print '</rdf:RDF>'
