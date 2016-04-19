#!/bin/sh
java -classpath ./ironjacamar-tracer.jar:../../lib/ironjacamar-core.jar org.ironjacamar.tracer.HTMLReport $*
