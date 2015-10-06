/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.perf;

import org.ironjacamar.embedded.EmbeddedFactory;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * NoopTS performance tests
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class NoopTSPerfTestCase extends Performance
{
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
      URL stdio = NoopTSPerfTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = NoopTSPerfTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = NoopTSPerfTestCase.class.getClassLoader().getResource("noop-transaction.xml");
      URL jca = NoopTSPerfTestCase.class.getClassLoader().getResource("jca.xml");

      embedded.deploy(stdio);
      embedded.deploy(naming);
      embedded.deploy(transaction);
      embedded.deploy(jca);

      System.err.println("Embedded=" + embedded);
      
      beforeRun();
      printSettings();
   }

   /**
    * Lifecycle stop, after the suite is executed
    * @throws Throwable throwable exception
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      afterRun();
      dumpData();

      System.err.println("Embedded=" + embedded);

      // Undeploy
      URL stdio = NoopTSPerfTestCase.class.getClassLoader().getResource("stdio.xml");
      URL naming = NoopTSPerfTestCase.class.getClassLoader().getResource("naming.xml");
      URL transaction = NoopTSPerfTestCase.class.getClassLoader().getResource("noop-transaction.xml");
      URL jca = NoopTSPerfTestCase.class.getClassLoader().getResource("jca.xml");

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
      org.junit.runner.JUnitCore.main(NoopTSPerfTestCase.class.getName());
   }
}
