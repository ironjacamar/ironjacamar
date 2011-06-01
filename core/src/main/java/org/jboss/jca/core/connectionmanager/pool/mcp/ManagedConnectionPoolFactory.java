/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.listener.ConnectionListenerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * Factory to create a managed connection pool
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ManagedConnectionPoolFactory
{   
   /** Default implementation */
   private static final String DEFAULT_IMPLEMENTATION = 
      "org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreArrayListManagedConnectionPool";

   /** Default class definition */
   private static Class<?> defaultImplementation;

   static
   {
      String clz = SecurityActions.getSystemProperty("ironjacamar.mcp");

      if (clz != null && !clz.trim().equals(""))
      {
         clz = clz.trim();
      }
      else
      {
         clz = DEFAULT_IMPLEMENTATION;
      }

      try
      {
         defaultImplementation = Class.forName(clz, 
                                               true, 
                                               ManagedConnectionPoolFactory.class.getClassLoader());
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Unable to load default managed connection pool implementation: " + clz);
      }
   }

   /**
    * Constructor
    */
   public ManagedConnectionPoolFactory()
   {
   }

   /**
    * Create a managed connection pool using the default implementation strategy
    * 
    * @param mcf the managed connection factory
    * @param clf the connection listener factory
    * @param subject the subject
    * @param cri the connection request info
    * @param pc the pool configuration
    * @param p The pool
    * @return The initialized managed connection pool
    * @exception Throwable Thrown in case of an error
    */
   public ManagedConnectionPool create(ManagedConnectionFactory mcf, ConnectionListenerFactory clf, Subject subject,
                                       ConnectionRequestInfo cri, PoolConfiguration pc, Pool p) 
      throws Throwable
   {
      ManagedConnectionPool mcp = (ManagedConnectionPool)defaultImplementation.newInstance();
      
      return init(mcp, mcf, clf, subject, cri, pc, p);
   }

   /**
    * Create a managed connection pool using a specific implementation strategy
    * 
    * @param strategy Fullt qualified class name for the managed connection pool strategy
    * @param mcf the managed connection factory
    * @param clf the connection listener factory
    * @param subject the subject
    * @param cri the connection request info
    * @param pc the pool configuration
    * @param p The pool
    * @return The initialized managed connection pool
    * @exception Throwable Thrown in case of an error
    */
   public ManagedConnectionPool create(String strategy, 
                                       ManagedConnectionFactory mcf, ConnectionListenerFactory clf, Subject subject,
                                       ConnectionRequestInfo cri, PoolConfiguration pc, Pool p)
      throws Throwable
   {
      Class<?> clz = Class.forName(strategy, 
                                   true, 
                                   ManagedConnectionPoolFactory.class.getClassLoader());
      
      ManagedConnectionPool mcp = (ManagedConnectionPool)clz.newInstance();
      
      return init(mcp, mcf, clf, subject, cri, pc, p);
   }

   /**
    * Initialize
    * @param mcp The managed connection pool
    * @param mcf the managed connection factory
    * @param clf the connection listener factory
    * @param subject the subject
    * @param cri the connection request info
    * @param pc the pool configuration
    * @param p The pool
    * @return The initialized managed connection pool
    */
   private ManagedConnectionPool init(ManagedConnectionPool mcp, 
                                      ManagedConnectionFactory mcf, ConnectionListenerFactory clf, Subject subject,
                                      ConnectionRequestInfo cri, PoolConfiguration pc, Pool p)
   {
      mcp.initialize(mcf, clf, subject, cri, pc, p);

      return mcp;
   }
}
