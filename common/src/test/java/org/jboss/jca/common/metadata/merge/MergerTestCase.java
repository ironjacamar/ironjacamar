/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.resourceadapter.Activations;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.ds.DsParser;
import org.jboss.jca.common.metadata.ironjacamar.IronJacamarParser;
import org.jboss.jca.common.metadata.resourceadapter.ResourceAdapterParser;
import org.jboss.jca.common.metadata.spec.ConfigPropertyImpl;
import org.jboss.jca.common.metadata.spec.RaParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

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
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
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

         List<ConfigProperty> properties = null;
         ResourceAdapter resourceAdapter = connector.getResourceadapter();
         if (resourceAdapter != null &&
             resourceAdapter.getOutboundResourceadapter() != null &&
             resourceAdapter.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            properties = resourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getConfigProperties();
         }

         //verify pre-condition
         assertThat(resourceAdapter.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.LocalTransaction));
         assertThat(resourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(), equalTo("org.jboss.jca.adapters.jdbc.local."
               + "LocalManagedConnectionFactory"));

         //when
         Merger m = new Merger();
         Connector merged = m.mergeConnectorAndDs(ds.getDataSource().get(0), connector);
         //then
         assertThat(merged, instanceOf(Connector.class));
         assertThat(merged.getVersion(), is(Version.V_15));

         List<ConfigProperty> mergedProperties = null;

         ResourceAdapter mergedResourceAdapter = merged.getResourceadapter();
         if (mergedResourceAdapter != null &&
             mergedResourceAdapter.getOutboundResourceadapter() != null &&
             mergedResourceAdapter.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            mergedProperties = mergedResourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getConfigProperties();
         }
         //then merged properties are presents
         assertThat(mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.USERNAME, "x")));
         assertThat(mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.PASSWORD, "y")));
         assertThat(mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.CONNECTIONURL,
               "jdbc:postgresql://[servername]:[port]/[database name]")));
         assertThat(mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.DRIVERCLASS, "org.postgresql.Driver")));

         //then metadata read from ra.xml still present (not deleted by merge)
         assertThat(mergedResourceAdapter.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.LocalTransaction));
         assertThat(mergedResourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(),
               equalTo("org.jboss.jca.adapters.jdbc.local.LocalManagedConnectionFactory"));

         //then it have empty property for not set ones
         assertThat(mergedProperties,
               not(hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
                     Merger.ConfigPropertyFactory.Prototype.PREPAREDSTATEMENTCACHESIZE, ""))));

         //then it does not contain property not in ra.xml
         assertThat(mergedProperties,
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

         List<ConfigProperty> properties = null;
         ResourceAdapter resourceAdapter = connector.getResourceadapter();
         if (resourceAdapter != null &&
             resourceAdapter.getOutboundResourceadapter() != null &&
             resourceAdapter.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            properties = resourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getConfigProperties();
         }

         //verify pre-condition
         assertThat(resourceAdapter.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.XATransaction));
         assertThat(resourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(),
               equalTo("org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory"));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorAndDs(ds.getXaDataSource().get(0), connector);
         //then
         assertThat(merged, instanceOf(Connector.class));
         assertThat(merged.getVersion(), is(Version.V_15));

         List<ConfigProperty> mergedProperties = null;

         ResourceAdapter mergedResourceAdapter = merged.getResourceadapter();
         if (mergedResourceAdapter != null &&
             mergedResourceAdapter.getOutboundResourceadapter() != null &&
             mergedResourceAdapter.getOutboundResourceadapter().getConnectionDefinitions() != null)
         {
            mergedProperties = mergedResourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getConfigProperties();
         }

         //then merged properties are presents

         // recreate config-property-value string from datasource
         XaDataSource xads = ds.getXaDataSource().get(0);
         StringBuffer configPropertyValueBuf = new StringBuffer();
         for (Map.Entry<String, String> xaConfigProperty : xads.getXaDataSourceProperty().entrySet())
         {
            configPropertyValueBuf.append(xaConfigProperty.getKey());
            configPropertyValueBuf.append("=");
            configPropertyValueBuf.append(xaConfigProperty.getValue());
            configPropertyValueBuf.append(";");
         }
         assertThat(mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.XADATASOURCEPROPERTIES, configPropertyValueBuf.toString())));

         assertThat(mergedProperties, hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
               Merger.ConfigPropertyFactory.Prototype.XADATASOURCECLASS, "org.postgresql.xa.PGXADataSource")));

         //then metadata read from ra.xml still present (not deleted by merge)
         assertThat(mergedResourceAdapter.getOutboundResourceadapter().getTransactionSupport(),
               is(TransactionSupportEnum.XATransaction));
         assertThat(mergedResourceAdapter.getOutboundResourceadapter().getConnectionDefinitions().get(0)
               .getManagedConnectionFactoryClass().getValue(),
               equalTo("org.jboss.jca.adapters.jdbc.xa.XAManagedConnectionFactory"));

         //then it have empty property for not set ones
         assertThat(mergedProperties,
               not(hasItem(Merger.ConfigPropertyFactory.createConfigProperty(
                     Merger.ConfigPropertyFactory.Prototype.PREPAREDSTATEMENTCACHESIZE, ""))));

         //then it does not contain property not in ra.xml
         assertThat(mergedProperties,
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
         Activations ra = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ra.getActivations().get(0), notNullValue());

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ra.getActivations().get(0), connector);
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
         Activation ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(connector.getResourceadapter().getOutboundResourceadapter()
                    .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringRAR", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("XMLOVERRIDE", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(merged.getResourceadapter().getResourceadapterClass(),
            equalTo(connector.getResourceadapter().getResourceadapterClass()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter(),
            is(connector.getResourceadapter().getOutboundResourceadapter()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(merged.getResourceadapter().getInboundResourceadapter(),
            equalTo(connector.getResourceadapter().getInboundResourceadapter()));
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
         Activation ij1 = ijParser.parse(is);

         xmlFile = new File(Thread.currentThread().getContextClassLoader()
            .getResource("merger/ironjacamar-merging-with-connector-distributivity2.xml").toURI());
         is = new FileInputStream(xmlFile);
         Activation ij2 = ijParser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij1, notNullValue());
         assertThat(ij2, notNullValue());
         assertThat(connector.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij1, connector);
         merged = mf.mergeConnectorWithCommonIronJacamar(ij2, connector);

         Connector merged2 = mf.mergeConnectorWithCommonIronJacamar(ij2, connector);
         merged2 = mf.mergeConnectorWithCommonIronJacamar(ij1, connector);
         //then
         ConfigProperty expectedConfigProp =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringRAR", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("XMLOVERRIDE", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         ConfigProperty expectedConfigProp2 =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringRAR2", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("XMLOVERRIDE", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties(),
            hasItems(expectedConfigProp, expectedConfigProp2));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(merged.getResourceadapter().getResourceadapterClass(),
            equalTo(connector.getResourceadapter().getResourceadapterClass()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter(),
            is(connector.getResourceadapter().getOutboundResourceadapter()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(merged.getResourceadapter().getInboundResourceadapter(),
            equalTo(connector.getResourceadapter().getInboundResourceadapter()));

         assertThat(merged2, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties(),
            hasItems(expectedConfigProp, expectedConfigProp2));
         assertThat(merged2.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged2.getVersion(), equalTo(connector.getVersion()));
         assertThat(merged2.getResourceadapter().getResourceadapterClass(),
            equalTo(connector.getResourceadapter().getResourceadapterClass()));
         assertThat(merged2.getResourceadapter().getOutboundResourceadapter(),
            is(connector.getResourceadapter().getOutboundResourceadapter()));
         assertThat(merged2.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(merged2.getResourceadapter().getInboundResourceadapter(),
            equalTo(connector.getResourceadapter().getInboundResourceadapter()));
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
         Activation ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(connector.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);

         ConfigProperty expectedConfigProp =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringRAR", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("XMLOVERRIDE", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(merged.getResourceadapter().getResourceadapterClass(),
            equalTo(connector.getResourceadapter().getResourceadapterClass()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter(),
            is(connector.getResourceadapter().getOutboundResourceadapter()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(merged.getResourceadapter().getInboundResourceadapter(),
            equalTo(connector.getResourceadapter().getInboundResourceadapter()));

         //another time!!
         merged = mf.mergeConnectorWithCommonIronJacamar(ij, merged);

         //then

         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(merged.getResourceadapter().getResourceadapterClass(),
            equalTo(connector.getResourceadapter().getResourceadapterClass()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter(),
            is(connector.getResourceadapter().getOutboundResourceadapter()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(merged.getResourceadapter().getInboundResourceadapter(),
            equalTo(connector.getResourceadapter().getInboundResourceadapter()));

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
         Activation ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(connector.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringRAR", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("XMLOVERRIDE", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat(connector.getResourceadapter().getConfigProperties(),
            hasItem(expectedConfigProp));
         assertThat(merged.getEisType(), equalTo(connector.getEisType()));
         assertThat(merged.getVersion(), equalTo(connector.getVersion()));
         assertThat(merged.getResourceadapter().getResourceadapterClass(),
            equalTo(connector.getResourceadapter().getResourceadapterClass()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter(),
            is(connector.getResourceadapter().getOutboundResourceadapter()));
         assertThat(merged.getResourceadapter().getOutboundResourceadapter()
            .getConnectionDefinitions().size(), is(1));
         assertThat(merged.getResourceadapter().getInboundResourceadapter(),
            equalTo(connector.getResourceadapter().getInboundResourceadapter()));
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
         Activation ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(connector.getResourceadapter().getAdminObjects().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringAdmin", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("default", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat(merged.getResourceadapter().getAdminObjects().size(), is(1));
         assertThat(merged.getResourceadapter().getAdminObjects().get(0)
            .getConfigProperties().size(), is(1));
         assertThat(
            merged.getResourceadapter().getAdminObjects().get(0)
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
         Activation ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(connector.getResourceadapter().getAdminObjects().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringAdmin", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("default", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat(merged.getResourceadapter().getAdminObjects().size(), is(1));
         assertThat(merged.getResourceadapter().getAdminObjects().get(0)
            .getConfigProperties().size(), is(1));
         assertThat(
            merged.getResourceadapter().getAdminObjects().get(0)
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
         Activation ij = raparser.parse(is);

         assertThat(connector, notNullValue());
         assertThat(ij, notNullValue());
         assertThat(connector.getResourceadapter().getAdminObjects().size(), is(1));

         //when
         Merger mf = new Merger();
         Connector merged = mf.mergeConnectorWithCommonIronJacamar(ij, connector);
         //then
         ConfigProperty expectedConfigProp =
            new ConfigPropertyImpl(null,
                                   new XsdString("StringAdmin", null, "config-property-name"),
                                   new XsdString("java.lang.String", null, "config-property-type"),
                                   new XsdString("default", null, "config-property-value"),
                                   null, null, null, null, false, null, null, null, null);
         assertThat(merged, notNullValue());
         assertThat(connector.getResourceadapter().getConfigProperties().size(), is(1));
         assertThat(merged.getResourceadapter().getAdminObjects().size(), is(1));
         assertThat(merged.getResourceadapter().getAdminObjects().get(0)
            .getConfigProperties().size(), is(1));
         assertThat(
            merged.getResourceadapter().getAdminObjects().get(0)
               .getConfigProperties(), hasItem(expectedConfigProp));
      }
      finally
      {
         if (is != null)
            is.close();
      }

   }
}
