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
package org.ironjacamar.common.api.metadata.ds;

import org.ironjacamar.common.api.metadata.JCAMetadata;
import org.ironjacamar.common.api.metadata.ValidatableMetadata;

/**
 *
 * A CommonDataSource.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public interface CommonDataSource extends JCAMetadata, ValidatableMetadata
{
   /**
    * Get the transactionIsolation.
    * @return the transactionIsolation.
    */
   public TransactionIsolation getTransactionIsolation();

   /**
    * Get the timeout
    * @return the timeout.
    */
   public Timeout getTimeout();

   /**
    * Get the security.
    * @return the security.
    */
   public DsSecurity getSecurity();

   /**
    * Get the validation.
    * @return the validation.
    */
   public Validation getValidation();

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
    * Get the jndiName.
    * @return the jndiName.
    */
   public String getJndiName();

   /**
    * Get the spy.
    * @return the spy.
    */
   public Boolean isSpy();

   /**
    * Get the use-ccm.
    * @return the use-ccm.
    */
   public Boolean isUseCcm();

   /**
    * Get the driver
    * @return The value
    */
   public String getDriver();

   /**
    * Set the enabled.
    * @param enabled The enabled to set.
    */
   public void setEnabled(Boolean enabled);
}
