/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.metadatarepository;

import org.ironjacamar.core.api.metadatarepository.Metadata;
import org.ironjacamar.core.api.metadatarepository.MetadataRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The metadata repository implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MetadataRepositoryImpl implements MetadataRepository
{
   /** The deployments */
   private Collection<Metadata> metadata;

   /**
    * Constructor
    */
   public MetadataRepositoryImpl()
   {
      this.metadata = Collections.synchronizedCollection(new ArrayList<Metadata>());
   }
   
   /**
    * {@inheritDoc}
    */
   public Collection<Metadata> getMetadata()
   {
      return Collections.unmodifiableCollection(metadata);
   }

   /**
    * {@inheritDoc}
    */
   public Metadata findByName(String name)
   {
      for (Metadata m : metadata)
      {
         if (m.getName().equals(name))
            return m;
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean registerMetadata(Metadata md)
   {
      if (metadata.contains(md))
         return false;

      return metadata.add(md);
   }

   /**
    * {@inheritDoc}
    */
   public boolean unregisterMetadata(Metadata md)
   {
      if (!metadata.contains(md))
         return false;

      return metadata.remove(md);
   }
}
