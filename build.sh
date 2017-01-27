#!/bin/sh


#-----------------------------------------------------------------------------
# Build script for Mulgara
#
# $Id: build.sh,v 1.5 2004/12/22 05:00:31 newmana Exp $
#-----------------------------------------------------------------------------

# The name of the build file to use
BUILDFILE=build.xml

# The default compiler to use
JAVAC=classic

# Root directory for the project
PROJECTDIR=.

# Directory contain jars required for runtime
LIBDIR="${PROJECTDIR}/lib"

# Directory contain jars required for compilation
BUILDDIR="${PROJECTDIR}/lib"

# set the base directory
if [ "$BASEDIR" = "" ]; then
  BASEDIR=/
fi

# Define ANT_HOME is necessary
if [ "$ANT_HOME" = "" ]; then
  ANT_HOME=${BUILDDIR}
fi

#--------------------------------------------
# No need to edit anything past here
#--------------------------------------------

if test -z "${JAVA_HOME}" ; then
  # JAVA_HOME is not set, try to set it if java is in PATH
  echo "ERROR: JAVA_HOME not found in your environment."
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit
fi

PATH="${JAVA_HOME}/bin:$PATH"

# Try to find Java Home directory, from JAVA_HOME environment
# or java executable found in PATH

JAVABIN="${JAVA_HOME}/bin/java"

# convert the existing path to unix
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
   JAVA_HOME=`cygpath --path --unix "$JAVA_HOME"`
fi

# Define the java executable path
if [ "$JAVABIN" = "" ] ; then
  JAVABIN=${JAVA_HOME}/bin/java
fi


# Add the ant libraries to the classpath
CLASSPATH="${CLASSPATH}:lib/ant-1.9.7.jar:lib/bsf-2.3.0.jar"
CLASSPATH="${CLASSPATH}:lib/ant-launcher-1.9.7.jar:lib/junit-3.8.1.jar"
CLASSPATH="${CLASSPATH}:lib/ant-junit-1.9.7.jar:lib/ant-apache-bsf-1.9.7.jar"
CLASSPATH="${CLASSPATH}:lib/js-1.5r3.jar"
CLASSPATH="${CLASSPATH}:lib/javacc.jar"

# Add known external dependencies
#for lib in ${JAR_DEPENDENCIES}
#do
#    CLASSPATH=${CLASSPATH}:${lib}
#done

# Try to include tools.jar for compilation
if test -f "${JAVA_HOME}/lib/tools.jar" ; then
    CLASSPATH="${CLASSPATH}:${JAVA_HOME}/lib/tools.jar"
fi

JAVAC="${JAVA_HOME}/bin/javac"

# If jikes is in PATH, use jikes
#if type jikes >/dev/null 2>/dev/null
#then
#    JAVAC=jikes
#fi

#echo "Java Home: ${JAVA_HOME}"
#echo "Java compiler ${JAVAC}"
#echo "Java buildpath ${BUILDCLASSPATH}"
#echo "Java libpath ${LIBCLASSPATH}"
#echo "Now building ${TARGET} with file ${BUILDFILE}..."
#echo Classpath="${CLASSPATH}"
#export CLASSPATH

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
   JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

ARCH="-Dnoop"
OSNAME=`echo $OSTYPE | cut -c 1-7`
if [ "$OSNAME" = "solaris" ] ; then
   SYSTYPE=`uname -m`
   if [ "$SYSTYPE" = "sun4u" ] ; then
      ARCH="-d64"
   else
      ARCH="-d32"
   fi
fi

if [ "$TARGET_VER" = "" ] ; then
  TARGET_VER="1.6"
fi

# Call Ant
"${JAVABIN}" ${ARCH} -Xms64m -Xmx256m "-Dant.home=${ANT_HOME}" "-DJAVAC=${JAVAC}" \
           -Darch.bits=${ARCH} \
           "-Ddir.base=${BASEDIR}" \
           -Dant.build.javac.target=${TARGET_VER} \
           -classpath "${CLASSPATH}" org.apache.tools.ant.Main \
           -buildfile "${BUILDFILE}" "$@"
