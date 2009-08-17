/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.sjc.deployers.ra;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.logging.Logger;
//import org.jboss.metadata.rar.jboss.JBossRA10DefaultNSMetaData;
import org.jboss.metadata.rar.jboss.JBossRA10MetaData;
import org.jboss.metadata.rar.jboss.JBossRA20DefaultNSMetaData;
import org.jboss.metadata.rar.jboss.JBossRA20MetaData;
import org.jboss.metadata.rar.jboss.JBossRAMetaData;
import org.jboss.metadata.rar.jboss.RaConfigPropertyMetaData;
import org.jboss.metadata.rar.spec.ConfigPropertyMetaData;
import org.jboss.metadata.rar.spec.ConnectorMetaData;
import org.jboss.metadata.rar.spec.JCA15DTDMetaData;
import org.jboss.metadata.rar.spec.JCA15MetaData;
import org.jboss.metadata.rar.spec.JCA16DTDMetaData;
import org.jboss.metadata.rar.spec.JCA16DefaultNSMetaData;
import org.jboss.metadata.rar.spec.JCA16MetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;

/**
 * The metadata processor for JCA 1.x
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@redhat.com">Jeff Zhang</a>
 */
public class Metadata
{
   private static Logger log = Logger.getLogger(Metadata.class);
   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   private Metadata()
   {
   }

