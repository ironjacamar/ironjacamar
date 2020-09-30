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
package org.jboss.jca.core.spi.transaction.xa;

import javax.transaction.xa.XAResource;

/**
 * An XAResource wrapper.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface XAResourceWrapper extends XAResource
{
   /**
    * Get the XAResource that is being wrapped
    * @return The XAResource
    */
   public XAResource getResource();

   /**
    * Get product name
    * @return Product name of the instance that created the wrapper if defined; otherwise <code>null</code>
    */
   public String getProductName();

   /**
    * Get product version
    * @return Product version of the instance that created the warpper if defined; otherwise <code>null</code>
    */
   public String getProductVersion();

   /**
    * Get the JNDI name
    * @return The value
    */
   public String getJndiName();
}
