/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.metadata.ds;

import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.Credential;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.ds.DataSource;
import org.ironjacamar.common.api.metadata.ds.DataSources;
import org.ironjacamar.common.api.metadata.ds.Driver;
import org.ironjacamar.common.api.metadata.ds.DsPool;
import org.ironjacamar.common.api.metadata.ds.DsSecurity;
import org.ironjacamar.common.api.metadata.ds.DsXaPool;
import org.ironjacamar.common.api.metadata.ds.Statement;
import org.ironjacamar.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.ironjacamar.common.api.metadata.ds.Timeout;
import org.ironjacamar.common.api.metadata.ds.TransactionIsolation;
import org.ironjacamar.common.api.metadata.ds.Validation;
import org.ironjacamar.common.api.metadata.ds.XaDataSource;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * DataSources 1.2 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DataSources12TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is = DataSources12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.2.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      DataSources ds = parser.parse(xsr);
      assertNotNull(ds);

      is.close();
      checkDS(ds);
   }

   /**
    * ToString
    * @throws Exception In case of an error
    */
   @Test
   public void testToString() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is = DataSources12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.2.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      DataSources ds = parser.parse(xsr);
      assertNotNull(ds);

      is.close();

      StringReader sr = new StringReader(ds.toString());
      XMLStreamReader nxsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      DataSources dsn = parser.parse(nxsr);
      checkDS(dsn);
      assertEquals(ds, dsn);
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is = DataSources10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.2.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      DataSources ds = parser.parse(xsr);
      assertNotNull(ds);

      is.close();

      StringWriter sw = new StringWriter();
      XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
      xsw.setDefaultNamespace("");

      xsw.writeStartDocument("UTF-8", "1.0");
      parser.store(ds, xsw);
      xsw.writeEndDocument();

      xsw.flush();
      xsw.close();

      assertEquals(ds.toString(), sw.toString());
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is1 = DataSources12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.2.xml");
      assertNotNull(is1);

      XMLStreamReader xsr1 = XMLInputFactory.newInstance().createXMLStreamReader(is1);

      DataSources ds1 = parser.parse(xsr1);
      assertNotNull(ds1);

      is1.close();

      InputStream is2 = DataSources12TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.2.xml");
      assertNotNull(is2);

      XMLStreamReader xsr2 = XMLInputFactory.newInstance().createXMLStreamReader(is2);

      DataSources ds2 = parser.parse(xsr2);
      assertNotNull(ds2);

      is2.close();

      assertEquals(ds1, ds2);
   }
   
   /**
    * Checks the data source parsed
    * @param result of data source parsing
    */
   private void checkDS(DataSources ds) 
   {
      List<DataSource> listDs = ds.getDataSource();
      assertEquals(1, listDs.size());
      DataSource d = listDs.get(0);
      
      assertFalse(d.isJTA());
      assertTrue(d.isSpy());
      assertFalse(d.isEnabled());
      assertFalse(d.isUseCcm());
      assertEquals("java:jboss/datasources/complexDs", d.getJndiName());
      assertEquals("complexDs_Pool", d.getPoolName());
      
      assertEquals("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", d.getConnectionUrl());
      assertEquals("org.hsqldb.jdbcDriver", d.getDriverClass());
      assertEquals("org.pg.JdbcDataSource", d.getDataSourceClass());
      assertEquals("h2", d.getDriver());
      Map<String, String> properties = d.getConnectionProperties();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("select 1", d.getNewConnectionSql());
      assertEquals(":", d.getUrlDelimiter());
      assertEquals("someClass", d.getUrlSelectorStrategyClassName());
      assertEquals(TransactionIsolation.valueOf("2"), d.getTransactionIsolation());
      
      DsPool pool = d.getPool();
      assertNotNull(pool);
      assertEquals(1, (int)pool.getMinPoolSize());
      assertEquals(2, (int)pool.getInitialPoolSize());
      assertEquals(5, (int)pool.getMaxPoolSize());
      assertTrue(pool.isPrefill());
      assertTrue(pool.isUseStrictMin());
      assertEquals(FlushStrategy.ALL_CONNECTIONS, pool.getFlushStrategy());
      assertTrue(pool.isAllowMultipleUsers());
      Capacity cp = pool.getCapacity();
      assertNotNull(cp);
      Extension e = cp.getIncrementer();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("ic", e.getClassName());
      e = cp.getDecrementer();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("dc", e.getClassName());
      
      DsSecurity s = d.getSecurity();
      assertNotNull(s);
      assertEquals("sa", s.getUserName());
      assertEquals("sa", s.getPassword());
      e = s.getReauthPlugin();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass1", e.getClassName());
      
      Validation v = d.getValidation();
      assertNotNull(v);
      assertEquals("select 1", v.getCheckValidConnectionSql());
      assertTrue(v.isBackgroundValidation());
      assertTrue(v.isValidateOnMatch());
      assertTrue(v.isUseFastFail());
      assertEquals(2000L, (long)v.getBackgroundValidationMillis());
      e = v.getValidConnectionChecker();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass2", e.getClassName());
      e = v.getStaleConnectionChecker();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass3", e.getClassName());
      e = v.getExceptionSorter();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass4", e.getClassName());
      
      Timeout t = d.getTimeout();
      assertNotNull(t);
      assertEquals(20000L, (long)t.getBlockingTimeoutMillis());
      assertEquals(4L, (long)t.getIdleTimeoutMinutes());
      assertEquals(120L, (long)t.getQueryTimeout());
      assertEquals(100L, (long)t.getUseTryLock());
      assertEquals(2L, (long)t.getAllocationRetry());
      assertEquals(3000L, (long)t.getAllocationRetryWaitMillis());
      assertTrue(t.isSetTxQueryTimeout());
      
      Statement st = d.getStatement();
      assertNotNull(st);
      assertEquals(30L, (long)st.getPreparedStatementsCacheSize());
      assertTrue(st.isSharePreparedStatements());
      assertEquals(TrackStatementsEnum.NOWARN, st.getTrackStatements());
      
      List<XaDataSource> xds = ds.getXaDataSource();
      assertEquals(1, xds.size());      
      XaDataSource xd = xds.get(0);
      
      assertFalse(xd.isSpy());
      assertTrue(xd.isEnabled());
      assertTrue(xd.isUseCcm());
      assertEquals("java:jboss/xa-datasources/complexXaDs", xd.getJndiName());
      assertEquals("complexXaDs_Pool", xd.getPoolName());
      
      assertEquals("org.pg.JdbcXADataSource", xd.getXaDataSourceClass());
      assertEquals("pg", xd.getDriver());
      properties = xd.getXaDataSourceProperty();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("select 1", xd.getNewConnectionSql());
      assertEquals(":", xd.getUrlDelimiter());
      assertEquals("someClass", xd.getUrlSelectorStrategyClassName());
      assertEquals(TransactionIsolation.TRANSACTION_READ_COMMITTED, xd.getTransactionIsolation());
      
      DsXaPool xpool = xd.getXaPool();
      assertNotNull(xpool);
      assertEquals(1, (int)xpool.getMinPoolSize());
      assertEquals(2, (int)xpool.getInitialPoolSize());
      assertEquals(5, (int)xpool.getMaxPoolSize());
      assertTrue(xpool.isPrefill());
      assertTrue(xpool.isUseStrictMin());
      assertEquals(FlushStrategy.GRACEFULLY, xpool.getFlushStrategy());
      assertTrue(xpool.isIsSameRmOverride());
      assertTrue(xpool.isNoTxSeparatePool());
      assertTrue(xpool.isPadXid());
      assertFalse(xpool.isWrapXaResource());
      assertTrue(xpool.isAllowMultipleUsers());
      cp = xpool.getCapacity();
      assertNotNull(cp);
      e = cp.getIncrementer();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("ic", e.getClassName());
      e = cp.getDecrementer();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("dc", e.getClassName());
 
      s = xd.getSecurity();
      assertNotNull(s);
      assertEquals("HsqlDbRealm", s.getSecurityDomain());
      e = s.getReauthPlugin();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass1", e.getClassName());
      
      Recovery r = xd.getRecovery();
      assertNotNull(r);
      assertFalse(r.isNoRecovery());
      Credential c = r.getCredential();
      assertNotNull(c);
      assertEquals("HsqlDbRealm", c.getSecurityDomain());
      e = r.getRecoverPlugin();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass5", e.getClassName());
      
      v = xd.getValidation();
      assertNotNull(v);
      assertEquals("select 1", v.getCheckValidConnectionSql());
      assertTrue(v.isBackgroundValidation());
      assertTrue(v.isValidateOnMatch());
      assertTrue(v.isUseFastFail());
      assertEquals(2000L, (long)v.getBackgroundValidationMillis());
      e = v.getValidConnectionChecker();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass2", e.getClassName());
      e = v.getStaleConnectionChecker();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass3", e.getClassName());
      e = v.getExceptionSorter();
      properties = e.getConfigPropertiesMap();
      assertEquals(2, properties.size()); 
      assertEquals("Property1", properties.get("name1"));
      assertEquals("Property2", properties.get("name2"));
      assertEquals("someClass4", e.getClassName());
      
      t = xd.getTimeout();
      assertNotNull(t);
      assertEquals(20000L, (long)t.getBlockingTimeoutMillis());
      assertEquals(4L, (long)t.getIdleTimeoutMinutes());
      assertEquals(120L, (long)t.getQueryTimeout());
      assertEquals(100L, (long)t.getUseTryLock());
      assertEquals(2L, (long)t.getAllocationRetry());
      assertEquals(3000L, (long)t.getAllocationRetryWaitMillis());
      assertTrue(t.isSetTxQueryTimeout());
      
      st = xd.getStatement();
      assertNotNull(st);
      assertEquals(30L, (long)st.getPreparedStatementsCacheSize());
      assertTrue(st.isSharePreparedStatements());
      assertEquals(TrackStatementsEnum.TRUE, st.getTrackStatements());
      
      List<Driver> drivers = ds.getDrivers();
      assertEquals(2, drivers.size());

      Driver driver = ds.getDriver("h2");
      assertTrue(drivers.contains(driver));
      assertNotNull(driver);
      assertEquals("h2", driver.getName());
      assertEquals(null, driver.getMajorVersion());
      assertEquals(null, driver.getMinorVersion());
      assertEquals("com.h2database.h2", driver.getModule());
      assertEquals(null, driver.getDriverClass());
      assertEquals(null, driver.getXaDataSourceClass());
      assertEquals("org.h2.jdbcx.JdbcDataSource", driver.getDataSourceClass());
     
      driver = ds.getDriver("pg");
      assertNotNull(driver);
      assertTrue(drivers.contains(driver));
      assertEquals(9, (int)driver.getMajorVersion());
      assertEquals(1, (int)driver.getMinorVersion());
      assertEquals("org.pg.postgres", driver.getModule());
      assertEquals("org.pg.Driver", driver.getDriverClass());
      assertEquals("org.pg.JdbcDataSource", driver.getXaDataSourceClass());
      assertEquals(null, driver.getDataSourceClass());

   }
}
