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

package org.ironjacamar.core.deploymentrepository;

import org.ironjacamar.core.api.deploymentrepository.ConfigProperty;
import org.ironjacamar.core.util.Injection;

/**
 * A config property implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConfigPropertyImpl implements ConfigProperty
{
   /** Target */
   private Object target;

   /** The name */
   private String name;
   
   /** The type */
   private Class<?> type;
   
   /** The value - since most are readonly */
   private Object value;

   /** Read-only */
   private boolean readOnly;
   
   /** Confidential */
   private boolean confidential;
   
   /** Declared */
   private boolean declared;
   
   /**
    * Constructor
    * @param target The target object
    * @param name The name
    * @param type The type
    * @param value The value
    * @param readOnly Is read-only
    * @param confidential Is confidential
    * @param declared Is declared
    */
   public ConfigPropertyImpl(Object target,
                             String name,
                             Class<?> type,
                             Object value,
                             boolean readOnly,
                             boolean confidential,
                             boolean declared)
   {
      this.target = target;
      this.name = name;
      this.type = type;
      this.value = value;
      this.readOnly = readOnly;
      this.confidential = confidential;
      this.declared = declared;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public Class<?> getType()
   {
      return type;
   }

   /**
    * {@inheritDoc}
    */
   public Object getValue()
   {
      return value;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isReadOnly()
   {
      return readOnly;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isConfidential()
   {
      return confidential;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDeclared()
   {
      return declared;
   }

   /**
    * {@inheritDoc}
    */
   public boolean setValue(Object value)
   {
      if (!readOnly)
      {
         try
         {
            Injection injector = new Injection();
            injector.inject(target, name, value);

            this.value = value;
            return true;
         }
         catch (Exception e)
         {
            // Nothing to do
         }
      }
      return false;
   }
}
