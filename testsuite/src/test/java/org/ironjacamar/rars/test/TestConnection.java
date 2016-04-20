/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.rars.test;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.WorkManager;

/**
 * TestConnection
 */
public interface TestConnection
{
   /**
    * Get create failure count
    * @return The value
    */
   public int getCreateFailureCount();

   /**
    * Get invalid connection failure count
    * @return The value
    */
   public int getInvalidConnectionFailureCount();

   /**
    * Close
    */
   public void close();

   /**
    * Get the WorkManager instance
    * @return The value
    */
   WorkManager getWorkManager();


   /**
    * Get the WorkManager Name instance
    * @return The value
    */
   String getWorkManagerName();

   /**
    * get BootstrapContext
    * @return BootstrapContext
    */
   BootstrapContext getBootstrapContext();

}
