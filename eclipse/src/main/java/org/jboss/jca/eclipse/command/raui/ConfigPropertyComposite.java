/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.eclipse.wizards.AddPropertyDialog;
import org.jboss.jca.eclipse.wizards.PropsContentProvider;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * 
 * The composite UI to edit <code>ConfigProperty</code>.
 * 
 * This is used only for the ConfigProperty modification, not for Add/Remove action.
 * 
 * @author Lin Gao <lin.gao@ironjacamar.org>
 *
 */
public class ConfigPropertyComposite
{
   
   private final Shell shell;
   
   private final List<ConfigPropType> configProperties;
   
   private PropsContentProvider configContentProvider;
   
   private boolean editOnly = true;
   
   private Button delBtn;
   
   /**
    * Constructor for the configuration properties composite UI.
    * 
    * @param shell the active shell
    * @param configProperties the configuration property list
    */
   public ConfigPropertyComposite(Shell shell, List<ConfigPropType> configProperties)
   {
      this.configProperties = configProperties;
      this.shell = shell;
      if (this.configProperties == null)
      {
         throw new IllegalArgumentException("Initial configuration property list can not be null.");
      }
   }
   
   /**
    * @param editOnly the editOnly to set
    */
   public void setEditOnly(boolean editOnly)
   {
      this.editOnly = editOnly;
   }

   /**
    * Creates a UI composite to edit the configuration properties.
    * 
    * @param parent the parent
    * @return a UI composite
    */
   public Composite createControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NULL);
      GridLayout wholeLayout = new GridLayout();
      wholeLayout.numColumns = 1;
      wholeLayout.verticalSpacing = 2;
      whole.setLayout(wholeLayout);
      
      Label configPropLabel = new Label(whole, SWT.NULL);
      configPropLabel.setText(UIResources.getString("ra.generate.mcf.extension.config.properties"));
      
      final Composite configPropContainer = new Composite(whole, SWT.NULL);
      GridLayout configPropLayout = new GridLayout();
      configPropLayout.numColumns = 2;
      configPropLayout.verticalSpacing = 2;
      configPropContainer.setLayout(configPropLayout);
      
      String fTableColumnHeaders[] = {UIResources.getString("config.props.name"), 
            UIResources.getString("config.props.type"), UIResources.getString("config.props.value")};
      final ColumnLayoutData[] fTableColumnLayouts = {new ColumnWeightData(30), new ColumnWeightData(40), 
         new ColumnWeightData(30)};
      final TableViewer configPropsTableView = UIResources.createTableViewer(configPropContainer, fTableColumnHeaders,
            fTableColumnLayouts);
      configContentProvider = new PropsContentProvider(configPropsTableView);
      configPropsTableView.setContentProvider(configContentProvider);
      
      Composite buttonGroup = new Composite(configPropContainer, SWT.NONE);
      GridLayout configPropslayoutBtns = new GridLayout();
      configPropslayoutBtns.marginHeight = 0;
      configPropslayoutBtns.marginWidth = 0;
      buttonGroup.setLayout(configPropslayoutBtns);
      buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
      buttonGroup.setFont(buttonGroup.getFont());
      
      if (!editOnly)
      {
         final Button addBtn = UIResources.createPushButton(buttonGroup, UIResources.getString("command.add.name"));
         addBtn.addSelectionListener(new SelectionAdapter()
         {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               String initialValues[] = new String[]{"", "String", ""};
               AddPropertyDialog dialog = new AddPropertyDialog(shell, 
                     UIResources.getString("command.add.name"), initialValues);
               if (dialog.open() == Window.CANCEL)
               {
                  return;
               }
               String[] pair = dialog.getNameValuePair();
               String name = pair[0];
               String type = pair[1];
               String value = pair[2];
               ConfigPropType prop = getConfigPropType(name);
               if (prop != null)
               {
                  prop.setType(type);
                  prop.setValue(value);
                  configContentProvider.update(prop);
               }
               else
               {
                  prop = new ConfigPropType();
                  prop.setName(name);
                  prop.setType(type);
                  prop.setValue(value);
                  configProperties.add(prop);
                  configContentProvider.add(prop);
               }
               configPropContainer.update();
            }

         });
      }
      
      final Button editPropsBtn = UIResources.createPushButton(buttonGroup, 
            UIResources.getString("command.edit.name"));
      GridData btnData = new GridData();
      btnData.widthHint = 60;
      editPropsBtn.setLayoutData(btnData);
      
      editPropsBtn.setEnabled(false);
      editPropsBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection)configPropsTableView.getSelection();
            ConfigPropType prop = (ConfigPropType) selection.getFirstElement();
            if (prop != null)
            {
               String type = prop.getType().substring(prop.getType().lastIndexOf(".") + 1);
               String initialValues[] = new String[]{prop.getName(), type, prop.getValue()};
               AddPropertyDialog dialog = new AddPropertyDialog(shell, 
                     UIResources.getString("command.edit.name"), initialValues, editOnly);
               if (dialog.open() == Window.CANCEL)
               {
                  return;
               }
               String[] pair = dialog.getNameValuePair();
               prop.setName(pair[0]);
               prop.setType(pair[1]);
               prop.setValue(pair[2]);
               configContentProvider.update(prop);
               configPropContainer.update();
            }
         }

      });
      
      if (!editOnly)
      {
         delBtn = UIResources.createPushButton(buttonGroup, UIResources.getString("command.remove.name"));
         delBtn.setEnabled(false);
         delBtn.addSelectionListener(new SelectionAdapter()
         {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
               IStructuredSelection selection = (IStructuredSelection) configPropsTableView.getSelection();
               if (selection != null)
               {
                  ConfigPropType prop = (ConfigPropType)selection.getFirstElement();
                  if (configProperties.remove(prop))
                  {
                     configContentProvider.remove(prop);
                  }
               }
               configPropContainer.update();
            }
         });
      }
      
      configPropsTableView.addSelectionChangedListener(new ISelectionChangedListener()
      {
         
         @Override
         public void selectionChanged(SelectionChangedEvent event)
         {
            editPropsBtn.setEnabled(configPropsTableView.getSelection() != null 
                  && !configPropsTableView.getSelection().isEmpty());
            if (!editOnly)
            {
               delBtn.setEnabled(configPropsTableView.getSelection() != null 
                     && !configPropsTableView.getSelection().isEmpty());
            }
         }
      });
      
      // initialize the configuration properties.
      for (ConfigPropType configProp : configProperties)
      {
         configContentProvider.add(configProp);
      }
      
      return whole;
   }
   
   private ConfigPropType getConfigPropType(String name)
   {
      for (ConfigPropType prop: this.configProperties)
      {
         if (prop.getName().equals(name))
         {
            return prop;
         }
      }
      return null;
   }
   
}
