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

package org.jboss.jca.core;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

/**
 * The core logger.
 *
 * Message ids ranging from 00001 to 05000 inclusively.
 */
@MessageLogger(projectCode = "IJ")
public interface CoreLogger extends BasicLogger
{

   // CACHED CONNECTION MANAGER (100)

   /**
    * Closing connection
    * @param handle The hande
    */
   @LogMessage(level = INFO)
   @Message(id = 100, value = "Closing a connection for you.  Please close them yourself: %s")
   public void closingConnection(Object handle);

   /**
    * Closing connection
    * @param handle The hande
    * @param t The exception
    */
   @LogMessage(level = INFO)
   public void closingConnection(Object handle, @Cause Throwable t);

   /**
    * Closing connection results in throwable
    * @param t The exception
    */
   @LogMessage(level = INFO)
   @Message(id = 102, value = "Throwable trying to close a connection for you, please close it yourself")
   public void closingConnectionThrowable(@Cause Throwable t);

   /**
    * No close method for closing connection
    * @param clz The class name
    */
   @LogMessage(level = INFO)
   @Message(id = 103, value = "Could not find a close method on alleged connection object (%s). " +
            "Please close your own connections.")
   public void closingConnectionNoClose(String clz);
}
