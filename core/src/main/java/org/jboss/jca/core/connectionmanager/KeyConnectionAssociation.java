/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager;

import org.jboss.jca.core.connectionmanager.listener.ConnectionCacheListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jboss.util.Strings;


/**
 * The class <code>KeyConnectionAssociation</code> wraps objects so they may be used in hashmaps
 * based on their object identity rather than equals implementation. Used for keys.
 * 
 * @author gurkanerdogdu
 * @version $Rev$ $Date$
 */
final class KeyConnectionAssociation
{
   //key
   private final Object metaAwareObject;

   //map of cm to list of connections for that cm.
   private Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap;

   /**
    * Creates a new instance.
    * @param metaAwareObject meta aware object
    */
   KeyConnectionAssociation(final Object metaAwareObject)
   {
      this.metaAwareObject = metaAwareObject;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      return (other instanceof KeyConnectionAssociation) && 
            this.metaAwareObject == ((KeyConnectionAssociation) other).metaAwareObject;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      return Strings.defaultToString(this.metaAwareObject);
   }
   
   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return System.identityHashCode(this.metaAwareObject);
   }

   /**
    * Set map instance.
    * @param cmToConnectionsMap connection manager to connections
    */
   public void setCMToConnectionsMap(Map<ConnectionCacheListener, Collection<ConnectionRecord>> cmToConnectionsMap)
   {
      this.cmToConnectionsMap = cmToConnectionsMap;
   }

   /**
    * 
    * @return map instance
    */
   public Map<ConnectionCacheListener, Collection<ConnectionRecord>> getCMToConnectionsMap()
   {
      if (cmToConnectionsMap == null)
      {
         cmToConnectionsMap = new HashMap<ConnectionCacheListener, Collection<ConnectionRecord>>();
      }
      
      return cmToConnectionsMap;
   }
}
