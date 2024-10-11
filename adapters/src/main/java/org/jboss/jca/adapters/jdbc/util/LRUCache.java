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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * Implementation of a Least Recently Used cache policy.
 *
 * @param <K> The key
 * @param <V> The value
 * @param <K> The key
 * @param <V> The value
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 */
@SuppressWarnings("unchecked")
public class LRUCache<K, V> implements Cache<K, V>
{
   /**
    * The map holding the cached objects
    */
   private ConcurrentMap<K, LRUCacheEntry<K, V>> mMap;

   /**
    * The linked list used to implement the LRU algorithm
    */
   private LRUList mList;

   /**
    * The maximum capacity of this cache
    */
   private int mMaxCapacity;

   /** The listener */
   private CacheListener mListener;

   /**
    * Creates a LRU cache
    * @param max The maximum number of entries
    */
   public LRUCache(int max)
   {
      mMaxCapacity = max;

      mMap = new ConcurrentHashMap<K, LRUCacheEntry<K, V>>();
      mList = new LRUList();
      mList.mMaxCapacity = mMaxCapacity;
      mList.mCapacity = mMaxCapacity;
      mListener = null;
   }

   /**
    * {@inheritDoc}
    */
   public V get(K key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("Requesting an object using a null key");
      }

      LRUCacheEntry<K, V> value = mMap.get(key);
      if (value != null)
      {
         mList.promote(value);
         return value.getValue();
      }
      else
      {
         cacheMiss();
         return null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public V peek(K key)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("Requesting an object using a null key");
      }

