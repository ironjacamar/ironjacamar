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
package org.jboss.jca.common.annotations.repository.papaki;

import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;

import java.net.URL;

/**
 *
 * A AnnotationScannerImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class AnnotationScannerImpl implements AnnotationScanner
{
   private final org.jboss.papaki.AnnotationScanner backingScanner;

   /**
    *
    * Create a new AnnotationScannerImpl with papaki backend
    *
    * @param annotationScanner the papaki backing annotation scanner
    * @throws IllegalArgumentException in case passed papaki scanner is null
    */
   public AnnotationScannerImpl(org.jboss.papaki.AnnotationScanner annotationScanner)
      throws IllegalArgumentException
   {
      if (annotationScanner == null)
         throw new IllegalArgumentException("annotationscanner cannot be null");
      this.backingScanner = annotationScanner;

   }

   @Override
   public AnnotationRepository scan(URL[] urls, ClassLoader cl)
   {
      return new AnnotationRepositoryImpl(backingScanner.scan(urls, cl));
   }

}
