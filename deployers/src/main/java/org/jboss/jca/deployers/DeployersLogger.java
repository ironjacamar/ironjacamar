/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jca.deployers;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The deployers logger.
 *
 * Message ids ranging from 020000 to 029999 inclusively.
 */
@MessageLogger(projectCode = "IJ")
public interface DeployersLogger extends BasicLogger
{
   // ABSTRACT RESOURCE ADAPTER DEPLOYER

   /**
    * Required license terms
    * @param url The license url
    */
   @LogMessage(level = INFO)
   @Message(id = 20001, value = "Required license terms for %s")
   public void requiredLicenseTerms(String url);

   /**
    * Deployed
    * @param url The url
    */
   @LogMessage(level = INFO)
   @Message(id = 20002, value = "Deployed: %s")
   public void deployed(String url);

   /**
    * Validation report failure
    * @param detail The details
    * @param t The exception
    */
   @LogMessage(level = WARN)
   @Message(id = 20003, value = "Failure during validation report generation: %s")
   public void validationReportFailure(String detail, @Cause Throwable t);

   /**
    * Connection definition mismatch
    * @param clz The class name
    */
   @LogMessage(level = WARN)
   @Message(id = 20004, value = "Only one connection definition found with a mismatch in class-name: %s")
   public void connectionDefinitionMismatch(String clz);

   /**
    * Admin object mismatch
    * @param clz The class name
    */
   @LogMessage(level = WARN)
   @Message(id = 20005, value = "Only one admin object found with a mismatch in class-name: %s")
   public void adminObjectMismatch(String clz);

   /**
    * Null connection factory
    */
   @LogMessage(level = ERROR)
   @Message(id = 20006, value = "ConnectionFactory is null")
   public void nullConnectionFactory();

   /**
    * Subject ceration error
    * @param detail The details
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 20007, value = "Exception during createSubject(): %s")
   public void subjectCreationError(String detail, @Cause Throwable t);

   /**
    * Invalid config property
    * @param cp The config property name
    */
   @LogMessage(level = WARN)
   @Message(id = 20008, value = "Invalid config-property: %s")
   public void invalidConfigProperty(String cp);
}
