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

import org.jboss.jca.as.converters.wls.api.metadata.AnonPrincipal;
import org.jboss.jca.as.converters.wls.api.metadata.AnonPrincipalCaller;
import org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity;
import org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext;

/**
 * @author shihang
 *
 */
public class ResourceAdapterSecurityImpl implements ResourceAdapterSecurity
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2014156739973647322L;
   
   private final SecurityWorkContext swc; 
   
   /**
    * constructor
    * 
    * @param swc SecurityWorkContext
    */
   public ResourceAdapterSecurityImpl(SecurityWorkContext swc)
   {
      this.swc = swc;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity#getDefaultPrincipalName()
    */
   @Override
   public AnonPrincipal getDefaultPrincipalName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity#getManageAsPrincipalName()
    */
   @Override
   public AnonPrincipal getManageAsPrincipalName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity#getRunAsPrincipalName()
    */
   @Override
   public AnonPrincipalCaller getRunAsPrincipalName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity#getRunWorkAsPrincipalName()
    */
   @Override
   public AnonPrincipalCaller getRunWorkAsPrincipalName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.ResourceAdapterSecurity#getSecurityWorkContext()
    */
   @Override
   public SecurityWorkContext getSecurityWorkContext()
   {
      return swc;
   }

}
