/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.api.connectionmanager.pool;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * The pool
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Pool
{
   /**
    * Get the type name
    * @return The value
    */
   public String getType();

   /**
    * Get the statistics
    * @return The value
    */
   public PoolStatistics getStatistics();

   /**
    * Get the janitor
    * @return The value
    */
   public Janitor getJanitor();

   /**
    * Prefill the connection pool
    */
   public void prefill();

   /**
    * Flush idle connections from the pool
    */
   public void flush();

   /**
    * Flush the pool
    * @param mode The flush mode
    */
   public void flush(FlushMode mode);

   /**
    * Test if a connection can be obtained
    * @return True if it was possible to get a connection; otherwise false
    */
   public boolean testConnection();

   /**
    * Test if a connection can be obtained
    * @param cri Optional connection request info object
    * @param subject Optional subject
    * @return True if it was possible to get a connection; otherwise false
    */
   public boolean testConnection(ConnectionRequestInfo cri, Subject subject);
}
