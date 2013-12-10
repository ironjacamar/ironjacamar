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
import org.jboss.jca.eclipse.ResourceBundles;
import org.jboss.jca.eclipse.command.raui.ConnectionFactoryConfig.Extension;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * 
 * The composite used to edit Extension.
 * 
 * @author lgao lin.gao@ironjacamar.org
 *
 */
public class ExtensionComposite
{

   private final ResourceBundles res = ResourceBundles.getInstance();
   
   private final Shell shell;
   
   private final Extension extension;
   
   /**
    * Constructor of the composite.
    * 
    * @param shell the active shell
    * @param extension the extension for the configuration. not-null
    */
   public ExtensionComposite(Shell shell, Extension extension)
   {
      this.shell = shell;
      this.extension = extension;
      if (this.extension == null)
      {
         throw new IllegalArgumentException("Extension can not be null for the configuration.");
      }
   }

   /**
    * Creates a Control to edit the Extension configuration. 
    * 
    * @param parent parent composite
    * @return a new Composite
    */
   public Composite createControl(Composite parent)
   {
      final Composite whole = new Composite(parent, SWT.NONE);
      
      GridLayout groupLayout = new GridLayout();
      groupLayout.numColumns = 1;
      groupLayout.verticalSpacing = 2;
      whole.setLayout(groupLayout);
      
      Composite clsNameContainer = new Composite(whole, SWT.NONE);
      GridLayout clsNameLayout = new GridLayout();
      clsNameLayout.numColumns = 2;
      clsNameLayout.verticalSpacing = 9;
      clsNameLayout.makeColumnsEqualWidth = true;
      clsNameContainer.setLayout(clsNameLayout);
      
      // class name
      Label label = new Label(clsNameContainer, SWT.NULL);
      label.setText(res.getString("ra.generate.mcf.extension.classname"));
      final Text clsNameText = new Text(clsNameContainer, SWT.BORDER | SWT.SINGLE);
      GridData layoutData = new GridData();
      layoutData.widthHint = 300;
      clsNameText.setLayoutData(layoutData);
      final String initialClassName = getExtenstionClassName();
      if (initialClassName != null)
      {
         clsNameText.setText(initialClassName);
      }
      
      clsNameText.addModifyListener(new ModifyListener()
      {
         
         @Override
         public void modifyText(ModifyEvent e)
         {
            String inputClsName = clsNameText.getText();
            extension.setClassName(inputClsName);
         }
      });
      
      List<ConfigPropType> configProperties = this.extension.getConfigProperties();
      ConfigPropertyComposite configPropComposite = new ConfigPropertyComposite(shell, configProperties);
      configPropComposite.setEditOnly(false);
      configPropComposite.createControl(whole);
      return whole;
   }
   
   private String getExtenstionClassName()
   {
      return this.extension.getClassName();
   }
   
}
