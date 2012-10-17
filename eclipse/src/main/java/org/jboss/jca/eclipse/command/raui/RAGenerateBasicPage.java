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
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * Basic page for settings of: archive; transactionsupport; bootstrapcontext; beanvalidationgroups;
 * config-properties; whether deploy the rar first.
 *  
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAGenerateBasicPage extends AbstractRAGenerateWizardPage
{

   private final ResourceAdapterConfig raConfig;
   private final ConnectorHelper connectorHelper;
   
   /**
    * The constructor.
    * 
    * @param wizard the RAGenerateWizard.
    */
   public RAGenerateBasicPage(RAGenerateWizard wizard)
   {
      super("Setting basic information");
      this.raConfig = wizard.getRaConfig();
      this.connectorHelper = wizard.getConnectorHelper();;
      setTitle("General information of " + wizard.getRAXMLFileName());
      setDescription("Configure generation information for " + wizard.getRAXMLFileName());
   }

   @Override
   public void createControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      whole.setLayout(layout);
      layout.numColumns = 1;

      final Group generalGrp = new Group(whole, SWT.SHADOW_IN);
      generalGrp.setText("General Information");
      
      layout = new GridLayout();
      generalGrp.setLayout(layout);
      layout.numColumns = 2;
      layout.verticalSpacing = 9;
      layout.horizontalSpacing = 100;

      // archive
      Label label = new Label(generalGrp, SWT.NULL);
      label.setText("Archive:");
      Label archiveLavel = new Label(generalGrp, SWT.NULL);
      archiveLavel.setText(raConfig.getArchive());

      // transaction support
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Transaction Support:");
      final String[] items =
      {TransactionSupportEnum.NoTransaction.name(), TransactionSupportEnum.LocalTransaction.name(),
            TransactionSupportEnum.XATransaction.name()};
      final Combo combo = new Combo(generalGrp, SWT.DROP_DOWN | SWT.READ_ONLY);
      combo.setItems(items);

      // TransactionSupport has been merged in connector
      final TransactionSupportEnum currentTransactionSupport = connectorHelper.getTransactionSupportEnum();

      combo.setText(currentTransactionSupport.name());
      combo.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            TransactionSupportEnum transactionSupport = TransactionSupportEnum.valueOf(combo.getText());
            if (transactionSupport.equals(currentTransactionSupport))
            {
               raConfig.setTransactionSupport(null);
            }
            else
            {
               raConfig.setTransactionSupport(transactionSupport);
            }
         }
      });

      // bootstrap-context
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Bootstrap Context:");
      final Text bootStrapContext = new Text(generalGrp, SWT.BORDER | SWT.SINGLE);
      GridData data = new GridData();
      data.widthHint = 300;
      bootStrapContext.setLayoutData(data);
      final String currentBoolStrapContext = connectorHelper.getBootStrapContext();
      bootStrapContext.setText(currentBoolStrapContext == null ? "" : currentBoolStrapContext);
      bootStrapContext.addModifyListener(new ModifyListener()
      {

         @Override
         public void modifyText(ModifyEvent e)
         {
            if (bootStrapContext.getText().trim().equals(currentTransactionSupport))
            {
               raConfig.setBootstrapContext(null);
            }
            else
            {
               raConfig.setBootstrapContext(bootStrapContext.getText().trim());
            }
         }
      });

      Label beanValidLabel = new Label(whole, SWT.NULL);
      beanValidLabel.setText("Set Bean Validation Groups:");
      
      final Composite beanValidContainer = new Composite(whole, SWT.NONE);
      GridLayout beanValidLayout = new GridLayout();
      beanValidLayout.numColumns = 2;
      beanValidLayout.verticalSpacing = 9;
      
      beanValidContainer.setLayout(beanValidLayout);
      
      // bean validation groups
      String fTableColumnHeaders[] = {"Bean Validation Groups"};
      final ColumnLayoutData[] fTableColumnLayouts = {new ColumnWeightData(300)};

      final TableViewer beanValidTableViewer = 
            createTableViewer(beanValidContainer, fTableColumnHeaders, fTableColumnLayouts);
      final BeanValidationGroupContentProvider contentProvider = 
            new BeanValidationGroupContentProvider(beanValidTableViewer, raConfig, this.connectorHelper);
      beanValidTableViewer.setContentProvider(contentProvider);

      Composite buttonGroup = new Composite(beanValidContainer, SWT.NONE);
      GridLayout layoutBtns = new GridLayout();
      layoutBtns.marginHeight = 0;
      layoutBtns.marginWidth = 0;
      buttonGroup.setLayout(layoutBtns);
      buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
      buttonGroup.setFont(buttonGroup.getFont());
      final Button addBtn = createPushButton(buttonGroup, "Add");
      addBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            BeanValidateGroupDialog dialog = new BeanValidateGroupDialog(beanValidTableViewer.getControl().getShell(), 
                  "Add Bean Validation Group", null);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }
            String value = dialog.getBeanValidateGroup();
            if (value != null && value.length() > 0)
            {
               contentProvider.add(value);
            }
            beanValidContainer.update();
         }
      });
      final Button editBtn = createPushButton(buttonGroup, "Edit");
      editBtn.setEnabled(false);
      editBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) beanValidTableViewer.getSelection();
            String initialValue = (String)selection.getFirstElement();
            BeanValidateGroupDialog dialog = new BeanValidateGroupDialog(beanValidTableViewer.getControl().getShell(), 
                  "Edit Bean Validation Group", initialValue);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }
            String value = dialog.getBeanValidateGroup();
            if (value != null && value.length() > 0)
            {
               contentProvider.update(initialValue, value);
            }
            beanValidContainer.update();
         }
      });
      final Button delBtn = createPushButton(buttonGroup, "Remove");
      delBtn.setEnabled(false);
      delBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) beanValidTableViewer.getSelection();
            if (selection != null)
            {
               String beanValidGrp = (String)selection.getFirstElement();
               contentProvider.remove(beanValidGrp);
            }
            beanValidContainer.update();
         }
      });
      
      beanValidTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {
         
         @Override
         public void selectionChanged(SelectionChangedEvent event)
         {
            editBtn.setEnabled(beanValidTableViewer.getSelection() != null);
            delBtn.setEnabled(beanValidTableViewer.getSelection() != null);
         }
      });

      // config properties
      createConfigPropertyTableViewer(whole, this.connectorHelper.getConfigProperties());
      setControl(whole);
   }
   
   @Override
   protected void onConfigPropUpdated(TableViewer configPropsTableView, ConfigPropType prop)
   {
      super.onConfigPropUpdated(configPropsTableView, prop);
      raConfig.getConfigProperties().put(prop.getName(), prop.getValue());
   }
}
