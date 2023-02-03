/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2018, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.connectionmanager.unit.pool.mcp;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ManagedConnection;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.common.MockConnectionManager;
import org.jboss.jca.core.connectionmanager.listener.AbstractConnectionListener;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;

public class TestConnectionManager extends MockConnectionManager {

	private final class TestConnectionListener extends AbstractConnectionListener {
		private TestConnectionListener(ConnectionManager cm, ManagedConnection managedConnection, Pool pool,
				ManagedConnectionPool mcp, FlushStrategy flushStrategy, Boolean tracking) {
			super(cm, managedConnection, pool, mcp, flushStrategy, tracking);
			managedConnection.addConnectionEventListener(this);
		}

		@Override
		public void connectionErrorOccurred(ConnectionEvent event) {
			try {
				pool.returnConnection(this, true);
			} catch (ResourceException e) {
				e.printStackTrace();
			}
			super.connectionErrorOccurred(event);
		}

		@Override
		protected CoreLogger getLogger() {
			return pool.getLogger();
		}
	}

	private Pool pool;

	public TestConnectionManager(Pool p) {
		this.pool = p;
	}

	@Override
	public org.jboss.jca.core.connectionmanager.listener.ConnectionListener createConnectionListener(
			ManagedConnection managedConnection, ManagedConnectionPool mcp) throws ResourceException {
		return new TestConnectionListener(null, managedConnection, pool, mcp, null, false);
	}
}
