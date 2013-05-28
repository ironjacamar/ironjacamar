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
package org.jboss.jca.common.metadata.ra.common;

import org.jboss.jca.common.api.metadata.CopyableMetaData;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A AbstractResourceAdapetrImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public abstract class AbstractResourceAdapetrImpl implements ResourceAdapter
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 6284653702718994497L;

   /** configProperties **/
   protected ArrayList<ConfigProperty> configProperties;

   /**
    * Get the configProperties.
    *
    * @return the configProperties.
    */
   @Override
   public synchronized List<? extends ConfigProperty> getConfigProperties()
   {
      return configProperties;
   }

   /**
    *
    * force configProperties with new content.
    * This method is thread safe
    *
    * @param newContents the list of new properties
    */
   public synchronized void forceNewConfigPropertiesContent(List<? extends ConfigProperty> newContents)
   {
      if (newContents != null)
      {
         this.configProperties = new ArrayList<ConfigProperty>(newContents.size());
         this.configProperties.addAll(newContents);
      }
      else
      {
         this.configProperties = new ArrayList<ConfigProperty>(0);
      }
   }

   @Override
   public abstract String getId();

   @Override
   public abstract void validate() throws ValidateException;

   @Override
   public abstract CopyableMetaData copy();

}
