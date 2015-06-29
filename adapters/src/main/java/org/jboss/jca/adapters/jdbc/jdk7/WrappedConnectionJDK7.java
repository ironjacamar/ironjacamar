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

package org.jboss.jca.adapters.jdbc.jdk7;

import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnection;
import org.jboss.jca.adapters.jdbc.WrappedCallableStatement;
import org.jboss.jca.adapters.jdbc.WrappedConnection;
import org.jboss.jca.adapters.jdbc.WrappedPreparedStatement;
import org.jboss.jca.adapters.jdbc.WrappedStatement;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * WrappedConnectionJDK7.
 * 
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@SuppressWarnings("unchecked")
public class WrappedConnectionJDK7 extends WrappedConnection
{
   private static final long serialVersionUID = 1L;

   /**
    * Create a new WrappedConnectionJDK7.
    * 
    * @param mc the managed connection
    * @param spy The spy value
    * @param jndiName The jndi name
    */
   public WrappedConnectionJDK7(BaseWrapperManagedConnection mc, boolean spy, String jndiName)
   {
      super(mc, spy, jndiName);
   }

   /**
    * Wrap statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @return The result
    */
   protected WrappedStatement wrapStatement(Statement statement, boolean spy, String jndiName)
   {
      return new WrappedStatementJDK7(this, statement, spy, jndiName);
   }

   /**
    * Wrap prepared statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @return The result
    */
   protected WrappedPreparedStatement wrapPreparedStatement(PreparedStatement statement, boolean spy, String jndiName)
   {
      return new WrappedPreparedStatementJDK7(this, statement, spy, jndiName);
   }

   /**
    * Wrap callable statement
    * @param statement The statement
    * @param spy The spy value
    * @param jndiName The jndi name
    * @return The result
    */
   protected WrappedCallableStatement wrapCallableStatement(CallableStatement statement, boolean spy, String jndiName)
   {
      return new WrappedCallableStatementJDK7(this, statement, spy, jndiName);
   }
}
