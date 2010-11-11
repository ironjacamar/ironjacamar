/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.adapters.jdbc.spi.ValidConnectionChecker;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * A TestValidConnectionChecker.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class TestValidConnectionChecker implements ValidConnectionChecker
{
   /** constructorInvoked **/
   private static boolean constructorInvoked = false;

   /** methodInvoked **/
   private static boolean methodInvoked = false;

   /** pingTimeOut **/
   private final Integer pingTimeOut = null;

   /**
    * Get the constructorInvoked.
    *
    * @return the constructorInvoked.
    */
   public static final boolean isConstructorInvoked()
   {
      return constructorInvoked;
   }

   /**
    * Get the methodInvoked.
    *
    * @return the methodInvoked.
    */
   public static final boolean isMethodInvoked()
   {
      return methodInvoked;
   }

   /**
    * Get the pingTimeOut.
    *
    * @return the pingTimeOut.
    */
   public final Integer getPingTimeOut()
   {
      return pingTimeOut;
   }

   /**
    *
    * Create a new TestValidConnectionChecker.
    *
    */
   public TestValidConnectionChecker()
   {
      TestValidConnectionChecker.constructorInvoked = true;
   }

   @Override
   public SQLException isValidConnection(Connection c)
   {
      TestValidConnectionChecker.methodInvoked = true;

      return null;
   }

}
