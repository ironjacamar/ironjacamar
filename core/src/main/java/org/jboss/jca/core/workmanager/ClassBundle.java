/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
import java.util.ArrayList;
import java.util.List;

/**
 * A class bundle
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ClassBundle implements Serializable
{
   /** SerialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The definitions */
   private List<ClassDefinition> definitions;

   /**
    * Constructor
    */
   public ClassBundle()
   {
      this(new ArrayList<ClassDefinition>(1));
   }

   /**
    * Constructor
    * @param definitions The definitions
    */
   public ClassBundle(List<ClassDefinition> definitions)
   {
      this.definitions = definitions;
   }

   /**
    * Get the definitions
    * @return The value
    */
   public List<ClassDefinition> getDefinitions()
   {
      return definitions;
   }

   /** 
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;

      result += definitions != null ? 7 * definitions.hashCode() : 0;

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

      if (!(other instanceof ClassBundle))
         return false;

      ClassBundle cb = (ClassBundle)other;
      if (definitions == null)
      {
         if (cb.definitions != null)
            return false;
      }
      else
      {
         if (!definitions.equals(cb.definitions))
            return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ClassBundle@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[definitions=").append(definitions);
      sb.append("]");

      return sb.toString();
   }
}
