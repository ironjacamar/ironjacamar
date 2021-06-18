/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * Arquillian {@link LoadableExtension} adaptor for Embedded JCA
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class EmbeddedJCALoadableExtension implements LoadableExtension
{
   /**
    * Constructor
    */
   EmbeddedJCALoadableExtension()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void register(ExtensionBuilder builder)
   {
      builder.service(DeployableContainer.class, EmbeddedJCAContainer.class);
      builder.service(TestEnricher.class, EmbeddedJCAEnricher.class);
      builder.service(ResourceProvider.class, EmbeddedJCAEmbeddedResourceProvider.class);
      builder.service(ResourceProvider.class, EmbeddedJCAContextResourceProvider.class);
      builder.observer(EmbeddedJCAObserver.class);
   }
}
