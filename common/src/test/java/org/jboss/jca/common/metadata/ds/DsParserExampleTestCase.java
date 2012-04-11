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
import org.jboss.jca.common.metadata.ds.v11.DsParser;

import java.io.File;
import java.io.FileInputStream;

import org.jboss.util.file.FileSuffixFilter;

import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 * A RaParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
@RunWith(Theories.class)
public class DsParserExampleTestCase
{

   private static DsParser parser;

   /** Datapoints for junit Theory **/
   @DataPoints
   public static File[] xmlFiles;

   /**
    *
    * beforeClass method
    *
    * @throws Exception in casae of file not found
    */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      File directory = new File(DsParserExampleTestCase.class.getClassLoader().getResource("ds/example").toURI());
      xmlFiles = directory.listFiles(new FileSuffixFilter("-ds.xml"));
      parser = new DsParser();
      //this property is set just to make possible property substitution defined in test resources.
      //but property substitution is not the goal of this test case see DsParserForTemplateReplaceTestCase for that
      System.setProperty("jboss.server.data.dir", "/tmp");

   }


   /**
    * shouldParseAnyExample
    * @param xmlFile the file on which apply this theory
    * @throws Exception in case of error
    */
   @Theory
   public void shouldParseAnyExample(final File xmlFile) throws Exception
   {
      FileInputStream is = null;

      //given
      try
      {
         is = new FileInputStream(xmlFile);
         //when
         DataSources ds = parser.parse(is);
         //then
         assertThat(ds.getDataSource().size() + ds.getXaDataSource().size(), is(1));

      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("got an exception for file" + xmlFile.toString() + " with message: " + e.getMessage());
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

}
