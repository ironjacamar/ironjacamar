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

import org.jboss.jca.adapters.jdbc.spi.ExceptionSorter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * A TestExceptionSorter.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class TestExceptionSorter implements ExceptionSorter
{

   /** constructorInvoked **/
   private static boolean constructorInvoked = false;

   /** methodInvoked **/
   private static boolean methodInvoked = false;

   private static List<String> stringInjected = new ArrayList<String>(2);

   private String testString;

   private String testString2;

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
    * Create a new TestExceptionSorter.
    *
    */
   public TestExceptionSorter()
   {
      TestExceptionSorter.constructorInvoked = true;
   }

   @Override
   public boolean isExceptionFatal(SQLException e)
   {
      TestExceptionSorter.methodInvoked = true;
      return false;
   }

   /**
    * Get the testString.
    *
    * @return the testString.
    */
   public final String getTestString()
   {
      return testString;
   }

   /**
    * Set the testString.
    *
    * @param testString The testString to set.
    */
   public final void setTestString(String testString)
   {
      this.testString = testString;
      TestExceptionSorter.appendStringInjected(testString);
   }

   /**
    * Get the stringInjected.
    *
    * @return the stringInjected.
    */
   public static final List<String> getStringInjected()
   {
      return stringInjected;
   }

   /**
    * Set the stringInjected.
    *
    * @param stringInjected The stringInjected to set.
    */
   public static final void appendStringInjected(String stringInjected)
   {
      TestExceptionSorter.stringInjected.add(stringInjected);
   }

   /**
    * Get the testString2.
    *
    * @return the testString2.
    */
   public final String getTestString2()
   {
      return testString2;
   }

   /**
    * Set the testString2.
    *
    * @param testString2 The testString2 to set.
    */
   public final void setTestString2(String testString2)
   {
      this.testString2 = testString2;
      TestExceptionSorter.appendStringInjected(testString2);
   }

}
