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
package org.ironjacamar.common.metadata.resourceadapter;
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
import org.ironjacamar.common.api.metadata.resourceadapter.Activations;
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
 * ResourceAdapters 1.0 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapters10TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activations a = parser.parse(xsr);
      assertNotNull(a);

      is.close();
      checkActivations(a);
   }

   /**
    * ToString
    * @throws Exception In case of an error
    */
   @Test
   public void testToString() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activations a = parser.parse(xsr);
      assertNotNull(a);

      is.close();

      StringReader sr = new StringReader(a.toString());
      XMLStreamReader nxsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      Activations an = parser.parse(nxsr);
      checkActivations(an);
      assertEquals(a, an);
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is);

      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);

      Activations a = parser.parse(xsr);
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
      ResourceAdapterParser parser = new ResourceAdapterParser();

      InputStream is1 = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is1);

      XMLStreamReader xsr1 = XMLInputFactory.newInstance().createXMLStreamReader(is1);

      Activations a1 = parser.parse(xsr1);
      assertNotNull(a1);

      is1.close();

      InputStream is2 = ResourceAdapters10TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/resourceadapter/dashra-1.0.xml");
      assertNotNull(is2);

      XMLStreamReader xsr2 = XMLInputFactory.newInstance().createXMLStreamReader(is2);

      Activations a2 = parser.parse(xsr2);
      assertNotNull(a2);

      is2.close();

      assertEquals(a1, a2);
   }

   /**
    * Checks activations parsing
    * @param Activation
    */
   private void checkActivations(Activations ac)
   {
      List<Activation> al = ac.getActivations();
      assertEquals(5, al.size());
      for (Activation a : al)
      {
         String archive = a.getArchive();
         List<String> ls =  a.getBeanValidationGroups();
         Map<String, String> mp = a.getConfigProperties();
         List<ConnectionDefinition> cds = a.getConnectionDefinitions();
         List<AdminObject> aos = a.getAdminObjects();
         if (archive.equals("some.rar"))
         {
            assertEquals("someContext", a.getBootstrapContext());
            assertEquals(2, ls.size());
            assertTrue(ls.contains("Class0"));
            assertTrue(ls.contains("Class00"));
            assertEquals(TransactionSupportEnum.XATransaction, a.getTransactionSupport());
            assertEquals(2, mp.size());
            assertEquals("A", mp.get("Property1"));
            assertEquals("B", mp.get("Property2"));
            assertEquals(3, cds.size());
            for (ConnectionDefinition cd : cds)
            {
               XaPool pool = (XaPool)cd.getPool();
               Security s = cd.getSecurity();
               Timeout t = cd.getTimeout();
               Validation v = cd.getValidation();
               Recovery r = cd.getRecovery();
               Credential c = r.getCredential();
               Extension e = r.getRecoverPlugin();
               if (cd.getClassName().contains("Class1"))
               {
                  assertEquals("java:jboss/name1", cd.getJndiName());
                  assertEquals("Pool1", cd.getPoolName());
                  assertTrue(cd.isUseCcm());
                  assertTrue(cd.isEnabled());
                  mp = cd.getConfigProperties();
                  assertEquals(2, mp.size());
                  assertEquals("1", mp.get("Property3"));
                  assertEquals("2", mp.get("Property4"));
                  assertTrue(cd.isXa());
                  
                  assertEquals(1, (int)pool.getMinPoolSize());
                  assertEquals(5, (int)pool.getMaxPoolSize());
                  assertEquals(FlushStrategy.IDLE_CONNECTIONS, pool.getFlushStrategy());
                  assertTrue(pool.isPrefill());
                  assertTrue(pool.isIsSameRmOverride());
                  assertTrue(pool.isNoTxSeparatePool());
                  assertTrue(pool.isPadXid());
                  assertFalse(pool.isWrapXaResource());
                  
                  assertTrue(s.isApplication());
                  assertNull(s.getSecurityDomain());
                  assertNull(s.getSecurityDomainAndApplication());
                  
                  assertEquals(5000L, (long)t.getBlockingTimeoutMillis());
                  assertEquals(4, (int)t.getIdleTimeoutMinutes());
                  assertEquals(2L, (long)t.getAllocationRetry());
                  assertEquals(3000L, (long)t.getAllocationRetryWaitMillis());
                  assertEquals(300L, (long)t.getXaResourceTimeout());
                  
                  assertEquals(5000L, (long)v.getBackgroundValidationMillis());
                  assertTrue(v.isBackgroundValidation());
                  assertTrue(v.isUseFastFail());
                  
                  assertFalse(r.isNoRecovery());
                  assertNotNull(c);
                  assertEquals("sa", c.getUserName());
                  assertEquals("sa-pass", c.getPassword());
                  assertNull(c.getSecurityDomain());
                  
                  assertNotNull(e);
                  assertEquals("someClass2", e.getClassName());
                  mp = e.getConfigPropertiesMap();
                  assertEquals(2, mp.size());
                  assertEquals("some", mp.get("Property5"));
                  assertEquals("true", mp.get("Property6"));
               }
               else if (cd.getClassName().contains("Class2"))
               {
                  assertEquals("java:jboss/name2", cd.getJndiName());
                  assertNull(cd.getPoolName());
                  assertFalse(cd.isUseCcm());
                  assertFalse(cd.isEnabled());
                  mp = cd.getConfigProperties();
                  assertEquals(0, mp.size());
                  assertTrue(cd.isXa());
                  
                  assertEquals(0, (int)pool.getMinPoolSize());
                  assertEquals(20, (int)pool.getMaxPoolSize());
                  assertEquals(FlushStrategy.ENTIRE_POOL, pool.getFlushStrategy());
                  assertFalse(pool.isPrefill());
                  assertFalse(pool.isIsSameRmOverride());
                  assertFalse(pool.isNoTxSeparatePool());
                  assertFalse(pool.isPadXid());
                  assertTrue(pool.isWrapXaResource());
                  
                  assertFalse(s.isApplication());
                  assertEquals("domain", s.getSecurityDomain());
                  assertNull(s.getSecurityDomainAndApplication());
                  
                  assertNull(t);
                  
                  assertNull(v.getBackgroundValidationMillis());
                  assertFalse(v.isBackgroundValidation());
                  assertFalse(v.isUseFastFail());
                  
                  assertTrue(r.isNoRecovery());
                  assertNotNull(c);
                  assertNull(c.getUserName());
                  assertNull(c.getPassword());
                  assertEquals("HsqlDbRealm", c.getSecurityDomain());
                  assertNull(e);
               }
               else if (cd.getClassName().contains("Class3"))
               {
                  assertEquals("java:jboss/name3", cd.getJndiName());
                  assertNull(cd.getPoolName());
                  assertTrue(cd.isUseCcm());
                  assertTrue(cd.isEnabled());
                  mp = cd.getConfigProperties();
                  assertEquals(0, mp.size());
                  assertTrue(cd.isXa());
                  
                  assertEquals(0, (int)pool.getMinPoolSize());
                  assertEquals(20, (int)pool.getMaxPoolSize());
                  assertEquals(FlushStrategy.FAILING_CONNECTION_ONLY, pool.getFlushStrategy());
                  assertFalse(pool.isPrefill());
                  assertNull(pool.isIsSameRmOverride());
                  assertFalse(pool.isNoTxSeparatePool());
                  assertFalse(pool.isPadXid());
                  assertTrue(pool.isWrapXaResource());
                  
                  assertFalse(s.isApplication());
                  assertEquals("domain", s.getSecurityDomainAndApplication());
                  assertNull(s.getSecurityDomain());
                  
                  assertNull(t);
                  assertNull(v);
                  assertFalse(r.isNoRecovery());
                  assertNull(c);
                  assertNull(e);
               }
               else
               {
                  fail("Unexpected element " + cd.getClassName());
               }
               
               assertEquals(2, aos.size());
               for (AdminObject ao : aos)
               {
                  mp = ao.getConfigProperties();
                  if (ao.getClassName().equals("Class4"))
                  {
                     assertEquals("java:jboss/name4", ao.getJndiName());
                     assertEquals("Pool4", ao.getPoolName());
                     assertTrue(ao.isEnabled());
                     assertEquals(2, mp.size());
                     assertEquals("3.6", mp.get("Property7"));
                     assertEquals("", mp.get("Property8"));
                  }
                  else if (ao.getClassName().equals("Class5"))
                  {
                     assertEquals("java:jboss/name5", ao.getJndiName());
                     assertNull(ao.getPoolName());
                     assertTrue(ao.isEnabled());
                     assertEquals(0, mp.size());
                  }
                  else
                  {
                     fail("Unexpected element " + ao.getClassName());
                  }
               }
            }
         }
         else if (archive.equals("some1.rar"))
         {
            assertNull(a.getBootstrapContext());
            assertNull(ls);
            assertEquals(TransactionSupportEnum.LocalTransaction, a.getTransactionSupport());
            assertNull(mp);
            assertEquals(1, cds.size());
            ConnectionDefinition cd = cds.get(0);
            assertEquals("java:jboss/name9", cd.getJndiName());
            assertNull(cd.getClassName());
            assertNull(cd.getPoolName());
            assertTrue(cd.isUseCcm());
            assertTrue(cd.isEnabled());
            mp = cd.getConfigProperties();
            assertEquals(0, mp.size());
            assertFalse(cd.isXa());
            
            Pool pool = cd.getPool();
            assertEquals(0, (int)pool.getMinPoolSize());
            assertEquals(20, (int)pool.getMaxPoolSize());
            assertEquals(FlushStrategy.FAILING_CONNECTION_ONLY, pool.getFlushStrategy());
            assertFalse(pool.isPrefill());
            
            assertNull(cd.getSecurity());
            assertNull(cd.getTimeout());
            assertNull(cd.getValidation());
            assertNull(cd.getRecovery());
            
            assertNull(aos);
         }
         else if (archive.equals("some2.rar"))
         {
            assertNull(a.getBootstrapContext());
            assertNull(ls);
            assertEquals(TransactionSupportEnum.NoTransaction, a.getTransactionSupport());
            assertNull(mp);
            assertEquals(1, cds.size());
            ConnectionDefinition cd = cds.get(0);
            assertEquals("java:jboss/name8", cd.getJndiName());
            assertNull(cd.getClassName());
            assertNull(cd.getPoolName());
            assertTrue(cd.isUseCcm());
            assertTrue(cd.isEnabled());
            mp = cd.getConfigProperties();
            assertEquals(0, mp.size());
            assertFalse(cd.isXa());
            
            Pool pool = cd.getPool();
            assertEquals(1, (int)pool.getMinPoolSize());
            assertEquals(5, (int)pool.getMaxPoolSize());
            assertEquals(FlushStrategy.IDLE_CONNECTIONS, pool.getFlushStrategy());
            assertTrue(pool.isPrefill());
            
            assertNull(cd.getSecurity());
            assertNull(cd.getTimeout());
            assertNull(cd.getValidation());
            assertNull(cd.getRecovery());
            
            assertEquals(2, aos.size());
            for (AdminObject ao : aos)
            {
               mp = ao.getConfigProperties();
               if ((ao.getClassName() != null) && ao.getClassName().equals("Class6"))
               {
                  assertEquals("java:jboss/name6", ao.getJndiName());
                  assertEquals("Pool6", ao.getPoolName());
                  assertFalse(ao.isEnabled());
                  assertEquals(2, mp.size());
                  assertEquals(".", mp.get("Property9"));
                  assertEquals("", mp.get("Property0"));
               }
               else if (ao.getClassName() == null)
               {
                  assertEquals("java:jboss/name7", ao.getJndiName());
                  assertNull(ao.getPoolName());
                  assertTrue(ao.isEnabled());
                  assertEquals(0, mp.size());
               }
               else
               {
                  fail("Unexpected element " + ao.getClassName());
               }
            }
         }
         else if (archive.equals("some3.rar"))
         {
            assertNull(a.getBootstrapContext());
            assertNull(ls);
            assertNull(a.getTransactionSupport());
            assertNull(mp);
            assertNull(cds);
            
            assertEquals(1, aos.size());
            AdminObject ao = aos.get(0);
            assertEquals("java:jboss/name0", ao.getJndiName());
            assertNull(ao.getClassName());
            assertNull(ao.getPoolName());
            assertTrue(ao.isEnabled());
            mp = ao.getConfigProperties();
            assertEquals(0, mp.size());
         }
         else if (archive.equals("some4.rar"))
         {
            assertNull(a.getBootstrapContext());
            assertNull(ls);
            assertNull(a.getTransactionSupport());
            assertNull(mp);
            assertNull(cds);
            assertNull(aos);
         }
         else
         {
            fail("Unexpected archive " + archive);
         }
      }
   }
}
