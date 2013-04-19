/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ConnectionDefinitionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.IronjacamarDescriptor;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.PoolType;

/**
 * 
 * A OnePoolNoTxTestCaseAbstract.
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class OnePoolNoTxTestCaseAbstract extends PoolTestCaseAbstract
{

   /**
    * 
    * create IronjacamarDescriptor  with prefilled pool and defined FlushStrategy
    * 
    * @param fs FlushStrategy
    * @return IronjacamarDescriptor 
    */
   public static IronjacamarDescriptor getPrefilledIJ(FlushStrategy fs)
   {
      IronjacamarDescriptor ij = getBasicIJXml(SimpleManagedConnectionFactory.class.getName());
      ConnectionDefinitionType ijCdt = ij.getOrCreateConnectionDefinitions().getOrCreateConnectionDefinition();
      PoolType pool = ijCdt.removePool().getOrCreatePool();
      pool.minPoolSize(2).maxPoolSize(5).prefill(true);
      if (fs != null)
      {
         pool.flushStrategy(fs.getName());
      }
      return ij;
   }



}

