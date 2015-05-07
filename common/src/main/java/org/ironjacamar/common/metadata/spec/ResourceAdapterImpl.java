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
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.CopyUtil;
import org.ironjacamar.common.api.metadata.MergeUtil;
import org.ironjacamar.common.api.metadata.spec.AdminObject;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.InboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.MergeableMetadata;
import org.ironjacamar.common.api.metadata.spec.OutboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.ResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.SecurityPermission;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Messages;

/**
 * A ResourceAdapter implementation
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterImpl extends AbstractMetadata implements ResourceAdapter
{
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private XsdString resourceadapterClass;

   private List<ConfigProperty> configProperties;

   private OutboundResourceAdapter outboundResourceadapter;

   private InboundResourceAdapter inboundResourceadapter;

   private List<AdminObject> adminobjects;

   private List<SecurityPermission> securityPermissions;

   private String id;

   /**
    * Constructor
    * @param resourceadapterClass full qualified name of the class
    * @param configProperties confi properties for this RA
    * @param outboundResourceadapter outbound RA
    * @param inboundResourceadapter inbound RA
    * @param adminobjects list of admin objects of this RA
    * @param securityPermissions supported security permissions
    * @param id XML ID
    */
   public ResourceAdapterImpl(XsdString resourceadapterClass, List<ConfigProperty> configProperties,
                              OutboundResourceAdapter outboundResourceadapter,
                              InboundResourceAdapter inboundResourceadapter,
                              List<AdminObject> adminobjects, List<SecurityPermission> securityPermissions,
                              String id)
   {
      super(null);
      this.resourceadapterClass = resourceadapterClass;
      if (!XsdString.isNull(this.resourceadapterClass))
         this.resourceadapterClass.setTag(XML.ELEMENT_RESOURCEADAPTER_CLASS);
      if (configProperties != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(configProperties);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
      this.outboundResourceadapter = outboundResourceadapter;
      this.inboundResourceadapter = inboundResourceadapter;
      if (adminobjects != null)
      {
         this.adminobjects = new ArrayList<AdminObject>(adminobjects);
      }
      else
      {
         this.adminobjects = new ArrayList<AdminObject>(0);
      }
      if (securityPermissions != null)
      {
         this.securityPermissions = new ArrayList<SecurityPermission>(securityPermissions);
      }
      else
      {
         this.securityPermissions = new ArrayList<SecurityPermission>(0);
      }
      this.id = id;
   }

   /**
    * {@inheritDoc}
    */
   public String getResourceadapterClass()
   {
      if (XsdString.isNull(resourceadapterClass))
         return null;
      return resourceadapterClass.getValue();
   }

   /**
    * {@inheritDoc}
    */
   public List<ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }

   /**
    * Force configProperties with new content.
    * This method is thread safe
    *
    * @param newContents the list of new properties
    */
   public synchronized void forceConfigProperties(List<ConfigProperty> newContents)
   {
      if (newContents != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(newContents);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
   }

   /**
    * {@inheritDoc}
    */
   public OutboundResourceAdapter getOutboundResourceadapter()
   {
      return outboundResourceadapter;
   }

   /**
    * {@inheritDoc}
    */
   public InboundResourceAdapter getInboundResourceadapter()
   {
      return inboundResourceadapter;
   }

   /**
    * {@inheritDoc}
    */
   public List<AdminObject> getAdminObjects()
   {
      return Collections.unmodifiableList(adminobjects);
   }

   /**
    * Force adminobjects with new content.
    * This method is thread safe
    *
    * @param newContent the list of new properties
    */
   public synchronized void forceAdminObjects(List<AdminObject> newContent)
   {
      if (newContent != null)
      {
         this.adminobjects = new ArrayList<AdminObject>(newContent);
      }
      else
      {
         this.adminobjects = new ArrayList<AdminObject>(0);
      }
   }

   /**
    * {@inheritDoc}
    */
   public List<SecurityPermission> getSecurityPermissions()
   {
      return Collections.unmodifiableList(securityPermissions);
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException
   {
      boolean inboundOrOutbound = false;

      if (this.getOutboundResourceadapter() != null && this.getOutboundResourceadapter().validationAsBoolean())
         inboundOrOutbound = true;

      if (this.getInboundResourceadapter() != null && this.getInboundResourceadapter().validationAsBoolean()
            && this.getResourceadapterClass() != null)
         inboundOrOutbound = true;

      if (!inboundOrOutbound
            && (XsdString.isNull(this.resourceadapterClass) || this.resourceadapterClass.getValue().trim().equals("")))
         throw new ValidateException(bundle.invalidMetadataForResourceAdapter());
   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapter merge(MergeableMetadata<?> jmd) throws Exception
   {
      if (jmd instanceof ResourceAdapterImpl)
      {
         ResourceAdapterImpl inputRA = (ResourceAdapterImpl) jmd;

         InboundResourceAdapter newInboundResourceadapter = this.inboundResourceadapter == null
               ? inputRA.inboundResourceadapter
               : this.inboundResourceadapter.merge(inputRA.inboundResourceadapter);

         OutboundResourceAdapter newOutboundResourceadapter = this.outboundResourceadapter == null
               ? inputRA.outboundResourceadapter
               : this.outboundResourceadapter.merge(inputRA.outboundResourceadapter);
         List<SecurityPermission> newSecurityPermission = MergeUtil.mergeList(this.securityPermissions,
               inputRA.securityPermissions);
         List<ConfigProperty> newConfigProperties = MergeUtil.mergeConfigList(this.configProperties,
               inputRA.configProperties);
         List<AdminObject> newAdminobjects = MergeUtil.mergeList(this.adminobjects, inputRA.adminobjects);
         String newId = this.id == null ? inputRA.id : this.id;
         XsdString newResourceadapterClass = XsdString.isNull(this.resourceadapterClass)
               ? inputRA.resourceadapterClass
               : this.resourceadapterClass;
         return new ResourceAdapterImpl(newResourceadapterClass, newConfigProperties, newOutboundResourceadapter,
                                        newInboundResourceadapter, newAdminobjects, newSecurityPermission, newId);
      }
      else
      {
         return this;
      }
   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapter copy()
   {
      return new ResourceAdapterImpl(CopyUtil.clone(resourceadapterClass), CopyUtil.cloneList(configProperties),
            CopyUtil.clone(outboundResourceadapter), CopyUtil.clone(inboundResourceadapter),
            CopyUtil.cloneList(adminobjects), CopyUtil.cloneList(securityPermissions), CopyUtil.cloneString(id));

   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((adminobjects == null) ? 0 : adminobjects.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((inboundResourceadapter == null) ? 0 : inboundResourceadapter.hashCode());
      result = prime * result + ((outboundResourceadapter == null) ? 0 : outboundResourceadapter.hashCode());
      result = prime * result + ((resourceadapterClass == null) ? 0 : resourceadapterClass.hashCode());
      result = prime * result + ((securityPermissions == null) ? 0 : securityPermissions.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ResourceAdapterImpl))
         return false;
      ResourceAdapterImpl other = (ResourceAdapterImpl) obj;
      if (adminobjects == null)
      {
         if (other.adminobjects != null)
            return false;
      }
      else if (!adminobjects.equals(other.adminobjects))
         return false;
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (inboundResourceadapter == null)
      {
         if (other.inboundResourceadapter != null)
            return false;
      }
      else if (!inboundResourceadapter.equals(other.inboundResourceadapter))
         return false;
      if (outboundResourceadapter == null)
      {
         if (other.outboundResourceadapter != null)
            return false;
      }
      else if (!outboundResourceadapter.equals(other.outboundResourceadapter))
         return false;
      if (resourceadapterClass == null)
      {
         if (other.resourceadapterClass != null)
            return false;
      }
      else if (!resourceadapterClass.equals(other.resourceadapterClass))
         return false;
      if (securityPermissions == null)
      {
         if (other.securityPermissions != null)
            return false;
      }
      else if (!securityPermissions.equals(other.securityPermissions))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<").append("resourceadapter");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      if (resourceadapterClass != null)
         sb.append(resourceadapterClass);

      if (configProperties != null)
      {
         for (ConfigProperty cp : configProperties)
         {
            sb.append(cp);
         }
      }

      if (outboundResourceadapter != null)
         sb.append(outboundResourceadapter);

      if (inboundResourceadapter != null)
         sb.append(inboundResourceadapter);

      if (adminobjects != null)
      {
         for (AdminObject ao : adminobjects)
         {
            sb.append(ao);
         }
      }

      if (securityPermissions != null)
      {
         for (SecurityPermission sp : securityPermissions)
         {
            sb.append(sp);
         }
      }

      sb.append("</resourceadapter>");

      return sb.toString();
   }
}
