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
package org.ironjacamar.samples.helloworld;

import java.util.logging.Logger;

/**
 * HelloWorldConnectionImpl
 */
public class HelloWorldConnectionImpl implements HelloWorldConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger("HelloWorldConnectionImpl");

   /** ManagedConnection */
   private HelloWorldManagedConnection mc;

   /** ManagedConnectionFactory */
   private HelloWorldManagedConnectionFactory mcf;

   /**
    * Default constructor
    * @param mc HelloWorldManagedConnection
    * @param mcf HelloWorldManagedConnectionFactory
    */
   public HelloWorldConnectionImpl(HelloWorldManagedConnection mc,
                                   HelloWorldManagedConnectionFactory mcf)
   {
      this.mc = mc;
      this.mcf = mcf;
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
         return "Not connected";
      
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
         mc = null;
      }
   }

   /**
    * Set ManagedConnection
    * @param mc The ManagedConnection
    */
   void setManagedConnection(HelloWorldManagedConnection mc)
   {
      this.mc = mc;
   }
}
