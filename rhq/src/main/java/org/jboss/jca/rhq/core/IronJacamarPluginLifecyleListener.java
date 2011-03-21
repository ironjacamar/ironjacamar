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
package org.jboss.jca.rhq.core;

import org.jboss.jca.rhq.util.ContainerHelper;

import org.jboss.logging.Logger;

import org.rhq.core.pluginapi.plugin.PluginContext;
import org.rhq.core.pluginapi.plugin.PluginLifecycleListener;

/**
 * IronJacamarPluginLifecyleListener
 * 
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class IronJacamarPluginLifecyleListener implements PluginLifecycleListener
{
   /** log */
   private static final Logger logger = Logger.getLogger(IronJacamarPluginLifecyleListener.class);
   /** 
    * initialize
    * 
    * @param context PluginContext
    * @throws Exception exception
    */
   @Override
   public void initialize(PluginContext context) throws Exception
   {
      if (ContainerHelper.useEmbeddedJCA())
      {
         ((Lifecycle)ContainerHelper.getEmbeddedDiscover()).start();
      }
      logger.info("IronJacamarPlugin started");
   }
   
   /** 
    * shutdown
    */
   @Override
   public void shutdown()
   {
      if (ContainerHelper.useEmbeddedJCA())
      {
         ((Lifecycle)ContainerHelper.getEmbeddedDiscover()).stop();
      }
      logger.info("IronJacamarPlugin stoped");
   }

}
