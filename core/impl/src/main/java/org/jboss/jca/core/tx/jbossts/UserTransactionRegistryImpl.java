/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.spi.transaction.usertx.UserTransactionListener;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * UserTransactionRegistry implementation
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class UserTransactionRegistryImpl implements org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry
{
   /** The delegator */
   private org.jboss.tm.usertx.UserTransactionRegistry delegator;

   /** Listener map */
   private Map<UserTransactionListener, UserTransactionListenerImpl> listeners;

   /** Provider map */
   private Map<UserTransactionProvider, UserTransactionProviderImpl> providers;

   /**
    * Constructor
    * @param delegator The delegator instance
    */
   public UserTransactionRegistryImpl(org.jboss.tm.usertx.UserTransactionRegistry delegator)
   {
      this.delegator = delegator;
      this.listeners =
         Collections.synchronizedMap(new HashMap<UserTransactionListener, UserTransactionListenerImpl>());
      this.providers =
         Collections.synchronizedMap(new HashMap<UserTransactionProvider, UserTransactionProviderImpl>());
   }

   /**
    * Add a listener
    * @param listener The listener
    */
   public void addListener(UserTransactionListener listener)
   {
      UserTransactionListenerImpl impl = new UserTransactionListenerImpl(listener);

      delegator.addListener(impl);
      listeners.put(listener, impl);
   }
   
   /**
    * Remove a listener
    * @param listener The listener
    */
   public void removeListener(UserTransactionListener listener)
   {
      UserTransactionListenerImpl impl = listeners.get(listener);

      if (impl != null)
      {
         delegator.removeListener(impl);
         listeners.remove(listener);
      }
   }

   /**
    * Add a provider
    * @param provider The provider
    */
   public void addProvider(UserTransactionProvider provider)
   {
      UserTransactionProviderImpl impl = new UserTransactionProviderImpl(provider);

      delegator.addProvider(impl);
      providers.put(provider, impl);
   }
   
   /**
    * Remove a provider
    * @param provider The provider
    */
   public void removeProvider(UserTransactionProvider provider)
   {
      UserTransactionProviderImpl impl = providers.get(provider);

      if (impl != null)
      {
         delegator.removeProvider(impl);
         providers.remove(provider);
      }
   }

   /**
    * Fire a user transaction started event
    */
   public void userTransactionStarted()
   {
      delegator.userTransactionStarted();
   }
}
