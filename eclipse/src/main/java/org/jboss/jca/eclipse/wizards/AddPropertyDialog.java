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

import org.jboss.jca.codegenerator.ConfigPropType;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * AddPropertyDialog
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class AddPropertyDialog extends Dialog
{

   private String name;

   private String value;

   private String type;

   private String title;

   private Label nameLabel;

   private Text nameText;

   private Label valueLabel;

   private Text valueText;

   private String[] initialValues;
   
   private List<ConfigPropType> propList;

   /**
    * AddPropertyDialog 
    * @param shell shell
    * @param title dialog title
    * @param initialValues initialValues
    */
   public AddPropertyDialog(Shell shell, String title, String[] initialValues)
   {
      super(shell);
      this.title = title;
      this.initialValues = initialValues;
   }

   /** (non-Javadoc)
    * @param parent Composite
    * @return Control Control
    * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   protected Control createDialogArea(Composite parent)
   {
      Composite comp = (Composite) super.createDialogArea(parent);
      ((GridLayout) comp.getLayout()).numColumns = 2;

      nameLabel = new Label(comp, SWT.NONE);
      nameLabel.setText("Name");
      nameLabel.setFont(comp.getFont());

      nameText = new Text(comp, SWT.BORDER | SWT.SINGLE);
      nameText.setText(initialValues[0]);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 300;
      nameText.setLayoutData(gd);
      nameText.setFont(comp.getFont());
      nameText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            updateButtons();
         }
      });

      nameLabel = new Label(comp, SWT.NONE);
      nameLabel.setText("Type");
      nameLabel.setFont(comp.getFont());

      final String[] items =
      {"String", "Boolean", "Integer", "Double", "Byte", "Short", "Long", "Float", "Character"};

      final Combo combo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
      combo.setItems(items);
      
      type = initialValues[1];
      if (type.equals(""))
      {
         type = "String";
      }
      combo.setText(type);

      combo.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent e)
         {
            type = combo.getText();
            updateButtons();
         }
      });

      valueLabel = new Label(comp, SWT.NONE);
      valueLabel.setText("Value");
      valueLabel.setFont(comp.getFont());

      valueText = new Text(comp, SWT.BORDER | SWT.SINGLE);
      valueText.setText(initialValues[2]);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 300;
      valueText.setLayoutData(gd);
      valueText.setFont(comp.getFont());
      valueText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            updateButtons();
         }
      });

      return comp;
   }

   /**
    * Return the name/value pair entered in this dialog.  If the cancel button was hit,
    * both will be <code>null</code>.
    * 
    * @return arrays of String
    */
   public String[] getNameValuePair()
   {
      return new String[] {name, type, value};
   }

   /** (non-Javadoc)
    * 
    * @param buttonId buttonId
    * 
    * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
    */
   protected void buttonPressed(int buttonId)
   {
      if (buttonId == IDialogConstants.OK_ID)
      {
         name = nameText.getText().trim();
         value = valueText.getText().trim();
      }
      else
      {
         name = null;
         value = null;
      }
      super.buttonPressed(buttonId);
   }

   /** (non-Javadoc) 
    * 
    * @param shell shell
    * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
    */
   protected void configureShell(Shell shell)
   {
      super.configureShell(shell);
      if (title != null)
      {
         shell.setText(title);
      }
   }

   /**
    * Enable the OK button if valid input
    */
   protected void updateButtons()
   {
      String name = nameText.getText().trim();
      String value = valueText.getText().trim();
      
      boolean validated = true;
      validated = validated && (name.length() > 0) && (value.length()) > 0;
      validated = validated && name.matches("[a-zA-Z_][a-zA-Z_0-9]*");
      if (propList != null)
      {
         for (ConfigPropType conf : propList)
         {
            if (name.equals(conf.getName()) && !name.equals(initialValues[0]))
            {
               validated = false;
               break;
            }
         }
      }
      if (type.equals("Boolean"))
      {
         validated = validated && (value.toLowerCase().equals("true") || value.toLowerCase().equals("false"));
      }
      if (type.equals("Integer") || type.equals("Short"))
      {
         try
         {
            Integer.parseInt(value);
         }
         catch (NumberFormatException e)
         {
            validated = false;
         }
      }
      if (type.equals("Double"))
      {
         try
         {
            Double.parseDouble(value);
         }
         catch (NumberFormatException e)
         {
            validated = false;
         }
      }
      if (type.equals("Long"))
      {
         try
         {
            Long.parseLong(value);
         }
         catch (NumberFormatException e)
         {
            validated = false;
         }
      }
      if (type.equals("Float"))
      {
         try
         {
            Float.parseFloat(value);
         }
         catch (NumberFormatException e)
         {
            validated = false;
         }
      }

      getButton(IDialogConstants.OK_ID).setEnabled(validated);
   }

   /**
    * Enable the buttons on creation.
    * @see org.eclipse.jface.window.Window#create()
    */
   public void create()
   {
      super.create();
      updateButtons();
   }

   /**
    * getPropList
    * @return the propList
    */
   public List<ConfigPropType> getPropList()
   {
      return propList;
   }

   /**
    * setPropList
    * @param propList the propList to set
    */
   public void setPropList(List<ConfigPropType> propList)
   {
      this.propList = propList;
   }
}
