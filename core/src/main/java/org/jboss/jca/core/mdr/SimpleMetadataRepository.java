/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.mdr;

import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;

import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A simple implementation of the metadata repository
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class SimpleMetadataRepository implements MetadataRepository
{
   /** Resource adapter templates */
   private ConcurrentMap<URL, Connector> raTemplates;

   /**
    * Constructor
    */
   public SimpleMetadataRepository()
   {
      this.raTemplates = new ConcurrentHashMap<URL, Connector>();
   }

   /**
    * {@inheritDoc}
    */
   public void registerResourceAdapter(URL deployment, Connector md) throws AlreadyExistsException
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (md == null)
         throw new IllegalArgumentException("Metadata is null");

      if (raTemplates.containsKey(deployment))
         throw new AlreadyExistsException(deployment + " already registered");

      raTemplates.put(deployment, md);
   }

   /**
    * {@inheritDoc}
    */
   public void unregisterResourceAdapter(URL deployment) throws NotFoundException
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (!raTemplates.containsKey(deployment))
         throw new NotFoundException(deployment + " isn't registered");

      raTemplates.remove(deployment);
   }

   /**
    * {@inheritDoc}
    */
   public Connector getResourceAdapter(URL deployment) throws NotFoundException
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (!raTemplates.containsKey(deployment))
         throw new NotFoundException(deployment + " isn't registered");

      Connector md = raTemplates.get(deployment);

      // Always return a copy as the caller may make changes to it
      return (Connector)md.copy();
   }

   /**
    * {@inheritDoc}
    */
   public Set<URL> getResourceAdapters()
   {
      return Collections.unmodifiableSet(raTemplates.keySet());
   }
}
