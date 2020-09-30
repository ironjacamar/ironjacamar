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

import org.jboss.jca.core.security.DefaultSubjectFactory;
import org.jboss.jca.core.security.reauth.eis.ReauthServer;
import org.jboss.jca.core.security.reauth.ra.subject.ReauthConnection;
import org.jboss.jca.core.security.reauth.ra.subject.ReauthConnectionFactory;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.logging.Logger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
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
public class SubjectTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(SubjectTestCase.class);

   private static Embedded embedded;
   private static DefaultSubjectFactory subjectFactory;

   // EIS server
   private static String host = "localhost";
   private static int port = 19000;
   private static ReauthServer reauthServer = null;

   public static ResourceAdapterArchive createDeployment()
   {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "reauth-subject.jar");
      jar.addPackage(ReauthConnection.class.getPackage());

      ResourceAdapterArchive rar =
              ShrinkWrap.create(ResourceAdapterArchive.class, "reauth-subject.rar");
      rar.addAsLibrary(jar);
      rar.addAsManifestResource("rars/security/reauth/subject/META-INF/ironjacamar.xml", "ironjacamar.xml");
      rar.addAsManifestResource("rars/security/reauth/subject/META-INF/ra.xml", "ra.xml");
      return rar;
   }


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
      ResourceAdapterArchive deployment = null;
      try
      {
         deployment = createDeployment();

         embedded.deploy(deployment);

         context = new InitialContext();

         String user = "user";
         String password = "password";

         subjectFactory.setUserName(user);
         subjectFactory.setPassword(password);

         ReauthConnectionFactory rcf = (ReauthConnectionFactory)context.lookup("java:/eis/Reauth");

         assertNotNull(rcf);

         ReauthConnection rc = rcf.getConnection();

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
      ResourceAdapterArchive deployment = null;
      try
      {
         deployment = createDeployment();

         embedded.deploy(deployment);

         context = new InitialContext();

         ReauthConnectionFactory rcf = (ReauthConnectionFactory)context.lookup("java:/eis/Reauth");

         assertNotNull(rcf);

         String user1 = "user1";
         String password1 = "password1";

         subjectFactory.setUserName(user1);
         subjectFactory.setPassword(password1);

         ReauthConnection rc1 = rcf.getConnection();

         assertNotNull(rc1);

         assertEquals(user1, rc1.getAuth());

         assertTrue(rc1.logout());
         
         rc1.close();

         String user2 = "user2";
         String password2 = "password2";

         subjectFactory.setUserName(user2);
         subjectFactory.setPassword(password2);

         ReauthConnection rc2 = rcf.getConnection();

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

      subjectFactory = embedded.lookup("DefaultSecurityDomain", DefaultSubjectFactory.class);
      assertNotNull(subjectFactory);
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
