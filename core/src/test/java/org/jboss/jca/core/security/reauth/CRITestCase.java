/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.core.security.reauth;

import org.jboss.jca.core.security.reauth.eis.ReauthServer;
import org.jboss.jca.core.security.reauth.ra.cri.ReauthConnection;
import org.jboss.jca.core.security.reauth.ra.cri.ReauthConnectionFactory;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for CRO based reauthentication.
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class CRITestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(CRITestCase.class);

   private static Embedded embedded;

   // EIS server
   private static String host = "localhost";
   private static int port = 19000;
   private static ReauthServer reauthServer = null;


   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Basic
    * @throws Throwable throwable exception 
    */
   @Test
   public void testBasic() throws Throwable
   {
      Context context = null;
      URL deployment = null;
      try
      {
         reauthServer = new ReauthServer();
         reauthServer.setHostName(host);
         reauthServer.setPort(port);
         reauthServer.setMaxConnections(1);
         reauthServer.start();

         deployment = CRITestCase.class.getClassLoader().getResource("reauth-cri.rar");

         embedded.deploy(deployment);

         context = new InitialContext();

         String user = "user";
         String password = "password";

         ReauthConnectionFactory rcf = (ReauthConnectionFactory)context.lookup("java:/eis/Reauth");

         assertNotNull(rcf);

         ReauthConnection rc = rcf.getConnection(user, password);

         assertNotNull(rc);

         assertEquals(user, rc.getAuth());

         assertTrue(rc.logout());
      }
      finally
      {
         if (deployment != null)
         {
            embedded.undeploy(deployment);
         }

         if (reauthServer != null)
         {
            reauthServer.stop();
            reauthServer = null;
         }

         if (context != null)
         {
            context.close();
         }
      }
   }

   // --------------------------------------------------------------------------------||
   // Lifecycle Methods --------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Lifecycle start, before each test is executed
    * @throws Throwable throwable exception 
    */
   @Before
   public void before() throws Throwable
   {
      embedded = EmbeddedFactory.create(true);
      embedded.startup();
   }

   /**
    * Lifecycle stop, after each test is executed
    * @throws Throwable throwable exception 
    */
   @After
   public void after() throws Throwable
   {
      embedded.shutdown();
      embedded = null;
   }
}
