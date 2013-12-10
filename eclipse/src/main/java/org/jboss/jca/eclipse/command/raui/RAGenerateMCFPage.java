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

import org.jboss.jca.codegenerator.ConfigPropType;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.CapacityConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.Extension;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.PoolConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.RecoveryConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.SecurityConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.TimeoutConfig;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.ValidationConfig;
import org.jboss.jca.eclipse.command.raui.ResourceAdapterConfig.VERSION;

import java.util.ArrayList;
import java.util.List;

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
 * Wizard page used to genereate Managed Connection Factory.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAGenerateMCFPage extends AbstractRAGenerateWizardPage 
       implements ResourceAdapterConfig.VersionChangeListener
{
   
   private final ConnectionFactoryConfig connFactoryConfig;

   private Button xaPoolBtn;
   
   private TabFolder tabContainer;
   
   // input components which need to be valid
   
   private Text jndiText;
   private Button useJavaCtxBtn;
   
   private Text userNameText;
   
   private Text recoverySecurityDomainText;
   
   private Text recoveryClsNameText;
   
   // security related components
   private Button appBtn;
   
   private Text securityDomainText;
   
   private Text securityDomainAndAppText;
   
   // pool components
   private Text minPoolSizeText;
   private Text initialPoolSizeText;
   private Text maxPoolSizeText;
   private Combo flushStrategyCombo;
   private Group xaPoolGroup;
   
   // timeout components
   private Text blockingTimeoutText;
   private Text idleTimeoutText;
   private Text allocateRetryText;
   private Text allocateRetryWaitText;
   private Text xaResTimeoutText;
   
   // validation components
   private Text backgroundValidText;
   
   private List<Control> versionEnabledControls = new ArrayList<Control>();
   
   /**
    * The constructor.
    * 
    * @param connConfig the ConnectionFactoryConfig.
    */
   public RAGenerateMCFPage(ConnectionFactoryConfig connConfig)
   {
      super("MCFPage");
      this.connFactoryConfig = connConfig;
      setTitle(getString("ra.generate.mcf.title"));
      setDescription(getString("ra.generate.mcf.description", this.connFactoryConfig.getMcfClsName()));
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
      for (Control control: this.versionEnabledControls)
      {
         control.setEnabled(enabled);
      }
   }
   
   @Override
   public void createControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout wholeLayout = new GridLayout();
      wholeLayout.numColumns = 1;
      wholeLayout.verticalSpacing = 9;
      whole.setLayout(wholeLayout);
      
      Composite container = new Composite(whole, SWT.NONE);
      GridLayout activeLayout = new GridLayout();
      activeLayout.numColumns = 2;
      activeLayout.verticalSpacing = 9;
      activeLayout.horizontalSpacing = 100;
      container.setLayout(activeLayout);
      
      // activate it?
      Label label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.active.label"));
      
      final Button activeBtn = new Button(container, SWT.CHECK);
      
      activeBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            boolean active = activeBtn.getSelection();
            updateComponents(active);
            connFactoryConfig.setActive(active);
         }

      });
      activeBtn.setSelection(this.connFactoryConfig.isActive());
      
      tabContainer = new TabFolder(whole, SWT.NONE);
      tabContainer.setEnabled(this.connFactoryConfig.isActive());
      
      // general tab
      TabItem generalTab = new TabItem(tabContainer, SWT.NULL);
      generalTab.setText(getString("ra.generate.mcf.general.title"));
      generalTab.setControl(createGeneralControl(tabContainer));
      
      // pool tab
      TabItem poolTab = new TabItem(tabContainer, SWT.NULL);
      poolTab.setText(getString("ra.generate.mcf.pool.title"));
      poolTab.setControl(createPoolControl(tabContainer));
      
      // security tab
      TabItem securityTab = new TabItem(tabContainer, SWT.NULL);
      securityTab.setText(getString("ra.generate.mcf.security.title"));
      securityTab.setControl(createSecuriyControl(tabContainer));
      
      // timeout tab
      TabItem timeoutTab = new TabItem(tabContainer, SWT.NULL);
      timeoutTab.setText(getString("ra.generate.mcf.timeout.title"));
      timeoutTab.setControl(createTimeoutControl(tabContainer));
      
      // validation tab
      TabItem validationTab = new TabItem(tabContainer, SWT.NULL);
      validationTab.setText(getString("ra.generate.mcf.validation.title"));
      validationTab.setControl(createValidationControl(tabContainer));
      
      // recover tab
      TabItem recoverTab = new TabItem(tabContainer, SWT.NULL);
      recoverTab.setText(getString("ra.generate.mcf.recover.title"));
      recoverTab.setControl(createRecoverControl(tabContainer));
      
      setControl(whole);
   }
   
   /**
    * Creates general control
    * 
    * @param parent the parent
    * @return the control
    */
   private Composite createGeneralControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 20;
      whole.setLayout(layout);
      
      Group generalGrp = new Group(whole, SWT.SHADOW_IN);
      generalGrp.setText(getString("ra.generate.general.group"));
      generalGrp.setLayout(getLayout());

      // mcf class name
      Label label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.mcf.general.mcf.class"));
      Label mcfClsLabel = new Label(generalGrp, SWT.NULL);
      String mcfClassName = this.connFactoryConfig.getMcfClsName();
      mcfClsLabel.setText(mcfClassName != null ? mcfClassName : "");
      
      // jndi name
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.jndi.name"));
      final String jndiName = getInitialJndiName();
      jndiText = UIResources.createText(generalGrp, jndiName);
      jndiText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkGeneralInput())
            {
               connFactoryConfig.setMcfJndiName(jndiText.getText().trim());
            }
         }
      });
      
      // pool name
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.pool.name"));
      final String poolName = getInitialPoolName();
      final Text poolText = UIResources.createText(generalGrp, poolName);
      poolText.addModifyListener(new ModifyListener()
      {
         @Override
         public void modifyText(ModifyEvent e)
         {
            String pool = poolText.getText().trim();
            connFactoryConfig.setMcfPoolName(pool);
         }
      });
      
      // enabled
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.enabled"));
      final Button enabledBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      final Boolean isEnabled = isMcfEnabled();
      enabledBtn.setSelection(isEnabled);
      enabledBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            connFactoryConfig.setMcfEnabled(Boolean.valueOf(enabledBtn.getSelection()));
         }
      });
      
      // use java context
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.use.java.context"));
      useJavaCtxBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      final Boolean useJavaCtx = isMcfUseJavaCtx();
      useJavaCtxBtn.setSelection(useJavaCtx);
      useJavaCtxBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (checkGeneralInput())
            {
               connFactoryConfig.setMcfUseJavaCtx(useJavaCtxBtn.getSelection());
            }
         }
      });
      
      // use ccm
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.mcf.general.use.ccm"));
      final Button useCcmBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      final Boolean useCcm = isMcfUseCCM();
      useCcmBtn.setSelection(useCcm);
      useCcmBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            connFactoryConfig.setMcfUseCCM(useCcmBtn.getSelection());
         }
      });
      
      // sharable
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.mcf.general.sharable"));
      this.versionEnabledControls.add(label);
      final Button sharableBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      this.versionEnabledControls.add(sharableBtn);
      
      final Boolean sharable = isMcfSharable();
      sharableBtn.setSelection(sharable);
      sharableBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            connFactoryConfig.setSharable(sharableBtn.getSelection());
         }
      });
      
      // enlistment
      label = new Label(generalGrp, SWT.NULL);
      versionEnabledControls.add(label);
      label.setText(getString("ra.generate.mcf.general.enlistment"));
      final Button enlistBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      versionEnabledControls.add(enlistBtn);
      final Boolean enlist = isMcfEnlist();
      enlistBtn.setSelection(enlist);
      enlistBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            connFactoryConfig.setEnlistment(enlistBtn.getSelection());
         }
      });
      
      ConfigPropertyComposite configPropComposite = new ConfigPropertyComposite(getShell(), 
            this.connFactoryConfig.getMcfConfigProps());
      configPropComposite.createControl(whole);
      
      return whole;
   }
   
   private Boolean isMcfEnlist()
   {
      Boolean enlistment = this.connFactoryConfig.getEnlistment();
      return enlistment == null ? Boolean.valueOf(false) : enlistment;
   }

   private Boolean isMcfSharable()
   {
      Boolean sharable = this.connFactoryConfig.getSharable();
      return sharable == null ? Boolean.valueOf(false) : sharable;
   }

   /**
    * Creates Pool configuration control
    * 
    * @param parent the parent
    * @return the control
    */
   private Composite createPoolControl(Composite parent)
   {
      TabFolder poolTab = new TabFolder(parent, SWT.NONE);
      
      // general tab
      TabItem poolTabItem = new TabItem(poolTab, SWT.NONE);
      poolTabItem.setText(getString("ra.generate.mcf.general.title"));
      Composite whole = new Composite(poolTab, SWT.NONE);
      poolTabItem.setControl(whole);
      
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      layout.verticalSpacing = 9;
      whole.setLayout(layout);
      
      // pool
      final Composite container = new Composite(whole, SWT.None);
      GridLayout containerLayout = new GridLayout();
      containerLayout.numColumns = 2;
      containerLayout.verticalSpacing = 9;
      containerLayout.makeColumnsEqualWidth = true;
      container.setLayout(containerLayout);
      
      // min-pool-size
      Label label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.min.pool.size"));
      final Integer minPoolSize = getMinPoolSize();
      minPoolSizeText = UIResources.createText(container, minPoolSize);
      minPoolSizeText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkPoolInput())
            {
               String minPool = minPoolSizeText.getText().trim();
               if (!minPool.isEmpty())
               {
                  getPoolConfig().setMinPoolSize(Integer.valueOf(minPool));
               }
               else
               {
                  getPoolConfig().setMinPoolSize(null);
               }
            }
         }
      });
      
      // initial-pool-size for version 1.1
      label = new Label(container, SWT.NULL);
      versionEnabledControls.add(label);
      label.setText(getString("ra.generate.mcf.pool.initial.pool.size"));
      final Integer initialPoolSize = getInitialPoolSize();
      initialPoolSizeText = UIResources.createText(container, initialPoolSize);
      versionEnabledControls.add(initialPoolSizeText);
      initialPoolSizeText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkPoolInput())
            {
               String initialPool = initialPoolSizeText.getText().trim();
               if (!initialPool.isEmpty())
               {
                  Integer newSize = Integer.valueOf(initialPool);
                  getPoolConfig().setInitialPoolSize(newSize);
               }
               else
               {
                  getPoolConfig().setInitialPoolSize(null);
               }
            }
         }
      });

      // max-pool-size
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.max.pool.size"));
      final Integer maxPoolSize = getMaxPoolSize();
      maxPoolSizeText = UIResources.createText(container, maxPoolSize);
      maxPoolSizeText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkPoolInput())
            {
               String maxPool = maxPoolSizeText.getText().trim();
               if (!maxPool.isEmpty())
               {
                  Integer newSize = Integer.valueOf(maxPool);
                  getPoolConfig().setMaxPoolSize(newSize);
               }
               else
               {
                  getPoolConfig().setMaxPoolSize(null);
               }
            }
         }
      });
      
      // prefill
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.prefill"));
      final Button prefillBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean prefill = isPoolPrefill();
      prefillBtn.setSelection(prefill);
      prefillBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setPrefill(prefillBtn.getSelection());
         }
      });
      
      // use-strict-min
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.strict.min.pool.size"));
      final Button useStrictMinBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean useStrictMin = isPoolUseStictMin();
      useStrictMinBtn.setSelection(useStrictMin);
      useStrictMinBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setUseStrictMin(useStrictMinBtn.getSelection());
         }
      });
      
      // flush strategy 
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.flush.stragery"));
      flushStrategyCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      String[] items = new String[]{FlushStrategy.FAILING_CONNECTION_ONLY.getName(), 
            FlushStrategy.IDLE_CONNECTIONS.getName(), FlushStrategy.ENTIRE_POOL.getName()};
      flushStrategyCombo.setItems(items);
      final FlushStrategy flushStrategy = getPoolFlushStrategy();
      getPoolConfig().setFlushStrategy(flushStrategy);
      flushStrategyCombo.setText(flushStrategy.getName());
      flushStrategyCombo.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setFlushStrategy(FlushStrategy.forName(flushStrategyCombo.getText()));
         }
      });
      
      // capacity tab
      TabItem capacityTabItem = new TabItem(poolTab, SWT.NONE);
      capacityTabItem.setText(getString("ra.generate.mcf.pool.capacity.title"));
      final Composite capacityTabWhole = new Composite(poolTab, SWT.NONE);
      capacityTabItem.setControl(capacityTabWhole);
      this.versionEnabledControls.add(capacityTabWhole);
      
      GridLayout capacityTabLayout = new GridLayout();
      capacityTabLayout.numColumns = 1;
      capacityTabLayout.verticalSpacing = 9;
      capacityTabWhole.setLayout(capacityTabLayout);
      
      Group incrementerGroup = new Group(capacityTabWhole, SWT.NONE);
      GridLayout increGroupLayout = new GridLayout();
      increGroupLayout.numColumns = 1;
      increGroupLayout.verticalSpacing = 9;
      incrementerGroup.setLayout(increGroupLayout);
      this.versionEnabledControls.add(incrementerGroup);
      incrementerGroup.setText(getString("ra.generate.mcf.pool.capacity.increment"));
      ExtensionComposite incrExtension = new ExtensionComposite(getShell(), getIncrementer());
      this.versionEnabledControls.add(incrExtension.createControl(incrementerGroup));
      
      Group decrementerGroup = new Group(capacityTabWhole, SWT.NONE);
      this.versionEnabledControls.add(decrementerGroup);
      GridLayout decreGroupLayout = new GridLayout();
      decreGroupLayout.numColumns = 1;
      decreGroupLayout.verticalSpacing = 9;
      decrementerGroup.setLayout(decreGroupLayout);
      decrementerGroup.setText(getString("ra.generate.mcf.pool.capacity.decrement"));
      ExtensionComposite decrExtension = new ExtensionComposite(getShell(), getDecrementer());
      this.versionEnabledControls.add(decrExtension.createControl(decrementerGroup));
      
      // xa pool tab
      TabItem xaPoolTabItem = new TabItem(poolTab, SWT.NONE);
      xaPoolTabItem.setText(getString("ra.generate.mcf.pool.xa.title"));
      final Composite xaPoolContainerWhole = new Composite(poolTab, SWT.NONE);
      xaPoolTabItem.setControl(xaPoolContainerWhole);
      
      GridLayout xaPoolWholeLayout = new GridLayout();
      xaPoolWholeLayout.numColumns = 1;
      xaPoolWholeLayout.verticalSpacing = 9;
      xaPoolContainerWhole.setLayout(xaPoolWholeLayout);
      
      // define a xa pool ?
      final Composite xaPoolContainer = new Composite(xaPoolContainerWhole, SWT.NONE);
      GridLayout xaPoolContainerLayout = new GridLayout();
      xaPoolContainerLayout.numColumns = 2;
      xaPoolContainerLayout.verticalSpacing = 9;
      xaPoolContainerLayout.makeColumnsEqualWidth = true;
      xaPoolContainer.setLayout(xaPoolContainerLayout);
      
      label = new Label(xaPoolContainer, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.define.xa"));
      xaPoolBtn = new Button(xaPoolContainer, SWT.BORDER | SWT.CHECK);
      xaPoolBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            xaPoolGroup.setVisible(xaPoolBtn.getSelection());
            xaPoolContainer.update();
            getPoolConfig().setDefineXA(xaPoolBtn.getSelection());
         }
      });
      
      xaPoolBtn.setSelection(getPoolConfig().getDefineXA());
      
      xaPoolGroup = new Group(xaPoolContainerWhole, SWT.NONE);
      xaPoolGroup.setText(getString("ra.generate.mcf.pool.xa.title"));
      GridLayout xaPoolGroupLayout = new GridLayout();
      xaPoolGroupLayout.numColumns = 2;
      xaPoolWholeLayout.horizontalSpacing = 108;
      xaPoolGroupLayout.makeColumnsEqualWidth = true;
      
      xaPoolGroup.setLayout(xaPoolGroupLayout);
      
      // is-same-rm-override
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.xa.overide.issamerm"));
      final Button isSameRMOverideBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean isSameRMOverride = isXAPoolRMOverride();
      isSameRMOverideBtn.setSelection(isSameRMOverride);
      isSameRMOverideBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setOverrideIsSameRM(isSameRMOverideBtn.getSelection());
         }
      });
      
      // interleaving
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.xa.interleaving"));
      final Button interLeavingBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean interLeaving = isXAPoolInterleaving();
      interLeavingBtn.setSelection(interLeaving);
      interLeavingBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setInterleaving(interLeavingBtn.getSelection());
         }
      });
      
      // no-tx-separate-pools
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.xa.seperate.pool"));
      final Button noTxSeparatePoolBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean noTxSepPool = isXAPoolNoTxSeparatePool();
      noTxSeparatePoolBtn.setSelection(noTxSepPool);
      noTxSeparatePoolBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setNoTxSeparatePool(noTxSeparatePoolBtn.getSelection());
         }
      });
      
      // pad-xid
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.xa.pad.xid"));
      final Button padXidBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean padXid = isXAPoolPadXid();
      padXidBtn.setSelection(padXid);
      padXidBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setPadXid(padXidBtn.getSelection());
         }
      });
      
      // wrap xa resource
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.pool.xa.wrap.xa.res"));
      final Button wrapXaResBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean wrapXA = isXAPoolWrapXARes();
      wrapXaResBtn.setSelection(wrapXA);
      wrapXaResBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getPoolConfig().setWrapXaResource(wrapXaResBtn.getSelection());
         }
      });

      xaPoolGroup.setVisible(xaPoolBtn.getSelection());
      
      return poolTab;
   }
   
   private Extension getDecrementer()
   {
      CapacityConfig capacityConfig = getCapacityConfig();
      Extension decrementer = capacityConfig.getDecrementer();
      if (decrementer == null)
      {
         decrementer = new Extension();
         capacityConfig.setDecrementer(decrementer);
      }
      return decrementer;
   }

   private Extension getIncrementer()
   {
      CapacityConfig capacityConfig = getCapacityConfig();
      Extension incrementer = capacityConfig.getIncrementer();
      if (incrementer == null)
      {
         incrementer = new Extension();
         capacityConfig.setIncrementer(incrementer);
      }
      return incrementer;
   }
   
   private CapacityConfig getCapacityConfig()
   {
      PoolConfig poolConfig = getPoolConfig();
      CapacityConfig capacityConfig = poolConfig.getCapacityConfig();
      if (capacityConfig == null)
      {
         capacityConfig = new CapacityConfig();
         poolConfig.setCapacityConfig(capacityConfig);
      }
      return capacityConfig;
   }
   
   /**
    * Creates security control.
    * 
    * @param parent the parent
    * @return the control
    */
   private Composite createSecuriyControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      whole.setLayout(layout);

      Composite container = new Composite(whole, SWT.NONE);
      container.setLayout(getLayout());
      
      // application
      appBtn = new Button(container, SWT.RADIO);
      Label invisibleLabel = new Label(container, SWT.NULL);
      invisibleLabel.setText("");
      appBtn.setText(getString("ra.generate.mcf.security.application"));
      final Boolean app = isInitialApplication();
      appBtn.setSelection(app);
      appBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getSecurityConfig().setApplication(appBtn.getSelection());
            if (appBtn.getSelection())
            {
               // if application is selected.
               getSecurityConfig().setSecurityDomain(null);
               getSecurityConfig().setSecurityDomainAndApp(null);
               securityDomainText.setEnabled(false);
               securityDomainAndAppText.setEnabled(false);
            }
            else
            {
               // it is boolean-presenceType
               getSecurityConfig().setApplication(null);
            }
         }
      });

      // security domain
      final String securityDomain = getSecurityDomain();
      final Button securityDomainBtn = new Button(container, SWT.RADIO);
      securityDomainBtn.setText(getString("ra.generate.mcf.security.security.domain"));
      securityDomainBtn.setSelection(securityDomain != null);
      securityDomainBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getSecurityConfig().setApplication(null);
            getSecurityConfig().setSecurityDomainAndApp(null);
            securityDomainText.setEnabled(true);
            securityDomainAndAppText.setEnabled(false);
            String security = securityDomainText.getText().trim();
            getSecurityConfig().setSecurityDomain(security);
         }
      });
      
      securityDomainText = UIResources.createText(container, securityDomain);
      securityDomainText.setEnabled(securityDomainBtn.getSelection());
      securityDomainText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            securityDomainBtn.setSelection(true);
            String security = securityDomainText.getText().trim();
            getSecurityConfig().setSecurityDomain(security);
         }
      });
      
      // securiry domain and application
      final String securityDomainAndApplication = getSecurityDomainAndApplication();
      final Button secDomainAndAppBtn = new Button(container, SWT.RADIO);
      secDomainAndAppBtn.setSelection(securityDomainAndApplication != null);
      secDomainAndAppBtn.setText(getString("ra.generate.mcf.security.security.domain.and.application"));
      secDomainAndAppBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getSecurityConfig().setApplication(null);
            getSecurityConfig().setSecurityDomain(null);
            securityDomainText.setEnabled(false);
            securityDomainAndAppText.setEnabled(true);
            String securityAndApp = securityDomainAndAppText.getText().trim();
            getSecurityConfig().setSecurityDomainAndApp(securityAndApp);
         }
      });
      
      
      securityDomainAndAppText = UIResources.createText(container, securityDomainAndApplication);
      securityDomainAndAppText.setEnabled(secDomainAndAppBtn.getSelection());
      securityDomainAndAppText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            secDomainAndAppBtn.setSelection(true);
            String securityAndApp = securityDomainAndAppText.getText().trim();
            getSecurityConfig().setSecurityDomainAndApp(securityAndApp);
         }
      });
      
      return whole;
   }
   
   /**
    * Creates timeout control.
    * 
    * @param parent the parent
    * @return the control
    */
   private Control createTimeoutControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      whole.setLayout(layout);
      
      final Composite container = new Composite(whole, SWT.None);
      container.setLayout(getLayout());
      
      // blocking timeout millis
      Label label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.timeout.blocking"));
      
      final Long blockTimeout = getBlockTimeout();
      blockingTimeoutText = UIResources.createText(container, blockTimeout);
      blockingTimeoutText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkTimeoutInput())
            {
               String newBlockTimeout = blockingTimeoutText.getText().trim();
               if (!newBlockTimeout.isEmpty())
               {
                  Long blkTimeout = Long.valueOf(newBlockTimeout);
                  getTimeoutConfig().setBlockingTimeoutMillis(blkTimeout);
               }
               else
               {
                  getTimeoutConfig().setBlockingTimeoutMillis(null);
               }
            }
         }
      });
      
      // idle timeout minutes
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.timeout.idle"));
      final Long idleTimeout = getIdleTimeout();
      idleTimeoutText = UIResources.createText(container, idleTimeout);
      idleTimeoutText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkTimeoutInput())
            {
               String newIdleTimeout = idleTimeoutText.getText().trim();
               if (!newIdleTimeout.isEmpty())
               {
                  Long idle = Long.valueOf(newIdleTimeout);
                  getTimeoutConfig().setIdleTimeoutMinutes(idle);
               }
               else
               {
                  getTimeoutConfig().setIdleTimeoutMinutes(null);
               }
            }
         }
      });
      
      // allocate retry
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.timeout.allocate.retry"));
      final Integer allocateRetry = getAllocationRetry();
      allocateRetryText = UIResources.createText(container, allocateRetry);
      allocateRetryText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkTimeoutInput())
            {
               String newAllocateRetry = allocateRetryText.getText().trim();
               if (!newAllocateRetry.isEmpty())
               {
                  Integer retry = Integer.valueOf(newAllocateRetry);
                  getTimeoutConfig().setAllocateRetry(retry);
               }
               else
               {
                  getTimeoutConfig().setAllocateRetry(null);
               }
            }
         }
      });
      
      // allocate retry wait
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.timeout.allocate.retry.wait"));
      final Long allocateRetryWait = getAllocateRetryWait();
      allocateRetryWaitText = UIResources.createText(container, allocateRetryWait);
      allocateRetryWaitText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkTimeoutInput())
            {
               String alloRetryWait = allocateRetryWaitText.getText().trim();
               if (!alloRetryWait.isEmpty())
               {
                  Long allocateRetryWaitMillis = Long.valueOf(alloRetryWait);
                  getTimeoutConfig().setAllocateRetryWait(allocateRetryWaitMillis);
               }
               else
               {
                  getTimeoutConfig().setAllocateRetryWait(null);
               }
            }
         }
      });
      
      // xa resource timeout
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.timeout.xa.resource"));
      final Integer xaResTimeout = getXAResourceTimeout();
      xaResTimeoutText = UIResources.createText(container, xaResTimeout);
      xaResTimeoutText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkTimeoutInput())
            {
               String xaResourceTimeoutStr = xaResTimeoutText.getText().trim();
               if (!xaResourceTimeoutStr.isEmpty())
               {
                  Integer xaResourceTimeout = Integer.valueOf(xaResourceTimeoutStr);
                  getTimeoutConfig().setXaResourceTimeout(xaResourceTimeout);
               }
               else
               {
                  getTimeoutConfig().setXaResourceTimeout(null);
               }
            }
         }
      });
      
      return whole;
   }
   
   /**
    * Creates Validation control.
    * 
    * @param parent the parent
    * @return the control
    */
   private Control createValidationControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      whole.setLayout(layout);
      
      final Composite container = new Composite(whole, SWT.None);
      container.setLayout(getLayout());
      
      // background validation
      Label label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.validation.background"));
      final Button backgroundValidationBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean backgroundValidation = isBackgroundValidation();
      
      
      // background validation millis
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.validation.background.validation.mills"));
      final Long backgroundValidationMills = getBackgroundValidationMillis();
      backgroundValidText = UIResources.createText(container, backgroundValidationMills);
      
      backgroundValidationBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            backgroundValidText.setEnabled(backgroundValidationBtn.getSelection());
            getValidationConfig().setBackgroundValidation(backgroundValidationBtn.getSelection());
         }
      });
      backgroundValidationBtn.setSelection(backgroundValidation);
      backgroundValidText.setEnabled(backgroundValidation);
      
      backgroundValidText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (checkValidationInput())
            {
               String bgValidationMills = backgroundValidText.getText().trim();
               if (!bgValidationMills.isEmpty())
               {
                  Long bgValidMills = Long.valueOf(bgValidationMills);
                  getValidationConfig().setBackgroundValidationMillis(bgValidMills);
               }
               else
               {
                  getValidationConfig().setBackgroundValidationMillis(null);
               }
            }
         }
      });
      
      // use fast fail
      label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.validation.use.fast.fail"));
      final Button useFastFailBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean useFastFail = isValidationUseFastFail();
      useFastFailBtn.setSelection(useFastFail);
      useFastFailBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getValidationConfig().setUseFastFail(useFastFailBtn.getSelection());
         }
      });
      
      return whole;
   }
   
   /**
    * Creates Recover control.
    * 
    * @param parent the parent
    * @return the control
    */
   private Control createRecoverControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      whole.setLayout(layout);
      
      final Composite container = new Composite(whole, SWT.None);
      container.setLayout(getLayout());
      
      // no recovery
      Label label = new Label(container, SWT.NULL);
      label.setText(getString("ra.generate.mcf.recover.no.recovery"));
      final Button noRecoveryBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean isNoRecovery = isNoRecovery();
      getRecoveryConfig().setNoRecovery(noRecoveryBtn.getSelection());
      noRecoveryBtn.setSelection(isNoRecovery);
      noRecoveryBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            getRecoveryConfig().setNoRecovery(noRecoveryBtn.getSelection());
         }
      });
      
      // recover credential
      final Group credentialGroup = new Group(whole, SWT.NONE);
      credentialGroup.setLayout(getLayout());
      credentialGroup.setText(getString("ra.generate.mcf.recover.credential.title"));
      
      // user name
      label = new Label(credentialGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.recover.credential.username"));
      final String userName = getUserName();
      userNameText = UIResources.createText(credentialGroup, userName);
      getRecoveryCredentialConfig().setUsername(userName);
      
      
      // password
      label = new Label(credentialGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.recover.credential.password"));
      String password = getPassword();
      if (null == password || password.equals(""))
      {
         password = "password";
      }
      final Text passwordText = new Text(credentialGroup, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
      getRecoveryCredentialConfig().setPassword(password);
      GridData layoutData = new GridData();
      layoutData.widthHint = 300;
      passwordText.setLayoutData(layoutData);
      if (password != null)
      {
         passwordText.setText(password);
      }
      passwordText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            getRecoveryCredentialConfig().setPassword(passwordText.getText().trim());
         }
      });
      
      // security-domain
      label = new Label(credentialGroup, SWT.NULL);
      label.setText(getString("ra.generate.mcf.recover.credential.security.domain"));
      final String securityDomain = getRecoverySecurityDomain();
      recoverySecurityDomainText = UIResources.createText(credentialGroup, securityDomain);
      
      userNameText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (isRecoveryInputValid())
            {
               String newUserName = userNameText.getText().trim();
               getRecoveryCredentialConfig().setUsername(newUserName);
            }
         }
      });
      
      recoverySecurityDomainText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (isRecoveryInputValid())
            {
               String newSecurityDomain = recoverySecurityDomainText.getText().trim();
               getRecoveryCredentialConfig().setSecurityDomain(newSecurityDomain);
            }
         }
      });
      
      // recover plugin
      final Group recoverPluginGrp = new Group(whole, SWT.NONE);
      GridLayout recoverLayout = new GridLayout();
      recoverLayout.numColumns = 1;
      recoverLayout.verticalSpacing = 9;
      recoverPluginGrp.setLayout(recoverLayout);
      recoverPluginGrp.setText(getString("ra.generate.mcf.recover.extenstion.title"));
      
      Composite clsNameContainer = new Composite(recoverPluginGrp, SWT.NONE);
      clsNameContainer.setLayout(getLayout());
      
      // class name
      label = new Label(clsNameContainer, SWT.NULL);
      label.setText(getString("ra.generate.class.name"));
      final String clsName = getRecoveryExtenstionClassName();
      recoveryClsNameText = UIResources.createText(clsNameContainer, clsName);
      recoveryClsNameText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (isRecoveryInputValid())
            {
               getRecoveryExtensionConfig().setClassName(recoveryClsNameText.getText().trim());
            }
         }
      });
      
      List<ConfigPropType> configProperties = getRecoveryConfigProperties();
      
      ConfigPropertyComposite configPropComposite = new ConfigPropertyComposite(getShell(), configProperties);
      configPropComposite.createControl(recoverPluginGrp);
      return whole;
   }

   /**
    * Validates input in Recovery configuration tab.
    * 
    * @return true if all input are OK, otherwise false.
    */
   private boolean isRecoveryInputValid()
   {
      // recovery credential
      String userName = this.userNameText.getText().trim();
      String recoverySecurityDomain = recoverySecurityDomainText.getText().trim();
      if (!userName.isEmpty() && !recoverySecurityDomain.isEmpty())
      {
         updateStatus(getString("ra.generate.error.user.name.security.null"));
         return false;
      }
      
      // recovery extension
      String recoveryClsName = this.recoveryClsNameText.getText().trim();
      if (!getRecoveryExtensionConfig().getConfigProperties().isEmpty() && recoveryClsName.isEmpty())
      {
         updateStatus(getString("ra.generate.error.recovery.class.name.null"));
         return false;
      }
      
      updateStatus(null);
      return true;
   }

   
   private void updateComponents(boolean active)
   {
      tabContainer.setEnabled(active);
   }
   

   private List<ConfigPropType> getRecoveryConfigProperties()
   {
      return getRecoveryConfig().getExtension().getConfigProperties();
   }
   
   private String getInitialJndiName()
   {
      return this.connFactoryConfig.getMcfJndiName();
   }
   
   private String getInitialPoolName()
   {
      return this.connFactoryConfig.getMcfPoolName();
   }
   
   private Boolean isMcfEnabled()
   {
      Boolean enabled = this.connFactoryConfig.getMcfEnabled();
      return enabled != null ? enabled : Boolean.valueOf(true);
   }
   
   private Boolean isMcfUseJavaCtx()
   {
      Boolean useJavaCtx = this.connFactoryConfig.getMcfUseJavaCtx();
      return useJavaCtx != null ? useJavaCtx : Boolean.valueOf(true);
   }
   
   private Boolean isMcfUseCCM()
   {
      Boolean useCcm = this.connFactoryConfig.getMcfUseCCM();
      return useCcm != null ? useCcm : Boolean.valueOf(true);
   }
   
   private Integer getMinPoolSize()
   {
      return getPoolConfig().getMinPoolSize();
   }
   
   private Integer getInitialPoolSize()
   {
      return getPoolConfig().getInitialPoolSize();
   }
   
   private Integer getMaxPoolSize()
   {
      return getPoolConfig().getMaxPoolSize();
   }
   
   private Boolean isPoolPrefill()
   {
      Boolean preFill = getPoolConfig().isPrefill();
      return preFill != null ? preFill : Boolean.valueOf(false);
   }
   
   private Boolean isPoolUseStictMin()
   {
      Boolean useStrictMin = getPoolConfig().isUseStrictMin();
      return useStrictMin != null ? useStrictMin : Boolean.valueOf(false);
   }
   
   private FlushStrategy getPoolFlushStrategy()
   {
      FlushStrategy flushStrategy = getPoolConfig().getFlushStrategy();
      return flushStrategy != null ? flushStrategy : FlushStrategy.FAILING_CONNECTION_ONLY;
   }
   
   private Boolean isXAPoolRMOverride()
   {
      Boolean rmOverride = getPoolConfig().isOverrideIsSameRM();
      return rmOverride != null ? rmOverride : Boolean.valueOf(false);
   }
   
   private Boolean isXAPoolInterleaving()
   {
      Boolean interleaving = getPoolConfig().isInterleaving();
      return interleaving != null ? interleaving : Boolean.valueOf(false);
   }
   
   private Boolean isXAPoolNoTxSeparatePool()
   {
      Boolean sep = getPoolConfig().isNoTxSeparatePool();
      return sep != null ? sep : Boolean.valueOf(false); 
   }
   
   private Boolean isXAPoolPadXid()
   {
      Boolean padXid = getPoolConfig().isPadXid();
      return padXid != null ? padXid : Boolean.valueOf(false); 
   }
   
   private Boolean isXAPoolWrapXARes()
   {
      Boolean wrapXA = getPoolConfig().isWrapXaResource();
      return wrapXA != null ? wrapXA : Boolean.valueOf(true); 
   }
   
   private ConnectionFactoryConfig.PoolConfig getPoolConfig()
   {
      ConnectionFactoryConfig.PoolConfig poolConfig = this.connFactoryConfig.getPoolConifg();
      if (poolConfig == null)
      {
         poolConfig = new PoolConfig();
         this.connFactoryConfig.setPoolConifg(poolConfig);
      }
      return poolConfig;
      
   }
   
   private SecurityConfig getSecurityConfig()
   {
      SecurityConfig secConfig = this.connFactoryConfig.getSecurityConfig();
      if (secConfig == null)
      {
         secConfig = new SecurityConfig();
         this.connFactoryConfig.setSecurityConfig(secConfig);
      }
      return secConfig;
   }
   
   private Boolean isInitialApplication()
   {
      Boolean app = getSecurityConfig().getApplication();
      return app != null ? app : Boolean.valueOf(false);
   }
   
   private String getSecurityDomain()
   {
      return getSecurityConfig().getSecurityDomain();
   }
   
   private String getSecurityDomainAndApplication()
   {
      return getSecurityConfig().getSecurityDomainAndApp();
   }
   
   private TimeoutConfig getTimeoutConfig()
   {
      TimeoutConfig timeout = this.connFactoryConfig.getTimeoutConfig();
      if (timeout == null)
      {
         timeout = new TimeoutConfig();
         this.connFactoryConfig.setTimeoutConfig(timeout);
      }
      return timeout;
   }
   
   private Long getBlockTimeout()
   {
      return getTimeoutConfig().getBlockingTimeoutMillis();
   }
   
   private Long getIdleTimeout()
   {
      return getTimeoutConfig().getIdleTimeoutMinutes();
   }
   
   private Integer getAllocationRetry()
   {
      return getTimeoutConfig().getAllocateRetry();
   }
   
   private Long getAllocateRetryWait()
   {
      return getTimeoutConfig().getAllocateRetryWait();
   }
   
   private Integer getXAResourceTimeout()
   {
      return getTimeoutConfig().getXaResourceTimeout();
   }
   
   private ValidationConfig getValidationConfig()
   {
      ValidationConfig validation = this.connFactoryConfig.getValidationConfig();
      if (validation == null)
      {
         validation = new ValidationConfig();
         this.connFactoryConfig.setValidationConfig(validation);
      }
      return validation;
   }
   
   private Boolean isBackgroundValidation()
   {
      Boolean background = getValidationConfig().getBackgroundValidation();
      return background != null ? background : Boolean.valueOf(false);
   }
   
   private Long getBackgroundValidationMillis()
   {
      return getValidationConfig().getBackgroundValidationMillis();
   }
   
   private Boolean isValidationUseFastFail()
   {
      Boolean fastFail = getValidationConfig().getUseFastFail();
      return fastFail != null ? fastFail : Boolean.valueOf(false);
   }
   
   private RecoveryConfig getRecoveryConfig()
   {
      RecoveryConfig recovery = this.connFactoryConfig.getRecoveryConfig();
      if (recovery == null)
      {
         recovery = new RecoveryConfig();
         this.connFactoryConfig.setRecoveryConfig(recovery);
      }
      return recovery;
   }
   
   private Boolean isNoRecovery()
   {
      Boolean noRecovery = getRecoveryConfig().getNoRecovery();
      return noRecovery != null ? noRecovery : Boolean.valueOf(true);
   }
   
   private String getUserName()
   {
      return getRecoveryConfig().getCredential().getUsername();
   }
   
   private String getPassword()
   {
      return getRecoveryConfig().getCredential().getPassword();
   }
   
   private String getRecoverySecurityDomain()
   {
      return getRecoveryConfig().getCredential().getSecurityDomain();
   }
   
   private ConnectionFactoryConfig.Credential getRecoveryCredentialConfig()
   {
      ConnectionFactoryConfig.Credential credentialConfig = getRecoveryConfig().getCredential();
      return credentialConfig;
   }
   
   private String getRecoveryExtenstionClassName()
   {
      return getRecoveryConfig().getExtension().getClassName();
   }
   
   private ConnectionFactoryConfig.Extension getRecoveryExtensionConfig()
   {
      ConnectionFactoryConfig.Extension extensionConfig = getRecoveryConfig().getExtension();
      return extensionConfig;
   }
   
   /**
    * Valid whether input values in general tab are correct.
    * 
    * @return true if all input are OK, otherwise, false.
    */
   private boolean checkGeneralInput()
   {
      // general tab
      String jndiName = jndiText.getText().trim();
      boolean useJavaCtx = this.useJavaCtxBtn.getSelection();
      if (!jndiName.isEmpty())
      {
         if (useJavaCtx && !jndiName.startsWith("java:/"))
         {
            updateStatus(getString("ra.generate.error.jndi.start.with.java"));
            return false;
         }
         else
         {
            updateStatus(null);
            return true;
         }
      }
      else
      {
         updateStatus(getString("ra.generate.error.jndi.name.empty"));
         return false;
      }
   }
   
   /**
    * Validates input in Pool configuration tab.
    * 
    * @return true if all input are OK, otherwise false.
    */
   private boolean checkPoolInput()
   {
      String minPool = minPoolSizeText.getText().trim();
      String maxPool = maxPoolSizeText.getText().trim();
      String initialPool = initialPoolSizeText.getText().trim();
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      Integer intialPoolSize = null;

      if (!minPool.isEmpty())
      {
         try
         {
            minPoolSize = Integer.valueOf(minPool);
            if (minPoolSize < 0)
            {
               updateStatus(getString("ra.generate.error.min.pool.size.larger.than.zero"));
               return false;
            }
         }
         catch (NumberFormatException nfe)
         {
            updateStatus(getString("ra.generate.error.min.pool.size.number"));
            return false;
         }
      }
      
      if (!initialPool.isEmpty())
      {
         try
         {
            intialPoolSize = Integer.valueOf(initialPool);
            if (intialPoolSize < 0)
            {
               updateStatus(getString("ra.generate.error.initial.pool.size.larger.than.zero"));
               return false;
            }
         }
         catch (NumberFormatException nfe)
         {
            updateStatus(getString("ra.generate.error.initial.pool.size.number"));
            return false;
         }
      }
      
      if (!maxPool.isEmpty())
      {
         try
         {
            maxPoolSize = Integer.valueOf(maxPool);
            if (maxPoolSize < 0)
            {
               updateStatus(getString("ra.generate.error.max.pool.size.larger.than.zero"));
               return false;
            }
         }
         catch (NumberFormatException nfe)
         {
            updateStatus(getString("ra.generate.error.max.pool.size.number"));
            return false;
         }
      }
      
      if (minPoolSize != null && maxPoolSize != null)
      {
         if (minPoolSize.intValue() > maxPoolSize.intValue())
         {
            updateStatus(getString("ra.generate.error.max.pool.size.less.than.min.pool.size"));
            return false;
         }
      }
      updateStatus(null);
      return true;
   }
   
   /**
    * Validates input in Timeout configuration tab.
    * 
    * @return true if all input are OK, otherwise false.
    */
   private boolean checkTimeoutInput()
   {
      String blockingTimeoutStr = this.blockingTimeoutText.getText().trim();
      String idleTimeoutStr = this.idleTimeoutText.getText().trim();
      String allocateRetryStr = this.allocateRetryText.getText().trim();
      String allocateRetryWaitStr = this.allocateRetryWaitText.getText().trim();
      String xaResTimeoutStr = this.xaResTimeoutText.getText().trim();
      
      String blockingTimeoutErr = checkPositiveLong(blockingTimeoutStr, "ra.generate.error.timeout.blocking");
      if (blockingTimeoutErr != null)
      {
         updateStatus(blockingTimeoutErr);
         return false;
      }
      
      String idleTimeoutErr = checkPositiveLong(idleTimeoutStr, "ra.generate.error.timeout.idle");
      if (idleTimeoutErr != null)
      {
         updateStatus(idleTimeoutErr);
         return false;
      }
      
      String allocateRetryErr = checkPositiveInteger(allocateRetryStr, "ra.generate.error.timeout.allocate.retry");
      if (allocateRetryErr != null)
      {
         updateStatus(allocateRetryErr);
         return false;
      }
      
      String allocateRetryWaitErr = checkPositiveLong(allocateRetryWaitStr, 
            "ra.generate.error.timeout.allocate.retry.wait");
      if (allocateRetryWaitErr != null)
      {
         updateStatus(allocateRetryWaitErr);
         return false;
      }
      
      String xaResTimeoutErr = checkPositiveInteger(xaResTimeoutStr, "ra.generate.error.timeout.xa.resource");
      if (xaResTimeoutErr != null)
      {
         updateStatus(xaResTimeoutErr);
         return false;
      }
      
      updateStatus(null);
      return true;
   }
   
   
   private String checkPositiveInteger(String str, String msgPrefix)
   {
      if (str != null && !str.isEmpty())
      {
         try
         {
            Integer value = Integer.valueOf(str);
            if (value.longValue() < 0)
            {
               return getString(msgPrefix + ".larger.than.zero");
            }
         }
         catch (NumberFormatException nfe)
         {
            return getString(msgPrefix + ".number");
         }
      }
      return null;
   }
   
   private String checkPositiveLong(String str, String msgPrefix)
   {
      if (str != null && !str.isEmpty())
      {
         try
         {
            Long value = Long.valueOf(str);
            if (value.longValue() < 0)
            {
               return getString(msgPrefix + ".larger.than.zero");
            }
         }
         catch (NumberFormatException nfe)
         {
            return getString(msgPrefix + ".number");
         }
      }
      return null;
   }
   
   /**
    * Validates the input in validation configuration tab.
    * 
    * @return true if are input are OK, otherwise false.
    */
   private boolean checkValidationInput()
   {
      String bgValidationMills = backgroundValidText.getText().trim();
      String bgValidationMillsErr = checkPositiveLong(bgValidationMills, 
            "ra.generate.error.validation.background.mills");
      if (bgValidationMillsErr != null)
      {
         updateStatus(bgValidationMillsErr);
         return false;
      }
      updateStatus(null);
      return true;
   }

}
