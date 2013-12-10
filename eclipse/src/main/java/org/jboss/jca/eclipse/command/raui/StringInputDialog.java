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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog used to add or edit strings
 * 
 * This is a modal dialog, after dispose, the <code>getValues()</code> method can be called
 * to get latest string values.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class StringInputDialog extends Dialog
{

   private String title = null;
   
   /**
    * Key is the I18N key, same key is assumed as same text input
    * Value is the string value
    */
   private Map<String, String> values = new HashMap<String, String>();
   
   private final ResourceBundles res = ResourceBundles.getInstance();
   
   /**
    * Constructor.
    * 
    * @param shell the shell
    * @param title the title
    * @param initialValues the initial values
    */
   public StringInputDialog(Shell shell, String title, Map<String, String> initialValues)
   {
      super(shell);
      this.title = title;
      if (initialValues != null)
      {
         this.values = initialValues;
      }
   }
   
   @Override
   protected Control createDialogArea(Composite parent)
   {
      Composite comp = (Composite) super.createDialogArea(parent);
      ((GridLayout) comp.getLayout()).numColumns = 2;
      
      for (final Map.Entry<String, String> entry: this.values.entrySet())
      {
         Label label = new Label(comp, SWT.NULL);
         label.setText(res.getString(entry.getKey()));
         
         final Text text = new Text(comp, SWT.SINGLE | SWT.BORDER);
         
         GridData data = new GridData(GridData.FILL_BOTH);
         data.widthHint = 200;
         text.setLayoutData(data);
         
         if (entry.getValue() != null)
         {
            text.setText(entry.getValue());
         }
         text.addModifyListener(new ModifyListener()
         {
            
            @Override
            public void modifyText(ModifyEvent e)
            {
               values.put(entry.getKey(), text.getText());
            }
         });
      }
      return comp;
   }
   
   @Override
   protected void configureShell(Shell newShell)
   {
      super.configureShell(newShell);
      if (title != null)
      {
         newShell.setText(title);
      }
   }
   
   /**
    * Gets latest string values
    * 
    * @return the string values in the input text boxes
    */
   public Map<String, String> getValues ()
   {
      return this.values;
   }

}
