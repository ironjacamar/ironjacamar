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

package org.ironjacamar.core.connectionmanager.pool;

import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Synchronization;

/**
 * Cleanup the transaction map
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class TransactionMapCleanup implements Synchronization
{
   /** The key */
   private Object key;

   /** The map */
   private ConcurrentHashMap<Object, Map<ManagedConnectionPool, ConnectionListener>> transactionMap;

   /**
    * Constructor
    * @param key The key
    * @param transactionMap The transaction map
    */
   public TransactionMapCleanup(Object key,
                                ConcurrentHashMap<Object, Map<ManagedConnectionPool,
                                ConnectionListener>> transactionMap)
   {
      this.key = key;
      this.transactionMap = transactionMap;
   }

   /**
    * {@inheritDoc}
    */
   public void beforeCompletion()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void afterCompletion(int status)
   {
      transactionMap.remove(key);
   }
}
