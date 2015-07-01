/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.jdk8;

import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnection;
import org.jboss.jca.adapters.jdbc.WrappedCallableStatement;
import org.jboss.jca.adapters.jdbc.WrappedConnection;
import org.jboss.jca.adapters.jdbc.WrappedPreparedStatement;
import org.jboss.jca.adapters.jdbc.WrappedStatement;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * WrappedConnectionJDK8.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@SuppressWarnings("unchecked")
public class WrappedConnectionJDK8 extends WrappedConnection
{
   private static final long serialVersionUID = 1L;

   /**
    * Create a new WrappedConnectionJDK8.
    * 
    * @param mc the managed connection
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    */
   public WrappedConnectionJDK8(BaseWrapperManagedConnection mc, boolean spy, String jndiName, boolean doLocking)
   {
      super(mc, spy, jndiName, doLocking);
   }

   /**
    * Wrap statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The result
    */
   protected WrappedStatement wrapStatement(Statement statement, boolean spy, String jndiName, boolean doLocking)
   {
      return new WrappedStatementJDK8(this, statement, spy, jndiName, doLocking);
   }

   /**
    * Wrap prepared statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The result
    */
   protected WrappedPreparedStatement wrapPreparedStatement(PreparedStatement statement, boolean spy, String jndiName,
                                                            boolean doLocking)
   {
      return new WrappedPreparedStatementJDK8(this, statement, spy, jndiName, doLocking);
   }

   /**
    * Wrap callable statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @param doLocking Do locking
    * @return The result
    */
   protected WrappedCallableStatement wrapCallableStatement(CallableStatement statement, boolean spy, String jndiName,
                                                            boolean doLocking)
   {
      return new WrappedCallableStatementJDK8(this, statement, spy, jndiName, doLocking);
   }
}
