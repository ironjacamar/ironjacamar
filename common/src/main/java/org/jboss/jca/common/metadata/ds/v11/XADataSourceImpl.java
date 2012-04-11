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
package org.jboss.jca.common.metadata.ds.v11;

import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.api.metadata.ds.v11.DsXaPool;
import org.jboss.jca.common.api.metadata.ds.v11.XaDataSource;
import org.jboss.jca.common.api.validator.ValidateException;

import java.util.Map;

/**
 * An XA datasource implementation
 *
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class XADataSourceImpl extends org.jboss.jca.common.metadata.ds.v10.XADataSourceImpl implements XaDataSource
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -1401087499308709724L;

   /**
    * Create a new XADataSourceImpl.
    *
    * @param transactionIsolation transactionIsolation
    * @param timeOut timeOut
    * @param security security
    * @param statement statement
    * @param validation validation
    * @param urlDelimiter urlDelimiter
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param useJavaContext useJavaContext
    * @param poolName poolName
    * @param enabled enabled
    * @param jndiName jndiName
    * @param spy spy
    * @param useCcm useCcm
    * @param xaDataSourceProperty xaDataSourceProperty
    * @param xaDataSourceClass xaDataSourceClass
    * @param driver driver
    * @param newConnectionSql newConnectionSql
    * @param xaPool xaPool
    * @param recovery recovery
    * @throws ValidateException ValidateException
    */
   public XADataSourceImpl(TransactionIsolation transactionIsolation, TimeOut timeOut, DsSecurity security,
      Statement statement, Validation validation, String urlDelimiter, String urlSelectorStrategyClassName,
      Boolean useJavaContext, String poolName, Boolean enabled, String jndiName, Boolean spy, Boolean useCcm,
      Map<String, String> xaDataSourceProperty, String xaDataSourceClass, String driver, String newConnectionSql,
      DsXaPool xaPool, Recovery recovery) throws ValidateException
   {
      super(transactionIsolation, timeOut, security, statement, validation, urlDelimiter,
            urlSelectorStrategyClassName, useJavaContext, poolName, enabled, jndiName, spy, useCcm,
            xaDataSourceProperty, xaDataSourceClass, driver, newConnectionSql, xaPool, recovery);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DsXaPool getXaPool()
   {
      return (DsXaPool)super.getXaPool();
   }
}
