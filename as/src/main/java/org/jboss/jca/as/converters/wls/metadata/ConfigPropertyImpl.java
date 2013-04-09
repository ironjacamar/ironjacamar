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
package org.jboss.jca.as.converters.wls.metadata;

import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperty;

/**
 *
 * A generic ConfigProperty.
 *
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 *
 */
public class ConfigPropertyImpl implements ConfigProperty
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2654156739973247322L;

   private final String name;
   private final String value;
   
   /**
    * constructor
    * 
    * @param name name
    * @param value value
    */
   public ConfigPropertyImpl(String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConfigProperty#getName()
    */
   @Override
   public String getName()
   {
      return name;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConfigProperty#getValue()
    */
   @Override
   public String getValue()
   {
      return value;
   }

}
