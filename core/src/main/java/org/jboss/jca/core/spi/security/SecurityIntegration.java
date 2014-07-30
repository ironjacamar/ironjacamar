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

package org.jboss.jca.core.spi.security;

import javax.security.auth.callback.CallbackHandler;

/**
 * SecurityIntegration
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface SecurityIntegration
{
   /**
    * Create a security context
    * @param sd The security domain
    * @return The context
    * @exception Exception Thrown in case of an error
    */
   public SecurityContext createSecurityContext(String sd) throws Exception;

   /**
    * Get the security context
    * @return The context, or <code>null</code> if unset
    */
   public SecurityContext getSecurityContext();

   /**
    * Set the security context
    * @param context The context
    */
   public void setSecurityContext(SecurityContext context);

   /**
    * Create a default callback handler
    * @return The handler
    */
   public CallbackHandler createCallbackHandler();

   /**
    * Create a callback handler
    * @param callback The callback configuration
    * @return The handler
    */
   public CallbackHandler createCallbackHandler(Callback callback);


}
