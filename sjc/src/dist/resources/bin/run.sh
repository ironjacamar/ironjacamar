#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  IronJacamar Script                                                      ##
##                                                                          ##
### ====================================================================== ###

### $Id: $ ###

DIRNAME=`dirname $0`

# Setup IRONJACAMAR_HOME
if [ "x$IRONJACAMAR_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    IRONJACAMAR_HOME=`cd $DIRNAME/..; pwd`
fi
export IRONJACAMAR_HOME

# Setup the java endorsed dirs
IRONJACAMAR_ENDORSED_DIRS="$IRONJACAMAR_HOME/lib/endorsed"

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

# Setup the JVM options
JAVA_OPTS="$JAVA_OPTS"

# Display the environment
echo "========================================================================="
echo ""
echo "  IronJacamar"
echo ""
echo "  IRONJACAMAR_HOME: $IRONJACAMAR_HOME"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

# Start IronJacamar
"$JAVA" $JAVA_OPTS \
    -Djava.endorsed.dirs="$IRONJACAMAR_ENDORSED_DIRS" \
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
    -Dorg.jboss.logging.Logger.pluginClass=org.jboss.logging.logmanager.LoggerPluginImpl \
    -Dlog4j.defaultInitOverride=true \
    -jar ../lib/ironjacamar-sjc.jar "$@"
