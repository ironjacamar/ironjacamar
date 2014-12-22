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

import org.ironjacamar.common.api.metadata.spec.XsdString;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A CopyUtil.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class CopyUtil
{

   /**
    *
    * clone a list of IdDecoratedMetadata and deep into the elements
    *
    * @param <T> type extending {@link CopyableMetaData}
    * @param list the list to clone
    * @return cloned list
    */

   public static <T extends CopyableMetaData> List<T> cloneList(List<T> list)
   {
      List<T> clone = new ArrayList<T>(list.size());
      for (T item : list)
         clone.add(clone(item));
      return clone;
   }

   /**
    *
    * clone a list of string deep into elements
    *
    * @param list the list to clone
    * @return cloned list
    */
   public static List<String> cloneListOfStrings(List<String> list)
   {
      List<String> clone = new ArrayList<String>(list.size());
      for (String item : list)
         clone.add(cloneString(item));
      return clone;
   }

   /**
    *
    * clone a String
    *
    * @param input the input
    * @return cloned String
    */
   public static String cloneString(String input)
   {
      return input;
   }

   /**
    *
    * Clone an IDDecoratedMetadata taking care of null cheks
    *
    * @param <T> type extending {@link CopyableMetaData}
    * @param input type extending IdDecoratedMetadata
    * @return cloned input
    *
    */
   @SuppressWarnings("unchecked")
   public static <T extends CopyableMetaData> T clone(T input)
   {
      if (input instanceof XsdString)
      {
         return XsdString.isNull((XsdString) input) ? (T) XsdString.NULL_XSDSTRING : (T) input.copy();
      }
      else
      {
         return input == null ? null : (T) input.copy();
      }
   }

}
