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
package org.jboss.jca.common.api.metadata.common;

import org.jboss.jca.common.api.metadata.JCAMetadata;

import java.util.Map;

/**
 *
 * A ConnectionDefinition.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface CommonConnDef extends JCAMetadata
{
   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   public Map<String, String> getConfigProperties();

   /**
    * Get the className.
    *
    * @return the className.
    */
   public String getClassName();

   /**
    * Get the jndiName.
    *
    * @return the jndiName.
    */
   public String getJndiName();

   /**
    * Get the poolName.
    *
    * @return the poolName.
    */
   public String getPoolName();

   /**
    * Get the enabled.
    *
    * @return the enabled.
    */
   public Boolean isEnabled();

   /**
    * Get the useJavaContext.
    *
    * @return the useJavaContext.
    */
   public Boolean isUseJavaContext();

   /**
    * Get the useCcm.
    *
    * @return the useCcm.
    */
   public Boolean isUseCcm();

   /**
    * Get the pool.
    *
    * @return the pool.
    */
   public CommonPool getPool();

   /**
    * Get the timeOut.
    *
    * @return the timeOut.
    */
   public CommonTimeOut getTimeOut();

   /**
    * Get the validation.
    *
    * @return the validation.
    */
   public CommonValidation getValidation();

   /**
    * Get the security.
    *
    * @return the security.
    */
   public CommonSecurity getSecurity();

   /**
    * Return true if this connectionDefnition have defined an XaPool
    *
    * @return true if this connectionDefnition have defined an XaPool
    */
   public Boolean isXa();

   /**
    * Get the recovery settings.
    *
    * @return the recovery settings.
    */
   public Recovery getRecovery();
}
