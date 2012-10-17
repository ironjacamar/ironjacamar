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
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * The page is used to configure AdminObject.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class RAGenerateAOPage extends AbstractRAGenerateWizardPage
{
   private final String aoClsName;
   
   private final CommonAdminObject commonAO;
   
   private final ConnectorHelper connectorHelper;
   
   private final AdminObjectConfig aoConfig;
   
   private final List<? extends ConfigProperty> intialAOConfigProperties;
   
   private final ResourceAdapterConfig raConfig;
   
   
   // components which need to be validate
   private Text jndiText;
   private Button useJavaCtxBtn;
   
   /**
    * The constructor.
    * 
    * @param wizard the RAGenerateWizard
    * @param ao the AdminObject
    */
   public RAGenerateAOPage(RAGenerateWizard wizard, AdminObject ao)
   {
      super("AOPage");
      this.aoClsName = ao.getAdminobjectClass().getValue();
      this.intialAOConfigProperties = ao.getConfigProperties();
      this.connectorHelper = wizard.getConnectorHelper();
      this.raConfig = wizard.getRaConfig();
      this.commonAO = this.connectorHelper.getCommonAdminObject(ao);
      setTitle("Information about Admin Object");
      setDescription("Configure Admin Object: " + this.aoClsName);
      this.aoConfig = new AdminObjectConfig();
      // class name and jndi name are required.
      this.aoConfig.setClssName(aoClsName);
      this.aoConfig.setJndiName(getInitialJndiName());
      if (isInitialActive())
      {
         this.raConfig.getAdminObjectConfigs().add(aoConfig);
      }
   }

   @Override
   public void createControl(Composite parent)
   {
      final Composite whole = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      whole.setLayout(layout);
      layout.numColumns = 1;

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
      
      
      final Group generalGrp = new Group(whole, SWT.SHADOW_IN);
      generalGrp.setText("General Information");
      generalGrp.setLayout(getLayout());

      // class name
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Class Name:");
      Label clsNameLabel = new Label(generalGrp, SWT.NULL);
      clsNameLabel.setText(this.aoClsName);
      
      // jndi name
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Jndi Name:");
      final String jndi = this.getInitialJndiName();
      
      jndiText = createText(generalGrp, jndi);
      jndiText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            String jndiName = jndiText.getText().trim();
            if (isInputValid())
            {
               aoConfig.setJndiName(jndiName);
            }
         }
      });
      
      // pool name
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Pool Name:");
      final String poolName = this.getInitialPoolName();
      final Text poolNameText = createText(generalGrp, poolName);
      poolNameText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            String newPoolName = poolNameText.getText().trim();
            if (newPoolName != null && newPoolName.length() > 0)
            {
               if (!newPoolName.equals(getInitialPoolName()))
               {
                  aoConfig.setPoolName(newPoolName);
               }
               else
               {
                  aoConfig.setPoolName(null);
               }
            }
         }
      });
      
      // enabled
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Enabled:");
      final Boolean enabled = this.isInitialEnabled();
      
      final Button enableBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      enableBtn.setSelection(enabled.booleanValue());
      enableBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (enabled ^ enableBtn.getSelection())
            {
               aoConfig.setEnabled(enableBtn.getSelection());
            }
            else
            {
               aoConfig.setEnabled(null);
            }
         }
      });
      
      // use java context
      label = new Label(generalGrp, SWT.NULL);
      label.setText("Use Java Context:");
      final Boolean useJavaCtx = this.isInitialUseJavaCtx();
      useJavaCtxBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      useJavaCtxBtn.setSelection(useJavaCtx);
      useJavaCtxBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (isInputValid())
            {
               if (useJavaCtx ^ useJavaCtxBtn.getSelection())
               {
                  aoConfig.setUseJavaCtx(useJavaCtxBtn.getSelection());
               }
               else
               {
                  aoConfig.setUseJavaCtx(null);
               }
            }
         }
      });
      
      // config properties
      final TableViewer configPropTabViewer = 
            createConfigPropertyTableViewer(whole, this.connectorHelper.getConfigProps(this.intialAOConfigProperties));
      
      // active or not
      activeBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            boolean active = activeBtn.getSelection();
            generalGrp.setEnabled(active);
            List<AdminObjectConfig> aoConfigs = raConfig.getAdminObjectConfigs();
            if (active)
            {
               if (!aoConfigs.contains(aoConfig))
               {
                  aoConfigs.add(aoConfig);
               }
            }
            else
            {
               aoConfigs.remove(aoConfig);
            }
            configPropTabViewer.getTable().setEnabled(active);
            whole.update();
         }

      });
      activeBtn.setSelection(isInitialActive());
      
      setControl(whole);
   }
   
   /**
    * Valid whether input values are correct.
    * 
    * @return true if all input are OK, otherwise, false.
    */
   private boolean isInputValid()
   {
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
         updateStatus("Jndi Name of AdminObject can not be empty.");
         return false;
      }
   }
   
   @Override
   protected void onConfigPropUpdated(TableViewer configPropsTableView, ConfigPropType prop)
   {
      List<ConfigPropType> configProps = aoConfig.getConfigProps();
      if (configProps == null)
      {
         configProps = new ArrayList<ConfigPropType>();
      }
      if (!configProps.contains(prop))
      {
         configProps.add(prop);
      }
   }
   
   /**
    * Gets default jndi name.
    * 
    * @return the jndi name
    */
   public String getInitialJndiName()
   {
      if (this.commonAO != null)
      {
         return this.commonAO.getJndiName();
      }
      return null;
   }
   
   /**
    * Gets default pool name.
    * 
    * @return the pool name
    */
   public String getInitialPoolName()
   {
      if (this.commonAO != null)
      {
         return this.commonAO.getPoolName();
      }
      return null;
   }
   
   /**
    * Gets initial enabled state.
    * 
    * @return initial enabled state.
    */
   public Boolean isInitialEnabled()
   {
      if (this.commonAO != null)
      {
         return this.commonAO.isEnabled();
      }
      return Boolean.valueOf(false);
   }
   
   /**
    * Gets initial use Java Context state
    * @return the initial state of use Java Context
    */
   public Boolean isInitialUseJavaCtx()
   {
      if (this.commonAO != null)
      {
         return this.commonAO.isUseJavaContext();
      }
      return Boolean.valueOf(false);
   }
   
   
   private boolean isInitialActive()
   {
      return this.commonAO != null;
   }

}
