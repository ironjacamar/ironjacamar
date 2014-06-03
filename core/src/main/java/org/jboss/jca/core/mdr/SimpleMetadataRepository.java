/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.logging.Messages;

/**
 * A simple implementation of the metadata repository
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class SimpleMetadataRepository implements MetadataRepository
{
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   /** Resource adapter templates */
   private ConcurrentMap<String, Connector> raTemplates;

   /** Resource adapter roots */
   private ConcurrentMap<String, File> raRoots;

   /** Activation metadata */
   private Map<String, Activation> activations;

   /** JNDI mappings */
   private ConcurrentMap<String, Map<String, List<String>>> jndiMappings;

   /**
    * Constructor
    */
   public SimpleMetadataRepository()
   {
      this.raTemplates = new ConcurrentHashMap<String, Connector>();
      this.raRoots = new ConcurrentHashMap<String, File>();
      this.activations = Collections.synchronizedMap(new HashMap<String, Activation>());
      this.jndiMappings = new ConcurrentHashMap<String, Map<String, List<String>>>();
   }

   /**
    * {@inheritDoc}
    */
   public void registerResourceAdapter(String uniqueId, File root, Connector md, Activation a)
      throws AlreadyExistsException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (root == null)
         throw new IllegalArgumentException("Root is null");

      if (md == null)
         throw new IllegalArgumentException("Connector is null");

      // The Activation metadata object can be null

      if (raTemplates.containsKey(uniqueId))
         throw new AlreadyExistsException(bundle.keyNotRegistered(uniqueId));

      raTemplates.put(uniqueId, md);
      raRoots.put(uniqueId, root);
      activations.put(uniqueId, a);
   }

   /**
    * {@inheritDoc}
    */
   public void unregisterResourceAdapter(String uniqueId) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!raTemplates.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      raTemplates.remove(uniqueId);
      raRoots.remove(uniqueId);
      activations.remove(uniqueId);
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasResourceAdapter(String uniqueId)
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");
      
      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      return raTemplates.containsKey(uniqueId);
   }

   /**
    * {@inheritDoc}
    */
   public Connector getResourceAdapter(String uniqueId) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!raTemplates.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      Connector md = raTemplates.get(uniqueId);

      // Always return a copy as the caller may make changes to it
      return (Connector)md.copy();
   }

   /**
    * {@inheritDoc}
    */
   public Set<String> getResourceAdapters()
   {
      return Collections.unmodifiableSet(raTemplates.keySet());
   }

   /**
    * {@inheritDoc}
    */
   public File getRoot(String uniqueId) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!raRoots.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      return raRoots.get(uniqueId);
   }

   /**
    * {@inheritDoc}
    */
   public Activation getActivation(String uniqueId) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!activations.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      return activations.get(uniqueId);
   }

   /**
    * {@inheritDoc}
    */
   public void registerJndiMapping(String uniqueId, String clz, String jndi)
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (clz == null)
         throw new IllegalArgumentException("Clz is null");

      if (clz.trim().equals(""))
         throw new IllegalArgumentException("Clz is empty");

      if (jndi == null)
         throw new IllegalArgumentException("Jndi is null");

      if (jndi.trim().equals(""))
         throw new IllegalArgumentException("Jndi is empty");

      Map<String, List<String>> mappings = jndiMappings.get(uniqueId);
      if (mappings == null)
      {
         Map<String, List<String>> newMappings = Collections.synchronizedMap(new HashMap<String, List<String>>(1));
         mappings = jndiMappings.putIfAbsent(uniqueId, newMappings);

         if (mappings == null)
         {
            mappings = newMappings;
         }
      }
      
      List<String> l = mappings.get(clz);

      if (l == null)
         l = Collections.synchronizedList(new ArrayList<String>(1));

      l.add(jndi);
      mappings.put(clz, l);
   }

   /**
    * {@inheritDoc}
    */
   public void unregisterJndiMapping(String uniqueId, String clz, String jndi) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");

      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("Uniqueid is empty");

      if (clz == null)
         throw new IllegalArgumentException("Clz is null");

      if (clz.trim().equals(""))
         throw new IllegalArgumentException("Clz is empty");

      if (jndi == null)
         throw new IllegalArgumentException("Jndi is null");

      if (jndi.trim().equals(""))
         throw new IllegalArgumentException("Jndi is empty");

      if (!jndiMappings.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      Map<String, List<String>> mappings = jndiMappings.get(uniqueId);

      if (mappings != null)
      {
         List<String> l = mappings.get(clz);

         if (l != null)
         {
            l.remove(jndi);

            if (l.size() == 0)
            {
               mappings.remove(clz);
            }
         }

         if (mappings.size() == 0)
         {
            jndiMappings.remove(uniqueId);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasJndiMappings(String uniqueId)
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");
      
      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      return jndiMappings.containsKey(uniqueId);
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, List<String>> getJndiMappings(String uniqueId) throws NotFoundException
   {
      if (uniqueId == null)
         throw new IllegalArgumentException("UniqueId is null");
      
      if (uniqueId.trim().equals(""))
         throw new IllegalArgumentException("UniqueId is empty");

      if (!jndiMappings.containsKey(uniqueId))
         throw new NotFoundException(bundle.keyNotRegistered(uniqueId));

      Map<String, List<String>> mappings = jndiMappings.get(uniqueId);

      if (mappings == null)
         return Collections.unmodifiableMap(new HashMap<String, List<String>>(0));

      return Collections.unmodifiableMap(mappings);
   }

   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("SimpleMetadataRepository@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[");
      sb.append(" raTemplates=").append(raTemplates);
      sb.append(" raRoots=").append(raRoots);
      sb.append(" activations=").append(activations);
      sb.append(" jndiMappings=").append(jndiMappings);
      sb.append("]");

      return sb.toString();
   }
}
