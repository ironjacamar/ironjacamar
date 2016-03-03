/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.util.Collection;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 * A resource adapter
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ResourceAdapter
{
   /**
    * Get the resource adapter - for introspection only
    * @return The value
    */
   public javax.resource.spi.ResourceAdapter getResourceAdapter();

   /**
    * Get the bootstrap context
    * @return The value
    */
   public BootstrapContext getBootstrapContext();

   /**
    * Get the config properties
    * @return The value
    */
   public Collection<ConfigProperty> getConfigProperties();

   /**
    * Get the statistics
    * @return The value
    */
   public StatisticsPlugin getStatistics();

   /**
    * Does the resource adapter support the message listener
    * @param ml The class name of the message listener
    * @return True if there is support, otherwise false
    */
   public boolean isMessageListenerSupported(String ml);

   /**
    * Create message listener
    * @param ml The class name of the message listener
    * @return The value
    * @exception Exception if the message listener can't be created
    */
   public MessageListener createMessageListener(String ml) throws Exception;

   /**
    * Endpoint activation
    * @param mef The message endpoint factory
    * @param as The activation spec
    * @exception Exception Thrown in case of an error
    */
   public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws Exception;

   /**
    * Endpoint deactivation
    * @param mef The message endpoint factory
    * @param as The activation spec
    * @exception Exception Thrown in case of an error
    */
   public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) throws Exception;

   /**
    * Get the inflow recovery
    * @return The value
    */
   public Collection<InflowRecovery> getRecovery();

   /**
    * Is the resource adapter active ?
    * @return <code>true</code> if activated, <code>false</code> if not
    */
   public boolean isActivated();

   /**
    * Activate the resource adapter
    * @return <code>true</code> if activated, <code>false</code> if already activated
    * @exception Exception Thrown in case of an error
    */
   public boolean activate() throws Exception;

   /**
    * Deactivate the resource adapter
    * @return <code>true</code> if deactivated, <code>false</code> if already deactivated
    * @exception Exception Thrown in case of an error
    */
   public boolean deactivate() throws Exception;
}
