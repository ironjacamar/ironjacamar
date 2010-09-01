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

import java.util.Arrays;
import java.util.List;

import org.jboss.papaki.AnnotationType;

/**
 *
 * A AnnotationImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class AnnotationImpl implements Annotation
{
   private final org.jboss.papaki.Annotation backingAnnotation;

   /**
    *
    * Create a new AnnotationImpl with papaki backend
    *
    * @param annotation the backing papaki annotation
    * @throws IllegalArgumentException in case passed annotation is null
    */
   public AnnotationImpl(org.jboss.papaki.Annotation annotation) throws IllegalArgumentException
   {
      if (annotation == null)
         throw new IllegalArgumentException("annotation cannot be null");
      this.backingAnnotation = annotation;

   }

   @Override
   public String getClassName()
   {
      return backingAnnotation.getClassName();
   }

   @Override
   public Object getAnnotation()
   {
      return backingAnnotation.getAnnotation();
   }

   @Override
   public List<String> getParameterTypes()
   {
      return Arrays.asList(backingAnnotation.getParameterTypes());
   }

   @Override
   public String getMemberName()
   {
      return backingAnnotation.getMemberName();
   }

   @Override
   public boolean isOnMethod()
   {
      return AnnotationType.METHOD.equals(backingAnnotation.getType());
   }

   @Override
   public boolean isOnField()
   {
      return AnnotationType.FIELD.equals(backingAnnotation.getType());
   }

}

