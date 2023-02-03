#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  IronJacamar Script                                                      ##
##                                                                          ##
### ====================================================================== ###

### $Id: $ ###

DIRNAME=`dirname $0`

# Setup IRON_JACAMAR_HOME
if [ "x$IRON_JACAMAR_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    IRON_JACAMAR_HOME=`cd $DIRNAME/..; pwd`
fi
export IRON_JACAMAR_HOME

# Setup the java endorsed dirs
IRON_JACAMAR_ENDORSED_DIRS="$IRON_JACAMAR_HOME/lib/endorsed"

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
echo "  IronJacamar"
echo ""
echo "  IRON_JACAMAR_HOME: $IRON_JACAMAR_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

# Start IronJacamar
"$JAVA" $JAVA_OPTS \
    -Djava.endorsed.dirs="$IRON_JACAMAR_ENDORSED_DIRS" \
    -Dorg.jboss.logging.Logger.pluginClass=org.jboss.logging.logmanager.LoggerPluginImpl \
    -Dlog4j.defaultInitOverride=true \
    -jar ironjacamar-sjc.jar "$@"
