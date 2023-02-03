#!/bin/sh
java -classpath ./ironjacamar-as.jar::../../lib/ironjacamar-core-impl.jar:../../lib/ironjacamar-core-api.jar org.jboss.jca.as.tracer.HTMLReport $*
