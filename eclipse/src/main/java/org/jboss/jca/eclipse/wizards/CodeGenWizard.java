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
package org.jboss.jca.eclipse.wizards;

import org.jboss.jca.codegenerator.BaseProfile;
import org.jboss.jca.codegenerator.Definition;
import org.jboss.jca.codegenerator.JCA10Profile;
import org.jboss.jca.codegenerator.JCA15Profile;
import org.jboss.jca.codegenerator.JCA16Profile;
import org.jboss.jca.eclipse.Activator;
import org.jboss.jca.eclipse.IronJacamarProjectNature;
import org.jboss.jca.eclipse.preferences.PreferenceConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import org.osgi.framework.Bundle;

/**
 * CodeGenWizard
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */

public class CodeGenWizard extends Wizard implements INewWizard
{
   private DefinitionWizardPage defPage;

   private RaWizardPage raPage;

   private McfWizardPage mcfPage;

   private InboundWizardPage inboundPage;

   private AoWizardPage aoPage;

   private BuildWizardPage buildPage;

   private ISelection selection;

   private Definition def;

   private String projectName;
   
   /** PropertyResourceBundle to load properties of code generation */
   private PropertyResourceBundle prb;

   /**
    * Constructor for CodeGenWizard.
    */
   public CodeGenWizard()
   {
      super();
      setNeedsProgressMonitor(true);
      def = new Definition();
      try
      {
         InputStream in = CodeGenWizard.class.getClassLoader().
            getResourceAsStream("codegenerator.properties");
         prb = new PropertyResourceBundle(in);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * getResourceString
    * @param key key string
    * @return resource
    */
   public String getResourceString(String key)
   {
      return prb.getString(key);
   }

   /**
    * Adding the page to the wizard.
    */

   public void addPages()
   {
      defPage = new DefinitionWizardPage(selection);
      addPage(defPage);
      raPage = new RaWizardPage(selection);
      addPage(raPage);

      mcfPage = new McfWizardPage(selection);
      addPage(mcfPage);

      inboundPage = new InboundWizardPage(selection);
      addPage(inboundPage);

      aoPage = new AoWizardPage(selection);
      addPage(aoPage);

      buildPage = new BuildWizardPage(selection);
      addPage(buildPage);
   }

   /**
    * get Definition
    * @return Definition
    */
   public Definition getDef()
   {
      return def;
   }

   /**
    * This method is called when 'Finish' button is pressed in
    * the wizard. We will create an operation and run it
    * using wizard as execution context.
    * 
    * @return boolean finished
    */
   public boolean performFinish()
   {

      IRunnableWithProgress op = new IRunnableWithProgress()
      {
         public void run(IProgressMonitor monitor) throws InvocationTargetException
         {
            try
            {
               doFinish(monitor);
            }
            catch (CoreException e)
            {
               throw new InvocationTargetException(e);
            }
            finally
            {
               monitor.done();
            }
         }
      };
      try
      {
         getContainer().run(true, false, op);
      }
      catch (InterruptedException e)
      {
         return false;
      }
      catch (InvocationTargetException e)
      {
         Throwable realException = e.getTargetException();
         MessageDialog.openError(getShell(), "Error", realException.getMessage());
         return false;
      }
      return true;
   }

   /**
    * The worker method. It will find the container, create the
    * file if missing or just replace its contents, and open
    * the editor on the newly created file.
    */

   private void doFinish(IProgressMonitor monitor) throws CoreException
   {

      monitor.beginTask("Initialize generator", 1);
      IProgressMonitor progressMonitor = new NullProgressMonitor();
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject project = root.getProject(getProjectName());
      project.create(progressMonitor);
      project.open(progressMonitor);

      IProjectDescription description = project.getDescription();
      String[] natures = description.getNatureIds();
      String[] newNatures = new String[natures.length + 2];
      System.arraycopy(natures, 0, newNatures, 0, natures.length);
      newNatures[natures.length] = JavaCore.NATURE_ID;
      newNatures[natures.length + 1] = IronJacamarProjectNature.NATURE_ID;
      description.setNatureIds(newNatures);
      project.setDescription(description, progressMonitor);

      String path = new File(project.getLocationURI()).toString();
      System.out.println(path);

      def.setOutputDir(path);

      BaseProfile profile;
      if (def.getVersion().equals("1.6"))
         profile = new JCA16Profile();
      else if (def.getVersion().equals("1.5"))
         profile = new JCA15Profile();
      else
         profile = new JCA10Profile();

      profile.generate(def);

      IJavaProject javaProject = JavaCore.create(project);

      IPreferenceStore store = Activator.getDefault().getPreferenceStore();
      String ijHome = store.getString(PreferenceConstants.JCA_HOME_PATH);
      
      List<String> ijJars = new ArrayList<String>();
      ijJars.add("/ironjacamar-spec-api.jar");
      String libDir;
      
      //if use ant to build, should copy all jars
      if (def.getBuild().equals("ant") && ijHome != null && !ijHome.equals(""))
      {
         libDir = ijHome + "/lib";

         File lib = new File(libDir);
         expandFilePath(lib, ijJars, "");
         ijJars.add("/../bin/ironjacamar-sjc.jar");
         
      }
      else
      {
         libDir = "libs";
      }

      Bundle bundle = Platform.getBundle("org.jboss.jca.eclipse");

      IFolder firstFolder = project.getFolder("lib");
      firstFolder.create(true, true, progressMonitor);

      for (String ijJarName : ijJars)
      {
         InputStream stream;
         try
         {
            if (def.getBuild().equals("ant") &&  ijHome != null && !ijHome.equals(""))
            {
               stream = FileLocator.resolve(new URL("file://" + libDir + ijJarName)).openStream();
            }
            else
            {
               Path srcPath = new Path(libDir + ijJarName);
               stream = FileLocator.openStream(bundle, srcPath, false);
            }

            IFile file = project.getFile("lib/" + ijJarName.substring(ijJarName.lastIndexOf("/")));
            if (!file.exists())
            {
               file.create(stream, true, null);
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

      Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
      entries.addAll(Arrays.asList(javaProject.getRawClasspath()));
      entries.remove(JavaCore.newSourceEntry(new Path("/" + getProjectName())));
      entries.add(JavaCore.newSourceEntry(new Path("/" + getProjectName() + "/src/main/java")));

      IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
      LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
      for (LibraryLocation element : locations)
      {
         entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
      }

      for (String ijJarName : ijJars)
      {
         entries.add(JavaCore.newLibraryEntry(new Path("/" + getProjectName() + "/lib/" + 
            ijJarName.substring(ijJarName.lastIndexOf("/"))), null, null));
      }

      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), progressMonitor);

      monitor.worked(1);

   }

   /**
    * @param ijJars
    * @param lib
    */
   private void expandFilePath(File dir, List<String> ijJars, String path)
   {
      
      File[] files = dir.listFiles();
      for (File f: files)
      {
         if (f.isDirectory())
         {
            expandFilePath(f, ijJars, path + "/" + f.getName());
         }
         else
         {
            ijJars.add(path + "/" + f.getName());
            //ijJars.add("ironjacamar-common-api.jar");
         }
      }

   }

   /**
    * We will accept the selection in the workbench to see if
    * we can initialize from it.
    * 
    * @param workbench IWorkbench
    * @param selection IStructuredSelection
    * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
    */
   public void init(IWorkbench workbench, IStructuredSelection selection)
   {
      this.selection = selection;
   }

   /**
    * setProjectName
    * @param projectName projectName
    */
   public void setProjectName(String projectName)
   {
      this.projectName = projectName;
   }

   /**
    * getProjectName
    * @return projectName
    */
   public String getProjectName()
   {
      return projectName;
   }

   /**
    * getWizardPage 
    * @return WizardPage
    */
   public DefinitionWizardPage getDefPage()
   {
      return defPage;
   }

   /**
    * getWizardPage 
    * @return WizardPage
    */
   public RaWizardPage getRaPage()
   {
      return raPage;
   }

   /**
    * getWizardPage 
    * @return WizardPage
    */
   public McfWizardPage getMcfPage()
   {
      return mcfPage;
   }

   /**
    * getWizardPage 
    * @return WizardPage
    */
   public InboundWizardPage getInboundPage()
   {
      return inboundPage;
   }

   /**
    * getWizardPage 
    * @return WizardPage
    */
   public AoWizardPage getAoPage()
   {
      return aoPage;
   }

   /**
    * getWizardPage 
    * @return WizardPage
    */
   public BuildWizardPage getBuildPage()
   {
      return buildPage;
   }
}
