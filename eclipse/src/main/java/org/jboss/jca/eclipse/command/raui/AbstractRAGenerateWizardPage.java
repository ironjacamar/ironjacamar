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

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.eclipse.ResourceBundles;
import org.jboss.jca.eclipse.wizards.AddPropertyDialog;
import org.jboss.jca.eclipse.wizards.PropsContentProvider;
import org.jboss.jca.eclipse.wizards.PropsLabelProvider;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public abstract class AbstractRAGenerateWizardPage extends WizardPage
{
   private PropsLabelProvider labelProvider = new PropsLabelProvider();
   
   private ResourceBundles res = ResourceBundles.getInstance();

   /**
    * The constructor
    * @param str the label
    */
   public AbstractRAGenerateWizardPage(String str)
   {
      super(str);
   }

   /**
    * Creates TableViewer.
    * 
    * @param parent the parent
    * @param fTableColumnHeaders the column headers
    * @param fTableColumnLayouts the column layouts
    * @return the TableViewer
    */
   TableViewer createTableViewer(Composite parent, String fTableColumnHeaders[], 
         ColumnLayoutData[] fTableColumnLayouts)
   {

      Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
      GridData data = new GridData(GridData.FILL_BOTH);

      data.widthHint = 600;
      data.heightHint = 100;
      table.setLayoutData(data);
      table.setFont(parent.getFont());

      TableViewer tableViewer = new TableViewer(table);
      tableViewer.setLabelProvider(labelProvider);

      TableLayout tableLayout = new TableLayout();
      table.setLayout(tableLayout);
      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      for (int i = 0; i < fTableColumnHeaders.length; i++)
      {
         tableLayout.addColumnData(fTableColumnLayouts[i]);
         TableColumn column = new TableColumn(table, SWT.NONE, i);
         column.setResizable(fTableColumnLayouts[i].resizable);
         column.setText(fTableColumnHeaders[i]);
      }

      return tableViewer;
   }

   /**
    * Creates TableViewer for config-property
    * 
    * @param parent the parent
    * @return the TableViewer
    */
   private TableViewer createConfigPropertyTableViewerInner(Composite parent)
   {
      String fTableColumnHeaders[] =
      {getString("config.props.name"), getString("config.props.type"), getString("config.props.value")};
      final ColumnLayoutData[] fTableColumnLayouts =
      {new ColumnWeightData(30), new ColumnWeightData(40), new ColumnWeightData(30)};
      TableViewer tableViewer = createTableViewer(parent, fTableColumnHeaders, fTableColumnLayouts);
      tableViewer.setContentProvider(new PropsContentProvider(tableViewer));
      return tableViewer;
   }

   /**
    * Creates and returns a configured button in the given composite with the given label.
    * @param parent the parent
    * @param label label
    * @return the Button
    */
   Button createPushButton(Composite parent, String label)
   {
      Button button = new Button(parent, SWT.PUSH);
      button.setFont(parent.getFont());
      button.setText(label);
      setButtonLayoutData(button);

      GridData gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
      button.setLayoutData(gridData);
      return button;
   }
   
   /**
    * Creates config properties panel.
    * 
    * @param parent the parent, which requires the numOfColumns of the layout is 1
    * @param initialValues initial values
    * @param label label of the table title
    * @return the Composite which holds all config-property related components.
    */
   Composite createConfigPropertyTableViewer(Composite parent, List<ConfigPropType> initialValues, String label)
   {
      Label configPropLabel = new Label(parent, SWT.NULL);
      configPropLabel.setText(label);
      
      final Composite configPropContainer = new Composite(parent, SWT.NULL);
      GridLayout configPropLayout = new GridLayout();
      configPropLayout.numColumns = 2;
      configPropLayout.verticalSpacing = 9;
      
      configPropContainer.setLayout(configPropLayout);
      
      final TableViewer configPropsTableView = createConfigPropertyTableViewerInner(configPropContainer);
      final PropsContentProvider configContentProvider = 
            (PropsContentProvider)configPropsTableView.getContentProvider();
      
      Composite buttonGroup = new Composite(configPropContainer, SWT.NONE);
      GridLayout configPropslayoutBtns = new GridLayout();
      configPropslayoutBtns.marginHeight = 0;
      configPropslayoutBtns.marginWidth = 0;
      buttonGroup.setLayout(configPropslayoutBtns);
      buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
      buttonGroup.setFont(buttonGroup.getFont());
      
      final Button editPropsBtn = createPushButton(buttonGroup, getString("command.edit.name"));
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
               AddPropertyDialog dialog = new AddPropertyDialog(getShell(), 
                     getString("command.edit.name"), initialValues, true);
               if (dialog.open() == Window.CANCEL)
               {
                  return;
               }
               String[] pair = dialog.getNameValuePair();
               if (!pair[2].equals(prop.getValue()) && pair[2].length() > 0)
               {
                  prop.setValue(pair[2]);
                  configContentProvider.update(prop);
                  configPropContainer.update();
                  onConfigPropUpdated(configPropsTableView, prop);
               }
            }
         }

      });
      configPropsTableView.addSelectionChangedListener(new ISelectionChangedListener()
      {
         
         @Override
         public void selectionChanged(SelectionChangedEvent event)
         {
            editPropsBtn.setEnabled(configPropsTableView.getSelection() != null 
                  && !configPropsTableView.getSelection().isEmpty());
         }
      });
      if (initialValues != null)
      {
         for (ConfigPropType configProp : initialValues)
         {
            configContentProvider.add(configProp);
         }
      }
      return configPropContainer;
   }
   
   /**
    * Notice that the ConfigPropType is changed.
    * 
    * @param configPropsTableView the TableViewer
    * @param prop the ConfigPropType
    */
   protected void onConfigPropUpdated(TableViewer configPropsTableView, ConfigPropType prop)
   {
      ;
   }
   
   /**
    * Creates general Text with initial value.
    * 
    * @param parent the parent
    * @param initialValue the initial value
    * @return the Text
    */
   protected Text createText(Composite parent, Object initialValue)
   {
      Text txt = new Text(parent, SWT.BORDER | SWT.SINGLE);
      if (initialValue != null)
      {
         txt.setText(initialValue.toString());
      }
      GridData layoutData = new GridData();
      layoutData.widthHint = 300;
      txt.setLayoutData(layoutData);
      return txt;
   }
   
   /**
    * General 2 columns layout.
    * @return general 2 columns layout
    */
   protected GridLayout getLayout()
   {
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.verticalSpacing = 9;
      layout.makeColumnsEqualWidth = true;
      return layout;
   }

   /**
    * Update dialog status.
    * 
    * @param message the error message if any
    */
   protected void updateStatus(String message)
   {
      setErrorMessage(message);
      setPageComplete(message == null);
   }
   
   /**
    * Returns string message according to the key.
    * 
    * @param key the key of the message
    * @return the string message or null
    */
   protected String getString(String key)
   {
      return this.res.getString(key);
   }
   
   /**
    * Returns string message according to the key.
    * 
    * @param key the key of the message
    * @param params parameters used to format the message
    * @return the string message or null
    */
   protected String getString(String key, Object... params)
   {
      return this.res.getString(key, params);
   }
   
}
