/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.common.api.metadata.ds;

import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.common.Recovery;

import java.util.Map;

/**
 * An XaDataSource.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 */
public interface XaDataSource extends CommonDataSource
{
   /**
    * Get the xaDataSourceClass.
    *
    * @return the xaDataSourceClass.
    */
   public String getXaDataSourceClass();

   /**
    * Get the statement.
    *
    * @return the statement.
    */
   public Statement getStatement();

   /**
    * Get the urlDelimiter.
    *
    * @return the urlDelimiter.
    */
   public String getUrlDelimiter();

   /**
    * Get the urlSelectorStrategyClassName.
    *
    * @return the urlSelectorStrategyClassName.
    */
   public String getUrlSelectorStrategyClassName();

   /**
    * Get the newConnectionSql.
    *
    * @return the newConnectionSql.
    */
   public String getNewConnectionSql();

   /**
    * Get the xaDataSourceProperty.
    *
    * @return the xaDataSourceProperty.
    */
   public Map<String, String> getXaDataSourceProperty();

   /**
    * Get the xaPool.
    *
    * @return the xaPool.
    */
   public CommonXaPool getXaPool();

   /**
    * Get the recovery settings.
    *
    * @return the recovery settings.
    */
   public Recovery getRecovery();
}
