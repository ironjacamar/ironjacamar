/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.workmanager.unit;

import org.jboss.jca.arquillian.embedded.Configuration;
import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.api.workmanager.DistributedWorkManager;
import org.jboss.jca.core.workmanager.rars.dwm.WorkConnectionFactory;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.util.UUID;

import javax.resource.spi.BootstrapContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Ignore;
import org.junit.runner.RunWith;


/**
 * DistributedWorkManagerJGroupsMJVMTestCase.
 *
 * Tests for the JBoss specific distributed work manager functionality.
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
@Configuration(autoActivate = false)
//TODO
@Ignore
public class DistributedWorkManagerJGroupsMJVMTestCase extends AbstractDistributedWorkManagerTest
{
   /** injected DistributedWorkManager */
   @Inject(name = "DistributedWorkManagerJGroups")
   protected DistributedWorkManager dwm;

   @Inject(name = "DistributedBootstrapContextJGroups")
   private BootstrapContext dbc;

   /**
    * {@inheritDoc}
    */
   protected DistributedWorkManager getDistributedWorkManager()
   {
      return dwm;
   }

   /**
    * {@inheritDoc}
    */
   protected BootstrapContext getBootstrapContext()
   {
      return dbc;
   }

   // --------------------------------------------------------------------------------||
   // Deployments --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Define the distributed work manager deployment
    * @return The deployment archive
    */
   @Deployment(name = "DWM", order = 1)
   public static InputStreamDescriptor createDistributedWorkManagerDeployment()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("dwm-jgroups.xml",
                                                            cl.getResourceAsStream("dwm-jgroups.xml"));
      return isd;
   }

   /**
    * Define the resource adapter deployment
    * @return The deployment archive
    */
   @Deployment(name = "RAR", order = 2)
   public static ResourceAdapterArchive createArchiveDeployment()
   {
      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "work.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(WorkConnectionFactory.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("rars/dwm/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Define the activation deployment
    * @return The deployment archive
    */
   @Deployment(name = "ACT", order = 3)
   public static InputStreamDescriptor createActivationDeployment()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("dwm-bc-jgroups-ra.xml",
                                                            cl.getResourceAsStream("dwm-bc-jgroups-ra.xml"));
      return isd;
   }

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

}
