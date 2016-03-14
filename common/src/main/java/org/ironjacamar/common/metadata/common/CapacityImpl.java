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
package org.ironjacamar.common.metadata.common;

import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.validator.ValidateException;

/**
 * Capacity definition 
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class CapacityImpl extends AbstractMetadata implements Capacity
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Extension incrementer;
   private Extension decrementer;

   /**
    * Constructor
    *
    * @param incrementer The incrementer plugin
    * @param decrementer The decrementer plugin
    * @throws ValidateException in case of not valid metadata creation
    */
   public CapacityImpl(Extension incrementer, Extension decrementer) throws ValidateException
   {
      super(null);
      this.incrementer = incrementer;
      this.decrementer = decrementer;
      this.validate();
   }

   /**
    * Get the incrementer
    * @return The value
    */
   public Extension getIncrementer()
   {
      return incrementer;
   }

   /**
    * Get the decrementer
    * @return The value
    */
   public Extension getDecrementer()
   {
      return decrementer;
   }

   @Override
   public void validate() throws ValidateException
   {
      // all values are ok
   }

   @Override
   public int hashCode()
   {
      int prime = 31;
      int result = 7;
      result = prime * result + ((incrementer == null) ? 7 : incrementer.hashCode());
      result = prime * result + ((decrementer == null) ? 7 : decrementer.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;

      if (obj == null)
         return false;

      if (!(obj instanceof CapacityImpl))
         return false;

      CapacityImpl other = (CapacityImpl) obj;

      if (incrementer == null)
      {
         if (other.incrementer != null)
            return false;
      }
      else if (!incrementer.equals(other.incrementer))
         return false;

      if (decrementer == null)
      {
         if (other.decrementer != null)
            return false;
      }
      else if (!decrementer.equals(other.decrementer))
         return false;

      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<capacity>");

      if (incrementer != null)
      {
         sb.append("<").append("incrementer");
         sb.append(incrementer.toString());

         sb.append("</").append("incrementer").append(">");
      }
      if (decrementer != null)
      {
         sb.append("<").append("decrementer");
         sb.append(decrementer.toString());


         sb.append("</").append("decrementer").append(">");
      }

      sb.append("</capacity>");
      
      return sb.toString();
   }
}

