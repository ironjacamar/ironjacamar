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

   private final AdminObjectConfig aoConfig;
   
   // components which need to be validate
   private Text jndiText;
   private Button useJavaCtxBtn;
   
   /**
    * The constructor.
    * 
    * @param aoConfig the AdminObjectConfig
    */
   public RAGenerateAOPage(AdminObjectConfig aoConfig)
   {
      super("AOPage");
      this.aoConfig = aoConfig;
      setTitle(getString("ra.generate.ao.title"));
      setDescription(getString("ra.generate.ao.description", this.aoConfig.getClssName()));
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
      label.setText(getString("ra.generate.active.label"));
      
      final Button activeBtn = new Button(container, SWT.CHECK);
      
      final Group generalGrp = new Group(whole, SWT.SHADOW_IN);
      generalGrp.setText(getString("ra.generate.general.group"));
      generalGrp.setLayout(getLayout());

      // class name
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.class.name"));
      Label clsNameLabel = new Label(generalGrp, SWT.NULL);
      clsNameLabel.setText(this.aoConfig.getClssName());
      
      // jndi name
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.jndi.name"));
      final String jndi = this.aoConfig.getJndiName();
      
      jndiText = UIResources.createText(generalGrp, jndi);
      jndiText.addModifyListener(new ModifyListener()
      {
         @Override
         public void modifyText(ModifyEvent e)
         {
            String jndiName = jndiText.getText().trim();
            if (checkInput())
            {
               aoConfig.setJndiName(jndiName);
            }
         }
      });
      
      // pool name
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.pool.name"));
      final String poolName = this.aoConfig.getPoolName();
      final Text poolNameText = UIResources.createText(generalGrp, poolName);
      poolNameText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            String newPoolName = poolNameText.getText().trim();
            aoConfig.setPoolName(newPoolName);
         }
      });
      
      // enabled
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.enabled"));
      final Boolean enabled = this.aoConfig.isEnabled();
      final Button enableBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      enableBtn.setSelection(enabled != null ? enabled : Boolean.TRUE);
      
      enableBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            aoConfig.setEnabled(enableBtn.getSelection());
         }
      });
      
      // use java context
      label = new Label(generalGrp, SWT.NULL);
      label.setText(getString("ra.generate.use.java.context"));
      final Boolean useJavaCtx = this.aoConfig.isUseJavaCtx();
      useJavaCtxBtn = new Button(generalGrp, SWT.BORDER | SWT.CHECK);
      useJavaCtxBtn.setSelection(useJavaCtx != null ? useJavaCtx : Boolean.TRUE);
      
      useJavaCtxBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            if (checkInput())
            {
               aoConfig.setUseJavaCtx(useJavaCtxBtn.getSelection());
            }
         }
      });
      
      ConfigPropertyComposite configComposite = new ConfigPropertyComposite(getShell(), 
            this.aoConfig.getConfigProps());
      final Composite configPropPanel = configComposite.createControl(whole);
      
      // active or not
      activeBtn.addSelectionListener(new SelectionAdapter()
      {
         @Override
         public void widgetSelected(SelectionEvent e)
         {
            boolean active = activeBtn.getSelection();
            generalGrp.setEnabled(active);
            configPropPanel.setEnabled(active);
            aoConfig.setActive(active);
            if (active)
            {
               checkInput();
            }
            else
            {
               updateStatus(null);
            }
            whole.update();
         }

      });
      activeBtn.setSelection(this.aoConfig.isActive());
      generalGrp.setEnabled(this.aoConfig.isActive());
      configPropPanel.setEnabled(this.aoConfig.isActive());
      setControl(whole);
   }
   
   /**
    * Valid whether input values are correct.
    * 
    * @return true if all input are OK, otherwise, false.
    */
   private boolean checkInput()
   {
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
}
