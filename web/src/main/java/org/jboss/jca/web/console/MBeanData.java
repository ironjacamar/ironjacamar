/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.web.console;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

/**
 * A MBean ObjectName and MBeanInfo pair that is orderable by ObjectName.
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MBeanData implements Comparable
{
   private ObjectName objectName;
   private MBeanInfo metaData;

   /**
    * Constructor
    */
   public MBeanData()
   {
   }

   /**
    * Constructor
    * @param objectName The object name
    * @param metaData The metadata about the bean
    */
   public MBeanData(ObjectName objectName, MBeanInfo metaData)
   {
      this.objectName = objectName;
      this.metaData = metaData;
   }

   /**
    * Get the object name
    * @return The name
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }
   
   /**
    * Set the object name
    * @param objectName The new value
    */
   public void setObjectName(ObjectName objectName)
   {
      this.objectName = objectName;
   }

   /**
    * Get the metadata
    * @return The data
    */
   public MBeanInfo getMetaData()
   {
      return metaData;
   }
   
   /**
    * Set the metadata
    * @param metaData The new value
    */
   public void setMetaData(MBeanInfo metaData)
   {
      this.metaData = metaData;
   }

   /**
    * Get the string repsentation of the object name
    * @return The string
    */
   public String getName()
   {
      return objectName.toString();
   }

   /**
    * Get the key propertues listing
    * @return The keys
    */
   public String getNameProperties()
   {
      return objectName.getCanonicalKeyPropertyListString();
   }

   /**
    * Get the class name
    * @return The class name
    */
   public String getClassName()
   {
      return metaData.getClassName();
   }

   /**
    * Compares MBeanData based on the ObjectName domain name and canonical
    * key properties
    *
    * @param o the MBeanData to compare against
    * @return < 0 if this is less than o, > 0 if this is greater than o,
    *    0 if equal.
    */
   public int compareTo(Object o)
   {
      MBeanData md = (MBeanData) o;
      String d1 = objectName.getDomain();
      String d2 = md.objectName.getDomain();
      int compare = d1.compareTo(d2);
      if (compare == 0)
      {
         String p1 = objectName.getCanonicalKeyPropertyListString();
         String p2 = md.objectName.getCanonicalKeyPropertyListString();
         compare = p1.compareTo(p2);
      }
      return compare;
   }

   /**
    * Hash code
    * @return The hash
    */
   public int hashCode()
   {
      return super.hashCode();
   }

   /**
    * Equals
    * @param o The other object
    * @return True if the two object are equal; otherwise false
    */
   public boolean equals(Object o)
   {
      if (o == null || (!(o instanceof MBeanData)))
         return false;

      if (this == o)
         return true;

      return (this.compareTo(o) == 0);
   }
}
