/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.impl.classloader;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Version comparator
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class VersionComparator implements Comparator<String>, Serializable
{
   /** Serial version UID */
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    */
   public VersionComparator()
   {
   }

   /**
    * Compare
    * @param o1 First object
    * @param o2 Second object
    * @return -1 if o1 is less than o2; 0 if o1 equals o2; 1 if o1 is greater than o2
    */
   public int compare(String o1, String o2)
   {
      if (o1 == null && o2 == null)
         return 0;

      if (o1 != null && o2 != null && o1.equals(o2))
         return 0;

      int oneMajor = 0;
      int oneMinor = 0;
      int onePatch = 0;

      int twoMajor = 0;
      int twoMinor = 0;
      int twoPatch = 0;

      if (o1 != null)
      {
         int index = o1.indexOf(".");

         if (index != -1)
         {
            try
            {
               oneMajor = Integer.valueOf(o1.substring(0, index)).intValue();
               
               int nextIndex = o1.indexOf(".", index + 1);
               
               if (nextIndex != -1)
               {
                  oneMinor = Integer.valueOf(o1.substring(index + 1, nextIndex)).intValue();

                  onePatch = Integer.valueOf(o1.substring(nextIndex + 1)).intValue();
               }
            }
            catch (NumberFormatException nfe)
            {
               // Ignore
            }
         }
      }

      if (o2 != null)
      {
         int index = o2.indexOf(".");

         if (index != -1)
         {
            try
            {
               twoMajor = Integer.valueOf(o2.substring(0, index)).intValue();
               
               int nextIndex = o2.indexOf(".", index + 1);
               
               if (nextIndex != -1)
               {
                  twoMinor = Integer.valueOf(o2.substring(index + 1, nextIndex)).intValue();

                  twoPatch = Integer.valueOf(o2.substring(nextIndex + 1)).intValue();
               }
            }
            catch (NumberFormatException nfe)
            {
               // Ignore
            }
         }
      }

      if (oneMajor < twoMajor)
      {
         return -1;
      }
      else if (oneMajor > twoMajor)
      {
         return 1;
      }

      if (oneMinor < twoMinor)
      {
         return -1;
      }
      else if (oneMinor > twoMinor)
      {
         return 1;
      }

      if (onePatch < twoPatch)
      {
         return -1;
      }
      else if (onePatch > twoPatch)
      {
         return 1;
      }

      return 0;
   }

   /**
    * Equals
    * @param other The other object
    * @return True if equal; otherwise false
    */
   public boolean equals(Object other)
   {
      if (other == null)
         return false;

      if (!(other instanceof VersionComparator))
         return false;

      return true;
   }

   /**
    * Hash code
    * @return The hash
    */
   public int hashCode()
   {
      return 42;
   }
}
