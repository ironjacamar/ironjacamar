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
package org.jboss.jca.core.spi.recovery;

import javax.resource.ResourceException;

/**
 * Defines the contract for an XA recovery plugin.
 *
 * An implementation of this SPI can provide feedback to the JCA container
 * if the physinal connection is still valid to use for getting recovery information
 * from.
 *
 * An implementation of this SPI must have a default constructor and will have
 * its Java bean properties set after initialization.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface RecoveryPlugin
{
   /**
    * Check if the passed connection object instance is still valid, and hence
    * the underlying physical connection
    *
    * @param c The connection instance
    * @return <code>True</code> if the connection is still valid, otherwise <code>false</code>
    * @exception ResourceException Thrown in case of an error
    */
   public boolean isValid(Object c) throws ResourceException;

   /**
    * Perform a close operation on the passed connection object instance - like
    * a CCI Connection instance.
    *
    * Any error during this operation should result in an exception, which
    * will force a close of the physical connection to the Enterprise Information System
    *
    * @param c The connection instance
    * @exception ResourceException Thrown in case of an error
    * @see javax.resource.cci.Connection
    */
   public void close(Object c) throws ResourceException;
}
