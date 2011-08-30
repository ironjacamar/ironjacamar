/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.metadata.ParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.util.file.FilenamePrefixFilter;

import org.hamcrest.core.IsNull;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;

/**
 *
 * A IronJacamarParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class IronJacamarParserTestCase
{
   private static Logger log = Logger.getLogger(IronJacamarParserTestCase.class);

   /**
    * shouldParseAnyExample
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseAnyExample() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("ironjacamar-")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            assertThat(ij, not(new IsNull<IronJacamar>()));

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
    *
    * shouldParseEmptyFileAndHaveNullMDContents
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseEmptyFileAndHaveNullMDContents() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("ironjacamar-empty.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            assertThat(ij.getAdminObjects(), new IsNull<List<CommonAdminObject>>());
            assertThat(ij.getConfigProperties(), new IsNull<Map<String, String>>());
            assertThat(ij.getBeanValidationGroups(), new IsNull<List<String>>());
            assertThat(ij.getConnectionDefinitions(), new IsNull<List<CommonConnDef>>());
            assertThat(ij.getBootstrapContext(), new IsNull<String>());
            assertThat(ij.getTransactionSupport(), new IsNull<TransactionSupportEnum>());

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
   *
   * shouldThrowExceptionWithUnexpectedAttributeOnConDef
   * @throws Exception in case of error
   */
   @Test(expected = ParserException.class)
   public void shouldThrowExceptionWithUnexpectedAttributeOnConDef() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("unexpected-attribute-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            //throw Exception

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
   *
   * shouldThrowExceptionWithUnexpectedAttributeOnAdminObj
   * @throws Exception in case of error
   */
   @Test(expected = ParserException.class)
   public void shouldThrowExceptionWithUnexpectedAttributeOnAdminObj() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("unexpected-attribute2-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            //throw Exception

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
   *
   * shouldThrowExceptionWithMissingMandatoryAttributeOnConnDef
   * @throws Exception in case of error
   */
   @Test(expected = ParserException.class)
   public void shouldThrowExceptionWithMissingMandatoryAttributeOnConnDef() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("missing-attribute-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            //throw Exception

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
   *
   * shouldThrowExceptionWithMissingMandatoryAttributeOnAdminObj
   * @throws Exception in case of error
   */
   @Test(expected = ParserException.class)
   public void shouldThrowExceptionWithMissingMandatoryAttributeOnAdminObj() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("missing-attribute-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            //throw Exception

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
   *
   * shouldParseEmptyFileAndHaveNullMDContents
   * @throws Exception in case of error
   */
   @Test(expected = ParserException.class)
   public void shouldThrowExceptionWithUnexpectedElement() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("unexpected-element-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            //throw Exception

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
   *
   * shouldThrowExceptionWithBothPoolAndXaPool
   * @throws Exception in case of error
   */
   @Test(expected = ParserException.class)
   public void shouldThrowExceptionWithBothPoolAndXaPool() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("pool-xa-pool-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            //throw Exception

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
   *
   * shouldParseFileAndHaveDefaultSecurityValues
   * @throws Exception in case of error
   */
   @Test
   public void shouldParseFileAndHaveDefaultSecurityValues() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("ironjacamar-security-defaults.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            CommonSecurity sec = ij.getConnectionDefinitions().get(0).getSecurity();
            assertThat(sec.getSecurityDomainAndApplication(), new IsNull<String>());
            assertThat(sec.getSecurityDomain(), new IsNull<String>());
            assertThat(sec.isApplication(), is(true));

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /** shouldParseFileMissingSecurityTagAndHaveNullSecurityValues
   * @throws Exception in case of error
   */
   @Test
   public void shouldParseFileMissingSecurityTagAndHaveNullSecurityValues() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory.listFiles(new FilenamePrefixFilter("ironjacamar-security-missed.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            CommonSecurity sec = ij.getConnectionDefinitions().get(0).getSecurity();
            assertThat(sec, new IsNull<CommonSecurity>());
            // Or should it be?
            //            assertThat(sec.getPassword(), new IsNull<String>());
            //            assertThat(sec.getUserName(), new IsNull<String>());
         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /** shouldParseFileWithEmptyCOnnectionDeginitionAndHaveAllDefaults
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseFileWithEmptyCOnnectionDeginitionAndHaveAllDefaults() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory
         .listFiles(new FilenamePrefixFilter("ironjacamar-connection-definition-empty.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            CommonConnDef connDef = ij.getConnectionDefinitions().get(0);
            //attribute defaults
            assertThat(connDef.getPoolName(), new IsNull<String>());
            assertThat(connDef.getClassName(), new IsNull<String>());
            assertThat(connDef.isEnabled(), is(true));
            assertThat(connDef.isUseJavaContext(), is(true));

            //pool default
            assertThat(connDef.isXa(), is(false));
            assertThat(connDef.getPool().getMinPoolSize(), new IsNull<Integer>());
            assertThat(connDef.getPool().getMaxPoolSize(), new IsNull<Integer>());
            assertThat(connDef.getPool().isPrefill(), is(false));
            assertThat(connDef.getPool().isUseStrictMin(), is(false));

            //security defaults
            CommonSecurity sec = connDef.getSecurity();
            assertThat(sec.getSecurityDomainAndApplication(), new IsNull<String>());
            assertThat(sec.getSecurityDomain(), new IsNull<String>());
            assertThat(sec.isApplication(), is(true));

            //timeout defaults
            CommonTimeOut t = connDef.getTimeOut();
            assertThat(t.getAllocationRetry(), new IsNull<Integer>());
            assertThat(t.getAllocationRetryWaitMillis(), new IsNull<Long>());
            assertThat(t.getBlockingTimeoutMillis(), new IsNull<Long>());
            assertThat(t.getIdleTimeoutMinutes(), new IsNull<Long>());
            assertThat(t.getXaResourceTimeout(), new IsNull<Integer>());

            //validation default
            CommonValidation v = connDef.getValidation();
            assertThat(v.getBackgroundValidationMillis(), new IsNull<Long>());
            assertThat(v.isBackgroundValidation(), is(false));
            assertThat(v.isUseFastFail(), is(false));

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /** shouldParseFileWithEmptyXaCOnnectionDeginitionAndHaveAllDefaults
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseFileWithEmptyXaCOnnectionDeginitionAndHaveAllDefaults() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory
         .listFiles(new FilenamePrefixFilter("ironjacamar-connection-definition-xa-empty.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            CommonConnDef connDef = ij.getConnectionDefinitions().get(0);
            //attribute defaults
            assertThat(connDef.getPoolName(), new IsNull<String>());
            assertThat(connDef.getClassName(), new IsNull<String>());
            assertThat(connDef.isEnabled(), is(true));
            assertThat(connDef.isUseJavaContext(), is(true));

            //pool default
            assertThat(connDef.isXa(), is(true));
            assertThat(connDef.getPool().getMinPoolSize(), is(0));
            assertThat(connDef.getPool().getMaxPoolSize(), is(20));
            assertThat(connDef.getPool().isPrefill(), is(false));
            assertThat(connDef.getPool().isUseStrictMin(), is(false));

            //security defaults
            CommonSecurity sec = connDef.getSecurity();
            assertThat(sec.getSecurityDomainAndApplication(), new IsNull<String>());
            assertThat(sec.getSecurityDomain(), new IsNull<String>());
            assertThat(sec.isApplication(), is(true));

            //timeout defaults
            CommonTimeOut t = connDef.getTimeOut();
            assertThat(t.getAllocationRetry(), new IsNull<Integer>());
            assertThat(t.getAllocationRetryWaitMillis(), new IsNull<Long>());
            assertThat(t.getBlockingTimeoutMillis(), new IsNull<Long>());
            assertThat(t.getIdleTimeoutMinutes(), new IsNull<Long>());
            assertThat(t.getXaResourceTimeout(), new IsNull<Integer>());

            //validation default
            CommonValidation v = connDef.getValidation();
            assertThat(v.getBackgroundValidationMillis(), new IsNull<Long>());
            assertThat(v.isBackgroundValidation(), is(false));
            assertThat(v.isUseFastFail(), is(false));

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /** shouldIgnoreXaSpecificElementForNormalPool
    * @throws Exception in case of error
    */
   @Test(expected = ParserException.class)
   public void shouldThrowExceptionForXaSpecificElementForNormalPool() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory
         .listFiles(new FilenamePrefixFilter("wrong-element-for-simple-pool-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /** shouldParseXaSpecificElementForXalPool
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseXaSpecificElementForXalPool() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory
         .listFiles(new FilenamePrefixFilter("right-element-for-xa-pool-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            CommonConnDef connDef = ij.getConnectionDefinitions().get(0);

            //pool default
            assertThat(connDef.isXa(), is(true));

            //timeout
            CommonTimeOut t = connDef.getTimeOut();
            assertThat(t.getXaResourceTimeout(), is(100));

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /** shouldParseXalPoolWithRecovery
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseXalPoolWithRecovery() throws Exception
   {
      FileInputStream is = null;

      //given
      File directory = new File(Thread.currentThread().getContextClassLoader().getResource("ironjacamar").toURI());
      for (File xmlFile : directory
         .listFiles(new FilenamePrefixFilter("xa-pool--recovery-ironjacamar.xml")))
      {
         try
         {
            is = new FileInputStream(xmlFile);
            IronJacamarParser parser = new IronJacamarParser();
            //when
            IronJacamar ij = parser.parse(is);
            //then
            CommonConnDef connDef = ij.getConnectionDefinitions().get(0);

            //pool default
            assertThat(connDef.isXa(), is(true));

            Recovery recovery = connDef.getRecovery();
            assertThat(recovery, not(isNull()));
            assertThat(recovery.getNoRecovery(), is(false));
            Credential security = recovery.getCredential();
            Extension plugin = recovery.getRecoverPlugin();
            assertThat(security.getUserName(), is("myUserName"));
            assertThat(security.getPassword(), is("myPassword"));
            assertThat(security.getSecurityDomain(), is("mySecurityDomain"));
            assertThat(plugin.getClassName(), is("myClassName"));
            assertThat(plugin.getConfigPropertiesMap().size(), is(1));
            assertThat(plugin.getConfigPropertiesMap().get("MyProperty"), is("MyPropertyValue"));

         }
         finally
         {
            if (is != null)
               is.close();
         }
      }
   }

   /**
    *
    * shouldParseXMLRepresentation
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseXMLRepresentation() throws Exception
   {
      FileInputStream is = null;
      ByteArrayInputStream bais = null;

      //given
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      File xmlFile = new File(cl.getResource("ironjacamar/ironjacamar-all.xml").toURI());
      try
      {
         is = new FileInputStream(xmlFile);
         IronJacamarParser parser = new IronJacamarParser();
         //when
         IronJacamar ij1 = parser.parse(is);

         String xmlRepresentation = ij1.toString();

         log.debug(xmlRepresentation);

         bais = new ByteArrayInputStream(xmlRepresentation.getBytes("UTF-8"));

         IronJacamar ij2 = parser.parse(bais);

         //then
         assertThat(ij1, instanceOf(IronJacamar.class));
         assertThat(ij2, instanceOf(IronJacamar.class));
      }
      finally
      {
         if (is != null)
            is.close();

         if (bais != null)
            bais.close();
      }
   }
}
