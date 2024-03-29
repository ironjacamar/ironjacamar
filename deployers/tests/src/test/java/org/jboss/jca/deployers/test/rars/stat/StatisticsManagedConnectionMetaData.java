/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.test.rars.stat;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnectionMetaData;

import org.jboss.logging.Logger;

/**
 * StatisticsManagedConnectionMetaData
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class StatisticsManagedConnectionMetaData implements ManagedConnectionMetaData
{
   /** The logger */
   private static Logger log = Logger.getLogger(StatisticsManagedConnectionMetaData.class.getName());

   /**
    * Default constructor
    */
   public StatisticsManagedConnectionMetaData()
   {

   }

   /**
    * Returns Product name of the underlying EIS instance connected through the ManagedConnection.
    *
    * @return Product name of the EIS instance
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getEISProductName() throws ResourceException
   {
      log.trace("getEISProductName()");
      return null; //TODO
   }

   /**
    * Returns Product version of the underlying EIS instance connected through the ManagedConnection.
    *
    * @return Product version of the EIS instance
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getEISProductVersion() throws ResourceException
   {
      log.trace("getEISProductVersion()");
      return null; //TODO
   }

   /**
    * Returns maximum limit on number of active concurrent connections 
    *
    * @return Maximum limit for number of active concurrent connections
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public int getMaxConnections() throws ResourceException
   {
      log.trace("getMaxConnections()");
      return 0; //TODO
   }

   /**
    * Returns name of the user associated with the ManagedConnection instance
    *
    * @return Name of the user
    * @throws ResourceException Thrown if an error occurs
    */
   @Override
   public String getUserName() throws ResourceException
   {
      log.trace("getUserName()");
      return null; //TODO
   }


}
