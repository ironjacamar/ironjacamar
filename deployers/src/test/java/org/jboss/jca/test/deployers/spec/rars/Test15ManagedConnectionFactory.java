/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars;

import java.io.PrintWriter;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ResourceAllocationException;
import javax.resource.spi.SecurityException;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * Test15ManagedConnectionFactory
 *
 * @author  <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>.
 * @version $Revision: $
 */
public class Test15ManagedConnectionFactory implements
      ManagedConnectionFactory
{
   private static final long serialVersionUID = 1L;
   private static Logger log = Logger.getLogger(Test15ManagedConnectionFactory.class);

   /**
    * Creates a Connection Factory instance. 
    *
    *  @param    cxManager    ConnectionManager to be associated with created EIS connection factory instance
    *  @return   EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    *  @throws   ResourceException     Generic exception
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      log.debug("call createConnectionFactory");
      return null;
   }

   /**
    * Creates a Connection Factory instance. 
    *
    *  @return   EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    *  @throws   ResourceException     Generic exception
    */
   public Object createConnectionFactory() throws ResourceException
   {
      log.debug("call createConnectionFactory");
      return null;
   }

   /** 
    * Creates a new physical connection to the underlying EIS resource manager.
    *
    *  @param   subject        Caller's security information
    *  @param   cxRequestInfo  Additional resource adapter specific connection request information
    *  @throws  ResourceException     generic exception
    *  @return  ManagedConnection instance
    */
   public ManagedConnection createManagedConnection(Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.debug("call createManagedConnection");
      return null;
   }

   /** 
    * Returns a matched connection from the candidate set of connections. 
    *  @param   connectionSet   candidate connection set
    *  @param   subject         caller's security information
    *  @param   cxRequestInfo   additional resource adapter specific connection request information  
    *
    *  @throws  ResourceException     generic exception
    *  @return  ManagedConnection     if resource adapter finds an acceptable match otherwise null
    **/
   public ManagedConnection matchManagedConnections(Set connectionSet,
      Subject subject, ConnectionRequestInfo cxRequestInfo)
      throws ResourceException
   {
      log.debug("call matchManagedConnections");
      return null;
   }

   /** 
    * Get the log writer for this ManagedConnectionFactory instance.
    *  @return  PrintWriter
    *  @throws  ResourceException     generic exception
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      log.debug("call getLogWriter");
      return null;
   }

   /** 
    * Set the log writer for this ManagedConnectionFactory instance.</p>
    *
    *  @param   out PrintWriter - an out stream for error logging and tracing
    *  @throws  ResourceException     generic exception
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      log.debug("call setLogWriter");

   }

}
