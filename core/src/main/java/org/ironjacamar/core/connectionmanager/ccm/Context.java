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

package org.ironjacamar.core.connectionmanager.ccm;

import org.ironjacamar.core.connectionmanager.ConnectionManager;
import org.ironjacamar.core.connectionmanager.listener.ConnectionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A CCM context which hold the context key, and the associated
 * ConnectionManager vs. ConnectionListener mappings
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
class Context
{
   /** The context key */
   private final Object contextKey;

   /** The connection manager to connection listener mapping */
   private Map<ConnectionManager, List<ConnectionListener>> cmToCl;
   
   /** The connection listener to connection mapping */
   private Map<ConnectionListener, List<Object>> clToC;
   
   /**
    * Constructor
    * @param contextKey The context key
    */
   Context(final Object contextKey)
   {
      this.contextKey = contextKey;
      this.cmToCl = null;
      this.clToC = null;
   }

   /**
    * Register a connection
    * @param cm The connection manager
    * @param cl The connection listener
    * @param c The connection
    */
   void registerConnection(ConnectionManager cm, ConnectionListener cl, Object c)
   {
      if (cmToCl == null)
         cmToCl = new HashMap<ConnectionManager, List<ConnectionListener>>();

      List<ConnectionListener> l = cmToCl.get(cm);

      if (l == null)
         l = new ArrayList<ConnectionListener>(1);

      l.add(cl);
      cmToCl.put(cm, l);

      if (clToC == null)
         clToC = new HashMap<ConnectionListener, List<Object>>();

      List<Object> connections = clToC.get(cl);

      if (connections == null)
         connections = new ArrayList<Object>(1);

      connections.add(c);
      clToC.put(cl, connections);
   }

   /**
    * Unregister a connection
    * @param cm The connection manager
    * @param cl The connection listener
    * @param c The connection
    * @return True if the connection was unregistered, otherwise false
    */
   boolean unregisterConnection(ConnectionManager cm, ConnectionListener cl, Object c)
   {
      if (clToC != null && clToC.get(cl) != null)
      {
         List<Object> l = clToC.get(cl);
         return l.remove(c);
      }

      return false;
   }

   /**
    * Get the connection managers
    * @return The value
    */
   Set<ConnectionManager> getConnectionManagers()
   {
      if (cmToCl == null)
         return Collections.unmodifiableSet(Collections.emptySet());

      return Collections.unmodifiableSet(cmToCl.keySet());
   }

   /**
    * Get the connection listeners for a connection manager
    * @param cm The connection manager
    * @return The value
    */
   List<ConnectionListener> getConnectionListeners(ConnectionManager cm)
   {
      if (cmToCl == null)
         return Collections.unmodifiableList(Collections.emptyList());

      List<ConnectionListener> l = cmToCl.get(cm);
      if (l == null)
         l = Collections.emptyList();
      
      return Collections.unmodifiableList(l);
   }
   
   /**
    * Get the connections for a connection listener
    * @param cl The connection listener
    * @return The value
    */
   List<Object> getConnections(ConnectionListener cl)
   {
      List<Object> l = null;

      if (clToC != null)
         l = clToC.get(cl);

      if (l == null)
         l = Collections.emptyList();
         
      return Collections.unmodifiableList(l);
   }
   
   /**
    * Switch the connection listener for a connection
    * @param c The connection
    * @param from The from connection listener
    * @param to The to connection listener
    */
   void switchConnectionListener(Object c, ConnectionListener from, ConnectionListener to)
   {
      if (clToC != null && clToC.get(from) != null && clToC.get(to) != null)
      {
         clToC.get(from).remove(c);
         clToC.get(to).add(c);
      }
   }
   
   /**
    * Remove a connection listener
    * @param cm The connection manager
    * @param cl The connection listener
    */
   void removeConnectionListener(ConnectionManager cm, ConnectionListener cl)
   {
      if (cmToCl != null && cmToCl.get(cm) != null)
      {
         cmToCl.get(cm).remove(cl);
         clToC.remove(cl);
      }
   }
   
   /**
    * Clear
    */
   void clear()
   {
      if (cmToCl != null)
         cmToCl.clear();

      if (clToC != null)
         clToC.clear();
   }
   
   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return System.identityHashCode(contextKey);
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      if (other == this)
         return true;

      if (other == null || !(other instanceof Context))
         return false;

      Context c = (Context)other;

      return contextKey.equals(c.contextKey);
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      // We only display the context key
      sb.append("Context[").append(Integer.toHexString(System.identityHashCode(contextKey)));
      sb.append("]");

      return sb.toString();
   }
}
