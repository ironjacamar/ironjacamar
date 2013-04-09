/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.api.workmanager;

import javax.resource.spi.work.WorkContext;

/**
 * Properties for distribution of work instances
 */
public class DistributableContext implements WorkContext 
{
   private static final long serialVersionUID = 1L;

   /** The distribute key */
   public static final String DISTRIBUTE = "org.jboss.jca.core.api.workmanager.Distribute";

   /** The workmanager key */
   public static final String WORKMANAGER = "org.jboss.jca.core.api.workmanager.WorkManager";

   private Boolean distribute;
   private String workManager;

   /**
    * Constructor
    */
   public DistributableContext()
   {
      this.distribute = null;
      this.workManager = null;
   }

   /**
    * {@inheritDoc}
    */
   public String getName() 
   {
      return "DistributableContext";
   }
   
   /**
    * {@inheritDoc}
    */
   public String getDescription() 
   {
      return "Distribution properties";
   }
   
   /**
    * Set the distribute value
    * @param v The value
    */
   public void setDistribute(Boolean v)
   {
      this.distribute = v;
   }
   
   /**
    * Get the distribute value
    * @return The value
    */
   public Boolean getDistribute()
   {
      return distribute;
   }

   /**
    * Set the work manager value
    * @param v The value
    */
   public void setWorkManager(String v)
   {
      this.workManager = v;
   }
   
   /**
    * Get the work manager value
    * @return The value
    */
   public String getWorkManager()
   {
      return workManager;
   }
}
