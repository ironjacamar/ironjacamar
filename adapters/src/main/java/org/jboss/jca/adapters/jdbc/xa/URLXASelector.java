/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.adapters.jdbc.xa;

import org.jboss.jca.adapters.jdbc.spi.URLXASelectorStrategy;
import org.jboss.jca.adapters.jdbc.spi.XAData;

import java.util.Collections;
import java.util.List;

/**
 * Default URL XA selector implementation
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class URLXASelector implements URLXASelectorStrategy
{
   private List<XAData> data;
   private XAData current;
   private int currentIndex;
   
   /**
    * Constructor
    */
   public URLXASelector()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void init(List<XAData> data)
   {
      if (data == null || data.size() == 0)
         throw new IllegalArgumentException("Data is empty");

      this.data = Collections.unmodifiableList(data);
      reset();
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasMore()
   {
      return currentIndex < data.size() - 1;
   }

   /**
    * {@inheritDoc}
    */
   public XAData active()
   {
      if (current != null)
         return current;

      currentIndex++;

      if (currentIndex == data.size())
         currentIndex = 0;

      return data.get(currentIndex);
   }

   /**
    * {@inheritDoc}
    */
   public void fail(XAData xd)
   {
      if (current.equals(xd))
         current = null;
   }

   /**
    * {@inheritDoc}
    */
   public void reset()
   {
      current = null;
      currentIndex = -1;
   }

   /**
    * {@inheritDoc}
    */
   public String getData()
   {
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < data.size(); i++)
      {
         XAData xd = data.get(i);
         sb.append(xd.getUrl());
         if (i < data.size() - 1)
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

      sb.append("URLXASelector@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[data=").append(data);
      sb.append(" current=").append(current);
      sb.append(" currentIndex=").append(currentIndex);
      sb.append("]");

      return sb.toString();
   }
}
