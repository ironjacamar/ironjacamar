/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

/**
 * Pool by {@link ConnectionRequestInfo} key.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @version $Rev: $
 */
class CriKey
{
   /** Identifies no connection request information */
   private static final Object NOCRI = new Object();
   
   /** The connection request information */
   private final Object cri;

   /** Separate no tx */
   private boolean separateNoTx;
   
   /** The cached hashCode */
   private int hashCode = Integer.MAX_VALUE;

   /**
    * Creates a new instance.
    * @param cri connection request info
    * @param separateNoTx separateNoTx
    */
   CriKey(ConnectionRequestInfo cri, boolean separateNoTx) 
   {
      this.cri = (cri == null) ? NOCRI : cri;
      this.separateNoTx = separateNoTx;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      if (hashCode == Integer.MAX_VALUE)
         hashCode = cri.hashCode();

      return hashCode;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;  

      if (obj == null || !(obj instanceof CriKey))
         return false;  
      
      CriKey other = (CriKey) obj;

      return cri.equals(other.cri) && separateNoTx == other.separateNoTx;
   }
}
