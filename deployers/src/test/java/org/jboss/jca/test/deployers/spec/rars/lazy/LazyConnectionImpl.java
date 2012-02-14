/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars.lazy;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;

import org.jboss.logging.Logger;

/**
 * LazyConnectionImpl
 *
 * @version $Revision: $
 */
public class LazyConnectionImpl implements LazyConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger(LazyConnectionImpl.class);

   /** Connection manager */
   private ConnectionManager cm;

   /** ManagedConnection */
   private LazyManagedConnection mc;

   /** ManagedConnectionFactory */
   private LazyManagedConnectionFactory mcf;

   /** ConnectionRequestInfo */
   private ConnectionRequestInfo cri;

   /**
    * Default constructor
    * @param mc LazyManagedConnection
    * @param mcf LazyManagedConnectionFactory
    * @param cm ConnectionManager
    * @param cri ConnectionRequestInfo
    */
   public LazyConnectionImpl(LazyManagedConnection mc, LazyManagedConnectionFactory mcf,
                             ConnectionManager cm, ConnectionRequestInfo cri)
   {
      this.mc = mc;
      this.mcf = mcf;
      this.cm = cm;
      this.cri = cri;
   }

   /**
    * Call isManagedConnectionSet
    * @return boolean
    */
   public boolean isManagedConnectionSet()
   {
      log.trace("isManagedConnectionSet()");

      return mc != null;
   }

   /**
    * Close managed connection
    */
   public void closeManagedConnection()
   {
      log.trace("closeManagedConnection()");
      
      if (mc != null)
      {
         try
         {
            if (cm instanceof org.jboss.jca.core.api.connectionmanager.ConnectionManager)
            {
               org.jboss.jca.core.api.connectionmanager.ConnectionManager jboss =
                  (org.jboss.jca.core.api.connectionmanager.ConnectionManager)cm;

               boolean result = jboss.dissociateManagedConnection(this, mc, mcf);
               log.trace("Result=" + result);

               mc = null;
            }
         }
         catch (Throwable t)
         {
            log.error("CloseManagedConnection", t);
         }
      }
   }

   /**
    * Associate
    */
   public void associate()
   {
      log.trace("associate()");
      if (cm instanceof LazyAssociatableConnectionManager)
      {
         try
         {
            LazyAssociatableConnectionManager lacm = (LazyAssociatableConnectionManager)cm;
            lacm.associateConnection(this, mcf, cri);
         }
         catch (Throwable t)
         {
            log.error("Associate", t);
         }
      }
   }

   /**
    * Close
    */
   public void close()
   {
      log.trace("close()");
      if (mc != null)
      {
         mc.closeHandle(this);
      }
      else
      {
         if (cm instanceof LazyAssociatableConnectionManager)
         {
            LazyAssociatableConnectionManager lacm = (LazyAssociatableConnectionManager)cm;
            lacm.inactiveConnectionClosed(this, mcf);
         }
      }
   }

   /**
    * Set the managed connection
    * @param mc The value
    */
   void setManagedConnection(LazyManagedConnection mc)
   {
      log.trace("setManagedConnection(" + mc + ")");
      this.mc = mc;
   }
}
