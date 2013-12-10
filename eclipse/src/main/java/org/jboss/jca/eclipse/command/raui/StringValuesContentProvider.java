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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider that maintains a generic list of objects which are shown in
 * a table viewer.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 * @param <T> the object type the table contains for each row
 */
public class StringValuesContentProvider<T> implements IStructuredContentProvider
{
   
   private final TableViewer tableViewer;
   
   private final List<T> initialValues;
   
   private List<T> elements = new ArrayList<T>();
   
   /**
    * The constructor
    * @param tableViewer the TableViewer
    * @param initialValues the initial values
    */
   public StringValuesContentProvider(TableViewer tableViewer, List<T> initialValues)
   {
      super();
      this.tableViewer = tableViewer;
      this.initialValues = initialValues;
      if (null != this.initialValues)
      {
         for (T str : initialValues)
         {
            addToUI(str);
         }
      }
   }

   @Override
   public void dispose()
   {
      ;
   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
      ;
   }

   @Override
   public Object[] getElements(Object inputElement)
   {
      return elements.toArray();
   }
   
   private void addToUI(T rowStr)
   {
      if (!elements.contains(rowStr))
      {
         this.elements.add(rowStr);
         this.tableViewer.add(rowStr);
         tableViewer.setSelection(new StructuredSelection(rowStr), true);
      }
   }
   
   /**
    * Adds an object
    * 
    * @param t an object
    */
   public void add(T t)
   {
      addToUI(t);
   }
   
   /**
    * Removes an object
    * 
    * @param t an object
    */
   public void remove(T t)
   {
      this.elements.remove(t);
      this.tableViewer.remove(t);
   }
   
   /**
    * Updates the object
    * 
    * @param oldT the old object
    * @param newT the new object
    */
   public void update(T oldT, T newT)
   {
      int oldIdx = elements.indexOf(oldT);
      int newIdx = elements.indexOf(newT);
      if (oldIdx != -1 && newIdx == -1)
      {
         this.elements.set(oldIdx, newT);
         this.tableViewer.setInput(elements);
         tableViewer.setSelection(new StructuredSelection(newT), true);
      }
   }

}
