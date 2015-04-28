/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.tx.narayana;

import org.ironjacamar.core.spi.transaction.FirstResource;
import org.ironjacamar.core.spi.transaction.XAResourceStatistics;

import javax.transaction.xa.XAResource;

/**
 * A first resource XAResourceWrapper with statistics
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class FirstResourceXAResourceWrapperStatImpl extends XAResourceWrapperStatImpl
   implements FirstResource,
              org.jboss.tm.FirstResource
{
   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param productName product name
    * @param productVersion product version
    * @param xastat The statistics
    */   
   public FirstResourceXAResourceWrapperStatImpl(XAResource resource,
                                                 String productName, String productVersion,
                                                 XAResourceStatistics xastat)
   {
      this(resource, false, null, productName, productVersion, null, xastat);
   }

   /**
    * Creates a new wrapper instance.
    * @param resource xaresource
    * @param pad pad
    * @param override override
    * @param productName product name
    * @param productVersion product version
    * @param jndiName jndi name
    * @param xastat The statistics
    */   
   public FirstResourceXAResourceWrapperStatImpl(XAResource resource, boolean pad, Boolean override, 
                                                 String productName, String productVersion,
                                                 String jndiName, XAResourceStatistics xastat)
   {
      super(resource, pad, override, productName, productVersion, jndiName, xastat);
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object object)
   {
      if (object == this)
         return true;

      if (object == null || !(object instanceof FirstResourceXAResourceWrapperStatImpl))
         return false;  

      FirstResourceXAResourceWrapperStatImpl other =
         (FirstResourceXAResourceWrapperStatImpl)object;

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
