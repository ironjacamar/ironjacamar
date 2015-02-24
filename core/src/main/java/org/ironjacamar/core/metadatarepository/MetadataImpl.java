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

import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.core.api.metadatarepository.Metadata;

import java.io.File;

/**
 * The metadata implementation
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MetadataImpl implements Metadata
{
   /** The name */
   private String name;
   
   /** The metadata */
   private Connector metadata;
   
   /** The archive */
   private File archive;

   /**
    * Constructor
    * @param name The name
    * @param metadata The metadata
    * @param archive The archive
    */
   public MetadataImpl(String name,
                       Connector metadata,
                       File archive)
   {
      this.name = name;
      this.metadata = metadata;
      this.archive = archive;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public Connector getMetadata()
   {
      return metadata;
   }

   /**
    * {@inheritDoc}
    */
   public File getArchive()
   {
      return archive;
   }
}
