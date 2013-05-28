/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.spi.statistics;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;

/**
 * Defines the contract for a statistics plugin.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface StatisticsPlugin extends Serializable
{
   /**
    * Get the statistics names
    * @return The value
    */
   public Set<String> getNames();

   /**
    * Get the type
    * @param name The name of the statistics
    * @return The value
    */
   public Class getType(String name);

   /**
    * Get the description
    * @param name The name of the statistics
    * @return The value
    */
   public String getDescription(String name);

   /**
    * Get the description
    * @param name The name of the statistics
    * @param locale The locale
    * @return The value
    */
   public String getDescription(String name, Locale locale);

   /**
    * Get the value of the statistics
    * @param name The name of the statistics
    * @return The value
    */
   public Object getValue(String name);

   /**
    * Is the statistics module enabled
    * @return The value
    */
   public boolean isEnabled();

   /**
    * Set the statistics module enabled
    * @param v The value
    */
   public void setEnabled(boolean v);

   /**
    * Clear all statistics
    */
   public void clear();
}
