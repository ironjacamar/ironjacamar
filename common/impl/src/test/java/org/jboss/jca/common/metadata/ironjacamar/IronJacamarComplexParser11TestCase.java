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
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.Security;
import org.jboss.jca.common.api.metadata.common.TimeOut;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.metadata.common.XaPool;
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
 * IronJacamarComplexParser11TestCase
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class IronJacamarComplexParser11TestCase extends XMLParserTestBase
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
      parsedFileName = "ironjacamar/unit/ironjacamar11-all.xml";
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
      assertEquals("someContext", ra.getBootstrapContext());
      List<String> bvg = ra.getBeanValidationGroups();
      assertEquals(2, bvg.size());
      assertEquals("Class0", bvg.get(0));
      assertEquals("Class00", bvg.get(1));
      assertEquals(ra.getTransactionSupport(), TransactionSupportEnum.XATransaction);
      Map<String, String> cp = ra.getConfigProperties();
      assertEquals(2, cp.size());
      assertEquals(cp.get("Property1"), "A");
      assertEquals(cp.get("Property2"), "B");

      List<org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition> cds = ra.getConnectionDefinitions();
      assertEquals(3, cds.size());
      //conn-def 1
      ConnectionDefinition cd = cds.get(0);
      assertEquals("Class1", cd.getClassName());
      assertEquals("java:jboss/name1", cd.getJndiName());
      assertEquals("Pool1", cd.getPoolName());
      assertTrue(cd.isEnabled());
      assertTrue(cd.isSharable());
      assertTrue(cd.isEnlistment());
      assertTrue(cd.isUseCcm());
      assertFalse(cd.isUseJavaContext());

      cp = cd.getConfigProperties();
      assertEquals(2, cp.size());
      assertEquals(cp.get("Property3"), "1");
      assertEquals(cp.get("Property4"), "2");

      assertTrue(cd.isXa());
      assertNotNull(cd.getPool());
      assertTrue(cd.getPool() instanceof XaPool);

      XaPool xaPool = (XaPool) cd.getPool();
      assertEquals(1, (int) xaPool.getMinPoolSize());
      assertEquals(2, (int) xaPool.getInitialPoolSize());
      assertEquals(5, (int) xaPool.getMaxPoolSize());
      assertTrue(xaPool.isPrefill());
      assertTrue(xaPool.isUseStrictMin());
      assertEquals(xaPool.getFlushStrategy(), FlushStrategy.IDLE_CONNECTIONS);
      assertNotNull(xaPool.getCapacity());
      assertNotNull(xaPool.getCapacity().getIncrementer());
      assertNotNull(xaPool.getCapacity().getIncrementer().getClassName());
      assertEquals("ic", xaPool.getCapacity().getIncrementer().getClassName());
      assertNotNull(xaPool.getCapacity().getIncrementer().getConfigPropertiesMap());
      assertEquals(2, xaPool.getCapacity().getIncrementer().getConfigPropertiesMap().size());
      assertNotNull(xaPool.getCapacity().getDecrementer());
      assertNotNull(xaPool.getCapacity().getDecrementer().getClassName());
      assertEquals("dc", xaPool.getCapacity().getDecrementer().getClassName());
      assertNotNull(xaPool.getCapacity().getDecrementer().getConfigPropertiesMap());
      assertEquals(2, xaPool.getCapacity().getDecrementer().getConfigPropertiesMap().size());
      assertTrue(xaPool.isSameRmOverride());
      assertTrue(xaPool.isInterleaving());
      assertTrue(xaPool.isNoTxSeparatePool());
      assertTrue(xaPool.isPadXid());
      assertFalse(xaPool.isWrapXaResource());

      Security cs = cd.getSecurity();
      assertTrue(cs.isApplication());
      assertEquals(null, cs.getSecurityDomain());
      assertEquals(null, cs.getSecurityDomainAndApplication());

      TimeOut cto = cd.getTimeOut();
      assertEquals(5000, (long) cto.getBlockingTimeoutMillis());
      assertEquals(4, (long) cto.getIdleTimeoutMinutes());
      assertEquals(2, (int) cto.getAllocationRetry());
      assertEquals(3000, (long) cto.getAllocationRetryWaitMillis());
      assertEquals(300, (long) cto.getXaResourceTimeout());

      Validation cv = cd.getValidation();
      assertEquals(5000, (long) cv.getBackgroundValidationMillis());
      assertTrue(cv.isBackgroundValidation());
      assertTrue(cv.isUseFastFail());

      Recovery rec = cd.getRecovery();
      assertFalse(rec.getNoRecovery());

      Credential cr = rec.getCredential();
      assertEquals("sa", cr.getUserName());
      assertEquals("sa-pass", cr.getPassword());
      assertEquals(null, cr.getSecurityDomain());

      Extension ex = rec.getRecoverPlugin();
      assertEquals("someClass2", ex.getClassName());
      cp = ex.getConfigPropertiesMap();
      assertEquals(2, cp.size());
      assertEquals(cp.get("Property5"), "some");
      assertEquals(cp.get("Property6"), "true");

      //conn-def 2
      cd = cds.get(1);
      assertEquals("Class2", cd.getClassName());
      assertEquals("java:jboss/name2", cd.getJndiName());
      assertEquals(null, cd.getPoolName());
      assertFalse(cd.isEnabled());
      assertFalse(cd.isSharable());
      assertFalse(cd.isEnlistment());
      assertFalse(cd.isUseCcm());
      assertTrue(cd.isUseJavaContext());

      cp = cd.getConfigProperties();
      assertEquals(0, cp.size());

      assertTrue(cd.isXa());
      xaPool = (XaPool) cd.getPool();
      //default values
      assertEquals(0, (int) xaPool.getMinPoolSize());
      assertEquals(20, (int) xaPool.getMaxPoolSize());
      assertFalse(xaPool.isPrefill());
      assertFalse(xaPool.isUseStrictMin());
      assertEquals(xaPool.getFlushStrategy(), FlushStrategy.ENTIRE_POOL);
      assertNull(xaPool.getCapacity());
      assertFalse(xaPool.isSameRmOverride());
      //default for boolean-presenceType
      assertFalse(xaPool.isInterleaving());
      assertFalse(xaPool.isNoTxSeparatePool());
      assertFalse(xaPool.isPadXid());
      assertTrue(xaPool.isWrapXaResource());

      cs = cd.getSecurity();
      assertFalse(cs.isApplication());
      assertEquals("domain", cs.getSecurityDomain());
      assertEquals(null, cs.getSecurityDomainAndApplication());

      cv = cd.getValidation();
      assertFalse(cv.isBackgroundValidation());
      assertFalse(cv.isUseFastFail());

      rec = cd.getRecovery();
      assertTrue(rec.getNoRecovery());

      cr = rec.getCredential();
      assertEquals(null, cr.getUserName());
      assertEquals(null, cr.getPassword());
      assertEquals("HsqlDbRealm", cr.getSecurityDomain());

      assertEquals(null, rec.getRecoverPlugin());
      assertEquals(null, cd.getTimeOut());

      //conn-def 3
      cd = cds.get(2);
      assertEquals("Class3", cd.getClassName());
      assertEquals("java:jboss/name3", cd.getJndiName());
      assertEquals(null, cd.getPoolName());
      //default values
      assertTrue(cd.isEnabled());
      assertTrue(cd.isSharable());
      assertTrue(cd.isEnlistment());
      assertTrue(cd.isUseCcm());
      assertTrue(cd.isUseJavaContext());

      cp = cd.getConfigProperties();
      assertEquals(0, cp.size());

      assertTrue(cd.isXa());
      xaPool = (XaPool) cd.getPool();
      //default values
      assertEquals(0, (int) xaPool.getMinPoolSize());
      assertEquals(20, (int) xaPool.getMaxPoolSize());
      assertFalse(xaPool.isPrefill());
      assertEquals(xaPool.getFlushStrategy(), FlushStrategy.FAILING_CONNECTION_ONLY);
      assertNull(xaPool.getCapacity());
      assertEquals(null, xaPool.isSameRmOverride());
      //default for boolean-presenceType
      assertFalse(xaPool.isInterleaving());
      assertFalse(xaPool.isNoTxSeparatePool());
      assertFalse(xaPool.isPadXid());
      assertTrue(xaPool.isWrapXaResource());

      cs = cd.getSecurity();
      assertFalse(cs.isApplication());
      assertEquals(null, cs.getSecurityDomain());
      assertEquals("domain", cs.getSecurityDomainAndApplication());

      assertEquals(null, cd.getTimeOut());
      assertEquals(null, cd.getValidation());

      rec = cd.getRecovery();
      //default value
      assertFalse(rec.getNoRecovery());
      assertEquals(null, rec.getCredential());
      assertEquals(null, rec.getRecoverPlugin());

      List<AdminObject> aos = ra.getAdminObjects();
      assertEquals(2, aos.size());
      //admin object 1
      AdminObject ao = aos.get(0);
      assertEquals("Class4", ao.getClassName());
      assertEquals("java:jboss/name4", ao.getJndiName());
      assertEquals("Pool4", ao.getPoolName());
      assertTrue(ao.isEnabled());
      assertFalse(ao.isUseJavaContext());

      cp = ao.getConfigProperties();
      assertEquals(2, cp.size());
      assertEquals(cp.get("Property7"), "3.6");
      assertEquals(cp.get("Property8"), "");

      //admin object 2
      ao = aos.get(1);
      assertEquals("Class5", ao.getClassName());
      assertEquals("java:jboss/name5", ao.getJndiName());
      assertEquals(null, ao.getPoolName());
      //default values
      assertTrue(ao.isEnabled());
      assertTrue(ao.isUseJavaContext());

      cp = ao.getConfigProperties();
      assertEquals(0, cp.size());

   }

}
