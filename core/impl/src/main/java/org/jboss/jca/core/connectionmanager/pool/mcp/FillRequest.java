/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

/**
 * Represents a fill request for a managed connection pool
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class FillRequest
{
   /** Managed connection pool */
   private ManagedConnectionPool mcp;

   /** Fill size */
   private int fillSize;

   /**
    * Constructor
    * @param mcp The managed connection pool
    * @param fillSize The fill size
    */
   FillRequest(ManagedConnectionPool mcp, int fillSize)
   {
      this.mcp = mcp;
      this.fillSize = fillSize;
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
    * Get the fill size
    * @return The value
    */
   int getFillSize()
   {
      return fillSize;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;
      result += 7 * mcp.hashCode();
      result += 7 * fillSize;
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

      if (!(obj instanceof FillRequest))
         return false;

      FillRequest other = (FillRequest) obj;

      if (mcp == null)
      {
         if (other.mcp != null)
            return false;
      }
      else if (System.identityHashCode(mcp) != System.identityHashCode(other.mcp))
         return false;

      if (fillSize != other.fillSize)
         return false;

      return true;
   }
}
