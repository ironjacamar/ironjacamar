/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars.ra16;

import org.jboss.jca.test.deployers.spec.rars.BaseResourceAdapter;

import javax.resource.spi.AuthenticationMechanism;
import javax.resource.spi.AuthenticationMechanism.CredentialInterface;
import javax.resource.spi.Connector;
import javax.resource.spi.TransactionSupport;

/**
 * TestResourceAdapter
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@Connector(
      description = { "Test RA" },
      displayName = { "displayName" },
      smallIcon = { "smallIcon" },
      largeIcon = { "largeIcon" },
      vendorName = "Red Hat Middleware LLC",
      eisType = "Test RA",
      version = "0.1",
      licenseDescription = { "licenseDescription" },
      licenseRequired = true,
      authMechanisms = { @AuthenticationMechanism(credentialInterface = CredentialInterface.GSSCredential) },
      transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction)
public class TestResourceAdapter extends BaseResourceAdapter
{
}
