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
package org.jboss.jca.common.spi.annotations.repository;

/**
 * The AnnotationScannerFactory which creates an annotation scanner instance
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class AnnotationScannerFactory
{
   /** Jandex implementation */
   private static final String JANDEX = "org.jboss.jca.common.annotations.repository.jandex.AnnotationScannerImpl";

   /** The default implementation of the annotation scanner */
   private static AnnotationScanner defaultImplementation = null;

   /** The activate implementation */
   private static AnnotationScanner active = null;

   static
   {
      try
      {
         Class<?> clz = Class.forName(JANDEX, true, AnnotationScannerFactory.class.getClassLoader());
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
         throw new IllegalStateException("Unable to find an annotation scanner implementation");

      return defaultImplementation;
   }
}
