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
package org.ironjacamar.embedded.deployers;

import org.ironjacamar.core.api.metadatarepository.Metadata;
import org.ironjacamar.core.api.metadatarepository.MetadataRepository;

import java.net.URL;

import org.jboss.logging.Logger;

import com.github.fungal.spi.deployers.Deployment;

/**
 * A metadata deployment
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MetadataDeployment implements Deployment
{
   /** The logger */
   private static Logger log = Logger.getLogger(MetadataDeployment.class);
   
   /** The deployment */
   private URL deployment;
   
   /** The metadata */
   private Metadata md;

   /** The metadata repository */
   private MetadataRepository mdr;

   /**
    * Constructor
    * @param deployment The deployment url
    * @param md The metadata
    * @param mdr The metadata repository
    */
   public MetadataDeployment(URL deployment, Metadata md, MetadataRepository mdr)
   {
      this.deployment = deployment;
      this.md = md;
      this.mdr = mdr;
   }

   /**
    * {@inheritDoc}
    */
   public URL getURL()
   {
      return deployment;
   }

   /**
    * {@inheritDoc}
    */
   public ClassLoader getClassLoader()
   {
      return getClass().getClassLoader();
   }

   /**
    * Stop
    */
   public void stop()
   {
      if (mdr != null)
      {
         try
         {
            mdr.unregisterMetadata(md);
         }
         catch (Throwable t)
         {
            log.warn("Exception during unregistering deployment in the metadata repository", t);
         }
      }
   }
}
