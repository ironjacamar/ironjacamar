/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.connectionmanager.NoTxConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.strategy.PoolByCri;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory1;
import org.jboss.jca.core.util.Injection;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ConnectionDefinitionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ExtensionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.IronjacamarDescriptor;

import org.junit.Test;

/**
 * A PoolByCriNoTxTestCaseAbstract.
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
public class PoolByCriNoTxTestCaseAbstract extends PoolTestCaseAbstract
{

   /**
    * get IronjacamarDescriptor for deployment with defined FlushStrategy
    *
    * @param fs FlushStrategy
    * @return IronjacamarDescriptor
    */
   public static IronjacamarDescriptor getCriIJ(FlushStrategy fs)
   {
      IronjacamarDescriptor ij = getBasicIJXml(SimpleManagedConnectionFactory1.class.getName());
      ConnectionDefinitionType ijCdt = ij.getOrCreateConnectionDefinitions().getOrCreateConnectionDefinition();
      ijCdt.getOrCreateSecurity().application();
      if (fs != null)
      {
         ijCdt.removePool().getOrCreatePool().minPoolSize(3).maxPoolSize(5).prefill(true).flushStrategy(fs.getName());
      }
      return ij;
   }

   /**
    * get IronjacamarDescriptor for deployment
    *
    * @param decrementerClass name of class, implementing Decrementer interface,
    *                         from package org.jboss.jca.core.connectionmanager.pool.capacity.
    * @param parameterName    parameter for Decrementer class constructor
    * @param parameterValue   value of parameter for constructor
    * @return IronjacamarDescriptor
    */
   public static IronjacamarDescriptor getCriIJWithDecrementer(String decrementerClass,
                                                               String parameterName, String parameterValue)
   {
      IronjacamarDescriptor ij = getBasicIJXml(SimpleManagedConnectionFactory1.class.getName());
      ConnectionDefinitionType ijCdt = ij.getOrCreateConnectionDefinitions().getOrCreateConnectionDefinition();
      ijCdt.getOrCreateSecurity().application();
      ExtensionType dt = ijCdt.removePool().getOrCreatePool().minPoolSize(2).maxPoolSize(5).prefill(true)
            .getOrCreateCapacity().getOrCreateDecrementer();
      dt.className("org.jboss.jca.core.connectionmanager.pool.capacity." + decrementerClass);
      if (parameterName != null)
      {
         dt.getOrCreateConfigProperty().name(parameterName).text(parameterValue);
      }
      return ij;
   }

   /**
    * get IronjacamarDescriptor for deployment
    *
    * @param decrementerClass name of class, implementing Decrementer interface,
    *                         from package org.jboss.jca.core.connectionmanager.pool.capacity.
    * @return IronjacamarDescriptor
    */
   public static IronjacamarDescriptor getCriIJWithDecrementer(String decrementerClass)
   {
      return getCriIJWithDecrementer(decrementerClass, null, null);
   }

   /**
    * Replaces value of lastIdleCheck variable and then execute removeIdleConnections()
    * method on ManagedConnectionPool instance
    *
    * @param mcp ManagedConnectionPool instance
    * @throws Exception in case of error
    */
   public void callRemoveIdleConnections(ManagedConnectionPool mcp) throws Exception
   {
      Injection in = new Injection();
      in.inject(mcp, "lastIdleCheck", Long.MIN_VALUE, long.class.getName(), true);
      mcp.removeIdleConnections();

   }


   /**
    * checkConfig
    */
   @Test
   public void checkConfig()
   {
      checkConfiguration(NoTxConnectionManager.class, PoolByCri.class);
   }

}
