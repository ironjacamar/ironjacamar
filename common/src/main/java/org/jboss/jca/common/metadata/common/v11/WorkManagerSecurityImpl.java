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
package org.jboss.jca.common.metadata.common.v11;

import org.jboss.jca.common.api.metadata.common.v11.WorkManagerSecurity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * WorkManager security configuration
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class WorkManagerSecurityImpl implements WorkManagerSecurity
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** Mapping required */
   private boolean mappingRequired;

   /** Domain */
   private String domain;

   /** Default principal */
   private String defaultPrincipal;

   /** Default groups */
   private List<String> defaultGroups;

   /** User mappings */
   private Map<String, String> userMappings;

   /** Group mappings */
   private Map<String, String> groupMappings;

   /**
    * Constructor
    * @param mappingRequired Is a mapping required
    * @param domain The security domain
    * @param defaultPrincipal A default principal
    * @param defaultGroups Default groups
    * @param userMappings User mappings
    * @param groupMappings Group mappings
    */
   public WorkManagerSecurityImpl(boolean mappingRequired, String domain,
                                  String defaultPrincipal, List<String> defaultGroups,
                                  Map<String, String> userMappings, Map<String, String> groupMappings)
   {
      this.mappingRequired = mappingRequired;
      this.domain = domain;
      this.defaultPrincipal = defaultPrincipal;
      this.defaultGroups = defaultGroups;
      this.userMappings = userMappings;
      this.groupMappings = groupMappings;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMappingRequired()
   {
      return mappingRequired;
   }

   /**
    * {@inheritDoc}
    */
   public String getDomain()
   {
      return domain;
   }

   /**
    * {@inheritDoc}
    */
   public String getDefaultPrincipal()
   {
      return defaultPrincipal;
   }

   /**
    * {@inheritDoc}
    */
   public List<String> getDefaultGroups()
   {
      return defaultGroups != null ? Collections.unmodifiableList(defaultGroups) : null;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, String> getUserMappings()
   {
      return userMappings != null ? Collections.unmodifiableMap(userMappings) : null;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, String> getGroupMappings()
   {
      return groupMappings != null ? Collections.unmodifiableMap(groupMappings) : null;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      int result = 31;

      result += mappingRequired ? 7 : 0;
      result += domain != null ? 7 * domain.hashCode() : 7;
      result += defaultPrincipal != null ? 7 * defaultPrincipal.hashCode() : 7;
      result += defaultGroups != null ? 7 * defaultGroups.hashCode() : 7;
      result += userMappings != null ? 7 * userMappings.hashCode() : 7;
      result += groupMappings != null ? 7 * groupMappings.hashCode() : 7;

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object o)
   {
      if (this == o)
         return true;

      if (o == null || !(o instanceof WorkManagerSecurityImpl))
         return false;

      WorkManagerSecurityImpl other = (WorkManagerSecurityImpl)o;

      if (mappingRequired != other.mappingRequired)
         return false;

      if (domain != null)
      {
         if (!domain.equals(other.domain))
            return false;
      }
      else
      {
         if (other.domain != null)
            return false;
      }

      if (defaultPrincipal != null)
      {
         if (!defaultPrincipal.equals(other.defaultPrincipal))
            return false;
      }
      else
      {
         if (other.defaultPrincipal != null)
            return false;
      }

      if (defaultGroups != null)
      {
         if (!defaultGroups.equals(other.defaultGroups))
            return false;
      }
      else
      {
         if (other.defaultGroups != null)
            return false;
      }

      if (userMappings != null)
      {
         if (!userMappings.equals(other.userMappings))
            return false;
      }
      else
      {
         if (other.userMappings != null)
            return false;
      }

      if (groupMappings != null)
      {
         if (!groupMappings.equals(other.groupMappings))
            return false;
      }
      else
      {
         if (other.groupMappings != null)
            return false;
      }

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<security>");

      sb.append("<").append(WorkManagerSecurity.Tag.MAPPING_REQUIRED).append(">");
      sb.append(mappingRequired);
      sb.append("</").append(WorkManagerSecurity.Tag.MAPPING_REQUIRED).append(">");

      sb.append("<").append(WorkManagerSecurity.Tag.DOMAIN).append(">");
      sb.append(domain);
      sb.append("</").append(WorkManagerSecurity.Tag.DOMAIN).append(">");

      if (defaultPrincipal != null)
      {
         sb.append("<").append(WorkManagerSecurity.Tag.DEFAULT_PRINCIPAL).append(">");
         sb.append(defaultPrincipal);
         sb.append("</").append(WorkManagerSecurity.Tag.DEFAULT_PRINCIPAL).append(">");
      }

      if (defaultGroups != null && defaultGroups.size() > 0)
      {
         sb.append("<").append(WorkManagerSecurity.Tag.DEFAULT_GROUPS).append(">");
         for (String group : defaultGroups)
         {
            sb.append("<").append(WorkManagerSecurity.Tag.GROUP).append(">");
            sb.append(group);
            sb.append("</").append(WorkManagerSecurity.Tag.GROUP).append(">");
         }
         sb.append("</").append(WorkManagerSecurity.Tag.DEFAULT_GROUPS).append(">");
      }

      if ((userMappings != null && userMappings.size() > 0) || (groupMappings != null && groupMappings.size() > 0))
      {
         sb.append("<").append(WorkManagerSecurity.Tag.MAPPINGS).append(">");

         if (userMappings != null && userMappings.size() > 0)
         {
            for (Map.Entry<String, String> entry : userMappings.entrySet())
            {
               sb.append("<").append(WorkManagerSecurity.Tag.MAP);

               sb.append(" ").append(WorkManagerSecurity.Attribute.FROM).append("=\"");
               sb.append(entry.getKey()).append("\"");

               sb.append(" ").append(WorkManagerSecurity.Attribute.TO).append("=\"");
               sb.append(entry.getValue()).append("\"");

               sb.append("/>");
            }
         }

         if (groupMappings != null && groupMappings.size() > 0)
         {
            for (Map.Entry<String, String> entry : groupMappings.entrySet())
            {
               sb.append("<").append(WorkManagerSecurity.Tag.MAP);

               sb.append(" ").append(WorkManagerSecurity.Attribute.FROM).append("=\"");
               sb.append(entry.getKey()).append("\"");

               sb.append(" ").append(WorkManagerSecurity.Attribute.TO).append("=\"");
               sb.append(entry.getValue()).append("\"");

               sb.append("/>");
            }
         }

         sb.append("</").append(WorkManagerSecurity.Tag.MAPPINGS).append(">");
      }

      sb.append("</security>");
      
      return sb.toString();
   }
}
