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

import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 *
 * Test case for parsing the spy functionality parameter
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 *
 */
public class DsParserForSpyTestCase
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
    * Spy enabled for a datasource
    *
    * @throws Exception in case of parser error
    */
   @Test
   public void shouldHaveSpyEnabled() throws Exception
   {

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader()
         .getResource("ds/unit/spy-ds.xml")
         .toURI());
      //when
      DataSources ds = doParse(xmlFile);
      //then
      boolean actualSpy = ds.getDataSource().get(0).isSpy();
      assertThat(actualSpy, is(true));
   }

   private DataSources doParse(File xmlFile) 
      throws FileNotFoundException, ParserException, IOException, ValidateException, Exception
   {
      FileInputStream is = null;

      try
      {
         is = new FileInputStream(xmlFile);
         //when
         return parser.parse(is);

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }
}
