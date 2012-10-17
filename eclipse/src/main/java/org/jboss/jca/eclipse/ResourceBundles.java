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
package org.jboss.jca.eclipse;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * ResourceBundles is used to get I18N messages.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class ResourceBundles
{
   private static ResourceBundles instance;
   
   private final List<ResourceBundle> resBundles;
   /**
    * Inner only
    */
   private ResourceBundles()
   {
      super();
      resBundles = new ArrayList<ResourceBundle>();
      resBundles.add(ResourceBundle.getBundle("eclipse-plugin")); // eclipse-plugin.properties
   }
   
   
   /**
    * Gets single-ton instance of ResourceBundles.
    * 
    * @return the ResourceBundles instance.
    */
   public static ResourceBundles getInstance()
   {
      if (instance == null)
      {
         instance = new ResourceBundles();
      }
      return instance;
   }
   
   /**
    * Gets String which associates with key.
    * 
    * @param key the key
    * @return the String value
    */
   public String getString(String key)
   {
      return getString(key, new Object[0]);
   }
   
   /**
    * Gets String value which associates with key, and format it with parameters.
    * 
    * @param key the key
    * @param params the parameters
    * @return the String value
    */
   public String getString(String key, Object... params)
   {
      String message = null;
      for (ResourceBundle resBundle : this.resBundles)
      {
         try
         {
            message = resBundle.getString(key);
         }
         catch (MissingResourceException e)
         {
            continue;
         }
         if (message != null)
         {
            break;
         }
      }
      if (message != null)
      {
         if (params != null)
         {
            message = MessageFormat.format(message, params);
         }
      }
      return message;
   }

}
