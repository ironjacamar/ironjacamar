#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBoss JCA Script                                                        ##
##                                                                          ##
### ====================================================================== ###

### $Id: $ ###

DIRNAME=`dirname $0`

# Setup JBOSS_JCA_HOME
if [ "x$JBOSS_JCA_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_JCA_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_JCA_HOME

# Setup the java endorsed dirs
JBOSS_JCA_ENDORSED_DIRS="$JBOSS_JCA_HOME/lib/endorsed"

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# Setup the JVM options
JAVA_OPTS="$JAVA_OPTS -Xmx512m"

# Display the environment
echo "========================================================================="
echo ""
echo "  JBoss JCA"
echo ""
echo "  JBOSS_JCA_HOME: $JBOSS_JCA_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

# Start JBoss JCA
"$JAVA" $JAVA_OPTS \
    -Djava.endorsed.dirs="$JBOSS_JCA_ENDORSED_DIRS" \
    -Dorg.jboss.logging.Logger.pluginClass=org.jboss.logging.logmanager.LoggerPluginImpl \
    -Dlog4j.defaultInitOverride=true \
    -jar ironjacamar-standalone.jar "$@"
