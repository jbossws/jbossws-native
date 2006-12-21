#!/bin/sh

# $Id: WSPUBLISH.sh 214 2006-04-20 17:43:45Z thomas.diesler@jboss.com $

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$TOMCAT_HOME" ] &&
        TOMCAT_HOME=`cygpath --unix "$TOMCAT_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi
export TOMCAT_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

#JPDA options. Uncomment and modify as appropriate to enable remote debugging .
#JAVA_OPTS="-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n $JAVA_OPTS"

# Setup JBoss sepecific properties
JAVA_OPTS="$JAVA_OPTS"

# Setup the WSPUBLISH classpath
WSPUBLISH_CLASSPATH="$WSPUBLISH_CLASSPATH:$TOMCAT_HOME/common/lib/jbossws-thirdparty.jar"
WSPUBLISH_CLASSPATH="$WSPUBLISH_CLASSPATH:$TOMCAT_HOME/common/lib/jbossws-core.jar"
WSPUBLISH_CLASSPATH="$WSPUBLISH_CLASSPATH:$TOMCAT_HOME/common/lib/log4j.jar"
WSPUBLISH_CLASSPATH="$WSPUBLISH_CLASSPATH:$TOMCAT_HOME/common/lib/servlet-api.jar"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    TOMCAT_HOME=`cygpath --path --windows "$TOMCAT_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    WSPUBLISH_CLASSPATH=`cygpath --path --windows "$WSPUBLISH_CLASSPATH"`
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  WSPUBLISH Environment"
echo ""
echo "  TOMCAT_HOME: $TOMCAT_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
#echo "  CLASSPATH: $WSPUBLISH_CLASSPATH"
#echo ""
echo "========================================================================="
echo ""

# Execute the JVM
"$JAVA" $JAVA_OPTS \
   -classpath "$WSPUBLISH_CLASSPATH" \
   org.jboss.ws.tools.wspublish -dest $TOMCAT_HOME/webapps "$@"