      LRUCacheEntry<K, V> value = mMap.get(key);
      if (value == null)
      {
         return null;
      }
      else
      {
         return value.getValue();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void insert(K key, V o)
   {
      if (o == null)
         throw new IllegalArgumentException("Cannot insert a null object in the cache");

      if (key == null)
         throw new IllegalArgumentException("Cannot insert an object in the cache with null key");

      if (mMap.containsKey(key))
      {
         throw new IllegalStateException("Attempt to put in the cache an object that is already there");
      }

      mList.demote();
      LRUCacheEntry<K, V> entry = createCacheEntry(key, o);
      mMap.put(key, entry);
      mList.promote(entry);
   }

   /**
    * {@inheritDoc}
    */
   public void remove(K key)
   {
      if (key == null)
         throw new IllegalArgumentException("Removing an object using a null key");

      LRUCacheEntry<K, V> value = mMap.remove(key);
      if (value != null)
      {
         mList.remove(value);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void flush()
   {
      LRUCacheEntry<K, V> entry = null;
      while ((entry = mList.mTail) != null)
      {
         ageOut(entry);
      }
   }

   public void flush(Predicate<V> predicate)
   {
      LRUCacheEntry<K, V> entry = mList.mTail;
      while (entry != null)
      {
         LRUCacheEntry<K, V> previous = entry.mPrev;
         if(predicate.test(entry.mValue))
         {
            ageOut(entry);
         }
         entry = previous;
      }
   }

   /**
    * {@inheritDoc}
    */
   public int size()
   {
      return mList.mCount;
   }

   /**
    * {@inheritDoc}
    */
   public void setListener(CacheListener listener)
   {
      mListener = listener;
   }

   /**
    * Callback method called when the cache algorithm ages out of the cache
    * the given entry. <br>
    * The implementation here is removing the given entry from the cache.
    * @param entry 
    */
   private void ageOut(LRUCacheEntry<K, V> entry)
   {
      if (mListener != null)
         mListener.onEviction(entry.getValue());

      remove(entry.getKey());
   }

   /**
    * Callback method called when a cache miss happens.
    */
   private void cacheMiss()
   {
      mList.entryCacheMiss();
   }

   /**
    * Factory method for cache entries
    * @param key 
    * @param value 
    * @return the entry 
    */
   private LRUCacheEntry<K, V> createCacheEntry(K key, V value)
   {
      return new LRUCacheEntry<K, V>(key, value);
   }

   /**
    * Double queued list used to store cache entries.
    */
   public class LRUList
   {
      /** The maximum capacity of the cache list */
      private int mMaxCapacity;

      /** The current capacity of the cache list */
      private int mCapacity;

      /** The number of cached objects */
      private int mCount;

      /** The head of the double linked list */
      private LRUCacheEntry<K, V> mHead;

      /** The tail of the double linked list */
      private LRUCacheEntry<K, V> mTail;

      /** The cache misses happened */
      private int mCacheMiss;

      /**
       * Creates a new double queued list.
       */
      protected LRUList()
      {
         mHead = null;
         mTail = null;
         mCount = 0;
         mCacheMiss = 0;
      }

      /**
       * Promotes the cache entry <code>entry</code> to the last used position
       * of the list. <br>
       * If the object is already there, does nothing.
       * @param entry the object to be promoted, cannot be null
       * @see #demote
       * @throws IllegalStateException if this method is called with a full cache
       */
      protected void promote(LRUCacheEntry<K, V> entry)
      {
         if (entry == null)
            throw new IllegalArgumentException("Trying to promote a null object");

         if (mCapacity < 1)
            throw new IllegalStateException("Can't work with capacity < 1");

         entryPromotion(entry);

         entry.updateTimestamp();
         if (entry.mPrev == null)
         {
            if (entry.mNext == null)
            {
               // entry is new or there is only the head
               if (mCount == 0) // cache is empty
               {
                  mHead = entry;
                  mTail = entry;
                  ++mCount;
                  entryAdded(entry);
               }
               else if (mCount == 1 && mHead == entry)
               {
                  // there is only the head and I want to promote it, do nothing
               }
               else if (mCount < mCapacity)
               {
                  entry.mPrev = null;
                  entry.mNext = mHead;
                  mHead.mPrev = entry;
                  mHead = entry;
                  ++mCount;
                  entryAdded(entry);
               }
               else if (mCount < mMaxCapacity)
               {
                  entry.mPrev = null;
                  entry.mNext = mHead;
                  mHead.mPrev = entry;
                  mHead = entry;
                  ++mCount;
                  int oldCapacity = mCapacity;
                  ++mCapacity;
                  entryAdded(entry);
                  capacityChanged(oldCapacity);
               }
               else
               {
                  throw new IllegalStateException("Attempt to put a new cache entry on a full cache");
               }
            }
            else
            {
               // entry is the head, do nothing
            }
         }
         else
         {
            if (entry.mNext == null) // entry is the tail
            {
               LRUCacheEntry<K, V> beforeLast = entry.mPrev;
               beforeLast.mNext = null;
               entry.mPrev = null;
               entry.mNext = mHead;
               mHead.mPrev = entry;
               mHead = entry;
               mTail = beforeLast;
            }
            else // entry is in the middle of the list
            {
               LRUCacheEntry<K, V> previous = entry.mPrev;
               previous.mNext = entry.mNext;
               entry.mNext.mPrev = previous;
               entry.mPrev = null;
               entry.mNext = mHead;
               mHead.mPrev = entry;
               mHead = entry;
            }
         }
      }

      /**
       * Demotes from the cache the least used entry. <br>
       * If the cache is not full, does nothing.
       * @see #promote
       */
      protected void demote()
      {
         if (mCapacity < 1)
            throw new IllegalStateException("Can't work with capacity < 1");

         if (mCount > mMaxCapacity)
            throw new IllegalStateException("Cache list entries number (" + mCount +
                                            ") > than the maximum allowed (" + mMaxCapacity + ")");

         if (mCount == mMaxCapacity)
         {
            LRUCacheEntry<K, V> entry = mTail;

            // the entry will be removed by ageOut
            ageOut(entry);
         }
         else
         {
            // cache is not full, do nothing
         }
      }

      /**
       * Removes from the cache list the specified entry.
       * @param entry 
       */
      protected void remove(LRUCacheEntry<K, V> entry)
      {
         if (entry == null)
            throw new IllegalArgumentException("Cannot remove a null entry from the cache");

         if (mCount < 1)
            throw new IllegalStateException("Trying to remove an entry from an empty cache");

         entry.reset();

         if (mCount == 1)
         {
            mHead = null;
            mTail = null;
         }
         else
         {
            if (entry.mPrev == null) // the head
            {
               mHead = entry.mNext;
               mHead.mPrev = null;
               entry.mNext = null;
            }
            else if (entry.mNext == null) // the tail
            {
               mTail = entry.mPrev;
               mTail.mNext = null;
               entry.mPrev = null;
            }
            else // in the middle
            {
               entry.mNext.mPrev = entry.mPrev;
               entry.mPrev.mNext = entry.mNext;
               entry.mPrev = null;
               entry.mNext = null;
            }
         }
         --mCount;
         entryRemoved(entry);
      }

      /**
       * Callback that signals that the given entry is just about to be added.
       * @param entry 
       */
      protected void entryPromotion(LRUCacheEntry<K, V> entry)
      {
      }

      /**
       * Callback that signals that the given entry has been added to the cache.
       * @param entry 
       */
      protected void entryAdded(LRUCacheEntry<K, V> entry)
      {
      }

      /**
       * Callback that signals that the given entry has been removed from the cache.
       * @param entry 
       */
      protected void entryRemoved(LRUCacheEntry<K, V> entry)
      {
      }

      /**
       * Entry cache miss
       */
      protected void entryCacheMiss()
      {
         mCacheMiss += 1;
      }

      /**
       * Callback that signals that the capacity of the cache is changed.
       * @param oldCapacity the capacity before the change happened
       */
      protected void capacityChanged(int oldCapacity)
      {
      }

      /**
       * Clear
       */
      protected void clear()
      {
         LRUCacheEntry<K, V> entry = mHead;
         mHead = null;
         mTail = null;
         mCount = 0;
         for (; entry != null; entry = entry.mNext)
            entryRemoved(entry);
      }

      /**
       * {@inheritDoc}
       */
      public String toString()
      {
         StringBuilder sb = new StringBuilder(Integer.toHexString(super.hashCode()));
         sb.append(" size: ").append(mCount);
         for (LRUCacheEntry<K, V> entry = mHead; entry != null; entry = entry.mNext)
         {
            sb.append("\n").append(entry);
         }
         return sb.toString();
      }
   }

   /**
    * Double linked cell used as entry in the cache list.
    */
   public class LRUCacheEntry<K, V>
   {
      /** Reference to the next cell in the list */
      private  LRUCacheEntry<K, V> mNext;

      /** Reference to the previous cell in the list */
      private LRUCacheEntry<K, V> mPrev;

      /** The key used to retrieve the cached object */
      private K mKey;

      /** The cached object */
      private V mValue;

      /** The timestamp of the creation */
      private long mTime;

      /**
       * Creates a new double linked cell, storing the object we
       * want to cache and the key that is used to retrieve it.
       * @param key The key
       * @param value The value
       */
      protected LRUCacheEntry(K key, V value)
      {
         mKey = key;
         mValue = value;
         mNext = null;
         mPrev = null;
         mTime = 0; // Set when inserted in the list.
      }

      /**
       * Get key
       * @return The value
       */
      public K getKey()
      {
         return mKey;
      }

      /**
       * Get value
       * @return The value
       */
      public V getValue()
      {
         return mValue;
      }

      /**
       * Set next
       * @param v The value
       */
      public void setNext(LRUCacheEntry<K, V> v)
      {
         mNext = v;
      }

      /**
       * Set prev
       * @param v The value
       */
      public void setPrev(LRUCacheEntry<K, V> v)
      {
         mPrev = v;
      }

      /**
       * Update timestamp
       */
      public void updateTimestamp()
      {
         mTime = System.currentTimeMillis();
      }

      /**
       * Reset
       */
      public void reset()
      {
         mKey = null;
         mValue = null;
      }

      /**
       * {@inheritDoc}
       */
      public String toString()
      {
         return "key: " + mKey + ", object: " +
            (mValue == null ? "null" : Integer.toHexString(mValue.hashCode())) +
            ", entry: " + Integer.toHexString(super.hashCode());
      }
   }
}
