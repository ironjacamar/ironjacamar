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

import org.jboss.jca.eclipse.command.raui.ResourceAdapterConfig.VERSION;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;


/**
 * RAVersionPage is used to select in which version to generate the -ra.xml file.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAVersionPage extends AbstractRAGenerateWizardPage
{
   
   private final RAGenerateWizard wizard;
   
   /**
    * MiscellaneousPage
    * 
    * @param wizard the RAGenerateWizard
    */
   public RAVersionPage(RAGenerateWizard wizard)
   {
      super("RAVersionPage");
      this.wizard = wizard;
      this.setTitle(getString("ra.generate.version.title"));
      this.setDescription(getString("ra.generate.version.description"));
   }

   @Override
   public void createControl(Composite parent)
   {
      
      Composite whole = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      whole.setLayout(layout);
      layout.numColumns = 1;
      
      final Group generalGrp = new Group(whole, SWT.SHADOW_IN);
      generalGrp.setText(getString("ra.generate.version.select"));
      
      layout = new GridLayout();
      generalGrp.setLayout(layout);
      layout.numColumns = 2;
      layout.verticalSpacing = 9;
      layout.horizontalSpacing = 100;
      
      // select resource adapter version
      Label label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.version.select.label"));
      
      final String[] items =
      {ResourceAdapterConfig.VERSION.VERSION_1_0.getVersion(), ResourceAdapterConfig.VERSION.VERSION_1_1.getVersion()};
      final Combo combo = new Combo(generalGrp, SWT.DROP_DOWN | SWT.READ_ONLY);
      combo.setItems(items);
      combo.setText(this.wizard.getRaConfig().getVersion().getVersion());

      combo.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            wizard.getRaConfig().setVersion(VERSION.forName(combo.getText()));
         }
      });
      setControl(whole);
   }

}
