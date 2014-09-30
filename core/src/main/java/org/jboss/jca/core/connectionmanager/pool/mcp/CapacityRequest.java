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

package org.jboss.jca.core.connectionmanager.pool.mcp;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * Represents a capacity request for a managed connection pool
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class CapacityRequest
{
   /** Managed connection pool */
   private ManagedConnectionPool mcp;

   /** Subject */
   private Subject subject;

   /** ConnectionRequestInfo */
   private ConnectionRequestInfo cri;

   /**
    * Constructor
    * @param mcp The managed connection pool
    * @param subject The subject
    * @param cri The connection request info object
    */
   CapacityRequest(ManagedConnectionPool mcp, Subject subject, ConnectionRequestInfo cri)
   {
      this.mcp = mcp;
      this.subject = subject;
      this.cri = cri;
   }

   /**
    * Get the managed connection pool
    * @return The value
    */
   ManagedConnectionPool getManagedConnectionPool()
   {
      return mcp;
   }

   /**
    * Get the subject
    * @return The value
    */
   Subject getSubject()
   {
      return subject;
   }

   /**
    * Get the connection request info object
    * @return The value
    */
   ConnectionRequestInfo getConnectionRequestInfo()
   {
      return cri;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;
      result += 7 * mcp.hashCode();
      result += subject != null ? 7 * SecurityActions.hashCode(subject) : 7;
      result += cri != null ? 7 * cri.hashCode() : 7;
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;

      if (obj == null)
         return false;

      if (!(obj instanceof CapacityRequest))
         return false;

      CapacityRequest other = (CapacityRequest) obj;

      if (mcp == null)
      {
         if (other.mcp != null)
            return false;
      }
      else if (System.identityHashCode(mcp) != System.identityHashCode(other.mcp))
         return false;

      if (subject == null)
      {
         if (other.subject != null)
            return false;
      }
      else if (!SecurityActions.equals(subject, other.subject))
         return false;

      if (cri == null)
      {
         if (other.cri != null)
            return false;
      }
      else if (!cri.equals(other.cri))
         return false;

      return true;
   }
}
