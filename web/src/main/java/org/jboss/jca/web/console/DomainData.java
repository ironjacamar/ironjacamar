/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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

import java.util.Arrays;
import java.util.TreeSet;

/**
 * The MBeanData for a given JMX domain name
 *
 * @author <a href="mailto:sstark@redhat.com">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DomainData
{
   private String domainName;
   private TreeSet<MBeanData> domainData;

   /**
    * Constructor
    * @param domainName The domain name
    */
   public DomainData(String domainName)
   {
      this.domainName = domainName;
      this.domainData = new TreeSet<MBeanData>();
   }

   /**
    * Constructor
    * @param domainName The domain name
    * @param data The MBean data
    */
   public DomainData(String domainName, MBeanData[] data)
   {
      this(domainName);

      domainData.addAll(Arrays.asList(data));
   }
   
   /**
    * Get the domain name
    * @return The name
    */
   public String getDomainName()
   {
      return domainName;
   }

   /**
    * Get the data
    * @return The data
    */
   public MBeanData[] getData()
   {
      MBeanData[] data = new MBeanData[domainData.size()];
      domainData.toArray(data);
      return data;
   }

   /**
    * Add data
    * @param data The MBean data entry
    */
   public void addData(MBeanData data)
   {
      domainData.add(data);
   }

   /**
    * Hash code
    * @return The hash
    */
   public int hashCode()
   {
      return domainName.hashCode();
   }

   /**
    * Equals
    * @param obj The other object
    * @return True if the two object are equal; otherwise false
    */
   public boolean equals(Object obj)
   {
      if (obj == null || (!(obj instanceof DomainData)))
         return false;

      if (this == obj)
         return true;

      DomainData data = (DomainData)obj;
      return domainName.equals(data.domainName);
   }
}
