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
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
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
{
   
   private final ConnectionDefinition connDef;
   
   private final ConnectorHelper connectorHelper;
   
   private final CommonConnDef initialConnDef;
   
   private final ResourceAdapterConfig raConfig;
   
   private final ConnectionFactoryConfig connFactoryConfig;
   
   private final String mcfClsName;
   
   /** The TableViewer in general tab */
   private Composite generalConfigPropertiesPanel;
   
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
   private Text maxPoolSizeText;
   private Combo flushStrategyCombo;
   
   // timeout components
   private Text blockingTimeoutText;
   private Text idleTimeoutText;
   private Text allocateRetryText;
   private Text allocateRetryWaitText;
   private Text xaResTimeoutText;
   
   // validation components
   private Text backgroundValidText;
   /**
    * The constructor.
    * 
    * @param wizard the RAGenerateWizard.
    * @param connDef the ConnectionDefinition.
    */
   public RAGenerateMCFPage(RAGenerateWizard wizard, ConnectionDefinition connDef)
   {
      super("MCFPage");
      this.raConfig = wizard.getRaConfig();
      this.connDef = connDef;
      this.connectorHelper = wizard.getConnectorHelper();
      this.initialConnDef = getCommonConnDef();
      this.connFactoryConfig = new ConnectionFactoryConfig();
      this.mcfClsName = this.connDef.getManagedConnectionFactoryClass().getValue();
      this.connFactoryConfig.setMcfClsName(this.mcfClsName);
      this.connFactoryConfig.setMcfJndiName(getInitialJndiName());
      if (isInitialActive())
      {
         this.raConfig.getConnectionDefinitions().add(connFactoryConfig);
      }
      setTitle("Information about Managed Connection Factory");
      setDescription("Configure the Managed Connection Factory: " + mcfClsName);
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
      label.setText("Active it");
      
      final Button activeBtn = new Button(container, SWT.CHECK);
      
      activeBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            boolean active = activeBtn.getSelection();
            updateComponents(active);
            List<ConnectionFactoryConfig> connFactoryConfigs = raConfig.getConnectionDefinitions();
            if (active)
            {
               if (!connFactoryConfigs.contains(connFactoryConfig))
               {
                  connFactoryConfigs.add(connFactoryConfig);
               }
            }
            else
            {
               connFactoryConfigs.remove(connFactoryConfig);
            }
         }

      });
      activeBtn.setSelection(isInitialActive());
      
      tabContainer = new TabFolder(whole, SWT.NONE);
      tabContainer.setEnabled(isInitialActive());
      
      // general tab
      TabItem generalTab = new TabItem(tabContainer, SWT.NULL);
      generalTab.setText("General");
      generalTab.setControl(createGeneralControl(tabContainer));
      
      // pool tab
      TabItem poolTab = new TabItem(tabContainer, SWT.NULL);
      poolTab.setText("Pool Configuration");
      poolTab.setControl(createPoolControl(tabContainer));
      
      // security tab
      TabItem securityTab = new TabItem(tabContainer, SWT.NULL);
      securityTab.setText("Security");
      securityTab.setControl(createSecuriyControl(tabContainer));
      
      // timeout tab
      TabItem timeoutTab = new TabItem(tabContainer, SWT.NULL);
      timeoutTab.setText("Timeout");
      timeoutTab.setControl(createTimeoutControl(tabContainer));
      
      // validation tab
      TabItem validationTab = new TabItem(tabContainer, SWT.NULL);
      validationTab.setText("Validation");
      validationTab.setControl(createValidationControl(tabContainer));
      
      // recover tab
      TabItem recoverTab = new TabItem(tabContainer, SWT.NULL);
      recoverTab.setText("Recover");
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
      generalGrp.setText("General Information");
      generalGrp.setLayout(getLayout());

      // mcf class name
      Label label = new Label(generalGrp, SWT.NULL);
      label.setText("Managed Connection Factory Class:");
      Label mcfClsLabel = new Label(generalGrp, SWT.NULL);
      mcfClsLabel.setText(this.mcfClsName);
      
      // jndi name
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Jndi Name:");
      final String jndiName = getInitialJndiName();
      jndiText = createText(generalGrp, jndiName);
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
      label.setText("Pool Name:");
      final String poolName = getInitialPoolName();
      final Text poolText = createText(generalGrp, poolName);
      poolText.addModifyListener(new ModifyListener()
      {
         @Override
         public void modifyText(ModifyEvent e)
         {
            String pool = poolText.getText().trim();
            if (!pool.isEmpty() && !(pool.equals(poolName)))
            {
               connFactoryConfig.setMcfPoolName(pool);
            }
            else
            {
               connFactoryConfig.setMcfPoolName(null);
            }
         }
      });
      
      // enabled
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Enabled:");
      final Button enabledBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      final Boolean isEnabled = isMcfEnabled();
      enabledBtn.setSelection(isEnabled);
      enabledBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (isEnabled ^ enabledBtn.getSelection())
            {
               connFactoryConfig.setMcfEnabled(Boolean.valueOf(enabledBtn.getSelection()));
            }
            else
            {
               connFactoryConfig.setMcfEnabled(null);
            }
         }
      });
      
      // use java context
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Use Java Context:");
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
               if (useJavaCtx ^ useJavaCtxBtn.getSelection())
               {
                  connFactoryConfig.setMcfUseJavaCtx(useJavaCtxBtn.getSelection());
               }
               else
               {
                  connFactoryConfig.setMcfUseJavaCtx(null);
               }
            }
         }
      });
      
      // use ccm
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Use Cached Connection Manager:");
      final Button useCcmBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      final Boolean useCcm = isMcfUseCCM();
      useCcmBtn.setSelection(useCcm);
      useCcmBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (useCcm ^ useCcmBtn.getSelection())
            {
               connFactoryConfig.setMcfUseCCM(useCcmBtn.getSelection());
            }
            else
            {
               connFactoryConfig.setMcfUseCCM(null);
            }
         }
      });
      
      // config properties
      this.generalConfigPropertiesPanel = createConfigPropertyTableViewer(whole, 
            this.connectorHelper.getConfigProps(this.connDef.getConfigProperties()));
      
      return whole;
   }
   
   /**
    * Creates Pool configuration control
    * 
    * @param parent the parent
    * @return the control
    */
   private Composite createPoolControl(Composite parent)
   {
      Composite whole = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 1;
      whole.setLayout(layout);
      CommonPool pool = getPool();
      CommonXaPool xaPool = null;
      if (pool instanceof CommonXaPool)
      {
         xaPool = (CommonXaPool)pool;
      }
      
      // pool
      final Composite container = new Composite(whole, SWT.None);
      container.setLayout(getLayout());
      
      // min-pool-size
      Label label = new Label(container, SWT.NULL);
      label.setText("Minimum Pool Size:");
      final Integer minPoolSize = getMinPoolSize(pool);
      minPoolSizeText = createText(container, minPoolSize);
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
                  Integer newSize = Integer.valueOf(minPool);
                  if (newSize != minPoolSize)
                  {
                     getPoolConfig().setMinPoolSize(newSize);
                  }
                  else
                  {
                     getPoolConfig().setMinPoolSize(null);
                  }
               }
               else
               {
                  getPoolConfig().setMinPoolSize(null);
               }
               checkPoolFlushStrategy();
            }
         }
      });

      // max-pool-size
      label = new Label(container, SWT.NULL);
      label.setText("Maximum Pool Size:");
      final Integer maxPoolSize = getMaxPoolSize(pool);
      maxPoolSizeText = createText(container, maxPoolSize);
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
                  if (newSize != maxPoolSize)
                  {
                     getPoolConfig().setMaxPoolSize(newSize);
                  }
                  else
                  {
                     getPoolConfig().setMaxPoolSize(null);
                  }
               }
               else
               {
                  getPoolConfig().setMaxPoolSize(null);
               }
               checkPoolFlushStrategy();
            }
         }
      });
      
      // prefill
      label = new Label(container, SWT.NULL);
      label.setText("Prefill Connection Pool:");
      final Button prefillBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean prefill = isPoolPrefill(pool);
      prefillBtn.setSelection(prefill);
      prefillBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (prefill ^ prefillBtn.getSelection())
            {
               getPoolConfig().setPrefill(prefillBtn.getSelection());
            }
            else
            {
               getPoolConfig().setPrefill(null);
            }
            checkPoolFlushStrategy();
         }
      });
      
      // use-strict-min
      label = new Label(container, SWT.NULL);
      label.setText("Use Strict min-pool-size:");
      final Button useStrictMinBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean useStrictMin = isPoolUseStictMin(pool);
      useStrictMinBtn.setSelection(useStrictMin);
      useStrictMinBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (useStrictMin ^ useStrictMinBtn.getSelection())
            {
               getPoolConfig().setUseStrictMin(useStrictMinBtn.getSelection());
            }
            else
            {
               getPoolConfig().setUseStrictMin(null);
            }
            checkPoolFlushStrategy();
         }
      });
      
      // flush strategy 
      label = new Label(container, SWT.NULL);
      label.setText("Flush Strategy:");
      flushStrategyCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
      String[] items = new String[]{FlushStrategy.FAILING_CONNECTION_ONLY.getName(), 
            FlushStrategy.IDLE_CONNECTIONS.getName(), FlushStrategy.ENTIRE_POOL.getName()};
      flushStrategyCombo.setItems(items);
      final FlushStrategy flushStrategy = getPoolFlushStrategy(pool);
      flushStrategyCombo.setText(flushStrategy.getName());
      flushStrategyCombo.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (!FlushStrategy.forName(flushStrategyCombo.getText()).equals(flushStrategy))
            {
               getPoolConfig().setFlushStrategy(FlushStrategy.forName(flushStrategyCombo.getText()));
            }
            else
            {
               checkPoolFlushStrategy();
            }
         }
      });
      
      final Group xaPoolGroup = new Group(whole, SWT.NONE);
      xaPoolGroup.setText("XA Pool");
      GridLayout xaPoolLayout = new GridLayout();
      xaPoolLayout.numColumns = 2;
      xaPoolLayout.horizontalSpacing = 108;
      xaPoolLayout.makeColumnsEqualWidth = true;
      
      xaPoolGroup.setLayout(xaPoolLayout);
      
      // define a xa pool ?
      label = new Label(container, SWT.NULL);
      label.setText("Define xa-pool:");
      xaPoolBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      xaPoolBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            xaPoolGroup.setVisible(xaPoolBtn.getSelection());
            container.update();
            getPoolConfig().setDefineXA(xaPoolBtn.getSelection());
         }
      });
      xaPoolBtn.setSelection(xaPool != null);
      getPoolConfig().setDefineXA(xaPool != null);
      
      // is-same-rm-override
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText("Overide the isSameRM method:");
      final Button isSameRMOverideBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean isSameRMOverride = isXAPoolRMOverride(xaPool);
      isSameRMOverideBtn.setSelection(isSameRMOverride);
      isSameRMOverideBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (isSameRMOverride ^ isSameRMOverideBtn.getSelection())
            {
               getPoolConfig().setOverrideIsSameRM(isSameRMOverideBtn.getSelection());
            }
            else
            {
               getPoolConfig().setOverrideIsSameRM(null);
            }
            checkPoolFlushStrategy();
         }
      });
      
      // interleaving
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText("Interleaving:");
      final Button interLeavingBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean interLeaving = isXAPoolInterleaving(xaPool);
      interLeavingBtn.setSelection(interLeaving);
      interLeavingBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (interLeaving ^ interLeavingBtn.getSelection())
            {
               getPoolConfig().setInterleaving(interLeavingBtn.getSelection());
            }
            else
            {
               getPoolConfig().setInterleaving(null);
            }
            if (!interLeavingBtn.getSelection())
            { 
               // it is boolean-presenceType
               getPoolConfig().setInterleaving(null);
            }
            checkPoolFlushStrategy();
         }
      });
      
      // no-tx-separate-pools
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText("Create Separate Sub-pools:");
      final Button noTxSeparatePoolBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean noTxSepPool = isXAPoolNoTxSeparatePool(xaPool);
      noTxSeparatePoolBtn.setSelection(noTxSepPool);
      noTxSeparatePoolBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (noTxSepPool ^ noTxSeparatePoolBtn.getSelection())
            {
               getPoolConfig().setNoTxSeparatePool(noTxSeparatePoolBtn.getSelection());
            }
            else
            {
               getPoolConfig().setNoTxSeparatePool(null);
            }
            if (!noTxSeparatePoolBtn.getSelection())
            {
               // it is boolean-presenceType
               getPoolConfig().setNoTxSeparatePool(null);
            }
            checkPoolFlushStrategy();
         }
      });
      
      // pad-xid
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText("Xid Padded:");
      final Button padXidBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean padXid = isXAPoolPadXid(xaPool);
      padXidBtn.setSelection(padXid);
      padXidBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (padXid ^ padXidBtn.getSelection())
            {
               getPoolConfig().setPadXid(padXidBtn.getSelection());
            }
            else
            {
               getPoolConfig().setPadXid(null);
            }
            checkPoolFlushStrategy();
         }
      });
      
      // wrap xa resource
      label = new Label(xaPoolGroup, SWT.NULL);
      label.setText("Wrap XA Resource:");
      final Button wrapXaResBtn = new Button(xaPoolGroup, SWT.BORDER | SWT.CHECK);
      final Boolean wrapXA = isXAPoolWrapXARes(xaPool);
      wrapXaResBtn.setSelection(wrapXA);
      wrapXaResBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (wrapXA ^ wrapXaResBtn.getSelection())
            {
               getPoolConfig().setWrapXaResource(wrapXaResBtn.getSelection());
            }
            else
            {
               getPoolConfig().setWrapXaResource(null);
            }
            checkPoolFlushStrategy();
         }
      });

      xaPoolGroup.setVisible(xaPoolBtn.getSelection());
      return whole;
   }
   
   /**
    * Returns the Pool.
    * 
    * @return the CommonPool
    */
   private CommonPool getPool()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.getPool();
      }
      return null;
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

      CommonSecurity commonSecurity = null;
      if (this.initialConnDef != null)
      {
         commonSecurity = this.initialConnDef.getSecurity();
      }
      Composite container = new Composite(whole, SWT.NONE);
      container.setLayout(getLayout());
      
      // application
      appBtn = new Button(container, SWT.RADIO);
      Label invisibleLabel = new Label(container, SWT.NULL);
      invisibleLabel.setText("");
      appBtn.setText("Application");
      final Boolean app = isInitialApplication(commonSecurity);
      appBtn.setSelection(app);
      appBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (app ^ appBtn.getSelection())
            {
               getSecurityConfig().setApplication(appBtn.getSelection());
            }
            else
            {
               getSecurityConfig().setApplication(null);
            }
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
      final String securityDomain = getSecurityDomain(commonSecurity);
      final Button securityDomainBtn = new Button(container, SWT.RADIO);
      securityDomainBtn.setText("Security Domain:");
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
            if (!security.isEmpty() && !security.equals(securityDomain))
            {
               getSecurityConfig().setSecurityDomain(security);
            }
            else
            {
               getSecurityConfig().setSecurityDomain(null);
            }
         }
      });
      
      securityDomainText = createText(container, securityDomain);
      securityDomainText.setEnabled(securityDomainBtn.getSelection());
      securityDomainText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            securityDomainBtn.setSelection(true);
            String security = securityDomainText.getText().trim();
            if (!security.isEmpty() && !security.equals(securityDomain))
            {
               getSecurityConfig().setSecurityDomain(security);
            }
            else
            {
               getSecurityConfig().setSecurityDomain(null);
            }
         }
      });
      
      // securiry domain and application
      final String securityDomainAndApplication = getSecurityDomainAndApplication(commonSecurity);
      final Button secDomainAndAppBtn = new Button(container, SWT.RADIO);
      secDomainAndAppBtn.setSelection(securityDomainAndApplication != null);
      secDomainAndAppBtn.setText("Security Domain And Application:");
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
            if (!securityAndApp.isEmpty() && !securityAndApp.equals(securityDomainAndApplication))
            {
               getSecurityConfig().setSecurityDomainAndApp(securityAndApp);
            }
            else
            {
               getSecurityConfig().setSecurityDomainAndApp(null);
            }
         }
      });
      
      
      securityDomainAndAppText = createText(container, securityDomainAndApplication);
      securityDomainAndAppText.setEnabled(secDomainAndAppBtn.getSelection());
      securityDomainAndAppText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            secDomainAndAppBtn.setSelection(true);
            String securityAndApp = securityDomainAndAppText.getText().trim();
            if (!securityAndApp.isEmpty() && !securityAndApp.equals(securityDomainAndApplication))
            {
               getSecurityConfig().setSecurityDomainAndApp(securityAndApp);
            }
            else
            {
               getSecurityConfig().setSecurityDomainAndApp(null);
            }
            
         }
      });
      
      return whole;
   }
   
   /**
    * Returns the TimeOut.
    * 
    * @return the CommonTimeOut
    */
   private CommonTimeOut getTimeOut()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.getTimeOut();
      }
      return null;
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
      CommonTimeOut timeOut = getTimeOut();
      
      final Composite container = new Composite(whole, SWT.None);
      container.setLayout(getLayout());
      
      // blocking timeout millis
      Label label = new Label(container, SWT.NULL);
      label.setText("Blocking Timeout Millis:");
      
      final Long blockTimeout = getBlockTimeout(timeOut);
      blockingTimeoutText = createText(container, blockTimeout);
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
                  if (blkTimeout != blockTimeout)
                  {
                     getTimeoutConfig().setBlockingTimeoutMillis(blkTimeout);
                  }
                  else
                  {
                     getTimeoutConfig().setBlockingTimeoutMillis(null);
                  }
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
      label.setText("Idle Timeout Minutes:");
      final Long idleTimeout = getIdleTimeout(timeOut);
      idleTimeoutText = createText(container, idleTimeout);
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
                  if (idle != idleTimeout)
                  {
                     getTimeoutConfig().setIdleTimeoutMinutes(idle);
                  }
                  else
                  {
                     getTimeoutConfig().setIdleTimeoutMinutes(null);
                  }
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
      label.setText("Allocate Retry:");
      final Integer allocateRetry = getAllocationRetry(timeOut);
      allocateRetryText = createText(container, allocateRetry);
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
                  if (retry != allocateRetry)
                  {
                     getTimeoutConfig().setAllocateRetry(retry);
                  }
                  else
                  {
                     getTimeoutConfig().setAllocateRetry(null);
                  }
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
      label.setText("Allocate Retry Wait:");
      final Long allocateRetryWait = getAllocateRetryWait(timeOut);
      allocateRetryWaitText = createText(container, allocateRetryWait);
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
                  if (allocateRetryWaitMillis != allocateRetryWait)
                  {
                     getTimeoutConfig().setAllocateRetryWait(allocateRetryWaitMillis);
                  }
                  else
                  {
                     getTimeoutConfig().setAllocateRetryWait(null);
                  }
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
      label.setText("XA Resource Timeout:");
      final Integer xaResTimeout = getXAResourceTimeout(timeOut);
      xaResTimeoutText = createText(container, xaResTimeout);
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
                  if (xaResourceTimeout != xaResTimeout)
                  {
                     getTimeoutConfig().setXaResourceTimeout(xaResourceTimeout);
                  }
                  else
                  {
                     getTimeoutConfig().setXaResourceTimeout(null);
                  }
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
    * Gets the Validation.
    * 
    * @return the CommonValidation
    */
   private CommonValidation getValidation()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.getValidation();
      }
      return null;
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
      
      CommonValidation validation = getValidation();
      
      // background validation
      Label label = new Label(container, SWT.NULL);
      label.setText("Background Validation:");
      final Button backgroundValidationBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean backgroundValidation = isBackgroundValidation(validation);
      
      
      // background validation millis
      label = new Label(container, SWT.NULL);
      label.setText("Background Validation Millis:");
      final Long backgroundValidationMills = getBackgroundValidationMillis(validation);
      backgroundValidText = createText(container, backgroundValidationMills);
      
      backgroundValidationBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            backgroundValidText.setEnabled(backgroundValidationBtn.getSelection());
            
            if (backgroundValidation ^ backgroundValidationBtn.getSelection())
            {
               getValidationConfig().setBackgroundValidation(backgroundValidationBtn.getSelection());
            }
            else
            {
               getValidationConfig().setBackgroundValidation(null);
            }
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
                  if (bgValidMills != backgroundValidationMills)
                  {
                     getValidationConfig().setBackgroundValidationMillis(bgValidMills);
                  }
                  else
                  {
                     getValidationConfig().setBackgroundValidationMillis(null);
                  }
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
      label.setText("Use Fast Fail:");
      final Button useFastFailBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean useFastFail = isValidationUseFastFail(validation);
      useFastFailBtn.setSelection(useFastFail);
      useFastFailBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (useFastFail ^ useFastFailBtn.getSelection())
            {
               getValidationConfig().setUseFastFail(useFastFailBtn.getSelection());
            }
            else
            {
               getValidationConfig().setUseFastFail(null);
            }
         }
      });
      
      return whole;
   }
   
   /**
    * Gets the Recovery.
    * 
    * @return the Recovery
    */
   private Recovery getRecovery()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.getRecovery();
      }
      return null;
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
      
      Recovery recovery = getRecovery();
      
      // no recovery
      Label label = new Label(container, SWT.NULL);
      label.setText("No Recovery:");
      final Button noRecoveryBtn = new Button(container, SWT.BORDER | SWT.CHECK);
      final Boolean isNoRecovery = isNoRecovery(recovery);
      noRecoveryBtn.setSelection(isNoRecovery);
      noRecoveryBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (isNoRecovery ^ noRecoveryBtn.getSelection())
            {
               getRecoveryConfig().setNoRecovery(noRecoveryBtn.getSelection());
            }
            else
            {
               getRecoveryConfig().setNoRecovery(null);
            }
         }
      });
      
      // recover credential
      final Group credentialGroup = new Group(whole, SWT.NONE);
      credentialGroup.setLayout(getLayout());
      credentialGroup.setText("Recover Credential");
      
      Credential credential = null;
      if (recovery != null)
      {
         credential = recovery.getCredential();
      }
      // user name
      label = new Label(credentialGroup, SWT.NULL);
      label.setText("User Name:");
      final String userName = getUserName(credential);
      userNameText = createText(credentialGroup, userName);
      
      
      // password
      label = new Label(credentialGroup, SWT.NULL);
      label.setText("Password:");
      final String password = getPassword(credential);
      final Text passwordText = new Text(credentialGroup, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
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
            if (!passwordText.getText().trim().equals(password))
            {
               getRecoveryCredentialConfig().setPassword(passwordText.getText().trim());
            }
            else
            {
               getRecoveryCredentialConfig().setPassword(null);
            }
         }
      });
      
      // security-domain
      label = new Label(credentialGroup, SWT.NULL);
      label.setText("Security Domain:");
      final String securityDomain = getRecoverySecurityDomain(credential);
      recoverySecurityDomainText = createText(credentialGroup, securityDomain);
      
      userNameText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (isRecoveryInputValid())
            {
               String newUserName = userNameText.getText().trim();
               if (!newUserName.equals(userName))
               {
                  getRecoveryCredentialConfig().setUsername(newUserName);
               }
               else
               {
                  getRecoveryCredentialConfig().setUsername(null);
               }
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
               if (!newSecurityDomain.equals(securityDomain))
               {
                  getRecoveryCredentialConfig().setSecurityDomain(newSecurityDomain);
               }
               else
               {
                  getRecoveryCredentialConfig().setSecurityDomain(null);
               }
            }
         }
      });
      
      // recover plugin
      final Group recoverPluginGrp = new Group(whole, SWT.NONE);
      GridLayout recoverLayout = new GridLayout();
      recoverLayout.numColumns = 1;
      recoverLayout.verticalSpacing = 9;
      recoverPluginGrp.setLayout(recoverLayout);
      recoverPluginGrp.setText("Recover Plugin");
      
      Composite clsNameContainer = new Composite(recoverPluginGrp, SWT.NONE);
      clsNameContainer.setLayout(getLayout());
      
      Extension recoverPlugin = null;
      if (recovery != null)
      {
         recoverPlugin = recovery.getRecoverPlugin();
      }
      // class name
      label = new Label(clsNameContainer, SWT.NULL);
      label.setText("Class Name:");
      final String clsName = getRecoveryExtenstionClassName(recoverPlugin);
      recoveryClsNameText = createText(clsNameContainer, clsName);
      recoveryClsNameText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            if (isRecoveryInputValid())
            {
               if (!recoveryClsNameText.getText().trim().equals(clsName))
               {
                  getRecoveryExtensionConfig().setClassName(recoveryClsNameText.getText().trim());
               }
               else
               {
                  getRecoveryExtensionConfig().setClassName(null);
               }
            }
         }
      });
      
      Map<String, String> configPropertiesMap = null;
      if (recoverPlugin != null)
      {
         configPropertiesMap = recoverPlugin.getConfigPropertiesMap();
      }
      List<ConfigPropType> configProperties = new ArrayList<ConfigPropType>();
      if (configPropertiesMap != null)
      {
         for (Map.Entry<String, String> entry : configPropertiesMap.entrySet())
         {
            ConfigPropType configPropType = new ConfigPropType();
            configPropType.setName(entry.getKey());
            configPropType.setType("String");
            configPropType.setValue(entry.getValue());
            configProperties.add(configPropType);
         }
      }
      createConfigPropertyTableViewer(recoverPluginGrp, configProperties);
      
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
         updateStatus("User Name and Security Domain can not be null at same time.");
         return false;
      }
      
      // recovery extension
      String recoveryClsName = this.recoveryClsNameText.getText().trim();
      if (!getRecoveryExtensionConfig().getConfigProperties().isEmpty() && recoveryClsName.isEmpty())
      {
         updateStatus("Recovery Class Name can not be empty.");
         return false;
      }
      
      updateStatus(null);
      return true;
   }

  
   @Override
   protected void onConfigPropUpdated(TableViewer configPropsTableView, ConfigPropType prop)
   {
      if (configPropsTableView.getTable().getParent().equals(this.generalConfigPropertiesPanel))
      {
         // config properties in the general tab
         List<ConfigPropType> configProps = connFactoryConfig.getMcfConfigProps();
         if (configProps == null)
         {
            configProps = new ArrayList<ConfigPropType>();
            connFactoryConfig.setMcfConfigProps(configProps);
         }
         if (!configProps.contains(prop))
         {
            configProps.add(prop);
         }
      }
      else
      {
         List<ConfigPropType> configProps = getRecoveryExtensionConfig().getConfigProperties();
         if (configProps == null)
         {
            configProps = new ArrayList<ConfigPropType>();
            getRecoveryExtensionConfig().setConfigProperties(configProps);
         }
         if (!configProps.contains(prop))
         {
            configProps.add(prop);
            isRecoveryInputValid(); // force check class name after config property updated
         }
      }
      
   }

   private void updateComponents(boolean active)
   {
      tabContainer.setEnabled(active);
   }
   
   private boolean isInitialActive()
   {
      return getCommonConnDef() != null;
   }
   
   private String getInitialJndiName()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.getJndiName();
      }
      return null;
   }
   
   private String getInitialPoolName()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.getPoolName();
      }
      return null;
   }
   
   private Boolean isMcfEnabled()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.isEnabled();
      }
      return Boolean.valueOf(true);
   }
   
   private Boolean isMcfUseJavaCtx()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.isUseJavaContext();
      }
      return Boolean.valueOf(true);
   }
   
   private Boolean isMcfUseCCM()
   {
      if (this.initialConnDef != null)
      {
         return this.initialConnDef.isUseCcm();
      }
      return Boolean.valueOf(true);
   }
   
   private Integer getMinPoolSize(CommonPool pool)
   {
      if (pool != null)
      {
         return pool.getMinPoolSize();
      }
      return null;
   }
   
   private Integer getMaxPoolSize(CommonPool pool)
   {
      if (pool != null)
      {
         return pool.getMaxPoolSize();
      }
      return null;
   }
   
   private Boolean isPoolPrefill(CommonPool pool)
   {
      if (pool != null)
      {
         return pool.isPrefill();
      }
      return Boolean.valueOf(false);
   }
   
   private Boolean isPoolUseStictMin(CommonPool pool)
   {
      if (pool != null)
      {
         return pool.isUseStrictMin();
      }
      return Boolean.valueOf(false);
   }
   
   private FlushStrategy getPoolFlushStrategy(CommonPool pool)
   {
      if (pool != null)
      {
         return pool.getFlushStrategy();
      }
      return FlushStrategy.FAILING_CONNECTION_ONLY;
   }
   
   private Boolean isXAPoolRMOverride(CommonXaPool xaPool)
   {
      if (xaPool != null)
      {
         return xaPool.isSameRmOverride();
      }
      return Boolean.valueOf(false);
   }
   
   private Boolean isXAPoolInterleaving(CommonXaPool xaPool)
   {
      if (xaPool != null)
      {
         return xaPool.isInterleaving();
      }
      return Boolean.valueOf(false);
   }
   
   private Boolean isXAPoolNoTxSeparatePool(CommonXaPool xaPool)
   {
      if (xaPool != null)
      {
         return xaPool.isNoTxSeparatePool();
      }
      return Boolean.valueOf(false); 
   }
   
   private Boolean isXAPoolPadXid(CommonXaPool xaPool)
   {
      if (xaPool != null)
      {
         return xaPool.isPadXid();
      }
      return Boolean.valueOf(false); 
   }
   
   private Boolean isXAPoolWrapXARes(CommonXaPool xaPool)
   {
      if (xaPool != null)
      {
         return xaPool.isWrapXaResource();
      }
      return Boolean.valueOf(true); 
   }
   
   private ConnectionFactoryConfig.PoolConfig getPoolConfig()
   {
      ConnectionFactoryConfig.PoolConfig poolConfig = this.connFactoryConfig.getPoolConifg();
      if (poolConfig == null)
      {
         poolConfig = new ConnectionFactoryConfig.PoolConfig();
         this.connFactoryConfig.setPoolConifg(poolConfig);
      }
      return poolConfig;
      
   }
   
   private Boolean isInitialApplication(CommonSecurity commonSecurity)
   {
      if (commonSecurity != null)
      {
         return commonSecurity.isApplication();
      }
      return Boolean.valueOf(false);
   }
   
   private String getSecurityDomain(CommonSecurity commonSecurity)
   {
      if (commonSecurity != null)
      {
         return commonSecurity.getSecurityDomain();
      }
      return null;
   }
   
   private String getSecurityDomainAndApplication(CommonSecurity commonSecurity)
   {
      if (commonSecurity != null)
      {
         return commonSecurity.getSecurityDomainAndApplication();
      }
      return null;
   }
   
   private ConnectionFactoryConfig.SecurityConfig getSecurityConfig()
   {
      ConnectionFactoryConfig.SecurityConfig securityConfig = this.connFactoryConfig.getSecurityConfig();
      if (securityConfig == null)
      {
         securityConfig = new ConnectionFactoryConfig.SecurityConfig();
         this.connFactoryConfig.setSecurityConfig(securityConfig);
      }
      return securityConfig;
   }
   
   private Long getBlockTimeout(CommonTimeOut timeOut)
   {
      if (timeOut != null)
      {
         return timeOut.getBlockingTimeoutMillis();
      }
      return null;
   }
   
   private Long getIdleTimeout(CommonTimeOut timeOut)
   {
      if (timeOut != null)
      {
         return timeOut.getIdleTimeoutMinutes();
      }
      return null;
   }
   
   private Integer getAllocationRetry(CommonTimeOut timeOut)
   {
      if (timeOut != null)
      {
         return timeOut.getAllocationRetry();
      }
      return null;
   }
   
   private Long getAllocateRetryWait(CommonTimeOut timeOut)
   {
      if (timeOut != null)
      {
         return timeOut.getAllocationRetryWaitMillis();
      }
      return null;
   }
   
   private Integer getXAResourceTimeout(CommonTimeOut timeOut)
   {
      if (timeOut != null)
      {
         return timeOut.getXaResourceTimeout();
      }
      return null;
   }
   
   private ConnectionFactoryConfig.TimeoutConfig getTimeoutConfig()
   {
      ConnectionFactoryConfig.TimeoutConfig timeoutConfig = this.connFactoryConfig.getTimeoutConfig();
      if (timeoutConfig == null)
      {
         timeoutConfig = new ConnectionFactoryConfig.TimeoutConfig();
         this.connFactoryConfig.setTimeoutConfig(timeoutConfig);
      }
      return timeoutConfig;
   }
   
   private Boolean isBackgroundValidation(CommonValidation validation)
   {
      if (validation != null)
      {
         return validation.isBackgroundValidation();
      }
      return Boolean.valueOf(false);
   }
   
   private Long getBackgroundValidationMillis(CommonValidation validation)
   {
      if (validation != null)
      {
         return validation.getBackgroundValidationMillis();
      }
      return null;
   }
   
   private Boolean isValidationUseFastFail(CommonValidation validation)
   {
      if (validation != null)
      {
         return validation.isUseFastFail();
      }
      return Boolean.valueOf(false);
   }
   
   private ConnectionFactoryConfig.ValidationConfig getValidationConfig()
   {
      ConnectionFactoryConfig.ValidationConfig validationConfig = this.connFactoryConfig.getValidationConfig();
      if (validationConfig == null)
      {
         validationConfig = new ConnectionFactoryConfig.ValidationConfig();
         this.connFactoryConfig.setValidationConfig(validationConfig);
      }
      return validationConfig;
   }
   
   private Boolean isNoRecovery(Recovery recovery)
   {
      if (recovery != null)
      {
         return recovery.getNoRecovery();
      }
      return Boolean.valueOf(false);
   }
   private ConnectionFactoryConfig.RecoveryConfig getRecoveryConfig()
   {
      ConnectionFactoryConfig.RecoveryConfig recoveryConfig = this.connFactoryConfig.getRecoveryConfig();
      if (recoveryConfig == null)
      {
         recoveryConfig = new ConnectionFactoryConfig.RecoveryConfig();
         this.connFactoryConfig.setRecoveryConfig(recoveryConfig);
      }
      return recoveryConfig;
   }
   
   private String getUserName(Credential credential)
   {
      if (credential != null)
      {
         return credential.getUserName();
      }
      return null;
   }
   
   private String getPassword(Credential credential)
   {
      if (credential != null)
      {
         return credential.getPassword();
      }
      return null;
   }
   
   private String getRecoverySecurityDomain(Credential credential)
   {
      if (credential != null)
      {
         return credential.getSecurityDomain();
      }
      return null;
   }
   
   private ConnectionFactoryConfig.RecoveryConfig.Credential getRecoveryCredentialConfig()
   {
      ConnectionFactoryConfig.RecoveryConfig.Credential credentialConfig = getRecoveryConfig().getCredential();
      if (credentialConfig == null)
      {
         credentialConfig = new ConnectionFactoryConfig.RecoveryConfig.Credential();
         getRecoveryConfig().setCredential(credentialConfig);
      }
      return credentialConfig;
   }
   
   private String getRecoveryExtenstionClassName(Extension extension)
   {
      if (extension != null)
      {
         return extension.getClassName();
      }
      return null;
   }
   
   private ConnectionFactoryConfig.RecoveryConfig.Extension getRecoveryExtensionConfig()
   {
      ConnectionFactoryConfig.RecoveryConfig.Extension extensionConfig = getRecoveryConfig().getExtension();
      if (extensionConfig == null)
      {
         extensionConfig = new ConnectionFactoryConfig.RecoveryConfig.Extension();
         getRecoveryConfig().setExtension(extensionConfig);
      }
      return extensionConfig;
   }
   
   private CommonConnDef getCommonConnDef()
   {
      return this.connectorHelper.getCommonConnDef(this.connDef.getManagedConnectionFactoryClass().getValue());
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
            updateStatus("Jndi Name needs start with 'java:/'");
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
         updateStatus("Jndi Name of ManagedConnectionFactory can not be empty.");
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
      Integer minPoolSize = null;
      Integer maxPoolSize = null;

      if (!minPool.isEmpty())
      {
         try
         {
            minPoolSize = Integer.valueOf(minPool);
            if (minPoolSize < 0)
            {
               updateStatus("Minimum pool size must be larger than 0.");
               return false;
            }
         }
         catch (NumberFormatException nfe)
         {
            updateStatus("Minimum pool size must be a number.");
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
               updateStatus("Maximum pool size must be larger than 0.");
               return false;
            }
         }
         catch (NumberFormatException nfe)
         {
            updateStatus("Maximum pool size must be a number.");
            return false;
         }
      }
      
      if (minPoolSize != null && maxPoolSize != null)
      {
         if (minPoolSize.intValue() > maxPoolSize.intValue())
         {
            updateStatus("Maximum pool size must be larger than Minimum pool size.");
            return false;
         }
      }
      updateStatus(null);
      return true;
   }
   
   private void checkPoolFlushStrategy()
   {
      FlushStrategy initialFlushStrategy = getPoolFlushStrategy(getPool());
      FlushStrategy flushStrategy = initialFlushStrategy;
      if (!FlushStrategy.forName(this.flushStrategyCombo.getText()).equals(initialFlushStrategy))
      {
         flushStrategy = FlushStrategy.forName(this.flushStrategyCombo.getText());
      }
      if (getPoolConfig().getMinPoolSize() != null || getPoolConfig().getMaxPoolSize() != null 
            || getPoolConfig().isInterleaving() != null || getPoolConfig().isNoTxSeparatePool() != null 
            || getPoolConfig().isOverrideIsSameRM() != null || getPoolConfig().isPadXid() != null 
            || getPoolConfig().isPrefill() != null || getPoolConfig().isUseStrictMin() != null 
            || getPoolConfig().isWrapXaResource() != null)
      {
         // FlushStrategy can not be null if any of other property is not null.
         getPoolConfig().setFlushStrategy(flushStrategy);
      }
      else
      {
         getPoolConfig().setFlushStrategy(null);
      }
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
      
      String blockingTimeoutErr = checkPositiveLong(blockingTimeoutStr, "Blocking Timeout");
      if (blockingTimeoutErr != null)
      {
         updateStatus(blockingTimeoutErr);
         return false;
      }
      
      String idleTimeoutErr = checkPositiveLong(idleTimeoutStr, "Idle Timeout");
      if (idleTimeoutErr != null)
      {
         updateStatus(idleTimeoutErr);
         return false;
      }
      
      String allocateRetryErr = checkPositiveInteger(allocateRetryStr, "Allocate Retry");
      if (allocateRetryErr != null)
      {
         updateStatus(allocateRetryErr);
         return false;
      }
      
      String allocateRetryWaitErr = checkPositiveLong(allocateRetryWaitStr, "Allocate Retry Wait");
      if (allocateRetryWaitErr != null)
      {
         updateStatus(allocateRetryWaitErr);
         return false;
      }
      
      String xaResTimeoutErr = checkPositiveInteger(xaResTimeoutStr, "XA Resource Timeout");
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
               return msgPrefix + " must be larger than 0.";
            }
         }
         catch (NumberFormatException nfe)
         {
            return msgPrefix + " must be a number.";
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
               return msgPrefix + " must be larger than 0.";
            }
         }
         catch (NumberFormatException nfe)
         {
            return msgPrefix + " must be a number.";
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
      String bgValidationMillsErr = checkPositiveLong(bgValidationMills, "Backgound Validation Millis");
      if (bgValidationMillsErr != null)
      {
         updateStatus(bgValidationMillsErr);
         return false;
      }
      updateStatus(null);
      return true;
   }

}
