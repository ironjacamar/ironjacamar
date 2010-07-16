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

package org.jboss.jca.common.metadata.jbossra;

import org.jboss.jca.common.metadata.JCAMetadata;
import org.jboss.jca.common.metadata.jbossra.jbossra20.RaConfigProperty;

import java.util.Collections;
import java.util.List;

/**
 *
 * A JbossRa. Abstract class containig common memeber for jboss_ra_1_0 and jboss_ra_2_0
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class JbossRa implements JCAMetadata
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private final List<RaConfigProperty<?>> raConfigProperties;

   /**
    *
    * Create a new JbossRa. Protected constructor for subclass convenience
    *
    * @param raConfigProperties properties list
    */
   protected JbossRa(List<RaConfigProperty<?>> raConfigProperties)
   {
      this.raConfigProperties = raConfigProperties;
   }

   /**
    * @return raConfigProperties properties list
    */
   public List<RaConfigProperty<?>> getRaConfigProperties()
   {
      return Collections.unmodifiableList(raConfigProperties);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((raConfigProperties == null) ? 0 : raConfigProperties.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof JbossRa))
         return false;
      JbossRa other = (JbossRa) obj;
      if (raConfigProperties == null)
      {
         if (other.raConfigProperties != null)
            return false;
      }
      else if (!raConfigProperties.equals(other.raConfigProperties))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "JbossRa [raConfigProperties=" + raConfigProperties + "]";
   }

}
