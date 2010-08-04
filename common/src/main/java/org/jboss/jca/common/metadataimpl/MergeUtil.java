/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadataimpl;

import org.jboss.jca.common.api.metadata.ra.ConfigProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * A static class with Merge Utilities methods.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class MergeUtil
{
   /**
    *
    * Merge to ArrayList. The results is the union of the two arrays. Element present in left and right arrayList
    * (letft.equals(right) = true) are present only one and left ones are selected
    *
    * @param <T> the type of ArrayList elements
    * @param left left side ArrayList to merge
    * @param right right side ArrayList to merge
    * @return merged ArrayList
    */
   public static <T> ArrayList<T> mergeArrayList(ArrayList<T> left, ArrayList<T> right)
   {
      Set<T> newSet = new HashSet<T>((left == null ? 0 : left.size())
               + (right == null ? 0 : right.size()));
      if (left != null)
         newSet.addAll(left);
      if (right != null)
         newSet.addAll(right);
      ArrayList<T> newArrayList = new ArrayList<T>(newSet.size());
      newArrayList.addAll(newSet);
      return newArrayList;
   }

   /**
    *
    * F Merge to ArrayList of ConfigProperty. The results is the union of the two arrays.
    * Element present in left and right arrayList
    * (letft.equals(right) = true) are present only one and left ones are selected
    *
    *
    *  @param left left side ArrayList to merge
    * @param right right side ArrayList to merge
    * @return merged ArrayList
    */
   public static ArrayList<? extends ConfigProperty> mergeConfigArrayList(
         ArrayList<? extends ConfigProperty> left, ArrayList<? extends ConfigProperty> right)
   {
      ArrayList<ConfigProperty> newArrayList = new ArrayList<ConfigProperty>((left == null ? 0 : left.size())
            + (right == null ? 0 : right.size()));
      if (left == null)
      {
         if (right != null)
            newArrayList.addAll(right);
      }
      else
      {
         ArrayList<String> leftNames = new ArrayList<String>(left.size());

         for (ConfigProperty l : left)
         {
            newArrayList.add(l);
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
                  newArrayList.add(r);
            }
         }
      }
      newArrayList.trimToSize();
      return newArrayList;
   }
}
