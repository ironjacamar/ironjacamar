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

import org.ironjacamar.common.api.metadata.CopyUtil;
import org.ironjacamar.common.api.metadata.spec.Activationspec;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.RequiredConfigProperty;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ActivationSpecImpl extends AbstractMetadata implements Activationspec
{
   private static final long serialVersionUID = 1L;

   private XsdString activationspecClass;
   private List<RequiredConfigProperty> requiredConfigProperty;
   private String id;
   private List<ConfigProperty> configProperties;

   /**
    * Constructor
    * @param activationspecClass full qualified name of the class
    * @param requiredConfigProperty a List of required config properties
    * @param configProperties a list of (optional) config property
    * @param id xmlID
    */
   public ActivationSpecImpl(XsdString activationspecClass, List<RequiredConfigProperty> requiredConfigProperty,
                             List<ConfigProperty> configProperties, String id)
   {
      super(null);
      this.activationspecClass = activationspecClass;
      if (!XsdString.isNull(this.activationspecClass))
         this.activationspecClass.setTag(XML.ELEMENT_ACTIVATIONSPEC_CLASS);
      if (requiredConfigProperty != null)
      {
         this.requiredConfigProperty = new ArrayList<RequiredConfigProperty>(requiredConfigProperty);
      }
      else
      {
         this.requiredConfigProperty = new ArrayList<RequiredConfigProperty>(0);
      }
      this.id = id;
      if (configProperties != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(configProperties);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
   }

   /**
    * {@inheritDoc}
    */
   public XsdString getActivationspecClass()
   {
      return activationspecClass;
   }

   /**
    * {@inheritDoc}
    */
   public List<RequiredConfigProperty> getRequiredConfigProperties()
   {
      return Collections.unmodifiableList(requiredConfigProperty);
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
   public List<ConfigProperty> getConfigProperties()
   {
      return Collections.unmodifiableList(configProperties);
   }

   /**
    * {@inheritDoc}
    */
   public Activationspec copy()
   {
      return new ActivationSpecImpl(CopyUtil.clone(activationspecClass),
                                    CopyUtil.cloneList(requiredConfigProperty),
                                    CopyUtil.cloneList(configProperties),
                                    CopyUtil.cloneString(id));
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((activationspecClass == null) ? 0 : activationspecClass.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((requiredConfigProperty == null) ? 0 : requiredConfigProperty.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
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
      if (!(obj instanceof ActivationSpecImpl))
         return false;
      ActivationSpecImpl other = (ActivationSpecImpl) obj;
      if (activationspecClass == null)
      {
         if (other.activationspecClass != null)
            return false;
      }
      else if (!activationspecClass.equals(other.activationspecClass))
         return false;
      if (id == null)
      {
         if (other.id != null)
            return false;
      }
      else if (!id.equals(other.id))
         return false;
      if (requiredConfigProperty == null)
      {
         if (other.requiredConfigProperty != null)
            return false;
      }
      else if (!requiredConfigProperty.equals(other.requiredConfigProperty))
         return false;
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<activationspec");
      if (id != null)
         sb.append(" id=\"").append(id).append("\"");
      sb.append(">");

      sb.append(activationspecClass);

      if (requiredConfigProperty != null)
      {
         for (RequiredConfigProperty rcp : requiredConfigProperty)
         {
            sb.append(rcp);
         }
      }

      if (configProperties != null)
      {
         for (ConfigProperty cp : configProperties)
         {
            sb.append(cp);
         }
      }

      sb.append("</activationspec>");

      return sb.toString();
   }
}
