/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.annotations.repository.jandex;

import org.ironjacamar.common.spi.annotations.repository.AnnotationRepository;
import org.ironjacamar.common.spi.annotations.repository.AnnotationScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;

/**
 * An AnnotationScannerImpl based on jandex.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AnnotationScannerImpl implements AnnotationScanner
{
   private static Logger log = Logger.getLogger(AnnotationScannerImpl.class);

   /**
    * Create a new AnnotationScannerImpl with a jandex backend
    */
   public AnnotationScannerImpl()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public AnnotationRepository scan(URL[] urls, ClassLoader cl)
   {
      Indexer indexer = new Indexer();

      if (urls != null && urls.length > 0)
      {
         for (URL url : urls)
         {
            String externalForm = url.toExternalForm();

            if (externalForm.endsWith(".class"))
            {
               InputStream is = null;
               try
               {
                  is = new FileInputStream(new File(url.toURI()));
                  indexer.index(is);
               }
               catch (Throwable t)
               {
                  log.error("Unable to process: " + externalForm, t);
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
                        // Nothing
                     }
                  }
               }
            }
            else if (externalForm.endsWith(".jar"))
            {
               JarFile jarFile = null;
               try
               {
                  jarFile = new JarFile(new File(url.toURI()));
                  Enumeration<JarEntry> entries = jarFile.entries();
                  while (entries.hasMoreElements())
                  {
                     JarEntry jarEntry = entries.nextElement();
                     if (jarEntry.getName().endsWith(".class"))
                     {
                        InputStream is = null;
                        try
                        {
                           is = jarFile.getInputStream(jarEntry);
                           indexer.index(is);
                        }
                        catch (Throwable t)
                        {
                           log.error("Unable to process: " + jarEntry.getName(), t);
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
                                 // Nothing
                              }
                           }
                        }
                     }
                  }
               }
               catch (Throwable t)
               {
                  log.error("Unable to process: " + externalForm, t);
               }
               finally
               {
                  if (jarFile != null)
                  {
                     try
                     {
                        jarFile.close();
                     }
                     catch (IOException ioe)
                     {
                        // Nothing
                     }
                  }
               }
            }
         }
      }

      return new AnnotationRepositoryImpl(indexer.complete(), cl);
   }
}
