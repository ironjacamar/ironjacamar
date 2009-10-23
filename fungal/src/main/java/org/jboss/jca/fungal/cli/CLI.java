/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.cli;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * The command line interface for the JCA/Fungal kernel
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class CLI
{
   /** Deploy */
   private static final int DEPLOY = 0;

   /** Undeploy */
   private static final int UNDEPLOY = 1;

   /**
    * Constructor
    */
   private CLI()
   {
   }

   /**
    * Main
    * @param args The command line arguments
    */
   public static void main(String[] args)
   {
      if (args.length < 2)
      {
         usage();
      }
      else
      {
         Socket socket = null;
         try
         {
            String host = null;
            int port = 1202;
            int counter = 0;
            int command = -1;

            if ("-h".equals(args[counter]))
            {
               counter++;
               host = args[counter];
               counter++;
            }

            if ("-p".equals(args[counter]))
            {
               counter++;
               port = Integer.valueOf(args[counter]).intValue();
               counter++;
            }

            if ("deploy".equals(args[counter]))
            {
               command = DEPLOY;
            }
            else if ("undeploy".equals(args[counter]))
            {
               command = UNDEPLOY;
            }
            counter++;

            File file = new File(args[counter]);

            if (host == null)
               host = "localhost";

            socket = new Socket(host, port);

            Communicator communicator = new Communicator();

            if (command == DEPLOY)
            {
               communicator.deploy(socket, file.toURI().toURL());
            }
            else if (command == UNDEPLOY)
            {
               communicator.undeploy(socket, file.toURI().toURL());
            }
            else
            {
               System.err.println("Unknown command: " + command);
            }
         }
         catch (IOException ioe)
         {
            ioe.printStackTrace(System.err);
         }
         finally
         {
            try
            {
               if (socket != null)
                  socket.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Usage
    */
   private static void usage()
   {
      System.out.println("Usage: CLI <common> <command>");

      System.out.println(" Common:");
      System.out.println(" -------");
      System.out.println(" -h <host> (default: localhost)");
      System.out.println(" -p <port> (default: 1202)");

      System.out.println("");

      System.out.println(" Commands:");
      System.out.println(" ---------");
      System.out.println(" deploy <file>");
      System.out.println(" undeploy <file>");
   }
}
