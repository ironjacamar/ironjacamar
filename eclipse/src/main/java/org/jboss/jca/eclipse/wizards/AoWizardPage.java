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

import org.jboss.jca.codegenerator.AdminObjectType;
import org.jboss.jca.codegenerator.ConfigPropType;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
 * AoWizardPage
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */

public class AoWizardPage extends WizardPage
{
   private Composite container;

   private Text aoText;

   private Text aoImplText;

   private ISelection selection;

   private TableViewer propsTableViewer;

   private final PropsLabelProvider labelProvider = new PropsLabelProvider();

   private Button editButton;

   private Button removeButton;

   private Button addButton;

   private AdminObjectType aoDef;

   /**
    * Constructor for AoWizardPage.
    * 
    * @param selection selection
    */
   public AoWizardPage(ISelection selection)
   {
      super("wizardPage");
      setTitle("Create Admin Object");
      setDescription("Input admin object information.");
      this.selection = selection;

   }

   /**
    * createControl
    * 
    * @param parent Composite
    * @see IDialogPage#createControl(Composite)
    */
   public void createControl(Composite parent)
   {
      aoDef = new AdminObjectType();
      List<AdminObjectType> aoTypes = new ArrayList<AdminObjectType>();
      aoTypes.add(aoDef);
      ((CodeGenWizard) getWizard()).getDef().setAdminObjects(aoTypes);

      List<ConfigPropType> propList = aoDef.getAoConfigProps();
      if (propList == null)
      {
         propList = new ArrayList<ConfigPropType>();
         aoDef.setAoConfigProps(propList);
      }

      Composite wholeContainer = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      wholeContainer.setLayout(layout);
      layout.numColumns = 1;

      Composite inContainer = new Composite(wholeContainer, SWT.NULL);
      layout = new GridLayout();
      inContainer.setLayout(layout);
      layout.numColumns = 2;
      layout.verticalSpacing = 9;

      Label label = new Label(inContainer, SWT.NULL);
      label.setText(((CodeGenWizard) getWizard()).getResourceString("gen.adminobject") + ":");
      final Button aoButton = new Button(inContainer, SWT.CHECK);

      aoButton.setSelection(true);
      ((CodeGenWizard) getWizard()).getDef().setGenAdminObject(true);

      label = new Label(inContainer, SWT.NULL);
      label.setText(((CodeGenWizard) getWizard()).getResourceString("adminobject.raa") + ":");

      final Button raaButton = new Button(inContainer, SWT.CHECK);

      raaButton.setSelection(true);
      ((CodeGenWizard) getWizard()).getDef().setAdminObjectImplRaAssociation(true);
      raaButton.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent event)
         {

            ((CodeGenWizard) getWizard()).getDef().setAdminObjectImplRaAssociation(raaButton.getSelection());

         }
      });

      label = new Label(inContainer, SWT.NULL);
      label.setText(((CodeGenWizard) getWizard()).getResourceString("adminobject.interface.name") + ":");

      aoButton.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent event)
         {

            ((CodeGenWizard) getWizard()).getDef().setGenAdminObject(aoButton.getSelection());
            if (aoButton.getSelection())
            {
               raaButton.setEnabled(true);
               aoText.setEnabled(true);
               aoImplText.setEnabled(true);
            }
            else
            {
               raaButton.setEnabled(false);
               aoText.setEnabled(false);
               aoImplText.setEnabled(false);
            }
         }
      });

      aoText = new Text(inContainer, SWT.BORDER | SWT.SINGLE);
      aoText.setBounds(5, 5, 600, 10);
      aoText.setText("AcmeAdminObject");
      aoDef.setAdminObjectInterface("AcmeAdminObject");
      aoText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = aoText.getText();
            if (string.length() > 0)
            {
               aoDef.setAdminObjectInterface(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });

      label = new Label(inContainer, SWT.NULL);
      label.setText(((CodeGenWizard) getWizard()).getResourceString("adminobject.class.name") + ":");

      aoImplText = new Text(inContainer, SWT.BORDER | SWT.SINGLE);
      aoImplText.setBounds(5, 5, 600, 10);
      aoImplText.setText("AcmeAdminObjectImpl");
      aoDef.setAdminObjectClass("AcmeAdminObjectImpl");
      aoImplText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = aoImplText.getText();
            if (string.length() > 0)
            {
               aoDef.setAdminObjectClass(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });

      label = new Label(inContainer, SWT.NULL);
      label.setText(((CodeGenWizard) getWizard()).getResourceString("adminobject.config.properties") + ":");

      container = new Composite(wholeContainer, SWT.NULL);
      layout = new GridLayout();
      container.setLayout(layout);
      layout.numColumns = 2;

      propsTableViewer = createTableViewer(container);

      Composite buttonGroup = new Composite(container, SWT.NONE);
      GridLayout layoutBtns = new GridLayout();
      layoutBtns.marginHeight = 0;
      layoutBtns.marginWidth = 0;
      buttonGroup.setLayout(layoutBtns);
      buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
      buttonGroup.setFont(buttonGroup.getFont());

      addButtonsToButtonGroup(buttonGroup);

      initialize();
      dialogChanged();
      setControl(wholeContainer);
   }

   /**
    * addButtonsToButtonGroup
    * @param parent Composite
    */
   protected void addButtonsToButtonGroup(Composite parent)
   {
      if (editButton == null)
      {
         addButton = createPushButton(parent, "Add");
         editButton = createPushButton(parent, "Edit");
         removeButton = createPushButton(parent, "Remove");
      }
   }

   /**
    * Creates and returns a configured button in the given composite with the given
    * label. Widget selection call-backs for the returned button will be processed
    * by the <code>buttonListener</code>
    */
   private Button createPushButton(Composite parent, String label)
   {
      Button button = new Button(parent, SWT.PUSH);
      button.setFont(parent.getFont());
      button.setText(label);
      this.setButtonLayoutData(button);

      button.addSelectionListener(buttonListener);
      GridData gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
      button.setLayoutData(gridData);
      return button;
   }

   /**
    * Button listener that delegates for widget selection events.
    */
   private SelectionAdapter buttonListener = new SelectionAdapter()
   {
      public void widgetSelected(SelectionEvent event)
      {
         if (event.widget == addButton)
         {
            addProperty();
         }
         else if (event.widget == editButton)
         {
            edit();
         }
         else if (event.widget == removeButton)
         {
            remove(propsTableViewer);
         }
      }
   };

   /**
    * Allows the user to enter a user property
    */
   private void addProperty()
   {
      String title = "Add";
      AddPropertyDialog dialog = new AddPropertyDialog(propsTableViewer.getControl().getShell(), title, new String[]
      {"", "", ""});
      if (dialog.open() == Window.CANCEL)
      {
         return;
      }

      String[] pair = dialog.getNameValuePair();
      String name = pair[0];
      /*
      if (!overwrite(name)) {
          return;
      }
      */

      List<ConfigPropType> propList = aoDef.getAoConfigProps();

      ConfigPropType prop = new ConfigPropType();
      prop.setName(name);
      prop.setType(pair[1]);
      prop.setValue(pair[2]);
      ((PropsContentProvider) propsTableViewer.getContentProvider()).add(prop);
      propList.add(prop);
      container.update();

   }

   private void edit()
   {
      IStructuredSelection selection = (IStructuredSelection) propsTableViewer.getSelection();
      ConfigPropType prop = (ConfigPropType) selection.getFirstElement();

      String title = "Edit";
      AddPropertyDialog dialog = new AddPropertyDialog(propsTableViewer.getControl().getShell(), title, new String[]
      {prop.getName(), prop.getType(), prop.getValue()});

      if (dialog.open() == Window.CANCEL)
      {
         return;
      }

      String[] pair = dialog.getNameValuePair();
      String name = pair[0];
      /*
      if (!name.equals(originalName)) {
          if (!overwrite(name)){
              return;
          }
      }
      */
      prop.setName(name);
      prop.setType(pair[1]);
      prop.setValue(pair[2]);
      ((PropsContentProvider) propsTableViewer.getContentProvider()).update(prop);

      container.update();

   }

   private void remove(TableViewer viewer)
   {
      PropsContentProvider propContentProvider = (PropsContentProvider) viewer.getContentProvider();
      IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
      ConfigPropType prop = (ConfigPropType) sel.getFirstElement();
      aoDef.getAoConfigProps().remove(prop);
      propContentProvider.remove(sel);

      container.update();
   }

   /**
    * Handles selection changes in the Property table viewer.
    */
   private void propertyTableSelectionChanged(IStructuredSelection newSelection)
   {

   }

   private ISelectionChangedListener tableListener = new ISelectionChangedListener()
   {
      public void selectionChanged(SelectionChangedEvent event)
      {

         if (event.getSource() == propsTableViewer)
         {
            propertyTableSelectionChanged((IStructuredSelection) event.getSelection());
         }
      }

   };

   private TableViewer createTableViewer(Composite parent)
   {
      String fTableColumnHeaders[] =
      {"Name", "Type", "Value"};
      final ColumnLayoutData[] fTableColumnLayouts =
      {new ColumnWeightData(30), new ColumnWeightData(20), new ColumnWeightData(50)};

      Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
      GridData data = new GridData(GridData.FILL_BOTH);

      data.widthHint = 600;
      table.setLayoutData(data);
      table.setFont(parent.getFont());

      TableViewer tableViewer = new TableViewer(table);
      tableViewer.setContentProvider(new PropsContentProvider(tableViewer));
      tableViewer.setLabelProvider(labelProvider);
      tableViewer.addSelectionChangedListener(tableListener);

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

      if (aoText.getText().length() == 0)
      {
         updateStatus("Admin object class name must be specified");
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
