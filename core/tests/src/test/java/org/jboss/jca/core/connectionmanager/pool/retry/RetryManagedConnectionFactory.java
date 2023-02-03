/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.pool.retry;

import java.io.PrintWriter;
import java.util.Set;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.RetryableUnavailableException;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * ManagedConnectionFactory
 */
public class RetryManagedConnectionFactory implements ManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Logger log = Logger.getLogger(RetryManagedConnectionFactory.class); 
   private Boolean retryable;
   private static int retryableCounter = 0;

   /**
    * Constructor
    */
   public RetryManagedConnectionFactory()
   {
      retryable = Boolean.TRUE;
   }

   /**
    * Set retryable
    * @param v The value
    */
   public void setRetryable(Boolean v)
   {
      this.retryable = v;
   }

   /**
    * Get retryable
    * @return The value
    */
   public Boolean getRetryable()
   {
      return retryable;
   }
   
   /**
    * Clear retryable counter
    */
   public static void clearRetryableCounter()
   {
      retryableCounter = 0;
   }

   /**
    * Get retryable counter
    * @return The value
    */
   public static int getRetryableCounter()
   {
      return retryableCounter;
   }
   
   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      return new RetryConnectionFactory(cm, this);
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("NYI");
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      if (Boolean.TRUE.equals(retryable))
      {
         retryableCounter++;
         throw new RetryableUnavailableException();
      }

      return new RetryManagedConnection(this);
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection matchManagedConnections(Set candidates, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      if (candidates.isEmpty()) 
         return null;

      return (ManagedConnection)candidates.iterator().next();
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return getClass().hashCode();
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      return (other != null) && (other.getClass() == getClass());
   }
}
