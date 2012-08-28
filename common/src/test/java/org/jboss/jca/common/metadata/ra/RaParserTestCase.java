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
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.ra10.Connector10;
import org.jboss.jca.common.api.metadata.ra.ra15.Connector15;
import org.jboss.jca.common.api.metadata.ra.ra16.Connector16;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

/**
 *
 * A RaParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class RaParserTestCase
{
   /**
    * shouldParseConnector16
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseConnector16() throws Exception
   {

      Connector connector = parseXML("connector-1.6-sample.xml");

      assertThat(connector, instanceOf(Connector16.class));
      assertThat(connector.getVersion(), is(Version.V_16));
   }

   /**
    * shouldParseConnector15
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseConnector15() throws Exception
   {
      Connector connector = parseXML("connector-1.5-sample.xml");

      assertThat(connector, instanceOf(Connector15.class));
      assertThat(connector.getVersion(), is(Version.V_15));
   }

   /**
    * shouldParseConnector15MoreComplex
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseConnector16MoreComplex() throws Exception
   {
      Connector connector = parseXML("ra.xml");

      assertThat(connector, instanceOf(Connector16.class));
      assertThat(connector.getVersion(), is(Version.V_16));
   }

   /**
    * shouldParseConnector10
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseConnector10() throws Exception
   {
      Connector connector = parseXML("connector-1.0-sample.xml");

      assertThat(connector, instanceOf(Connector10.class));
      assertThat(connector.getVersion(), is(Version.V_10));
   }

   /**
    * Parses XML representation
    * @param fileName containing XML
    * @return connector object
    * @throws Exception in case of error
    */
   public Connector parseXML(String fileName) throws Exception
   {
      FileInputStream is = null;
      ByteArrayInputStream bais = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource(fileName).toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         //when
         Connector connector1 = parser.parse(is);

         String xmlRepresentation = connector1.toString();
         System.out.println(xmlRepresentation);
         bais = new ByteArrayInputStream(xmlRepresentation.getBytes("UTF-8"));
         Connector connector2 = parser.parse(bais);

         //then
         assertEquals(connector1.getClass(), connector2.getClass());
         assertEquals(xmlRepresentation, connector2.toString());
         assertEquals(connector1, connector2);
         return connector1;
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
