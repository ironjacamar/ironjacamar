/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.api.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The management repository
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @author <a href="mailto:jeff.zhang@ironjacamar.org">Jeff Zhang</a> 
 */
public class ManagementRepository
{
   /** Resource adapter archives */
   private List<Connector> connectors;

   /** data sources */
   private List<DataSource> datasources;
   
   /**
    * Constructor
    */
   public ManagementRepository()
   {
      this.connectors = Collections.synchronizedList(new ArrayList<Connector>(1));
      this.datasources = Collections.synchronizedList(new ArrayList<DataSource>(1));
   }

   /**
    * Get the list of connectors
    * @return The value
    */
   public List<Connector> getConnectors()
   {
      return connectors;
   }

   /**
    * Get the list of connectors
    * @return The value
    */
   public List<DataSource> getDataSources()
   {
      return datasources;
   }
   
   /**
    * String representation
    * @return The string
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("ManagementRepository@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[connectors=").append(connectors);
      sb.append(" datasources=").append(datasources);
      sb.append("]");

      return sb.toString();
   }
}
