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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.Driver;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.metadata.ds.v11.DataSourceImpl;
import org.jboss.jca.common.metadata.ds.v11.DsParser;
import org.jboss.jca.common.metadata.ds.v11.DsPoolImpl;
import org.jboss.jca.common.metadata.ds.v11.DsXaPoolImpl;
import org.jboss.jca.common.metadata.ds.v11.XADataSourceImpl;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for parsing XML representation based upon metadata model's toString
 *
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
public class DsParserXMLTestCase extends DsParserTestBase
{
   /**
   *
   * beforeClass method
   *
   * @throws Exception in casae of error
   */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      parser = new DsParser();
   }

   /**
    * shouldParseXMLRepresentation
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseXMLRepresentation() throws Exception
   {
      DataSources dss = parseDsFromFile("ds/unit/complex-ds.xml");

      //Ds part
      DataSourceImpl ds = (DataSourceImpl) dss.getDataSource().get(0);
      assertNotNull(ds);
      assertEquals(ds.getJndiName(), "java:jboss/datasources/complexDs");
      assertEquals(ds.getPoolName(), "complexDs_Pool");
      assertFalse(ds.isJTA());
      assertFalse(ds.isEnabled());
      assertTrue(ds.isUseJavaContext());
      assertFalse(ds.isSpy());
      assertTrue(ds.isUseCcm());
      assertEquals(ds.getConnectionUrl(), "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
      assertEquals(ds.getDriverClass(), "org.hsqldb.jdbcDriver");
      assertEquals(ds.getDataSourceClass(), "org.jboss.as.connector.subsystems.datasources.ModifiableDataSource");
      assertEquals(ds.getDriver(), "h2");
      checkProperties(ds.getConnectionProperties());
      assertEquals(ds.getNewConnectionSql(), "select 1");
      assertEquals(ds.getTransactionIsolation(), TransactionIsolation.forName("2"));
      assertEquals(ds.getUrlDelimiter(), ":");
      assertEquals(ds.getUrlSelectorStrategyClassName(), "someClass");

      DsPoolImpl pool = (DsPoolImpl) ds.getPool();
      assertNotNull(pool);
      assertEquals((int) pool.getMinPoolSize(), 1);
      assertEquals((int) pool.getMaxPoolSize(), 5);
      assertTrue(pool.isPrefill());
      assertTrue(pool.isUseStrictMin());
      assertEquals(pool.getFlushStrategy(), FlushStrategy.ENTIRE_POOL);
      assertTrue(pool.isAllowMultipleUsers());

      DsSecurityImpl security = (DsSecurityImpl) ds.getSecurity();
      assertNotNull(security);
      assertEquals(security.getUserName(), "sa");
      assertEquals(security.getPassword(), "sa");
      assertNull(security.getSecurityDomain());
      checkExtension(security.getReauthPlugin(), "someClass1");

      ValidationImpl validation = (ValidationImpl) ds.getValidation();
      assertNotNull(validation);
      assertTrue(validation.isBackgroundValidation());
      assertEquals((long) validation.getBackgroundValidationMillis(), 2000L);
      assertTrue(validation.isUseFastFail());
      assertEquals(validation.getCheckValidConnectionSql(), "select 1");
      assertTrue(validation.isValidateOnMatch());
      checkExtension(validation.getValidConnectionChecker(), "someClass2");
      checkExtension(validation.getStaleConnectionChecker(), "someClass3");
      checkExtension(validation.getExceptionSorter(), "someClass4");

      TimeOutImpl timeout = (TimeOutImpl) ds.getTimeOut();
      assertNotNull(timeout);
      assertEquals((long) timeout.getBlockingTimeoutMillis(), 20000L);
      assertEquals((long) timeout.getIdleTimeoutMinutes(), 4L);
      assertEquals((int) timeout.getAllocationRetry(), 2);
      assertEquals((long) timeout.getAllocationRetryWaitMillis(), 3000L);
      assertNull(timeout.getXaResourceTimeout());
      assertTrue(timeout.isSetTxQueryTimeout());
      assertEquals((long) timeout.getQueryTimeout(), 120L);
      assertEquals((long) timeout.getUseTryLock(), 100L);

      StatementImpl statement = (StatementImpl) ds.getStatement();
      assertNotNull(statement);
      assertTrue(statement.isSharePreparedStatements());
      assertEquals((long) statement.getPreparedStatementsCacheSize(), 30L);
      assertEquals(statement.getTrackStatements(), Statement.TrackStatementsEnum.NOWARN);

      //      Xa Ds part
      XADataSourceImpl xads = (XADataSourceImpl) dss.getXaDataSource().get(0);
      assertNotNull(xads);

      assertEquals(xads.getJndiName(), "java:jboss/xa-datasources/complexXaDs");
      assertEquals(xads.getPoolName(), "complexXaDs_Pool");
      assertTrue(xads.isUseJavaContext());
      assertFalse(xads.isSpy());
      assertTrue(xads.isUseCcm());
      assertTrue(xads.isEnabled());
      assertEquals(xads.getXaDataSourceClass(), "org.jboss.as.connector.subsystems.datasources.ModifiableXaDataSource");
      assertEquals(xads.getDriver(), "pg");
      checkProperties(xads.getXaDataSourceProperty());
      assertEquals(xads.getNewConnectionSql(), "select 1");
      assertEquals(xads.getTransactionIsolation(), TransactionIsolation.TRANSACTION_READ_COMMITTED);
      assertEquals(xads.getUrlDelimiter(), ":");
      assertEquals(xads.getUrlSelectorStrategyClassName(), "someClass");

      DsXaPoolImpl poolXa = (DsXaPoolImpl) xads.getXaPool();
      assertNotNull(poolXa);
      assertEquals((int) poolXa.getMinPoolSize(), 1);
      assertEquals((int) poolXa.getMaxPoolSize(), 5);
      assertTrue(poolXa.isPrefill());
      assertTrue(poolXa.isUseStrictMin());
      assertEquals(poolXa.getFlushStrategy(), FlushStrategy.IDLE_CONNECTIONS);
      assertTrue(poolXa.isAllowMultipleUsers());
      assertTrue(poolXa.isSameRmOverride());
      assertTrue(poolXa.isInterleaving());
      assertTrue(poolXa.isPadXid());
      assertTrue(poolXa.isWrapXaResource());
      assertTrue(poolXa.isNoTxSeparatePool());

      security = (DsSecurityImpl) xads.getSecurity();
      assertNotNull(security);
      assertNull(security.getUserName());
      assertNull(security.getPassword());
      assertEquals(security.getSecurityDomain(), "HsqlDbRealm");
      checkExtension(security.getReauthPlugin(), "someClass1");

      validation = (ValidationImpl) xads.getValidation();
      assertNotNull(validation);
      assertTrue(validation.isBackgroundValidation());
      assertEquals((long) validation.getBackgroundValidationMillis(), 2000L);
      assertTrue(validation.isUseFastFail());
      assertEquals(validation.getCheckValidConnectionSql(), "select 1");
      assertTrue(validation.isValidateOnMatch());
      checkExtension(validation.getValidConnectionChecker(), "someClass2");
      checkExtension(validation.getStaleConnectionChecker(), "someClass3");
      checkExtension(validation.getExceptionSorter(), "someClass4");

      timeout = (TimeOutImpl) xads.getTimeOut();
      assertNotNull(timeout);
      assertEquals((long) timeout.getBlockingTimeoutMillis(), 20000L);
      assertEquals((long) timeout.getIdleTimeoutMinutes(), 4L);
      assertEquals((int) timeout.getAllocationRetry(), 2);
      assertEquals((long) timeout.getAllocationRetryWaitMillis(), 3000L);
      assertEquals((int) timeout.getXaResourceTimeout(), 120);
      assertTrue(timeout.isSetTxQueryTimeout());
      assertEquals((long) timeout.getQueryTimeout(), 120L);
      assertEquals((long) timeout.getUseTryLock(), 100L);

      statement = (StatementImpl) xads.getStatement();
      assertNotNull(statement);
      assertTrue(statement.isSharePreparedStatements());
      assertEquals((long) statement.getPreparedStatementsCacheSize(), 30L);
      assertEquals(statement.getTrackStatements(), Statement.TrackStatementsEnum.TRUE);

      Recovery recovery = xads.getRecovery();
      assertFalse(recovery.getNoRecovery());
      checkExtension(recovery.getRecoverPlugin(), "someClass5");
      Credential cred = recovery.getCredential();
      assertEquals(cred.getSecurityDomain(), "HsqlDbRealm");
      assertNull(cred.getUserName());
      assertNull(cred.getPassword());

      // drivers part
      List<Driver> drivers = dss.getDrivers();
      assertEquals(drivers.size(), 2);
      Driver driver = dss.getDriver("h2");
      log.info("driver:" + driver);
      assertNotNull(driver);
      assertNull(driver.getDriverClass());
      assertNull(driver.getMajorVersion());
      assertNull(driver.getMinorVersion());
      assertNull(driver.getXaDataSourceClass());
      assertEquals(driver.getModule(), "com.h2database.h2");
      assertEquals(driver.getDataSourceClass(), "org.h2.jdbcx.JdbcDataSource");

      driver = dss.getDriver("pg");
      assertNotNull(driver);
      assertEquals(driver.getDriverClass(), "org.pg.Driver");
      assertEquals((int) driver.getMajorVersion(), 9);
      assertEquals((int) driver.getMinorVersion(), 1);
      assertNull(driver.getDataSourceClass());
      assertEquals(driver.getModule(), "org.pg.postgres");
      assertEquals(driver.getXaDataSourceClass(), "org.pg.JdbcDataSource");
   }

   /**
    * Checks, that all properties are proceeded correctly 
    * @param map containing properties
    */
   public void checkProperties(Map<String, String> map)
   {
      assertNotNull(map);
      String result = null;
      if (map.size() != 2)
         result = "Wrong number of properties: " + map;
      else if (!map.containsKey("name1") || !map.containsKey("name2"))
         result = "There aren't properties with required names: " + map;
      else if (!map.get("name1").equals("Property1") || !map.get("name2").equals("Property2"))
         result = "There aren't properties with required values: " + map;
      assertNull(result, result);
   }

   /**
    * Checks, if extension is proceeded correctly
    * @param ext - Extension object
    * @param name - class name in Extension
    */
   public void checkExtension(Extension ext, String name)
   {
      assertNotNull(ext);
      if (!ext.getClassName().equals(name))
         fail("Extension class name " + ext + " doesn't equal: " + name);
      else
         checkProperties(ext.getConfigPropertiesMap());
   }
}
