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
package org.ironjacamar.common.metadata.ironjacamar;

import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.Credential;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.common.api.metadata.common.Pool;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.common.Security;
import org.ironjacamar.common.api.metadata.common.Timeout;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.common.Validation;
import org.ironjacamar.common.api.metadata.common.XaPool;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;

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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * IronJacamar 2.0 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class IronJacamar20TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      IronJacamarParser parser = new IronJacamarParser();

      InputStream is = IronJacamar20TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-2.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activation a = parser.parse(xsr);
      assertNotNull(a);

      is.close();
      checkActivation(a);
   }

   /**
    * ToString
    * @throws Exception In case of an error
    */
   @Test
   public void testToString() throws Exception
   {
      IronJacamarParser parser = new IronJacamarParser();

      InputStream is = IronJacamar20TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-2.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activation a = parser.parse(xsr);
      assertNotNull(a);

      is.close();

      StringReader sr = new StringReader(a.toString());
      XMLStreamReader nxsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      Activation an = parser.parse(nxsr);
      checkActivation(an);
      assertEquals(a, an);
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      IronJacamarParser parser = new IronJacamarParser();

      InputStream is = IronJacamar20TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-2.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activation a = parser.parse(xsr);
      assertNotNull(a);

      is.close();

      StringWriter sw = new StringWriter();
      XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
      xsw.setDefaultNamespace("");

      xsw.writeStartDocument("UTF-8", "1.0");
      parser.store(a, xsw);
      xsw.writeEndDocument();

      xsw.flush();
      xsw.close();

      assertEquals(a.toString(), sw.toString());
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      IronJacamarParser parser = new IronJacamarParser();

      InputStream is1 = IronJacamar20TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-2.0.xml");
      assertNotNull(is1);

      XMLStreamReader xsr1 = XMLInputFactory.newInstance().createXMLStreamReader(is1);

      Activation a1 = parser.parse(xsr1);
      assertNotNull(a1);

      is1.close();

      InputStream is2 = IronJacamar20TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ironjacamar/ironjacamar-2.0.xml");
      assertNotNull(is2);

      XMLStreamReader xsr2 = XMLInputFactory.newInstance().createXMLStreamReader(is2);

      Activation a2 = parser.parse(xsr2);
      assertNotNull(a2);

      is2.close();

      assertEquals(a1, a2);
   }

   /**
    * Checks activation parsing
    * @param Activation
    */
   private void checkActivation(Activation a)
   {
      assertEquals("someContext", a.getBootstrapContext());
      assertNull(a.getId());
      assertNull(a.getArchive());
      assertNull(a.getWorkManager());
      assertEquals(TransactionSupportEnum.XATransaction, a.getTransactionSupport());

      List<String> list = a.getBeanValidationGroups();
      assertEquals(2, list.size());
      assertTrue(list.contains("Class0"));
      assertTrue(list.contains("Class00"));

      Map<String, String> map = a.getConfigProperties();
      assertEquals(2, map.size());
      assertEquals("A", map.get("Property1"));
      assertEquals("B", map.get("Property2"));

      List<ConnectionDefinition> cds = a.getConnectionDefinitions();
      assertEquals(3, cds.size());

      for (ConnectionDefinition cd : cds)
      {
         Pool pool = cd.getPool();
         XaPool xpool;
         Security s = cd.getSecurity();
         Timeout t = cd.getTimeout();
         Validation v = cd.getValidation();
         Recovery r = cd.getRecovery();

         if (cd.getClassName().equals("Class1"))
         {
            assertEquals("java:jboss/name1", cd.getJndiName());
            assertEquals("Pool1", cd.getPoolName());
            assertTrue(cd.isUseCcm());
            assertTrue(cd.isEnabled());
            assertTrue(cd.isXa());
            assertTrue(cd.isSharable());
            assertTrue(cd.isEnlistment());
            assertFalse(cd.isConnectable());
            assertNull(cd.isTracking());

            map = cd.getConfigProperties();
            assertEquals(2, map.size());
            assertEquals("1", map.get("Property3"));
            assertEquals("2", map.get("Property4"));

            assertTrue(pool instanceof XaPool);
            xpool = (XaPool) pool;
            assertEquals(1, (int)xpool.getMinPoolSize());
            assertEquals(5, (int)xpool.getMaxPoolSize());
            assertEquals(2, (int)xpool.getInitialPoolSize());
            assertEquals(FlushStrategy.IDLE_CONNECTIONS, xpool.getFlushStrategy());
            assertTrue(xpool.isPrefill());
            assertTrue(xpool.isUseStrictMin());
            assertTrue(xpool.isIsSameRmOverride());
            assertTrue(xpool.isNoTxSeparatePool());
            assertTrue(xpool.isPadXid());
            assertFalse(xpool.isWrapXaResource());
            Capacity cp = xpool.getCapacity();
            assertNotNull(cp);
            Extension e = cp.getIncrementer();
            map = e.getConfigPropertiesMap();
            assertEquals(2, map.size());
            assertEquals("1", map.get("P1"));
            assertEquals("2", map.get("P2"));
            assertEquals("ic", e.getClassName());
            e = cp.getDecrementer();
            map = e.getConfigPropertiesMap();
            assertEquals(2, map.size());
            assertEquals("1", map.get("P1"));
            assertEquals("2", map.get("P2"));
            assertEquals("dc", e.getClassName());

            assertTrue(s.isApplication());
            assertNull(s.getSecurityDomain());
            assertNull(s.getSecurityDomainAndApplication());

            assertEquals(5000L, (long)t.getBlockingTimeoutMillis());
            assertEquals(4L, (long)t.getIdleTimeoutMinutes());
            assertEquals(2L, (long)t.getAllocationRetry());
            assertEquals(3000L, (long)t.getAllocationRetryWaitMillis());
            assertEquals(300L, (long)t.getXaResourceTimeout());

            assertEquals(5000L, (long)v.getBackgroundValidationMillis());
            assertTrue(v.isBackgroundValidation());
            assertNull(v.isValidateOnMatch());
            assertTrue(v.isUseFastFail());

            assertFalse(r.isNoRecovery());
            Credential c = r.getCredential();
            assertNotNull(c);
            assertEquals("sa", c.getUserName());
            assertEquals("sa-pass", c.getPassword());
            assertNull(c.getSecurityDomain());
            e = r.getRecoverPlugin();
            assertEquals("someClass2", e.getClassName());
            map = e.getConfigPropertiesMap();
            assertEquals(2, map.size());
            assertEquals("some", map.get("Property5"));
            assertEquals("true", map.get("Property6"));
         }
         else if (cd.getClassName().equals("Class2"))
         {
            assertEquals("java:jboss/name2", cd.getJndiName());
            assertNull(cd.getPoolName());
            assertFalse(cd.isUseCcm());
            assertFalse(cd.isEnabled());
            assertTrue(cd.isXa());
            assertFalse(cd.isSharable());
            assertFalse(cd.isEnlistment());
            assertTrue(cd.isConnectable());
            assertTrue(cd.isTracking());

            map = cd.getConfigProperties();
            assertEquals(0, map.size());

            assertTrue(pool instanceof XaPool);
            xpool = (XaPool) pool;
            assertEquals(0, (int)xpool.getMinPoolSize());
            assertEquals(20, (int)xpool.getMaxPoolSize());
            assertEquals(FlushStrategy.ENTIRE_POOL, xpool.getFlushStrategy());
            assertFalse(xpool.isPrefill());
            assertFalse(xpool.isUseStrictMin());
            assertFalse(xpool.isIsSameRmOverride());
            assertFalse(xpool.isNoTxSeparatePool());
            assertFalse(xpool.isPadXid());
            assertTrue(xpool.isWrapXaResource());

            assertFalse(s.isApplication());
            assertEquals("domain", s.getSecurityDomain());
            assertNull(s.getSecurityDomainAndApplication());

            assertNull(t);

            assertNull(v.getBackgroundValidationMillis());
            assertTrue(v.isValidateOnMatch());
            assertNull(v.isBackgroundValidation());
            assertFalse(v.isUseFastFail());

            assertTrue(r.isNoRecovery());
            Credential c = r.getCredential();
            assertNotNull(c);
            assertEquals("HsqlDbRealm", c.getSecurityDomain());
            assertNull(c.getUserName());
            assertNull(c.getPassword());

            assertNull(r.getRecoverPlugin());
         }
         else if (cd.getClassName().equals("Class3"))
         {
            assertEquals("java:jboss/name3", cd.getJndiName());
            assertNull(cd.getPoolName());
            assertTrue(cd.isUseCcm());
            assertTrue(cd.isEnabled());
            assertTrue(cd.isXa());
            assertTrue(cd.isSharable());
            assertTrue(cd.isEnlistment());
            assertFalse(cd.isConnectable());
            assertNull(cd.isTracking());

            map = cd.getConfigProperties();
            assertEquals(0, map.size());

            assertTrue(pool instanceof XaPool);
            xpool = (XaPool) pool;
            assertEquals(0, (int)xpool.getMinPoolSize());
            assertEquals(20, (int)xpool.getMaxPoolSize());
            assertNull(xpool.getInitialPoolSize());
            assertEquals(FlushStrategy.FAILING_CONNECTION_ONLY, xpool.getFlushStrategy());
            assertFalse(xpool.isPrefill());
            assertFalse(xpool.isUseStrictMin());
            assertNull(xpool.isIsSameRmOverride());
            assertFalse(xpool.isNoTxSeparatePool());
            assertFalse(xpool.isPadXid());
            assertTrue(xpool.isWrapXaResource());

            assertFalse(s.isApplication());
            assertNull(s.getSecurityDomain());
            assertEquals("domain", s.getSecurityDomainAndApplication());

            assertNull(t);
            assertNull(v);

            assertFalse(r.isNoRecovery());
            assertNull(r.getCredential());
            assertNull(r.getRecoverPlugin());
         }
         else
         {
            fail("Unexpected element:" + cd.getClassName());
         }
      }

      List<AdminObject> aos = a.getAdminObjects();
      assertEquals(2, aos.size());
      AdminObject ao1 = aos.get(0);
      AdminObject ao2;
      if (ao1.getClassName().equals("Class5"))
      {
         ao2 = ao1;
         ao1 = aos.get(1);
      }
      else
      {
         ao2 = aos.get(1);
      }
      assertEquals("Class4", ao1.getClassName());
      assertEquals("java:jboss/name4", ao1.getJndiName());
      assertEquals("Pool4", ao1.getPoolName());
      assertTrue(ao1.isEnabled());

      map = ao1.getConfigProperties();
      assertEquals(2, map.size());
      assertEquals("3.6", map.get("Property7"));
      assertEquals("", map.get("Property8"));

      assertEquals("Class5", ao2.getClassName());
      assertEquals("java:jboss/name5", ao2.getJndiName());
      assertNull(ao2.getPoolName());
      assertTrue(ao2.isEnabled());

      map = ao2.getConfigProperties();
      assertEquals(0, map.size());
   }
}
