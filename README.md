Mulgara Semantic Store (Mulgara) Installation Guide
===================================================

Table of Contents

 * [Introduction][introduction]
 ** [Directory Layout][directory-layout]
 ** [Release Notes][release-notes]
 * [Installing Java][installing-java]
 * [Building Mulgara][building-mulgara]
 ** [Building Mulgara in Eclipse][building-a-mulgara-server]
 * [Running a Mulgara Server][running-a-mulgara-server]
 * [Mulgara Server Options][mulgara-server-options]
 * [License][license]


# Introduction
===============

Mulgara Semantic Store is a directed graph daatabase designed to store metadata in
a highly scalable, transaction safe environment.

## Directory Layout
-------------------

 * bin -     Executables
 * conf -    Build configuration templates (read only)
 * data -    Test data (read only)
 * dist -    Distributable product
 * doc -     Documentation, both sources and generated
 * lib -     Components external to this project (read only)
 * obj -     Generated files
 * rules -   Documentation for using rules, and RDFS rule data
 * scripts - Scripts used by the build process
 * src -     Source code
 * test -    Test results (auto generated)

ii. Release Notes
-----------------

New Features:
* New Transaction framework fixing several Concurrent Modification bugs, and
  ensuring ACID compliance.
* A RelationalResolver that provides access to sql databases via a D2RQ mapping.
* A DistributedResolver allowing selections from multiple servers in one query,
  and also permitting insert/select queries between multiple servers.
* TQL now supports triple patterns with Predicate-Object lists and Object lists,
  similarly to SPARQL..
* Removal of debug code, resulting in significant speedup and lower memory usage.
* Improved documentation.
* Added datatypes to XML results.
* Added support for inserting explicit URIs/Literals in an insert/select query.
* Found and fixed several resource leaks.
* Fixed Non-Union-Compatible OR queries (Disjunctions).
* Fixed bug where on disk data can be momentarily inconsistent, destroying data
  if the operating system crashes at the wrong moment.
* Fixed inconsistent RMI access for streaming results over the network.
* Fixed class loading bug when a non-system class loader has been loaded.
* Fixed certain types of queries (like MINUS) failing when intermediate results
  are empty.
* Fixed imports of blank nodes in N3 documents.
* Improved dependencies in build process.
* Build now has meaningful build IDs.
* Cleanup of a lot of redundant code.


Known Bugs:
* Not all bugs yet brought forward from Kowari. MGR-3
* Models are currently tied to server names. MGR-58
* AVLNodes may be released more than once.  Currently being hidden. MGR-63
* Blank nodes from different servers need server IDs incorporated into them.
  MGR-56
* Cannot access WebUI while server is being queried and inserts and deletes
  are being performed. MGR-11
* Large temporary files being created with certain usage patterns. MGR-8
* Unimplemented security layer being bypassed. This will be an issue when
  Security is implemented for Mulgara. MGR-31
* Transactions need to be properly cleaned up during exception failures, so
  logging will be more useful. MGR-52
* Intermittent OOM errors while running full test suite on OS X for PPC
  architectures. MGR-67
* Intermittent OOM errors in FileSystemResolver unit test. MGR-53
* Poor performance due to non-optimal ordering after disjunctions.

The list of bugs may be browsed (and updated) at:
http://mulgara.org/jira/secure/Dashboard.jspa

II. Installing Java
===================

Download a J2SE 1.5.X (or higher) for your platform from http://java.sun.com/j2se/,
and install it. Installation instructions for Windows and Linux are
available. You should then check that the installation added the java
commands to your path by typing:

```bash
$ java -version
```

You should get something like the following:

```
java version "1.5.0_07"
Java(TM) 2 Runtime Environment, Standard Edition (build 1.5.0_07-164)
Java HotSpot(TM) Client VM (build 1.5.0_07-87, mixed mode, sharing)
```

If your shell reports that it cannot find the command, add <JAVA_HOME>/bin
(where JAVA_HOME is the location where you installed J2SE to) to your path
in the appropriate way for your shell.

Note. You must use a Java in the J2SE 1.5.x series for compiling and running
Mulgara.  Java 1.6.0 and above is not yet supported.


