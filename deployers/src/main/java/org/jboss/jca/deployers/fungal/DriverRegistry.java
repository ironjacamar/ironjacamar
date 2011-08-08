/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.deployers.fungal;

import org.jboss.jca.deployers.DeployersLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * A driver registry
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DriverRegistry
{
   /** The logger */
   private static DeployersLogger log = 
      Logger.getMessageLogger(DeployersLogger.class, DriverRegistry.class.getName());

   /** The classloader to scan */
   private ClassLoader cl;

   /** Driver map */
   private Map<String, String> driverMap;

   /**
    * Constructor
    */
   public DriverRegistry()
   {
      this.cl = null;
      this.driverMap = new HashMap<String, String>();
   }

   /**
    * Set the classloader
    * @param cl The value
    */
   public void setScanClassLoader(ClassLoader cl)
   {
      this.cl = cl;
   }

   /**
    * Get driver for a module
    * @param module The module definition
    * @return The driver; <code>null</code> if not defined
    */
   public String getDriver(String module)
   {
      if (module == null)
         return null;

      return driverMap.get(module);
   }

   /**
    * Start
    * @exception Throwable If an error occurs
    */
   public void start() throws Throwable
   {
      if (cl != null)
      {
         Enumeration<URL> drivers = cl.getResources("META-INF/services/java.sql.Driver");
         if (drivers != null)
         {
            while (drivers.hasMoreElements())
            {
               URL driver = null;
               InputStream is = null;
               try
               {
                  driver = drivers.nextElement();

                  is = driver.openStream();

                  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                  int i = is.read();
                  while (i != -1)
                  {
                     bos.write((byte)i);
                     i = is.read();
                  }

                  String driverClass = bos.toString();

                  String module = driver.toExternalForm();
                  module = module.substring(0, module.indexOf("!"));
                  module = module.substring(module.lastIndexOf("/") + 1);
                 
                  log.debugf("Driver class: %s Module: %s", driverClass, module);

                  driverMap.put(module, driverClass);
               }
               catch (Throwable t)
               {
                  log.debug("Exception for driver: " + driver, t);
               }
               finally
               {
                  if (is != null)
                  {
                     try
                     {
                        is.close();
                     }
                     catch (IOException ioe)
                     {
                        // Ignore
                     }
                  }
               }
            }
         }
      }
   }
}
