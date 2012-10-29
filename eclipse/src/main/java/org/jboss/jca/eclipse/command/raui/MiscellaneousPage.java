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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * MiscellaneousPage is used to configure miscellaneous options to generate the -ra.xml file.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class MiscellaneousPage extends AbstractRAGenerateWizardPage
{
   
   private final RAGenerateWizard wizard;
   
   /**
    * MiscellaneousPage
    * 
    * @param wizard the RAGenerateWizard
    */
   public MiscellaneousPage(RAGenerateWizard wizard)
   {
      super("MiscellaneousPage");
      this.wizard = wizard;
      this.setTitle(getString("ra.generate.misc.title"));
      this.setDescription(getString("ra.generate.misc.description"));
   }

   @Override
   public void createControl(Composite parent)
   {
      
      Composite whole = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      whole.setLayout(layout);
      layout.numColumns = 1;
      
      Composite container = new Composite(whole, SWT.NULL);
      GridLayout footLayout = new GridLayout();
      footLayout.numColumns = 2;
      footLayout.horizontalSpacing = 100;
      container.setLayout(footLayout);

      // deploy rar first ?
      Label label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.misc.deploy.rar.first"));
      final Button deployRARBtn = new Button(container, SWT.CHECK);
      deployRARBtn.setSelection(false);
      deployRARBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            wizard.setDeployRAR(deployRARBtn.getSelection());
         }
      });
      setControl(whole);
   }

}
