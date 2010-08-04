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
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.Connector10;
import org.jboss.jca.common.api.metadata.ra.ra15.Connector15;
import org.jboss.jca.common.api.metadata.ra.ra16.Connector16;
import org.jboss.jca.common.metadataimpl.ra.RaParser;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

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
         //then
         assertThat(connector, instanceOf(Connector16.class));
         assertThat(connector.getVersion(), is(Version.V_16));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldParseConnector15
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseConnector15() throws Exception
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
         //then
         assertThat(connector, instanceOf(Connector15.class));
         assertThat(connector.getVersion(), is(Version.V_16));
         ResourceAdapter1516 ra = (ResourceAdapter1516) connector.getResourceadapter();
         assertThat(ra.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedconnectionfactoryClass().getValue(), is("aa"));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldParseConnector10
    * @throws Exception in case of error
    */
   @Test
   public void shouldParseConnector10() throws Exception
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
         //then
         assertThat(connector, instanceOf(Connector10.class));
         assertThat(connector.getVersion(), is(Version.V_10));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

}
