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
   }

   /**
    *
    * shouldThrowalidateExceptionIfNoDriverHasBeenSpecified
    *
    * @throws Exception test passes if a {@link ValidateException} has been
    * thrown
    */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfNoDriverHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("ds/unit/nodriver-ds.xml")
         .toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfNoConnectionUrlHasBeenSpecified
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfNoConnectionUrlHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/noconnectionurl-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfNoJndiNameHasBeenSpecified
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfNoJndiNameHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("ds/unit/nojndiname-ds.xml")
         .toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfNoPoolHasBeenSpecified
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfNoPoolHasBeenSpecified() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("ds/unit/nopool-ds.xml")
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
   * shouldThrowalidateExceptionIfallocationRetryNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfallocationRetryNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/allocationRetryNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfallocationRetryWaitMillisNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfallocationRetryWaitMillisNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/allocationRetryWaitMillisNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfBlockingTimeoutMillisNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfBlockingTimeoutMillisNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/BlockingTimeoutMillisNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfIdleTimeoutMinutesNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfIdleTimeoutMinutesNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/IdleTimeoutMinutesNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfnoconnectionurl
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfnoconnectionurl() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/noconnectionurl-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfqueryTimeoutNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfqueryTimeoutNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/queryTimeoutNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfuseTryLockNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfuseTryLockNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/useTryLockNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfxaresourcetimeoutNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfxaresourcetimeoutNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/xaresourcetimeoutNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfpreparedStatementCacheSizeNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfpreparedStatementCacheSizeNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/preparedStatementCacheSizeNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfbackgroundValidationMinutesNegative
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfbackgroundValidationMinutesNegative() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/backgroundValidationMinutesNegative-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfExceptionSorterClassNameMissing
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfExceptionSorterClassNameMissing() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/ExceptionSorterClassNameMissing-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfStaleConnectionCheckerClassNameMissing
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfStaleConnectionCheckerClassNameMissing() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/StaleConnectionCheckerClassNameMissing-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

   /**
   *
   * shouldThrowalidateExceptionIfValidationCheckerClassNameMissing
   *
   * @throws Exception test passes if a {@link ValidateException} has been
   * thrown
   */
   @Test(expected = ValidateException.class)
   public void shouldThrowalidateExceptionIfValidationCheckerClassNameMissing() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/ValidationCheckerClassNameMissing-ds.xml").toURI());
      //when
      doParse(xmlFile);
      //then throw ValidateException
   }

}
