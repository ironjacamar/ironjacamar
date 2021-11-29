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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
   public static List<TraceEvent> filterPoolEvents(List<TraceEvent> data) throws Exception
   {
      List<TraceEvent> result = new ArrayList<TraceEvent>();

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
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_PREFILL ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_INCREMENTER ||
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
         
         result.add(te);
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
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_PREFILL ||
             te.getType() == TraceEvent.DESTROY_CONNECTION_LISTENER_INCREMENTER ||
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
    * Filter the CCM events
    * @param data The data
    * @return The filtered events
    * @exception Exception If an error occurs
    */
   public static List<TraceEvent> filterCCMEvents(List<TraceEvent> data) throws Exception
   {
      List<TraceEvent> result = new ArrayList<TraceEvent>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.PUSH_CCM_CONTEXT ||
             te.getType() == TraceEvent.POP_CCM_CONTEXT)
         {
            result.add(te);
         }
      }

      return result;
   }

   /**
    * Filter the CCM pool events
    * @param data The data
    * @return The filtered events
    * @exception Exception If an error occurs
    */
   public static Map<String, List<TraceEvent>> filterCCMPoolEvents(List<TraceEvent> data) throws Exception
   {
      Map<String, List<TraceEvent>> result = new TreeMap<String, List<TraceEvent>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.REGISTER_CCM_CONNECTION ||
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
    * Pool to Managed Connection Pools mapping
    * @param data The data
    * @return The mapping
    * @exception Exception If an error occurs
    */
   public static Map<String, Set<String>> poolManagedConnectionPools(List<TraceEvent> data) throws Exception
   {
      Map<String, Set<String>> result = new TreeMap<String, Set<String>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.GET_CONNECTION_LISTENER ||
             te.getType() == TraceEvent.GET_CONNECTION_LISTENER_NEW ||
             te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER ||
             te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW)
         {
            Set<String> s = result.get(te.getPool());

            if (s == null)
               s = new TreeSet<String>();

            s.add(te.getManagedConnectionPool());

            result.put(te.getPool(), s);
         }
      }

      return result;
   }

   /**
    * ToC: Connections
    * @param data The data
    * @return The events
    * @exception Exception If an error occurs
    */
   public static Map<String, List<TraceEvent>> tocConnections(List<TraceEvent> data) throws Exception
   {
      Map<String, List<TraceEvent>> result = new TreeMap<String, List<TraceEvent>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.GET_CONNECTION)
         {
            List<TraceEvent> l = result.get(te.getPayload1());

            if (l == null)
               l = new ArrayList<TraceEvent>();

            l.add(te);

            result.put(te.getPayload1(), l);
         }
      }

      return result;
   }

   /**
    * ToC: Managed connections
    * @param data The data
    * @return The events
    * @exception Exception If an error occurs
    */
   public static Map<String, TraceEvent> tocManagedConnections(List<TraceEvent> data) throws Exception
   {
      Map<String, TraceEvent> result = new TreeMap<String, TraceEvent>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_GET ||
             te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_PREFILL ||
             te.getType() == TraceEvent.CREATE_CONNECTION_LISTENER_INCREMENTER)
         {
            result.put(te.getPayload1(), te);
         }
      }

      return result;
   }

   /**
    * ToC: Connection listeners
    * @param data The data
    * @return The events
    * @exception Exception If an error occurs
    */
   public static Map<String, List<TraceEvent>> tocConnectionListeners(List<TraceEvent> data) throws Exception
   {
      Map<String, List<TraceEvent>> result = new TreeMap<String, List<TraceEvent>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.GET_CONNECTION_LISTENER ||
             te.getType() == TraceEvent.GET_CONNECTION_LISTENER_NEW ||
             te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER ||
             te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW)
         {
            List<TraceEvent> l = result.get(te.getConnectionListener());

            if (l == null)
               l = new ArrayList<TraceEvent>();

            l.add(te);

            result.put(te.getConnectionListener(), l);
         }
      }

      return result;
   }

   /**
    * ToC: Managed Connection Pools
    * @param data The data
    * @return The events
    * @exception Exception If an error occurs
    */
   public static Map<String, List<TraceEvent>> tocManagedConnectionPools(List<TraceEvent> data) throws Exception
   {
      Map<String, List<TraceEvent>> result = new TreeMap<String, List<TraceEvent>>();

      for (TraceEvent te : data)
      {
         if (te.getType() == TraceEvent.GET_CONNECTION_LISTENER ||
             te.getType() == TraceEvent.GET_CONNECTION_LISTENER_NEW ||
             te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER ||
             te.getType() == TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW)
         {
            List<TraceEvent> l = result.get(te.getManagedConnectionPool());

            if (l == null)
               l = new ArrayList<TraceEvent>();

            l.add(te);

            result.put(te.getManagedConnectionPool(), l);
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
    * @param ignoreIncomplete Ignore incomplete traces
    * @return The overall result
    */
   public static Map<String, TraceEventStatus> getStatus(Map<String, List<TraceEvent>> input,
                                                         boolean ignoreDelist, boolean ignoreTracking,
                                                         boolean ignoreIncomplete)
   {
      Map<String, TraceEventStatus> result = new TreeMap<String, TraceEventStatus>();

      Iterator<Map.Entry<String, List<TraceEvent>>> it = input.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry<String, List<TraceEvent>> entry = it.next();
         result.put(entry.getKey(), getStatus(entry.getValue(), ignoreDelist, ignoreTracking, ignoreIncomplete));
      }

      return result;
   }

   /**
    * Get status
    * @param data The data
    * @param ignoreDelist Should DELIST be ignored
    * @param ignoreTracking Should TRACKING be ignored
    * @param ignoreIncomplete Ignore incomplete traces
    * @return The status
    */
   public static TraceEventStatus getStatus(List<TraceEvent> data, boolean ignoreDelist, boolean ignoreTracking,
                                            boolean ignoreIncomplete)
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
               if (!inTx)
                  explicit = TraceEventStatus.RED;

               inTx = false;

               if (!ignoreTracking && knownConnections.size() > 0 && explicit != TraceEventStatus.RED)
                  explicit = TraceEventStatus.YELLOW;

               break;

            case TraceEvent.DELIST_ROLLEDBACK_CONNECTION_LISTENER:
               if (!inTx)
                  explicit = TraceEventStatus.RED;

               inTx = false;

               if (!ignoreTracking && explicit != TraceEventStatus.RED)
                  explicit = TraceEventStatus.YELLOW;

               break;

            case TraceEvent.DELIST_ROLLEDBACK_CONNECTION_LISTENER_FAILED:
               if (!inTx)
                  explicit = TraceEventStatus.RED;

               inTx = false;

               if (!ignoreTracking)
                  explicit = TraceEventStatus.RED;

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
            case TraceEvent.DESTROY_CONNECTION_LISTENER_PREFILL:
               break;
            case TraceEvent.DESTROY_CONNECTION_LISTENER_INCREMENTER:
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
   public static TraceEventStatus mergeStatus(Collection<TraceEventStatus> data)
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
    * Get the structured pool data
    * @param data The data
    * @param ignoreDelist Should DELIST be ignored
    * @param ignoreTracking Should TRACKING be ignored
    * @param ignoreIncomplete Ignore incomplete traces
    * @return The result
    */
   public static Map<String, List<Interaction>> getPoolData(List<TraceEvent> data,
                                                            boolean ignoreDelist, boolean ignoreTracking,
                                                            boolean ignoreIncomplete)
   {
      // Pool -> Interactions
      Map<String, List<Interaction>> result = new TreeMap<String, List<Interaction>>();
      
      // Pool -> ConnectionListener -> Events
      Map<String, Map<String, List<TraceEvent>>> temp = new TreeMap<String, Map<String, List<TraceEvent>>>();

      for (int i = 0; i < data.size(); i++)
      {
         TraceEvent te = data.get(i);

         Map<String, List<TraceEvent>> m = temp.get(te.getPool());

         if (m == null)
            m = new TreeMap<String, List<TraceEvent>>();

         List<TraceEvent> l = m.get(te.getConnectionListener());

         if (l == null)
            l = new ArrayList<TraceEvent>();

         l.add(te);

         if (isEndState(te))
         {
            Interaction interaction = new Interaction(te.getThreadId(),
                                                      l.get(0).getTimestamp(),
                                                      l.get(l.size() - 1).getTimestamp(),
                                                      l,
                                                      getStatus(l, ignoreDelist, ignoreTracking, ignoreIncomplete));
            List<Interaction> pool = result.get(te.getPool());

            if (pool == null)
               pool = new ArrayList<Interaction>();

            pool.add(interaction);
            result.put(te.getPool(), pool);
            l = null;
         }
         
         m.put(te.getConnectionListener(), l);
         temp.put(te.getPool(), m);
      }

      if (!ignoreIncomplete)
      {
         for (Map.Entry<String, Map<String, List<TraceEvent>>> poolEntry : temp.entrySet())
         {
            for (Map.Entry<String, List<TraceEvent>> clEntry : poolEntry.getValue().entrySet())
            {
               List<TraceEvent> l = clEntry.getValue();

               if (l != null)
               {
                  Interaction interaction = new Interaction(l.get(0).getThreadId(),
                                                            l.get(0).getTimestamp(),
                                                            l.get(l.size() - 1).getTimestamp(),
                                                            l,
                                                            getStatus(l, ignoreDelist, ignoreTracking,
                                                                      ignoreIncomplete));
                  List<Interaction> pool = result.get(poolEntry.getKey());
               
                  if (pool == null)
                     pool = new ArrayList<Interaction>();

                  pool.add(interaction);
                  result.put(poolEntry.getKey(), pool);
               }
            }
         }
      }
      
      return result;
   }

  /**
    * Get a connection listener map
    * @param data The data
    * @return The result
    */
   public static Map<String, List<Interaction>> getConnectionListenerData(List<Interaction> data)
   {
      Map<String, List<Interaction>> result = new TreeMap<String, List<Interaction>>();

      for (int i = 0; i < data.size(); i++)
      {
         Interaction interaction = data.get(i);

         List<Interaction> l = result.get(interaction.getConnectionListener());

         if (l == null)
            l = new ArrayList<Interaction>();

         l.add(interaction);

         result.put(interaction.getConnectionListener(), l);
      }

      return result;
   }

  /**
    * Get a transaction map
    * @param data The data
    * @return The result
    */
   public static Map<String, List<Interaction>> getTransactionData(List<Interaction> data)
   {
      Map<String, List<Interaction>> result = new TreeMap<String, List<Interaction>>();

      for (int i = 0; i < data.size(); i++)
      {
         Interaction interaction = data.get(i);

         if (interaction.getTransaction() != null)
         {
            List<Interaction> l = result.get(interaction.getTransaction());

            if (l == null)
               l = new ArrayList<Interaction>();

            l.add(interaction);

            result.put(interaction.getTransaction(), l);
         }
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
    * @param encoded The encoded string
    * @return The string
    */
   public static String exceptionDescription(String encoded)
   {
      char[] data = encoded.toCharArray();
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
      if (te.getType() != TraceEvent.GET_CONNECTION_LISTENER &&
          te.getType() != TraceEvent.GET_CONNECTION_LISTENER_NEW &&
          te.getType() != TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER &&
          te.getType() != TraceEvent.GET_INTERLEAVING_CONNECTION_LISTENER_NEW &&
          te.getType() != TraceEvent.RETURN_CONNECTION_LISTENER &&
          te.getType() != TraceEvent.RETURN_CONNECTION_LISTENER_WITH_KILL &&
          te.getType() != TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER &&
          te.getType() != TraceEvent.RETURN_INTERLEAVING_CONNECTION_LISTENER_WITH_KILL &&
          te.getType() != TraceEvent.CREATE_CONNECTION_LISTENER_GET &&
          te.getType() != TraceEvent.CREATE_CONNECTION_LISTENER_PREFILL &&
          te.getType() != TraceEvent.CREATE_CONNECTION_LISTENER_INCREMENTER &&
          te.getType() != TraceEvent.DESTROY_CONNECTION_LISTENER_RETURN &&
          te.getType() != TraceEvent.DESTROY_CONNECTION_LISTENER_IDLE &&
          te.getType() != TraceEvent.DESTROY_CONNECTION_LISTENER_INVALID &&
          te.getType() != TraceEvent.DESTROY_CONNECTION_LISTENER_FLUSH &&
          te.getType() != TraceEvent.DESTROY_CONNECTION_LISTENER_ERROR &&
          te.getType() != TraceEvent.DESTROY_CONNECTION_LISTENER_PREFILL &&
          te.getType() != TraceEvent.DESTROY_CONNECTION_LISTENER_INCREMENTER &&
          te.getType() != TraceEvent.EXCEPTION &&
          te.getType() != TraceEvent.PUSH_CCM_CONTEXT &&
          te.getType() != TraceEvent.POP_CCM_CONTEXT)
         return te.toString();

      StringBuilder sb = new StringBuilder();
      sb.append("IJTRACER");
      sb.append("-");
      sb.append(te.getPool());
      sb.append("-");
      sb.append(te.getManagedConnectionPool());
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
    * Get CCM pool status
    * @param data The data
    * @param ignoreIncomplete Ignore incomplete stacks
    * @return The status
    */
   public static TraceEventStatus getCCMStatus(List<TraceEvent> data, boolean ignoreIncomplete)
   {
      Map<Long, Deque<TraceEvent>> stacks = new TreeMap<Long, Deque<TraceEvent>>();

      for (TraceEvent te : data)
      {
         Long id = Long.valueOf(te.getThreadId());

         Deque<TraceEvent> stack = stacks.get(id);

         if (stack == null)
            stack = new ArrayDeque<TraceEvent>();
         
         if (te.getType() == TraceEvent.PUSH_CCM_CONTEXT)
         {
            stack.push(te);
         }
         else
         {
            TraceEvent top = stack.peek();
            if (top.getPayload1().equals(te.getPayload1()))
            {
               stack.pop();
            }
            else
            {
               return TraceEventStatus.RED;
            }
         }

         stacks.put(id, stack);
      }

      for (Deque<TraceEvent> entry : stacks.values())
      {
         if (!ignoreIncomplete && !entry.isEmpty())
            return TraceEventStatus.YELLOW;
      }
      
      return TraceEventStatus.GREEN;
   }

   /**
    * Get CCM pool status
    * @param data The data
    * @param ignoreIncomplete Ignore incomplete stacks
    * @return The status
    */
   public static TraceEventStatus getCCMPoolStatus(List<TraceEvent> data, boolean ignoreIncomplete)
   {
      // Thread -> Connection Listener -> Set<Connection>
      Map<Long, Map<String, Set<String>>> threads = new HashMap<Long, Map<String, Set<String>>>();
      TraceEventStatus status = TraceEventStatus.GREEN;
      
      for (TraceEvent te : data)
      {
         Long id = Long.valueOf(te.getThreadId());

         Map<String, Set<String>> m = threads.get(id);

         if (m == null)
            m = new HashMap<String, Set<String>>();

         if (te.getType() == TraceEvent.REGISTER_CCM_CONNECTION)
         {
            Set<String> s = m.get(te.getConnectionListener());
            if (s == null)
            {
               s = new HashSet<String>();
               m.put(te.getConnectionListener(), s);
            }
            if (!s.add(te.getPayload1()))
               status = TraceEventStatus.YELLOW;
         }
         else if (te.getType() == TraceEvent.UNREGISTER_CCM_CONNECTION)
         {
            Set<String> s = m.get(te.getConnectionListener());
            if (s == null)
            {
               s = new HashSet<String>();
               m.put(te.getConnectionListener(), s);
            }
            if (!s.remove(te.getPayload1()))
               status = TraceEventStatus.YELLOW;
         }
         else if (te.getType() == TraceEvent.UNKNOWN_CCM_CONNECTION)
         {
            return TraceEventStatus.RED;
         }
         else if (te.getType() == TraceEvent.CLOSE_CCM_CONNECTION)
         {
            status = TraceEventStatus.YELLOW;
         }

         threads.put(id, m);
      }

      for (Map.Entry<Long, Map<String, Set<String>>> t : threads.entrySet())
      {
         for (Map.Entry<String, Set<String>> m : t.getValue().entrySet())
         {
            if (!ignoreIncomplete && m.getValue().size() > 0)
               return TraceEventStatus.RED;
         }
      }

      return status;
   }

   /**
    * Get a specific event type
    * @param events The events
    * @param types The types
    * @return The first event type found; otherwise <code>null</code> if none
    */
   public static TraceEvent getType(List<TraceEvent> events, int... types)
   {
      return getType(events, null, types);
   }

   /**
    * Get a specific event type
    * @param events The events
    * @param identifier The connection listener
    * @param types The types
    * @return The first event type found; otherwise <code>null</code> if none
    */
   public static TraceEvent getType(List<TraceEvent> events, String identifier, int... types)
   {
      for (TraceEvent te : events)
      {
         for (int type : types)
         {
            if (te.getType() == type && (identifier == null || te.getConnectionListener().equals(identifier)))
               return te;
         }
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
