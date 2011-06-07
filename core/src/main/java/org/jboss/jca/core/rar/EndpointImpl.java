/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.rar;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.bv.BeanValidationUtil;
import org.jboss.jca.core.spi.rar.Endpoint;

import java.lang.ref.WeakReference;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.logging.Messages;

/**
 * An endpoint representation
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class EndpointImpl implements Endpoint
{
   /** The reference to the resource adapter instance */
   private WeakReference<ResourceAdapter> ra;
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /**
    * Constructor
    * @param ra The resource adapter reference
    */
   EndpointImpl(WeakReference<ResourceAdapter> ra)
   {
      this.ra = ra;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void activate(MessageEndpointFactory endpointFactory,
                        ActivationSpec spec) throws ResourceException
   {
      if (endpointFactory == null)
         throw new IllegalArgumentException("MessageEndpointFactory is null");

      if (spec == null)
         throw new IllegalArgumentException("ActivationSpec is null");

      ResourceAdapter rar = ra.get();

      if (rar == null)
         throw new ResourceException(bundle.resourceAdapterInstanceNotActive());

      spec.validate();

      Validator validator = BeanValidationUtil.createValidator();
      Set errors = validator.validate(spec, Default.class);

      if (errors != null && errors.size() > 0)
      {
         throw new ResourceException(bundle.validationException(), new ConstraintViolationException(errors));
      }

      rar.endpointActivation(endpointFactory, spec);
   }

   /**
    * {@inheritDoc}
    */
   public void deactivate(MessageEndpointFactory endpointFactory,
                          ActivationSpec spec) throws ResourceException
   {
      if (endpointFactory == null)
         throw new IllegalArgumentException("MessageEndpointFactory is null");

      if (spec == null)
         throw new IllegalArgumentException("ActivationSpec is null");

      ResourceAdapter rar = ra.get();

      if (rar == null)
         throw new ResourceException(bundle.resourceAdapterInstanceNotActive());

      rar.endpointDeactivation(endpointFactory, spec);
   }
}
