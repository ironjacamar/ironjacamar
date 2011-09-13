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
package org.jboss.jca.core.api.connectionmanager.ccm;

import org.jboss.jca.core.api.connectionmanager.listener.ConnectionCacheListener;
import org.jboss.jca.core.api.connectionmanager.listener.ConnectionListener;
import org.jboss.jca.core.spi.connectionmanager.ComponentStack;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionListener;

import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.TransactionManager;

/**
 * CacheConnectionManager.
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface CachedConnectionManager extends UserTransactionListener, ComponentStack
{
   /**
    * Gets transaction manager.
    * @return transaction manager
    */
   public TransactionManager getTransactionManager();

   /**
    * Set debug flag
    * @param v The value
    */
   public void setDebug(boolean v);

   /**
    * Set error flag
    * @param v The value
    */
   public void setError(boolean v);

   /**
    * Register connection.
    * @param cm connection manager
    * @param cl connection listener
    * @param connection connection handle
    * @param cri connection request info.
    */
   public void registerConnection(ConnectionCacheListener cm, ConnectionListener cl,
                                  Object connection, ConnectionRequestInfo cri);

   /**
    * Unregister connection.
    * @param cm connection manager
    * @param connection connection handle
    */
   public void unregisterConnection(ConnectionCacheListener cm, Object connection);

   /**
    * Start
    */
   public void start();

   /**
    * Stop
    */
   public void stop();
}
