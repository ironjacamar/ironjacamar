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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Helper class for TraceEvent
 */
public class TraceEventHelper
{
   /**
    * Filter the pool events
    * @param data The data
    * @return The filtered events
    * @exception Exception If an error occurs
    */
   public static Map<String, Map<String, List<TraceEvent>>> filterPoolEvents(List<TraceEvent> data) throws Exception
   {
      Map<String, Map<String, List<TraceEvent>>> result = new TreeMap<String, Map<String, List<TraceEvent>>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_GET ||
             te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_PREFILL ||
             te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_INCREMENTER ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_RETURN ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_IDLE ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_INVALID ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_FLUSH ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_ERROR ||
             te.getType() == TraceEvent.MANAGED_CONNECTION_POOL_CREATE ||
             te.getType() == TraceEvent.MANAGED_CONNECTION_POOL_DESTROY ||
             te.getType() == TraceEvent.PUSH_CCM_CONTEXT ||
             te.getType() == TraceEvent.POP_CCM_CONTEXT ||
             te.getType() == TraceEvent.REGISTER_CCM_CONNECTION ||
             te.getType() == TraceEvent.UNREGISTER_CCM_CONNECTION ||
             te.getType() == TraceEvent.CCM_USER_TRANSACTION ||
             te.getType() == TraceEvent.UNKNOWN_CCM_CONNECTION ||
             te.getType() == TraceEvent.CLOSE_CCM_CONNECTION ||
             te.getType() == TraceEvent.VERSION)
            continue;
         
         Map<String, List<TraceEvent>> m = result.get(te.getPool());

         if (m == null)
            m = new TreeMap<String, List<TraceEvent>>();

         List<TraceEvent> l = m.get(te.getConnectionListener());

         if (l == null)
            l = new ArrayList<TraceEvent>();

         l.add(te);

         m.put(te.getConnectionListener(), l);

