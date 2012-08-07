/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.test.eis.unit;

import org.jboss.jca.embedded.arquillian.Configuration;
import org.jboss.jca.test.eis.rars.echo.EchoConnection;
import org.jboss.jca.test.eis.rars.echo.EchoConnectionFactory;

import java.util.UUID;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Echo test for the EIS test server
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
@Configuration(autoActivate = true)
public class EchoTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(EchoTestCase.class);

   /**
    * Define the deployment
    * @return The deployment archive
    */
   @Deployment
   public static ResourceAdapterArchive createDeployment()
   {
      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "echo.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(EchoConnection.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("echo.rar/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Resource(mappedName = "java:/eis/echo")
   private EchoConnectionFactory connectionFactory;
 
   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      assertNotNull(connectionFactory);

      EchoConnection c = null;
      try
      {
         c = connectionFactory.getConnection();
         String s = "Hello World!";
         assertEquals(s, c.echo(s));

         s = "Hello again!";
         assertEquals(s, c.echo(s));
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }
}
