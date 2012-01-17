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
package org.jboss.jca.eclipse.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Content provider that maintains a generic list of objects which are shown in
 * a table viewer.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class PropsContentProvider implements IStructuredContentProvider
{
   private List elements = new ArrayList();

   private TableViewer tableViewer;

   private ViewerComparator fComparator = null;

   private boolean defaultcomparator = true;

   /**
    * Default Constructor
    * 
    * @param tableViewer TableViewer
    */
   public PropsContentProvider(TableViewer tableViewer)
   {
      this.tableViewer = tableViewer;
   }

   /**
    * Constructor
    * 
    * @param defaultcomparator if the default comparator should be used for the returned data
    * @since 3.5
    */
   public PropsContentProvider(boolean defaultcomparator)
   {
      this.defaultcomparator = defaultcomparator;
   }

   /**
    * Add an element to the current listing of elements and to the underlying viewer. Does nothing
    * if the listing already contains the given element
    * @param o object
    */
   public void add(Object o)
   {
      if (elements.contains(o))
      {
         return;
      }
      elements.add(o);
      tableViewer.add(o);
      tableViewer.setSelection(new StructuredSelection(o), true);
   }

   /**
    * @see org.eclipse.jface.viewers.IContentProvider#dispose()
    */
   public void dispose()
   {
   }

   /**
    * getElements
    * 
    * @param inputElement object
    * @return Object[] array of object
    * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
    */
   public Object[] getElements(Object inputElement)
   {
      return elements.toArray();
   }

   /**
    * inputChanged
    * 
    * @param viewer Viewer
    * @param oldInput Object
    * @param newInput Object
    * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, 
    * java.lang.Object, java.lang.Object)
    */
   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
   }

   /**
    * update
    * @param o Object
    */
   public void update(Object o)
   {
      tableViewer.update(o, null);
   }

   /**
    * Removes the given element from the listing of elements and from the backing viewer.
    * @param o Object
    */
   public void remove(Object o)
   {
      elements.remove(o);
      tableViewer.remove(o);
   }

   /**
    * Removes the given selection of the listing of elements and from the backing viewer
    * @param selection IStructuredSelection
    */
   public void remove(IStructuredSelection selection)
   {
      Object[] array = selection.toArray();
      elements.removeAll(Arrays.asList(array));
      tableViewer.remove(array);
   }

   /**
    * Returns the default comparator which compares the {@link #toString()} value of the elements for ordering
    * @return the default comparator
    */
   protected ViewerComparator getComparator()
   {
      if (fComparator == null)
      {
         fComparator = new ViewerComparator()
         {
            /* (non-Javadoc)
             * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, 
             * java.lang.Object, java.lang.Object)
             */
            public int compare(Viewer viewer, Object e1, Object e2)
            {
               return e1.toString().compareToIgnoreCase(e2.toString());
            }
         };
      }
      return fComparator;
   }
}
