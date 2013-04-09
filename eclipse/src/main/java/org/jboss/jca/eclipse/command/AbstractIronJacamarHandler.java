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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class provides common methods used in each Handler implementation.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public abstract class AbstractIronJacamarHandler extends AbstractHandler
{

   private static final String IRONJACAMAR_CONSOLE_NAME = "IronJacamar";
   
   /**
    * Gets current selected <code>IProject</code>.
    * 
    * @param selection current selection
    * @return <code>IProject</code> which current selection belongs to, null when no selection.
    */
   IProject getSelectedProject (ISelection selection)
   {
      if (!(selection instanceof IStructuredSelection))
      {
         return null;
      }
      IStructuredSelection structuredSelection = (IStructuredSelection)selection;
      Object element = structuredSelection.getFirstElement();
      IResource res = null;
      if (element instanceof IResource)
      {
         res = (IResource)element;
      }
      else if (element instanceof IAdaptable)
      {
         res = getIResource((IAdaptable)element);
      }
      if (res != null)
      {
         return res.getProject();
      }
      return null;
   }
   
   /**
    * Gets <code>IResource</code> from the <code>IAdaptable</code>.
    * 
    * @param adapter the <code>IAdaptable</code>
    * @return <code>IResource</code> associated with the adapter or null
    */
   private IResource getIResource (final IAdaptable adapter)
   {
      Object obj = adapter.getAdapter(IResource.class);
      if (obj != null)
      {
         return (IResource)obj;
      }
      else
      {
         obj = adapter.getAdapter(IWorkbenchAdapter.class);
         IWorkbenchAdapter workBenchAdapter = (IWorkbenchAdapter)obj;
         if (null != workBenchAdapter)
         {
            Object parent = workBenchAdapter.getParent(adapter);
            if (parent instanceof IAdaptable)
            {
               return getIResource((IAdaptable)parent);
            }
         }
         return null;
      }
   }
   
   /**
    * Looks up RAR file from project root directory.
    * 
    * @param project the <code>IProject</code>
    * @return the first RAR file looked up from the directory or null if not found
    */
   IFile lookupRarFile (IProject project)
   {
      String projectRoot = project.getLocation().toOSString();
      File targetDir = new File(projectRoot + File.separator + "target");
      if (!targetDir.exists() || !targetDir.isDirectory())
      {
         return null;
      }
      File rarFiles[] = targetDir.listFiles(new FilenameFilter()
      {
         
         @Override
         public boolean accept(File dir, String name)
         {
            return name.endsWith(".rar");
         }
      });
      if (rarFiles != null && rarFiles.length > 0)
      {
         return project.getFile("target" + File.separator + rarFiles[0].getName());
      }
      return null;
   }
   
   /**
    * Launches building for IronJacamar Project.
    * 
    * If it is <code>Ant</code> build, it will launch an <code>AntLauncher</code>, if it is <code>Maven</code> build, 
    * it will launch a MavenLauncher.
    * 
    * @param ironjacamarProject the <strong>IronJacamar</strong> Project
    * @return an <code>ILaunchConfigurationWorkingCopy</code> instance  
    * @throws CoreException thrown on any exceptions
    */
   private ILaunchConfigurationWorkingCopy launchBuild(IProject ironjacamarProject) throws CoreException
   {
      IFile buildXMLFile = ironjacamarProject.getFile("build.xml");
      IFile pomFile = ironjacamarProject.getFile("pom.xml");
      ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
      ILaunchConfigurationWorkingCopy wc = null;
      if (buildXMLFile.exists())
      {
         ILaunchConfigurationType launchConfigType = launchManager.
               getLaunchConfigurationType("org.eclipse.ant.AntLaunchConfigurationType");
         wc = launchConfigType.newInstance(null, IRONJACAMAR_CONSOLE_NAME);
         wc.setAttribute("org.eclipse.ui.externaltools.ATTR_LOCATION", buildXMLFile.getLocation().toOSString());
      }
      else if (pomFile.exists())
      {
         ILaunchConfigurationType launchConfigType = launchManager.
               getLaunchConfigurationType("org.eclipse.m2e.Maven2LaunchConfigurationType");
         if (launchConfigType == null)
         {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
                  "No m2e plugin found, please install m2e plugin."));
         }
         wc = launchConfigType.newInstance(null, IRONJACAMAR_CONSOLE_NAME);
         wc.setAttribute("M2_GOALS", "package");
         wc.setAttribute("M2_SKIP_TESTS", true);  // skip test for now!
         wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
               ironjacamarProject.getLocation().toOSString());
         
      }
      else
      {
         throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
               "Either build.xml nor pom.xml is found in the project root."));
      }
      wc.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
      wc.setAttribute(IProcess.ATTR_PROCESS_LABEL, IRONJACAMAR_CONSOLE_NAME);
      return wc;
   }
   
   /**
    * Builds RAR file of specified <strong>IronJacamar</strong> Project.
    * After the building is finished. it will invoke <code>onBuildFinished</code> method.
    * 
    * @param project the <strong>IronJacamar</strong> Project
    * @throws ExecutionException any exception
    */
   void buildRar (IProject project) throws ExecutionException
   {
      try
      {
         ILaunchConfigurationWorkingCopy wc = launchBuild(project);
         BuildingLaunchListener buildLaunchListener = new BuildingLaunchListener(project);
         ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
         launchManager.addLaunchListener(buildLaunchListener);
         buildLaunchListener.buildLaunch = wc.launch(ILaunchManager.RUN_MODE, null);
         logMessageToConsole("Start to build RAR file for project: " + project.getFullPath());
      }
      catch (CoreException e)
      {
         throw new ExecutionException("Can not launch the build", e);
      }
   }
   
   /**
    * <code>BuildingLaunchListener</code> will listen on the terminated event of specified 
    * <code>ILaunch</code> instance.
    * 
    * When the <code>ILaunch</code> terminates, it will call <code>onBuildFinished</code> method. 
    * The subclasses should implement this method.
    * 
    * It does not make sure that the build success or fail. 
    * subclasses need to take consideration of the building result.
    *
    */
   private class BuildingLaunchListener implements ILaunchesListener2
   {
      
      private ILaunch buildLaunch = null;
      
      private final IProject project;
      
      BuildingLaunchListener (IProject prj)
      {
         super();
         this.project = prj;
      }

      @Override
      public void launchesRemoved(ILaunch[] launches)
      {
         ;
      }

      @Override
      public void launchesAdded(ILaunch[] launches)
      {
         ;
      }

      @Override
      public void launchesChanged(ILaunch[] launches)
      {
         ;
      }

      @Override
      public void launchesTerminated(ILaunch[] launches)
      {
         for (ILaunch launch: launches)
         {
            if (launch.equals(buildLaunch))
            {
               try
               {
                  onBuildFinished(project);
               }
               finally
               {
                  ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
                  launchManager.removeLaunchListener(this);
               }
               return;
            }
         }
      }
      
   }
   
   /**
    * Gets an <code>IOConsole</code> used for logging <strong>IronJacamar</strong> project message.
    * 
    * @return a non-null <code>IOConsole</code>
    */
   private IOConsole getIronJacamarConsole ()
   {
      IOConsole buildingProcessConsole = null;
      IOConsole ironJacamarConsole = null;
      for (IConsole console: ConsolePlugin.getDefault().getConsoleManager().getConsoles())
      {
         if (console instanceof IOConsole)
         {
            String name = console.getName();
            if (name.indexOf(IRONJACAMAR_CONSOLE_NAME) != -1 && !name.equals(IRONJACAMAR_CONSOLE_NAME))
            {
               buildingProcessConsole = (IOConsole)console;
            }
            else if (name.equals(IRONJACAMAR_CONSOLE_NAME))
            {
               ironJacamarConsole = (IOConsole)console;
            }
         }
      }
      if (ironJacamarConsole == null)
      {
         ironJacamarConsole = new MessageConsole(IRONJACAMAR_CONSOLE_NAME, null);
         ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ironJacamarConsole});
      }
      
      if (buildingProcessConsole != null)
      {
         return buildingProcessConsole;
      }
      return ironJacamarConsole;
   }
   
   /**
    * Logs message to <strong>IronJacamar</strong> console using font color: <b>blue</b>.
    * 
    * @param msg message to log
    */
   protected void logMessageToConsole (String msg)
   {
      logMessageToConsole(msg, new Color(null, 0, 0, 255));
   }
   
   /**
    * Logs message to <strong>IronJacamar</strong> console.
    * 
    * @param msg the message to log
    * @param fontColor the font Color in the console.
    */
   protected void logMessageToConsole (String msg, final Color fontColor)
   {

      IOConsole console = getIronJacamarConsole();
      final IOConsoleOutputStream output = console.newOutputStream();
      try
      {
         Display.getDefault().syncExec(new Runnable()
         {
            
            @Override
            public void run()
            {
               output.setColor(fontColor);
            }
         });
         output.write("\n" + msg + "\n");
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            output.close();
         }
         catch (IOException e)
         {
            ;
         }
      }
   }
   
   /**
    * Enable the command handler
    */
   protected void enableHandler ()
   {
      Display.getDefault().asyncExec(new Runnable()
      {
         
         @Override
         public void run()
         {
            setBaseEnabled(true);
         }
      });
   }
   
   /**
    * Gets active shell.
    * 
    * @return the Shell
    */
   protected Shell getActiveShell()
   {
      IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if (null != activeWindow)
      {
         return activeWindow.getShell();
      }
      return null;
   }
   
   /**
    * Things to do after the building is finished. No matter succeeds or fails.
    * 
    * This is called in background thread, any GUI thread access should not be called directly.
    * 
    * @param project the <strong>IronJacamar</strong> Project
    */
   protected abstract void onBuildFinished (IProject project);
   
   
}
