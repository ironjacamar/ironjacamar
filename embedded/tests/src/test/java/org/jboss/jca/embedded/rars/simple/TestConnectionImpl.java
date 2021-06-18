/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.embedded.rars.simple;

import java.util.logging.Logger;

/**
 * TestConnectionImpl
 *
 * @version $Revision: $
 */
public class TestConnectionImpl implements TestConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger("TestConnectionImpl");

   /** ManagedConnection */
   private TestManagedConnection mc;

   /** ManagedConnectionFactory */
   private TestManagedConnectionFactory mcf;

   /**
    * Default constructor
    * @param mc TestManagedConnection
    * @param mcf TestManagedConnectionFactory
    */
   public TestConnectionImpl(TestManagedConnection mc, TestManagedConnectionFactory mcf)
   {
      this.mc = mc;
      this.mcf = mcf;
   }

   /**
    * Call me
    */
   public void callMe()
   {
      mc.callMe();
   }

   /**
    * Close
    */
   public void close()
   {
      mc.closeHandle(this);
   }

}
