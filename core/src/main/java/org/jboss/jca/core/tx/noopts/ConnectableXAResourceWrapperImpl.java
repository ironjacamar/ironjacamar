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
package org.jboss.jca.core.tx.noopts;

import org.jboss.jca.core.spi.transaction.ConnectableResource;
import org.jboss.jca.core.spi.transaction.ConnectableResourceListener;

import javax.transaction.xa.XAResource;

/**
 * A connectable XAResourceWrapper
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnectableXAResourceWrapperImpl extends XAResourceWrapperImpl implements ConnectableResource
{
   /** Connectable resource */
   private ConnectableResource cr;
   
   /** Connectable resource listener */
   private ConnectableResourceListener crl;
   
   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param override override
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    * @param cr connectable resource
    */   
   public ConnectableXAResourceWrapperImpl(XAResource resource, Boolean override, 
                                           String productName, String productVersion,
                                           String jndiName, ConnectableResource cr)
   {
      super(resource, override, productName, productVersion, jndiName);
      this.cr = cr;
      this.crl = null;
   }

   /**
    * {@inheritDoc}
    */
   public Object getConnection() throws Exception
   {
      Object result = cr.getConnection();

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
}
