/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.pool.strategy;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.AbstractPool;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * Pool implementation that uses subject and connection
 * request info for its pool partition.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class PoolBySubjectAndCri extends AbstractPool
{
   /** The logger */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, PoolBySubjectAndCri.class.getName());
   
   /**
    * Creates a new instance.
    * @param mcf managed connection factory
    * @param pc pool configuration
    * @param noTxSeparatePools notx seperate pool
    * @param sharable Are the connections sharable
    */
   public PoolBySubjectAndCri(final ManagedConnectionFactory mcf, final PoolConfiguration pc,
                              final boolean noTxSeparatePools, final boolean sharable)
   {
      super(mcf, pc, noTxSeparatePools, sharable);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Object getKey(Subject subject, ConnectionRequestInfo cri, boolean separateNoTx) throws ResourceException
   {
      return new SubjectCriKey(subject, cri, separateNoTx);
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection()
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public boolean testConnection(ConnectionRequestInfo cri, Subject subject)
   {
      return internalTestConnection(cri, subject);
   }

   /**
    * {@inheritDoc}
    */
   public CoreLogger getLogger()
   {
      return log;
   }
}
