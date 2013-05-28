/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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

         rc.close();
      }
      finally
      {
         if (deployment != null)
         {
            embedded.undeploy(deployment);
         }

         if (context != null)
         {
            context.close();
         }
      }
   }

   /**
    * Two users using the same managed connection in turn
    * @throws Throwable throwable exception 
    */
   @Test
   public void testTwoUsers() throws Throwable
   {
      Context context = null;
      URL deployment = null;
      try
      {
         deployment = CRITestCase.class.getClassLoader().getResource("reauth-cri.rar");

         embedded.deploy(deployment);

         context = new InitialContext();

         ReauthConnectionFactory rcf = (ReauthConnectionFactory)context.lookup("java:/eis/Reauth");

         assertNotNull(rcf);

         String user1 = "user1";
         String password1 = "password1";

         ReauthConnection rc1 = rcf.getConnection(user1, password1);

         assertNotNull(rc1);

         assertEquals(user1, rc1.getAuth());

         assertTrue(rc1.logout());
         
         rc1.close();

         String user2 = "user2";
         String password2 = "password2";

         ReauthConnection rc2 = rcf.getConnection(user2, password2);

         assertNotNull(rc2);

         assertEquals(user2, rc2.getAuth());

         assertTrue(rc2.logout());

         rc2.close();
      }
      finally
      {
         if (deployment != null)
         {
            embedded.undeploy(deployment);
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
      reauthServer = new ReauthServer();
      reauthServer.setHostName(host);
      reauthServer.setPort(port);
      reauthServer.setMaxConnections(1);
      reauthServer.start();

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
      if (embedded != null)
      {
         embedded.shutdown();
         embedded = null;
      }

      if (reauthServer != null)
      {
         reauthServer.stop();
         reauthServer = null;
      }
   }
}
