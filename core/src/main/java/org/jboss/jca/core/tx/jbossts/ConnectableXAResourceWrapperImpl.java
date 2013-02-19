/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.spi.transaction.ConnectableResource;

import javax.transaction.xa.XAResource;

/**
 * A connectable XAResourceWrapper.
 * 
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class ConnectableXAResourceWrapperImpl extends XAResourceWrapperImpl
   implements ConnectableResource//,
              //org.jboss.tm.ConnectableLastResource
{
   /** The connectable resource */
   private ConnectableResource cr;

   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param pad pad
    * @param override override
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    * @param cr connectable resource
    */   
   public ConnectableXAResourceWrapperImpl(XAResource resource, boolean pad, Boolean override, 
                                           String productName, String productVersion,
                                           String jndiName, ConnectableResource cr)
   {
      super(resource, pad, override, productName, productVersion, jndiName);
      this.cr = cr;
   }

   /**
    * {@inheritDoc}
    */
   public AutoCloseable getConnection() throws Exception
   {
      return cr.getConnection();
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object object)
   {
      if (object == this)
         return true;

      if (object == null || !(object instanceof ConnectableXAResourceWrapperImpl))
         return false;

      ConnectableXAResourceWrapperImpl other = (ConnectableXAResourceWrapperImpl)object;

      if (!super.equals(other))
         return false;

      if (cr != null)
      {
         if (!cr.equals(other.cr))
            return false;
      }
      else
      {
         if (other.cr != null)
            return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;

      result += cr != null ? 7 * cr.hashCode() : 7;

      return result;
   }
}
