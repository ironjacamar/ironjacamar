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
package org.jboss.jca.common.metadata.jbossra.jbossra20;

import org.jboss.jca.common.metadata.jbossra.JbossRa;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 */
public class JbossRa20 extends JbossRa
{

   /**
    * NAMESPACE of xml file for which this metadata is generated
    */
   public static final String NAMESPACE = "http://www.jboss.org/schema/ra/2.0";

   private static final long serialVersionUID = -1494921311038998843L;

   private final String bootstrapContext;

   private final List<BeanValidationGroup> beanValidationGroups;

   /**
    * @param raConfigProperties List of properties for configuration
    * @param bootstrapContext String representing the bootstrap context name
    * @param beanValidationGroups for validations
    */
   public JbossRa20(List<RaConfigProperty<?>> raConfigProperties, String bootstrapContext,
         List<BeanValidationGroup> beanValidationGroups)
   {
      super(raConfigProperties);
      this.bootstrapContext = bootstrapContext;
      this.beanValidationGroups = beanValidationGroups;
   }

   /**
    * @return bootstrapContext
    */
   public String getBootstrapContext()
   {
      return bootstrapContext;
   }

   /**
    * @return beanValidationGroups
    */
   public List<BeanValidationGroup> getBeanValidationGroups()
   {
      return Collections.unmodifiableList(beanValidationGroups);
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((beanValidationGroups == null) ? 0 : beanValidationGroups.hashCode());
      result = prime * result + ((bootstrapContext == null) ? 0 : bootstrapContext.hashCode());
      result = prime * result + ((getRaConfigProperties() == null) ? 0 : getRaConfigProperties().hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof JbossRa20))
         return false;
      JbossRa20 other = (JbossRa20) obj;
      if (beanValidationGroups == null)
      {
         if (other.beanValidationGroups != null)
            return false;
      }
      else if (!beanValidationGroups.equals(other.beanValidationGroups))
         return false;
      if (bootstrapContext == null)
      {
         if (other.bootstrapContext != null)
            return false;
      }
      else if (!bootstrapContext.equals(other.bootstrapContext))
         return false;
      if (getRaConfigProperties() == null)
      {
         if (other.getRaConfigProperties() != null)
            return false;
      }
      else if (!getRaConfigProperties().equals(other.getRaConfigProperties()))
         return false;
      return true;
   }

   /**
    *
    * A Tag.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public enum Tag
   {
      /**always first
       *
       */
      UNKNOWN(null),

      /** ra-config-property tag name
       *
       */
      RA_CONFIG_PROPERTY("ra-config-property"),

      /**
       * bootstrap-context tag name
       */
      BOOTSTRAP_CONTEXT("bootstrap-context"),

      /**
       * bean-validation-groups tag name
       */
      BEAN_VALIDATION_GROUPS ("bean-validation-groups");

      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
      *
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }

}
