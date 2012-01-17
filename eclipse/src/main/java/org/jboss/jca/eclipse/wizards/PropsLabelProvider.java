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

import org.jboss.jca.codegenerator.ConfigPropType;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for type elements
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class PropsLabelProvider extends LabelProvider implements ITableLabelProvider
{

   /**
    * Method declared on IBaseLabelProvider.
    */
   @Override
   public void dispose()
   {
   }

   /**
    * Method declared on ITableLabelProvider.
    * 
    * @param element object
    * @param columnIndex integer
    * @return Image image reference
    */
   @Override
   public Image getColumnImage(Object element, int columnIndex)
   {
      if (columnIndex != 0)
      {
         return null;
      }

      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
   }

   /**
    * Method declared on ITableLabelProvider.
    * 
    * @param element object
    * @param columnIndex integer
    * @return String column text
    */
   @Override
   public String getColumnText(Object element, int columnIndex)
   {

      if (element instanceof ConfigPropType)
      {
         return getPropertyText((ConfigPropType) element, columnIndex);
      }
      return element.toString();
   }

   /**
    * getPropertyText
    * 
    * @param property ConfigPropType
    * @param columnIndex int
    * @return string
    */
   public String getPropertyText(ConfigPropType property, int columnIndex)
   {
      switch (columnIndex)
      {
         case 0 :
            return property.getName();
         case 1 :
            return property.getType();
         case 2 :
            return property.getValue();
      }
      return "";
   }

}
