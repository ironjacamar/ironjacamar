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
package org.jboss.jca.deployers.test.rars.stat;

import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Statistics plugin for the resource adapter
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class StatisticsResourceAdapterStatisticsPlugin implements StatisticsPlugin
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   private boolean enabled;
   private Set<String> names;

   /**
    * Constructor
    */
   public StatisticsResourceAdapterStatisticsPlugin()
   {
      this.enabled = true;
      this.names = new HashSet<String>();
      this.names.add("ResourceAdapter");
   }

   /**
    * {@inheritDoc}
    */
   public Set<String> getNames()
   {
      return names;
   }

   /**
    * {@inheritDoc}
    */
   public Class getType(String name)
   {
      return String.class;
   }

   /**
    * {@inheritDoc}
    */
   public String getDescription(String name)
   {
      return getDescription(name, Locale.US);
   }

   /**
    * {@inheritDoc}
    */
   public String getDescription(String name, Locale locale)
   {
      return "ResourceAdapter Description";
   }

   /**
    * {@inheritDoc}
    */
   public Object getValue(String name)
   {
      return "ResourceAdapter Value";
   }

   /**
    * {@inheritDoc}
    */
   public boolean isEnabled()
   {
      return enabled;
   }

   /**
    * {@inheritDoc}
    */
   public void setEnabled(boolean v)
   {
      enabled = v;
   }

   /**
    * {@inheritDoc}
    */
   public void clear()
   {
   }
}
