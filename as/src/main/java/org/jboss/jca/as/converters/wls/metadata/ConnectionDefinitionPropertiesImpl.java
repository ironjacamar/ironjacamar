/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.converters.wls.metadata;

import org.jboss.jca.as.converters.wls.api.metadata.ConfigProperties;
import org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties;
import org.jboss.jca.as.converters.wls.api.metadata.Logging;
import org.jboss.jca.as.converters.wls.api.metadata.PoolParams;
import org.jboss.jca.as.converters.wls.api.metadata.TransactionSupport;

/**
*
* A generic ConnectionDefinitionProperties.
*
* @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
*
*/
public class ConnectionDefinitionPropertiesImpl implements ConnectionDefinitionProperties
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2654156739973657822L;

   private final PoolParams poolParams;
   private final Logging logging;
   private final TransactionSupport trans;
   private final String authMech;
   private final Boolean reAuthSupport;
   private final ConfigProperties props;
   private final String resAuth;
   
   /**
    * constructor
    * 
    * @param poolParams PoolParams
    * @param logging Logging
    * @param trans TransactionSupport
    * @param authMech authMech
    * @param reAuthSupport reAuthSupport
    * @param props ConfigProperties
    * @param resAuth resAuth
    */
   public ConnectionDefinitionPropertiesImpl(PoolParams poolParams, Logging logging,
      TransactionSupport trans, String authMech, Boolean reAuthSupport, ConfigProperties props, String resAuth)
   {
      this.poolParams = poolParams;
      this.logging = logging;
      this.trans = trans;
      this.authMech = authMech;
      this.reAuthSupport = reAuthSupport;
      this.props = props;
      this.resAuth = resAuth;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties#getPoolParams()
    */
   @Override
   public PoolParams getPoolParams()
   {
      return poolParams;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties#getLogging()
    */
   @Override
   public Logging getLogging()
   {
      return logging;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties#getTransactionSupport()
    */
   @Override
   public TransactionSupport getTransactionSupport()
   {
      return trans;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties#getAuthenticationMechanism()
    */
   @Override
   public String getAuthenticationMechanism()
   {
      return authMech;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties#getReauthenticationSupport()
    */
   @Override
   public Boolean getReauthenticationSupport()
   {
      return reAuthSupport;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties#getProperties()
    */
   @Override
   public ConfigProperties getProperties()
   {
      return props;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ConnectionDefinitionProperties#getResAuth()
    */
   @Override
   public String getResAuth()
   {
      return resAuth;
   }

}
