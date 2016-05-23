/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.deployers;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import static org.jboss.logging.Logger.Level.INFO;

/**
 * The deployers logger.
 *
 * Message ids ranging from 020000 to 029999 inclusively.
 */
@MessageLogger(projectCode = "IJ2")
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
    * @param archiveName The archive name
    */
   @LogMessage(level = INFO)
   @Message(id = 20002, value = "Deployed: %s")
   public void deployed(String archiveName);

   /**
    * Changed TransactionSupport
    * @param jndiName The JNDI name
    */
   @LogMessage(level = INFO)
   @Message(id = 20003, value = "Changed TransactionSupport for %s")
   public void changedTransactionSupport(String jndiName);

}
