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
package org.ironjacamar.core.connectionmanager.pool.dflt;

import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.txlog.TxLogConnection;
import org.ironjacamar.rars.txlog.TxLogConnectionFactory;

import java.net.URL;

import javax.annotation.Resource;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Recovery test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = false)
public class RecoveryTestCase
{
   /** The txlog connection factory */
   @Resource(mappedName = "java:/eis/TxLogConnectionFactory")
   private static TxLogConnectionFactory cf;

   /**
    * Naming
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static URL createNaming() throws Throwable
   {
      return Configuration.class.getClassLoader().getResource("naming.xml");
   }
   
   /**
    * Stdio
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static URL createStdIO() throws Throwable
   {
      return Configuration.class.getClassLoader().getResource("stdio.xml");
   }
   
   /**
    * NoopTS
    * @throws Throwable In case of an error
    */
   @Deployment(order = 3)
   private static URL createNoopTS() throws Throwable
   {
      return Configuration.class.getClassLoader().getResource("noop-transaction.xml");
   }
   
   /**
    * JCA
    * @throws Throwable In case of an error
    */
   @Deployment(order = 4)
   private static URL createJCA() throws Throwable
   {
      return Configuration.class.getClassLoader().getResource("jca.xml");
   }
   
   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 5)
   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogRar();
   }
   
   /**
    * The activation
    * @throws Throwable In case of an error
    */
   @Deployment(order = 6)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.XATransaction);
   }
   
   /**
    * Recovery
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testRecovery() throws Throwable
   {
      assertNotNull(cf);
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      assertTrue(c.isRecovery());
      
      c.close();
   }
}
