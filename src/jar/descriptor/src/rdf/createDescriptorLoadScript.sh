#!/bin/sh
DESCRIPTOR="<rmi://"`hostname -f`"/server1#descriptors>"
# we create,drop, create to stop errors from dropping if theres no model
# creating twice causes no problems
echo "create $DESCRIPTOR;"
echo "drop $DESCRIPTOR;"
echo "create $DESCRIPTOR;"
for i in $PWD/*.rdf $PWD/*.rdfs; 
do 
  echo "load rdf <file:"$i"> into $DESCRIPTOR;"; 
done
