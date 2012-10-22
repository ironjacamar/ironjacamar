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
package org.jboss.jca.arquillian.embedded;

import org.jboss.jca.embedded.Embedded;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.logging.Logger;

/**
 * Arquillian {@link ResourceProvider} resource provider for Embedded
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class EmbeddedJCAEmbeddedResourceProvider implements ResourceProvider
{
   /** The logger */
   private static Logger log = Logger.getLogger(EmbeddedJCAEmbeddedResourceProvider.class);

   /** Trace logging */
   private static boolean trace = log.isTraceEnabled();

   /** Embedded */
   @org.jboss.arquillian.core.api.annotation.Inject
   private Instance<Embedded> embeddedInst;

   /**
    * Constructor
    */
   EmbeddedJCAEmbeddedResourceProvider()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object lookup(ArquillianResource resource, Annotation... annotations)
   {
      return embeddedInst.get();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean canProvide(Class<?> type)
   {
      if (type != null)
      {
         if (Embedded.class.isAssignableFrom(type))
         {
            return true;
         }
      }

      return false;
   }
}
