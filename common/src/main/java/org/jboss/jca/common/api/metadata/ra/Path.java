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
package org.jboss.jca.common.api.metadata.ra;

import org.jboss.jca.common.api.metadata.CopyUtil;
import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.JCAMetadata;

import java.security.InvalidParameterException;

/**
 * @author <a href="mailto:stefano.maestri@jboss.org">Stefano Maestri</a>
 *
 */
public class Path implements JCAMetadata, CopyableMetaData
{
   /**
    */
   private static final long serialVersionUID = 3452844893341380928L;

   private final String value;
   

   /**
    * @param value Path String
    */
   private Path(String value)
   {
      super();
      this.value = value;
   }

   /**
    *
    * convenient method to cfreate a path object validating it according JCA specs
    *
    * @param path the Path String
    * @return the Path object
    * @throws InvalidParameterException in case path could not be validated according JCA specs
    */
   public static Path valueOf(String path) throws InvalidParameterException
   {
      if (isValid(path))
      {
         return new Path(path);
      }
      else
      {
         throw new InvalidParameterException();
      }
   }

   // implement me

   private static boolean isValid(String path)
   {
      return true;
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
      result = prime * result + ((value == null) ? 0 : value.hashCode());
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
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof Path))
      {
         return false;
      }
      Path other = (Path) obj;
      if (value == null)
      {
         if (other.value != null)
         {
            return false;
         }
      }
      else if (!value.equals(other.value))
      {
         return false;
      }
      return true;
   }

   /**
    * {@inheritDoc}
    *
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Path [value=" + value + "]";
   }

   @Override
   public CopyableMetaData copy()
   {
      return new Path(CopyUtil.cloneString(value));
   }

}
