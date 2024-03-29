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
package org.jboss.jca.as.converters;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.ds.DsSecurity;
import org.jboss.jca.common.api.metadata.ds.DsXaPool;
import org.jboss.jca.common.api.metadata.ds.Statement;
import org.jboss.jca.common.api.metadata.ds.Statement.TrackStatementsEnum;
import org.jboss.jca.common.api.metadata.ds.TimeOut;
import org.jboss.jca.common.api.metadata.ds.TransactionIsolation;
import org.jboss.jca.common.api.metadata.ds.Validation;
import org.jboss.jca.common.metadata.common.CredentialImpl;
import org.jboss.jca.common.metadata.ds.DsSecurityImpl;
import org.jboss.jca.common.metadata.ds.DsXaPoolImpl;
import org.jboss.jca.common.metadata.ds.StatementImpl;
import org.jboss.jca.common.metadata.ds.TimeOutImpl;
import org.jboss.jca.common.metadata.ds.ValidationImpl;
import org.jboss.jca.common.metadata.ds.XADataSourceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * A XaDataSource impl.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class LegacyXaDataSourceImp implements XaDataSource
{

   private XADataSourceImpl dsImpl = null;

   private String xaDataSourceClass;

   private final String driver;

   private final HashMap<String, String> xaDataSourceProperty;

   private final TransactionIsolation transactionIsolation;

   private TimeOut timeOut = null;

   private DsSecurity security = null;

   private Statement statement = null;

   private Validation validation = null;

   private DsXaPool xaPool = null;

   private String urlDelimiter;

   private String urlSelectorStrategyClassName;
   
   private String newConnectionSql;

   private Boolean useJavaContext;

   private String poolName;

   private Boolean enabled;

   private String jndiName;

   private Boolean spy;

   private Boolean useCcm;
   
   private Boolean jta;
   
   private Recovery recovery;
   
   private Boolean isSameRmOverride;

   private Boolean interleaving;

   private Boolean padXid;

   private Boolean wrapXaDataSource;

   private Boolean noTxSeparatePool;
   
   
   /**
    * create a LegacyXaDataSourceImp
    * @param xaDataSourceClass xaDataSourceClass
    * @param driver driver
    * @param transactionIsolation transactionIsolation
    * @param xaDataSourceProperty xaDataSourceProperty
    */
   public LegacyXaDataSourceImp(String xaDataSourceClass, String driver,
         TransactionIsolation transactionIsolation, Map<String, String> xaDataSourceProperty)
   {
      this.xaDataSourceClass = xaDataSourceClass;
      this.driver = driver;
      if (xaDataSourceProperty != null)
      {
         this.xaDataSourceProperty = new HashMap<String, String>(xaDataSourceProperty.size());
         this.xaDataSourceProperty.putAll(xaDataSourceProperty);
      }
      else
      {
         this.xaDataSourceProperty = new HashMap<String, String>(0);
      }
      this.transactionIsolation = transactionIsolation;
   }
   
   /**
    * buildXaDataSourceImpl
    * @throws Exception exception
    */
   public void buildXaDataSourceImpl()  throws Exception
   {
      dsImpl = new XADataSourceImpl(transactionIsolation, timeOut, security,
                                    statement, validation, urlDelimiter, "|", urlSelectorStrategyClassName, 
                                    useJavaContext, poolName, enabled, jndiName, spy, useCcm,
                                    Defaults.CONNECTABLE, Defaults.TRACKING, Defaults.MCP, Defaults.ENLISTMENT_TRACE,
                                    xaDataSourceProperty, xaDataSourceClass, driver, newConnectionSql, 
                                    xaPool, recovery);
   }
   
   @Override
   public String toString()
   {
      String out = dsImpl.toString();
      return out;
   }
   
   /**
    * build timeout part
    * 
    * @param blockingTimeoutMillis blockingTimeoutMillis
    * @param idleTimeoutMinutes idleTimeoutMinutes
    * @param allocationRetry allocationRetry
    * @param allocationRetryWaitMillis allocationRetryWaitMillis
    * @param xaResourceTimeout xaResourceTimeout
    * @param setTxQueryTimeout setTxQueryTimeout
    * @param queryTimeout queryTimeout
    * @param useTryLock useTryLock
    * @return this 
    * @throws Exception exception
    */
   public LegacyXaDataSourceImp buildTimeOut(Long blockingTimeoutMillis, Long idleTimeoutMinutes, 
         Integer allocationRetry, Long allocationRetryWaitMillis, Integer xaResourceTimeout, 
         Boolean setTxQueryTimeout, Long queryTimeout,
         Long useTryLock) throws Exception
   {
      timeOut = new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
            allocationRetryWaitMillis, xaResourceTimeout, setTxQueryTimeout,
            queryTimeout, useTryLock);
      return this;
   }
   
   /** 
    * build security part
    * 
    * @param userName userName
    * @param password password
    * @param securityDomain securityDomain
    * @param reauthPlugin reauthPlugin
    * @return this
    * @throws Exception exception
    */
   public LegacyXaDataSourceImp buildDsSecurity(String userName, String password, String securityDomain, 
         Extension reauthPlugin) throws Exception
   {
      security = new DsSecurityImpl(userName, password, securityDomain, reauthPlugin);
      return this;
   }

   /**
    * build statement part
    * 
    * @param sharePreparedStatements sharePreparedStatements
    * @param preparedStatementsCacheSize preparedStatementsCacheSize
    * @param trackStatements trackStatements
    * @return this
    * @throws Exception exception
    */
   public LegacyXaDataSourceImp buildStatement(Boolean sharePreparedStatements, Long preparedStatementsCacheSize,
         TrackStatementsEnum trackStatements) throws Exception
   {
      statement = new StatementImpl(sharePreparedStatements, preparedStatementsCacheSize, trackStatements);
      return this;
   }

   /**
    * build validation part
    * 
    * @param backgroundValidation backgroundValidation
    * @param backgroundValidationMillis backgroundValidationMillis
    * @param useFastFail useFastFail
    * @param validConnectionChecker validConnectionChecker
    * @param checkValidConnectionSql checkValidConnectionSql
    * @param validateOnMatch validateOnMatch
    * @param staleConnectionChecker staleConnectionChecker
    * @param exceptionSorter exceptionSorter
    * @return this
    * @throws Exception exception
    */
   public LegacyXaDataSourceImp buildValidation(Boolean backgroundValidation, Long backgroundValidationMillis, 
         Boolean useFastFail,
         Extension validConnectionChecker, String checkValidConnectionSql, Boolean validateOnMatch,
         Extension staleConnectionChecker, Extension exceptionSorter) throws Exception
   {
      validation = new ValidationImpl(backgroundValidation, backgroundValidationMillis, useFastFail,
            validConnectionChecker, checkValidConnectionSql, validateOnMatch,
            staleConnectionChecker, exceptionSorter);
      return this;
   }

   /**
    * build pool part
    * 
    * @param minPoolSize minPoolSize
    * @param initialPoolSize initialPoolSize
    * @param maxPoolSize maxPoolSize
    * @param prefill prefill
    * @param useStrictMin useStrictMin
    * @param flushStrategy flushStrategy
    * @param isSameRmOverride isSameRmOverride
    * @param interleaving interleaving
    * @param padXid padXid
    * @param wrapXaResource wrapXaResource
    * @param noTxSeparatePool noTxSeparatePool
    * @return this
    * @throws Exception exception
    */
   public LegacyXaDataSourceImp buildCommonPool(Integer minPoolSize, Integer initialPoolSize, Integer maxPoolSize, 
         Boolean prefill, Boolean useStrictMin,
         FlushStrategy flushStrategy, Boolean isSameRmOverride, Boolean interleaving, 
         Boolean padXid, Boolean wrapXaResource,
         Boolean noTxSeparatePool) throws Exception
   {
      xaPool = new DsXaPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin, flushStrategy,
            isSameRmOverride, interleaving, padXid,
            wrapXaResource, noTxSeparatePool, false, null, null, null);

      return this;
   }
   
   /**
    * build recovery part
    * 
    * @param recoveryUsername recoveryUsername
    * @param recoveryPassword recoveryPassword
    * @param noRecovery noRecovery
    * @return this
    * @throws Exception exception
    */
   public LegacyXaDataSourceImp buildRecovery(String recoveryUsername, String recoveryPassword, 
      Boolean noRecovery) throws Exception
   {
      if (recoveryUsername == null || recoveryUsername.equals(""))
         recoveryUsername = "user";
      if (recoveryPassword == null || recoveryPassword.equals(""))
         recoveryPassword = "password";
      recovery = new Recovery(new CredentialImpl(recoveryUsername, recoveryPassword, null), null, noRecovery);
      return this;
   }
   
   /**
    * build other properties
    * 
    * @param urlDelimiter urlDelimiter
    * @param urlSelectorStrategyClassName urlSelectorStrategyClassName
    * @param newConnectionSql newConnectionSql
    * @param useJavaContext useJavaContext
    * @param poolName poolName
    * @param enabled enabled
    * @param jndiName jndiName
    * @param spy spy
    * @param useCcm useCcm
    * @param jta jta
    * @return this
    */
   public LegacyXaDataSourceImp buildOther(String urlDelimiter, String urlSelectorStrategyClassName, 
         String newConnectionSql, 
         Boolean useJavaContext, String poolName, Boolean enabled, String jndiName, 
         Boolean spy, Boolean useCcm, Boolean jta)
   {
      this.urlDelimiter = urlDelimiter;
      this.urlSelectorStrategyClassName = urlSelectorStrategyClassName;
      this.newConnectionSql = newConnectionSql;
      this.useJavaContext = useJavaContext;
      this.poolName = poolName;
      this.enabled = enabled;
      this.jndiName = jndiName;
      this.spy = spy;
      this.useCcm = useCcm;
      this.setJta(jta);
      return this;
   }
   

   @Override
   public String getJndiName()
   {
      return this.jndiName;
   }

   @Override
   public Boolean isUseJavaContext()
   {
      return this.useJavaContext;
   }

   @Override
   public String getUrlDelimiter()
   {
      return this.urlDelimiter;
   }

   @Override
   public String getUrlSelectorStrategyClassName()
   {
      return this.urlSelectorStrategyClassName;
   }

   @Override
   public String getUserName()
   {
      return this.security.getUserName();
   }

   @Override
   public String getPassword()
   {
      return this.security.getPassword();
   }

   @Override
   public String getSecurityDomain()
   {
      return null;
   }

   @Override
   public Integer getMinPoolSize()
   {
      return this.xaPool.getMinPoolSize();
   }

   @Override
   public Integer getMaxPoolSize()
   {
      return this.xaPool.getMaxPoolSize();
   }

   @Override
   public Long getBlockingTimeoutMillis()
   {
      return this.timeOut.getBlockingTimeoutMillis();
   }

   @Override
   public Boolean isBackgroundValidation()
   {
      return this.validation.isBackgroundValidation();
   }

   @Override
   public Long getBackgroundValidationMillis()
   {
      return this.validation.getBackgroundValidationMillis();
   }

   @Override
   public Long getIdleTimeoutMinutes()
   {
      return this.timeOut.getIdleTimeoutMinutes();
   }

   @Override
   public Integer getAllocationRetry()
   {
      return this.timeOut.getAllocationRetry();
   }

   @Override
   public Long getAllocationRetryWaitMillis()
   {
      return this.timeOut.getAllocationRetryWaitMillis();
   }

   @Override
   public Boolean isValidateOnMatch()
   {
      return this.validation.isValidateOnMatch();
   }

   @Override
   public String getNewConnectionSql()
   {
      return this.newConnectionSql;
   }

   @Override
   public String getCheckValidConnectionSql()
   {
      return this.validation.getCheckValidConnectionSql();
   }

   @Override
   public Extension getValidConnectionChecker()
   {
      return this.validation.getValidConnectionChecker();
   }

   @Override
   public Extension getExceptionSorter()
   {
      return null;
   }

   @Override
   public Extension getStaleConnectionChecker()
   {
      return null;
   }

   @Override
   public TrackStatementsEnum getTrackStatements()
   {

      return this.statement.getTrackStatements();
   }

   @Override
   public Boolean isPrefill()
   {
      return this.xaPool.isPrefill();
   }

   @Override
   public Boolean isUseFastFail()
   {
      return this.validation.isUseFastFail();
   }

   @Override
   public Long getPreparedStatementsCacheSize()
   {
      return this.statement.getPreparedStatementsCacheSize();
   }

   @Override
   public Boolean isSharePreparedStatements()
   {
      return this.statement.isSharePreparedStatements();
   }

   @Override
   public Boolean isSetTxQueryTimeout()
   {
      return this.timeOut.isSetTxQueryTimeout();
   }

   @Override
   public Long getQueryTimeout()
   {
      return this.timeOut.getQueryTimeout();
   }

   @Override
   public Long getUseTryLock()
   {
      return this.timeOut.getUseTryLock();
   }

   @Override
   public Boolean isNoTxSeparatePools()
   {
      return this.noTxSeparatePool;
   }

   
   @Override
   public Boolean isTrackConnectionByTx()
   {
      return false;
   }

   @Override
   public Integer getXaResourceTimeout()
   {
      return this.timeOut.getXaResourceTimeout();
   }

   @Override
   public String getXaDataSourceClass()
   {

      return this.xaDataSourceClass;
   }

   @Override
   public Map<String, String> getXaDataSourceProperty()
   {
      return this.xaDataSourceProperty;
   }

   @Override
   public Boolean isSameRmOverride()
   {
      return this.isSameRmOverride;
   }

   @Override
   public Boolean isInterleaving()
   {
      return this.interleaving;
   }

   @Override
   public Boolean isPadXid()
   {
      return this.padXid;
   }

   @Override
   public Boolean isWrapXaResource()
   {
      return this.wrapXaDataSource;
   }

   /**
    * setJta
    * @param jta jta
    */
   public void setJta(Boolean jta)
   {
      this.jta = jta;
   }

   /**
    * getJta
    * @return jta Boolean 
    */
   public Boolean getJta()
   {
      return jta;
   }
}
