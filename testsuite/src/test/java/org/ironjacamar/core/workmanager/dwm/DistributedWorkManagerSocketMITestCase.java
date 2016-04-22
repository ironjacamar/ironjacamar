/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.core.workmanager.dwm;

import org.ironjacamar.embedded.Embedded;
import org.ironjacamar.embedded.dsl.InputStreamDescriptor;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.rars.ResourceAdapterFactory;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * WorkAdapterTestCase.
 * <p>
 * Tests for the JCA specific API about WorkAdapter
 *
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 */

public class DistributedWorkManagerSocketMITestCase extends AbstractDistributedWorkManagerTest
{

   /**
    * deploy an all instances and lookup wcf
    * @throws Throwable in case of error
    */
   @BeforeClass
   public static void deploy() throws Throwable
   {
      int i = 0;
      for (Embedded embedded : embeddedList)
      {
         embedded.deploy(createBootstrapContext(i));
         embedded.deploy(createResourceAdapter());
         embedded.deploy(createActivation());
         i++;
      }

   }

   /**
    * undeploy an all instances
    * @throws Throwable in case of error
    */
   @AfterClass
   public static void undelpoy() throws Throwable
   {
      int i = 0;
      for (Embedded embedded : embeddedList)
      {
         embedded.undeploy(createActivation());
         embedded.undeploy(createResourceAdapter());
         embedded.undeploy(createBootstrapContext(i));
         i++;
      }

   }

   private static InputStreamDescriptor createBootstrapContext(int instanceNumber) throws Throwable
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("dwm-socket.xml",
            cl.getResourceAsStream("dwm/instance-" + instanceNumber + "/dwm-socket.xml"));
      return isd;

   }

   private static ResourceAdapterArchive createResourceAdapter() throws Throwable
   {
      return ResourceAdapterFactory.createWorkRar();
   }

   private static ResourceAdaptersDescriptor createActivation() throws Throwable
   {
      return ResourceAdapterFactory.createWorkDeployment("DWMBC-Socket");
   }

   /**
    * Main
    * @param args The arguments
    */
   public static void main(String[] args)
   {
      org.junit.runner.JUnitCore.main(DistributedWorkManagerJGroupsMITestCase.class.getName());
   }
}
