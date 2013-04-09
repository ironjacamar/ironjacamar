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
 *
 */
public class BeanValidationGroupContentProvider implements IStructuredContentProvider
{
   
   private final TableViewer tableViewer;
   
   private final ResourceAdapterConfig raConfig;
   
   private final List<String> initalBeanValidationGroups;
   
   private List<Object> elements = new ArrayList<Object>();
   
   /**
    * The constructor
    * @param tableViewer the TableViewer
    * @param raConfig the ResourceAdapterConfig
    * @param connectorHelper the ConnectorHelper
    */
   public BeanValidationGroupContentProvider(TableViewer tableViewer, ResourceAdapterConfig raConfig, 
         ConnectorHelper connectorHelper)
   {
      super();
      this.tableViewer = tableViewer;
      this.raConfig = raConfig;
      this.initalBeanValidationGroups = connectorHelper.getBeanValidationGrp();
      if (null != this.initalBeanValidationGroups)
      {
         for (String str : initalBeanValidationGroups)
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
   
   private void addToUI(String beanValidationGrp)
   {
      if (!elements.contains(beanValidationGrp))
      {
         this.elements.add(beanValidationGrp);
         this.tableViewer.add(beanValidationGrp);
         tableViewer.setSelection(new StructuredSelection(beanValidationGrp), true);
      }
   }
   
   /**
    * Adds bean validation group.
    * 
    * @param beanValidationGrp the bean validation group
    */
   public void add(String beanValidationGrp)
   {
      addToUI(beanValidationGrp);
      if (!this.raConfig.getBeanValidationGroups().contains(beanValidationGrp))
      {
         this.raConfig.getBeanValidationGroups().add(beanValidationGrp);
      }
   }
   
   /**
    * Removes the bean validation group.
    * 
    * @param beanValidationGrp the bean validation group
    */
   public void remove(String beanValidationGrp)
   {
      this.elements.remove(beanValidationGrp);
      this.raConfig.getBeanValidationGroups().remove(beanValidationGrp);
      this.tableViewer.remove(beanValidationGrp);
   }
   
   /**
    * Updates the bean validation group
    * 
    * @param oldBeanValidationGrp the old bean validation group
    * @param newBeanValidationGrp the new bean validation group
    */
   public void update(String oldBeanValidationGrp, String newBeanValidationGrp)
   {
      int oldIdx = elements.indexOf(oldBeanValidationGrp);
      int newIdx = elements.indexOf(newBeanValidationGrp);
      if (oldIdx != -1 && newIdx == -1)
      {
         this.elements.set(oldIdx, newBeanValidationGrp);
         this.tableViewer.setInput(elements);
         tableViewer.setSelection(new StructuredSelection(newBeanValidationGrp), true);
      }
      oldIdx = raConfig.getBeanValidationGroups().indexOf(oldBeanValidationGrp);
      newIdx = raConfig.getBeanValidationGroups().indexOf(newBeanValidationGrp);
      if (oldIdx != -1 && newIdx == -1)
      {
         raConfig.getBeanValidationGroups().set(oldIdx, newBeanValidationGrp);
      }
      else if (newIdx == -1)
      {
         raConfig.getBeanValidationGroups().add(newBeanValidationGrp);
      }
   }

}
