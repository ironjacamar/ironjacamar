/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.metadata.ra.common;

import org.jboss.jca.common.api.metadata.jbossra.JbossRa;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.MergeableMetadata;
import org.jboss.jca.common.api.metadata.ra.OverrideElementAttribute;
import org.jboss.jca.common.api.metadata.ra.RaConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter1516;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra10.Connector10;
import org.jboss.jca.common.api.metadata.ra.ra16.Activationspec16;
import org.jboss.jca.common.api.metadata.ra.ra16.Connector16;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * A Connector.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class ConnectorAbstractmpl implements Connector
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -2054156739973617322L;

   /**
    * vendor name
    */
   protected final XsdString vendorName;

   /**
    * EIS type
    */
   protected final XsdString eisType;

   /**
    * license information
    */
   protected final LicenseType license;

   /**
    * resource adapter
    */
   protected final ResourceAdapter resourceadapter;

   /**
    * id attribute
    */
   protected final String id;

   /**
    * Create a new Connector.
    *
    * @param vendorName vandor name
    * @param eisType tyeo of EIS
    * @param license license information
    * @param resourceadapter resource adapter instance
    * @param id id attribute in xml file
    */
   protected ConnectorAbstractmpl(XsdString vendorName, XsdString eisType, LicenseType license,
         ResourceAdapter resourceadapter, String id)
   {
      super();
      this.vendorName = vendorName;
      this.eisType = eisType;
      this.license = license;
      this.resourceadapter = resourceadapter;
      this.id = id;
   }

   /**
    * Get the vendorName.
    *
    * @return the vendorName.
    */
   @Override
   public XsdString getVendorName()
   {
      return vendorName;
   }

   /**
    * Get the eisType.
    *
    * @return the eisType.
    */
   @Override
   public XsdString getEisType()
   {
      return eisType;
   }

   /**
    * Get the license.
    *
    * @return the license.
    */
   @Override
   public LicenseType getLicense()
   {
      return license;
   }

   /**
    * Get the resourceadapter.
    *
    * @return the resourceadapter.
    */
   @Override
   public ResourceAdapter getResourceadapter()
   {
      return resourceadapter;
   }

   /**
    * Get the id.
    *
    * @return the id.
    */
   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((eisType == null) ? 0 : eisType.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((license == null) ? 0 : license.hashCode());
      result = prime * result + ((resourceadapter == null) ? 0 : resourceadapter.hashCode());
      result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ConnectorAbstractmpl))
         return false;
      ConnectorAbstractmpl other = (ConnectorAbstractmpl) obj;
      if (eisType == null)
      {
         if (other.eisType != null)
            return false;
      }
      else if (!eisType.equals(other.eisType))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (license == null)
      {
         if (other.license != null)
            return false;
      }
      else if (!license.equals(other.license))
         return false;
      if (resourceadapter == null)
      {
         if (other.resourceadapter != null)
            return false;
      }
      else if (!resourceadapter.equals(other.resourceadapter))
         return false;
      if (vendorName == null)
      {
         if (other.vendorName != null)
            return false;
      }
      else if (!vendorName.equals(other.vendorName))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "Connector [vendorName=" + vendorName + ", eisType=" + eisType + ", license=" + license
            + ", resourceadapter=" + resourceadapter + ", id=" + id + "]";
   }

   /**
    * Validate specification metadata
    * @exception ValidateException Thrown if an error occurs
    */
   @Override
   public void validate() throws ValidateException
   {
      ResourceAdapter ra = this.getResourceadapter();

      //make sure all need metadata parsered and processed after annotation handle
      if (ra == null)
         throw new ValidateException("ResourceAdapter metadata should be defined");

      //make sure ra metadata contains inbound or outbound at least
      ra.validate();

   }

   /**
    * Merge metadatas
    * @param inputMd The metadata to merge with this
    * @exception Exception Thrown if an error occurs
    * @return a new immutable connector instance result of the merging
    */
   @Override
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {
      {
         if (inputMd instanceof JbossRa)
         {
            mergeJbossMetaData((JbossRa) inputMd);
         }

         return this;

      }
   }

   /**
    * Merge specification metadata with vendor metadata
    *
    * @param jmd the vendor metadata
    */
   protected void mergeJbossMetaData(JbossRa jmd)
   {
      if (jmd != null)
      {
         /*
         <xs:restriction base="javaee:string">
         <xs:enumeration value="connection-definition"/>
         <xs:enumeration value="resourceadapter"/>
         <xs:enumeration value="activationspec"/>
         <xs:enumeration value="adminobject"/>
         </xs:restriction>
         */

         List<RaConfigProperty<?>> props = jmd.getRaConfigProperties();

         List<ConfigProperty> append = null;

         if (props != null)
         {
            for (RaConfigProperty<?> rcmd : props)
            {
               List<? extends ConfigProperty> listConfigProp = null;
               OverrideElementAttribute override = rcmd.getOverrideElementAttribute();
               if (override == OverrideElementAttribute.UNKNOWN
                     || override == OverrideElementAttribute.RESOURCE_ADAPTER)
               {
                  if (this.getResourceadapter() != null)
                  {
                     listConfigProp = this.getResourceadapter().getConfigProperties();
                  }
               }
               else if (override == OverrideElementAttribute.CONNECTION_DEFINITION)
               {
                  if (this.getResourceadapter() != null
                        && !(this instanceof Connector10)
                        && ((ResourceAdapter1516) this.getResourceadapter()).getOutboundResourceadapter() != null
                        && ((ResourceAdapter1516) this.getResourceadapter()).getOutboundResourceadapter()
                              .getConnectionDefinitions() != null
                        && ((ResourceAdapter1516) this.getResourceadapter()).getOutboundResourceadapter()
                              .getConnectionDefinitions().size() > 0
                        && ((ResourceAdapter1516) this.getResourceadapter()).getOutboundResourceadapter()
                              .getConnectionDefinitions().get(0) != null)
                  {
                     listConfigProp = ((ResourceAdapter1516) this.getResourceadapter()).getOutboundResourceadapter()
                           .getConnectionDefinitions().get(0).getConfigProperties();
                  }
               }
               else if (override == OverrideElementAttribute.ACTIVATIONSPEC)
               {
                  if (this.getResourceadapter() != null
                        && (this instanceof Connector16)
                        && ((ResourceAdapter1516) this.getResourceadapter()).getInboundResourceadapter() != null
                        && ((ResourceAdapter1516) this.getResourceadapter()).getInboundResourceadapter()
                              .getMessageadapter() != null
                        && ((ResourceAdapter1516) this.getResourceadapter()).getInboundResourceadapter()
                              .getMessageadapter().getMessagelisteners() != null
                        && ((ResourceAdapter1516) this.getResourceadapter()).getInboundResourceadapter()
                              .getMessageadapter().getMessagelisteners().size() > 0
                        && ((ResourceAdapter1516) this.getResourceadapter()).getInboundResourceadapter()
                              .getMessageadapter().getMessagelisteners().get(0) != null
                        && ((ResourceAdapter1516) this.getResourceadapter()).getInboundResourceadapter()
                              .getMessageadapter().getMessagelisteners().get(0).getActivationspec() != null)
                  {
                     listConfigProp = ((Activationspec16) ((ResourceAdapter1516) this.getResourceadapter())
                           .getInboundResourceadapter()
                           .getMessageadapter().getMessagelisteners().get(0).getActivationspec()).getConfigProperties();
                  }
               }
               else if (override == OverrideElementAttribute.ADMINOBJECT)
               {
                  if (this.getResourceadapter() != null
                        && !(this instanceof Connector10)
                        && ((ResourceAdapter1516) this.getResourceadapter()).getAdminobjects() != null
                        && ((ResourceAdapter1516) this.getResourceadapter()).getAdminobjects().size() > 0
                        && ((ResourceAdapter1516) this.getResourceadapter()).getAdminobjects().get(0) != null)
                  {
                     listConfigProp = ((ResourceAdapter1516) this.getResourceadapter()).getAdminobjects().get(0)
                           .getConfigProperties();
                  }
               }

               boolean found = false;

               if (listConfigProp != null)
               {
                  Iterator<? extends ConfigProperty> it = listConfigProp.iterator();

                  while (!found && it.hasNext())
                  {
                     ConfigProperty cpmd = it.next();
                     if (cpmd.getConfigPropertyName().getValue().equals(rcmd.getName())
                           && cpmd.getConfigPropertyType().getValue().equals(rcmd.getTypeName()))
                     {
                        found = true;
                     }
                  }
               }

               if (!found)
               {
                  if (append == null)
                     append = new ArrayList<ConfigProperty>();

                  ConfigProperty cpmd = new ConfigPropertyImpl(null, new XsdString(rcmd.getName(), null),
                        new XsdString(
                        rcmd.getTypeName(), null), new XsdString(rcmd.getValue().toString(), null), null);

                  append.add(cpmd);
               }
            }

            if (append != null)
            {
               for (ConfigProperty cpmd : append)
               {
                  ((List<ConfigProperty>) this.getResourceadapter().getConfigProperties()).add(cpmd);
               }
            }
         }
      }
   }

}
