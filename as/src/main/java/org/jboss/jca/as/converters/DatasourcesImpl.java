/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.converters;

import java.util.List;

/**
 * A XaDataSource impl.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class DatasourcesImpl implements DataSources
{
   private List<NoTxDataSource> noTxDatasource;
   private List<LocalTxDataSource> localTxDatasource;
   private List<XaDataSource> xaDataSource;

   /**
    * DatasourcesImpl
    * @param noTxDatasource list of NoTxDatasource
    * @param localTxDatasource list of LocalTxDataSource
    * @param xaDataSource list of XaDataSource
    */
   public DatasourcesImpl(
         List<NoTxDataSource> noTxDatasource,
         List<LocalTxDataSource> localTxDatasource,
         List<XaDataSource> xaDataSource)
   {
      this.noTxDatasource = noTxDatasource;
      this.localTxDatasource = localTxDatasource;
      this.xaDataSource = xaDataSource;
   }
   
   @Override
   public String toString()
   {
      StringBuilder out = new StringBuilder();
      out.append("<datasources>");
      for (DataSource ds : noTxDatasource)
      {
         out.append(ds.toString());
      }
      for (DataSource ds : localTxDatasource)
      {
         out.append(ds.toString());
      }
      for (DataSource ds : xaDataSource)
      {
         out.append(ds.toString());
      }
      out.append("</datasources>");
      return out.toString();
   }

   @Override
   public List<LocalTxDataSource> getLocalTxDataSource()
   {
      return localTxDatasource;
   }

   @Override
   public List<NoTxDataSource> getNoTxDataSource()
   {
      return noTxDatasource;
   }

   @Override
   public List<XaDataSource> getXaDataSource()
   {
      return xaDataSource;
   }
}
