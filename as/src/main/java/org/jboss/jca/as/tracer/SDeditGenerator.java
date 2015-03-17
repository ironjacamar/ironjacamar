/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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

package org.jboss.jca.as.tracer;

import org.jboss.jca.core.tracer.TraceEvent;

import java.io.FileWriter;
import java.util.List;

/**
 * Generator for a SDedit file
 */
public class SDeditGenerator
{
   /**
    * Write string
    * @param fw The file writer
    * @param s The string
    * @exception Exception If an error occurs
    */
   private static void writeString(FileWriter fw, String s) throws Exception
   {
      for (int i = 0; i < s.length(); i++)
      {
         fw.write((int)s.charAt(i));
      }
   }

   /**
    * Write EOL
    * @param fw The file writer
    * @exception Exception If an error occurs
    */
   private static void writeEOL(FileWriter fw) throws Exception
   {
      fw.write((int)'\n');
   }

   /**
    * Generate the SDedit file
    * @param events The trace events
    * @param fw The file writer
    * @exception Exception If an error occurs
    */
   public static void generateSDedit(List<TraceEvent> events, FileWriter fw) throws Exception
   {
      String connectionListener = events.get(0).getConnectionListener();
      long start = events.get(0).getTimestamp();
      long end = events.get(events.size() - 1).getTimestamp();

      boolean newCl = false;
      boolean hasTx = false;
      boolean newSync = false;

      for (TraceEvent te : events)
      {
         if (te.getType() == TraceEvent.GET_CONNECTION_LISTENER_NEW ||
             te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW)
         {
            newCl = true;
         }
         else if (te.getType() == TraceEvent.ENLIST_CONNECTION_LISTENER ||
                  te.getType() == TraceEvent.ENLIST_CONNECTION_LISTENER_FAILED ||
                  te.getType() == TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER ||
                  te.getType() == TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED)
         {
            hasTx = true;
         }
      }


      writeString(fw, "#![ConnectionListener: " + connectionListener + "]");
      writeEOL(fw);

      writeString(fw, "#!>>");
      writeEOL(fw);
      writeString(fw, "#!Start: " + start);
      writeEOL(fw);
      writeString(fw, "#!End  : " + end);
      writeEOL(fw);
      writeString(fw, "#!<<");
      writeEOL(fw);

      writeString(fw, "application:Application[a]");
      writeEOL(fw);

      writeString(fw, "cm:ConnectionManager[a]");
      writeEOL(fw);

      writeString(fw, "pool:Pool[a]");
      writeEOL(fw);

      writeString(fw, "mcp:MCP[a]");
      writeEOL(fw);

      if (!newCl)
      {
         writeString(fw, "cl:ConnectionListener[a]");
         writeEOL(fw);
      }
      else
      {
         writeString(fw, "/cl:ConnectionListener[a]");
         writeEOL(fw);
      }

      if (hasTx)
      {
         writeString(fw, "tx:Transaction[a]");
         writeEOL(fw);

         writeString(fw, "/sync:Synchronization[a,x]");
         writeEOL(fw);
      }

      writeEOL(fw);

      for (int i = 0; i < events.size(); i++)
      {
         TraceEvent te = events.get(i);
         switch (te.getType())
         {
            case TraceEvent.GET_CONNECTION_LISTENER:
               writeString(fw, "application:cm.allocateConnection()");
               writeEOL(fw);

               writeString(fw, "cm:pool.getConnection()");
               writeEOL(fw);

               writeString(fw, "pool:mcp." + TraceEvent.asText(te));
               writeEOL(fw);

               writeString(fw, "mcp:cl.get()");
               writeEOL(fw);

               break;
            case TraceEvent.GET_CONNECTION_LISTENER_NEW:
               writeString(fw, "application:cm.allocateConnection()");
               writeEOL(fw);

               writeString(fw, "cm:pool.getConnection()");
               writeEOL(fw);

               writeString(fw, "pool:mcp." + TraceEvent.asText(te));
               writeEOL(fw);

               writeString(fw, "mcp:cl.new()");
               writeEOL(fw);

               break;
            case TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER:
               writeString(fw, "application:cm.allocateConnection()");
               writeEOL(fw);

               writeString(fw, "cm:pool.getConnection()");
               writeEOL(fw);

               writeString(fw, "pool:mcp." + TraceEvent.asText(te));
               writeEOL(fw);

               writeString(fw, "mcp:cl.get()");
               writeEOL(fw);

               break;
            case TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW:
               writeString(fw, "application:cm.allocateConnection()");
               writeEOL(fw);

               writeString(fw, "cm:pool.getConnection()");
               writeEOL(fw);

               writeString(fw, "pool:mcp." + TraceEvent.asText(te));
               writeEOL(fw);

               writeString(fw, "mcp:cl.new()");
               writeEOL(fw);

               break;
            case TraceEvent.RETURN_CONNECTION_LISTENER:

               if (hasTx)
               {
                  writeString(fw, "[c afterCompletion]");
                  writeEOL(fw);

                  writeString(fw, "sync:mcp." + TraceEvent.asText(te));
                  writeEOL(fw);

                  writeString(fw, "[/c]");
                  writeEOL(fw);
               }
               else
               {
                  writeString(fw, "cl:mcp." + TraceEvent.asText(te));
                  writeEOL(fw);
               }

               break;
            case TraceEvent.RETURN_CONNECTION_LISTENER_WITH_KILL:

               if (hasTx)
               {
                  writeString(fw, "[c afterCompletion]");
                  writeEOL(fw);

                  writeString(fw, "sync:>mcp." + TraceEvent.asText(te));
                  writeEOL(fw);

                  writeString(fw, "mcp:cl.doDestroy()");
                  writeEOL(fw);

                  writeString(fw, "[/c]");
                  writeEOL(fw);
               }
               else
               {
                  writeString(fw, "cl:mcp." + TraceEvent.asText(te));
                  writeEOL(fw);

                  writeString(fw, "mcp:cl.doDestroy()");
                  writeEOL(fw);
               }

               break;
            case TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER:

               writeString(fw, "cl:mcp." + TraceEvent.asText(te));
               writeEOL(fw);

               break;
            case TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL:

               writeString(fw, "cl:mcp." + TraceEvent.asText(te));
               writeEOL(fw);

               writeString(fw, "mcp:cl.doDestroy()");
               writeEOL(fw);

               break;

            case TraceEvent.CLEAR_CONNECTION_LISTENER:

               break;

            case TraceEvent.ENLIST_CONNECTION_LISTENER:
            case TraceEvent.ENLIST_CONNECTION_LISTENER_FAILED:
               writeString(fw, "cl:tx." + TraceEvent.asText(te));
               writeEOL(fw);

               if (!newSync)
               {
                  writeString(fw, "cl:sync.new()");
                  writeEOL(fw);
                  newSync = true;
               }
               else
               {
                  writeString(fw, "cl:sync.register()");
                  writeEOL(fw);
               }

               writeString(fw, "sync:tx.registerInterposed()");
               writeEOL(fw);

               break;
            case TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER:
            case TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:
               writeString(fw, "cl:tx." + TraceEvent.asText(te));
               writeEOL(fw);

               break;
            case TraceEvent.DELIST_CONNECTION_LISTENER:
            case TraceEvent.DELIST_CONNECTION_LISTENER_FAILED:

               writeString(fw, "[c beforeCompletion]");
               writeEOL(fw);

               writeString(fw, "cl:>sync.<free>");
               writeEOL(fw);

               writeString(fw, "sync:tx." + TraceEvent.asText(te));
               writeEOL(fw);

               writeString(fw, "[/c]");
               writeEOL(fw);

               break;
            case TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER:
            case TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:

               writeString(fw, "cl:tx." + TraceEvent.asText(te));
               writeEOL(fw);

               break;
            case TraceEvent.DELIST_ROLLEDBACK_CONNECTION_LISTENER:

               writeString(fw, "[c afterCompletion]");
               writeEOL(fw);

               writeString(fw, "cl:>sync.<rollback>");
               writeEOL(fw);

               writeString(fw, "sync:tx." + TraceEvent.asText(te));
               writeEOL(fw);

               writeString(fw, "[/c]");
               writeEOL(fw);

               break;
            case TraceEvent.GET_CONNECTION:
               writeString(fw, "application:cl." + TraceEvent.asText(te));
               writeEOL(fw);

               break;
            case TraceEvent.RETURN_CONNECTION:
               if (TraceEventHelper.hasMoreApplicationEvents(events, i + 1))
               {
                  writeString(fw, "application:cl." + TraceEvent.asText(te));
               }
               else
               {
                  writeString(fw, "application:>cl." + TraceEvent.asText(te));
               }

               writeEOL(fw);

               break;
            case TraceEvent.CLEAR_CONNECTION:
               break;
            case TraceEvent.EXCEPTION:
               break;
            case TraceEvent.CREATE_CONNECTION_LISTENER_GET:
               break;
            case TraceEvent.CREATE_CONNECTION_LISTENER_PREFILL:
               break;
            case TraceEvent.CREATE_CONNECTION_LISTENER_INCREMENTER:
               break;
            case TraceEvent.DESTROY_CONNECTION_LISTENER_RETURN:
               break;
            case TraceEvent.DESTROY_CONNECTION_LISTENER_IDLE:
               break;
            case TraceEvent.DESTROY_CONNECTION_LISTENER_INVALID:
               break;
            case TraceEvent.DESTROY_CONNECTION_LISTENER_FLUSH:
               break;
            case TraceEvent.DESTROY_CONNECTION_LISTENER_ERROR:
               break;
            case TraceEvent.MANAGED_CONNECTION_POOL_CREATE:
               break;
            case TraceEvent.MANAGED_CONNECTION_POOL_DESTROY:
               break;
            default:
               System.err.println("SDeditGenerator: Unknown code: " + te);
         }
      }
   }
}
