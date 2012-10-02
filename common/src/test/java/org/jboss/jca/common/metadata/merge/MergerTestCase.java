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
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra15.Connector15;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapters;
import org.jboss.jca.common.metadata.ds.v11.DsParser;
import org.jboss.jca.common.metadata.ironjacamar.v10.IronJacamarParser;
import org.jboss.jca.common.metadata.ra.RaParser;
import org.jboss.jca.common.metadata.ra.common.ConfigPropertyImpl;
import org.jboss.jca.common.metadata.resourceadapter.v10.ResourceAdapterParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

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
         File xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("merger/adapters/ra.xml")
               .toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("merger/ds/postgres-ds.xml")
               .toURI());
         is = new FileInputStream(xmlFile);
         DsParser dsparser = new DsParser();
         //when
         DataSources ds = dsparser.parse(is);

         List<? extends ConfigProperty> properties = null;
         ResourceAdapter1516 resourceAdapter1516 = (ResourceAdapter1516) connector.getResourceadapter();
         if (connector.getResourceadapter() != null && connector.getResourceadapter() instanceof ResourceAdapter1516
               && resourceAdapter1516.getOutboundResourceadapter() != null
               && resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            properties = resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
         }

         //verify pre-condition
         assertThat(resourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.LocalTransaction));
         assertThat(resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(), equalTo("org.jboss.jca.adapters.jdbc.local."
               + "LocalManagedConnectionFactory"));

         //when
         Merger m = new Merger();
         Connector merged = m.mergeConnectorAndDs(ds.getDataSource().get(0), connector);
         //then
         assertThat(merged, instanceOf(Connector15.class));
         assertThat(merged.getVersion(), is(Version.V_15));

         List<? extends ConfigProperty> mergedProperties = null;

         ResourceAdapter1516 mergedResourceAdapter1516 = (ResourceAdapter1516) merged.getResourceadapter();
         if (connector.getResourceadapter() != null && connector.getResourceadapter() instanceof ResourceAdapter1516
               && resourceAdapter1516.getOutboundResourceadapter() != null
               && resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            mergedProperties = mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
         }
         //then merged properties are presents
         assertThat((List<ConfigProperty>) mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.USERNAME, "x")));
         assertThat((List<ConfigProperty>) mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.PASSWORD, "y")));
         assertThat((List<ConfigProperty>) mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.CONNECTIONURL,
               "jdbc:postgresql://[servername]:[port]/[database name]")));
         assertThat((List<ConfigProperty>) mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.DRIVERCLASS, "org.postgresql.Driver")));

         //then metadata read from ra.xml still present (not deleted by merge)
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.LocalTransaction));
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(),
               equalTo("org.jboss.jca.adapters.jdbc.local.LocalManagedConnectionFactory"));

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
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("merger/adapters/ra-xa.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader().getResource("merger/ds/postgres-xa-ds.xml")
               .toURI());
         is = new FileInputStream(xmlFile);
         DsParser dsparser = new DsParser();
         //when
         DataSources ds = dsparser.parse(is);

         List<? extends ConfigProperty> properties = null;
         ResourceAdapter1516 resourceAdapter1516 = (ResourceAdapter1516) connector.getResourceadapter();
         if (connector.getResourceadapter() != null && connector.getResourceadapter() instanceof ResourceAdapter1516
               && resourceAdapter1516.getOutboundResourceadapter() != null
               && resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            properties = resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
         }

         //verify pre-condition
         assertThat(resourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.XATransaction));
         assertThat(resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(),
               equalTo("org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory"));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorAndDs(ds.getXaDataSource().get(0), connector);
         //then
         assertThat(merged, instanceOf(Connector15.class));
         assertThat(merged.getVersion(), is(Version.V_15));

         List<? extends ConfigProperty> mergedProperties = null;

         ResourceAdapter1516 mergedResourceAdapter1516 = (ResourceAdapter1516) merged.getResourceadapter();
         if (connector.getResourceadapter() != null && connector.getResourceadapter() instanceof ResourceAdapter1516
               && resourceAdapter1516.getOutboundResourceadapter() != null
               && resourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            mergedProperties = mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
                  .getConfigProperties();
         }

         //then merged properties are presents

         assertThat((List<ConfigProperty>) mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.XADATASOURCEPROPERTIES,
               "DatabaseName=database_name;User=user;ServerName=server_name;PortNumber=5432;" + "Password=password;")));

         assertThat((List<ConfigProperty>) mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.XADATASOURCECLASS, "org.postgresql.xa.PGXADataSource")));

         //then metadata read from ra.xml still present (not deleted by merge)
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.XATransaction));
         assertThat(mergedResourceAdapter1516.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(),
               equalTo("org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory"));

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

   /**
    * shouldMergeRaXmlConnector
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeRaXmlConnector() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("merger/connector-merging-with-ra.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
               .getResource("merger/ra-merging-with-connector.xml").toURI());
         is = new FileInputStream(xmlFile);
         ResourceAdapterParser raparser = new ResourceAdapterParser();
         ResourceAdapters ra = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ra.getResourceAdapters().get(0), notNullValue());

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ra.getResourceAdapters().get(0), connector);
         //then

         assertThat(merged, notNullValue());
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldMergeIronJacamarConnector
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeIronJacamarConnector() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/connector-merging-with-ironjacamar.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-with-connector.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamarParser raparser = new IronJacamarParser();
         IronJacamar ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp = new ConfigPropertyImpl(null,
               new XsdString("StringRAR", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("XMLOVERRIDE", null, "config-property-value"), null);
         assertThat(merged, notNullValue());
         assertThat((List<ConfigProperty>) connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getResourceadapterClass(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getResourceadapterClass()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter(),
            is(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getInboundResourceadapter(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getInboundResourceadapter()));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldMergeCommonIronJacamarAndConnectorBeDistributive
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeCommonIronJacamarAndConnectorBeDistributive() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/connector-merging-with-ironjacamar-distributivity.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-with-connector-distributivity1.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamarParser ijParser = new IronJacamarParser();
         IronJacamar ij1 = ijParser.parse(is);

         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-with-connector-distributivity2.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamar ij2 = ijParser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij1, notNullValue());
         assertThat(ij2, notNullValue());
         assertThat(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij1, connector);
         merged = mf.mergeConnectorWithCommonIronJacamar(ij2, connector);

         Connector merged2 = mf.mergeConnectorWithCommonIronJacamar(ij2, connector);
         merged2 = mf.mergeConnectorWithCommonIronJacamar(ij1, connector);
         //then
         ConfigProperty expectedConfigProp = new ConfigPropertyImpl(null,
               new XsdString("StringRAR", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("XMLOVERRIDE", null, "config-property-value"), null);
         ConfigProperty expectedConfigProp2 = new ConfigPropertyImpl(null,
               new XsdString("StringRAR2", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("XMLOVERRIDE", null, "config-property-value"), null);
         assertThat(merged, notNullValue());
         assertThat((List<ConfigProperty>) connector.getResourceadapter().getConfigProperties(),
            hasItems(expectedConfigProp, expectedConfigProp2));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getResourceadapterClass(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getResourceadapterClass()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter(),
            is(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getInboundResourceadapter(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getInboundResourceadapter()));

         assertThat(merged2, notNullValue());
         assertThat((List<ConfigProperty>) connector.getResourceadapter().getConfigProperties(),
            hasItems(expectedConfigProp, expectedConfigProp2));
         assertThat(merged2.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged2.getVersion(), equalTo(connector.getVersion()));
         assertThat(((ResourceAdapter1516) merged2.getResourceadapter()).getResourceadapterClass(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getResourceadapterClass()));
         assertThat(((ResourceAdapter1516) merged2.getResourceadapter()).getOutboundResourceadapter(),
            is(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()));
         assertThat(((ResourceAdapter1516) merged2.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(((ResourceAdapter1516) merged2.getResourceadapter()).getInboundResourceadapter(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getInboundResourceadapter()));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldMergeCommonIronJacamarAndConnectorBeDistributive
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeCommonIronJacamarAndConnectorBeNeutralOnDoubleMerge() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/connector-merging-with-ironjacamar.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-with-connector.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamarParser raparser = new IronJacamarParser();
         IronJacamar ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);

         ConfigProperty expectedConfigProp = new ConfigPropertyImpl(null,
               new XsdString("StringRAR", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("XMLOVERRIDE", null, "config-property-value"), null);
         assertThat(merged, notNullValue());
         assertThat((List<ConfigProperty>) connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getResourceadapterClass(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getResourceadapterClass()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter(),
            is(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getInboundResourceadapter(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getInboundResourceadapter()));

         //another time!!
         merged = mf.mergeConnectorWithCommonIronJacamar(ij, merged);

         //then

         assertThat(merged, notNullValue());
         assertThat((List<ConfigProperty>) connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getResourceadapterClass(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getResourceadapterClass()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter(),
            is(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getInboundResourceadapter(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getInboundResourceadapter()));

      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldIgnorePropertyInIronJacamarAndNotInConnector
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldIgnorePropertyInIronJacamarAndNotInConnector() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/connector-merging-with-ironjacamar.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-with-connector-withNewProperty.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamarParser raparser = new IronJacamarParser();
         IronJacamar ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp = new ConfigPropertyImpl(null,
               new XsdString("StringRAR", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("XMLOVERRIDE", null, "config-property-value"), null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat((List<ConfigProperty>) connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getResourceadapterClass(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getResourceadapterClass()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter(),
            is(((ResourceAdapter1516) connector.getResourceadapter()).getOutboundResourceadapter()));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getInboundResourceadapter(),
            equalTo(((ResourceAdapter1516) connector.getResourceadapter()).getInboundResourceadapter()));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }


   /**
    * shouldMergeAdminObj
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeAdminObj() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/connector-merging-with-ironjacamar-adminObj.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-admi-obj.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamarParser raparser = new IronJacamarParser();
         IronJacamar ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(((ResourceAdapter1516) connector.getResourceadapter()).getAdminObjects().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp = new ConfigPropertyImpl(null,
               new XsdString("StringAdmin", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("default", null, "config-property-value"), null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().get(0)
            .getConfigProperties().size(), is(1));
         assertThat(
            (List<ConfigProperty>) ((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().get(0)
               .getConfigProperties(), hasItem(expectedConfigProp));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }


   /**
    * shouldMergeAdminObjIgnoringNotEnabled
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeAdminObjIgnoringNotEnabled() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/connector-merging-with-ironjacamar-adminObj.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-admi-obj-notenabled.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamarParser raparser = new IronJacamarParser();
         IronJacamar ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(((ResourceAdapter1516) connector.getResourceadapter()).getAdminObjects().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp = new ConfigPropertyImpl(null,
               new XsdString("StringAdmin", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("default", null, "config-property-value"), null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().get(0)
            .getConfigProperties().size(), is(1));
         assertThat(
            (List<ConfigProperty>) ((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().get(0)
               .getConfigProperties(), hasItem(expectedConfigProp));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }

   /**
    * shouldMergeAdminObjIgnoringNotEnabled
    * @throws Exception in case of error
    */
   @SuppressWarnings("unchecked")
   @Test
   public void shouldMergeAdminObjIgnoringNonMatching() throws Exception
   {
      FileInputStream is = null;
      try
      {
         //given
         File xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/connector-merging-with-ironjacamar-adminObj.xml").toURI());
         is = new FileInputStream(xmlFile);
         RaParser parser = new RaParser();
         Connector connector = parser.parse(is);
         is.close();
         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-admi-obj-notmatching.xml").toURI());
         is = new FileInputStream(xmlFile);
         IronJacamarParser raparser = new IronJacamarParser();
         IronJacamar ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(((ResourceAdapter1516) connector.getResourceadapter()).getAdminObjects().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp = new ConfigPropertyImpl(null,
               new XsdString("StringAdmin", null, "config-property-name"),
               new XsdString("java.lang.String", null, "config-property-type"),
               new XsdString("default", null, "config-property-value"), null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().size(), is(1));
         assertThat(((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().get(0)
            .getConfigProperties().size(), is(1));
         assertThat(
            (List<ConfigProperty>) ((ResourceAdapter1516) merged.getResourceadapter()).getAdminObjects().get(0)
               .getConfigProperties(), hasItem(expectedConfigProp));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }
}
