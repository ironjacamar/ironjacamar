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
package org.jboss.jca.eclipse.wizards;

import org.jboss.jca.eclipse.Activator;
import org.jboss.jca.eclipse.ResourceBundles;
import org.jboss.jca.eclipse.preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * BuildWizardPage
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */

public class BuildWizardPage extends WizardPage
{

   private ISelection selection;
   
   private ResourceBundles pluginPrb = ResourceBundles.getInstance();

   /**
    * Constructor for SampleNewWizardPage.
    * 
    * @param selection ISelection
    */
   public BuildWizardPage(ISelection selection)
   {
      super("wizardPage");
      setTitle(pluginPrb.getString("codegen.build.title"));
      setDescription(pluginPrb.getString("codegen.build.desc"));
      this.selection = selection;
   }

   /**
    * createControl
    * @param parent Composite 
    * @see IDialogPage#createControl(Composite)
    */
   public void createControl(Composite parent)
   {
      Composite container = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      container.setLayout(layout);
      layout.numColumns = 2;
      layout.verticalSpacing = 9;

      Label label = new Label(container, SWT.NULL);
      label.setText(((CodeGenWizard) getWizard()).getCodegenResourceString("gen.mbean") + ":");
      
      final Button mbeanButton = new Button(container, SWT.CHECK);
      mbeanButton.setSelection(false);
      ((CodeGenWizard) getWizard()).getDef().setGenMbean(false);
      mbeanButton.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent event)
         {
            ((CodeGenWizard) getWizard()).getDef().setGenMbean(mbeanButton.getSelection());
         }
      });
      
      createBuildGroup(container);

      Link homeSettingLink = new Link(container, SWT.NULL);
      GridData gdHomeSettingLink = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
      homeSettingLink.setLayoutData(gdHomeSettingLink);
      homeSettingLink.setText("<a>IronJacamar home setting...</a>");
      //homeSettingLink.setToolTipText("IronJacamar home setting:");
      homeSettingLink.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent e)
         {
            PreferenceDialog createPreferenceDialogOn = PreferencesUtil.createPreferenceDialogOn(null,
                  "org.jboss.jca.eclipse.preferences.IronJacamarPreferencePage", null, null);
            createPreferenceDialogOn.open();
            dialogChanged();
         }
      });
      
      initialize();
      dialogChanged();
      setControl(container);
   }

   /**
    * @param container
    */
   private void createBuildGroup(Composite container)
   {
      Label label = new Label(container, SWT.NULL);
      label.setText(((CodeGenWizard) getWizard()).getCodegenResourceString("build.env") + ":");

      final String[] items =
      {"Apache Ant", "Apache Ant / Apache Ivy", "Apache Maven", "Gradle"};
      final Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
      combo.setItems(items);
      combo.setText("Apache Ant");
      ((CodeGenWizard) getWizard()).getDef().setBuild("ant");

      combo.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent e)
         {
            if (combo.getText().equals("Apache Maven"))
            {
               ((CodeGenWizard) getWizard()).getDef().setBuild("maven");
            } 
            else if (combo.getText().equals("Apache Ant"))
            {
               ((CodeGenWizard) getWizard()).getDef().setBuild("ant");
            } 
            else if (combo.getText().equals("Apache Ant / Apache Ivy"))
            {
               ((CodeGenWizard) getWizard()).getDef().setBuild("ivy");
            }
            else if (combo.getText().equals("Gradle"))
            {
               ((CodeGenWizard) getWizard()).getDef().setBuild("gradle");
            }
         }
      });
   }

   /**
    * Tests if the current workbench selection is a suitable container to use.
    */

   private void initialize()
   {
   }

   /**
    * Ensures that both text fields are set.
    */

   private void dialogChanged()
   {
      IPreferenceStore store = Activator.getDefault().getPreferenceStore();
      String ijHome = store.getString(PreferenceConstants.JCA_HOME_PATH);
      
      if (((CodeGenWizard) getWizard()).getDef().getBuild().equals("ant") && ijHome.equals(""))
      {
         updateStatus(pluginPrb.getString("codegen.build.ijhome"));
         return;
      }

      updateStatus(null);
      return;

   }

   private void updateStatus(String message)
   {
      setErrorMessage(message);
      setPageComplete(message == null);
   }

}
