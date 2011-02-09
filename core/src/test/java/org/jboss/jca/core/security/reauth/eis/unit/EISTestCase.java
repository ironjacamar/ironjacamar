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

package org.jboss.jca.core.security.reauth.eis.unit;

import org.jboss.jca.core.security.reauth.eis.Auth;
import org.jboss.jca.core.security.reauth.eis.Close;
import org.jboss.jca.core.security.reauth.eis.Connect;
import org.jboss.jca.core.security.reauth.eis.Echo;
import org.jboss.jca.core.security.reauth.eis.ReauthServer;
import org.jboss.jca.core.security.reauth.eis.SimpleCRI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.jboss.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the reauthencation EIS server.
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class EISTestCase
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static Logger log = Logger.getLogger(EISTestCase.class);

   private static ReauthServer reauthServer;

   private static String host = "localhost";
   private static int port = 19000;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Connect
    * @throws Throwable throwable exception 
    */
   @Test
   public void testConnect() throws Throwable
   {
      Socket socket = null;
      try
      {
         socket = new Socket(host, port);

         log.infof("Socket: %s", socket);

         ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

         // Connect
         oos.writeByte(Connect.KEY);
         oos.flush();

         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

         Boolean granted = (Boolean)ois.readObject();
         assertTrue(granted.booleanValue());

         // Write
         Integer payload = new Integer(0);
         oos.writeByte(Echo.KEY);
         oos.writeObject(payload);
         oos.flush();

         // Read
         Object result = ois.readObject();

         // Assert
         assertEquals(payload, result);

         // Close
         oos.writeByte(Close.KEY);
         oos.flush();
      }
      finally
      {
         if (socket != null)
         {
            try
            {
               socket.close();
            }
            catch (IOException ioe)
            {
               log.debugf(ioe.getMessage(), ioe);
            }
         }
      }
   }

   /**
    * Connect
    * @throws Throwable throwable exception 
    */
   @Test
   public void testDoubleConnect() throws Throwable
   {
      Socket socket1 = null;
      Socket socket2 = null;

      try
      {
         socket1 = new Socket(host, port);

         log.infof("Socket1: %s", socket1);

         ObjectOutputStream oos1 = new ObjectOutputStream(socket1.getOutputStream());

         // Connect
         oos1.writeByte(Connect.KEY);
         oos1.flush();

         ObjectInputStream ois1 = new ObjectInputStream(socket1.getInputStream());

         Boolean granted1 = (Boolean)ois1.readObject();
         assertTrue(granted1.booleanValue());

         // Payload
         Integer payload = new Integer(0);

         // Write
         oos1.writeByte(Echo.KEY);
         oos1.writeObject(payload);
         oos1.flush();

         // Read
         Object result = ois1.readObject();

         // Assert
         assertEquals(payload, result);

         socket2 = new Socket(host, port);

         log.infof("Socket2: %s", socket2);

         ObjectOutputStream oos2 = new ObjectOutputStream(socket2.getOutputStream());

         // Connect
         oos2.writeByte(Connect.KEY);
         oos2.flush();

         ObjectInputStream ois2 = new ObjectInputStream(socket2.getInputStream());

         Boolean granted2 = (Boolean)ois2.readObject();
         assertTrue(!granted2.booleanValue());

         // Close
         oos1.writeByte(Close.KEY);
         oos1.flush();
         oos2.writeByte(Close.KEY);
         oos2.flush();
      }
      finally
      {
         if (socket1 != null)
         {
            try
            {
               socket1.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }

         if (socket2 != null)
         {
            try
            {
               socket2.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Auth CRI test
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAuthCri() throws Throwable
   {
      Socket socket = null;
      try
      {
         socket = new Socket(host, port);

         log.infof("Socket: %s", socket);

         // User
         String userName = "user";
         String password = "password";

         // Connect
         ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
         oos.writeByte(Connect.KEY);
         oos.flush();

         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

         Boolean granted = (Boolean)ois.readObject();
         assertTrue(granted.booleanValue());

         // Payload
         SimpleCRI payload = new SimpleCRI(userName, password);

         // Write
         oos.writeByte(Auth.KEY);
         oos.writeUTF(payload.getUserName());
         oos.writeUTF(payload.getPassword());
         oos.flush();

         // Read
         Object result = ois.readObject();

         // Assert
         assertEquals(userName, result);

         // Close
         oos.writeByte(Close.KEY);
         oos.flush();
      }
      finally
      {
         if (socket != null)
         {
            try
            {
               socket.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Auth CRI test - change user name
    * @throws Throwable throwable exception 
    */
   @Test
   public void testAuthCriChangeUserName() throws Throwable
   {
      Socket socket = null;
      try
      {
         socket = new Socket(host, port);

         log.infof("Socket: %s", socket);

         // User
         String userName1 = "user1";
         String password1 = "password1";
         String userName2 = "user2";
         String password2 = "password2";

         // Connect
         ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
         oos.writeByte(Connect.KEY);
         oos.flush();

         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

         Boolean granted = (Boolean)ois.readObject();
         assertTrue(granted.booleanValue());

         // Payload
         SimpleCRI payload1 = new SimpleCRI(userName1, password1);

         // Write
         oos.writeByte(Auth.KEY);
         oos.writeUTF(payload1.getUserName());
         oos.writeUTF(payload1.getPassword());
         oos.flush();

         // Read
         Object result = ois.readObject();

         // Assert
         assertEquals(userName1, result);

         // Payload
         SimpleCRI payload2 = new SimpleCRI(userName2, password2);

         // Write
         oos.writeByte(Auth.KEY);
         oos.writeUTF(payload2.getUserName());
         oos.writeUTF(payload2.getPassword());
         oos.flush();

         // Read
         result = ois.readObject();

         // Assert
         assertEquals(userName2, result);

         // Close
         oos.writeByte(Close.KEY);
         oos.flush();
      }
      finally
      {
         if (socket != null)
         {
            try
            {
               socket.close();
            }
            catch (IOException ioe)
            {
               // Ignore
            }
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
      
      assertNotNull(reauthServer);

      reauthServer.start();
   }

   /**
    * Lifecycle stop, after each test is executed
    * @throws Throwable throwable exception 
    */
   @After
   public void after() throws Throwable
   {
      reauthServer.stop();
      reauthServer = null;
   }
}
