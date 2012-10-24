/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.test.unit.connector15;

import org.jboss.jca.common.metadata.ra.ra15.Connector15Impl;
import org.jboss.jca.deployers.test.DeploymentTestBase;
import org.jboss.jca.deployers.test.rars.inout.SimpleAdminObject;
import org.jboss.jca.deployers.test.rars.inout.SimpleConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleManagedConnectionFactory;
import org.jboss.jca.deployers.test.rars.inout.SimpleResourceAdapter;

import static org.junit.Assert.*;

/**
 * 
 * A ActivationTestCase.
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */

public abstract class ActivationTestBase extends DeploymentTestBase
{
   /**
    * 
    * checks, if metadata is of appropriate type
    * 
    * @param piId - metadata name
    * @throws Exception in case of error
    */
   @Override
   public void checkMetadata(String piId) throws Exception
   {
      assertTrue(mdr.getResourceAdapter(piId) instanceof Connector15Impl);
   }

   /**
    * 
    * tests properties of all objects after deployment
    * 
    * @param cf connection factory
    * @param cf1 cf 1st property
    * @param cf2 cf 2nd property
    * @param ra1 ResourceAdapter 1st property
    * @param ra2 ResourceAdapter 2nd property
    * @param ao AdminObject
    * @param ao1 ao 1st property
    * @param ao2 ao 2nd property
    * @throws Exception in case of error
    */
   public void allObjectsTest(SimpleConnectionFactory cf, String cf1, char cf2, String ra1, boolean ra2,
      SimpleAdminObject ao, String ao1, double ao2) throws Exception
   {
      SimpleManagedConnectionFactory mcf = testSimpleCF(cf, cf1, cf2);
      testSimpleRA((SimpleResourceAdapter) mcf.getResourceAdapter(), ra1, ra2);
      testSimpleAO(ao, ao1, ao2);
   }
}
