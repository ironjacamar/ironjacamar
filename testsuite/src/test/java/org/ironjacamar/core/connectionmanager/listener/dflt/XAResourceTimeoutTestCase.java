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
package org.ironjacamar.core.connectionmanager.listener.dflt;

import org.ironjacamar.embedded.Configuration;
import org.ironjacamar.embedded.Deployment;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.embedded.junit4.AllChecks;
import org.ironjacamar.embedded.junit4.IronJacamar;
import org.ironjacamar.embedded.junit4.PostCondition;
import org.ironjacamar.embedded.junit4.PreCondition;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.txlog.TxLogConnection;
import org.ironjacamar.rars.txlog.TxLogConnectionFactory;

import javax.annotation.Resource;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Timeout for XATransaction connection listener
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamar.class)
@Configuration(full = true)
@PreCondition(condition = AllChecks.class)
@PostCondition(condition = AllChecks.class)
public class XAResourceTimeoutTestCase
{
   /** The txlog connection factory */
   @Resource(mappedName = "java:/eis/TxLogConnectionFactory")
   private static TxLogConnectionFactory cf;

   /**
    * The resource adapter
    * @throws Throwable In case of an error
    */
   @Deployment(order = 1)
   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogRar();
   }
   
   /**
    * The activation for resource 1
    * @throws Throwable In case of an error
    */
   @Deployment(order = 2)
   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createTxLogDeployment(TransactionSupportLevel.XATransaction);
   }
   
   /**
    * Default timeout
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testDefaultTimeout() throws Throwable
   {
      assertNotNull(cf);
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      assertEquals(200, c.getTransactionTimeout());

      c.clearState();
      c.close();
   }

   /**
    * Override timeout
    * @throws Throwable In case of an error
    */
   @Test
   @SuppressWarnings("unchecked")
   public void testOverrideTimeout() throws Throwable
   {
      assertNotNull(cf);
      
      TxLogConnection c = cf.getConnection();
      assertNotNull(c);

      assertEquals(200, c.getTransactionTimeout());
      
      assertTrue(c.setTransactionTimeout(100));
      
      assertEquals(100, c.getTransactionTimeout());

      c.clearState();
      c.close();

      c = cf.getConnection();
      assertNotNull(c);

      assertEquals(200, c.getTransactionTimeout());

      c.clearState();
      c.close();
   }
}
