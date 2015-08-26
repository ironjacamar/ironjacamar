/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.workmanager.transport.remote.jgroups;

import org.jgroups.JChannel;

/**
 *
 * A JGroupsConfiguration.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class JGroupsConfiguration
{
   /** the configuration **/
   private String configuration;

   /** the channel **/
   private JChannel channel;

   /**
    *
    * Create a new JGroupsConfiguration.
    *
    */
   public JGroupsConfiguration()
   {

   }

   /**
    * Start method for bean lifecycle
    *
    * @throws Throwable in case of error
    */
   public void start() throws Throwable
   {

      if (configuration == null)
      {
         channel = new JChannel();
      }
      else
      {
         channel = new JChannel(configuration);
      }


   }

   /**
    * Stop method for bean lifecycle
    *
    * @throws Throwable in case of error
    */
   public void stop() throws Throwable
   {

   }

   /**
    * Get the configuration.
    *
    * @return the configuration.
    */
   public final String getConfiguration()
   {
      return configuration;
   }

   /**
    * Set the configuration.
    *
    * @param configuration The configuration to set.
    */
   public final void setConfiguration(String configuration)
   {
      this.configuration = configuration;

   }

   /**
    * Get the channel.
    *
    * @return the channel.
    */
   public final JChannel getChannel()
   {

      return channel;

   }

}
