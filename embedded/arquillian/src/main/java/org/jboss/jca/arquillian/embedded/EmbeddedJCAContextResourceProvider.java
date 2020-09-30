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
package org.jboss.jca.arquillian.embedded;

import java.lang.annotation.Annotation;

import javax.naming.Context;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.logging.Logger;

/**
 * Arquillian {@link ResourceProvider} resource provider for a javax.naming.Context
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class EmbeddedJCAContextResourceProvider implements ResourceProvider
{
   /** The logger */
   private static Logger log = Logger.getLogger(EmbeddedJCAContextResourceProvider.class);

   /** Trace logging */
   private static boolean trace = log.isTraceEnabled();

   /** Context */
   @org.jboss.arquillian.core.api.annotation.Inject
   private Instance<Context> contextInst;

   /**
    * Constructor
    */
   EmbeddedJCAContextResourceProvider()
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object lookup(ArquillianResource resource, Annotation... annotations)
   {
      return contextInst.get();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean canProvide(Class<?> type)
   {
      if (type != null)
      {
         if (Context.class.isAssignableFrom(type))
         {
            return true;
         }
      }

      return false;
   }
}
