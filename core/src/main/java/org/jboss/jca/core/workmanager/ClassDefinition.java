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

package org.jboss.jca.core.workmanager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A definition of a class
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ClassDefinition implements Serializable
{
   /** SerialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The name */
   private String name;

   /** The serialVersionUID */
   private long svu;

   /** The data */
   private byte[] data;


   private static final String NAME = "NAME";
   private static final String SVU = "SVU";
   private static final String DATA = "DATA";


   /**
    * create an instance from a Map
    * @param map the map
    * @return the instance
    */
   public static ClassDefinition fromMap(Map<String, Object> map)
   {
      return new ClassDefinition((String) map.get(NAME), (Long) map.get(SVU), (byte[]) map.get(DATA));

   }

   /**
    * return a map representing the instance
    * @return the map
    */
   public Map<String, Object> toMap()
   {
      Map<String, Object> returnMap = new LinkedHashMap<String, Object>(3);
      returnMap.put(NAME, this.getName());
      returnMap.put(SVU, this.getSerialVersionUID());
      returnMap.put(DATA, this.getData());
      return returnMap;
   }

   /**
    * Constructor
    * @param name The name of the class
    * @param serialVersionUID The serial version unique identifier
    * @param data The class
    */
   public ClassDefinition(String name, long serialVersionUID, byte[] data)
   {
      this.name = name;
      this.svu = serialVersionUID;
      this.data = new byte[data.length];
      
      System.arraycopy(data, 0, this.data, 0, data.length);
   }

   /**
    * Get the name
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the serial version identifier
    * @return The value
    */
   public long getSerialVersionUID()
   {
      return svu;
   }

   /**
    * Get the data
    * @return The value
    */
   public byte[] getData()
   {
      byte[] copy = new byte[data.length];
      System.arraycopy(data, 0, copy, 0, data.length);
      return copy;
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;

      result += 7 * name.hashCode();
      result += 7 * svu;
      result += 7 * Arrays.hashCode(data);

      return result;
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;

      if (other == this)
         return true;

      if (!(other instanceof ClassDefinition))
         return false;

      ClassDefinition cd = (ClassDefinition)other;

      if (!name.equals(cd.name))
         return false;

      if (svu != cd.svu)
         return false;

      return Arrays.equals(data, cd.data);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ClassDefinition@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[name=").append(name);
      sb.append(" serialVersionUID=").append(svu);
      sb.append(" data=").append(Arrays.toString(data));
      sb.append("]");

      return sb.toString();
   }
}
