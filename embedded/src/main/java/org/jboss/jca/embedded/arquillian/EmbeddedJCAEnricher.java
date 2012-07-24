/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.embedded.arquillian;

import org.jboss.jca.embedded.Embedded;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.logging.Logger;

/**
 * Handle @Inject fields for test cases based on kernel based lookups.
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class EmbeddedJCAEnricher implements TestEnricher
{
   /** The logger */
   private static Logger log = Logger.getLogger(EmbeddedJCAEnricher.class);

   /** Trace logging */
   private static boolean trace = log.isTraceEnabled();

   @org.jboss.arquillian.core.api.annotation.Inject
   private Instance<Embedded> embeddedInst;
   
   /**
    * {@inheritDoc}
    */
   public void enrich(Object testCase)
   {
      if (embeddedInst.get() != null)
      {
         if (trace)
            log.tracef("Injecting test case: %s", testCase.getClass().getName());

         injectClass(testCase);
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object[] resolve(Method method)
   {
      return new Object[method.getParameterTypes().length];
   }

   @SuppressWarnings("unchecked")
   private void injectClass(Object testCase)
   {
      List<Field> annotatedFields =
         SecurityActions.getFieldsWithAnnotation(testCase.getClass(),
                                                 org.jboss.jca.embedded.arquillian.Inject.class);

      if (trace)
         log.tracef("Fields: %s", annotatedFields);
         
      if (annotatedFields != null)
      {
         for (Field field : annotatedFields)
         {
            if (trace)
               log.tracef("Injecting field: %s", field);

            org.jboss.jca.embedded.arquillian.Inject annotation = null;
            try
            {
               field.setAccessible(true);
               annotation = field.getAnnotation(org.jboss.jca.embedded.arquillian.Inject.class);
               field.set(testCase, resolveResource(field, annotation));
            }
            catch (IllegalAccessException iae)
            {
               throw new RuntimeException("Could not inject: " + annotation + " (" + field.getType() + ")", iae);
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   private Object resolveResource(Field field, org.jboss.jca.embedded.arquillian.Inject annotation)
   {
      try
      {
         return embeddedInst.get().lookup(annotation.name(), field.getType());
      }
      catch (Throwable t)
      {
         // Nothing to do
      }

      throw new RuntimeException("Could not inject: " + annotation.name() + " (" + field.getType() + ")");
   }
}
