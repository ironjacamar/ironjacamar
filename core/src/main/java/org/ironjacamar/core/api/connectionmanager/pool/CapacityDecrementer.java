/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.core.api.connectionmanager.pool;

import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

/**
 * The capacity decrementer policy
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface CapacityDecrementer
{
   /**
    * Should the connection listener be destroyed
    * @param cl The connection listener
    * @param timeout The timeout watermark
    * @param currentSize The current pool size
    * @param minPoolSize The minimum pool size
    * @param destroyed The number of connection listeners destroyed during this call cycle
    * @return <code>True</code> if the connection listener should be destroyed; otherwise <code>false</code>
    */
   public boolean shouldDestroy(ConnectionListener cl, long timeout, int currentSize, int minPoolSize, int destroyed);
}
