/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.jca.core.api.MetaDataRepository;

import org.jboss.logging.Logger;
import org.jboss.metadata.plugins.loader.memory.MemoryMetaDataLoader;
import org.jboss.metadata.plugins.repository.basic.BasicMetaDataRepository;
import org.jboss.metadata.rar.jboss.RARDeploymentMetaData;
import org.jboss.metadata.rar.jboss.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.metadata.spi.loader.MutableMetaDataLoader;
import org.jboss.metadata.spi.repository.MutableMetaDataRepository;
import org.jboss.metadata.spi.retrieval.MetaDataRetrieval;
import org.jboss.metadata.spi.retrieval.MetaDataRetrievalToMetaDataBridge;
import org.jboss.metadata.spi.scope.CommonLevels;
import org.jboss.metadata.spi.scope.ScopeKey;

/**
 * MetaDataRepository implementation backed by MDR
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class MetaDataRepositoryImpl implements MetaDataRepository
{
   /** The logger */
   private static Logger log = Logger.getLogger(MetaDataRepositoryImpl.class);

   /** The backing MDR */
   private MutableMetaDataRepository mdr;

   /**
    * Constructor
    */
   public MetaDataRepositoryImpl()
   {
      mdr = new BasicMetaDataRepository();
   }

   /**
    * Add RAR deployment metadata
    * @param name The name
    * @param cmd The RAR deployment metadata
    * @return The previous value
    */
   public RARDeploymentMetaData addRARDeploymentMetaData(String name, RARDeploymentMetaData cmd)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      if (cmd == null)
         throw new IllegalArgumentException("Null cmd");

      ScopeKey scopeKey = new ScopeKey(CommonLevels.JVM, name);
      MutableMetaDataLoader loader = new MemoryMetaDataLoader(scopeKey);
      loader.addMetaData(cmd, RARDeploymentMetaData.class);

      MetaDataRetrieval r = mdr.addMetaDataRetrieval(loader);

      if (r != null)
      {
         MetaDataRetrievalToMetaDataBridge b = new MetaDataRetrievalToMetaDataBridge(r);
         return b.getMetaData(RARDeploymentMetaData.class);
      }

      return null;
   }

   /**
    * Get RAR deployment metadata
    * @param name The name
    * @return The RAR deployment metadata
    */
   public RARDeploymentMetaData getRARDeploymentMetaData(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      ScopeKey scopeKey = new ScopeKey(CommonLevels.JVM, name);
      MetaDataRetrieval r = mdr.getMetaDataRetrieval(scopeKey);

      if (r != null)
      {
         MetaDataRetrievalToMetaDataBridge b = new MetaDataRetrievalToMetaDataBridge(r);
         return b.getMetaData(RARDeploymentMetaData.class);
      }

      return null;
   }

   /**
    * Remove RAR deployment metadata
    * @param name The name
    * @return True if the metadata was removed; otherwise false
    */
   public boolean removeRARDeploymentMetaData(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      ScopeKey scopeKey = new ScopeKey(CommonLevels.JVM, name);
      MetaDataRetrieval mr = mdr.removeMetaDataRetrieval(scopeKey);

      return mr != null;
   }

   /**
    * Add managed connecton metadata
    * @param name The name
    * @param group The managed connection metadata
    * @return The previous value
    */
   public ManagedConnectionFactoryDeploymentGroup 
   addManagedConnectionFactoryDeploymentGroup(String name, ManagedConnectionFactoryDeploymentGroup group)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      if (group == null)
         throw new IllegalArgumentException("Null group");

      ScopeKey scopeKey = new ScopeKey(CommonLevels.JVM, name);
      MutableMetaDataLoader loader = new MemoryMetaDataLoader(scopeKey);
      loader.addMetaData(group, ManagedConnectionFactoryDeploymentGroup.class);

      MetaDataRetrieval r = mdr.addMetaDataRetrieval(loader);

      if (r != null)
      {
         MetaDataRetrievalToMetaDataBridge b = new MetaDataRetrievalToMetaDataBridge(r);
         return b.getMetaData(ManagedConnectionFactoryDeploymentGroup.class);
      }

      return null;
   }

   /**
    * Get managed connection metadata
    * @param name The name
    * @return The managed connection metadata
    */
   public ManagedConnectionFactoryDeploymentGroup getManagedConnectionFactoryDeploymentGroup(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      ScopeKey scopeKey = new ScopeKey(CommonLevels.JVM, name);
      MetaDataRetrieval r = mdr.getMetaDataRetrieval(scopeKey);

      if (r != null)
      {
         MetaDataRetrievalToMetaDataBridge b = new MetaDataRetrievalToMetaDataBridge(r);
         return b.getMetaData(ManagedConnectionFactoryDeploymentGroup.class);
      }

      return null;
   }

   /**
    * Remove managed connection metadata
    * @param name The name
    * @return True if the metadata was added; otherwise false
    */
   public boolean removeManagedConnectionFactoryDeploymentGroup(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      ScopeKey scopeKey = new ScopeKey(CommonLevels.JVM, name);
      MetaDataRetrieval mr = mdr.removeMetaDataRetrieval(scopeKey);

      return mr != null;
   }
}
