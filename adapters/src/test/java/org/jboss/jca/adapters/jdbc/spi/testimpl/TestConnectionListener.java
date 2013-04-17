/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.adapters.jdbc.spi.testimpl;

import org.jboss.jca.adapters.jdbc.spi.listener.ConnectionListener;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A TestConnectionListener.
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class TestConnectionListener implements ConnectionListener
{
   /** initializedCalled **/
   private static boolean initializedCalled = false;

   /** activatedCalled **/
   private static boolean activatedCalled = false;

   /** passivatedCalled **/
   private static boolean passivatedCalled = false;

   /** propertyCalled **/
   private static boolean propertyCalled = false;

   /**
    * Constructor
    */
   public TestConnectionListener()
   {
      initializedCalled = false;
      activatedCalled = false;
      passivatedCalled = false;
      propertyCalled = false;
   }

   /**
    * Get initializedCalled
    * @return The value
    */
   public static boolean isInitializedCalled()
   {
      return initializedCalled;
   }

   /**
    * Get activatedCalled
    * @return The value
    */
   public static boolean isActivatedCalled()
   {
      return activatedCalled;
   }

   /**
    * Get passivatedCalled
    * @return The value
    */
   public static boolean isPassivatedCalled()
   {
      return passivatedCalled;
   }

   /**
    * Get propertyCalled
    * @return The value
    */
   public static boolean isPropertyCalled()
   {
      return propertyCalled;
   }

   /**
    * {@inheritDoc}
    */
   public void initialize(ClassLoader cl) throws SQLException
   {
      initializedCalled = true;
   }

   /**
    * {@inheritDoc}
    */
   public void activated(Connection c) throws SQLException
   {
      activatedCalled = true;
   }

   /**
    * {@inheritDoc}
    */
   public void passivated(Connection c) throws SQLException
   {
      passivatedCalled = true;
   }

   /**
    * Set the testString
    * @param v The value
    */
   public void setTestString(String v)
   {
      if ("MyTest".equals(v))
         propertyCalled = true;
   }
}
