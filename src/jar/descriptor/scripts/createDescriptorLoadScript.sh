#!/bin/sh

# 1 pass in hostname to use
# 2 pass in the path of the dir where the rdf to be loaded is
# 3 pass in scheme specific part up to and including the descriptors dir i.e. the mapping of 2

DESCRIPTOR="<rmi://"$1"/server1#descriptors>"
# we create,drop, create to stop errors from dropping if theres no model
# creating twice causes no problems
echo "create $DESCRIPTOR;"
echo "drop $DESCRIPTOR;"
echo "create $DESCRIPTOR;"

#maps local to remote (e.g. http/file)
pattern="s@$2@$3@"

# create schema load statements
for i in $2/*/*.xsl
do 
  echo "load <$i> into $DESCRIPTOR;" | sed `echo $pattern` 
done
