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
package org.ironjacamar.embedded.deployers;

import org.ironjacamar.deployers.common.AbstractResourceAdapterDeployer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.github.fungal.api.util.JarFilter;

/**
 * Abstract Fungal resource adapter deployer
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class AbstractFungalRADeployer extends AbstractResourceAdapterDeployer
{
   /**
    * Constructor
    */
   public AbstractFungalRADeployer()
   {
      super();
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   protected URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         if (jars != null)
         {
            for (int j = 0; j < jars.length; j++)
            {
               list.add(jars[j].getCanonicalFile().toURI().toURL());
            }
         }
      }
      return list.toArray(new URL[list.size()]);
   }

   /**
    * Does the URL represent a .rar archive
    * @param url The URL
    * @return <code>true</code> if .rar archive, otherwise <code>false</code>
    */
   protected boolean isRarArchive(URL url)
   {
      if (url == null)
         return false;

      return isRarFile(url) || isRarDirectory(url);
   }

   /**
    * Does the URL represent a .rar file
    * @param url The URL
    * @return <code>true</code> if .rar file, otherwise <code>false</code>
    */
   protected boolean isRarFile(URL url)
   {
      if (url != null && url.toExternalForm().endsWith(".rar") && !url.toExternalForm().startsWith("jar"))
         return true;

      return false;
   }

   /**
    * Does the URL represent a .rar directory
    * @param url The URL
    * @return <code>true</code> if .rar directory, otherwise <code>false</code>
    */
   protected boolean isRarDirectory(URL url)
   {
      if (url != null && url.toExternalForm().endsWith(".rar/") && !url.toExternalForm().startsWith("jar"))
         return true;

      return false;
   }
}
