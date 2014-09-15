/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.spi.transaction.FirstResource;
import org.jboss.jca.core.spi.transaction.XAResourceStatistics;

import javax.transaction.xa.XAResource;

/**
 * A first resource connectable XAResourceWrapper with statistics
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class FirstResourceConnectableXAResourceWrapperStatImpl extends ConnectableXAResourceWrapperStatImpl
   implements FirstResource,
              org.jboss.tm.FirstResource
{
   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param pad pad
    * @param override override
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    * @param cr connectable resource
    * @param xastat The statistics
    */   
   public FirstResourceConnectableXAResourceWrapperStatImpl(XAResource resource, boolean pad, Boolean override,
                                                            String productName, String productVersion,
                                                            String jndiName, ConnectableResource cr,
                                                            XAResourceStatistics xastat)
   {
      super(resource, pad, override, productName, productVersion, jndiName, cr, xastat);
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
    * @param xastat The statistics
    */   
   public FirstResourceConnectableXAResourceWrapperStatImpl(XAResource resource, boolean pad, Boolean override,
                                                            String productName, String productVersion,
                                                            String jndiName, org.jboss.tm.ConnectableResource cr,
                                                            XAResourceStatistics xastat)
   {
      super(resource, pad, override, productName, productVersion, jndiName, cr, xastat);
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object object)
   {
      if (object == this)
         return true;

      if (object == null || !(object instanceof FirstResourceConnectableXAResourceWrapperStatImpl))
         return false;

      FirstResourceConnectableXAResourceWrapperStatImpl other =
         (FirstResourceConnectableXAResourceWrapperStatImpl)object;

      if (!super.equals(other))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return super.hashCode();
   }
}
