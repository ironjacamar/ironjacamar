/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.common.api.metadata;

import org.ironjacamar.common.api.metadata.spec.ConfigProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * A static class with Merge Utilities methods.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class MergeUtil
{
   /**
    *
    * Merge to List. The results is the union of the two arrays. Element present in left and right List
    * (letft.equals(right) = true) are present only one and left ones are selected
    *
    * @param <T> the type of List elements
    * @param left left side List to merge
    * @param right right side List to merge
    * @return merged List
    */
   public static <T> List<T> mergeList(List<T> left, List<T> right)
   {
      Set<T> newSet = new HashSet<T>((left == null ? 0 : left.size())
               + (right == null ? 0 : right.size()));
      if (left != null)
         newSet.addAll(left);
      if (right != null)
         newSet.addAll(right);
      List<T> newList = new ArrayList<T>(newSet.size());
      newList.addAll(newSet);
      return newList;
   }

   /**
    *
    * F Merge to List of ConfigProperty. The results is the union of the two arrays.
    * Element present in left and right List
    * (letft.equals(right) = true) are present only one and left ones are selected
    *
    *
    *  @param left left side List to merge
    * @param right right side List to merge
    * @return merged List
    */
   public static List<ConfigProperty> mergeConfigList(
         List<ConfigProperty> left, List<ConfigProperty> right)
   {
      ArrayList<ConfigProperty> newList = new ArrayList<ConfigProperty>((left == null ? 0 : left.size())
            + (right == null ? 0 : right.size()));
      if (left == null)
      {
         if (right != null)
            newList.addAll(right);
      }
      else
      {
         List<String> leftNames = new ArrayList<String>(left.size());

         for (ConfigProperty l : left)
         {
            newList.add(l);
            leftNames.add(l.getConfigPropertyName().getValue());
         }
         if (right != null)
         {
            for (ConfigProperty r : right)
            {
               boolean toAdd = true;
               for (String name : leftNames)
               {
                  if (name.equals(r.getConfigPropertyName().getValue()))
                     toAdd = false;
               }
               if (toAdd)
                  newList.add(r);
            }
         }
      }
      newList.trimToSize();
      return newList;
   }


}
