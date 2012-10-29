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

import org.jboss.jca.eclipse.ResourceBundles;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * Dialog used to add or edit bean validation group.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class BeanValidateGroupDialog extends Dialog
{
   private final String initialValue;
   
   private ResourceBundles res = ResourceBundles.getInstance();
   
   private String title = null;
   
   private String value = null; 
   
   /**
    * Constructor.
    * 
    * @param shell the Shell
    * @param title the title
    * @param initialValue the initial value
    */
   public BeanValidateGroupDialog(Shell shell, String title, String initialValue)
   {
      super(shell);
      this.title = title;
      this.initialValue = initialValue;
   }
   
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite comp = (Composite) super.createDialogArea(parent);
      ((GridLayout) comp.getLayout()).numColumns = 2;
      
      Label label = new Label(comp, SWT.NULL);
      label.setText(res.getString("ra.generate.general.bean.validate.group.dialog.label"));
      
      final Text beanValidateGrpText = new Text(comp, SWT.SINGLE | SWT.BORDER);
      GridData data = new GridData(GridData.FILL_BOTH);
      data.widthHint = 200;
      
      beanValidateGrpText.setLayoutData(data);
      if (initialValue != null)
      {
         beanValidateGrpText.setText(initialValue);
      }
      beanValidateGrpText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            value = beanValidateGrpText.getText().trim();
         }
      });
      
      return comp;
   }
   
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      if (title != null)
      {
         newShell.setText(title);
      }
   }
   
   /**
    * Gets bean validation group
    * 
    * @return the bean validation group
    */
   public String getBeanValidateGroup ()
   {
      return this.value;
   }

}
