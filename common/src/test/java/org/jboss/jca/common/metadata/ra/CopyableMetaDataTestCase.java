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
package org.jboss.jca.common.metadata.ra;

import org.jboss.jca.common.api.metadata.ra.Connector;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 *
 * A RaParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class CopyableMetaDataTestCase
{
   /**
    * shouldCopyConnector16
    * @throws Exception in case of error
    */
   @Test
   public void shouldCopyConnector16() throws Exception
   {

      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("connector-1.6-sample.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         //when
         Connector connector = parser.parse(is);
         Connector newConnector = (Connector) connector.copy();
         //then
         assertEquals(connector.toString(), newConnector.toString());
         assertThat(connector == newConnector, is(false));
         assertThat(connector.equals(newConnector), is(true));
         assertThat(connector.hashCode(), is(newConnector.hashCode()));



      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldCopyConnector16
    * @throws Exception in case of error
    */
   @Test
   public void shouldCopyConnector15() throws Exception
   {

      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("connector-1.5-sample.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         //when
         Connector connector = parser.parse(is);
         Connector newConnector = (Connector) connector.copy();
         //then
         assertEquals(connector.toString(), newConnector.toString());
         assertThat(connector == newConnector, is(false));
         assertThat(connector.equals(newConnector), is(true));
         assertThat(connector.hashCode(), is(newConnector.hashCode()));

      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldCopyConnector16 more complex
    * @throws Exception in case of error
    */
   @Test
   public void shouldCopyConnector16MoreComplex() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("ra.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         //when
         Connector connector = parser.parse(is);
         Connector newConnector = (Connector) connector.copy();
         //then
         assertEquals(connector.toString(), newConnector.toString());
         assertThat(connector == newConnector, is(false));
         assertThat(connector.equals(newConnector), is(true));
         assertThat(connector.hashCode(), is(newConnector.hashCode()));


      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldCopyConnector10
    * @throws Exception in case of error
    */
   @Test
   public void shouldCopyConnector10() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("connector-1.0-sample.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         //when
         Connector connector = parser.parse(is);
         Connector newConnector = (Connector) connector.copy();
         //then
         assertEquals(connector.toString(), newConnector.toString());
         assertThat(connector == newConnector, is(false));
         assertThat(connector.equals(newConnector), is(true));
         assertThat(connector.hashCode(), is(newConnector.hashCode()));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

}
