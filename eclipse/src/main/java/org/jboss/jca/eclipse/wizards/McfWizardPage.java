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
import org.jboss.jca.codegenerator.McfDef;

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
import org.eclipse.jface.wizard.IWizardPage;
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
 * McfWizardPage
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */

public class McfWizardPage extends WizardPage
{
   private Composite container;

   private Text mcfText;

   private Text mcText;

   private Text cfText;

   private Text cfImplText;

   private Text connText;

   private Text connImplText;

   private ISelection selection;

   private TableViewer propsTableViewer;

   private final PropsLabelProvider labelProvider = new PropsLabelProvider();

   private Button editButton;

   private Button removeButton;

   private Button addButton;

   private McfDef mcfDef;

   /**
    * Constructor for SampleNewWizardPage.
    * 
    * @param selection ISelection
    */
   public McfWizardPage(ISelection selection)
   {
      super("wizardPage");
      setTitle("Create Mcf");
      setDescription("Input Mcf Information.");
      this.selection = selection;

   }

   /**
    * @param parent Composite
    * @see IDialogPage#createControl(Composite)
    */
   public void createControl(Composite parent)
   {
      mcfDef = new McfDef(1, ((CodeGenWizard) getWizard()).getDef());
      List<McfDef> mcfDefs = new ArrayList<McfDef>();
      mcfDefs.add(mcfDef);
      ((CodeGenWizard) getWizard()).getDef().setMcfDefs(mcfDefs);

      List<ConfigPropType> propList = mcfDef.getMcfConfigProps();
      if (propList == null)
      {
         propList = new ArrayList<ConfigPropType>();
         mcfDef.setMcfConfigProps(propList);
      }
      Composite wholeContainer = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      wholeContainer.setLayout(layout);
      layout.numColumns = 1;

      Composite mcfContainer = new Composite(wholeContainer, SWT.NULL);
      layout = new GridLayout();
      mcfContainer.setLayout(layout);
      layout.numColumns = 2;
      layout.verticalSpacing = 9;

      Label label = new Label(mcfContainer, SWT.NULL);
      label.setText("Management Connection Factory class name:");

      mcfText = new Text(mcfContainer, SWT.BORDER | SWT.SINGLE);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 600;
      mcfText.setLayoutData(gd);
      mcfText.setText("AcmeManagedConnectionFactory");
      mcfDef.setMcfClass("AcmeManagedConnectionFactory");
      mcfText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = mcfText.getText();
            if (string.length() > 0)
            {
               mcfDef.setMcfClass(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Management Connection Factory Properties:");

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

      mcfContainer = new Composite(wholeContainer, SWT.NULL);
      layout = new GridLayout();
      mcfContainer.setLayout(layout);
      layout.numColumns = 2;
      layout.verticalSpacing = 9;

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Use ResourceAdapterAssociation:");

      final Button annoButton = new Button(mcfContainer, SWT.CHECK);
      annoButton.setSelection(true);
      mcfDef.setImplRaAssociation(true);
      annoButton.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent event)
         {

            mcfDef.setImplRaAssociation(annoButton.getSelection());
         }
      });

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Management Connection:");

      mcText = new Text(mcfContainer, SWT.BORDER | SWT.SINGLE);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 600;
      mcText.setLayoutData(gd);
      mcText.setText("AcmeManagedConnection");
      mcfDef.setMcClass("AcmeManagedConnection");
      mcText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = mcText.getText();
            if (string.length() > 0)
            {
               mcfDef.setMcClass(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Use CCi:");

      final Button cciButton = new Button(mcfContainer, SWT.CHECK);
      cciButton.setSelection(false);
      mcfDef.setUseCciConnection(false);
      cciButton.addSelectionListener(new SelectionAdapter()
      {
         public void widgetSelected(SelectionEvent event)
         {

            mcfDef.setUseCciConnection(cciButton.getSelection());
            if (cciButton.getSelection())
            {
               cfText.setEnabled(false);
               cfImplText.setEnabled(false);
               connText.setEnabled(false);
               connImplText.setEnabled(false);
            }
            else
            {
               cfText.setEnabled(true);
               cfImplText.setEnabled(true);
               connText.setEnabled(true);
               connImplText.setEnabled(true);
            }
         }
      });

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Connection factory interface:");

      cfText = new Text(mcfContainer, SWT.BORDER | SWT.SINGLE);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 600;
      cfText.setLayoutData(gd);
      cfText.setText("AcmeConnectionFactory");
      mcfDef.setCfInterfaceClass("AcmeConnectionFactory");
      cfText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = cfText.getText();
            if (string.length() > 0)
            {
               mcfDef.setCfInterfaceClass(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Management Connection:");

      cfImplText = new Text(mcfContainer, SWT.BORDER | SWT.SINGLE);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 600;
      cfImplText.setLayoutData(gd);
      cfImplText.setText("AcmeConnectionFactoryImpl");
      mcfDef.setCfClass("AcmeConnectionFactoryImpl");
      cfImplText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = cfImplText.getText();
            if (string.length() > 0)
            {
               mcfDef.setCfClass(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Connection interface:");

      connText = new Text(mcfContainer, SWT.BORDER | SWT.SINGLE);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 600;
      connText.setLayoutData(gd);
      connText.setText("AcmeConnection");
      mcfDef.setConnInterfaceClass("AcmeConnection");
      connText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = connText.getText();
            if (string.length() > 0)
            {
               mcfDef.setConnInterfaceClass(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });

      label = new Label(mcfContainer, SWT.NULL);
      label.setText("Connection implementation:");

      connImplText = new Text(mcfContainer, SWT.BORDER | SWT.SINGLE);
      gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.widthHint = 600;
      connImplText.setLayoutData(gd);
      connImplText.setText("AcmeConnectionImpl");
      mcfDef.setConnImplClass("AcmeConnectionImpl");
      connImplText.addModifyListener(new ModifyListener()
      {
         public void modifyText(ModifyEvent e)
         {
            String string = connImplText.getText();
            if (string.length() > 0)
            {
               mcfDef.setConnImplClass(string);
               updateStatus(null);
            }
            dialogChanged();
         }
      });
      initialize();
      dialogChanged();
      setControl(wholeContainer);
   }

   private void addButtonsToButtonGroup(Composite parent)
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

      List<ConfigPropType> propList = mcfDef.getMcfConfigProps();

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
      mcfDef.getMcfConfigProps().remove(prop);
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
      {new ColumnWeightData(30), new ColumnWeightData(40), new ColumnWeightData(30)};

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

      if (mcfText.getText().length() == 0)
      {
         updateStatus("ResourceAdapter class name must be specified");
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

   @Override
   public IWizardPage getNextPage()
   {
      if (!((CodeGenWizard) getWizard()).getDef().isSupportInbound())
      {
         return ((CodeGenWizard) getWizard()).getAoPage();
      }
      return super.getNextPage();
   }
}
