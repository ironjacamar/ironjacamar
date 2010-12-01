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

import java.io.File;
import java.io.FileInputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DsParserForMinimalFileTestCase
{

   public static DsParser parser;

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      parser = new DsParser();
   }

   @Test
   public void shouldFailIfNoDriverHasBeenSpecified() throws Exception
   {

      FileInputStream is = null;

      //given
      File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("ds/unit/nodriver-ds.xml")
         .toURI());
      try
      {
         is = new FileInputStream(xmlFile);
         //when
         DataSources ds = parser.parse(is);
         //then
         assertThat(ds.getDataSource().size() + ds.getXaDataSource().size(), is(1));

      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

}

