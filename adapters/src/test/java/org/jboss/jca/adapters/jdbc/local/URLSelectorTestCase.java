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
package org.jboss.jca.adapters.jdbc.local;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.jca.adapters.jdbc.jdk8.WrappedConnectionJDK8;
import org.jboss.jca.adapters.jdbc.local.testimpl.MockConnection;
import org.jboss.jca.adapters.jdbc.local.testimpl.MockDriver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class URLSelectorTestCase {
	private Subject s;
	
	private LocalManagedConnectionFactory conFac;
	
	@Before
	public void setUpConnectionFactory() {
		conFac = new LocalManagedConnectionFactory();
		conFac.setConnectionURL("url1|url2|url3");
		conFac.setURLDelimiter("|");
		conFac.setDriverClass(MockDriver.class.getName());
		conFac.setUserName("test");
		conFac.setPassword("test");
		s = new Subject();
		PasswordCredential pc = new PasswordCredential("test", "test".toCharArray());
		pc.setManagedConnectionFactory(conFac);
		s.getPrivateCredentials().add(pc);
		
		MockConnection.reset();
	}
	
	@Test
	public void testWrapsAroundEndOfUrlList() throws Exception {
		MockConnection.fail("url1");
		MockConnection con = unwrap(createConnection(conFac));
		Assert.assertEquals("url2", con.getUrl());
		
		MockConnection.reset();
		
		MockConnection.fail("url2");
		MockConnection.fail("url3");
		con = unwrap(createConnection(conFac));
		Assert.assertEquals("url1", con.getUrl());
	}
	
	@Test
	public void testStartsIteratingAtLastUsedConnection() throws Exception {
		MockConnection.fail("url1");
		MockConnection con = unwrap(createConnection(conFac));
		Assert.assertEquals("url2", con.getUrl());
		
		MockConnection.reset();
		MockConnection.fail("url2");
		con = unwrap(createConnection(conFac));
		Assert.assertEquals("url3", con.getUrl());
	}
	
	@Test(expected=ResourceException.class)
	public void testFailesIfAllOptionsFail() throws Exception {
		MockConnection.fail("url1");
		MockConnection.fail("url2");
		MockConnection.fail("url3");
		unwrap(createConnection(conFac));
	}
	
	private MockConnection unwrap(LocalManagedConnection c) throws Exception {
		return (MockConnection)((WrappedConnectionJDK8)c.getConnection()).getUnderlyingConnection();
	}
	
	private LocalManagedConnection createConnection(LocalManagedConnectionFactory conFac) throws Exception {
		return (LocalManagedConnection) conFac.createManagedConnection(s, null);
	}
}
