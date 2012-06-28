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
package org.jboss.jca.test.deployers.spec;

import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.util.UUID;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 *
 * A ArquillianJCATestUtils.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public final class ArquillianJCATestUtils
{

   /**
    * Build a shrinkwrap rar adding all necessary classes
    *
    * @param archiveName the archhive name
    * @param packageName the package name
    * @return the shrinkwrapped rar
    * @throws Exception in case of error creating the archive
    */
   public static ResourceAdapterArchive buidShrinkwrapRa(String archiveName, String packageName) throws Exception
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, archiveName);

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");

      ja.addPackage(packageName);
      
      raa.addAsLibrary(ja);

      return raa;
   }
   
   /**
    * Define the deployment of file
    * @param fileName to deploy
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   public static Descriptor createDescriptor(String fileName) throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor(fileName, 
                                                            cl.getResourceAsStream(fileName));
      return isd;
   }

}
