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

import org.jboss.jca.eclipse.ResourceBundles;
import org.jboss.jca.validator.Validation;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ValidateHandler extends AbstractIronJacamarHandler
{
   private IWorkbenchWindow window = null;
   
   private ResourceBundles pluginPrb = ResourceBundles.getInstance();

   /**
    * The constructor.
    */
   public ValidateHandler()
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
      window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

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
      if (rarFile != null)
      {
         validate(rarFile);
      }
      return null;
   }
   
   private void validate(IFile rarFile)
   {
      int result = -1;
      try
      {
         result = Validation.validate(rarFile.getLocationURI().toURL(), ".");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         result = -2;
      }

      if (result == 0)
      {
         MessageDialog.openInformation(window.getShell(), 
            "Ironjacamar-eclipse", pluginPrb.getString("rar.validate.success"));
      }
      else
      {
         MessageDialog.openInformation(window.getShell(), 
            "Ironjacamar-eclipse", pluginPrb.getString("rar.validate.fail"));
      }
      enableHandler();
   }

   @Override
   protected void onBuildFinished(IProject project)
   {
      enableHandler();
   }
}
