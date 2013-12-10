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

import org.jboss.jca.eclipse.ResourceBundles;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;


/**
 * This provides methods to create UI components used by *-ra.xml generation.
 * 
 * @author lgao lin.gao@ironjacamar.org
 *
 */
final class UIResources
{

   /**
    * The Label Provider for table
    */
   private static final LabelProvider labelProvider = new LabelProvider();
   
   /**
    * No public constructor.
    */
   private UIResources()
   {
      ;
   }
   
   /**
    * Gets string of a resource bundle key.
    * 
    * @param key the resource bundle key
    * @return the string associated with the key
    */
   public static String getString(String key)
   {
      ResourceBundles res = ResourceBundles.getInstance();
      return res.getString(key);
   }
   
   /**
    * Creates a TableViewer with provided table headers and column layout data.
    * 
    * @param parent the parent
    * @param tableHeaders the table headers
    * @param tableColumnLayouts the table column layout data
    * @return a TableViewer
    */
   public static TableViewer createTableViewer(Composite parent, String tableHeaders[], 
         ColumnLayoutData[] tableColumnLayouts)
   {
      Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
      GridData data = new GridData(GridData.FILL_BOTH);
      data.widthHint = 600;
      data.heightHint = 80;
      table.setLayoutData(data);
      table.setFont(parent.getFont());
      TableViewer tableViewer = new TableViewer(table);
      tableViewer.setLabelProvider(labelProvider);

      TableLayout tableLayout = new TableLayout();
      table.setLayout(tableLayout);
      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      for (int i = 0; i < tableHeaders.length; i++)
      {
         tableLayout.addColumnData(tableColumnLayouts[i]);
         TableColumn column = new TableColumn(table, SWT.NONE, i);
         column.setResizable(tableColumnLayouts[i].resizable);
         column.setText(tableHeaders[i]);
      }

      return tableViewer;
   }
   
   /**
    * Creates and returns a configured button in the given composite with the given label.
    * @param parent the parent
    * @param label label
    * @return the Button
    */
   public static Button createPushButton(Composite parent, String label)
   {
      Button button = new Button(parent, SWT.PUSH);
      button.setFont(parent.getFont());
      button.setText(label);

      GridData gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
      button.setLayoutData(gridData);
      return button;
   }
   
   /**
    * Creates general Text with initial value.
    * 
    * @param parent the parent
    * @param value the initial value
    * @return the Text
    */
   public static Text createText(Composite parent, Object value)
   {
      Text txt = new Text(parent, SWT.BORDER | SWT.SINGLE);
      if (value != null)
      {
         txt.setText(value.toString());
      }
      GridData layoutData = new GridData();
      layoutData.widthHint = 300;
      txt.setLayoutData(layoutData);
      return txt;
   }
   
}
