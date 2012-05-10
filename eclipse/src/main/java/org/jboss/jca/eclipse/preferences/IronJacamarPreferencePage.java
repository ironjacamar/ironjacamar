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
package org.jboss.jca.eclipse.preferences;

import org.jboss.jca.eclipse.Activator;

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class IronJacamarPreferencePage extends FieldEditorPreferencePage 
   implements IWorkbenchPreferencePage
{
   private DirectoryFieldEditor ijHomeFieldEditor;

   /**
    * IronJacamarPreferencePage
    */
   public IronJacamarPreferencePage()
   {
      super(GRID);
      setPreferenceStore(Activator.getDefault().getPreferenceStore());
      setDescription("Settings about IronJacamar");
   }

   /**
    * Creates the field editors. Field editors are abstractions of
    * the common GUI blocks needed to manipulate various types
    * of preferences. Each field editor knows how to save and
    * restore itself.
    */
   public void createFieldEditors()
   {
      ijHomeFieldEditor = new DirectoryFieldEditor(PreferenceConstants.JCA_HOME_PATH, "IronJacamar home:", 
         getFieldEditorParent());
      addField(ijHomeFieldEditor);
   }

   /** init
    * @param workbench IWorkbench
    * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
    */
   public void init(IWorkbench workbench)
   {
   }
   
   /** propertyChange
    * @param event PropertyChangeEvent 
    * @see org.eclipse.jface.preference.FieldEditorPreferencePage#
    * propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
    */
   @Override
   public void propertyChange(PropertyChangeEvent event)
   {
      super.propertyChange(event);

      if (event.getProperty().equals(FieldEditor.VALUE))
      {
         checkState();
      }
   }

   /** checkState
    * @see org.eclipse.jface.preference.FieldEditorPreferencePage#checkState()
    */
   @Override
   protected void checkState()
   {
      super.checkState();

      String ijHome = ijHomeFieldEditor.getStringValue();
      File ironJacamarCoreApi = new File(ijHome + File.separator + "lib" 
         + File.separator + "ironjacamar-core-api.jar");

      if (!ironJacamarCoreApi.exists())
      {
         setErrorMessage("Please check Ironjacamar home directory");
         setValid(false);
      }
      else
      {
         setErrorMessage(null);
         setValid(true);
      }
   }

}
