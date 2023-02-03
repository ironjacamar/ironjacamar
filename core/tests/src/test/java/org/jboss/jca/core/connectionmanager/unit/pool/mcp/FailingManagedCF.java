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

import java.util.Set;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import javax.security.auth.Subject;

import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;

final class FailingManagedCF extends SimpleManagedConnectionFactory {
	/**
	 *
	 */
	private boolean fail;

	@Override
	public Set getInvalidConnections(Set connectionSet) throws ResourceException {
		ManagedConnection mc = (ManagedConnection) connectionSet.iterator().next();
		SimpleConnection sc = (SimpleConnection) mc.getConnection(null, null);
		sc.fail();
		return connectionSet;
	}

	@Override
	public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
			throws ResourceException {
		if (this.fail) {
		  throw new ResourceException();
		} else {
		  ManagedConnection mc = super.createManagedConnection(subject, cxRequestInfo);
		  return mc;
		}
	}

	void setFailing(boolean fail) {
		this.fail = fail;
	}
}
