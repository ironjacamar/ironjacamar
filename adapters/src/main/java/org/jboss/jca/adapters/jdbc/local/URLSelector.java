/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.local;

import org.jboss.jca.adapters.jdbc.spi.URLSelectorStrategy;

import java.util.Collections;
import java.util.List;

/**
 * Default URL selector implementation
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class URLSelector implements URLSelectorStrategy
{
   private List<String> urls;
   private String currentUrl;
   private int currentIndex;
   private int triesCounter;
   
   /**
    * Constructor
    */
   public URLSelector()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void init(List<String> urls)
   {
      if (urls == null || urls.size() == 0)
         throw new IllegalArgumentException("Urls is empty");

      this.urls = Collections.unmodifiableList(urls);
      reset();
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasMore()
   {
      if (currentUrl != null)
         return true;

      if (triesCounter < urls.size())
      {
         return true;
      }
      else
      {
         currentIndex = 0;
         triesCounter = 0;
         return false;
      }
   }

   /**
    * {@inheritDoc}
    */
   public String active()
   {
      if (currentUrl != null)
         return currentUrl;

      triesCounter++;
      currentUrl = urls.get((currentIndex++)%urls.size());
      return currentUrl;
   }

   /**
    * {@inheritDoc}
    */
   public void fail(String url)
   {
      if (currentUrl != null && currentUrl.equals(url))
         currentUrl = null;
   }

   /**
    * {@inheritDoc}
    */
   public void success(String url)
   {
      triesCounter = 0;
   }

   /**
    * {@inheritDoc}
    */
   public void reset()
   {
      currentUrl = null;
      currentIndex = 0;
   }

   /**
    * {@inheritDoc}
    */
   public String getData()
   {
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < urls.size(); i++)
      {
         String url = urls.get(i);
         sb.append(url);
         if (i < urls.size() - 1)
            sb.append(", ");
      }

      return sb.toString();
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("URLSelector@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[urls=").append(urls);
      sb.append(" currentUrl=").append(currentUrl);
      sb.append(" currentIndex=").append(currentIndex);
      sb.append("]");

      return sb.toString();
   }
}
