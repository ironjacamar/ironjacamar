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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.ConnectableResourceListener;

import javax.transaction.xa.XAResource;

/**
 * A connectable XAResourceWrapper.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnectableXAResourceWrapperImpl extends XAResourceWrapperImpl
   implements ConnectableResource,
              org.jboss.tm.ConnectableResource
{
   /** The connectable resource */
   private ConnectableResource cr1;

   /** The connectable resource */
   private org.jboss.tm.ConnectableResource cr2;

   /** The connectable resource listener */
   private ConnectableResourceListener crl;

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
      this.cr1 = cr;
      this.cr2 = null;
      this.crl = null;
   }

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
                                           String jndiName, org.jboss.tm.ConnectableResource cr)
   {
      super(resource, pad, override, productName, productVersion, jndiName);
      this.cr1 = null;
      this.cr2 = cr;
      this.crl = null;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection() throws Exception
   {
      Object result = null;

      if (cr1 != null)
      {
         result = cr1.getConnection();
      }
      else
      {
         try
         {
            result = cr2.getConnection();
         }
         catch (Throwable t)
         {
            throw new Exception(t.getMessage(), t);
         }
      }

      if (crl != null)
         crl.handleCreated(result);

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public void setConnectableResourceListener(ConnectableResourceListener crl)
   {
      this.crl = crl;
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

      if (cr1 != null)
      {
         if (!cr1.equals(other.cr1))
            return false;
      }
      else
      {
         if (other.cr1 != null)
            return false;
      }
      if (cr2 != null)
      {
         if (!cr2.equals(other.cr2))
            return false;
      }
      else
      {
         if (other.cr2 != null)
            return false;
      }
      if (crl != null)
      {
         if (!crl.equals(other.crl))
            return false;
      }
      else
      {
         if (other.crl != null)
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

      result += cr1 != null ? 7 * cr1.hashCode() : 7;
      result += cr2 != null ? 7 * cr2.hashCode() : 7;
      result += crl != null ? 7 * crl.hashCode() : 7;

      return result;
   }
}
