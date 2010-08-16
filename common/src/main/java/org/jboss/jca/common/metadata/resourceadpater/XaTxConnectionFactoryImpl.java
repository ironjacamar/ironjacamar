/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata.resourceadpater;

import org.jboss.jca.common.api.metadata.resourceadapter.Security;
import org.jboss.jca.common.api.metadata.resourceadapter.TimeOut;
import org.jboss.jca.common.api.metadata.resourceadapter.Validation;
import org.jboss.jca.common.api.metadata.resourceadapter.XaTxConnectionFactory;

import java.util.HashMap;

/**
 *
 * A XaTxConnectionFactoryImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class XaTxConnectionFactoryImpl extends LocalTxConnectionFactoryImpl implements XaTxConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8211429129210108351L;
   private final String xaResourceTimeout;

   /**
    * Create a new XaTxConnectionFactoryImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param userName userName
    * @param password password
    * @param connectionDefinition connectionDefinition
    * @param configProperty configProperty
    * @param security security
    * @param timeOut timeOut
    * @param validation validation
    * @param poolName poolName
    * @param className className
    * @param jndiName jndiName
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    * @param noTxSeparatePools noTxSeparatePools
    * @param trackConnectionByTx trackConnectionByTx
    * @param xaResourceTimeout xaResourceTimeout
    */
   public XaTxConnectionFactoryImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, String userName,
         String password, String connectionDefinition, HashMap<String, String> configProperty, Security security,
         TimeOut timeOut, Validation validation, String poolName, String className, String jndiName, boolean enabled,
         boolean useJavaContext, boolean noTxSeparatePools, boolean trackConnectionByTx, String xaResourceTimeout)
   {
      super(minPoolSize, maxPoolSize, prefill, userName, password, connectionDefinition, configProperty, security,
            timeOut, validation, poolName, className, jndiName, enabled, useJavaContext, noTxSeparatePools,
            trackConnectionByTx);
      this.xaResourceTimeout = xaResourceTimeout;
   }

   /**
    * Get the xaResourceTimeout.
    *
    * @return the xaResourceTimeout.
    */
   @Override
   public final String getXaResourceTimeout()
   {
      return xaResourceTimeout;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((xaResourceTimeout == null) ? 0 : xaResourceTimeout.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof XaTxConnectionFactoryImpl))
         return false;
      XaTxConnectionFactoryImpl other = (XaTxConnectionFactoryImpl) obj;
      if (xaResourceTimeout == null)
      {
         if (other.xaResourceTimeout != null)
            return false;
      }
      else if (!xaResourceTimeout.equals(other.xaResourceTimeout))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "XaTxConnectionFactoryImpl [xaResourceTimeout=" + xaResourceTimeout + ", minPoolSize=" + minPoolSize
            + ", maxPoolSize=" + maxPoolSize + ", prefill=" + prefill + ", userName=" + userName + ", password="
            + password + ", connectionDefinition=" + connectionDefinition + ", configProperty=" + configProperty
            + ", security=" + security + ", timeOut=" + timeOut + ", validation=" + validation + ", poolName="
            + poolName + ", className=" + className + ", jndiName=" + jndiName + ", enabled=" + enabled
            + ", useJavaContext=" + useJavaContext + "]";
   }
}

