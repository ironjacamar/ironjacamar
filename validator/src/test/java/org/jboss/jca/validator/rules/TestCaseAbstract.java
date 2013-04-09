/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.validator.rules;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.validator.rules.ra.TestActivationSpec;
import org.jboss.jca.validator.rules.ra.TestManagedConnection;
import org.jboss.jca.validator.rules.ra.TestResourceAdapterRight;
import org.jboss.jca.validator.rules.ra.TestResourceAdapterWrong;

import java.io.File;
import java.util.UUID;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.runner.RunWith;

/**
 * Abstract class used from TestCase for conveninet methods and Lifecycle methods
 *
 * @author Stefano Maestri mailto:stefano.maestri@javalinux.it
 *
 */
@RunWith(Arquillian.class)
public abstract class TestCaseAbstract
{

   /**
    * Protected embedded JCA used by subclass to deploy
    */
   @ArquillianResource
   protected static Embedded embedded;

   /**
    * It create the Resource adapter to deploy using shrinkwrap
    *
    * @param archiveName name of archive
    * @return the ResourceAdapter ot deploy
    * @throws Throwable in case of error
    */
   protected ResourceAdapterArchive getArchive(String archiveName) throws Throwable
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, archiveName);

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addClasses(TestActivationSpec.class, TestManagedConnection.class, TestResourceAdapterRight.class,
            TestResourceAdapterWrong.class);

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("validator/" + archiveName + "/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * 
    * Creates resource adapter from annotated classes only
    * 
    * @param classes for RA
    * @return the ResourceAdapter ot deploy
    * @throws Throwable in case of error
    */
   protected ResourceAdapterArchive getAnnoArchive(Class... classes) throws Throwable
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "archive.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addClasses(classes);
      raa.addAsLibrary(ja);
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

}