III. Building Mulgara
====================

If you have downloaded the binary distribution of Mulgara please skip this
section and go on to "Running a Mulgara Server".

To build Mulgara, you must either use build.sh (for Unix opeating systems) or
build.bat (for Windows).  You must have you JAVA_HOME enviroment variable
set in order for the script to work or modify the script to point to your
current installation of Java.

To build the distribution in Unix:
$ ./build.sh dist

To build the distribution in Windows:
C:\Mulgara\> build dist

ii. Building Mulgara in Eclipse
-------------------------------

The Mulgara sources include the .project and .classpath files required for
Eclipse.  However for an error free environment Eclipse also requires access
to files which are only generated during a build.  To overcome this there is
an extra ANT target which builds a library containing the required classes
for the Eclipse IDE.

To build this library from a Unix command line:

```bash
$ ./build.sh ideSupport
```

To build the library in Windows:

```bash
C:\Mulgara\> build ideSupport
```

After building the library, do a Refresh (F5) in Eclipse to make the library
available to the project.


IV. Running a Mulgara Server
============================

The Mulgara server is currently run from a shell script under Linux or a batch
file under Windows. To start the server using this script, you'll need to do
the following:

Note. This assumes PATH has been set to the
C:\Program Files\Java\j2re1.5.0\bin directory.

1. Change to the Mulgara directory:
```bash
$ cd <mulgarahome>
```

Note. If the directory does not exist create one and copy the mulgara-1.1.0.jar
and itql-1.1.0.jar into it.

2. Start the executable JAR :

```bash
$ cd <mulgarahome>
$ java -jar mulgara-1.1.0.jar
```

Once you see the following line appear in the console the server is ready to be used.

```
11:01:47.763 EVENT Started SocketListener on 0.0.0.0:8080
```

However, if the following message appears then the HTTP port is already occupied by
another process. Please refer to Mulgara Server options to change this
configuration.

```
2004-04-23 11:20:23,823 ERROR EmbeddedMulgaraServer - java.net.BindException: Address already in use
```

To verify your installation is working correctly open your browser and enter the following URL

```
http://localhost:8080
```

Your HTTP port may be different if you have supplied a -p option.

Follow the links to the user documentation to learn more about using Mulgara.


V. Mulgara Server Options
=========================

You can change the basic Mulgara server options by suppling them as arguments
to the startup command. To view the basic options supply the --help option.

```bash
$ java -jar mulgara-1.1.0.jar --help
```

This will return the following options :

```
-h, --help          display this help screen
-n, --normi         disable automatic starting of the RMI registry
-x, --shutdown      shutdown the local running server
-l, --logconfig     use an external logging configuration file
-c, --serverconfig  use an external server configuration file
-k, --serverhost    the hostname to bind the server to
-o, --httphost      the hostname for HTTP requests
-p, --port          the port for HTTP requests
-r, --rmiport       the RMI registry port
-s, --servername    the (RMI) name of the server
-a, --path          the path server data will persist to, specifying
                    '.' or 'temp' will use the current working directory
                    or the system temporary directory respectively
-m, --smtp          the SMTP server for email notifications
```

Since Mulgara has an embedded HTTP server you may have a conflict with an
existing HTTP running on port 8080. For example, to change the HTTP port of
the Mulgara server to 8081

```bash
$ java -jar mulgara-1.1.0.jar -p 8081
```

By default the database files are stored in the current directory under a
server1 directory. To change the location of the database files supply the
-a followed by a path. For example :

```bash
$java -jar mulgara-1.1.0.jar -a file:///usr/local/mulgara
```

Under Windows :

```bash
$java -jar mulgara-1.1.0.jar -a c:\mulgara-data
```

VI. License
==========

The Mulgara Semantic Store is licensed under the Open Software License
version 1.1 which is included with the distribution in a file called
LICENSE.txt.

Copyright (c) 2001-2004 Tucana Technologies, Inc. All rights reserved.
Copyright (c) 2005 Kowari Project. All rights reserved.
Copyright (c) 2006-2007 Mulgara Project. Some rights reserved.

Last updated on 10 February 2014

