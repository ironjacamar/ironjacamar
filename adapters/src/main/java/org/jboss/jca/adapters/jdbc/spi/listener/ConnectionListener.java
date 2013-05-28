/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.spi.listener;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Define the SPI contract for a connection listener plugin for the
 * JDBC resource adapter.
 *
 * The implementing class must have a default constructor.
 *
 * The implementing class must be thread safe.
 *
 * Java bean properties can be set, using the supported types
 * by the Java EE Connector Architecture specification.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface ConnectionListener
{
   /**
    * Initialize
    * @param cl The class loader which can be used for initialization
    * @exception SQLException Thrown in case of an error
    */
   public void initialize(ClassLoader cl) throws SQLException;

   /**
    * Connection activated
    * @param c The connection
    * @exception SQLException Thrown in case of an error
    */
   public void activated(Connection c) throws SQLException;

   /**
    * Connection passivated
    * @param c The connection
    * @exception SQLException Thrown in case of an error
    */
   public void passivated(Connection c) throws SQLException;
}
