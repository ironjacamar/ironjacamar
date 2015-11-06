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

package org.ironjacamar.eis.support;

import org.ironjacamar.eis.Handler;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Echo handler
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class EchoHandler implements Handler
{
   /**
    * Constructor
    */
   public EchoHandler()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void handle(InputStream is, OutputStream os)
   {
      try
      {
         ObjectInputStream ois = new ObjectInputStream(is);
         ObjectOutputStream oos = new ObjectOutputStream(os);
         boolean done = false;

         while (!done)
         {
            String command = ois.readUTF();
            if ("echo".equals(command))
            {
               String s = ois.readUTF();
               oos.writeUTF(s);
               oos.flush();
            }
            else if ("close".equals(command))
            {
               done = true;
            }
            else
            {
               // Unknown command - terminate
               done = true;
            }
         }
      }
      catch (Throwable t)
      {
         // Nothing
      }
   }
}
