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

import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.core.api.connectionmanager.ConnectionManager;
import org.ironjacamar.core.spi.statistics.StatisticsPlugin;

import java.util.Collection;

/**
 * A connection factory
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ConnectionFactory
{
   /**
    * Get the JNDI name
    * @return The value
    */
   public String getJndiName();

   /**
    * Get the connection factory
    * @return The value
    */
   public Object getConnectionFactory();

   /**
    * Get the config properties
    * @return The value
    */
   public Collection<ConfigProperty> getConfigProperties();

   /**
    * Get the activation
    * @return The value
    */
   public ConnectionDefinition getActivation();

   /**
    * Get the connection manager
    * @return The value
    */
   public ConnectionManager getConnectionManager();

   /**
    * Get the pool
    * @return The value
    */
   public Pool getPool();

   /**
    * Get the statistics
    * @return The value
    */
   public StatisticsPlugin getStatistics();

   /**
    * Get the recovery
    * @return The value
    */
   public Recovery getRecovery();

   /**
    * Is the connection factory active ?
    * @return <code>true</code> if activated, <code>false</code> if not
    */
   public boolean isActivated();

   /**
    * Activate the connection factory
    * @return <code>true</code> if activated, <code>false</code> if already activated
    * @exception Exception Thrown in case of an error
    */
   public boolean activate() throws Exception;

   /**
    * Deactivate the connection factory
    * @return <code>true</code> if deactivated, <code>false</code> if already deactivated
    * @exception Exception Thrown in case of an error
    */
   public boolean deactivate() throws Exception;
}
