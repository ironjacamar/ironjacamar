/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.inflow.ra.inflow;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.jboss.logging.Logger;

/**
 * PureInflowActivationSpec
 *
 * @version $Revision: $
 */
public class PureInflowActivationSpec implements ActivationSpec
{
   /** The logger */
   private static Logger log = Logger.getLogger(PureInflowActivationSpec.class);

   /** Default string */
   private String defaultString;

   /** Default boolean */
   private Boolean defaultBoolean;

   /** The resource adapter */
   private ResourceAdapter ra;

   /**
    * Default constructor
    */
   public PureInflowActivationSpec()
   {

   }

   /**
    * Set the default string
    * @param v The value
    */
   public void setDefaultString(String v)
   {
      log.trace("setDefaultString(" + v + ")");
      this.defaultString = v;
   }

   /**
    * Get the default string
    * @return The value
    */
   public String getDefaultString()
   {
      return defaultString;
   }

   /**
    * Set the default boolean
    * @param v The value
    */
   public void setDefaultBoolean(Boolean v)
   {
      log.trace("setDefaultBoolean(" + v + ")");
      this.defaultBoolean = v;
   }

   /**
    * Get the default boolean
    * @return The value
    */
   public Boolean getDefaultBoolean()
   {
      return defaultBoolean;
   }

   /**
    * This method may be called by a deployment tool to validate the overall
    * activation configuration information provided by the endpoint deployer.
    *
    * @throws InvalidPropertyException indicates invalid onfiguration property settings.
    */
   public void validate() throws InvalidPropertyException
   {
      log.trace("validate()");
   }

   /**
    * Get the resource adapter
    *
    * @return The handle
    */
   public ResourceAdapter getResourceAdapter()
   {
      log.trace("getResourceAdapter()");
      return ra;
   }

   /**
    * Set the resource adapter
    *
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      log.trace("setResourceAdapter()");
      this.ra = ra;
   }
}
