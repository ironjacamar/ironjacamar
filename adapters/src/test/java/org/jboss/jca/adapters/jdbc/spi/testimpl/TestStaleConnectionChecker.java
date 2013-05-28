/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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

import org.jboss.jca.adapters.jdbc.spi.StaleConnectionChecker;

import java.sql.SQLException;

/**
 *
 * A TestStaleConnectionChecker.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class TestStaleConnectionChecker implements StaleConnectionChecker
{

   /** constructorInvoked **/
   private static boolean constructorInvoked = false;

   /** methodInvoked **/
   private static boolean methodInvoked = false;

   private static Integer injectedInteger = null;

   private Integer integerTest;

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
    *
    * Create a new TestStaleConnectionChecker.
    *
    */
   public TestStaleConnectionChecker()
   {
      TestStaleConnectionChecker.constructorInvoked = true;
   }

   @Override
   public boolean isStaleConnection(SQLException e)
   {
      TestStaleConnectionChecker.methodInvoked = true;
      return false;
   }

   /**
    * Get the integerTest.
    *
    * @return the integerTest.
    */
   public final Integer getIntegerTest()
   {
      return integerTest;
   }

   /**
    * Get the injectedInteger.
    *
    * @return the injectedInteger.
    */
   public static final Integer getInjectedInteger()
   {
      return injectedInteger;
   }

   /**
    * Set the injectedInteger.
    *
    * @param injectedInteger The injectedInteger to set.
    */
   public static final void setInjectedInteger(Integer injectedInteger)
   {
      TestStaleConnectionChecker.injectedInteger = injectedInteger;
   }

   /**
    * Set the integerTest.
    *
    * @param integerTest The integerTest to set.
    */
   public final void setIntegerTest(Integer integerTest)
   {
      this.integerTest = integerTest;
      TestStaleConnectionChecker.setInjectedInteger(integerTest);
   }

}
