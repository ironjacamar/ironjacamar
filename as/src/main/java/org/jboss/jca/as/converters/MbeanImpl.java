/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.converters;

import java.util.HashMap;
import java.util.Map;

/**
 * A LegacyConnectionFactoryImp impl.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */

public class MbeanImpl implements Mbean
{

   private String code;
   private String name;
   private Map<String, String> attributes;
   
   /**
    * constructor
    * 
    * @param code code
    * @param name name 
    * @param attributes attributes
    */
   public MbeanImpl(String code, String name, Map<String, String> attributes)
   {
      this.code = code;
      this.name = name;
      if (attributes != null)
      {
         this.attributes = new HashMap<String, String>(attributes.size());
         this.attributes.putAll(attributes);
      }
      else
      {
         this.attributes = new HashMap<String, String>(0);
      }
   }
   
   /**
    * Get the code.
    *
    * @return the code.
    */
   @Override
   public String getCode()
   {
      return code;
   }

   /**
    * Get the name.
    *
    * @return the name.
    */
   @Override
   public String getName()
   {
      return name;
   }

   /**
    * Get the attributes.
    *
    * @return the attributes.
    */
   @Override
   public Map<String, String> getAttributes()
   {
      return attributes;
   }

}
