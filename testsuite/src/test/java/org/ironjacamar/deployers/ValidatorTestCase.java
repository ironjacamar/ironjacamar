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
package org.ironjacamar.deployers;

import org.ironjacamar.embedded.Embedded;
import org.ironjacamar.embedded.EmbeddedFactory;
import org.ironjacamar.embedded.dsl.resourceadapters20.api.ResourceAdaptersDescriptor;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.validator.ValidatorException;

import java.net.URL;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Basic Validator on Workmanager test case
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public class ValidatorTestCase
{
   private static Embedded embedded = null;

   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create(false);

      // Startup
      embedded.startup();

      // Deploy
      URL stdio = ValidatorTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = ValidatorTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = ValidatorTestCase.class.getClassLoader().getResource("noop-transaction.xml");
      URL jca = ValidatorTestCase.class.getClassLoader().getResource("jca.xml");

      embedded.deploy(stdio);
      embedded.deploy(naming);
      embedded.deploy(transaction);
      embedded.deploy(jca);

   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Undeploy
      URL stdio = ValidatorTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = ValidatorTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = ValidatorTestCase.class.getClassLoader().getResource("noop-transaction.xml");
      URL jca = ValidatorTestCase.class.getClassLoader().getResource("jca.xml");

      embedded.undeploy(jca);
      embedded.undeploy(transaction);
      embedded.undeploy(naming);
      embedded.undeploy(stdio);

      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }

   
   /**
    * Deployment
    * @throws Throwable In case of an error
    */
   @Test
   public void testDeployment() throws Throwable
   {
      ResourceAdapterArchive rar = ResourceAdapterFactory.createWorkRarMCFNoHashCode();
      ResourceAdaptersDescriptor raxml = ResourceAdapterFactory.createWorkDeploymentMCFNoHashCode(null);

      try
      {
         embedded.deploy(rar);
         embedded.deploy(raxml);
         fail();
      }
      catch (Throwable t)
      {
         if (!t.getCause().getCause().getClass().equals(ValidatorException.class))
            fail();
      }
      finally
      {
         embedded.undeploy(raxml);
         embedded.undeploy(rar);
      }

   }
}
