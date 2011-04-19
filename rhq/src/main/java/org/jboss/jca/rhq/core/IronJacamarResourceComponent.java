/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.rhq.core;

import org.jboss.jca.rhq.util.ContainerHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.content.PackageDetailsKey;
import org.rhq.core.domain.content.transfer.ResourcePackageDetails;
import org.rhq.core.domain.resource.CreateResourceStatus;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.content.ContentContext;
import org.rhq.core.pluginapi.content.ContentServices;
import org.rhq.core.pluginapi.inventory.CreateChildResourceFacet;
import org.rhq.core.pluginapi.inventory.CreateResourceReport;

/**
 * A IronJacamarResourceComponent
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class IronJacamarResourceComponent extends AbstractResourceComponent implements CreateChildResourceFacet
{

   /**
    * loadResourceConfiguration
    * 
    * @return Configuration the configuration
    * @throws Exception exception
    */
   @Override
   public Configuration loadResourceConfiguration() throws Exception
   {
      return new Configuration();
   }

   /**
    * createResource
    * 
    * @param report the CreateResourceReport
    * @return CreateResourceReport the report
    */
   @Override
   public CreateResourceReport createResource(CreateResourceReport report)
   {
      ResourceType resType = report.getResourceType();
      String resName = resType.getName();
      ResourcePackageDetails pkgDetail = report.getPackageDetails();
      ContentContext contentContext = getResourceContext().getContentContext();
      ContentServices contentServices = contentContext.getContentServices();
      String tmpDir = System.getProperty("java.io.tmpdir");
      String fileName = pkgDetail.getFileName();
      if (!fileName.toLowerCase().endsWith(".rar"))
      {
         report.setErrorMessage(fileName + " is not a valid RAR file.");
         report.setStatus(CreateResourceStatus.FAILURE);
         return report;
      }
      File outFile = new File(tmpDir, pkgDetail.getFileName()); // change to plugin configuration ??
      OutputStream output;
      try
      {
         output = new FileOutputStream(outFile);
         PackageDetailsKey key = pkgDetail.getKey();
         long bits = contentServices.downloadPackageBitsForChildResource(contentContext, resName, key, output);
         if (bits < 0)
         {
            report.setErrorMessage("Can not download package content.");
            report.setStatus(CreateResourceStatus.FAILURE);
            return report;
         }
         Deploy deployer = (Deploy)ContainerHelper.getEmbeddedDiscover();
         deployer.deploy(outFile.toURI().toURL());
         
         String resKey = outFile.getName();
         
         // set resource key
         report.setResourceKey(resKey);
         
         // set resource name
         report.setResourceName(resKey);
         
         report.setStatus(CreateResourceStatus.SUCCESS);
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         report.setStatus(CreateResourceStatus.FAILURE);
         report.setErrorMessage(e.getMessage());
         report.setException(e);
      }
      return report;
   }
}
