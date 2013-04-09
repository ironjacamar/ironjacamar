/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.deployers.fungal;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The comparator for the RA activator
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class RAActivatorComparator implements Comparator<String>, Serializable
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    */
   RAActivatorComparator()
   {
   }

   /**
    * Compare
    * @param o1 The first object
    * @param o2 The second object
    * @return <code>-1</code> if o1 should be invoked first, <code>1</code> if o2 should
    *         be invoked first, otherwise <code>0</code>
    */
   public int compare(String o1, String o2)
   {
      boolean raXml1 = o1.endsWith("-ra.xml");
      boolean raXml2 = o2.endsWith("-ra.xml");

      if (raXml1 && !raXml2)
      {
         return -1;
      }
      else if (!raXml1 && raXml2)
      {
         return 1;
      }

      return o1.compareTo(o2);
   }

   /**
    * Hash code
    * @return The hash
    */
   public int hashCode()
   {
      return 42;
   }

   /**
    * Equals
    * @param o The object
    * @return True if equal; otherwise false
    */
   public boolean equals(Object o)
   {
      if (o == this)
         return true;

      if (o == null)
         return false;

      if (!(o instanceof RAActivatorComparator))
         return false;

      return true;
   }

}
