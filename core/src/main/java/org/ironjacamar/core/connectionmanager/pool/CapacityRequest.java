/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.connectionmanager.pool;

/**
 * Represents a capacity request for a managed connection pool
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class CapacityRequest
{
   /** Managed connection pool */
   private ManagedConnectionPool mcp;

   /**
    * Constructor
    * @param mcp The managed connection pool
    */
   public CapacityRequest(ManagedConnectionPool mcp)
   {
      this.mcp = mcp;
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
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;
      result += 7 * mcp.hashCode();
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

      return true;
   }
}
