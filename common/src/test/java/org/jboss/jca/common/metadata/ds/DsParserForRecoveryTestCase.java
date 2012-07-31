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

import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ds.v11.DsParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNull;

/**
 *
 * A DsParserForMinimalFileTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class DsParserForRecoveryTestCase
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
   @Test()
   public void shouldParseXaDsWithRecoveryInformation() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/xa-resource-with-recovery-ds.xml")
         .toURI());
      //when
      DataSources datasources = doParse(xmlFile);
      XaDataSource ds = datasources.getXaDataSource().get(0);
      Recovery recovery = ds.getRecovery();
      assertThat(recovery, not(isNull()));
      assertThat(recovery.getNoRecovery(), is(false));
      Credential security = recovery.getCredential();
      Extension plugin = recovery.getRecoverPlugin();
      assertThat(security.getUserName(), is("myUserName"));
      assertThat(security.getPassword(), is("myPassword"));
      assertThat(plugin.getClassName(), is("myClassName"));
      assertThat(plugin.getConfigPropertiesMap().size(), is(1));
      assertThat(plugin.getConfigPropertiesMap().get("MyProperty"), is("MyPropertyValue"));

      //then
   }

   private DataSources doParse(File xmlFile) throws FileNotFoundException, Exception, IOException, ValidateException
   {
      FileInputStream is = null;

      try
      {
         is = new FileInputStream(xmlFile);
         //when
         DataSources ds = parser.parse(is);
         return ds;
      }
      finally
      {
         if (is != null)
            is.close();
      }
   }


}
