/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.api.metadata.resourceadapter;

import org.ironjacamar.common.api.metadata.JCAMetadata;
import org.ironjacamar.common.api.metadata.common.Pool;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.common.Security;
import org.ironjacamar.common.api.metadata.common.Timeout;
import org.ironjacamar.common.api.metadata.common.Validation;

import java.util.Map;

/**
 * A ConnectionDefinition.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public interface ConnectionDefinition extends JCAMetadata
{
   /**
    * Get the configProperties.
    * @return the configProperties.
    */
   public Map<String, String> getConfigProperties();

   /**
    * Get the className.
    * @return the className.
    */
   public String getClassName();

   /**
    * Get the jndiName.
    * @return the jndiName.
    */
   public String getJndiName();

   /**
    * Get the identifier
    * @return the value
    */
   public String getId();

   /**
    * Get the enabled.
    * @return the enabled.
    */
   public Boolean isEnabled();

   /**
    * Get the useCcm.
    * @return the useCcm.
    */
   public Boolean isUseCcm();

   /**
    * Get the pool.
    * @return the pool.
    */
   public Pool getPool();

   /**
    * Get the timeout.
    * @return the timeout.
    */
   public Timeout getTimeout();

   /**
    * Get the validation.
    * @return the validation.
    */
   public Validation getValidation();

   /**
    * Get the security.
    * @return the security.
    */
   public Security getSecurity();

   /**
    * Return true if this connectionDefnition have defined an XaPool
    * @return true if this connectionDefnition have defined an XaPool
    */
   public Boolean isXa();

   /**
    * Get the recovery settings.
    * @return the recovery settings.
    */
   public Recovery getRecovery();

   /**
    * Get the sharable
    * @return the value
    */
   public Boolean isSharable();

   /**
    * Get the enlistment
    * @return the value
    */
   public Boolean isEnlistment();

   /**
    * Get the connectable flag
    * @return True, if connectable should be supported
    */
   public Boolean isConnectable();

   /**
    * Get the tracking flag
    * @return <code>null</code> is container default, a value is an override
    */
   public Boolean isTracking();
}
