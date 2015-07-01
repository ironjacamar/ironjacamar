/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * WrappedConnectionFactory.
 * 
 * @author <a href="abrock@redhat.com">Adrian Brock</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface WrappedConnectionFactory
{
   /**
    * Create a wrapped connection
    * @param mc The managed connection
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The wrapped connection
    */
   WrappedConnection createWrappedConnection(BaseWrapperManagedConnection mc, boolean spy, String jndiName,
                                             boolean doLocking);

   /**
    * Create a cached prepared statement
    * @param ps The prepared statement
    * @return The cached prepared statement
    * @exception SQLException Thrown if an error occurs
    */
   CachedPreparedStatement createCachedPreparedStatement(PreparedStatement ps) throws SQLException;

   /**
    * Create a cached callable statement
    * @param cs The callable statement
    * @return The cached callable statement
    * @exception SQLException Thrown if an error occurs
    */
   CachedCallableStatement createCachedCallableStatement(CallableStatement cs) throws SQLException;
}
