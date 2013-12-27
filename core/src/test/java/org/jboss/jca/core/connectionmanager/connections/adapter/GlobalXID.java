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
package org.jboss.jca.core.connectionmanager.connections.adapter;

import java.util.Arrays;

import javax.transaction.xa.Xid;

/**
 * Xid
 */
public class GlobalXID
{
   private byte[] gid;
   private int hashCode;
   private String toString;
   
   /**
    * Constructor
    * @param xid The Xid
    */
   public GlobalXID(Xid xid)
   {
      gid = xid.getGlobalTransactionId();
      
      for (int i = 0; i < gid.length; ++i)
         hashCode += 37 * gid[i];

      toString = Arrays.toString(gid);
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return hashCode;
   }
   
   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return toString;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (obj == null || (!(obj instanceof GlobalXID)))
         return false;
      
      GlobalXID other = (GlobalXID) obj;
      return toString.equals(other.toString);
   }
}
