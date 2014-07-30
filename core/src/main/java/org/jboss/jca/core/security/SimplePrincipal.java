/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * Simple principal implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SimplePrincipal implements Principal, Serializable
{
   private static final long serialVersionUID = 1L;

   /** Principal name */
   private final String name;

   /**
    * Constructor
    * @param name The principal name
    */
   public SimplePrincipal(String name)
   {
      this.name = name;
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
   public boolean equals(Object o)
   {
      if (o == this)
         return true;

      if (o == null || !(o instanceof Principal))
         return false;

      Principal p = (Principal)o;

      if (name == null)
      {
         return p.getName() == null;
      }
      else
      {
         return name.equals(p.getName());
      }
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return name == null ? 7 : name.hashCode();
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return name;
   }
}
