@echo off

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=run.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
  if exist "%JAVA_HOME%\lib\tools.jar" (
    set "JAVAC_JAR=%JAVA_HOME%\lib\tools.jar"
  )
)

if "%JBOSS_HOME%" == "" set JBOSS_HOME=%DIRNAME%\..

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

rem Shared libs
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JAVAC_JAR%
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-spi.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-common.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-framework.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/activation.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/getopt.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossall-client.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/log4j.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/mail.jar

rem Shared jaxws libs
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxb-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxb-impl.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxb-xjc.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxws-tools.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxws-rt.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/policy.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/streambuffer.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/stax-ex.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxws-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jsr181-api.jar

rem Stack specific dependencies
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jboss-xml-binding.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossxb.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/javassist.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossall-client.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-native-client.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-native-core.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxrpc-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/saaj-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/wscommons-policy.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/wsdl4j.jar

rem Execute the command
"%JAVA%" %JAVA_OPTS% -Djava.endorsed.dirs="%JBOSS_ENDORSED_DIRS%" -classpath "%WSPROVIDE_CLASSPATH%" org.jboss.wsf.spi.tools.cmd.WSProvide %*
