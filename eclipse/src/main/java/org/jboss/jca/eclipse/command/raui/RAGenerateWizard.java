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

import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.ra10.ResourceAdapter10;
import org.jboss.jca.common.metadata.ra.common.ConnectionDefinitionImpl;
import org.jboss.jca.eclipse.Activator;
import org.jboss.jca.eclipse.command.IronJacamarDeployHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;


/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAGenerateWizard extends Wizard
{
   private final IFile rarFile;

   private final IProject project;

   private final String archive;
   
   private final ConnectorHelper connectorHelper;

   private final ResourceAdapterConfig raConfig;

   private boolean deployRAR = false;

   /**
    * The constructor.
    * 
    * @param rarFile the rar file
    * @param connectorHelper the ConnectorHelper
    * @param project the IronJacamar project
    */
   public RAGenerateWizard(IFile rarFile, ConnectorHelper connectorHelper, IProject project)
   {
      super();
      this.rarFile = rarFile;
      this.project = project;
      this.connectorHelper = connectorHelper;
      this.archive = rarFile.getName();
      this.raConfig = new ResourceAdapterConfig();
      this.raConfig.setArchive(archive);
   }

   @Override
   public void addPages()
   {
      // general information setting
      addPage(new RAGenerateBasicPage(this));

      List<ConnectionDefinition> mcfs = null;
      List<AdminObject> aos = null;
      ResourceAdapter ra = connectorHelper.getResourceAdapter();
      if (ra instanceof ResourceAdapter1516)
      {
         ResourceAdapter1516 ra1516 = (ResourceAdapter1516) ra;
         if (null != ra1516.getOutboundResourceadapter())
         {
            mcfs = ra1516.getOutboundResourceadapter().getConnectionDefinitions();
         }
         aos = ra1516.getAdminObjects();
      }
      else if (ra instanceof ResourceAdapter10)
      {
         ResourceAdapter10 ra10 = (ResourceAdapter10) ra;
         mcfs = new ArrayList<ConnectionDefinition>();
         mcfs.add(new ConnectionDefinitionImpl(ra10.getManagedConnectionFactoryClass(), ra10.getConfigProperties(),
               ra10.getConnectionFactoryInterface(), ra10.getConnectionFactoryImplClass(), ra10
                     .getConnectionInterface(), ra10.getConnectionImplClass(), ra10.getId()));
      }

      // MCF pages
      if (mcfs != null)
      {
         for (ConnectionDefinition connDef : mcfs)
         {
            addPage(new RAGenerateMCFPage(this, connDef));
         }
      }
      // AO pages
      if (aos != null)
      {
         for (AdminObject ao : aos)
         {
            addPage(new RAGenerateAOPage(this, ao));
         }
      }
      
      // MiscellaneousPage
      addPage(new MiscellaneousPage(this));
   }

   @Override
   public boolean performFinish()
   {
      
      File file = rarFile.getLocation().toFile();

      // *-ra.xml is generated at the same location as the rar file.
      final String raXMLFileName = getRAXMLFileName();
      final File raXMLFile = new File(file.getParentFile(), raXMLFileName);
      Job generateRAXMLJob = new Job("Generating " + raXMLFileName)
      {

         @Override
         protected IStatus run(IProgressMonitor monitor)
         {
            try
            {
               monitor.subTask("Generating " + raXMLFileName);
               RAXMLGenerator raXMLGenerator = new RAXMLGenerator();
               raXMLGenerator.generateRAXML(raConfig, raXMLFile);
               
               // refresh project to make the *-ra.xml visible.
               project.refreshLocal(IResource.DEPTH_INFINITE, null);
               IronJacamarDeployHelper deployHelper = new IronJacamarDeployHelper();
               if (deployRAR)
               {
                  deployHelper.deploy(rarFile, monitor);
               }
               // deploy the *-ra.xml
               IFile raXMLFile = project.getFile("target" + File.separator + raXMLFileName);
               deployHelper.deploy(raXMLFile, monitor);
            }
            catch (Throwable t)
            {
               return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error on generating " + raXMLFileName, t);
            }
            finally
            {
               monitor.worked(1);
            }
            return Status.OK_STATUS;
         }
      };
      generateRAXMLJob.schedule();
      return true;
   }
   
   /**
    * Gets the *-ra.xml file name.
    * 
    * @return *-ra.xml file name
    */
   public String getRAXMLFileName()
   {
      String rarFileName = rarFile.getName();
      final String raXMLFileName = rarFileName.substring(0, rarFileName.indexOf(".rar")) + "-ra.xml";
      return raXMLFileName;
   }
   
   /**
    * Get raConfig
    * @return The raConfig
    */
   public ResourceAdapterConfig getRaConfig()
   {
      return raConfig;
   }
   
   /**
    * Get deployRAR
    * @return The deployRAR
    */
   public boolean isDeployRAR()
   {
      return deployRAR;
   }

   /**
    * Set deployRAR
    * @param deployRAR The value to set
    */
   public void setDeployRAR(boolean deployRAR)
   {
      this.deployRAR = deployRAR;
   }

   /**
    * Get rarFile
    * @return The rarFile
    */
   public IFile getRarFile()
   {
      return rarFile;
   }

   /**
    * Get project
    * @return The project
    */
   public IProject getProject()
   {
      return project;
   }

   /**
    * Get archive
    * @return The archive
    */
   public String getArchive()
   {
      return archive;
   }

   /**
    * Get connectorHelper
    * @return The connectorHelper
    */
   public ConnectorHelper getConnectorHelper()
   {
      return connectorHelper;
   }
   
}
