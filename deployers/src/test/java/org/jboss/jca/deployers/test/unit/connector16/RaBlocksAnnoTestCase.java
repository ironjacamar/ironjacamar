/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.test.unit.connector16;

import org.jboss.jca.deployers.test.rars.anno.AnnoActivation;
import org.jboss.jca.deployers.test.rars.anno.AnnoActivationSpec;
import org.jboss.jca.deployers.test.rars.anno.AnnoAdminObject;
import org.jboss.jca.deployers.test.rars.anno.AnnoAdminObjectImpl;
import org.jboss.jca.deployers.test.rars.anno.AnnoConnection;
import org.jboss.jca.deployers.test.rars.anno.AnnoConnectionFactory;
import org.jboss.jca.deployers.test.rars.anno.AnnoConnectionFactoryImpl;
import org.jboss.jca.deployers.test.rars.anno.AnnoConnectionImpl;
import org.jboss.jca.deployers.test.rars.anno.AnnoManagedConnection;
import org.jboss.jca.deployers.test.rars.anno.AnnoManagedConnectionFactory;
import org.jboss.jca.deployers.test.rars.anno.AnnoManagedConnectionMetaData;
import org.jboss.jca.deployers.test.rars.anno.AnnoMessageListener;
import org.jboss.jca.deployers.test.rars.anno.AnnoMessageListener1;
import org.jboss.jca.deployers.test.rars.anno.AnnoResourceAdapter;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Multiple activation of annotated RA
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
@RunWith(Arquillian.class)
public class RaBlocksAnnoTestCase extends Activation16TestBase
{

   /**
    * Define the deployment
    *
    * @return The deployment archive
    * @throws Exception in case of error
    */
   @Deployment
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      ResourceAdapterArchive raa = buidShrinkwrapRa("ra16anno.rar", AnnoConnectionFactory.class,
         AnnoManagedConnectionFactory.class, AnnoConnectionImpl.class, AnnoConnection.class, AnnoResourceAdapter.class,
         AnnoAdminObjectImpl.class, AnnoAdminObject.class, AnnoManagedConnection.class,
         AnnoManagedConnectionMetaData.class, AnnoMessageListener.class, AnnoMessageListener1.class,
         AnnoActivationSpec.class, AnnoActivation.class, AnnoConnectionFactoryImpl.class);
      addRaXml(raa);
      return raa;
   }

   /** Resource */
   @Resource(mappedName = "java:/eis/ra16anno")
   private AnnoConnectionFactory connectionFactory1;

   /** Resource */
   @Resource(mappedName = "java:/eis/ao/ra16anno")
   private AnnoAdminObject adminObject;

   /**
    * Test getConnection
    *
    * @exception Throwable Thrown if case of an error
    */
   @Test
   @Ignore("JBJCA-984")
   public void testConnection1() throws Throwable
   {
      testConnection(connectionFactory1, (byte) 23, (short) 55);
   }

   /**
    * Test admin objects
   *
   * @exception Throwable Thrown if case of an error
   */
   @Test
   @Ignore("JBJCA-984")
   public void testAdminOjbect() throws Throwable
   {
      testAdminOjbect(adminObject, 54321, true);
   }

   /**
    * test activation 1
    * 
    * @exception Throwable
    *                Thrown if case of an error
    */
   @Test
   @Ignore("JBJCA-984")
   public void testActivation1() throws Throwable
   {
      testActivation(AnnoMessageListener.class, 'U', 4.4, "G", 99);
   }

   /**
    * Test metadata
    *     
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testMetaData() throws Throwable
   {
      testMetaData(1);
   }

}
