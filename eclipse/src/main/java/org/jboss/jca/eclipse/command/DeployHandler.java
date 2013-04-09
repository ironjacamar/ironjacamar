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
package org.jboss.jca.eclipse.command;

import org.jboss.jca.eclipse.Activator;
import org.jboss.jca.eclipse.command.raui.ConnectorHelper;
import org.jboss.jca.eclipse.command.raui.RAGenerateWizard;

import com.github.fungal.spi.deployers.DeployException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * <code>DeployHandler</code> will deploy the generated RAR file to <strong>IronJacamar</strong> server.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class DeployHandler extends AbstractIronJacamarHandler
{
   
   private static final String PARAM_NAME = "ironjacamar-eclipse.params.deployment";
   
   private static final String PARAM_DEPLOY_RAR = "ironjacamar-eclipse.params.deploy-rar";
   
   private static final String PARAM_DEPLOY_RAXML = "ironjacamar-eclipse.params.deploy-ra.xml";
   
   private String deployment;
   
   /**
    * The Constructor
    */
   public DeployHandler()
   {  
   }

   /**
    * the command has been executed, so extract the needed information
    * from the application context.
    * 
    * @param event ExecutionEvent
    * @return Object null
    * @throws ExecutionException ExecutionException
    */
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException
   {
      this.deployment = event.getParameter(PARAM_NAME);
      setBaseEnabled(false);
      
      ISelection selection = HandlerUtil.getCurrentSelection(event);
      
      // check current selected project
      final IProject project = getSelectedProject(selection);
      if (project == null)
      {
         setBaseEnabled(true);
         throw new ExecutionException("There is no IronJacamar project selected.");
      }
      
      // lookup generated rar file
      IFile rarFile = lookupRarFile(project);
      
      // rar is not generated, build it first
      if (rarFile == null || !rarFile.exists())
      {
         try
         {
            buildRar(project);
         }
         catch (ExecutionException e)
         {
            setBaseEnabled(true);
            IStatus errStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can not build project", e.getCause());
            StatusManager.getManager().handle(errStatus, StatusManager.SHOW);
            throw e;
         }
      }
      else
      {
         startDeployment(rarFile, project);
      }
      return null;
   }
   
   private void startDeployment(IFile rarFile, IProject project)
   {
      if (PARAM_DEPLOY_RAR.equals(deployment))
      {
         DeployJob deployJob = new DeployJob(rarFile);
         deployJob.schedule();
      }
      else if (PARAM_DEPLOY_RAXML.equals(deployment))
      {
         openRAgenerationWizard(getActiveShell(), rarFile, project);
      }
      else
      {
         throw new IllegalStateException("Unkown parameter: " + deployment);
      }
   }
   
   /**
    * Opens the wizard dialog to generate the -ra.xml
    * 
    * @param shell the Shell
    * @param rarFile the RAR file
    * @param project the Project
    */
   private void openRAgenerationWizard(final Shell shell, final IFile rarFile, final IProject project)
   {
      // Parse Connector from RAR file, then open the wizard dialog.
      Job parseConnectorJob = new Job("Paring connector from " + rarFile.getFullPath())
      {

         @Override
         protected IStatus run(IProgressMonitor monitor)
         {
            try
            {
               final ConnectorHelper connectorHelper = new ConnectorHelper();
               connectorHelper.parseConnectorData(rarFile);
               Display.getDefault().asyncExec(new Runnable()
               {

                  @Override
                  public void run()
                  {
                     WizardDialog wizard = new WizardDialog(shell,
                           new RAGenerateWizard(rarFile, connectorHelper, project));
                     wizard.open();
                  }
               });
            }
            catch (Exception e)
            {
               return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can not parse Connector information", e);
            }
            finally
            {
               enableHandler();
            }
            return Status.OK_STATUS;
         }
      };
      parseConnectorJob.schedule();
   }
   
   /**
    * <code>RemoteDeployJob</code> will transfer the RAR file to remote <strong>IronJacamar</strong> server.
    * 
    * It assumes that the RAR has been generated already.
    */
   private class DeployJob extends Job
   {
      private final IFile rarFile;
      
      private DeployJob(IFile file)
      {
         super("Deplying " + file.getFullPath() + " to IronJacamar server");
         this.rarFile = file;
      }
      
      @Override
      protected IStatus run(final IProgressMonitor monitor)
      {
         monitor.beginTask(getName(), 1);
         IronJacamarDeployHelper deployHelper = new IronJacamarDeployHelper();
         String host = deployHelper.getRemoteIronJacamarHost();
         int port = deployHelper.getRemoteIronJacamarPort();
         try
         {
            if (deployHelper.deploy(rarFile, monitor))
            {
               String msg = "Deploy " + rarFile.getFullPath() + " to server " + host + ":" + port + 
                     " successfully!";
               logMessageToConsole(msg);
               return new Status(IStatus.INFO, Activator.PLUGIN_ID, msg);
            }
            
            String msg = "Can not deploy " + rarFile.getFullPath() + " to server " + host + ":" + port;
            logMessageToConsole(msg, new Color(null, 255, 0, 0));
            return new Status(IStatus.WARNING, Activator.PLUGIN_ID, msg);
            
         }
         catch (Throwable t)
         {
            if (t instanceof DeployException)
            {
               t = t.getCause();
            }
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
                  "Can not deploy " + rarFile.getFullPath() + " to server", t);
         }
         finally
         {
            monitor.worked(1);
            enableHandler();
         }
      }
   }

   @Override
   protected void onBuildFinished(IProject project)
   {
      try
      {
         project.refreshLocal(IResource.DEPTH_INFINITE, null);
         IFile rarFile = lookupRarFile(project);
         if (rarFile != null && rarFile.exists())
         {
            startDeployment(rarFile, project);
         }
         else
         {
            enableHandler();
         }
      }
      catch (CoreException e)
      {
         e.printStackTrace();
      }
   }
}
