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

/**
 * UserTransactionProvider implementation
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class UserTransactionProviderImpl implements org.jboss.jca.core.spi.transaction.usertx.UserTransactionProvider,
                                                    org.jboss.tm.usertx.UserTransactionProvider
{
   /** The user transaction provider */
   private org.jboss.jca.core.spi.transaction.usertx.UserTransactionProvider utp;

   /**
    * Constructor
    * @param utp The user transaction provider
    */
   public UserTransactionProviderImpl(org.jboss.jca.core.spi.transaction.usertx.UserTransactionProvider utp)
   {
      this.utp = utp;
   }

   /**
    * Set the user transaction registry
    * @param v The value
    */
   public void setUserTransactionRegistry(org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry v)
   {
      utp.setUserTransactionRegistry(v);
   }

   /**
    * Set the user transaction registry
    * @param v The value
    */
   public void setTransactionRegistry(org.jboss.tm.usertx.UserTransactionRegistry v)
   {
      ((org.jboss.tm.usertx.UserTransactionProvider)utp).setTransactionRegistry(v);
   }
}
