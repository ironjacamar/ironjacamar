/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.api.deploymentrepository;

/**
 * A config property
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ConfigProperty
{
   /**
    * Get the name
    * @return The value
    */
   public String getName();

   /**
    * Get the type
    * @return The value
    */
   public Class<?> getType();

   /**
    * Get the value
    * @return The value
    */
   public Object getValue();

   /**
    * Is read-only ?
    * @return The value
    */
   public boolean isReadOnly();

   /**
    * Is confidential ?
    * @return The value
    */
   public boolean isConfidential();

   /**
    * Is declared (Declared or introspected)
    * @return The value
    */
   public boolean isDeclared();

   /**
    * Set the value
    * @param value The value
    * @return <code>true</code> if the value was set, otherwise <code>false</code>
    */
   public boolean setValue(Object value);
}
