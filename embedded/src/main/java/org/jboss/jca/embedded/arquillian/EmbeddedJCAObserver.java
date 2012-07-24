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
package org.jboss.jca.embedded.arquillian;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.logging.Logger;

/**
 * Observer for the @BeforeClass annotation
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
class EmbeddedJCAObserver
{
   /** The logger */
   private static Logger log = Logger.getLogger(EmbeddedJCAObserver.class);

   /** Trace logging */
   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   EmbeddedJCAObserver()
   {
   }

   /**
    * BeforeClass
    * @param event The event
    * @param registry The container registry
    */
   public void beforeClass(@Observes BeforeClass event, ContainerRegistry registry)
   {
      for (Container container : registry.getContainers())
      {
         DeployableContainer dc = container.getDeployableContainer();

         if (dc instanceof EmbeddedJCAContainer)
         {
            EmbeddedJCAContainer ejc = (EmbeddedJCAContainer)dc;

            if (event.getTestClass().isAnnotationPresent(Configuration.class))
            {
               Configuration c = event.getTestClass().getAnnotation(Configuration.class);
               ejc.setAutoActivate(c.autoActivate());
            }
         }
      }
   }
}
