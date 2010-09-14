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
package org.jboss.jca.common.metadata.merge;

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra15.Connector15;
import org.jboss.jca.common.metadata.ds.DsParser;
import org.jboss.jca.common.metadata.ra.RaParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 *
 * A RaParserTestCase.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class MergerTestCase
{
   /**
    * shouldMergeDsAndConnector
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeDsAndConnector() throws Exception
   {

      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("adapters/ra.xml")
               .toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("ds/postgres-ds.xml")
               .toURI());
         is = new FileInputStream(xmlFile);
         DsParser dsparser = new DsParser();
         //when
         DataSources ds = dsparser.parse(is);

         List<? extends ConfigProperty> properties = null;
         ResourceAdapter1516 resourceAdapter1516 = (ResourceAdapter1516) connector.getResourceadapter();
         if (connector.getResourceadapter() != null &&
             connector.getResourceadapter() instanceof ResourceAdapter1516 &&
             resourceAdapter1516.getOutboundResourceadapter() != null &&
             resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            properties = resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
         }

         //verify pre-condition
         assertThat(resourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.LocalTransaction));
         assertThat(
               resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                     .getManagedConnectionFactoryClass(),
               equalTo(new XsdString("org.jboss.jca.adapters.jdbc.local." + "LocalManagedConnectionFactory", null)));

         //when
         Merger m = new Merger();
         Connector merged = m.mergeConnectorAndDs(ds.getDataSource().get(0), connector);
         //then
         assertThat(merged, instanceOf(Connector15.class));
         assertThat(merged.getVersion(), is(Version.V_15));

         List<? extends ConfigProperty> mergedProperties = null;

         ResourceAdapter1516 mergedResourceAdapter1516 = (ResourceAdapter1516) merged.getResourceadapter();
         if (connector.getResourceadapter() != null &&
             connector.getResourceadapter() instanceof ResourceAdapter1516 &&
             resourceAdapter1516.getOutboundResourceadapter() != null &&
             resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            mergedProperties = mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions()
                  .get(0).getConfigProperties();
         }
         //then merged properties are presents
         assertThat((List<ConfigProperty>) mergedProperties,
            hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.USERNAME, "x")));
         assertThat((List<ConfigProperty>) mergedProperties,
            hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.PASSWORD, "y")));
         assertThat((List<ConfigProperty>) mergedProperties,
            hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.CONNECTIONURL,
                     "jdbc:postgresql://[servername]:[port]/[database name]")));
         assertThat((List<ConfigProperty>) mergedProperties,
            hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.DRIVERCLASS, "org.postgresql.Driver")));

         //then metadata read from ra.xml still present (not deleted by merge)
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.LocalTransaction));
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass(),
               equalTo(new XsdString("org.jboss.jca.adapters.jdbc.local.LocalManagedConnectionFactory", null)));

         //then it have empty property for not set ones
         assertThat((List<ConfigProperty>) mergedProperties,
            not(hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.PREPAREDSTATEMENTCACHESIZE, ""))));

         //then it does not contain property not in ra.xml
         assertThat((List<ConfigProperty>) mergedProperties,
            not(hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.XADATASOURCEPROPERTIES, ""))));

      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldMergeXaDsAndConnector
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeXaDsAndConnector() throws Exception
   {

      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("adapters/ra-xa.xml")
               .toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("ds/postgres-xa-ds.xml")
               .toURI());
         is = new FileInputStream(xmlFile);
         DsParser dsparser = new DsParser();
         //when
         DataSources ds = dsparser.parse(is);

         List<? extends ConfigProperty> properties = null;
         ResourceAdapter1516 resourceAdapter1516 = (ResourceAdapter1516) connector.getResourceadapter();
         if (connector.getResourceadapter() != null &&
             connector.getResourceadapter() instanceof ResourceAdapter1516 &&
             resourceAdapter1516.getOutboundResourceadapter() != null &&
             resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            properties = resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
         }

         //verify pre-condition
         assertThat(resourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.XATransaction));
         assertThat(resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass(),
               equalTo(new XsdString("org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory", null)));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorAndDs(ds.getXaDataSource().get(0), connector);
         //then
         assertThat(merged, instanceOf(Connector15.class));
         assertThat(merged.getVersion(), is(Version.V_15));

         List<? extends ConfigProperty> mergedProperties = null;

         ResourceAdapter1516 mergedResourceAdapter1516 = (ResourceAdapter1516) merged.getResourceadapter();
         if (connector.getResourceadapter() != null &&
             connector.getResourceadapter() instanceof ResourceAdapter1516 &&
             resourceAdapter1516.getOutboundResourceadapter() != null &&
             resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            mergedProperties = mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions()
                  .get(0).getConfigProperties();
         }

         //then merged properties are presents

         assertThat((List<ConfigProperty>) mergedProperties,
            hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.XADATASOURCEPROPERTIES,
                     "DatabaseName=database_name;User=user;ServerName=server_name;PortNumber=5432;"
                           + "Password=password;")));

         assertThat((List<ConfigProperty>) mergedProperties,
            hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.XADATASOURCECLASS,
                     "org.postgresql.xa.PGXADataSource")));

         //then metadata read from ra.xml still present (not deleted by merge)
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.XATransaction));
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass(),
               equalTo(new XsdString("org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory", null)));

         //then it have empty property for not set ones
         assertThat((List<ConfigProperty>) mergedProperties,
            not(hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.PREPAREDSTATEMENTCACHESIZE, ""))));

         //then it does not contain property not in ra.xml
         assertThat((List<ConfigProperty>) mergedProperties,
            not(hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.CONNECTIONURL, ""))));

      }
      finally
      {
         if (is != null)
            is.close();
      }

   }
}
