/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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

package org.jboss.jca.embedded;

import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test cases for the embedded container
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class EmbeddedTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(EmbeddedTestCase.class);

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Basic container
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasicContainerLifecycle() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(false);
      embedded.startup();
      embedded.shutdown();
      embedded = null;
   }

   /**
    * Full container
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testFullContainerLifecycle() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create();
      embedded.startup();
      embedded.shutdown();
      embedded = null;
   }

   /**
    * Full container
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testFullContainerLifecycleBoolean() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(true);
      embedded.startup();
      embedded.shutdown();
      embedded = null;
   }

   /**
    * Successful deployment
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testSuccessfulDeployment() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(false);
      embedded.startup();

      URL test = EmbeddedTestCase.class.getClassLoader().getResource("simple-deployment.xml");
      embedded.deploy(test);
      embedded.undeploy(test);

      embedded.shutdown();
      embedded = null;
   }

   /**
    * Fail double startup
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalStateException.class)
   public void testFailDoubleStartup() throws Throwable
   {
      Embedded embedded = null;
      try
      {
         embedded = EmbeddedFactory.create(false);
         embedded.startup();
         embedded.startup();
      }
      finally
      {
         if (embedded != null)
         {
            embedded.shutdown();
            embedded = null;
         }
      }
   }

   /**
    * Fail shutdown since container not started
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalStateException.class)
   public void testFailShutdownSinceNotStarted() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(false);
      embedded.shutdown();
   }

   /**
    * Fail deployment since container not started
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalStateException.class)
   public void testFailDeploymentSinceNotStarted() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(false);

      URL test = EmbeddedTestCase.class.getClassLoader().getResource("simple-deployment.xml");
      embedded.deploy(test);
   }

   /**
    * Fail undeployment since container not started
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalStateException.class)
   public void testFailUndeploymentSinceNotStarted() throws Throwable
   {
      Embedded embedded = EmbeddedFactory.create(false);

      URL test = EmbeddedTestCase.class.getClassLoader().getResource("simple-deployment.xml");
      embedded.undeploy(test);
   }

   /**
    * Fail null deployment
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testFailNullURLDeployment() throws Throwable
   {
      Embedded embedded = null;
      URL deployment = null;
      try
      {
         embedded = EmbeddedFactory.create(false);
         embedded.startup();
         embedded.deploy(deployment);
      }
      finally
      {
         if (embedded != null)
         {
            embedded.shutdown();
            embedded = null;
         }
      }
   }

   /**
    * Fail null undeployment
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testFailNullURLUndeployment() throws Throwable
   {
      Embedded embedded = null;
      URL deployment = null;
      try
      {
         embedded = EmbeddedFactory.create(false);
         embedded.startup();
         embedded.undeploy(deployment);
      }
      finally
      {
         if (embedded != null)
         {
            embedded.shutdown();
            embedded = null;
         }
      }
   }

   /**
    * Fail null deployment
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testFailNullShrinkWrapDeployment() throws Throwable
   {
      Embedded embedded = null;
      ResourceAdapterArchive deployment = null;
      try
      {
         embedded = EmbeddedFactory.create(false);
         embedded.startup();
         embedded.deploy(deployment);
      }
      finally
      {
         if (embedded != null)
         {
            embedded.shutdown();
            embedded = null;
         }
      }
   }

   /**
    * Fail null undeployment
    * @exception Throwable Thrown if case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testFailNullShrinkWrapUndeployment() throws Throwable
   {
      Embedded embedded = null;
      ResourceAdapterArchive deployment = null;
      try
      {
         embedded = EmbeddedFactory.create(false);
         embedded.startup();
         embedded.undeploy(deployment);
      }
      finally
      {
         if (embedded != null)
         {
            embedded.shutdown();
            embedded = null;
         }
      }
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception 
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception 
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
   }
}
