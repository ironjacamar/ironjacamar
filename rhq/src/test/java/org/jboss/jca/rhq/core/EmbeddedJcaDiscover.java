/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.rhq.core;

import org.jboss.jca.core.api.management.Connector;
import org.jboss.jca.core.api.management.ManagementRepository;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import org.jboss.logging.Logger;

/**
 * EmbeddedJcaDiscover
 * Discover implement by embedded JCA
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a> 
 */
public class EmbeddedJcaDiscover implements Discover
{
   /** log */
   private static final Logger logger = Logger.getLogger(EmbeddedJcaDiscover.class);
   
   /** instance of EmbeddedRHQ */
   private static EmbeddedJcaDiscover instance;
   
   /** embedJCA */
   private Embedded embedJCA;

   /** stopped */
   private boolean stopped = true;
   
   /** ManagementRepository */
   ManagementRepository mr = null;
   
   /** 
    * singleton getInstance
    * 
    * @return EmbeddedJCAContainer EmbeddedJCAContainer
    */
   public static synchronized EmbeddedJcaDiscover getInstance()
   {
      if (null == instance)
      {
         instance = new EmbeddedJcaDiscover();
      }
      return instance;
   }
   
   /** 
    * default constructor
    */
   private EmbeddedJcaDiscover()
   {
   }
   
   /** 
    * start jca container
    */
   public void start()
   {
      try
      {
         embedJCA = EmbeddedFactory.create(true);
         embedJCA.startup();
         logger.debug("embedded JCA container started");
         
         //embedJCA.deploy(EmbeddedJcaDiscover.class.getResource("h2-ds.xml"));
         //embedJCA.deploy(EmbeddedJcaDiscover.class.getResource("xa.rar"));
         
         stopped = false;
      }
      catch (Throwable e)
      {
         throw new IllegalStateException("Something wrong when starting Embedded JCA container", e);
      }

   }

   /** 
    * getManagementRepository
    * 
    * @return ManagementRepository 
    */
   @Override
   public synchronized ManagementRepository getManagementRepository()
   {
      if (mr != null)
         return mr;

      try
      {
         if (stopped)
         {
            start();
            if (stopped)
               return null;
         }
         mr = embedJCA.lookup("ManagementRepository", ManagementRepository.class);
         return mr;
      }
      catch (Throwable e)
      {
         throw new IllegalStateException("Can not get the beanManagementRepository", e);
      }
   }
   
   /**
    * stop the embedded jca container.
    */
   public void stop()
   {
      try
      {
         embedJCA.shutdown();
         stopped = true;
      }
      catch (Throwable e)
      {
         throw new IllegalStateException("Can not shutdown the Embedded JCA container", e);
      }
   }
   
   /**
    * getConnectorByUniqueId
    * 
    * @param rarUniqueId The RAR unique ID
    * @return the Connector associated with the RAR resource with the unique id.
    */
   public Connector getConnectorByUniqueId(String rarUniqueId)
   {
      if (null == rarUniqueId)
      {
         throw new IllegalArgumentException("rar unique id can not be null.");
      }
      ManagementRepository manRepo = getManagementRepository();
      for (Connector connector : manRepo.getConnectors())
      {
         if (rarUniqueId.equals(connector.getUniqueId()))
         {
            return connector;
         }
      }
      return null;
   }
}
