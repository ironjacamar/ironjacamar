/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
package org.jboss.jca.adapters.jdbc.util;

import java.util.function.Predicate;

/**
 * Interface that specifies a policy for caches. <p>
 * Implementation classes can implement a LRU policy, a random one, 
 * a MRU one, or any other suitable policy.
 * 
 * @param <K> The key
 * @param <V> The value
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 */
public interface Cache<K, V>
{
   /**
    * Returns the object paired with the specified key if it's 
    * present in the cache, otherwise must return null. <br>
    * Implementations of this method must have complexity of order O(1).
    * Differently from {@link #peek} this method not only return whether
    * the object is present in the cache or not, but also 
    * applies the implemented policy that will "refresh" the cached 
    * object in the cache, because this cached object
    * was really requested.
    * 
    * @param key the key paired with the object
    * @return the object
    * @see #peek
    */
   public V get(K key);

   /**
    * Returns the object paired with the specified key if it's 
    * present in the cache, otherwise must return null. <br>
    * Implementations of this method must have complexity of order O(1).
    * This method should not apply the implemented caching policy to the 
    * object paired with the given key, so that a client can 
    * query if an object is cached without "refresh" its cache status. Real 
    * requests for the object must be done using {@link #get}.
    * 
    * @param key the key paired with the object
    * @see #get
    * @return the object
    */
   public V peek(K key);
   
   /**
    * Inserts the specified object into the cache following the 
    * implemented policy. <br>
    * Implementations of this method must have complexity of order O(1).
    * 
    * @param key the key paired with the object
    * @param object the object to cache
    * @see #remove
    */
   public void insert(K key, V object);
   
   /**
    * Remove the cached object paired with the specified key. <br>
    * Implementations of this method must have complexity of order O(1).
    * 
    * @param key the key paired with the object
    * @see #insert
    */
   public void remove(K key);
   
   /**
    * Flushes the cached objects from the cache.
    */
   public void flush();


   public void flush(Predicate<V> predicate);
   /**
    * The cache size
    * @return the size of the cache
    */
   public int size();

   /**
    * Set the cache listener
    * @param listener The listener
    */
   public void setListener(CacheListener<V> listener);
}
