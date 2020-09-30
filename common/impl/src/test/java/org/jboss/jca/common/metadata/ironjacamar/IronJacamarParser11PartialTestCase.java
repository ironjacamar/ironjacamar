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
package org.jboss.jca.common.metadata.ironjacamar;

import org.jboss.jca.common.api.metadata.JCAMetadata;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.metadata.XMLParserTestBase;
import org.jboss.jca.common.metadata.resourceadapter.ActivationImpl;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 *
 * IronJacamarParser11PartialTestCase
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class IronJacamarParser11PartialTestCase extends XMLParserTestBase
{
   /**
   *
   * beforeClass method
   *
   */
   @BeforeClass
   public static void beforeClass()
   {
      parser = new IronJacamarParser();
      parsedFileName = "ironjacamar/unit/ironjacamar-partial.xml";
   }

   /**
    * 
    * checks, if all IronJacamars 1.1 properties set correctly
    * {inheritDoc}
    * @param result of parsing
    */
   @Override
   public void checkMetadata(JCAMetadata result)
   {
      ActivationImpl ra = (ActivationImpl) result;
      
      assertEquals(null, ra.getBootstrapContext());
      assertEquals(null, ra.getBeanValidationGroups());
      assertEquals(null, ra.getConfigProperties());
      assertEquals(ra.getTransactionSupport(), TransactionSupportEnum.NoTransaction);
      
      List<ConnectionDefinition> cds = ra.getConnectionDefinitions();
      assertEquals(1, cds.size());
      //conn-def 1
      ConnectionDefinition cd = cds.get(0);
      assertEquals(null, cd.getClassName());
      assertEquals("java:jboss/name8", cd.getJndiName());
      assertEquals(null, cd.getPoolName());
      //default values
      assertTrue(cd.isEnabled());
      assertTrue(cd.isSharable());
      assertTrue(cd.isEnlistment());
      assertTrue(cd.isUseCcm());
      assertTrue(cd.isUseJavaContext());
      
      assertFalse(cd.isXa());
      Pool pool = cd.getPool();
      assertEquals(1, (int) pool.getMinPoolSize());
      assertEquals(5, (int) pool.getMaxPoolSize());
      assertTrue(pool.isPrefill());
      assertTrue(pool.isUseStrictMin());
      assertEquals(pool.getFlushStrategy(), FlushStrategy.IDLE_CONNECTIONS);
      
      assertEquals(null, cd.getSecurity());
      assertEquals(null, cd.getTimeOut());
      assertEquals(null, cd.getValidation());
      assertEquals(null, cd.getRecovery());

      List<AdminObject> aos = ra.getAdminObjects();
      assertEquals(2, aos.size());
      //admin object 1
      AdminObject ao = aos.get(0);
      assertEquals("Class6", ao.getClassName());
      assertEquals("java:jboss/name6", ao.getJndiName());
      assertEquals("Pool6", ao.getPoolName());
      assertFalse(ao.isEnabled());
      assertTrue(ao.isUseJavaContext());

      Map<String, String> cp = ao.getConfigProperties();
      assertEquals(2, cp.size());
      assertEquals(cp.get("Property9"), ".");
      assertEquals(cp.get("Property0"), "");

      //admin object 2
      ao = aos.get(1);
      assertEquals(null, ao.getClassName());
      assertEquals("java:jboss/name7", ao.getJndiName());
      assertEquals(null, ao.getPoolName());
      //default values
      assertTrue(ao.isEnabled());
      assertTrue(ao.isUseJavaContext());

      cp = ao.getConfigProperties();
      assertEquals(0, cp.size());
   }

}
