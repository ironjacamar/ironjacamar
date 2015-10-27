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
package org.ironjacamar.common.api.metadata.resourceadapter;

import org.ironjacamar.common.api.metadata.JCAMetadata;

import java.util.Map;

/**
 * An AdminObject.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public interface AdminObject extends JCAMetadata
{
   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   public Map<String, String> getConfigProperties();

   /**
    * Get the className.
    *
    * @return the className.
    */
   public String getClassName();

   /**
    * Get the jndiName.
    *
    * @return the jndiName.
    */
   public String getJndiName();

   /**
    * Get the identifier
    *
    * @return the value
    */
   public String getId();

   /**
    * Get the enabled.
    *
    * @return the enabled.
    */
   public Boolean isEnabled();
}