         result.put(te.getPool(), m);
      }

      return result;
   }

   /**
    * Filter the lifecycle events
    * @param data The data
    * @return The filtered events
    * @exception Exception If an error occurs
    */
   public static Map<String, List<TraceEvent>> filterLifecycleEvents(List<TraceEvent> data) throws Exception
   {
      Map<String, List<TraceEvent>> result = new TreeMap<String, List<TraceEvent>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_GET ||
             te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_PREFILL ||
             te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_INCREMENTER ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_RETURN ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_IDLE ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_INVALID ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_FLUSH ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_ERROR ||
             te.getType() == TraceEvent.MANAGED_CONNECTION_POOL_CREATE ||
             te.getType() == TraceEvent.MANAGED_CONNECTION_POOL_DESTROY)
         {
            List<TraceEvent> l = result.get(te.getPool());

            if (l == null)
               l = new ArrayList<TraceEvent>();

            l.add(te);

            result.put(te.getPool(), l);
         }
      }

      return result;
   }

   /**
    * Filter the ccm events
    * @param data The data
    * @return The filtered events
    * @exception Exception If an error occurs
    */
   public static Map<String, List<TraceEvent>> filterCCMEvents(List<TraceEvent> data) throws Exception
   {
      Map<String, List<TraceEvent>> result = new TreeMap<String, List<TraceEvent>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.PUSH_CCM_CONTEXT ||
             te.getType() == TraceEvent.POP_CCM_CONTEXT ||
             te.getType() == TraceEvent.REGISTER_CCM_CONNECTION ||
             te.getType() == TraceEvent.UNREGISTER_CCM_CONNECTION ||
             te.getType() == TraceEvent.CCM_USER_TRANSACTION ||
             te.getType() == TraceEvent.UNKNOWN_CCM_CONNECTION ||
             te.getType() == TraceEvent.CLOSE_CCM_CONNECTION)
         {
            List<TraceEvent> l = result.get(te.getPool());

            if (l == null)
               l = new ArrayList<TraceEvent>();

            l.add(te);

            result.put(te.getPool(), l);
         }
      }

      return result;
   }

   /**
    * Get the events
    * @param fr The file reader
    * @param directory The directory
    * @return The events
    * @exception Exception If an error occurs
    */
   public static List<TraceEvent> getEvents(FileReader fr, File directory) throws Exception
   {
      return getEvents(getData(fr, directory));
   }

   /**
    * Get status
    * @param input The input
    * @param ignoreDelist Should DELIST be ignored
    * @param ignoreTracking Should TRACKING be ignored
    * @return The overall result
    */
   public static Map<String, TraceEventStatus> getStatus(Map<String, List<TraceEvent>> input,
                                                         boolean ignoreDelist, boolean ignoreTracking)
   {
      Map<String, TraceEventStatus> result = new TreeMap<String, TraceEventStatus>();

      Iterator<Map.Entry<String, List<TraceEvent>>> it = input.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<String, List<TraceEvent>> entry = it.next();
         result.put(entry.getKey(), getStatus(entry.getValue(), ignoreDelist, ignoreTracking));
      }

      return result;
   }

   /**
    * Get status
    * @param data The data
    * @param ignoreDelist Should DELIST be ignored
    * @param ignoreTracking Should TRACKING be ignored
    * @return The status
    */
   public static TraceEventStatus getStatus(List<TraceEvent> data, boolean ignoreDelist, boolean ignoreTracking)
   {
      TraceEventStatus explicit = null;
      Set<String> knownConnections = new HashSet<String>();
      boolean gotCl = false;
      boolean inTx = false;
      boolean gotClear = false;
      
      for (TraceEvent te : data)
      {
         switch (te.getType())
         {
            case TraceEvent.GET_CONNECTION_LISTENER:
            case TraceEvent.GET_CONNECTION_LISTENER_NEW:
            case TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER:
            case TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW:
               if (gotCl)
                  explicit = TraceEventStatus.RED;

               gotCl = true;
               break;

            case TraceEvent.RETURN_CONNECTION_LISTENER:
            case TraceEvent.RETURN_CONNECTION_LISTENER_WITH_KILL:
            case TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER:
            case TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL:
               if (!gotCl)
                  explicit = TraceEventStatus.RED;

               gotCl = false;
               break;

            case TraceEvent.CLEAR_CONNECTION_LISTENER:
               explicit = TraceEventStatus.RED;
               break;

            case TraceEvent.ENLIST_CONNECTION_LISTENER:
               if (inTx)
                  explicit = TraceEventStatus.RED;

               inTx = true;
               break;

            case TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER:
               if (inTx)
                  explicit = TraceEventStatus.YELLOW;

               inTx = true;
               break;

            case TraceEvent.ENLIST_CONNECTION_LISTENER_FAILED:
            case TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:
               if (inTx)
               {
                  explicit = TraceEventStatus.RED;
               }
               else
               {
                  explicit = TraceEventStatus.YELLOW;
               }

               inTx = true;
               break;

            case TraceEvent.DELIST_CONNECTION_LISTENER:
            case TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER:
            case TraceEvent.DELIST_ROLLEDBACK_CONNECTION_LISTENER:
               if (!inTx)
                  explicit = TraceEventStatus.RED;

               inTx = false;

               if (!ignoreTracking && knownConnections.size() > 0 && explicit != TraceEventStatus.RED)
                  explicit = TraceEventStatus.YELLOW;

               break;

            case TraceEvent.DELIST_CONNECTION_LISTENER_FAILED:
            case TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:
               if (!inTx)
               {
                  explicit = TraceEventStatus.RED;
               }
               else
               {
                  explicit = TraceEventStatus.YELLOW;
               }

               inTx = false;

               if (!ignoreTracking && knownConnections.size() > 0 && explicit != TraceEventStatus.RED)
                  explicit = TraceEventStatus.YELLOW;

               break;

            case TraceEvent.GET_CONNECTION:
               if (!knownConnections.add(te.getPayload1()))
                  explicit = TraceEventStatus.RED;

               break;
            case TraceEvent.RETURN_CONNECTION:
               if (!knownConnections.remove(te.getPayload1()))
                  explicit = TraceEventStatus.RED;

               break;

            case TraceEvent.CLEAR_CONNECTION:
               gotClear = true;

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
            case TraceEvent.REGISTER_CCM_CONNECTION:
               break;
            case TraceEvent.UNREGISTER_CCM_CONNECTION:
               break;

            default:
               System.err.println("TraceEventHelper: Unknown code: " + te);
         }
      }

      if (explicit != null)
         return explicit;

      if (gotCl)
         return TraceEventStatus.RED;

      if (inTx && !ignoreDelist)
         return TraceEventStatus.RED;

      if (knownConnections.size() > 0)
         return TraceEventStatus.RED;

      if (gotClear)
         return TraceEventStatus.RED;

      return TraceEventStatus.GREEN;
   }

   /**
    * Get status
    * @param data The data
    * @return The status
    */
   public static TraceEventStatus mergeStatus(List<TraceEventStatus> data)
   {
      TraceEventStatus result = TraceEventStatus.GREEN;

      for (TraceEventStatus tes : data)
      {
         if (tes == TraceEventStatus.YELLOW)
         {
            result = TraceEventStatus.YELLOW;
         }
         else if (tes == TraceEventStatus.RED)
         {
            return TraceEventStatus.RED;
         }
      }

      return result;
   }

   /**
    * Is start state
    * @param te The event
    * @return The value
    */
   public static boolean isStartState(TraceEvent te)
   {
      if (te.getType() == TraceEvent.GET_CONNECTION_LISTENER ||
          te.getType() == TraceEvent.GET_CONNECTION_LISTENER_NEW ||
          te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER ||
          te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW ||
          te.getType() == TraceEvent.CLEAR_CONNECTION_LISTENER)
         return true;

      return false;
   }

   /**
    * Is end state
    * @param te The event
    * @return The value
    */
   public static boolean isEndState(TraceEvent te)
   {
      if (te.getType() == TraceEvent.RETURN_CONNECTION_LISTENER ||
          te.getType() == TraceEvent.RETURN_CONNECTION_LISTENER_WITH_KILL ||
          te.getType() == TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER ||
          te.getType() == TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL ||
          te.getType() == TraceEvent.CLEAR_CONNECTION_LISTENER)
         return true;

      return false;
   }

   /**
    * Is red
    * @param te The event
    * @return The value
    */
   public static boolean isRed(TraceEvent te)
   {
      if (te.getType() == TraceEvent.CLEAR_CONNECTION ||
          te.getType() == TraceEvent.CLEAR_CONNECTION_LISTENER)
         return true;

      return false;
   }

   /**
    * Is yellow
    * @param te The event
    * @return The value
    */
   public static boolean isYellow(TraceEvent te)
   {
      return false;
   }

   /**
    * Split a connection listener events into their lifecycle
    * @param data The data
    * @return The result
    */
   public static Map<String, List<TraceEvent>> split(List<TraceEvent> data)
   {
      Map<String, List<TraceEvent>> result = new TreeMap<String, List<TraceEvent>>();
      long start = 0L;
      List<TraceEvent> l = new ArrayList<TraceEvent>(); 

      for (int i = 0; i < data.size(); i++)
      {
         TraceEvent te = data.get(i);
         l.add(te);

         if (start == 0L)
            start = te.getTimestamp();

         if (isEndState(te))
         {
            result.put(Long.toString(start) + "-" + Long.toString(te.getTimestamp()), l);

            start = 0L;
            l = new ArrayList<TraceEvent>();
         }
      }

      if (l.size() > 0)
      {
         result.put(Long.toString(start) + "-" + Long.toString(l.get(l.size() - 1).getTimestamp()), l);
      }

      return result;
   }

   /**
    * Has an exception event
    * @param events The events
    * @return True if there is an exception
    */
   public static boolean hasException(List<TraceEvent> events)
   {
      for (TraceEvent te : events)
      {
         if (te.getType() == TraceEvent.EXCEPTION)
            return true;
      }

      return false;
   }

   /**
    * Get exception description
    * @param te The event
    * @return The string
    */
   public static String exceptionDescription(TraceEvent te)
   {
      if (te.getType() != TraceEvent.EXCEPTION)
         return "";

      char[] data = te.getPayload1().toCharArray();
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < data.length; i++)
      {
         char c = data[i];
         if (c == '|')
         {
            sb = sb.append('\n');
         }
         else if (c == '/')
         {
            sb = sb.append('\r');
         }
         else if (c == '\\')
         {
            sb = sb.append('\t');
         }
         else if (c == '_')
         {
            sb = sb.append(' ');
         }
         else
         {
            sb = sb.append(c);
         }
      }

      return sb.toString();
   }

   /**
    * Pretty print event
    * @param te The event
    * @return The string
    */
   public static String prettyPrint(TraceEvent te)
   {
      if (te.getType() != TraceEvent.EXCEPTION)
         return te.toString();

      StringBuilder sb = new StringBuilder();
      sb.append("IJTRACER");
      sb.append("-");
      sb.append(te.getPool());
      sb.append("-");
      sb.append(te.getThreadId());
      sb.append("-");
      sb.append(te.getType());
      sb.append("-");
      sb.append(te.getTimestamp());
      sb.append("-");
      sb.append(te.getConnectionListener());
      sb.append("-");
      sb.append("DATA");
      return sb.toString();
   }

   /**
    * Get the version
    * @param events The events
    * @return The version information
    */
   public static TraceEvent getVersion(List<TraceEvent> events)
   {
      for (TraceEvent te : events)
      {
         if (te.getType() == TraceEvent.VERSION)
            return te;
      }

      return null;
   }

   /**
    * Has more application events
    * @param events The events
    * @param index The index
    * @return True if more application events after the index
    */
   static boolean hasMoreApplicationEvents(List<TraceEvent> events, int index)
   {
      if (index < 0 || index >= events.size())
         return false;

      for (int j = index; j < events.size(); j++)
      {
         TraceEvent te = events.get(j);
         if (te.getType() == TraceEvent.GET_CONNECTION ||
             te.getType() == TraceEvent.RETURN_CONNECTION)
            return true;
      }

      return false;
   }

   /**
    * Get data
    * @param fr The file reader
    * @param directory The directory
    * @return The data
    * @exception Exception If an error occurs
    */
   private static List<String> getData(FileReader fr, File directory) throws Exception
   {
      List<String> result = new ArrayList<String>();

      LineNumberReader r = new LineNumberReader(fr);
      String s = r.readLine();
      while (s != null)
      {
         if (s.indexOf("IJTRACER") != -1)
         {
            result.add(s.substring(s.indexOf("IJTRACER")));
         }
 
         s = r.readLine();
      }

      FileWriter fw = null;
      try
      {
         fw = new FileWriter(directory.getAbsolutePath() + "/" + "raw.txt");
         for (String data : result)
         {
            HTMLReport.writeString(fw, data);
            HTMLReport.writeEOL(fw);
         }
      }
      finally
      {
         if (fw != null)
         {
            try
            {
               fw.flush();
               fw.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }

      
      return result;
   }

   /**
    * Get events
    * @param data The data
    * @return The events
    * @exception Exception If an error occurs
    */
   private static List<TraceEvent> getEvents(List<String> data) throws Exception
   {
      List<TraceEvent> result = new ArrayList<TraceEvent>();

      for (String s : data)
      {
         result.add(TraceEvent.parse(s));
      }

      return result;
   }
}
