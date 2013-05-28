/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.common;

import org.jboss.jca.core.api.bootstrap.CloneableBootstrapContext;

import java.util.Map;

/**
 * Configuration interface
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Configuration
{
   /**
    * Set if bean validation should be performed
    * @param value The value
    */
   public void setBeanValidation(boolean value);

   /**
    * Should bean validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getBeanValidation();

   /**
    * Set if archive validation should be performed
    * @param value The value
    */
   public void setArchiveValidation(boolean value);

   /**
    * Should archive validation be performed
    * @return True if validation; otherwise false
    */
   public boolean getArchiveValidation();

   /**
    * Set if a failed warning archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnWarn(boolean value);

   /**
    * Does a failed archive validation warning report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnWarn();

   /**
    * Set if a failed error archive validation report should fail the deployment
    * @param value The value
    */
   public void setArchiveValidationFailOnError(boolean value);

   /**
    * Does a failed archive validation error report fail the deployment
    * @return True if failing; otherwise false
    */
   public boolean getArchiveValidationFailOnError();

   /**
    * Set the default bootstrap context
    * @param value The value
    */
   @Deprecated
   public void setDefaultBootstrapContext(CloneableBootstrapContext value);

   /**
    * Get the default bootstrap context
    * @return The handle
    */
   @Deprecated
   public CloneableBootstrapContext getDefaultBootstrapContext();

   /**
    * Set the bootstrap context map
    * @param value The value
    */
   @Deprecated
   public void setBootstrapContexts(Map<String, CloneableBootstrapContext> value);

   /**
    * Get the bootstrap context map
    * @return The handle
    */
   @Deprecated
   public Map<String, CloneableBootstrapContext> getBootstrapContexts();
}
