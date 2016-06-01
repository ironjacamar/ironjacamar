#!/bin/sh
java -classpath ../../lib/ironjacamar-common.jar:../../lib/ironjacamar-spec-api.jar:../../lib/ironjacamar-validator.jar:../../lib/jboss-logging.jar:../../lib/jandex.jar:../../lib/validation-api.jar:../../lib/hibernate-validator.jar org.ironjacamar.validator.cli.Main $*
