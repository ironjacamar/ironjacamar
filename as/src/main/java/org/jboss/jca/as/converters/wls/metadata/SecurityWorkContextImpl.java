/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
import org.jboss.jca.as.converters.wls.api.metadata.InboundCallerPrincipalMapping;
import org.jboss.jca.as.converters.wls.api.metadata.InboundGroupPrincipalMapping;
import org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A generic SecurityWorkContext.
 *
 * @author <a href="jeff.zhang@jboss.org">Jeff Zhang</a>
 *
 */
public class SecurityWorkContextImpl implements SecurityWorkContext
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2054156739973647322L;

   /**
    * nativeLibdir
    */
   protected final Boolean inboundMappingRequired;

   private final AnonPrincipal cpdm;
   private final ArrayList<InboundCallerPrincipalMapping> cpms;
   private final String gpdm;
   private final ArrayList<InboundGroupPrincipalMapping> gpms;
   
   /**
    * constructor
    * @param inboundMappingRequired inboundMappingRequired
    * @param cpdm AnonPrincipal
    * @param cpms List<? extends InboundCallerPrincipalMapping>
    * @param gpdm gpdm
    * @param gpms List<? extends InboundGroupPrincipalMapping>
    */
   public SecurityWorkContextImpl(Boolean inboundMappingRequired, AnonPrincipal cpdm, 
         List<? extends InboundCallerPrincipalMapping> cpms, String gpdm, 
         List<? extends InboundGroupPrincipalMapping> gpms)
   {
      this.inboundMappingRequired = inboundMappingRequired;
      this.cpdm = cpdm;
      if (cpms != null)
      {
         this.cpms = new ArrayList<InboundCallerPrincipalMapping>(cpms.size());
         this.cpms.addAll(cpms);
      }
      else
      {
         this.cpms = new ArrayList<InboundCallerPrincipalMapping>(0);
      }
      this.gpdm = gpdm;
      if (gpms != null)
      {
         this.gpms = new ArrayList<InboundGroupPrincipalMapping>(gpms.size());
         this.gpms.addAll(gpms);
      }
      else
      {
         this.gpms = new ArrayList<InboundGroupPrincipalMapping>(0);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext#getInboundMappingRequired()
    */
   @Override
   public Boolean getInboundMappingRequired()
   {
      return inboundMappingRequired;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext#getCallerPrincipalDefaultMapped()
    */
   @Override
   public AnonPrincipal getCallerPrincipalDefaultMapped()
   {
      return cpdm;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext#getCallerPrincipalMapping()
    */
   @Override
   public List<InboundCallerPrincipalMapping> getCallerPrincipalMapping()
   {
      return cpms;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext#getGroupPrincipalDefaultMapped()
    */
   @Override
   public String getGroupPrincipalDefaultMapped()
   {
      return gpdm;
   }

   /* (non-Javadoc)
    * @see org.jboss.jca.as.converters.wls.api.metadata.SecurityWorkContext#getGroupPrincipalMapping()
    */
   @Override
   public List<InboundGroupPrincipalMapping> getGroupPrincipalMapping()
   {
      return gpms;
   }

}
