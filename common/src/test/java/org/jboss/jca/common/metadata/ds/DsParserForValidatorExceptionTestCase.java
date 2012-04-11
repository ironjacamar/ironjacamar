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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ds.v11.DsParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * A DsParserForMinimalFileTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class DsParserForValidatorExceptionTestCase
{

   private static DsParser parser;

   /**
   *
   * beforeClass method
   *
   * @throws Exception in casae of file not found
   */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      parser = new DsParser();
      //this property is set just to make possible property substitution defined in test resources.
      //but property substitution is not the goal of this test case see DsParserForTemplateReplaceTestCase for that
      System.setProperty("jboss.server.data.dir", "/tmp");
   }

   /**
    *
    * shouldThrowValidateExceptionIfNoDriverHasBeenSpecified
    *
    * @throws Exception test passes if a {@link ValidateException} has been
    * thrown
    */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfNoDriverHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/no-driver-class-ds.xml")
         .toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfNoConnectionUrlHasBeenSpecified
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfNoConnectionUrlHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/no-connection-url-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfNoJndiNameHasBeenSpecified
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfNoJndiNameHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/no-jndi-name-ds.xml")
         .toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfNoPoolHasBeenSpecified
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfNoPoolHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/no-pool-name-ds.xml")
         .toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   private void doParse(File xmlFile) throws FileNotFoundException, Exception, IOException, ValidateException
   {
      FileInputStream is = null;

      try
      {
         is = new FileInputStream(xmlFile);
         //when
         DataSources ds = parser.parse(is);

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
   *
   * shouldThrowValidateExceptionIfallocationRetryNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfallocationRetryNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/allocation-retry-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfallocationRetryWaitMillisNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfallocationRetryWaitMillisNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/allocation-retry-wait-millis-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfBlockingTimeoutMillisNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfBlockingTimeoutMillisNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/blocking-timeout-millis-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfIdleTimeoutMinutesNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfIdleTimeoutMinutesNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/idle-timeout-minutes-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfnoconnectionurl
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfnoconnectionurl() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/no-connection-url-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfqueryTimeoutNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfqueryTimeoutNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/query-timeout-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfuseTryLockNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfuseTryLockNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/use-try-lock-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfxaresourcetimeoutNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfxaresourcetimeoutNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/xa-resource-timeout-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfpreparedStatementCacheSizeNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfpreparedStatementCacheSizeNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/prepared-statement-cache-size-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfbackgroundValidationMinutesNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfbackgroundValidationMinutesNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/background-validation-minutes-negative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfExceptionSorterClassNameMissing
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfExceptionSorterClassNameMissing() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/exception-sorter-class-name-missing-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfStaleConnectionCheckerClassNameMissing
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfStaleConnectionCheckerClassNameMissing() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/stale-connection-checker-class-name-missing-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfValidationCheckerClassNameMissing
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowValidateExceptionIfValidationCheckerClassNameMissing() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/validation-checker-class-name-missing-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowValidateExceptionIfValidationCheckerClassNameMissing
   *
   * @throws Exception test passes if a {@link ValidateException} has not been
   * thrown
   */
   @Test()
   public void shouldNotThrowValidateExceptionWithJeremysFile() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("ds/unit/jeremy-ds.xml")
         .toURI());
      //when
      doParse(xmlFile);
      //then don't throw ValidateException
   }

}
