/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.strategy;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * Simple reauth pool with same properties as an OnePool
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
class ReauthKey
{
   /** The cached hashCode */
   private int hashCode = Integer.MAX_VALUE;

   /**
    * Constructor
    * @param subject subject instance
    * @param cri connection request info
    * @param separateNoTx seperateNoTx
    */
   ReauthKey(Subject subject, ConnectionRequestInfo cri, boolean separateNoTx)
   {
      this.hashCode = separateNoTx ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode();
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      return hashCode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;  
      }
      
      if (obj == null || !(obj instanceof ReauthKey))
      {
         return false;  
      }
      
      ReauthKey other = (ReauthKey)obj;
      return hashCode == other.hashCode;
   }
}
