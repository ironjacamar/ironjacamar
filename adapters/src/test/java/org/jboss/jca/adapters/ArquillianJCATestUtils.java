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
package org.jboss.jca.adapters;

import java.util.UUID;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

/**
 * An ArquillianJCATestUtils.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
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
      ja.addPackages(true, packageName.replace('.', '/'));

      raa.addAsLibrary(ja);

      return raa;
   }

   /**
    * Build a shrinkwrap rar for jdbc local
    *
    * @return the shrinkwrapped rar
    * @throws Exception in case of error creating the archive
    */
   public static ResourceAdapterArchive buildShrinkwrapJdbcLocal() throws Exception
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "jdbc-local.rar");
      JavaArchive ja = buildShrinkwrapJdbcJar();

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("jdbc/local/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Build a shrinkwrap rar for jdbc xa
    *
    * @return the shrinkwrapped rar
    * @throws Exception in case of error creating the archive
    */
   public static ResourceAdapterArchive buildShrinkwrapJdbcXa() throws Exception
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "jdbc-xa.rar");
      JavaArchive ja = buildShrinkwrapJdbcJar();

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("jdbc/xa/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Build the JDBC JAR archive
    * @return The archive
    */
   private static JavaArchive buildShrinkwrapJdbcJar()
   {
      String packageName = "org/jboss/jca/adapters/jdbc";

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, "ironjacamar-jdbc.jar");
      ja.addPackages(true, packageName);

      return ja;
   }
}
