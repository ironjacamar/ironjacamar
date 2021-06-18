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
package org.jboss.jca.common.api.metadata.spec;

import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.ValidatableMetadata;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.List;

/**
 *
 * A ResourceAdapter.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public interface ResourceAdapter extends IdDecoratedMetadata, ValidatableMetadata,
                                         CopyableMetaData, MergeableMetadata<ResourceAdapter>
{
   /**
    * @return resourceadapterClass
    */
   public String getResourceadapterClass();

   /**
    * @return configProperty
    */
   public List<ConfigProperty> getConfigProperties();

   /**
    * @return outboundResourceadapter
    */
   public OutboundResourceAdapter getOutboundResourceadapter();

   /**
    * @return inboundResourceadapter
    */
   public InboundResourceAdapter getInboundResourceadapter();

   /**
    * @return adminobject
    */
   public List<AdminObject> getAdminObjects();

   /**
    * @return securityPermission
    */
   public List<SecurityPermission> getSecurityPermissions();

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException;
}

