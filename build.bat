@echo off

REM -----------------------------------------------------------------------------
REM Build script for Mulgara
REM
REM -----------------------------------------------------------------------------


REM ---------------------------------------------------
REM Modify the following to change the default settings
REM The defaults should be OK for all configurations
REM ---------------------------------------------------

REM -- root directory for the project
set _PROJECTDIR=%PROJECTDIR%
set PROJECTDIR=.

REM -- Directory containing jars required for runtime
set _LIBDIR=%LIBDIR%
set LIBDIR=%PROJECTDIR%\lib

REM -- Directory containing jars required for compilation
set _BUILDDIR=%BUILDDIR%
set BUILDDIR=%PROJECTDIR%\lib

REM -- Name of the build file to use
set _BUILDFILE=%BUILDFILE%
set BUILDFILE=build.xml

REM -- Default compiler to use
set _JAVAC=%JAVAC%
set JAVAC=classic

REM -- External dependencies jar required for building this project
rem set _JAR_DEPENDENCIES=%JAR_DEPENDENCIES%
rem set JAR_DEPENDENCIES="%PROJECTDIR%\..\jakarta-site2\lib\jdom*.jar"

REM --------------------------------------------
REM No need to edit anything past here
REM --------------------------------------------

:init
set _CLASSPATH=%CLASSPATH%
set LOCALPATH=

:testant
if "%ANT_HOME%" == "" goto setant
goto buildpath

:setant
set ANT_HOME=%BUILDDIR%
goto buildpath

:buildpath
REM for %%l IN (%LIBDIR%\*.jar) DO call setlocalpath %%l
REM for %%l IN (%BUILDDIR%\*.jar) DO call setlocalpath %%l
REM for %%l IN (%JAR_DEPENDENCIES%) DO call setlocalpath %%l
set LOCALPATH=%LIBDIR%\ant-1.9.7.jar
set LOCALPATH=%LOCALPATH%;%LIBDIR%\bsf-2.3.0.jar
set LOCALPATH=%LOCALPATH%;%LIBDIR%\ant-launcher-1.9.7.jar
set LOCALPATH=%LOCALPATH%;%LIBDIR%\junit-3.8.1.jar
set LOCALPATH=%LOCALPATH%;%LIBDIR%\ant-junit-1.9.7.jar
set LOCALPATH=%LOCALPATH%;%LIBDIR%\ant-apache-bsf-1.9.7.jar
set LOCALPATH=%LOCALPATH%;%LIBDIR%\js-1.5r3.jar
set LOCALPATH=%LOCALPATH%;%LIBDIR%\javacc.jar

:testjavahome
if "%JAVA_HOME%" == "" goto setjavahome
goto testjikes

:setjavahome
if not "%OS%" == "Windows_NT" goto javahomeerror

:setjavahoment
for %%j IN (java.exe) DO set JAVABIN=%%~dp$PATH:j
if "%JAVABIN%" == "" goto javahomeerror
for %%j IN (%JAVABIN%..\) DO set JAVA_HOME=%%~dpj

:testjikes
if not "%OS%" == "Windows_NT" goto setjikes

:testjikesnt
for %%k IN (jikes.exe) DO set JIKES=%%~f$PATH:k

:setjikes
if not "%JIKES%" == "" set JAVAC=jikes

:build
if exist "%JAVA_HOME%\lib\tools.jar" set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar

"%JAVA_HOME%\bin\java.exe" -Xms64m -Xmx256m -classpath "%CLASSPATH%;%LOCALPATH%" -Dant.home="%ANT_HOME%" -DJAVAC=%JAVAC% org.apache.tools.ant.Main -buildfile %BUILDFILE% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto end

:javahomeerror
echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

set CLASSPATH=%_CLASSPATH%
set PROJECTDIR=%_PROJECTDIR%
set LIBDIR=%_LIBDIR%
set BUILDDIR=%_BUILDDIR%
set BUILDFILE=%_BUILDFILE%
set JAVAC=%_JAVAC%
set JAR_DEPENDENCIES=%_JAR_DEPENDENCIES%

endlocal
