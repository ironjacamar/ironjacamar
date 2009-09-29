/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package javax.resource.spi.work;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A standard {@link WorkContext WorkContext} that allows a {@link Work
 * Work} instance to propagate quality-of-service (QoS) hints about the
 * {@link Work Work} to the <code>WorkManager</code>.
 * 
 * @since 1.6
 * @see javax.resource.spi.work.WorkContextProvider
 * @version Java EE Connector Architecture 1.6
 */

public class HintsContext implements WorkContext 
{

   /**
    * Determines if a deserialized instance of this class
    * is compatible with this class.
    */
   private static final long serialVersionUID = 7956353628297167255L;

   /** The name hint */
   public static final String NAME_HINT = "javax.resource.Name";

   /** The long running hint */
   public static final String LONGRUNNING_HINT = "javax.resource.LongRunning";
   
   /** Description */
   protected String description = "Hints Context";

   /** Name */
   protected String name = "HintsContext";

   /** The hints */
   Map<String, Serializable> hints = new HashMap<String, Serializable>();
      
   /**
    * {@inheritDoc}
    */
   public String getDescription() 
   {
      return description;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getName() 
   {
      return name;
   }
   
   /**
    * Set a brief description of the role played by the instance of
    * HintsContext and any other related debugging information.
    *
    * This could be used by the resource adapter and the WorkManager
    * for logging and debugging purposes.
    * @param description The description
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * Set the associated name of the HintsContext. This
    * could be used by the resource adapter and the WorkManager
    * for logging and debugging purposes.
    * @param name The name
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * Set a Hint and a related value. The hintName must be non-Null.
    * Standard HintNames are defined in the Connector specification. Use of
    * "javax.resource." prefixed hintNames are reserved for use by the 
    * Connector specification.
    * @param hintName The hint name
    * @param value The hint value
    *
    */
   public void setHint(String hintName, Serializable value) 
   {
      hints.put(hintName, value);
   }
   
   /**
    * Get the hints
    * @return The hints
    */
   public Map<String, Serializable> getHints() 
   {
      return hints;
   }
}
