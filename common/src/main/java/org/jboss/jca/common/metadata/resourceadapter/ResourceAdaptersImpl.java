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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * A ResourceAdaptersImpl.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ResourceAdaptersImpl implements ResourceAdapters
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 4852619088689662467L;
   private final ArrayList<ResourceAdapter> resourceAdapters;

   /**
    * Create a new ResopurceAdaptersImpl.
    *
    * @param resourceAdapters resourceAdapters
    */
   public ResourceAdaptersImpl(List<ResourceAdapter> resourceAdapters)
   {
      super();
      if (resourceAdapters != null)
      {
         this.resourceAdapters = new ArrayList<ResourceAdapter>(resourceAdapters.size());
         this.resourceAdapters.addAll(resourceAdapters);
      }
      else
      {
         this.resourceAdapters = new ArrayList<ResourceAdapter>(0);
      }
   }

   /**
    * Get the resourceAdapters.
    *
    * @return the resourceAdapters.
    */
   @Override
   public final List<ResourceAdapter> getResourceAdapters()
   {
      return Collections.unmodifiableList(resourceAdapters);
   }

   @Override
   public int hashCode()
   {
      return 37 + ((resourceAdapters == null) ? 0 : resourceAdapters.hashCode());
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!(obj instanceof ResourceAdaptersImpl))
         return false;

      ResourceAdaptersImpl other = (ResourceAdaptersImpl) obj;
      if (resourceAdapters == null)
      {
         if (other.resourceAdapters != null)
            return false;
      }
      else if (!resourceAdapters.equals(other.resourceAdapters))
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
      sb.append("<resource-adapters>");

      if (resourceAdapters != null && resourceAdapters.size() > 0)
      {
         for (ResourceAdapter ra : resourceAdapters)
         {
            sb.append(ra);
         }
      }

      sb.append("</resource-adapters>");

      return sb.toString();
   }
}

