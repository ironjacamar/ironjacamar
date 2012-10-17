/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.eclipse.command.raui;

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.MetadataFactory;
import org.jboss.jca.common.metadata.merge.Merger;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScannerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.fungal.api.util.FileUtil;
import com.github.fungal.api.util.JarFilter;

import org.eclipse.core.resources.IFile;

/**
 * ConnectorHelper is used to parse Connector data from a RAR file.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class ConnectorHelper
{
   private Connector cmd;

   private IronJacamar ijmd;
   
   /**
    * The constructor
    */
   public ConnectorHelper()
   {
      super();
   }
   
   /**
    * Parse ConnectorHolder from the RAR file.
    * 
    * @param rarFile the RAR file
    * @throws Exception the exception
    */
   public void parseConnectorData(IFile rarFile) throws Exception
   {
      FileUtil fileUtil = new FileUtil();
      File root = fileUtil.extract(rarFile.getLocation().toFile(), new File(System.getProperty("java.io.tmpdir")));
      try
      {
         MetadataFactory metadataFactory = new MetadataFactory();
         cmd = metadataFactory.getStandardMetaData(root);
         ijmd = metadataFactory.getIronJacamarMetaData(root);

         URLClassLoader cl = new URLClassLoader(getURLs(root), Thread.currentThread().getContextClassLoader());
         Annotations annotator = new Annotations();
         AnnotationScanner scanner = AnnotationScannerFactory.getAnnotationScanner();
         AnnotationRepository repository = scanner.scan(cl.getURLs(), cl);

         cmd = annotator.merge(cmd, repository, cl);
         cmd.validate();
         cmd = (new Merger()).mergeConnectorWithCommonIronJacamar(ijmd, cmd);
      }
      finally
      {
         fileUtil.delete(root);
      }
   }
   
   /**
    * Gets all jar files URL array.
    * This is used to make the ClassLoader to parse connector from the RAR file.
    * 
    * @param directory directory which jar files are located.
    * @return URL array
    * @throws Exception the exception
    */
   private URL[] getURLs(File directory) throws Exception
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         if (jars != null)
         {
            for (int j = 0; j < jars.length; j++)
            {
               list.add(jars[j].getCanonicalFile().toURI().toURL());
            }
         }
      }
      return list.toArray(new URL[list.size()]);
   }

   private void checkValidation()
   {
      if (this.cmd == null)
      {
         throw new IllegalStateException
         ("No Connector is parsed, please call parseConnectorData(IFile rarFile) first!");
      }
   }
   
   /**
    * Gets the ResourceAdapter of the Connector.
    * 
    * @return the ResourceAdapter
    */
   public ResourceAdapter getResourceAdapter()
   {
      checkValidation();
      return this.cmd.getResourceadapter();
   }
   
   /**
    * Gets TransactionSupportEnum from the Connector.
    * 
    * @return the TransactionSupportEnum
    */
   public TransactionSupportEnum getTransactionSupportEnum()
   {
      checkValidation();
      ResourceAdapter ra = this.cmd.getResourceadapter();
      if (ra instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) ra;
         return ra1516.getOutboundResourceadapter() == null ? TransactionSupportEnum.NoTransaction : ra1516
               .getOutboundResourceadapter().getTransactionSupport();
      }
      else if (ra instanceof ResourceAdapter10)
      {
         ResourceAdapter10 ra10 = (ResourceAdapter10) ra;
         return ra10.getTransactionSupport();
      }
      return null;
   }
   
   /**
    * Gets the bootstrap context
    * 
    * @return the bootstrap context or null
    */
   public String getBootStrapContext()
   {
      checkValidation();
      if (this.ijmd != null)
      {
         return this.ijmd.getBootstrapContext();
      }
      return null;
   }
   
   /**
    * Gets Bean validation groups
    * 
    * @return the bean validation groups or null
    */
   public List<String> getBeanValidationGrp()
   {
      checkValidation();
      if (this.ijmd != null)
      {
         return this.ijmd.getBeanValidationGroups();
      }
      return null;
   }
   
   /**
    * Gets ConfigPropType list of the ResourceAdapter.
    * 
    * @return the ConfigPropType list
    */
   public List<ConfigPropType> getConfigProperties()
   {
      checkValidation();
      List<ConfigPropType> result = new ArrayList<ConfigPropType>();
      List<? extends ConfigProperty> configProperties = null;
      ResourceAdapter ra = this.cmd.getResourceadapter();
      if (ra instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) ra;
         configProperties = ra1516.getConfigProperties();
      }
      else if (ra instanceof ResourceAdapter10)
      {
         ResourceAdapter10 ra10 = (ResourceAdapter10) ra;
         configProperties = ra10.getConfigProperties();
      }
      if (configProperties != null)
      {
         for (ConfigProperty configProp : configProperties)
         {
            String name = configProp.getConfigPropertyName().getValue();
            String type = configProp.getConfigPropertyType().getValue();
            String value = configProp.getConfigPropertyValue().getValue();
            ConfigPropType configPropType = new ConfigPropType(name, type, value, false);
            result.add(configPropType);
         }
      }
      return result;
   }
   
   /**
    * Gets CommonConnDef according to the Managed Connection Factory class name.
    * 
    * @param mcfClsName the Managed Connection Factory class name
    * @return the CommonConnDef or null
    */
   public CommonConnDef getCommonConnDef(String mcfClsName)
   {
      checkValidation();
      if (null != this.ijmd)
      {
         for (CommonConnDef connDef : this.ijmd.getConnectionDefinitions())
         {
            if (mcfClsName.equals(connDef.getClassName()))
            {
               return connDef;
            }
         }
      }
      return null;
   }

   /**
    * Gets List<ConfigPropType> from the List<? extends ConfigProperty>
    * 
    * @param configProperties the List<? extends ConfigProperty>.
    * @return the ConfigPropType list.
    */
   public List<ConfigPropType> getConfigProps(List<? extends ConfigProperty> configProperties)
   {
      List<ConfigPropType> result = new ArrayList<ConfigPropType>();
      if (configProperties != null)
      {
         for (ConfigProperty configProp : configProperties)
         {
            String name = configProp.getConfigPropertyName().getValue();
            String type = configProp.getConfigPropertyType().getValue();
            String value = configProp.getConfigPropertyValue().getValue();
            ConfigPropType configPropType = new ConfigPropType(name, type, value, false);
            result.add(configPropType);
         }
      }
      return result;
   }
   
   /**
    * Gets CommonAdminObject.
    * 
    * @param ao the AdminObject
    * @return the CommonAdminObject
    */
   public CommonAdminObject getCommonAdminObject(AdminObject ao)
   {
      checkValidation();
      if (this.ijmd != null)
      {
         for (CommonAdminObject commonAO : this.ijmd.getAdminObjects())
         {
            if (commonAO.getClassName().equals(ao.getAdminobjectClass().getValue()))
            {
               return commonAO;
            }
         }
      }
      return null;
   }
   
}
