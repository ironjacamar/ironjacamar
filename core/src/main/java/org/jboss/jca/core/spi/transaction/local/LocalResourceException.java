/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.spi.transaction.local;

import javax.resource.ResourceException;

/**
 * LocalResourceException.
 * <p/>
 * Throwing this exception from your <code>LocalTransaction.commit()</code> or
 * <code>LocalTransaction.rollback()</code> methods will result in a
 * <code>XAException.XAER_RMFAIL</code> error code being sent to the transaction manager
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalResourceException extends ResourceException
{
   /** Serial version UID */
   private static final long serialVersionUID = 1L;
   
   /**
    * Creates a new instance.
    * @param message message
    */   
   public LocalResourceException(String message)
   {
      this(message, null);
   }

   /**
    * Creates a new instance.
    * @param message message
    * @param t cause
    */   
   public LocalResourceException(String message, Throwable t)
   {
      super(message, t);
   }
}
