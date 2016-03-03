/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.api.deploymentrepository;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 * Inflow recovery module
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface InflowRecovery
{
   /**
    * Get the message endpoint factory
    * @return The value
    */
   public MessageEndpointFactory getMessageEndpointFactory();

   /**
    * Get the activation spec
    * @return The value
    */
   public ActivationSpec getActivationSpec();

   /**
    * Is the recovery active ?
    * @return <code>true</code> if activated, <code>false</code> if not
    */
   public boolean isActivated();

   /**
    * Activate the recovery
    * @return <code>true</code> if activated, <code>false</code> if already activated
    * @exception Exception Thrown in case of an error
    */
   public boolean activate() throws Exception;

   /**
    * Deactivate the recovery
    * @return <code>true</code> if deactivated, <code>false</code> if already deactivated
    * @exception Exception Thrown in case of an error
    */
   public boolean deactivate() throws Exception;
}
