/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.common.v11;

import org.jboss.jca.common.api.metadata.common.v11.WorkManager;
import org.jboss.jca.common.api.metadata.common.v11.WorkManagerSecurity;

/**
 * WorkManager configuration
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkManagerImpl implements WorkManager
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The WorkManager security */
   private WorkManagerSecurity security;

   /**
    * Constructor
    * @param security security
    */
   public WorkManagerImpl(WorkManagerSecurity security)
   {
      this.security = security;
   }

   /**
    * {@inheritDoc}
    */
   public WorkManagerSecurity getSecurity()
   {
      return security;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;

      result += security != null ? 7 * security.hashCode() : 7;

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object o)
   {
      if (this == o)
         return true;

      if (o == null || !(o instanceof WorkManagerImpl))
         return false;

      WorkManagerImpl other = (WorkManagerImpl)o;

      if (security != null)
      {
         if (!security.equals(other.security))
            return false;
      }
      else
      {
         if (other.security != null)
            return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<workmanager>");

      if (security != null)
         sb.append(security);

      sb.append("</workmanager>");

      return sb.toString();
   }
}

