/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.api.metadata.ds.Driver;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A DatasourcesImpl.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class DatasourcesImpl implements DataSources
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 6933310057105771370L;

   private final List<DataSource> datasource;

   private final List<XaDataSource> xaDataSource;

   private final Map<String, Driver> drivers;

   /**
    * Create a new DatasourcesImpl.
    *
    * @param datasource datasource
    * @param xaDataSource xaDataSource
    * @param drivers drivers
    * @throws ValidateException ValidateException
    */
   public DatasourcesImpl(List<DataSource> datasource,
                          List<XaDataSource> xaDataSource,
                          Map<String, Driver> drivers)
      throws ValidateException
   {
      super();
      if (datasource != null)
      {
         this.datasource = new ArrayList<DataSource>(datasource.size());
         this.datasource.addAll(datasource);
      }
      else
      {
         this.datasource = new ArrayList<DataSource>(0);
      }
      if (xaDataSource != null)
      {
         this.xaDataSource = new ArrayList<XaDataSource>(xaDataSource.size());
         this.xaDataSource.addAll(xaDataSource);
      }
      else
      {
         this.xaDataSource = new ArrayList<XaDataSource>(0);
      }
      if (drivers != null)
      {
         this.drivers = new HashMap<String, Driver>(drivers.size());
         this.drivers.putAll(drivers);
      }
      else
      {
         this.drivers = new HashMap<String, Driver>(0);
      }
      this.validate();
   }

   /**
    * Get the datasource.
    *
    * @return the datasource.
    */
   @Override
   public final List<DataSource> getDataSource()
   {
      return Collections.unmodifiableList(datasource);
   }

   /**
    * Get the xaDataSource.
    *
    * @return the xaDataSource.
    */
   @Override
   public final List<XaDataSource> getXaDataSource()
   {
      return Collections.unmodifiableList(xaDataSource);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((datasource == null) ? 0 : datasource.hashCode());
      result = prime * result + ((xaDataSource == null) ? 0 : xaDataSource.hashCode());
      result = prime * result + ((drivers == null) ? 0 : drivers.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof DatasourcesImpl))
         return false;
      DatasourcesImpl other = (DatasourcesImpl) obj;
      if (datasource == null)
      {
         if (other.datasource != null)
            return false;
      }
      else if (!datasource.equals(other.datasource))
         return false;
      if (xaDataSource == null)
      {
         if (other.xaDataSource != null)
            return false;
      }
      else if (!xaDataSource.equals(other.xaDataSource))
         return false;
      if (drivers == null)
      {
         if (other.drivers != null)
            return false;
      }
      else if (!drivers.equals(other.drivers))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<datasources>");

      if (datasource != null && datasource.size() > 0)
      {
         for (DataSource ds : datasource)
         {
            sb.append(ds);
         }
      }

      if (xaDataSource != null && xaDataSource.size() > 0)
      {
         for (XaDataSource xads : xaDataSource)
         {
            sb.append(xads);
         }
      }

      if (drivers != null && drivers.size() > 0)
      {
         sb.append("<").append(DataSources.Tag.DRIVERS).append(">");

         for (Driver d : drivers.values())
         {
            sb.append(d);
         }

         sb.append("</").append(DataSources.Tag.DRIVERS).append(">");
      }

      sb.append("</datasources>");

      return sb.toString();
   }

   @Override
   public void validate() throws ValidateException
   {
      //always validate if all content is validating
      for (DataSource ds : this.datasource)
      {
         ds.validate();
      }
      for (XaDataSource xads : this.xaDataSource)
      {
         xads.validate();
      }
   }

   @Override
   public Driver getDriver(String name)
   {
      return drivers.get(name);
   }

   @Override
   public List<Driver> getDrivers()
   {
      return Collections.unmodifiableList(new ArrayList<Driver>(drivers.values()));
   }
}
