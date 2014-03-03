/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.JCAMetadata;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.v11.ConnDefPool;
import org.jboss.jca.common.api.metadata.common.v11.ConnDefXaPool;
import org.jboss.jca.common.api.metadata.common.v12.CommonConnDef;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapters;
import org.jboss.jca.common.metadata.XMLParserTestBase;
import org.jboss.jca.common.metadata.resourceadapter.v12.ResourceAdapterImpl;
import org.jboss.jca.common.metadata.resourceadapter.v12.ResourceAdapterParser;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 * A ResourceAdapterParser12TestCase.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterParser12TestCase extends XMLParserTestBase
{
   /**
   *
   * beforeClass method
   *
   */
   @BeforeClass
   public static void beforeClass()
   {
      parser = new ResourceAdapterParser();
      parsedFileName = "resource-adapter/unit/complex-12-ra.xml";
   }

   /**
    * checks, if all ResourceAdapters 1.2 properties set correctly
    * 
    * @param result of parsing
    */
   @Override
   public void checkMetadata(JCAMetadata result)
   {
      ResourceAdapters ras = (ResourceAdapters) result;
      assertNotNull(ras);
      List<ResourceAdapter> lrs = ras.getResourceAdapters();
      assertEquals(5, lrs.size());

      ResourceAdapterImpl ra = (ResourceAdapterImpl) lrs.get(0);
      assertEquals("ID1", ra.getId());
      assertEquals("some.rar", ra.getArchive());
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

      List<org.jboss.jca.common.api.metadata.common.CommonConnDef> cds = ra.getConnectionDefinitions();
      assertEquals(3, cds.size());
      //conn-def 1
      CommonConnDef cd = (CommonConnDef) cds.get(0);
      assertEquals("Class1", cd.getClassName());
      assertEquals("java:jboss/name1", cd.getJndiName());
      assertEquals("Pool1", cd.getPoolName());
      assertTrue(cd.isEnabled());
      assertTrue(cd.isSharable());
      assertTrue(cd.isEnlistment());
      assertTrue(cd.isUseCcm());
      assertFalse(cd.isUseJavaContext());
      assertTrue(cd.isConnectable());

      cp = cd.getConfigProperties();
      assertEquals(2, cp.size());
      assertEquals(cp.get("Property3"), "1");
      assertEquals(cp.get("Property4"), "2");

      assertTrue(cd.isXa());
      ConnDefXaPool xaPool = (ConnDefXaPool) cd.getPool();
      assertEquals(1, (int) xaPool.getMinPoolSize());
      assertEquals(5, (int) xaPool.getInitialPoolSize());
      assertEquals(5, (int) xaPool.getMaxPoolSize());
      assertTrue(xaPool.isPrefill());
      assertTrue(xaPool.isUseStrictMin());
      assertEquals(xaPool.getFlushStrategy(), FlushStrategy.IDLE_CONNECTIONS);
      assertNotNull(xaPool.getCapacity());
      assertNotNull(xaPool.getCapacity().getIncrementer());
      assertNotNull(xaPool.getCapacity().getDecrementer());
      assertTrue(xaPool.isSameRmOverride());
      assertTrue(xaPool.isInterleaving());
      assertTrue(xaPool.isNoTxSeparatePool());
      assertTrue(xaPool.isPadXid());
      assertFalse(xaPool.isWrapXaResource());

      CommonSecurity cs = cd.getSecurity();
      assertTrue(cs.isApplication());
      assertEquals(null, cs.getSecurityDomain());
      assertEquals(null, cs.getSecurityDomainAndApplication());

      CommonTimeOut cto = cd.getTimeOut();
      assertEquals(5000, (long) cto.getBlockingTimeoutMillis());
      assertEquals(4, (long) cto.getIdleTimeoutMinutes());
      assertEquals(2, (int) cto.getAllocationRetry());
      assertEquals(3000, (long) cto.getAllocationRetryWaitMillis());
      assertEquals(300, (long) cto.getXaResourceTimeout());

      CommonValidation cv = cd.getValidation();
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
      cd = (CommonConnDef) cds.get(1);
      assertEquals("Class2", cd.getClassName());
      assertEquals("java:jboss/name2", cd.getJndiName());
      assertEquals(null, cd.getPoolName());
      assertFalse(cd.isEnabled());
      assertFalse(cd.isSharable());
      assertFalse(cd.isEnlistment());
      assertFalse(cd.isUseCcm());
      assertTrue(cd.isUseJavaContext());
      assertFalse(cd.isConnectable());

      cp = cd.getConfigProperties();
      assertEquals(0, cp.size());

      assertTrue(cd.isXa());
      xaPool = (ConnDefXaPool) cd.getPool();
      //default values
      assertEquals(0, (int) xaPool.getMinPoolSize());
      assertEquals(20, (int) xaPool.getMaxPoolSize());
      assertFalse(xaPool.isPrefill());
      assertFalse(xaPool.isUseStrictMin());
      assertEquals(xaPool.getFlushStrategy(), FlushStrategy.ENTIRE_POOL);
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
      cd = (CommonConnDef) cds.get(2);
      assertEquals("Class3", cd.getClassName());
      assertEquals("java:jboss/name3", cd.getJndiName());
      assertEquals(null, cd.getPoolName());
      //default values
      assertTrue(cd.isEnabled());
      assertTrue(cd.isSharable());
      assertTrue(cd.isEnlistment());
      assertTrue(cd.isUseCcm());
      assertTrue(cd.isUseJavaContext());
      assertFalse(cd.isConnectable());

      cp = cd.getConfigProperties();
      assertEquals(0, cp.size());

      assertTrue(cd.isXa());
      xaPool = (ConnDefXaPool) cd.getPool();
      //default values
      assertEquals(0, (int) xaPool.getMinPoolSize());
      assertEquals(20, (int) xaPool.getMaxPoolSize());
      assertFalse(xaPool.isPrefill());
      assertEquals(xaPool.getFlushStrategy(), FlushStrategy.FAILING_CONNECTION_ONLY);
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

      List<CommonAdminObject> aos = ra.getAdminObjects();
      assertEquals(2, aos.size());
      //admin object 1
      CommonAdminObject ao = aos.get(0);
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
      
      //resource adapter 2
      ra = (ResourceAdapterImpl) lrs.get(1);
      assertEquals("ID2", ra.getId());
      assertEquals("some2.rar", ra.getArchive());
      assertEquals(null, ra.getBootstrapContext());
      assertEquals(null, ra.getBeanValidationGroups());
      assertEquals(null, ra.getConfigProperties());
      assertEquals(ra.getTransactionSupport(), TransactionSupportEnum.NoTransaction);
      
      cds = ra.getConnectionDefinitions();
      assertEquals(1, cds.size());
      //conn-def 1
      cd = (CommonConnDef) cds.get(0);
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
      ConnDefPool pool = (ConnDefPool) cd.getPool();
      assertEquals(1, (int) pool.getMinPoolSize());
      assertEquals(5, (int) pool.getInitialPoolSize());
      assertEquals(5, (int) pool.getMaxPoolSize());
      assertTrue(pool.isPrefill());
      assertTrue(pool.isUseStrictMin());
      assertEquals(pool.getFlushStrategy(), FlushStrategy.IDLE_CONNECTIONS);
      assertNotNull(pool.getCapacity());
      assertNotNull(pool.getCapacity().getIncrementer());
      assertNotNull(pool.getCapacity().getDecrementer());
      
      assertEquals(null, cd.getSecurity());
      assertEquals(null, cd.getTimeOut());
      assertEquals(null, cd.getValidation());
      assertEquals(null, cd.getRecovery());

      aos = ra.getAdminObjects();
      assertEquals(2, aos.size());
      //admin object 1
      ao = aos.get(0);
      assertEquals("Class6", ao.getClassName());
      assertEquals("java:jboss/name6", ao.getJndiName());
      assertEquals("Pool6", ao.getPoolName());
      assertFalse(ao.isEnabled());
      assertTrue(ao.isUseJavaContext());

      cp = ao.getConfigProperties();
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
      
      //resource adapter 3
      ra = (ResourceAdapterImpl) lrs.get(2);
      assertEquals(null, ra.getId());
      assertEquals("some1.rar", ra.getArchive());
      assertEquals(null, ra.getBootstrapContext());
      assertEquals(null, ra.getBeanValidationGroups());
      assertEquals(null, ra.getConfigProperties());
      assertEquals(ra.getTransactionSupport(), TransactionSupportEnum.LocalTransaction);
      
      cds = ra.getConnectionDefinitions();
      assertEquals(1, cds.size());
      //conn-def 1
      cd = (CommonConnDef) cds.get(0);
      assertEquals(null, cd.getClassName());
      assertEquals("java:jboss/name9", cd.getJndiName());
      assertEquals(null, cd.getPoolName());
      //default values
      assertTrue(cd.isEnabled());
      assertTrue(cd.isSharable());
      assertTrue(cd.isEnlistment());
      assertTrue(cd.isUseCcm());
      assertTrue(cd.isUseJavaContext());
      
      assertFalse(cd.isXa());
      CommonPool cpool = cd.getPool();
      assertFalse(cpool.isPrefill());
      assertFalse(cpool.isUseStrictMin());
      //default values
      assertEquals(0, (int) cpool.getMinPoolSize());
      assertEquals(20, (int) cpool.getMaxPoolSize());
      assertEquals(cpool.getFlushStrategy(), FlushStrategy.FAILING_CONNECTION_ONLY);

      assertEquals(null, cd.getSecurity());
      assertEquals(null, cd.getTimeOut());
      assertEquals(null, cd.getValidation());
      assertEquals(null, cd.getRecovery());

      assertEquals(null, ra.getAdminObjects());

      //resource adapter 4
      ra = (ResourceAdapterImpl) lrs.get(3);
      assertEquals(null, ra.getId());
      assertEquals("some3.rar", ra.getArchive());
      assertEquals(null, ra.getBootstrapContext());
      assertEquals(null, ra.getBeanValidationGroups());
      assertEquals(null, ra.getConfigProperties());
      assertEquals(null, ra.getTransactionSupport());
      
      assertEquals(null, ra.getConnectionDefinitions());
      
      aos = ra.getAdminObjects();
      assertEquals(1, aos.size());
      //admin object 1
      ao = aos.get(0);
      assertEquals(null, ao.getClassName());
      assertEquals("java:jboss/name9", ao.getJndiName());
      assertEquals(null, ao.getPoolName());
      //default values
      assertTrue(ao.isEnabled());
      assertTrue(ao.isUseJavaContext());
      
      //resource adapter 5
      ra = (ResourceAdapterImpl) lrs.get(4);
      assertEquals(null, ra.getId());
      assertEquals("some4.rar", ra.getArchive());
      assertEquals(null, ra.getBootstrapContext());
      assertEquals(null, ra.getBeanValidationGroups());
      assertEquals(null, ra.getConfigProperties());
      assertEquals(null, ra.getTransactionSupport());
      
      assertEquals(null, ra.getConnectionDefinitions());
      assertEquals(null, ra.getAdminObjects());
   }

}
