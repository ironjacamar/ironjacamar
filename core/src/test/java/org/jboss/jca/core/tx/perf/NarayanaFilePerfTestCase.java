/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.tx.perf;

import org.jboss.jca.deployers.fungal.RAActivator;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import org.jboss.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Narayana/FILE performance tests
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class NarayanaFilePerfTestCase extends Performance
{
   private static Logger log = Logger.getLogger(NarayanaFilePerfTestCase.class);

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
      URL stdio = NarayanaFilePerfTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = NarayanaFilePerfTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = NarayanaFilePerfTestCase.class.getClassLoader().getResource("file-transaction.xml");
      URL jca = NarayanaFilePerfTestCase.class.getClassLoader().getResource("jca.xml");

      embedded.deploy(stdio);
      embedded.deploy(naming);
      embedded.deploy(transaction);
      embedded.deploy(jca);

      // Disable RAActivator
      RAActivator raa = embedded.lookup("RAActivator", RAActivator.class);

      if (raa == null)
         throw new IllegalStateException("RAActivator not defined");

      raa.setEnabled(false);

      printSettings();
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      dumpData();

      // Undeploy
      URL stdio = NarayanaFilePerfTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = NarayanaFilePerfTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = NarayanaFilePerfTestCase.class.getClassLoader().getResource("file-transaction.xml");
      URL jca = NarayanaFilePerfTestCase.class.getClassLoader().getResource("jca.xml");

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
    * Main
    * @param args The arguments
    */
   public static void main(String[] args)
   {
      org.junit.runner.JUnitCore.main(NarayanaFilePerfTestCase.class.getName());
   }
}
