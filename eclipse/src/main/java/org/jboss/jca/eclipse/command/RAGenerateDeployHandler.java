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
package org.jboss.jca.eclipse.command;

import org.jboss.jca.eclipse.Activator;
import org.jboss.jca.eclipse.command.raui.ConnectorHelper;
import org.jboss.jca.eclipse.command.raui.RAGenerateWizard;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * This generates *-ra.xml according to selected RAR file and some input values in the wizard dialog,
 * then deploy the *-ra.xml to IronJacamar server.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAGenerateDeployHandler extends AbstractIronJacamarHandler
{

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException
   {
      ISelection selection = HandlerUtil.getCurrentSelection(event);

      // check current selected project
      final IProject project = getSelectedProject(selection);
      if (project == null)
      {
         setBaseEnabled(true);
         throw new ExecutionException("There is no IronJacamar project selected.");
      }

      final IFile rarFile = lookupRarFile(project);
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
         openRAgenerationWizard(HandlerUtil.getActiveShell(event), rarFile, project);
      }
      return null;
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
            return Status.OK_STATUS;
         }
      };
      parseConnectorJob.schedule();
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
            openRAgenerationWizard(getActiveShell(), rarFile, project);
         }
      }
      catch (CoreException e)
      {
         e.printStackTrace();
      }
   }
   
   private Shell getActiveShell()
   {
      IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if (null != activeWindow)
      {
         return activeWindow.getShell();
      }
      return null;
   }

}
