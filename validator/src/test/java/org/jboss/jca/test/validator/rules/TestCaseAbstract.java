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
package org.jboss.jca.test.validator.rules;

import org.jboss.jca.embedded.EmbeddedJCA;
import org.jboss.jca.test.validator.rules.ra.TestActivationSpec;
import org.jboss.jca.test.validator.rules.ra.TestManagedConnection;
import org.jboss.jca.test.validator.rules.ra.TestResourceAdapterRight;
import org.jboss.jca.test.validator.rules.ra.TestResourceAdapterWrong;

import java.io.File;
import java.util.UUID;

import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Abstract class used from TestCase for conveninet methods and Lifecycle methods
 *
 * @author Stefano Maestri mailto:stefano.maestri@javalinux.it
 *
 */
public abstract class TestCaseAbstract
{

   /**
    * Protected embedded JCA used by subclass to deploy
    */
   protected static EmbeddedJCA embedded;

   /**
    * It create the Resource adapter to deploy using shrinkwrap
    *
    * @param archiveName name of archive
    * @return the ResourceAdapter ot deploy
    * @throws Throwable in case of error
    */
   protected ResourceAdapterArchive getArchive(String archiveName) throws Throwable
   {
      ResourceAdapterArchive raa = Archives.create(archiveName, ResourceAdapterArchive.class);

      JavaArchive ja = Archives.create(UUID.randomUUID().toString() + ".jar", JavaArchive.class);
      ja.addClasses(TestActivationSpec.class, TestManagedConnection.class, TestResourceAdapterRight.class,
            TestResourceAdapterWrong.class);

      raa.addLibrary(ja);
      raa.addManifestResource("validator/" + archiveName + "/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Delete a temporary directory and all its content
    *
    * @param path directory to delete
    * @return true if it is effectively delete, false in negative case.
    */
   protected boolean deleteDirectory(File path)
   {
      if (path.exists())
      {
         File[] files = path.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            if (files[i].isDirectory())
            {
               deleteDirectory(files[i]);
            }
            else
            {
               files[i].delete();
            }
         }
      }
      return (path.delete());
   }

   /**
   *
   * Lifecycle method creating the {@link EmbeddedJCA} and starting it up
   *
   * @throws Throwable in case of setup errors
   *
   */
   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create and set an embedded JCA instance
      embedded = new EmbeddedJCA();

      // Startup
      embedded.startup();
   }

   /**
    *
    * Lifecycle method shutting down the {@link EmbeddedJCA}
    *
    * @throws Throwable in case of shutdown errors
    *
    */
   @AfterClass
   public static void afterClass() throws Throwable
   {
      // Shutdown embedded
      embedded.shutdown();

      // Set embedded to null
      embedded = null;
   }

}
