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
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.jboss.util.file.FilenamePrefixFilter;

import org.hamcrest.core.IsNull;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 *
 * A IronJacamarParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class IronJacamarParserTestCase
{
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

}
