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

import org.jboss.jca.common.spi.annotations.repository.Annotation;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * A AnnotationRepositoryImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class AnnotationRepositoryImpl implements AnnotationRepository
{

   private final org.jboss.papaki.AnnotationRepository backingRepository;

   /**
    *
    * Create a new AnnotationRepositoryImpl using papaki backend
    *
    * @param backingRepository the caking papaki repository
    * @throws IllegalArgumentException in case pas  sed repository is null
    */
   public AnnotationRepositoryImpl(org.jboss.papaki.AnnotationRepository backingRepository)
      throws IllegalArgumentException
   {
      if (backingRepository == null)
         throw new IllegalArgumentException("repository cannot be null");
      this.backingRepository = backingRepository;

   }

   @Override
   public Collection<Annotation> getAnnotation(Class<?> class1)
   {
      Collection<org.jboss.papaki.Annotation> backingAnnotations = backingRepository.getAnnotation(class1);
      if (backingAnnotations != null)
      {
         Collection<Annotation> annotations = new ArrayList<Annotation>();
         for (org.jboss.papaki.Annotation annotation : backingAnnotations)
         {
            annotations.add(new AnnotationImpl(annotation));
         }
         return annotations;
      }
      else
         return null;

   }

}
