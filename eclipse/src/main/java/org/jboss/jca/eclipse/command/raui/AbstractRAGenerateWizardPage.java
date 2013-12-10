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

import org.jboss.jca.eclipse.ResourceBundles;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridLayout;

/**
 * Abstract wizard page for generating *-ra.xml.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public abstract class AbstractRAGenerateWizardPage extends WizardPage
{
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
