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

import org.jboss.jca.eclipse.wizards.PropsLabelProvider;

import java.util.Map;

/**
 * 
 * @author lgao lin.gao@ironjacamar.org
 *
 */
public class LabelProvider extends PropsLabelProvider
{
   
   @Override
   public String getColumnText(Object element, int columnIndex)
   {
      if (element instanceof Map)
      {
         return getTextOfMapCol(element, columnIndex);
      }
      else if (element.getClass().isArray())
      {
         return getTextOfArray(element, columnIndex);
      }
      return super.getColumnText(element, columnIndex);
   }

   private String getTextOfArray(Object element, int columnIndex)
   {
      Object[] array = (Object[])element;
      switch (columnIndex)
      {
         case 0 :
            return array[0].toString();
         case 1 :
            return array[1].toString();
      }
      return element.toString();
   }

   private String getTextOfMapCol(Object element, int columnIndex)
   {
      Map<?, ?> map = (Map<?, ?>)element;
      for (Map.Entry<?, ?> entry: map.entrySet()) // only the first element which the key is not null.
      {
         Object key = entry.getKey();
         Object value = entry.getValue();
         String valueStr = value != null ? value.toString() : "";
         if (key != null)
         {
            switch (columnIndex)
            {
               case 0 :
                  return key.toString();
               case 1 :
                  return valueStr;
            }
         }
      }
      return element.toString();
   }

}
