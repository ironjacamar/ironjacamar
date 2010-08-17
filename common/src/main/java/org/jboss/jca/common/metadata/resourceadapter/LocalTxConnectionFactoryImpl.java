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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.resourceadapter.LocalTxConnectionFactory;
import org.jboss.jca.common.api.metadata.resourceadapter.Security;
import org.jboss.jca.common.api.metadata.resourceadapter.TimeOut;
import org.jboss.jca.common.api.metadata.resourceadapter.Validation;

import java.util.Map;

/**
 *
 * A LocalTxConnectionFactoryImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class LocalTxConnectionFactoryImpl extends NoTxConnectionFactoryImpl implements LocalTxConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7691787666361594149L;

   private final boolean noTxSeparatePools;

   private final boolean trackConnectionByTx;

   /**
    * Create a new LocalTxConnectionFactoryImpl.
    *
    * @param minPoolSize minPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param userName userName
    * @param password password
    * @param connectionDefinition connectionDefinition
    * @param configProperties configProperties
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
    */
   public LocalTxConnectionFactoryImpl(Integer minPoolSize, Integer maxPoolSize, boolean prefill, String userName,
         String password, String connectionDefinition, Map<String, String> configProperties, Security security,
         TimeOut timeOut, Validation validation, String poolName, String className, String jndiName, boolean enabled,
         boolean useJavaContext, boolean noTxSeparatePools, boolean trackConnectionByTx)
   {
      super(minPoolSize, maxPoolSize, prefill, userName, password, connectionDefinition, configProperties, security,
            timeOut, validation, poolName, className, jndiName, enabled, useJavaContext);
      this.noTxSeparatePools = noTxSeparatePools;
      this.trackConnectionByTx = trackConnectionByTx;
   }

   /**
    * Get the noTxSeparatePools.
    *
    * @return the noTxSeparatePools.
    */
   @Override
   public final boolean isNoTxSeparatePools()
   {
      return noTxSeparatePools;
   }

   /**
    * Get the trackConnectionByTx.
    *
    * @return the trackConnectionByTx.
    */
   @Override
   public final boolean isTrackConnectionByTx()
   {
      return trackConnectionByTx;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + (noTxSeparatePools ? 1231 : 1237);
      result = prime * result + (trackConnectionByTx ? 1231 : 1237);
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (!(obj instanceof LocalTxConnectionFactoryImpl))
         return false;
      LocalTxConnectionFactoryImpl other = (LocalTxConnectionFactoryImpl) obj;
      if (noTxSeparatePools != other.noTxSeparatePools)
         return false;
      if (trackConnectionByTx != other.trackConnectionByTx)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "LocalTxConnectionFactoryImpl [noTxSeparatePools=" + noTxSeparatePools + ", trackConnectionByTx="
            + trackConnectionByTx + ", minPoolSize=" + minPoolSize + ", maxPoolSize=" + maxPoolSize + ", prefill="
            + prefill + ", userName=" + userName + ", password=" + password + ", connectionDefinition="
            + connectionDefinition + ", configProperties=" + configProperties + ", security=" + security + ", timeOut="
            + timeOut + ", validation=" + validation + ", poolName=" + poolName + ", className=" + className
            + ", jndiName=" + jndiName + ", enabled=" + enabled + ", useJavaContext=" + useJavaContext + "]";
   }

}

