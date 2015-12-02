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

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Narayana/memory performance tests
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class NarayanaMemPerfTestCase extends Performance
{
   /**
    * Lifecycle start, before the suite is executed
    * @throws Throwable throwable exception
    */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = EmbeddedFactory.create();

      // Startup
      embedded.startup();

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
      org.junit.runner.JUnitCore.main(NarayanaMemPerfTestCase.class.getName());
   }
}
