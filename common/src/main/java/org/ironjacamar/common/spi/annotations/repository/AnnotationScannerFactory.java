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
package org.ironjacamar.common.spi.annotations.repository;

import org.ironjacamar.common.CommonBundle;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.logging.Messages;

/**
 * The AnnotationScannerFactory which creates an annotation scanner instance
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class AnnotationScannerFactory
{
   /** Jandex implementation */
   private static final String JANDEX = "org.ironjacamar.common.annotations.repository.jandex.AnnotationScannerImpl";

   /** The default implementation of the annotation scanner */
   private static AnnotationScanner defaultImplementation = null;

   /** The activate implementation */
   private static AnnotationScanner active = null;

   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);


   static
   {
      try
      {
         Class<?> clz = Class.forName(JANDEX, true, SecurityActions.getClassLoader(AnnotationScannerFactory.class));
         defaultImplementation = (AnnotationScanner)clz.newInstance();
      }
      catch (Throwable t)
      {
         // Ok - count on registerAnnotationScanner
      }
   }

   /**
    * Register an annotation scanner
    * @param scanner The scanner
    */
   public static void registerAnnotationScanner(AnnotationScanner scanner)
   {
      active = scanner;
   }

   /**
    * Get the annotation scanner
    * @return The scanner
    */
   public static AnnotationScanner getAnnotationScanner()
   {
      if (active != null)
         return active;

      if (defaultImplementation == null)
         throw new IllegalStateException(bundle.noAnnotationScanner());

      return defaultImplementation;
   }

   private static class SecurityActions
   {
      /**
       * Constructor
       */
      private SecurityActions()
      {
      }

      /**
       * Get the classloader.
       * @param c The class
       * @return The classloader
       */
      static ClassLoader getClassLoader(final Class<?> c)
      {
         if (System.getSecurityManager() == null)
            return c.getClassLoader();

         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return c.getClassLoader();
            }
         });
      }
   }
}
