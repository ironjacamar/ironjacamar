/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.rars.generic.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * GenericResourceAdapter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class GenericResourceAdapter implements ResourceAdapter
{
   /** The bootstrap context */
   private BootstrapContext ctx;
   
   /**
    * Get the bootstrap context
    * 
    * @return the context
    */
   public BootstrapContext getBootstrapContext()
   {
      return ctx;
   }
   
   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      this.ctx = ctx;
      // @todo start
   }

   public void stop()
   {
      // @todo stop
      this.ctx = null;
   }

   public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException
   {
      // @todo endpointActivation
      throw new org.jboss.util.NotImplementedException("endpointActivation");
      
   }

   public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
   {
      // @todo endpointDeactivation
      throw new org.jboss.util.NotImplementedException("endpointDeactivation");
      
   }

   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      // @todo getXAResources
      throw new org.jboss.util.NotImplementedException("getXAResources");
   }
}
