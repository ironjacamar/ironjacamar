/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security.picketbox;

import org.jboss.jca.core.spi.security.Callback;
import org.jboss.jca.core.spi.security.SecurityIntegration;

import javax.security.auth.callback.CallbackHandler;

import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;

/**
 * SecurityIntegration implementation using PicketBox
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PicketBoxSecurityIntegration implements SecurityIntegration
{
   /**
    * Constructor
    */
   public PicketBoxSecurityIntegration()
   {
   }

   /**
    * {@inheritDoc}
    */
   public org.jboss.jca.core.spi.security.SecurityContext createSecurityContext(String sd)
      throws Exception
   {
      org.jboss.security.SecurityContext sc = SecurityContextFactory.createSecurityContext(sd);
      return new PicketBoxSecurityContext(sc);
   }

   /**
    * {@inheritDoc}
    */
   public org.jboss.jca.core.spi.security.SecurityContext getSecurityContext()
   {
      org.jboss.security.SecurityContext sc = SecurityContextAssociation.getSecurityContext();

      if (sc == null)
         return null;

      return new PicketBoxSecurityContext(sc);
   }

   /**
    * {@inheritDoc}
    */
   public void setSecurityContext(org.jboss.jca.core.spi.security.SecurityContext context)
   {
      if (context == null)
      {
         SecurityContextAssociation.setSecurityContext(null);
      }
      else if (context instanceof PicketBoxSecurityContext)
      {
         PicketBoxSecurityContext psc = (PicketBoxSecurityContext)context;
         SecurityContextAssociation.setSecurityContext(psc.getDelegator());
      }
      else
      {
         throw new IllegalArgumentException("Invalid SecurityContext: " + context);
      }
   }

   /**
    * {@inheritDoc}
    */
   public CallbackHandler createCallbackHandler()
   {
      return new PicketBoxCallbackHandler();
   }

   /**
    * {@inheritDoc}
    */
   public CallbackHandler createCallbackHandler(Callback callback)
   {
      return new PicketBoxCallbackHandler(callback);
   }
}
