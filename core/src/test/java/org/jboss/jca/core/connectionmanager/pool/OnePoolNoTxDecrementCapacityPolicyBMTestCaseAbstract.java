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

import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ConnectionDefinitionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ExtensionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.IronjacamarDescriptor;

/**
 * 
 * A OnePoolNoTxDecrementCapacityPolicyBMTestCaseAbstract
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access to 
 * AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public abstract class OnePoolNoTxDecrementCapacityPolicyBMTestCaseAbstract extends 
      OnePoolNoTxTestCaseAbstract
{

   /**
    * 
    * get IronjacamarDescriptor for deployment
    * @param decrementerClass name of class, implementing Decrementer interface, 
    * from package org.jboss.jca.core.connectionmanager.pool.capacity.
    * @param parameterName parameter for Decrementer class constructor
    * @param parameterValue value of parameter for constructor
    * 
    * @return IronjacamarDescriptor
    */
   public static IronjacamarDescriptor getIJWithDecrementer(String decrementerClass, 
      String parameterName, String parameterValue)
   {
      IronjacamarDescriptor ij = getBasicIJXml(SimpleManagedConnectionFactory.class.getName());
      ConnectionDefinitionType ijCdt = ij.getOrCreateConnectionDefinitions().getOrCreateConnectionDefinition();
      ExtensionType dt = ijCdt.removePool().getOrCreatePool().minPoolSize(2).maxPoolSize(5).getOrCreateCapacity()
         .getOrCreateDecrementer();
      dt.className("org.jboss.jca.core.connectionmanager.pool.capacity." + decrementerClass);
      if (parameterName != null)
      {
         dt.getOrCreateConfigProperty().name(parameterName).text(parameterValue);
      }

      return ij;
   }

   /**
    * 
    * get IronjacamarDescriptor for deployment
    * @param decrementerClass name of class, implementing Decrementer interface, 
    * from package org.jboss.jca.core.connectionmanager.pool.capacity.
    * 
    * @return IronjacamarDescriptor
    */
   public static IronjacamarDescriptor getIJWithDecrementer(String decrementerClass)
   {
      return getIJWithDecrementer(decrementerClass, null, null);
   }

   /**
    * 
    * filling pool by idle connections to the defined size 
    * 
    * @param size count of idle connections
    * @throws Exception in case of error
    */
   public void fillPool(int size) throws Exception
   {
      SimpleConnection[] c = new SimpleConnection[size];
      for (int i = 0; i < size; i++)
         c[i] = cf.getConnection();
      for (int i = 0; i < size; i++)
         c[i].close();
   }
}
