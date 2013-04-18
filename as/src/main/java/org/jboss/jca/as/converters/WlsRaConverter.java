/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.converters;

import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectGroup;
import org.jboss.jca.as.converters.wls.api.metadata.AdminObjectInstance;
import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperty;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinition;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionInstance;
import org.jboss.jca.as.converters.wls.api.metadata.PoolParams;
import org.jboss.jca.as.converters.wls.api.metadata.TransactionSupport;
import org.jboss.jca.as.converters.wls.api.metadata.WeblogicConnector;
import org.jboss.jca.as.converters.wls.metadata.ConnectionDefinitionPropertiesImpl;
import org.jboss.jca.as.converters.wls.metadata.WeblogicRaPasrer;
import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * ConnectionFactoryConverter
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class WlsRaConverter
{

   /**
    * ConnectionFactoryConverter constructor
    */
   public WlsRaConverter()
   {
   }

   /**
    * convert 
    * @param in inputStream
    * @param out outputStream
    * @throws Exception exception
    */
   public void convert(InputStream in, OutputStream out) throws Exception
   {
      WeblogicRaPasrer parser = new WeblogicRaPasrer();
      WeblogicConnector wlsConnector = parser.parse(in);

      ConnectionFactories ds = transform(wlsConnector);

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(new StringReader(ds.toString())));

      TransformerFactory tfactory = TransformerFactory.newInstance();
      Transformer serializer;

      serializer = tfactory.newTransformer();
      //Setup indenting to "pretty print"
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");
      serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      serializer.transform(new DOMSource(doc), new StreamResult(out));

   }
   
   private ConnectionFactories transform(WeblogicConnector ra) throws Exception
   {
      List<NoTxConnectionFactory> noTxConnectionFactory = new ArrayList<NoTxConnectionFactory>();
      List<TxConnectionFactory> txConnectionFactory = new ArrayList<TxConnectionFactory>();
   
      if (ra.getOutboundResourceAdapter() == null || 
            ra.getOutboundResourceAdapter().getConnectionDefinitionGroup() == null ||
            ra.getOutboundResourceAdapter().getConnectionDefinitionGroup().size() == 0)
         return null;
      
      ConnectionDefinitionProperties defaultCdProps = ra.getOutboundResourceAdapter().getDefaultConnectionProperties();
            
      for (ConnectionDefinition conDef : ra.getOutboundResourceAdapter().getConnectionDefinitionGroup())
      {
         ConnectionDefinitionProperties groupCdProps = mergedCdProps(defaultCdProps,
               conDef.getDefaultConnectionProperties());
         if (conDef.getConnectionInstance() == null)
            continue;
         
         for (ConnectionInstance conInstance : conDef.getConnectionInstance())
         {
            ConnectionDefinitionProperties myCdProps = mergedCdProps(groupCdProps,
                  conInstance.getConnectionProperties());
            
            if (myCdProps.getTransactionSupport().equals(TransactionSupport.NoTransaction))
            {
               noTxConnectionFactory.add(buildNoTxConnectionFactory(conInstance.getJndiName(), myCdProps, ra));
            }
            else
            {
               txConnectionFactory.add(buildTxConnectionFactory(conInstance.getJndiName(), myCdProps, ra));
            }
         }
      }
      return new ConnectionFactoriesImpl(noTxConnectionFactory, txConnectionFactory);
   }
   
   private NoTxConnectionFactory buildNoTxConnectionFactory(String jndiName, 
         ConnectionDefinitionProperties myCdProps, WeblogicConnector ra) throws Exception
   {
      Map<String, String> rarProps = transformConfigProperties(ra.getProperties());
      Map<String, String> connProps = transformConfigProperties(myCdProps.getProperties());
      
      LegacyConnectionFactoryImp noTxCf = new LegacyConnectionFactoryImp(jndiName, "wls.rar", rarProps, "FIXME", 
            "FIXME", connProps, TransactionSupportEnum.NoTransaction);
      transformAdminObjects(noTxCf, ra);
      transformResourceAdapter(noTxCf, myCdProps);
      noTxCf.buildResourceAdapterImpl();
      transformSecurity(noTxCf, ra);
      return noTxCf;
   }

   private TxConnectionFactory buildTxConnectionFactory(String jndiName, 
         ConnectionDefinitionProperties myCdProps, WeblogicConnector ra) throws Exception
   {
      Map<String, String> rarProps = transformConfigProperties(ra.getProperties());
      Map<String, String> connProps = transformConfigProperties(myCdProps.getProperties());
      
      LegacyConnectionFactoryImp txCf;
      if (myCdProps.getTransactionSupport().equals(TransactionSupport.LocalTransaction))
      {
         txCf = new LegacyConnectionFactoryImp(jndiName, "wls.rar", rarProps, "FIXME", "FIXME", connProps,
               TransactionSupportEnum.LocalTransaction);
      }
      else
      {
         txCf = new LegacyConnectionFactoryImp(jndiName, "wls.rar", rarProps, "FIXME", "FIXME", connProps,
               TransactionSupportEnum.XATransaction);
      }
      transformAdminObjects(txCf, ra);
      transformResourceAdapter(txCf, myCdProps);
      txCf.buildResourceAdapterImpl();
      return txCf;
   }
   
   private void transformResourceAdapter(LegacyConnectionFactoryImp lcf, ConnectionDefinitionProperties myCdProps)
      throws Exception
   {
      lcf.buildCommonPool(myCdProps.getPoolParams().getInitialCapacity(), myCdProps.getPoolParams().getMaxCapacity(),
            Defaults.PREFILL, Defaults.USE_STRICT_MIN, Defaults.INTERLEAVING);
      lcf.buildTimeOut(new Long(myCdProps.getPoolParams().getConnectionReserveTimeoutSeconds() * 1000), new Long(
            myCdProps.getPoolParams().getConnectionReserveTimeoutSeconds()), 5, 
            new Long(myCdProps.getPoolParams().getTestFrequencySeconds() * 1000), 0);
      lcf.buildSecurity("", "", true);
   }

   private ConnectionDefinitionProperties mergedCdProps(ConnectionDefinitionProperties oldCdProps, 
         ConnectionDefinitionProperties newCdProps)
   {
      //TODO should merge more properties
      PoolParams poolParams = newCdProps.getPoolParams() == null ? 
            oldCdProps.getPoolParams() : newCdProps.getPoolParams();
      ConfigProperties props = newCdProps.getProperties() == null ?
            oldCdProps.getProperties() : newCdProps.getProperties();
      
      TransactionSupport trans = oldCdProps.getTransactionSupport();
      if (newCdProps.getTransactionSupport() != null &&
            newCdProps.getTransactionSupport() != TransactionSupport.NotDefined)
      {
         trans = newCdProps.getTransactionSupport();
      }
      return new ConnectionDefinitionPropertiesImpl(poolParams, null, trans, 
            newCdProps.getAuthenticationMechanism(), newCdProps.getReauthenticationSupport(), 
            props, newCdProps.getResAuth());
   }

   private Map<String, String> transformConfigProperties(ConfigProperties props)
   {
      if (props == null || props.getProperty() == null ||
            props.getProperty().size() == 0)
         return null;
      
      Map<String, String> newprops = new HashMap<String, String>();
      for (ConfigProperty cp : props.getProperty())
      {
         newprops.put(cp.getName(), cp.getValue());
      }
      return newprops;
   }

   private void transformAdminObjects(LegacyConnectionFactoryImp lcf, WeblogicConnector ra) throws Exception
   {
      if (ra.getAdminObjects() == null || 
            ra.getAdminObjects().getAdminObjectGroup() == null ||
            ra.getAdminObjects().getAdminObjectGroup().size() == 0)
         return;
      
      final Map<String, String> defaultProps = new HashMap<String, String>();
      if (ra.getAdminObjects().getDefaultProperties() != null &&
            ra.getAdminObjects().getDefaultProperties().getProperty() != null)
      {
         for (ConfigProperty cp : ra.getAdminObjects().getDefaultProperties().getProperty())
         {
            defaultProps.put(cp.getName(), cp.getValue());
         }
      }

      for (AdminObjectGroup aog : ra.getAdminObjects().getAdminObjectGroup())
      {
         if (aog.getAdminObjectInstance() == null ||
               aog.getAdminObjectInstance().size() == 0)
            return;
         
         final Map<String, String> aogProps = new HashMap<String, String>();
         aogProps.putAll(defaultProps);
         if (aog.getDefaultProperties() != null && 
               aog.getDefaultProperties().getProperty() != null)
         {
            for (ConfigProperty cp : aog.getDefaultProperties().getProperty())
            {
               aogProps.put(cp.getName(), cp.getValue());
            }
         }
         
         for (AdminObjectInstance aoi : aog.getAdminObjectInstance())
         {
            Map<String, String> aoiProps = null;
            if (aoi.getProperties() != null && 
                  aoi.getProperties().getProperty() != null)
            {
               aoiProps = new HashMap<String, String>();
               aoiProps.putAll(aogProps);
               
               for (ConfigProperty cp : aoi.getProperties().getProperty())
               {
                  aoiProps.put(cp.getName(), cp.getValue());
               }
            }
            else
               aoiProps = aogProps;
            
            lcf.buildAdminObejcts(aog.getAdminObjectClass(), "java:jboss/" + aoi.getJndiName(), "FIXME", 
                  aoiProps, true, true);
         }
      }
   }
   
   private void transformSecurity(LegacyConnectionFactoryImp lcf, WeblogicConnector ra) throws Exception
   {
      
   }
}
