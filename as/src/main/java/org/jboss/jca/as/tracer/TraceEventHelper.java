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

import java.io.FileReader;
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
    * Filter the events
    * @param data The data
    * @return The filtered events
    * @exception Exception If an error occurs
    */
   public static Map<String, Map<String, List<TraceEvent>>> filterEvents(List<TraceEvent> data) throws Exception
   {
      Map<String, Map<String, List<TraceEvent>>> result = new TreeMap<String, Map<String, List<TraceEvent>>>();

      for (TraceEvent te : data)
      {
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
    * Get the events
    * @param fr The file reader
    * @return The events
    * @exception Exception If an error occurs
    */
   public static List<TraceEvent> getEvents(FileReader fr) throws Exception
   {
      return getEvents(getData(fr));
   }

   /**
    * Get status
    * @param input The input
    * @return The overall result
    */
   public static Map<String, TraceEventStatus> getStatus(Map<String, List<TraceEvent>> input)
   {
      Map<String, TraceEventStatus> result = new TreeMap<String, TraceEventStatus>();

      Iterator<Map.Entry<String, List<TraceEvent>>> it = input.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<String, List<TraceEvent>> entry = it.next();

         result.put(entry.getKey(), getStatus(entry.getValue()));
      }

      return result;
   }

   /**
    * Get status
    * @param data The data
    * @return The status
    */
   public static TraceEventStatus getStatus(List<TraceEvent> data)
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
            case TraceEvent.ENLIST_CONNECTION_LISTENER_FAILED:
            case TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER:
            case TraceEvent.ENLIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:
               if (inTx)
                  explicit = TraceEventStatus.RED;

               inTx = true;
               break;

            case TraceEvent.DELIST_CONNECTION_LISTENER:
            case TraceEvent.DELIST_CONNECTION_LISTENER_FAILED:
            case TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER:
            case TraceEvent.DELIST_INTERLEAVING_CONNECTION_LISTENER_FAILED:
               if (!inTx)
                  explicit = TraceEventStatus.RED;

               inTx = false;
               break;

            case TraceEvent.GET_CONNECTION:
               knownConnections.add(te.getConnection());

               break;
            case TraceEvent.RETURN_CONNECTION:
               knownConnections.remove(te.getConnection());

               break;

            case TraceEvent.CLEAR_CONNECTION:
               gotClear = true;

               break;

            default:
               System.err.println("TraceEventHelper: Unknown code: " + te);
         }
      }

      if (explicit != null)
         return explicit;

      if (gotCl)
         return TraceEventStatus.RED;

      if (inTx)
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
    * @return The data
    * @exception Exception If an error occurs
    */
   private static List<String> getData(FileReader fr) throws Exception
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
