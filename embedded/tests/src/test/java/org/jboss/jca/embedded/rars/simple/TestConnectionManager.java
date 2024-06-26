/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.embedded.rars.simple;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;

/**
 * TestConnectionManager
 *
 * @author  <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class TestConnectionManager implements ConnectionManager
{
   private static final long serialVersionUID = 1L;
   private static Logger log = Logger.getLogger(TestConnectionManager.class);

   /**
    * Constructor
    */
   public TestConnectionManager()
   {
   }

   /**
    * Allocate connection
    * @param mcf The managed connection factory
    * @param cri The connection request information
    * @return The connection
    * @exception ResourceException Thrown if an error occurs
    */
   public Object allocateConnection(ManagedConnectionFactory mcf,
                                    ConnectionRequestInfo cri)
      throws ResourceException
   {
      return null;
   }

   /**
    * Hash code
    * @return The hash
    */
   @Override
   public int hashCode()
   {
      return 42;
   }

   /**
    * Equals
    * @param other The other object
    * @return True if equal; otherwise false
    */
   public boolean equals(Object other)
   {
      if (other == null)
         return false;

      return getClass().equals(other.getClass());
   }
}
