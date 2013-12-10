/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

import org.jboss.jca.eclipse.Activator;
import org.jboss.jca.eclipse.command.IronJacamarDeployHelper;

import java.io.File;
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
   
   private final ResourceAdapterConfig raConfig;
   
   private final ResourceAdapterConfig initialConfig;

   private boolean deployRAR = false;

   /**
    * The constructor.
    * 
    * @param rarFile the rar file
    * @param project the IronJacamar project
    */
   public RAGenerateWizard(IFile rarFile, IProject project)
   {
      super();
      this.rarFile = rarFile;
      this.project = project;
      ResourceAdapterHelper raHelper = new ResourceAdapterHelper();
      try
      {
         this.raConfig = raHelper.parseResourceAdapterConfig(this.rarFile.getLocation().toFile());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Can't parse ResourceAdpater from file: " + this.rarFile.getLocation().
               toFile().getAbsolutePath(), e);
      }
      this.archive = rarFile.getName();
      this.initialConfig = this.raConfig.clone();
   }

   @Override
   public void addPages()
   {
      // resource adapter version select
      addPage(new RAVersionPage(this));
      
      // general information setting
      addPage(new RAGenerateBasicPage(this));

      List<ConnectionFactoryConfig> connConfigs = this.raConfig.getConnectionDefinitions();
      List<AdminObjectConfig> aoConfigs = this.raConfig.getAdminObjectConfigs();
      
      for (ConnectionFactoryConfig connConfig : connConfigs)
      {
         RAGenerateMCFPage mcfPage = new RAGenerateMCFPage(connConfig);
         this.raConfig.addVersionChangeListener(mcfPage);
         addPage(mcfPage);
      }
      
      for (AdminObjectConfig aoConfig : aoConfigs)
      {
         addPage(new RAGenerateAOPage(aoConfig));
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
               raXMLGenerator.generateRAXML(initialConfig, raConfig, raXMLFile);
               
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
   
}
