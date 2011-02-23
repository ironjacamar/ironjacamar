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
package org.jboss.jca.rhq.core;

import org.jboss.jca.core.api.management.ManagementRepository;

import java.lang.reflect.Method;

/**
 * ManagementRepositoryManager
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class ManagementRepositoryManager
{
   /**
    * Get a management repository instance
    * 
    * @return ManagementRepository
    */
   public static ManagementRepository getManagementRepository()
   {
      //currently use embedded JCA
      String jcaClass = ManagementRepositoryManager.class.getPackage().getName() + ".EmbeddedJcaDiscover";
      try
      {
         Class<?> cls = Class.forName (jcaClass);
         Method method = cls.getMethod ("getInstance");
         Discover discover = (Discover)method.invoke(cls, (Object[])null);
         return discover.getManagementRepository();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;

   }

}
