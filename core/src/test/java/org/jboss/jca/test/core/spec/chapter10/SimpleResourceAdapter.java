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
package org.jboss.jca.test.core.spec.chapter10;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * SimpleBootstrapContext.

 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class SimpleResourceAdapter implements ResourceAdapter
{

   public void endpointActivation(MessageEndpointFactory endpointFactory,
         ActivationSpec spec) throws ResourceException
   {
      // TODO Auto-generated method stub

   }

   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
         ActivationSpec spec)
   {
      // TODO Auto-generated method stub

   }

   public XAResource[] getXAResources(ActivationSpec[] specs)
         throws ResourceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void start(BootstrapContext ctx)
         throws ResourceAdapterInternalException
   {
      // TODO Auto-generated method stub

   }

   public void stop()
   {
      // TODO Auto-generated method stub

   }

}
