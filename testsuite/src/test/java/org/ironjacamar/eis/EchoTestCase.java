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

package org.ironjacamar.eis;

import org.ironjacamar.eis.support.EchoHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Echo test for the EIS test server
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class EchoTestCase
{
   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   private static EISServer server = new EISServer();

   /**
    * Start EIS server
    *
    * @throws Throwable In case of an error
    */
   @BeforeClass
   public static void startEISServer() throws Throwable
   {
      server.setHandlerClassName(EchoHandler.class.getName());
      server.setHost("localhost");
      server.setPort(1400);
      server.startup();
   }

   /**
    * Stop EIS server
    *
    * @throws Throwable In case of an error
    */
   @BeforeClass
   public static void stopEISServer() throws Throwable
   {
      server.shutdown();
   }

   /**
    * Basic
    *
    * @throws Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      Socket socket = null;
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      try
      {
         socket = new Socket("localhost", 1400);
         oos = new ObjectOutputStream(socket.getOutputStream());
         ois = new ObjectInputStream(socket.getInputStream());

         String s = "Hello World!";

         assertEquals(s, echo(oos, ois, s));

         s = "Hello Again!";

         assertEquals(s, echo(oos, ois, s));

         oos.writeUTF("close");
         oos.flush();

      }
      finally
      {
         if (oos != null)
            oos.close();
         if (ois != null)
            ois.close();
         if (socket != null)
            socket.close();

      }
   }

   private String echo(ObjectOutputStream oos, ObjectInputStream ois, String s) throws IOException
   {
      oos.writeUTF("echo");
      oos.writeUTF(s);
      oos.flush();

      return ois.readUTF();
   }
}
