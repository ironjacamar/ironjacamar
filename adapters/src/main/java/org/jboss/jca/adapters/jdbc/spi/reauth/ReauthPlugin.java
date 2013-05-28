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

package org.jboss.jca.adapters.jdbc.spi.reauth;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Define the SPI contract for a reauthentication plugin for the
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
public interface ReauthPlugin
{
   /**
    * Initialize
    * @param cl The class loader which can be used for initialization
    * @exception SQLException Thrown in case of an error
    */
   public void initialize(ClassLoader cl) throws SQLException;

   /**
    * Reauthenticate
    * @param c The connection
    * @param userName The user name
    * @param password The password
    * @exception SQLException Thrown in case of an error
    */
   public void reauthenticate(Connection c, String userName, String password) throws SQLException;
}
