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
import org.ironjacamar.common.api.metadata.spec.AuthenticationMechanism;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.Connector.Version;
import org.ironjacamar.common.api.metadata.spec.Icon;
import org.ironjacamar.common.api.metadata.spec.LicenseType;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.MergeableMetadata;
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
 * Implementation of Connector
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnectorImpl extends AbstractMetadata implements Connector
{
   /** The serial version uid */
   private static final long serialVersionUID = 1L;

   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   private Version version;

   private XsdString moduleName;

   private List<XsdString> requiredWorkContexts;

   private boolean metadataComplete;

   private XsdString resourceadapterVersion;

   private XsdString vendorName;

   private XsdString eisType;

   private LicenseType license;

   private ResourceAdapter resourceadapter;

   private String id;

   private List<LocalizedXsdString> description;

   private List<LocalizedXsdString> displayName;

   private List<Icon> icon;

   /**
    * Constructor
    * @param version The version
    * @param moduleName name of the module
    * @param vendorName vendor name
    * @param eisType eis type
    * @param resourceadapterVersion version number for the RA
    * @param license license information
    * @param resourceadapter full qualified name of the resource adapter
    * @param requiredWorkContexts list od work context required
    * @param metadataComplete not mandatory boolean value
    * @param description descriptions of this connector
    * @param displayNames name to display for this connecotro
    * @param icons icon representing this connectore
    * @param id XML ID
    */
   public ConnectorImpl(Version version, XsdString moduleName, XsdString vendorName, XsdString eisType,
                        XsdString resourceadapterVersion,
                        LicenseType license, ResourceAdapter resourceadapter, List<XsdString> requiredWorkContexts,
                        boolean metadataComplete, List<LocalizedXsdString> description,
                        List<LocalizedXsdString> displayNames, List<Icon> icons, String id)
   {
      super(null);
      this.version = version;
      this.moduleName = moduleName;
      if (!XsdString.isNull(this.moduleName))
         this.moduleName.setTag(XML.ELEMENT_MODULE_NAME);
      if (requiredWorkContexts != null)
      {
         this.requiredWorkContexts = new ArrayList<XsdString>(requiredWorkContexts);
         for (XsdString wc: this.requiredWorkContexts)
            wc.setTag(XML.ELEMENT_REQUIRED_WORK_CONTEXT);
      }
      else
      {
         this.requiredWorkContexts = new ArrayList<XsdString>(0);
      }
      this.metadataComplete = metadataComplete;

      this.resourceadapterVersion = resourceadapterVersion;
      if (!XsdString.isNull(this.resourceadapterVersion))
      {
         if (version == Version.V_10)
         {
            this.resourceadapterVersion.setTag(XML.ELEMENT_VERSION);
         }
         else
         {
            this.resourceadapterVersion.setTag(XML.ELEMENT_RESOURCEADAPTER_VERSION);
         }
      }
      this.vendorName = vendorName;
      if (!XsdString.isNull(this.vendorName))
         this.vendorName.setTag(XML.ELEMENT_VENDOR_NAME);
      
      this.eisType = eisType;
      if (!XsdString.isNull(this.eisType))
         this.eisType.setTag(XML.ELEMENT_EIS_TYPE);
      this.license = license;
      this.resourceadapter = resourceadapter;
      this.id = id;
      if (description != null)
      {
         this.description = new ArrayList<LocalizedXsdString>(description);
         for (LocalizedXsdString d: this.description)
            d.setTag(XML.ELEMENT_DESCRIPTION);
      }
      else
      {
         this.description = new ArrayList<LocalizedXsdString>(0);
      }
      if (displayNames != null)
      {
         this.displayName = new ArrayList<LocalizedXsdString>(displayNames);
         for (LocalizedXsdString d: this.displayName)
            d.setTag(XML.ELEMENT_DISPLAY_NAME);
      }
      else
      {
         this.displayName = new ArrayList<LocalizedXsdString>(0);
      }
      if (icons != null)
      {
         this.icon = new ArrayList<Icon>(icons);
      }
      else
      {
         this.icon = new ArrayList<Icon>(0);
      }
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getVendorName()
   {
      return vendorName;
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getEisType()
   {
      return eisType;
   }

   /**
    * {@inheritDoc}
    */
   public LicenseType getLicense()
   {
      return license;
   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapter getResourceadapter()
   {
      return resourceadapter;
   }

   /**
    * {@inheritDoc}
    */
   public List<LocalizedXsdString> getDescriptions()
   {
      return Collections.unmodifiableList(description);
   }

   /**
    * {@inheritDoc}
    */
   public List<LocalizedXsdString> getDisplayNames()
   {
      return Collections.unmodifiableList(displayName);
   }

   /**
    * {@inheritDoc}
    */
   public List<Icon> getIcons()
   {
      return Collections.unmodifiableList(icon);
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
   public XsdString getResourceadapterVersion()
   {
      return resourceadapterVersion;
   }

   /**
    * {@inheritDoc}
    */
   public List<String> getRequiredWorkContexts()
   {
      List<String> result = new ArrayList<String>(requiredWorkContexts.size());
      for (XsdString wc : requiredWorkContexts)
         result.add(wc.getValue());
      return Collections.unmodifiableList(result);
   }

   /**
    * {@inheritDoc}
    */
   public String getModuleName()
   {
      if (XsdString.isNull(moduleName))
         return null;
      return moduleName.getValue();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMetadataComplete()
   {
      return metadataComplete;
   }

   /**
    * {@inheritDoc}
    */
   public Version getVersion()
   {
      return version;
   }

   /**
    * {@inheritDoc}
    */
   public Connector merge(MergeableMetadata<?> inputMd) throws Exception
   {
      if (inputMd instanceof ConnectorImpl)
      {
         ConnectorImpl input = (ConnectorImpl) inputMd;
         XsdString newResourceadapterVersion = XsdString.isNull(this.resourceadapterVersion)
               ? input.resourceadapterVersion
               : this.resourceadapterVersion;
         XsdString newEisType = XsdString.isNull(this.eisType) ? input.eisType : this.eisType;
         List<XsdString> newRequiredWorkContexts = MergeUtil.mergeList(this.requiredWorkContexts,
               input.requiredWorkContexts);
         XsdString newModuleName = this.moduleName == null ? input.moduleName : this.moduleName;
         List<Icon> newIcons = MergeUtil.mergeList(this.icon, input.icon);
         boolean newMetadataComplete = this.metadataComplete || input.metadataComplete;
         LicenseType newLicense = this.license == null ? input.license : this.license.merge(input.license);
         List<LocalizedXsdString> newDescriptions = MergeUtil.mergeList(this.description, input.description);
         List<LocalizedXsdString> newDisplayNames = MergeUtil.mergeList(this.displayName, input.displayName);
         XsdString newVendorName = XsdString.isNull(this.vendorName) ? input.vendorName : this.vendorName;;
         ResourceAdapter newResourceadapter = this.resourceadapter == null
               ? (ResourceAdapter) input.resourceadapter
               : ((ResourceAdapter) this.resourceadapter).merge((ResourceAdapter) input.resourceadapter);
         return new ConnectorImpl(version, newModuleName, newVendorName, newEisType, newResourceadapterVersion,
                                  newLicense, newResourceadapter, newRequiredWorkContexts, newMetadataComplete,
                                  newDescriptions, newDisplayNames, newIcons, null);
      }
      return this;

   }

   /**
    * {@inheritDoc}
    */
   public Connector copy()
   {
      XsdString newResourceadapterVersion = CopyUtil.clone(this.resourceadapterVersion);
      XsdString newEisType = XsdString.isNull(this.eisType) ? null : (XsdString) this.eisType.copy();
      List<XsdString> newRequiredWorkContexts = CopyUtil.cloneList(this.requiredWorkContexts);
      XsdString newModuleName = CopyUtil.clone(this.moduleName);
      List<Icon> newIcons = CopyUtil.cloneList(this.icon);
      boolean newMetadataComplete = this.metadataComplete;
      LicenseType newLicense = CopyUtil.clone(this.license);
      List<LocalizedXsdString> newDescriptions = CopyUtil.cloneList(this.description);
      List<LocalizedXsdString> newDisplayNames = CopyUtil.cloneList(this.displayName);
      XsdString newVendorName = CopyUtil.clone(this.vendorName);
      ResourceAdapter newResourceadapter = CopyUtil.clone((ResourceAdapter) this.resourceadapter);
      return new ConnectorImpl(version, newModuleName, newVendorName, newEisType, newResourceadapterVersion,
                               newLicense, newResourceadapter, newRequiredWorkContexts, newMetadataComplete,
                               newDescriptions, newDisplayNames, newIcons, CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
   public void validate() throws ValidateException
   {
      ResourceAdapter ra = this.getResourceadapter();

      if (ra == null)
         throw new ValidateException(bundle.noMetadataForResourceAdapter());

      ra.validate();
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((eisType == null) ? 0 : eisType.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((license == null) ? 0 : license.hashCode());
      result = prime * result + ((resourceadapter == null) ? 0 : resourceadapter.hashCode());
      result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
      result = prime * result + ((resourceadapterVersion == null) ? 0 : resourceadapterVersion.hashCode());
      result = prime * result + (metadataComplete ? 1231 : 1237);
      result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
      result = prime * result + ((requiredWorkContexts == null) ? 0 : requiredWorkContexts.hashCode());
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

      if (!(obj instanceof ConnectorImpl))
         return false;

      ConnectorImpl other = (ConnectorImpl) obj;
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
      if (description == null)
      {
         if (other.description != null)
            return false;
      }
      else if (!description.equals(other.description))
         return false;
      if (displayName == null)
      {
         if (other.displayName != null)
            return false;
      }
      else if (!displayName.equals(other.displayName))
         return false;
      if (resourceadapterVersion == null)
      {
         if (other.resourceadapterVersion != null)
            return false;
      }
      else if (!resourceadapterVersion.equals(other.resourceadapterVersion))
         return false;
      if (metadataComplete != other.metadataComplete)
         return false;
      if (moduleName == null)
      {
         if (other.moduleName != null)
            return false;
      }
      else if (!moduleName.equals(other.moduleName))
         return false;
      if (requiredWorkContexts == null)
      {
         if (other.requiredWorkContexts != null)
            return false;
      }
      else if (!requiredWorkContexts.equals(other.requiredWorkContexts))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

      // For V_10, we just hack everything here
      if (version == Version.V_10)
      {
         sb.append("<").append("connector");
         if (id != null)
            sb.append(" id=\"" + id + "\"");
         sb.append(">");

         for (LocalizedXsdString l : displayName)
         {
            sb.append(l);
         }

         for (LocalizedXsdString l : description)
         {
            sb.append(l);
         }

         for (Icon i : icon)
         {
            sb.append(i);
         }

         if (!XsdString.isNull(vendorName))
            sb.append(vendorName);

         sb.append("<").append(XML.ELEMENT_SPEC_VERSION).append(">");
         sb.append("1.0");
         sb.append("</").append(XML.ELEMENT_SPEC_VERSION).append(">");

         if (!XsdString.isNull(eisType))
            sb.append(eisType);

         if (!XsdString.isNull(resourceadapterVersion))
            sb.append(resourceadapterVersion);

         if (license != null)
            sb.append(license);

         sb.append("<").append(XML.ELEMENT_RESOURCEADAPTER);
         if (resourceadapter.getId() != null)
            sb.append(" id=\"" + resourceadapter.getId() + "\"");
         sb.append(">");

         ConnectionDefinition cd = resourceadapter.getOutboundResourceadapter().getConnectionDefinitions().get(0);
         sb.append(cd.getManagedConnectionFactoryClass());
         sb.append(cd.getConnectionFactoryInterface());
         sb.append(cd.getConnectionFactoryImplClass());
         sb.append(cd.getConnectionInterface());
         sb.append(cd.getConnectionImplClass());
         sb.append(cd.getConnectionImplClass());

         sb.append("<").append(XML.ELEMENT_TRANSACTION_SUPPORT);
         if (resourceadapter.getOutboundResourceadapter().getTransactionSupportId() != null)
            sb.append(" id=\"" + resourceadapter.getOutboundResourceadapter().getTransactionSupportId() + "\"");
         sb.append(">");
         sb.append(resourceadapter.getOutboundResourceadapter().getTransactionSupport());
         sb.append("</").append(XML.ELEMENT_TRANSACTION_SUPPORT).append(">");

         for (ConfigProperty cp : cd.getConfigProperties())
         {
            sb.append(cp);
         }

         for (AuthenticationMechanism am : resourceadapter.getOutboundResourceadapter().getAuthenticationMechanisms())
         {
            sb.append(am);
         }

         sb.append("<").append(XML.ELEMENT_REAUTHENTICATION_SUPPORT);
         if (resourceadapter.getOutboundResourceadapter().getReauthenticationSupportId() != null)
            sb.append(" id=\"" + resourceadapter.getOutboundResourceadapter().getReauthenticationSupportId() + "\"");
         sb.append(">");
         sb.append(resourceadapter.getOutboundResourceadapter().getReauthenticationSupport());
         sb.append("</").append(XML.ELEMENT_REAUTHENTICATION_SUPPORT).append(">");

         for (SecurityPermission sp : resourceadapter.getSecurityPermissions())
         {
            sb.append(sp);
         }

         sb.append("</").append(XML.ELEMENT_RESOURCEADAPTER).append(">");
      }
      else
      {
         sb.append("<").append("connector");
         sb.append(" " + XML.ATTRIBUTE_VERSION + "=\"" + version + "\"");
         if (version == Version.V_16 || version == Version.V_17 || version == Version.V_20)
            sb.append(" " + XML.ATTRIBUTE_METADATA_COMPLETE + "=\"" + metadataComplete + "\"");
         if (id != null)
            sb.append(" id=\"" + id + "\"");
         sb.append(">");

         if (!XsdString.isNull(moduleName))
            sb.append(moduleName);

         for (LocalizedXsdString l : description)
         {
            sb.append(l);
         }

         for (LocalizedXsdString l : displayName)
         {
            sb.append(l);
         }

         for (Icon i : icon)
         {
            sb.append(i);
         }

         if (!XsdString.isNull(vendorName))
            sb.append(vendorName);

         if (!XsdString.isNull(eisType))
            sb.append(eisType);

         if (!XsdString.isNull(resourceadapterVersion))
            sb.append(resourceadapterVersion);

         if (license != null)
            sb.append(license);

         sb.append(resourceadapter);

         if (requiredWorkContexts != null)
         {
            for (XsdString rwc : requiredWorkContexts)
               sb.append(rwc);
         }
      }

      sb.append("</").append("connector").append(">");

      return sb.toString();
   }
}
