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

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.eclipse.command.raui.ResourceAdapterConfig.VERSION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Basic page for settings of: archive; transactionsupport; bootstrapcontext; beanvalidationgroups;
 * config-properties; whether deploy the rar first.
 *  
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAGenerateBasicPage extends AbstractRAGenerateWizardPage
      implements
         ResourceAdapterConfig.VersionChangeListener
{

   private final ResourceAdapterConfig raConfig;

   private final ResourceAdapterConfig.WorkManagerConfig workManagerConfig;

   private List<Control> switchEnabledOnVersion = new ArrayList<Control>();

   /**
    * The constructor.
    * 
    * @param wizard the RAGenerateWizard.
    */
   public RAGenerateBasicPage(RAGenerateWizard wizard)
   {
      super("RAGenerateBasicPage");
      this.raConfig = wizard.getRaConfig();
      setTitle(getString("ra.generate.general.title", wizard.getRAXMLFileName()));
      setDescription(getString("ra.generate.general.description", wizard.getRAXMLFileName()));
      this.raConfig.addVersionChangeListener(this);
      this.workManagerConfig = this.raConfig.getWorkManagerConfig();
   }

   @Override
   public void versionChanged(VERSION version)
   {
      if (VERSION.VERSION_1_0.equals(version))
      {
         switchEnabled(false);
      }
      else if (VERSION.VERSION_1_1.equals(version))
      {
         switchEnabled(true);
      }
   }

   private void switchEnabled(boolean enabled)
   {
      for (Control control : this.switchEnabledOnVersion)
      {
         control.setEnabled(enabled);
      }
   }

   /**
    * Creates work manager security control
    * 
    * @param parent the parent
    * @return the control
    */
   private Composite createWorkManagerSecurity(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 5;
      whole.setLayout(layout);

      final Composite container = new Composite(whole, SWT.None);
      GridLayout containerLayout = new GridLayout();
      containerLayout.numColumns = 2;
      containerLayout.verticalSpacing = 2;
      containerLayout.makeColumnsEqualWidth = true;
      container.setLayout(containerLayout);

      // mapping required
      Label label = new Label(container, SWT.NULL);
      this.switchEnabledOnVersion.add(label);
      label.setText(getString("ra.generate.general.workmanager.mapping.required"));
      final Button mappingBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      switchEnabledOnVersion.add(mappingBtn);
      final Boolean mappingRequired = getInitialMappingRequired();
      mappingBtn.setSelection(mappingRequired);
      mappingBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            workManagerConfig.setMappingRequired(mappingBtn.getSelection());
            container.update();
         }
      });

      // domain
      label = new Label(container, SWT.NULL);
      this.switchEnabledOnVersion.add(label);
      label.setText(getString("ra.generate.general.workmanager.domain"));
      final Text domainTxt = new Text(container, SWT.BORDER | SWT.SINGLE);
      String initialDomain = getInitialDomain();
      domainTxt.setText(initialDomain != null ? initialDomain : "");
      switchEnabledOnVersion.add(domainTxt);
      domainTxt.setLayoutData(gridData(280));
      domainTxt.addModifyListener(new ModifyListener()
      {

         @Override
         public void modifyText(ModifyEvent event)
         {
            workManagerConfig.setDomain(domainTxt.getText());
         }
      });

      // default principal
      label = new Label(container, SWT.NULL);
      this.switchEnabledOnVersion.add(label);
      label.setText(getString("ra.generate.general.workmanager.principal"));
      final Text principalTxt = new Text(container, SWT.BORDER | SWT.SINGLE);
      String initialPrincipal = getInitialPrincipal();
      principalTxt.setText(initialPrincipal != null ? initialPrincipal : "");
      switchEnabledOnVersion.add(principalTxt);
      principalTxt.setLayoutData(gridData(280));
      principalTxt.addModifyListener(new ModifyListener()
      {

         @Override
         public void modifyText(ModifyEvent event)
         {
            workManagerConfig.setDefaultPricipal(principalTxt.getText());
         }
      });

      // default groups
      Label defaultGroups = new Label(whole, SWT.NULL);
      this.switchEnabledOnVersion.add(defaultGroups);
      defaultGroups.setText(getString("ra.generate.general.workmanager.groups"));

      final Composite defaultGrpsContainer = new Composite(whole, SWT.NONE);
      GridLayout defaultGrpsLayout = new GridLayout();
      defaultGrpsLayout.numColumns = 2;
      defaultGrpsLayout.verticalSpacing = 5;
      defaultGrpsContainer.setLayout(defaultGrpsLayout);

      String groupsTabHeaders[] =
      {getString("ra.generate.general.workmanager.table.group")};
      final ColumnLayoutData[] defaultGrpsLayoutData =
      {new ColumnWeightData(300)};

      final TableViewer defaultGroupsTableViewer = UIResources.createTableViewer(defaultGrpsContainer,
            groupsTabHeaders, defaultGrpsLayoutData);
      switchEnabledOnVersion.add(defaultGroupsTableViewer.getControl());
      final List<String> initialDefaultGroups = getDefaultGroups();
      final StringValuesContentProvider<String> contentProvider = new StringValuesContentProvider<String>(
            defaultGroupsTableViewer, initialDefaultGroups);

      defaultGroupsTableViewer.setContentProvider(contentProvider);

      Composite buttonGroup = new Composite(defaultGrpsContainer, SWT.NONE);
      GridLayout layoutBtns = new GridLayout();
      layoutBtns.marginHeight = 0;
      layoutBtns.marginWidth = 0;
      buttonGroup.setLayout(layoutBtns);
      buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
      buttonGroup.setFont(buttonGroup.getFont());

      final Map<String, String> defaultGroupsMap = new HashMap<String, String>();
      final String groupKey = "ra.generate.general.workmanager.group.label";

      final Button addBtn = UIResources.createPushButton(buttonGroup, getString("command.add.name"));
      switchEnabledOnVersion.add(addBtn);
      addBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            defaultGroupsMap.put(groupKey, "");
            StringInputDialog dialog = new StringInputDialog(defaultGroupsTableViewer.getControl().getShell(),
                  getString("ra.generate.general.workmanager.group.add.title"), defaultGroupsMap);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }
            String value = dialog.getValues().get(groupKey);
            if (value != null && value.length() > 0)
            {
               if (!initialDefaultGroups.contains(value))
               {
                  initialDefaultGroups.add(value);
                  contentProvider.add(value);
               }
            }
            defaultGrpsContainer.update();
         }
      });
      final Button editBtn = UIResources.createPushButton(buttonGroup, getString("command.edit.name"));
      editBtn.setEnabled(false);
      editBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) defaultGroupsTableViewer.getSelection();
            String initialValue = (String) selection.getFirstElement();

            defaultGroupsMap.put(groupKey, initialValue);
            StringInputDialog dialog = new StringInputDialog(defaultGroupsTableViewer.getControl().getShell(),
                  getString("ra.generate.general.workmanager.group.edit.title"), defaultGroupsMap);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }

            String value = dialog.getValues().get(groupKey);
            if (value != null && value.length() > 0 && !value.equals(initialValue))
            {
               int idx = initialDefaultGroups.indexOf(initialValue);
               if (idx != -1)
               {
                  initialDefaultGroups.set(idx, value); // update data
                  // update UI
                  contentProvider.update(initialValue, value);
               }
            }
            defaultGrpsContainer.update();
         }
      });
      final Button delBtn = UIResources.createPushButton(buttonGroup, getString("command.remove.name"));
      delBtn.setEnabled(false);
      delBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) defaultGroupsTableViewer.getSelection();
            if (selection != null)
            {
               String value = (String) selection.getFirstElement();
               if (initialDefaultGroups.remove(value))
               {
                  contentProvider.remove(value);
               }
            }
            defaultGrpsContainer.update();
         }
      });

      defaultGroupsTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {

         @Override
         public void selectionChanged(SelectionChangedEvent event)
         {
            editBtn.setEnabled(defaultGroupsTableViewer.getSelection() != null
                  && !defaultGroupsTableViewer.getSelection().isEmpty());
            delBtn.setEnabled(defaultGroupsTableViewer.getSelection() != null
                  && !defaultGroupsTableViewer.getSelection().isEmpty());
         }
      });

      // user mappings
      Label userMappings = new Label(whole, SWT.NULL);
      switchEnabledOnVersion.add(userMappings);
      userMappings.setText(getString("ra.generate.general.workmanager.user.mappings"));

      final Composite userMappingsContainer = new Composite(whole, SWT.NONE);
      GridLayout userMappingLayout = new GridLayout();
      userMappingLayout.numColumns = 2;
      userMappingLayout.verticalSpacing = 5;
      userMappingsContainer.setLayout(userMappingLayout);

      String userMappingTabHeaders[] =
      {getString("ra.generate.general.workmanager.table.mappings.from"),
            getString("ra.generate.general.workmanager.table.mappings.to")};
      final ColumnLayoutData[] userMappingLayoutData =
      {new ColumnWeightData(150), new ColumnWeightData(150)};

      final TableViewer userMappingTabViewer = UIResources.createTableViewer(userMappingsContainer,
            userMappingTabHeaders, userMappingLayoutData);
      List<String[]> initialValues = new ArrayList<String[]>();
      switchEnabledOnVersion.add(userMappingTabViewer.getControl());
      Map<String, String> initialUserMaps = getInitialUserMaps();
      if (initialUserMaps != null)
      {
         for (Map.Entry<String, String> entry : initialUserMaps.entrySet())
         {
            String[] rowLine = new String[2];
            rowLine[0] = entry.getKey();
            rowLine[1] = entry.getValue();
            initialValues.add(rowLine);
         }
      }

      final StringValuesContentProvider<String[]> userMappingContentProvider = 
            new StringValuesContentProvider<String[]>(userMappingTabViewer, initialValues);
      userMappingTabViewer.setContentProvider(userMappingContentProvider);

      buttonGroup = new Composite(userMappingsContainer, SWT.NONE);
      layoutBtns = new GridLayout();
      layoutBtns.marginHeight = 0;
      layoutBtns.marginWidth = 0;
      buttonGroup.setLayout(layoutBtns);
      buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
      buttonGroup.setFont(buttonGroup.getFont());

      final Map<String, String> userMap = raConfig.getWorkManagerConfig().getUserMap();
      //      userMap.putAll(initialUserMaps);

      final Map<String, String> userMapsData = new LinkedHashMap<String, String>();
      final String userMapKeyFrom = "ra.generate.general.workmanager.table.mappings.from";
      final String userMapKeyTo = "ra.generate.general.workmanager.table.mappings.to";

      final Button addUserMapBtn = UIResources.createPushButton(buttonGroup, getString("command.add.name"));
      this.switchEnabledOnVersion.add(addUserMapBtn);
      addUserMapBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            userMapsData.put(userMapKeyFrom, "");
            userMapsData.put(userMapKeyTo, "");
            StringInputDialog dialog = new StringInputDialog(userMappingTabViewer.getControl().getShell(),
                  getString("ra.generate.general.workmanager.usermap.add.title"), userMapsData);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }
            String from = dialog.getValues().get(userMapKeyFrom);
            String to = dialog.getValues().get(userMapKeyTo);
            if (from != null && from.length() > 0)
            {
               userMap.put(from, to);
               userMappingContentProvider.add(new String[]
               {from, to});
            }
            userMappingsContainer.update();
         }
      });
      final Button editUserMapBtn = UIResources.createPushButton(buttonGroup, getString("command.edit.name"));
      editUserMapBtn.setEnabled(false);
      editUserMapBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) userMappingTabViewer.getSelection();
            String[] initialValue = (String[]) selection.getFirstElement();

            userMapsData.put(userMapKeyFrom, initialValue[0]);
            userMapsData.put(userMapKeyTo, initialValue[1]);

            StringInputDialog dialog = new StringInputDialog(userMappingTabViewer.getControl().getShell(),
                  getString("ra.generate.general.workmanager.usermap.edit.title"), userMapsData);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }

            String from = dialog.getValues().get(userMapKeyFrom);
            String to = dialog.getValues().get(userMapKeyTo);
            if (from != null && from.length() > 0 && to != null && to.length() > 0)
            {
               if (from.equals(initialValue[0]) && to.equals(initialValue[1]))
               {
                  return;
               }
               userMap.put(from, to);
               userMappingContentProvider.update(initialValue, new String[]
               {from, to});
            }
            userMappingsContainer.update();
         }
      });
      final Button delUserMapBtn = UIResources.createPushButton(buttonGroup, getString("command.remove.name"));
      delUserMapBtn.setEnabled(false);
      delUserMapBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) userMappingTabViewer.getSelection();
            if (selection != null)
            {
               String[] value = (String[]) selection.getFirstElement();

               if (userMap.containsKey(value[0]))
               {
                  userMap.remove(value[0]);
                  userMappingContentProvider.remove(value);
               }
            }
            userMappingsContainer.update();
         }
      });

      userMappingTabViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {

         @Override
         public void selectionChanged(SelectionChangedEvent event)
         {
            editUserMapBtn.setEnabled(userMappingTabViewer.getSelection() != null
                  && !userMappingTabViewer.getSelection().isEmpty());
            delUserMapBtn.setEnabled(userMappingTabViewer.getSelection() != null
                  && !userMappingTabViewer.getSelection().isEmpty());
         }
      });

      // group mappings
      Label groupMappings = new Label(whole, SWT.NULL);
      switchEnabledOnVersion.add(groupMappings);
      groupMappings.setText(getString("ra.generate.general.workmanager.group.mappings"));

      final Composite groupMappingsContainer = new Composite(whole, SWT.NONE);
      GridLayout groupMappingLayout = new GridLayout();
      groupMappingLayout.numColumns = 2;
      groupMappingLayout.verticalSpacing = 5;
      groupMappingsContainer.setLayout(groupMappingLayout);

      String groupMappingTabHeaders[] =
      {getString("ra.generate.general.workmanager.table.mappings.from"),
            getString("ra.generate.general.workmanager.table.mappings.to")};
      final ColumnLayoutData[] groupMappingLayoutData =
      {new ColumnWeightData(150), new ColumnWeightData(150)};

      final TableViewer groupMappingTabViewer = UIResources.createTableViewer(groupMappingsContainer,
            groupMappingTabHeaders, groupMappingLayoutData);
      initialValues = new ArrayList<String[]>();
      switchEnabledOnVersion.add(groupMappingTabViewer.getControl());
      Map<String, String> initialGroupMaps = getInitialGroupMaps();
      if (initialGroupMaps != null)
      {
         for (Map.Entry<String, String> entry : initialGroupMaps.entrySet())
         {
            String[] rowLine = new String[2];
            rowLine[0] = entry.getKey();
            rowLine[1] = entry.getValue();
            initialValues.add(rowLine);
         }
      }

      final StringValuesContentProvider<String[]> groupMappingContentProvider = 
            new StringValuesContentProvider<String[]>(groupMappingTabViewer, initialValues);
      groupMappingTabViewer.setContentProvider(groupMappingContentProvider);

      buttonGroup = new Composite(groupMappingsContainer, SWT.NONE);
      layoutBtns = new GridLayout();
      layoutBtns.marginHeight = 0;
      layoutBtns.marginWidth = 0;
      buttonGroup.setLayout(layoutBtns);
      buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
      buttonGroup.setFont(buttonGroup.getFont());

      final Map<String, String> groupMap = raConfig.getWorkManagerConfig().getGroupMap();
      //      groupMap.putAll(initialGroupMaps);

      final Map<String, String> groupMapsData = new LinkedHashMap<String, String>();
      final String groupMapKeyFrom = "ra.generate.general.workmanager.table.mappings.from";
      final String groupMapKeyTo = "ra.generate.general.workmanager.table.mappings.to";

      final Button addGroupMapBtn = UIResources.createPushButton(buttonGroup, getString("command.add.name"));
      switchEnabledOnVersion.add(addGroupMapBtn);
      addGroupMapBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            groupMapsData.put(groupMapKeyFrom, "");
            groupMapsData.put(groupMapKeyTo, "");
            StringInputDialog dialog = new StringInputDialog(groupMappingTabViewer.getControl().getShell(),
                  getString("ra.generate.general.workmanager.groupmap.add.title"), groupMapsData);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }
            String from = dialog.getValues().get(groupMapKeyFrom);
            String to = dialog.getValues().get(groupMapKeyTo);
            if (from != null && from.length() > 0)
            {
               groupMap.put(from, to);
               groupMappingContentProvider.add(new String[]
               {from, to});
            }
            groupMappingsContainer.update();
         }
      });
      final Button editGroupMapBtn = UIResources.createPushButton(buttonGroup, getString("command.edit.name"));
      editGroupMapBtn.setEnabled(false);
      editGroupMapBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) groupMappingTabViewer.getSelection();
            String[] initialValue = (String[]) selection.getFirstElement();

            groupMapsData.put(groupMapKeyFrom, initialValue[0]);
            groupMapsData.put(groupMapKeyTo, initialValue[1]);

            StringInputDialog dialog = new StringInputDialog(groupMappingTabViewer.getControl().getShell(),
                  getString("ra.generate.general.workmanager.groupmap.edit.title"), groupMapsData);
            if (dialog.open() == Window.CANCEL)
            {
               return;
            }

            String from = dialog.getValues().get(groupMapKeyFrom);
            String to = dialog.getValues().get(groupMapKeyTo);
            if (from != null && from.length() > 0 && to != null && to.length() > 0)
            {
               if (from.equals(initialValue[0]) && to.equals(initialValue[1]))
               {
                  return;
               }
               groupMap.put(from, to);
               groupMappingContentProvider.update(initialValue, new String[]
               {from, to});
            }
            groupMappingsContainer.update();
         }
      });
      final Button delGroupMapBtn = UIResources.createPushButton(buttonGroup, getString("command.remove.name"));
      delGroupMapBtn.setEnabled(false);
      delGroupMapBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) groupMappingTabViewer.getSelection();
            if (selection != null)
            {
               String[] value = (String[]) selection.getFirstElement();

               if (groupMap.containsKey(value[0]))
               {
                  groupMap.remove(value[0]);
                  groupMappingContentProvider.remove(value);
               }
            }
            groupMappingsContainer.update();
         }
      });

      groupMappingTabViewer.addSelectionChangedListener(new ISelectionChangedListener()
      {

         @Override
         public void selectionChanged(SelectionChangedEvent event)
         {
            editGroupMapBtn.setEnabled(groupMappingTabViewer.getSelection() != null
                  && !groupMappingTabViewer.getSelection().isEmpty());
            delGroupMapBtn.setEnabled(groupMappingTabViewer.getSelection() != null
                  && !groupMappingTabViewer.getSelection().isEmpty());
         }
      });

      return whole;
   }

   private Map<String, String> getInitialGroupMaps()
   {
      if (this.workManagerConfig != null)
      {
         return this.workManagerConfig.getGroupMap();
      }
      return null;
   }

   private Map<String, String> getInitialUserMaps()
   {
      if (this.workManagerConfig != null)
      {
         return this.workManagerConfig.getUserMap();
      }
      return null;
   }

   private List<String> getDefaultGroups()
   {
      if (this.workManagerConfig != null)
      {
         return this.workManagerConfig.getDefaultGroups();
      }
      return null;
   }

   private String getInitialPrincipal()
   {
      if (this.workManagerConfig != null)
      {
         return this.workManagerConfig.getDefaultPricipal();
      }
      return null;
   }

   private String getInitialDomain()
   {
      if (this.workManagerConfig != null)
      {
         return this.workManagerConfig.getDomain();
      }
      return null;
   }

   private Boolean getInitialMappingRequired()
   {
      if (this.workManagerConfig != null)
      {
         Boolean mappingRequired = this.workManagerConfig.isMappingRequired();
         if (mappingRequired != null)
         {
            return mappingRequired;
         }
      }
      return Boolean.FALSE;
   }

   private GridData gridData(int width)
   {
      GridData gridData = new GridData();
      gridData.widthHint = width;
      return gridData;
   }

   private Composite createGeneralControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      whole.setLayout(layout);
      layout.numColumns = 1;

      final Group generalGrp = new Group(whole, SWT.SHADOW_IN);
      generalGrp.setText(getString("ra.generate.general.group"));

      layout = new GridLayout();
      generalGrp.setLayout(layout);
      layout.numColumns = 2;
      layout.verticalSpacing = 9;
      layout.horizontalSpacing = 100;

      // archive
      Label label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.general.archive.label"));
      Label archiveLavel = new Label(generalGrp, SWT.NULL);
      archiveLavel.setText(raConfig.getArchive());

      // enable id if version is 1.1
      Label idLabel = new Label(generalGrp, SWT.NULL);
      idLabel.setText(getString("ra.generate.general.id"));

      final Text idText = new Text(generalGrp, SWT.BORDER | SWT.SINGLE);
      GridData gridData = new GridData();
      gridData.widthHint = 100;
      idText.setLayoutData(gridData);
      idText.setText(raConfig.getId() == null ? raConfig.getArchive() : raConfig.getId());
      idText.addModifyListener(new ModifyListener()
      {

         @Override
         public void modifyText(ModifyEvent e)
         {
            raConfig.setId(idText.getText());
         }
      });
      switchEnabledOnVersion.add(idLabel);
      switchEnabledOnVersion.add(idText);

      // transaction support
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.general.transaction"));
      final String[] items =
      {TransactionSupportEnum.NoTransaction.name(), TransactionSupportEnum.LocalTransaction.name(),
            TransactionSupportEnum.XATransaction.name()};
      final Combo combo = new Combo(generalGrp, SWT.DROP_DOWN | SWT.READ_ONLY);
      combo.setItems(items);

      // TransactionSupport has been merged in connector
      final TransactionSupportEnum currentTransactionSupport = this.raConfig.getTransactionSupport();

      combo.setText(currentTransactionSupport.name());
      combo.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            TransactionSupportEnum transactionSupport = TransactionSupportEnum.valueOf(combo.getText());
            raConfig.setTransactionSupport(transactionSupport);
         }
      });

      // bootstrap-context
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.general.bootstrap"));
      final Text bootStrapContext = new Text(generalGrp, SWT.BORDER | SWT.SINGLE);
      GridData data = new GridData();
      data.widthHint = 300;
      bootStrapContext.setLayoutData(data);
      final String currentBoolStrapContext = this.raConfig.getBootstrapContext();
      bootStrapContext.setText(currentBoolStrapContext == null ? "" : currentBoolStrapContext);
      bootStrapContext.addModifyListener(new ModifyListener()
      {

         @Override
         public void modifyText(ModifyEvent e)
         {
            raConfig.setBootstrapContext(bootStrapContext.getText().trim());
         }
      });

      Label beanValidLabel = new Label(whole, SWT.NULL);
      beanValidLabel.setText(getString("ra.generate.general.bean.validate.group"));

      final Composite beanValidContainer = new Composite(whole, SWT.NONE);
      GridLayout beanValidLayout = new GridLayout();
      beanValidLayout.numColumns = 2;
      beanValidLayout.verticalSpacing = 9;

      beanValidContainer.setLayout(beanValidLayout);

      // bean validation groups
      String fTableColumnHeaders[] =
      {getString("ra.generate.general.bean.validate.group.table.header")};
      final ColumnLayoutData[] fTableColumnLayouts =
      {new ColumnWeightData(300)};

      final TableViewer beanValidTableViewer = UIResources.createTableViewer(beanValidContainer, fTableColumnHeaders,
            fTableColumnLayouts);
      List<String> initialBeanValidationGroups = this.raConfig.getBeanValidationGroups();
      final StringValuesContentProvider<String> contentProvider = new StringValuesContentProvider<String>(
            beanValidTableViewer, initialBeanValidationGroups);
      beanValidTableViewer.setContentProvider(contentProvider);

      Composite buttonGroup = new Composite(beanValidContainer, SWT.NONE);
      GridLayout layoutBtns = new GridLayout();
      layoutBtns.marginHeight = 0;
      layoutBtns.marginWidth = 0;
      buttonGroup.setLayout(layoutBtns);
      buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL));
      buttonGroup.setFont(buttonGroup.getFont());
      final Button addBtn = UIResources.createPushButton(buttonGroup, getString("command.add.name"));
      addBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            BeanValidateGroupDialog dialog = new BeanValidateGroupDialog(beanValidTableViewer.getControl().getShell(),
                  getString("ra.generate.general.bean.validate.group.dialog.add.title"), null);
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
      final Button editBtn = UIResources.createPushButton(buttonGroup, getString("command.edit.name"));
      editBtn.setEnabled(false);
      editBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) beanValidTableViewer.getSelection();
            String initialValue = (String) selection.getFirstElement();
            BeanValidateGroupDialog dialog = new BeanValidateGroupDialog(beanValidTableViewer.getControl().getShell(),
                  getString("ra.generate.general.bean.validate.group.dialog.edit.title"), initialValue);
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
      final Button delBtn = UIResources.createPushButton(buttonGroup, getString("command.remove.name"));
      delBtn.setEnabled(false);
      delBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            IStructuredSelection selection = (IStructuredSelection) beanValidTableViewer.getSelection();
            if (selection != null)
            {
               String beanValidGrp = (String) selection.getFirstElement();
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
            editBtn.setEnabled(beanValidTableViewer.getSelection() != null
                  && !beanValidTableViewer.getSelection().isEmpty());
            delBtn.setEnabled(beanValidTableViewer.getSelection() != null
                  && !beanValidTableViewer.getSelection().isEmpty());
         }
      });

      ConfigPropertyComposite configPropComposite = new ConfigPropertyComposite(getShell(),
            this.raConfig.getConfigProperties());
      configPropComposite.createControl(whole);
      return whole;
   }

   @Override
   public void createControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout wholeLayout = new GridLayout();
      wholeLayout.numColumns = 1;
      wholeLayout.verticalSpacing = 6;
      whole.setLayout(wholeLayout);

      TabFolder tabContainer = new TabFolder(whole, SWT.NONE);

      // general tab
      TabItem generalTab = new TabItem(tabContainer, SWT.NULL);
      generalTab.setText(getString("ra.generate.mcf.general.title"));
      generalTab.setControl(createGeneralControl(tabContainer));

      // security tab
      TabItem securityTab = new TabItem(tabContainer, SWT.NULL);
      securityTab.setText(getString("ra.generate.general.workmanager.security.title"));
      securityTab.setControl(createWorkManagerSecurity(tabContainer));
      setControl(whole);

      this.switchEnabledOnVersion.add(securityTab.getControl());
      versionChanged(raConfig.getVersion());
   }
}
