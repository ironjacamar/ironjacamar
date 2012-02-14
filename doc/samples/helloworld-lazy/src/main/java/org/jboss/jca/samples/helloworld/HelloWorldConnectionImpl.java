/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.samples.helloworld;

import java.util.logging.Logger;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;

/**
 * HelloWorldConnectionImpl
 *
 * @version $Revision: $
 */
public class HelloWorldConnectionImpl implements HelloWorldConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger("HelloWorldConnectionImpl");

   /** Connection Manager */
   private ConnectionManager cm;

   /** ManagedConnection */
   private HelloWorldManagedConnection mc;

   /** ManagedConnectionFactory */
   private HelloWorldManagedConnectionFactory mcf;

   /** ConnectionRequestInfo */
   private ConnectionRequestInfo cri;

   /**
    * Default constructor
    * @param mc HelloWorldManagedConnection
    * @param mcf HelloWorldManagedConnectionFactory
    * @param cm The connection manager
    * @param cri The connection request info
    */
   public HelloWorldConnectionImpl(HelloWorldManagedConnection mc,
                                   HelloWorldManagedConnectionFactory mcf,
                                   ConnectionManager cm,
                                   ConnectionRequestInfo cri)
   {
      this.mc = mc;
      this.mcf = mcf;
      this.cm = cm;
      this.cri = cri;
   }

   /**
    * Call helloWorld
    * @return String helloworld
    */
   public String helloWorld()
   {
      return helloWorld(((HelloWorldResourceAdapter)mcf.getResourceAdapter()).getName());
   }

   /**
    * Call helloWorld
    * @param name String name
    * @return String helloworld
    */
   public String helloWorld(String name)
   {
      if (mc == null)
         associate();

      return mc.helloWorld(name);
   }

   /**
    * Close
    */
   public void close()
   {
      if (mc != null)
      {
         mc.closeHandle(this);
      }
      else
      {
         if (cm instanceof LazyAssociatableConnectionManager)
         {
            LazyAssociatableConnectionManager lacm = (LazyAssociatableConnectionManager)cm;
            lacm.inactiveConnectionClosed(this, mcf);
         }
      }
   }

   /**
    * Set the managed connection
    * @param mc The managed connection
    */
   void setManagedConnection(HelloWorldManagedConnection mc)
   {
      this.mc = mc;
   }

   /**
    * Associate
    */
   private void associate()
   {
      if (cm instanceof LazyAssociatableConnectionManager)
      {
         try
         {
            LazyAssociatableConnectionManager lacm = (LazyAssociatableConnectionManager)cm;
            lacm.associateConnection(this, mcf, cri);
         }
         catch (Throwable t)
         {
            log.severe("Associate" + t.getMessage());
         }
      }
   }
}
