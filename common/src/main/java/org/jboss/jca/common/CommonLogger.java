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

package org.jboss.jca.common;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.ERROR;

/**
 * The common logger.
 *
 * Message ids ranging from 05001 to 10000 inclusively.
 */
@MessageLogger(projectCode = "IJ")
public interface CommonLogger extends BasicLogger
{
   /**
    * Parsing error for ra.xml
    * @param url The url
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 05001, value = "Parsing error of ra.xml file: %s")
   public void parsingErrorRaXml(String url, @Cause Throwable t);

   /**
    * Parsing error for ironjacamar.xml
    * @param url The url
    * @param t The exception
    */
   @LogMessage(level = ERROR)
   @Message(id = 05002, value = "Parsing error of ironjacamar.xml file: %s")
   public void parsingErrorIronJacamarXml(String url, @Cause Throwable t);

   /**
    * No connector
    */
   @LogMessage(level = ERROR)
   @Message(id = 05003, value = "No @Connector was found and no definition in the ra.xml metadata either")
   public void noConnector();

   /**
    * More than one connector
    */
   @LogMessage(level = ERROR)
   @Message(id = 05004, value = "More than one @Connector was found but the correct one " +
            "wasn't defined in the ra.xml metadata")
   public void moreThanOneConnector();
}
