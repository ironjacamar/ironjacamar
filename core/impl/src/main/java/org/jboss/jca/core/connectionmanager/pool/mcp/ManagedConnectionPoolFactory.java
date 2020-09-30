/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * Factory to create a managed connection pool
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ManagedConnectionPoolFactory
{   
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           ManagedConnectionPoolFactory.class.getName());

   /** Default implementation */
   public static final String DEFAULT_IMPLEMENTATION = 
      "org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreArrayListManagedConnectionPool";

   /** Experimental implementation */
   public static final String EXPERIMENTAL_IMPLEMENTATION = 
      "org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreConcurrentLinkedDequeManagedConnectionPool";

   /** Deprecated implementations */
   private static final String[] DEPRECATED_IMPLEMENTATIONS = new String[] {
      "org.jboss.jca.core.connectionmanager.pool.mcp.ArrayBlockingQueueManagedConnectionPool",
      "org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreConcurrentLinkedQueueManagedConnectionPool"
   };
   
   /** Default class definition */
   private static Class<?> defaultImplementation;

   /** Override */
   private static boolean override;
   
   static
   {
      String clz = SecurityActions.getSystemProperty("ironjacamar.mcp");

      if (clz != null && !clz.trim().equals(""))
      {
         clz = clz.trim();
         for (String impl : DEPRECATED_IMPLEMENTATIONS)
         {
            if (clz.equals(impl))
            {
               log.deprecatedPool(clz, EXPERIMENTAL_IMPLEMENTATION);
               clz = EXPERIMENTAL_IMPLEMENTATION;
            }
         }

         override = true;
      }
      else
      {
         clz = DEFAULT_IMPLEMENTATION;
         override = false;
      }

      try
      {
         defaultImplementation = Class.forName(clz, 
                                               true, 
                                               SecurityActions.getClassLoader(ManagedConnectionPoolFactory.class));
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
    * Get the default implementation
    * @return The value
    */
   public String getDefaultImplementation()
   {
      return defaultImplementation.getName();
   }

   /**
    * Is override
    * @return The value
    */
   public boolean isOverride()
   {
      return override;
   }

   /**
    * Create a managed connection pool using the default implementation strategy
    * 
    * @param mcf the managed connection factory
    * @param cm the connection manager
    * @param subject the subject
    * @param cri the connection request info
    * @param pc the pool configuration
    * @param p The pool
    * @return The initialized managed connection pool
    * @exception Throwable Thrown in case of an error
    */
   public ManagedConnectionPool create(ManagedConnectionFactory mcf, ConnectionManager cm, Subject subject,
                                       ConnectionRequestInfo cri, PoolConfiguration pc, Pool p) 
      throws Throwable
   {
      ManagedConnectionPool mcp = (ManagedConnectionPool)defaultImplementation.newInstance();
      
      return init(mcp, mcf, cm, subject, cri, pc, p);
   }

   /**
    * Create a managed connection pool using a specific implementation strategy
    * 
    * @param strategy Fullt qualified class name for the managed connection pool strategy
    * @param mcf the managed connection factory
    * @param cm the connection manager
    * @param subject the subject
    * @param cri the connection request info
    * @param pc the pool configuration
    * @param p The pool
    * @return The initialized managed connection pool
    * @exception Throwable Thrown in case of an error
    */
   public ManagedConnectionPool create(String strategy, 
                                       ManagedConnectionFactory mcf, ConnectionManager cm, Subject subject,
                                       ConnectionRequestInfo cri, PoolConfiguration pc, Pool p)
      throws Throwable
   {
      Class<?> clz = Class.forName(strategy, 
                                   true, 
                                   SecurityActions.getClassLoader(ManagedConnectionPoolFactory.class));
      
      ManagedConnectionPool mcp = (ManagedConnectionPool)clz.newInstance();
      
      return init(mcp, mcf, cm, subject, cri, pc, p);
   }

   /**
    * Initialize
    * @param mcp The managed connection pool
    * @param mcf the managed connection factory
    * @param cm the connection manager
    * @param subject the subject
    * @param cri the connection request info
    * @param pc the pool configuration
    * @param p The pool
    * @return The initialized managed connection pool
    */
   private ManagedConnectionPool init(ManagedConnectionPool mcp, 
                                      ManagedConnectionFactory mcf, ConnectionManager cm, Subject subject,
                                      ConnectionRequestInfo cri, PoolConfiguration pc, Pool p)
   {
      mcp.initialize(mcf, cm, subject, cri, pc, p);

      return mcp;
   }
}
