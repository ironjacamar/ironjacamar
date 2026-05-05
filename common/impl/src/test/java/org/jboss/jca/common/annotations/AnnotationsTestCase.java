/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.annotations;

import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.spi.annotations.repository.Annotation;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;

import java.util.Collection;

import org.junit.Test;

/**
 * Tests for {@link Annotations}
 *
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
public class AnnotationsTestCase
{
   /**
    * JBJCA-1435: When no @Connector annotation is found (getAnnotation returns null)
    * and no xmlResourceAdapterClass is provided, process() should throw ValidateException.
    */
   @Test(expected = ValidateException.class)
   public void testNoConnectorAnnotationShouldThrowValidateException() throws Exception
   {
      AnnotationRepository repository = new AnnotationRepository()
      {
         @Override
         public Collection<Annotation> getAnnotation(Class<?> class1)
         {
            return null;
         }
      };

      Annotations annotations = new Annotations();
      annotations.process(repository, null, Thread.currentThread().getContextClassLoader());
   }
}
