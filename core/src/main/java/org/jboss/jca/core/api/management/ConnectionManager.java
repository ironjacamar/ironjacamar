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

package org.jboss.jca.core.api.management;

import java.lang.ref.WeakReference;

/**
 * Represents a connector instance
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a> 
 */
public class ConnectionManager implements ManagedEnlistmentTrace
{
   /** The unique id */
   private String uniqueId;


   /** The enlistment trace */
   private WeakReference<Boolean> enlistmentTrace;


   /**
    * Constructor
    * @param uniqueId The unique id
    */
   public ConnectionManager(String uniqueId)
   {
      this.uniqueId = uniqueId;
   }

   /**
    * Get the unique id
    * @return The value
    */
   public String getUniqueId()
   {
      return uniqueId;
   }

   /**
    * Get the resource adapter
    * @return The value
    */

   @Override
   public Boolean getEnlistmentTrace()
   {
      if (enlistmentTrace == null)
         return null;

      return enlistmentTrace.get();
   }

   /**
    * Set the enlistmentTrace
    * @param enlistmentTrace The enlistmentTrace module
    */
   public void setEnlistmentTrace(Boolean enlistmentTrace)
   {
      this.enlistmentTrace = new WeakReference<Boolean>(enlistmentTrace);
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("Connector@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[uniqueId=").append(uniqueId);
      sb.append(" enlistmentTrace=").append(getEnlistmentTrace());
      sb.append("]");

      return sb.toString();
   }
}