   /**
    * Get the JCA standard metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public static ConnectorMetaData getStandardMetaData(File root) throws Exception
   {
      ConnectorMetaData result = null;

      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();

      MutableSchemaResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      resolver.mapLocationToClass("connector_1_6.xsd", JCA16MetaData.class);
      resolver.mapLocationToClass("connector_1_5.xsd", JCA15MetaData.class);
      resolver.mapLocationToClass("connector_1_5.dtd", JCA15DTDMetaData.class);
      resolver.mapLocationToClass("connector_1_6.dtd", JCA16DTDMetaData.class);
      resolver.mapLocationToClass("connector", JCA16DefaultNSMetaData.class);

      File metadataFile = new File(root, "/META-INF/ra.xml");

      if (metadataFile.exists())
      {
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();

            result = (ConnectorMetaData)unmarshaller.unmarshal(url, resolver);
            
            log.debug("Total parse for " + url + " took " + (System.currentTimeMillis() - start) + "ms");

            if (trace)
            {
               log.trace("successful parse " + result.getVersion() + " rar package " + result);
            }
               
         }
         catch (Exception e)
         {
            log.error("Error during parsing: " + url, e);
            throw e;
         }
      }
      
      return result;
   }

   /**
    * Get the JBoss specific metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public static JBossRAMetaData getJBossMetaData(File root) throws Exception
   {
      JBossRAMetaData result = null;

      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();

      MutableSchemaResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();
      resolver.mapLocationToClass("http://www.jboss.org/schema/jboss-ra_1_0.xsd", JBossRA10MetaData.class);
      resolver.mapLocationToClass("http://www.jboss.org/schema/jboss-ra_2_0.xsd", JBossRA20MetaData.class);
      resolver.mapLocationToClass("jboss-ra", JBossRA20DefaultNSMetaData.class);

      File metadataFile = new File(root, "/META-INF/jboss-ra.xml");

      if (metadataFile.exists())
      {
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();

            result = (JBossRAMetaData)unmarshaller.unmarshal(url, resolver);
            
            log.debug("Total parse for " + url + " took " + (System.currentTimeMillis() - start) + "ms");

            if (trace)
               log.trace(result);
         }
         catch (Exception e)
         {
            log.error("Error during parsing: " + url, e);
            throw e;
         }
      }
      
      return result;
   }

   /**
    * Merge specification metadata with vendor metadata
    * @param cmd The specification metadata
    * @param jmd The vendor metadata
    * @return The merged metadata
    * @exception Exception Thrown if an error occurs
    */
   public static ConnectorMetaData merge(ConnectorMetaData cmd, JBossRAMetaData jmd) throws Exception
   {
      if (cmd != null && jmd != null)
      {
         /*
         <xs:restriction base="javaee:string">
         <xs:enumeration value="connection-definition"/>
         <xs:enumeration value="resourceadapter"/>
         <xs:enumeration value="activationspec"/>
         <xs:enumeration value="adminobject"/>
         </xs:restriction>
         */
         
         List<RaConfigPropertyMetaData> props = jmd.getRaConfigProps();

         List<ConfigPropertyMetaData> append = null;

         if (props != null)
         {
            for (RaConfigPropertyMetaData rcmd : props)
            {
               List<ConfigPropertyMetaData> listConfigProp = null;
               String override = rcmd.getOverride();
               if (override == null || override.equals("resourceadapter"))
               {
                  if (cmd.getRa() != null)
                  {
                     listConfigProp = cmd.getRa().getConfigProperty();
                  }
               }
               else if (override.equals("connection-definition"))
               {
                  if (cmd.getRa() != null &&
                     cmd.getRa().getOutboundRa() != null &&
                     cmd.getRa().getOutboundRa().getConDefs() != null &&
                     cmd.getRa().getOutboundRa().getConDefs().size() > 0 &&
                     cmd.getRa().getOutboundRa().getConDefs().get(0) != null)
                  {
                     listConfigProp = cmd.getRa().getOutboundRa().getConDefs().get(0).getConfigProps();
                  }
               } 
               else if (override.equals("activationspec"))
               {
                  if (cmd.getRa() != null &&
                     cmd.getRa().getInboundRa() != null &&
                     cmd.getRa().getInboundRa().getMessageAdapter() != null &&
                     cmd.getRa().getInboundRa().getMessageAdapter().getMessageListeners() != null &&
                     cmd.getRa().getInboundRa().getMessageAdapter().getMessageListeners().size() > 0 &&
                     cmd.getRa().getInboundRa().getMessageAdapter().getMessageListeners().get(0) != null &&
                     cmd.getRa().getInboundRa().getMessageAdapter().getMessageListeners().get(0).
                        getActivationSpecType() != null)
                  {
                     listConfigProp = cmd.getRa().getInboundRa().getMessageAdapter().getMessageListeners().
                        get(0).getActivationSpecType().getConfigProps();
                  }
               }
               else if (override.equals("adminobject"))
               {
                  if (cmd.getRa() != null &&
                     cmd.getRa().getAdminObjects() != null &&
                     cmd.getRa().getAdminObjects().size() > 0 &&
                     cmd.getRa().getAdminObjects().get(0) != null)
                  {
                     listConfigProp = cmd.getRa().getAdminObjects().get(0).getConfigProps();
                  }
               }
               
               boolean found = false;

               if (listConfigProp != null)
               {
                  Iterator<ConfigPropertyMetaData> it = listConfigProp.iterator();
                  
                  while (!found && it.hasNext())
                  {
                     ConfigPropertyMetaData cpmd = it.next();
                     if (cpmd.getName().equals(rcmd.getName()) &&
                         cpmd.getType().equals(rcmd.getType()))
                     {
                        cpmd.setValue(rcmd.getValue());
                        found = true;
                     }
                  }
               }

               if (!found)
               {
                  if (append == null)
                     append = new ArrayList<ConfigPropertyMetaData>();

                  ConfigPropertyMetaData cpmd = new ConfigPropertyMetaData();
                  cpmd.setName(rcmd.getName());
                  cpmd.setType(rcmd.getType());
                  cpmd.setValue(rcmd.getValue());
                  
                  append.add(cpmd);
               }
            }

            if (append != null)
            {
               if (cmd.getRa().getConfigProperty() == null)
                  cmd.getRa().setConfigProperty(new ArrayList<ConfigPropertyMetaData>());

               for (ConfigPropertyMetaData cpmd : append)
               {
                  cmd.getRa().getConfigProperty().add(cpmd);
               }
            }
         }
      }

      return cmd;
   }
}
