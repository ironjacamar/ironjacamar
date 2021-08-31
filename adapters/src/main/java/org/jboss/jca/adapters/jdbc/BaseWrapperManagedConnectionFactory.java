/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2021, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc;

import org.jboss.jca.adapters.AdaptersBundle;
import org.jboss.jca.adapters.AdaptersLogger;
import org.jboss.jca.adapters.jdbc.classloading.TCClassLoaderPlugin;
import org.jboss.jca.adapters.jdbc.extensions.novendor.NullExceptionSorter;
import org.jboss.jca.adapters.jdbc.extensions.novendor.NullStaleConnectionChecker;
import org.jboss.jca.adapters.jdbc.extensions.novendor.NullValidConnectionChecker;
import org.jboss.jca.adapters.jdbc.spi.ClassLoaderPlugin;
import org.jboss.jca.adapters.jdbc.spi.ExceptionSorter;
import org.jboss.jca.adapters.jdbc.spi.StaleConnectionChecker;
import org.jboss.jca.adapters.jdbc.spi.ValidConnectionChecker;
import org.jboss.jca.adapters.jdbc.spi.listener.ConnectionListener;
import org.jboss.jca.adapters.jdbc.spi.reauth.ReauthPlugin;
import org.jboss.jca.adapters.jdbc.statistics.JdbcStatisticsPlugin;
import org.jboss.jca.adapters.jdbc.util.Injection;
import org.jboss.jca.core.spi.statistics.Statistics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/**
 * BaseWrapperManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:wprice@redhat.com">Weston Price</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class BaseWrapperManagedConnectionFactory
   implements ManagedConnectionFactory, ValidatingManagedConnectionFactory, ResourceAdapterAssociation,
              Statistics, Serializable
{
   /** @since 4.0.1 */
   static final long serialVersionUID = -84923705377702088L;

   /** Track statements - false */
   public static final int TRACK_STATEMENTS_FALSE_INT = 0;

   /** Track statements - true */
   public static final int TRACK_STATEMENTS_TRUE_INT = 1;

   /** Track statements - no warning */
   public static final int TRACK_STATEMENTS_NOWARN_INT = 2;

   /** Track statements - false */
   public static final String TRACK_STATEMENTS_FALSE = "false";

   /** Track statements - true */
   public static final String TRACK_STATEMENTS_TRUE = "true";

   /** Track statements - no warning */
   public static final String TRACK_STATEMENTS_NOWARN = "nowarn";

   /** The logger */
   protected final AdaptersLogger log = Logger.getMessageLogger(AdaptersLogger.class, getClass().getName());

   /** The bundle */
   protected static AdaptersBundle bundle = Messages.getBundle(AdaptersBundle.class);

   private static boolean copyGssCredentials;

   static
   {
      String c = SecurityActions.getSystemProperty("ironjacamar.jdbc.kerberos.copygsscredentials");

      if (c != null)
      {
         copyGssCredentials = Boolean.valueOf(c);
      }
      else
      {
         copyGssCredentials = false;
      }
   }

   /** The resource adapter */
   private JDBCResourceAdapter jdbcRA;

   /** The print writer */
   private PrintWriter printWriter;

   /** The user name */
   protected String userName;

   /** The password */
   protected String password;

   /** The transaction isolation level */
   protected int transactionIsolation = -1;

   /** The prepared statement cache size */
   protected Integer preparedStatementCacheSize = Integer.valueOf(0);

   /** Query timeout enabled */
   protected boolean doQueryTimeout = false;

   protected boolean poolValidationLoggingEnabled = true;

   /**
    * The variable <code>newConnectionSQL</code> holds an SQL
    * statement which if not null is executed when a new Connection is
    * obtained for a new ManagedConnection.
    */
   protected String newConnectionSQL;

   /**
    * The variable <code>checkValidConnectionSQL</code> holds an sql
    * statement that may be executed whenever a managed connection is
    * removed from the pool, to check that it is still valid.  This
    * requires setting up an mbean to execute it when notified by the
    * ConnectionManager.
    */
   protected String checkValidConnectionSQL;

   /**
    * The classname used to check whether a connection is valid
    */
   protected String validConnectionCheckerClassName;

   private ClassLoader validConnectionCheckerClassLoader;

   private String validConnectionCheckerProperties;

   /**
    * The properties injected in the class used to check whether a connection is valid
    */
   protected final Properties validConnectionCheckerProps = new Properties();

   /**
    * The instance of the valid connection checker
    */
   protected ValidConnectionChecker connectionChecker;


   /** The instance of the stale connection checker */
   protected StaleConnectionChecker staleConnectionChecker;

   /** The staleConnectionCheckerClassName */
   private String staleConnectionCheckerClassName;

   private ClassLoader staleConnectionCheckerClassLoader;

   private String staleConnectionCheckerProperties;

   /**
    * The properties injected in the stale connection checker
    */
   protected final Properties staleConnectionCheckerProps = new Properties();

   private String exceptionSorterClassName;

   private ClassLoader exceptionSorterClassLoader;

   private String exceptionSorterProperties;

   private final Properties exceptionSorterProps = new Properties();

   private ExceptionSorter exceptionSorter;

   /** Track statement */
   protected int trackStatements = TRACK_STATEMENTS_NOWARN_INT;

   /** Whether to share cached prepared statements */
   protected Boolean sharePS = Boolean.FALSE;

   /** Transaction query timeout */
   protected Boolean isTransactionQueryTimeout = Boolean.FALSE;

   /** Query timeout */
   protected Integer queryTimeout = Integer.valueOf(0);

   /**
    * The variable <code>urlDelimiter</code> holds the url delimiter
    * information to be used for HA DS configuration .
    */
   protected String urlDelimiter;

   /** URL selector strategy class name */
   protected String urlSelectorStrategyClassName;

   /** Whether to use a try lock */
   private Integer useTryLock = Integer.valueOf(60);

   /** Spy functionality */
   private Boolean spy = Boolean.FALSE;

   /** JNDI name */
   private String jndiName;

   /** Reauth enabled */
   private Boolean reauthEnabled = Boolean.FALSE;

   /** Reauth plugin class name */
   private String reauthPluginClassName;

   /** Reauth plugin properties - format: [key|value](,key|value)+ */
   private String reauthPluginProperties;

   /** Reauth plugin */
   private ReauthPlugin reauthPlugin;

   private ClassLoaderPlugin classLoaderPlugin;

   /** The JNDI name for the user transaction */
   private String userTransactionJndiName;

   /** The statistics plugin */
   private JdbcStatisticsPlugin statisticsPlugin = new JdbcStatisticsPlugin();

   /** JTA enabled */
   private Boolean jta = Boolean.TRUE;

   /** Connection listener plugin class name */
   private String connectionListenerClassName;

   /** Connection listener plugin properties - format: [key|value](,key|value)+ */
   private String connectionListenerProperties;

   /** Connection listener plugin */
   private ConnectionListener connectionListenerPlugin;

   private ClassLoader originalTCCL;

   /**
    * Constructor
    */
   public BaseWrapperManagedConnectionFactory ()
   {
   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapter getResourceAdapter()
   {
      return jdbcRA;
   }

   /**
    * {@inheritDoc}
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      this.jdbcRA = (JDBCResourceAdapter)ra;
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return printWriter;
   }

   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter v) throws ResourceException
   {
      this.printWriter = v;
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      if (getURLDelimiter() != null && !getURLDelimiter().trim().equals(""))
         log.haDetected(getJndiName());

      return new WrapperDataSource(this, cm, getClassLoaderPlugin());
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException(bundle.nonManagedEnvironment());
   }

   /**
    * Get the user name
    * @return The value
    */
   public String getUserName()
   {
      return userName;
   }

   /**
    * Set the user name
    * @param userName The value
    */
   public void setUserName(final String userName)
   {
      this.userName = userName;
   }

   /**
    * Get the password
    * @return The value
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * Set the password
    * @param password The value
    */
   public void setPassword(final String password)
   {
      this.password = password;
   }

   /**
    * Get the prepared statement cache size
    * @return The value
    */
   public Integer getPreparedStatementCacheSize()
   {
      return preparedStatementCacheSize;
   }

   /**
    * Set the prepared statement cache size
    * @param size The value
    */
   public void setPreparedStatementCacheSize(Integer size)
   {
      if (size != null)
         preparedStatementCacheSize = size;
   }

   /**
    * Get the prepared statement share status
    * @return The value
    */
   public Boolean getSharePreparedStatements()
   {
      return sharePS;
   }

   /**
    * Set the prepared statement share status
    * @param sharePS The value
    */
   public void setSharePreparedStatements(Boolean sharePS)
   {
      if (sharePS != null)
         this.sharePS = sharePS;
   }

   /**
    * Get the transaction isolation level
    * @return The value
    */
   public String getTransactionIsolation()
   {
      switch (this.transactionIsolation)
      {
         case Connection.TRANSACTION_NONE:
            return "TRANSACTION_NONE";
         case Connection.TRANSACTION_READ_COMMITTED:
            return "TRANSACTION_READ_COMMITTED";
         case Connection.TRANSACTION_READ_UNCOMMITTED:
            return "TRANSACTION_READ_UNCOMMITTED";
         case Connection.TRANSACTION_REPEATABLE_READ:
            return "TRANSACTION_REPEATABLE_READ";
         case Connection.TRANSACTION_SERIALIZABLE:
            return "TRANSACTION_SERIALIZABLE";
         case -1:
            return "DEFAULT";
         default:
            return Integer.toString(transactionIsolation);
      }
   }

   /**
    * Set the transaction isolation level
    * @param transactionIsolation The value
    */
   public void setTransactionIsolation(String transactionIsolation)
   {
      if (transactionIsolation.equals("TRANSACTION_NONE"))
      {
         this.transactionIsolation = Connection.TRANSACTION_NONE;
      }
      else if (transactionIsolation.equals("TRANSACTION_READ_COMMITTED"))
      {
         this.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
      }
      else if (transactionIsolation.equals("TRANSACTION_READ_UNCOMMITTED"))
      {
         this.transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;
      }
      else if (transactionIsolation.equals("TRANSACTION_REPEATABLE_READ"))
      {
         this.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ;
      }
      else if (transactionIsolation.equals("TRANSACTION_SERIALIZABLE"))
      {
         this.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE;
      }
      else
      {
         try
         {
            this.transactionIsolation = Integer.parseInt(transactionIsolation);
         }
         catch (NumberFormatException nfe)
         {
            throw new IllegalArgumentException("Setting Isolation level to unknown state: " + transactionIsolation);
         }
      }
   }

   /**
    * Get the new connection SQL statement
    * @return The value
    */
   public String getNewConnectionSQL()
   {
      return newConnectionSQL;
   }

   /**
    * Set the new connection SQL statement
    * @param newConnectionSQL The value
    */
   public void setNewConnectionSQL(String newConnectionSQL)
   {
      this.newConnectionSQL = newConnectionSQL;
   }

   /**
    * Get the check valid connection SQL statement
    * @return The value
    */
   public String getCheckValidConnectionSQL()
   {
      return checkValidConnectionSQL;
   }

   /**
    * Set the check valid connection SQL statement
    * @param checkValidConnectionSQL The value
    */
   public void setCheckValidConnectionSQL(String checkValidConnectionSQL)
   {
      this.checkValidConnectionSQL = checkValidConnectionSQL;
   }

   /**
    * Get the stale connection checker class name
    * @return The value
    */
   public String getStaleConnectionCheckerClassName()
   {
      return staleConnectionCheckerClassName;
   }

   /**
    * Set the stale connection checker class name
    * @param staleConnectionCheckerClassName The staleConnectionCheckerClassName
    */
   public void setStaleConnectionCheckerClassName(String staleConnectionCheckerClassName)
   {
      this.staleConnectionCheckerClassName = staleConnectionCheckerClassName;
   }

   /**
    * Get the stale connection checker module name
    * @return The value
    */
   public ClassLoader getStaleConnectionClassLoader()
   {
      return staleConnectionCheckerClassLoader;
   }

   /**
    * Set the stale connection checker module name
    * @param staleConnectionCheckerClassLoader The staleConnectionCheckerModuleName
    */
   public void setStaleConnectionCheckerClassLoader(ClassLoader staleConnectionCheckerClassLoader)
   {
      this.staleConnectionCheckerClassLoader = staleConnectionCheckerClassLoader;
   }

   /**
    * Get the track statement value
    * @return The value
    */
   public String getTrackStatements()
   {
      if (trackStatements == TRACK_STATEMENTS_FALSE_INT)
      {
         return TRACK_STATEMENTS_FALSE;
      }
      else if (trackStatements == TRACK_STATEMENTS_TRUE_INT)
      {
         return TRACK_STATEMENTS_TRUE;
      }

      return TRACK_STATEMENTS_NOWARN;
   }

   /**
    * Set the track statement value
    * @param value The value
    */
   public void setTrackStatements(String value)
   {
      if (value == null)
         throw new IllegalArgumentException("Null value for trackStatements");

      String trimmed = value.trim();

      if (trimmed.equalsIgnoreCase(TRACK_STATEMENTS_FALSE))
      {
         trackStatements = TRACK_STATEMENTS_FALSE_INT;
      }
      else if (trimmed.equalsIgnoreCase(TRACK_STATEMENTS_TRUE))
      {
         trackStatements = TRACK_STATEMENTS_TRUE_INT;
      }
      else
      {
         trackStatements = TRACK_STATEMENTS_NOWARN_INT;
      }
   }

   /**
    * Get the exception sorter class name
    * @return The value
    */
   public String getExceptionSorterClassName()
   {
      return exceptionSorterClassName;
   }

   /**
    * Set the exception sorter class name
    * @param exceptionSorterClassName The value
    */
   public void setExceptionSorterClassName(String exceptionSorterClassName)
   {
      this.exceptionSorterClassName = exceptionSorterClassName;
   }

   /**
    * Get the exception sorter module name
    * @return The value
    */
   public ClassLoader getExceptionSorterClassLoader()
   {
      return exceptionSorterClassLoader;
   }

   /**
    * Set the exception sorter module name
    * @param exceptionSorterClassLoader The value
    */
   public void setExceptionSorterClassLoader(ClassLoader exceptionSorterClassLoader)
   {
      this.exceptionSorterClassLoader = exceptionSorterClassLoader;
   }

   /**
    * Get the valid connection checker class name
    * @return The value
    */
   public String getValidConnectionCheckerClassName()
   {
      return validConnectionCheckerClassName;
   }

   /**
    * Set the valid connection checker class name
    * @param validConnectionCheckerClassName The value
    */
   public void setValidConnectionCheckerClassName(String validConnectionCheckerClassName)
   {
      this.validConnectionCheckerClassName = validConnectionCheckerClassName;
   }

   /**
    * Get the valid connection checker class loader
    * @return The value
    */
   public ClassLoader getValidConnectionCheckerClassLoader()
   {
      return validConnectionCheckerClassLoader;
   }

   /**
    * Set the valid connection checker class name
    * @param validConnectionCheckerClassLoader The value
    */
   public void setValidConnectionCheckerClassLoader(ClassLoader validConnectionCheckerClassLoader)
   {
      this.validConnectionCheckerClassLoader = validConnectionCheckerClassLoader;
   }

   /**
    * Is transaction query timeout set
    * @return The value
    */
   public Boolean isTransactionQueryTimeout()
   {
      return isTransactionQueryTimeout;
   }

   /**
    * Set transaction query timeout
    * @param value The value
    */
   public void setTransactionQueryTimeout(Boolean value)
   {
      if (value != null)
         isTransactionQueryTimeout = value;
   }

   /**
    * Get the query timeout
    * @return The value
    */
   public Integer getQueryTimeout()
   {
      return queryTimeout;
   }

   /**
    * Set the query timeout
    * @param timeout The value
    */
   public void setQueryTimeout(Integer timeout)
   {
      if (timeout != null)
         queryTimeout = timeout;
   }

   /**
    * Get the use try lock value
    * @return The value
    */
   public Integer getUseTryLock()
   {
      return useTryLock;
   }

   /**
    * Set the use try lock value
    * @param useTryLock The value
    */
   public void setUseTryLock(Integer useTryLock)
   {
      if (useTryLock != null)
         this.useTryLock = useTryLock;
   }

   /**
    * Are we doing locking
    * @return <code>true</code> if locking, otherwise <code>false</code>
    */
   public boolean isDoLocking()
   {
      return useTryLock.intValue() >= 0;
   }
   
   /**
    * Set the spy value
    * @param v The value
    */
   public void setSpy(Boolean v)
   {
      if (v != null)
         this.spy = v;
   }

   /**
    * Get the spy value
    * @return The value
    */
   public Boolean getSpy()
   {
      return spy;
   }

   /**
    * Set the jndi name value
    * @param v The value
    */
   public void setJndiName(String v)
   {
      if (v != null)
         this.jndiName = v;
   }

   /**
    * Get the jndi name value
    * @return The value
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * Get reauth enabled
    * @return The value
    */
   public Boolean getReauthEnabled()
   {
      return reauthEnabled;
   }

   /**
    * Set reauth enabled
    * @param v The value
    */
   public void setReauthEnabled(Boolean v)
   {
      if (v != null)
         reauthEnabled = v;
   }

   /**
    * Get reauth plugin class name
    * @return The value
    */
   public String getReauthPluginClassName()
   {
      return reauthPluginClassName;
   }

   /**
    * Set reauth plugin class name
    * @param v The value
    */
   public void setReauthPluginClassName(String v)
   {
      if (v != null)
         reauthPluginClassName = v;
   }

   /**
    * Get reauth plugin properties
    * @return The value
    */
   public String getReauthPluginProperties()
   {
      return reauthPluginProperties;
   }

   /**
    * Set reauth plugin properties
    * @param v The value
    */
   public void setReauthPluginProperties(String v)
   {
      if (v != null)
         reauthPluginProperties = v;
   }

   /**
    * Load reauth plugin
    * @exception ResourceException Thrown in case of an error
    */
   synchronized void loadReauthPlugin() throws ResourceException
   {
      if (reauthPlugin != null)
         return;

      if (Boolean.FALSE.equals(reauthEnabled))
         throw new IllegalStateException("Reauthentication not enabled");

      if (reauthPluginClassName == null || reauthPluginClassName.trim().equals(""))
         throw new IllegalStateException("ReauthPlugin class name not defined");
      
      Class<?> clz = null;
      ClassLoader usedCl = null;

      try
      {
         clz = Class.forName(reauthPluginClassName, true, getClassLoaderPlugin().getClassLoader());
         usedCl = getClassLoaderPlugin().getClassLoader();
      }
      catch (ClassNotFoundException cnfe)
      {
         // Not found
      }

      if (clz == null)
      {
         try
         {
            clz = Class.forName(reauthPluginClassName, true, new TCClassLoaderPlugin().getClassLoader());
            usedCl = new TCClassLoaderPlugin().getClassLoader();
         }
         catch (ClassNotFoundException cnfe)
         {
            // Not found
         }
      }

      if (clz == null)
      {
         try
         {
            clz = Class.forName(reauthPluginClassName, true, 
                                SecurityActions.getClassLoader(BaseWrapperManagedConnectionFactory.class));
            usedCl = SecurityActions.getClassLoader(BaseWrapperManagedConnectionFactory.class);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new ResourceException(bundle.errorDuringLoadingReauthPlugin(), cnfe);
         }
      }

      try
      {
         reauthPlugin = (ReauthPlugin)clz.newInstance();

         if (reauthPluginProperties != null)
         {
            Injection injector = new Injection();

            StringTokenizer st = new StringTokenizer(reauthPluginProperties, ",");
            while (st.hasMoreTokens())
            {
               String keyValue = st.nextToken();

               int split = keyValue.indexOf("|");

               if (split == -1)
                  throw new IllegalStateException("Reauth plugin property incorrect: " + keyValue);

               String key = keyValue.substring(0, split);
               String value = "";

               if (keyValue.length() > (split + 1))
                  value = keyValue.substring(split + 1);

               injector.inject(reauthPlugin, key, value);
            }
         }

         reauthPlugin.initialize(usedCl);
      }
      catch (Throwable t)
      {
         throw new ResourceException(bundle.errorDuringLoadingReauthPlugin(), t);
      }
   }

   /**
    * Get the reauth plugin
    * @return The value
    */
   ReauthPlugin getReauthPlugin()
   {
      return reauthPlugin;
   }

   /**
    * Get connection listener class name
    * @return The value
    */
   public String getConnectionListenerClassName()
   {
      return connectionListenerClassName;
   }

   /**
    * Set connection listener class name
    * @param v The value
    */
   public void setConnectionListenerClassName(String v)
   {
      if (v != null)
         connectionListenerClassName = v;
   }

   /**
    * Get connection listener properties
    * @return The value
    */
   public String getConnectionListenerProperties()
   {
      return connectionListenerProperties;
   }

   /**
    * Set connection listener properties
    * @param v The value
    */
   public void setConnectionListenerProperties(String v)
   {
      if (v != null)
         connectionListenerProperties = v;
   }

   /**
    * Load connection listener
    * @exception ResourceException Thrown in case of an error
    */
   synchronized void loadConnectionListenerPlugin() throws ResourceException
   {
      if (connectionListenerPlugin != null)
         return;

      if (connectionListenerClassName == null || connectionListenerClassName.trim().equals(""))
         throw new IllegalStateException("ConnectionListener class name not defined");
      
      Class<?> clz = null;
      ClassLoader usedCl = null;

      try
      {
         clz = Class.forName(connectionListenerClassName, true, getClassLoaderPlugin().getClassLoader());
         usedCl = getClassLoaderPlugin().getClassLoader();
      }
      catch (ClassNotFoundException cnfe)
      {
         // Not found
      }

      if (clz == null)
      {
         try
         {
            clz = Class.forName(connectionListenerClassName, true, new TCClassLoaderPlugin().getClassLoader());
            usedCl = new TCClassLoaderPlugin().getClassLoader();
         }
         catch (ClassNotFoundException cnfe)
         {
            // Not found
         }
      }

      if (clz == null)
      {
         try
         {

            clz = Class.forName(connectionListenerClassName, true,
                                SecurityActions.getClassLoader(BaseWrapperManagedConnectionFactory.class));
            usedCl = SecurityActions.getClassLoader(BaseWrapperManagedConnectionFactory.class);
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new ResourceException(bundle.errorDuringLoadingConnectionListenerPlugin(), cnfe);
         }
      }

      try
      {
         connectionListenerPlugin = (ConnectionListener)clz.newInstance();

         if (connectionListenerProperties != null)
         {
            Injection injector = new Injection();

            StringTokenizer st = new StringTokenizer(connectionListenerProperties, ",");
            while (st.hasMoreTokens())
            {
               String keyValue = st.nextToken();

               int split = keyValue.indexOf("|");

               if (split == -1)
                  throw new IllegalStateException("ConnectionListener property incorrect: " + keyValue);

               String key = keyValue.substring(0, split);
               String value = "";

               if (keyValue.length() > (split + 1))
                  value = keyValue.substring(split + 1);

               injector.inject(connectionListenerPlugin, key, value);
            }
         }

         connectionListenerPlugin.initialize(usedCl);
      }
      catch (Throwable t)
      {
         throw new ResourceException(bundle.errorDuringLoadingConnectionListenerPlugin(), t);
      }
   }

   /**
    * Get the connection listener plugin
    * @return The value
    */
   ConnectionListener getConnectionListenerPlugin()
   {
      try
      {
         if (connectionListenerClassName != null && connectionListenerPlugin == null)
         {
            ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
            try {
               SecurityActions.setThreadContextClassLoader(originalTCCL);
               loadConnectionListenerPlugin();
            } finally {
               SecurityActions.setThreadContextClassLoader(tccl);
            }

         }

         return connectionListenerPlugin;
      }
      catch (ResourceException re)
      {
         log.unableToLoadConnectionListener(re.getMessage(), re);
      }

      return null;
   }

   /**
    * Get the url delimiter
    * @return The value
    */
   public String getURLDelimiter()
   {
      return urlDelimiter;
   }

   /**
    * Set the url delimiter.
    * @param urlDelimiter The value
    */
   public void setURLDelimiter(String urlDelimiter)
   {
      this.urlDelimiter = urlDelimiter;
   }

   /**
    * Get the url selector strategy class name
    * @return The value
    */
   public String getUrlSelectorStrategyClassName()
   {
      return urlSelectorStrategyClassName;
   }

   /**
    * Set the url selector strategy class name
    * @param urlSelectorStrategyClassName The value
    */
   public void setUrlSelectorStrategyClassName(String urlSelectorStrategyClassName)
   {
      this.urlSelectorStrategyClassName = urlSelectorStrategyClassName;
   }

   /**
    * Get the statistics plugin
    * @return The value
    */
   public JdbcStatisticsPlugin getStatistics()
   {
      return statisticsPlugin;
   }

   /**
    * Get the JTA status
    * @return The value
    */
   public Boolean isJTA()
   {
      return jta;
   }

   /**
    * Set the JTA status
    * @param v The value
    */
   public void setJTA(Boolean v)
   {
      if (v != null)
         this.jta = v;
   }

   /**
    * Get the TSR
    * @return The instance
    */
   TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
   {
      return jdbcRA.getTransactionSynchronizationRegistry();
   }

   /**
    * Get the invalid connections
    * @param connectionSet The connection set
    * @return The invalid connections
    * @exception ResourceException Thrown if an error occurs
    */
   @SuppressWarnings("rawtypes")
   public Set<BaseWrapperManagedConnection> getInvalidConnections(final Set connectionSet) throws ResourceException
   {
      final Set<BaseWrapperManagedConnection> invalid = new HashSet<BaseWrapperManagedConnection>();

      for (Iterator<?> iter = connectionSet.iterator(); iter.hasNext();)
      {
         final Object anonymous = iter.next();

         if (anonymous instanceof BaseWrapperManagedConnection)
         {
            BaseWrapperManagedConnection mc = (BaseWrapperManagedConnection) anonymous;
            Connection c = null;
            try
            {
               mc.checkTransaction(); // this prevents connection validator from opening a transaction (JBJCA-1338)
               c = mc.getRealConnection();
               SQLException e = isValidConnection(c);

               if (e != null && poolValidationLoggingEnabled)
               {
                  log.invalidConnection(c.toString(), e);
                  invalid.add(mc);
               }
            }
            catch (SQLException se)
            {
               invalid.add(mc);
            }
         }
      }

      return invalid;
   }


   /**
    * Gets full set of connection properties, i.e. whatever is provided
    * in config plus "user" and "password" from subject/cri.
    *
    * <p>Note that the set is used to match connections to datasources as well
    * as to create new managed connections.
    *
    * <p>In fact, we have a problem here. Theoretically, there is a possible
    * name collision between config properties and "user"/"password".
    * @param connectionProps The connection properties
    * @param subject The subject
    * @param cri The connection request info
    * @return The properties
    * @exception ResourceException Thrown if an error occurs
    */
   protected Properties getConnectionProperties(Properties connectionProps, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      if (cri != null && cri.getClass() != WrappedConnectionRequestInfo.class)
         throw new ResourceException(bundle.wrongConnectionRequestInfo(cri.getClass().getName()));

      Properties props = new Properties();
      
      if (connectionProps != null)
         props.putAll(connectionProps);

      WrappedConnectionRequestInfo lcri = (WrappedConnectionRequestInfo)cri;

      if (subject != null)
      {
         if (SubjectActions.addMatchingProperties(subject, lcri, props, userName, password, this))
            return props;

         throw new ResourceException(bundle.noMatchingCredentials());
      }

      if (lcri != null)
      {
         props.setProperty("user", (lcri.getUserName() == null) ? "" : lcri.getUserName());
         props.setProperty("password", (lcri.getPassword() == null) ? "" : lcri.getPassword());
         return props;
      }

      if (userName != null)
      {
         props.setProperty("user", userName);
         props.setProperty("password", (password == null) ? "" : password);
      }

      return props;
   }

   /**
    * Load plugin class
    * @param plugin The plugin class name
    * @param props Optional properties that should be injected
    * @return The configured object
    * @exception Exception Thrown if the plugin couldn't be loaded
    */
   Object loadPlugin(String plugin, ClassLoader classLoader, Properties props) throws Exception
   {
      if (plugin == null)
         throw new IllegalArgumentException("Plugin is null");

      if (plugin.trim().equals(""))
         throw new IllegalArgumentException("Plugin isn't defined");

      Class<?> clz = null;
      if(classLoader != null)
      {
         //if class cannot be loaded by provided ClassLoader we throw ClassNotFoundException immediately
         clz = Class.forName(plugin, true, classLoader);
      }

      if (clz == null)
      {
         try {
            clz = Class.forName(plugin, true, getClassLoaderPlugin().getClassLoader());
         } catch (ClassNotFoundException cnfe) {
            // Not found
         }
      }

      if (clz == null)
      {
         try
         {
            clz = Class.forName(plugin, true, new TCClassLoaderPlugin().getClassLoader());
         }
         catch (ClassNotFoundException cnfe)
         {
            // Not found
         }
      }

      if (clz == null)
      {
         try
         {
            clz = Class.forName(plugin, true,
                                SecurityActions.getClassLoader(BaseWrapperManagedConnectionFactory.class));
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new Exception("Unable to load: " + plugin);
         }
      }

      Object result = clz.newInstance();

      if (props != null)
      {
         Injection injection = new Injection();
         for (Entry<Object, Object> prop : props.entrySet())
         {
            injection.inject(result, (String)prop.getKey(), (String)prop.getValue());
         }
      }

      return result;
   }

   /**
    * Is the exception fatal
    * @param e The exception
    * @return True if fatal; otherwise false
    */
   public boolean isExceptionFatal(SQLException e)
   {
      try
      {
         if (exceptionSorter != null)
            return exceptionSorter.isExceptionFatal(e);

         if (exceptionSorterClassName != null && !exceptionSorterClassName.trim().equals(""))
         {
            try
            {
               Object o = loadPlugin(exceptionSorterClassName, exceptionSorterClassLoader, exceptionSorterProps);

               if (o != null && o instanceof ExceptionSorter)
               {
                  exceptionSorter = (ExceptionSorter)o;
                  return exceptionSorter.isExceptionFatal(e);
               }
               else
               {
                  log.disableExceptionSorter(jndiName);
                  exceptionSorter = new NullExceptionSorter();
               }
            }
            catch (Exception e2)
            {
               log.disableExceptionSorterExt(jndiName, e2);
               exceptionSorter = new NullExceptionSorter();
            }
         }
      }
      catch (Throwable t)
      {
         log.errorDuringExceptionSorter(jndiName, t);
      }
      return false;
   }

   /**
    * Is the connection valid
    * @param c The connection
    * @return <code>null</code> if valid; otherwise the exception
    */
   SQLException isValidConnection(Connection c)
   {
      // Already got a checker
      if (connectionChecker != null)
         return connectionChecker.isValidConnection(c);

      // Class specified
      if (validConnectionCheckerClassName != null && !validConnectionCheckerClassName.trim().equals(""))
      {
         try
         {
            Object o = loadPlugin(validConnectionCheckerClassName, validConnectionCheckerClassLoader, validConnectionCheckerProps);

            if (o != null && o instanceof ValidConnectionChecker)
            {
               connectionChecker = (ValidConnectionChecker)o;
               return connectionChecker.isValidConnection(c);
            }
            else
            {
               log.disableValidationChecker(jndiName);
               connectionChecker = new NullValidConnectionChecker();
            }
         }
         catch (Exception e)
         {
            log.disableValidationCheckerExt(jndiName, e);
            connectionChecker = new NullValidConnectionChecker();
         }
      }

      // SQL statement specified
      if (checkValidConnectionSQL != null)
      {
         connectionChecker = new CheckValidConnectionSQL(checkValidConnectionSQL);
         return connectionChecker.isValidConnection(c);
      }

      // No Check
      return null;
   }


   /**
    * Is the connection stale
    * @param e The exception
    * @return <code>True</code> if stale; otherwise false
    */
   boolean isStaleConnection(SQLException e)
   {
      if (staleConnectionChecker != null)
         return staleConnectionChecker.isStaleConnection(e);

      if (staleConnectionCheckerClassName != null && !staleConnectionCheckerClassName.trim().equals(""))
      {
         try
         {
            Object o = loadPlugin(staleConnectionCheckerClassName, staleConnectionCheckerClassLoader, staleConnectionCheckerProps);

            if (o != null && o instanceof StaleConnectionChecker)
            {
               staleConnectionChecker = (StaleConnectionChecker)o;
               return staleConnectionChecker.isStaleConnection(e);
            }
            else
            {
               log.disableStaleChecker(jndiName);
               staleConnectionChecker = new NullStaleConnectionChecker();
            }
         }
         catch (Exception ex2)
         {
            log.disableStaleCheckerExt(jndiName, ex2);
            staleConnectionChecker = new NullStaleConnectionChecker();
         }
      }

      return false;
   }

   public void setPoolValidationLoggingEnabled(boolean poolValidationLoggingEnabled) {
      this.poolValidationLoggingEnabled = poolValidationLoggingEnabled;
   }

   /**
    * SubjectActions
    */
   static class SubjectActions implements PrivilegedAction<Boolean>
   {
      private final Subject subject;

      private final WrappedConnectionRequestInfo cri;

      private final Properties props;

      private final String userName;

      private final String password;

      private final ManagedConnectionFactory mcf;

      /**
       * Constructor
       * @param subject The subject
       * @param cri The connection request info
       * @param props The properties
       * @param userName The user name
       * @param password The password
       * @param mcf The managed connection factory
       */
      SubjectActions(Subject subject, WrappedConnectionRequestInfo cri, Properties props,
                     String userName, String password, ManagedConnectionFactory mcf)
      {
         this.subject = subject;
         this.cri = cri;
         this.props = props;
         this.userName = userName;
         this.password = password;
         this.mcf = mcf;
      }

      /**
       * doCheck
       * @return The result
       */
      private Boolean doCheck()
      {
         Set<PasswordCredential> pcCreds = subject.getPrivateCredentials(PasswordCredential.class);
         if (pcCreds != null && pcCreds.size() > 0)
         {
            for (PasswordCredential cred : pcCreds)
            {
               if (cred.getManagedConnectionFactory().equals(mcf))
               {
                  String user = null;
                  String pass = null;

                  if (cri != null)
                  {
                     user = cri.getUserName();
                     pass = cri.getPassword();
                  }
                  else
                  {
                     user = cred.getUserName();
                     if (cred.getPassword() != null)
                        pass = new String(cred.getPassword());
                  }

                  if (userName != null)
                  {
                     user = userName;

                     if (password != null)
                        pass = password;
                  }

                  props.setProperty("user", (user == null) ? "" : user);
                  props.setProperty("password", (pass == null) ? "" : pass);

                  return Boolean.TRUE;
               }
            }
         }

         Set<GSSCredential> gssCreds = subject.getPrivateCredentials(GSSCredential.class);
         if (gssCreds != null && gssCreds.size() > 0)
         {
            boolean done = false;
            for (GSSCredential cred : gssCreds)
            {
               String user = null;
               String pass = null;

               if (cri != null)
               {
                  user = cri.getUserName();
                  pass = cri.getPassword();
                  done = true;
               }
               else
               {
                  try
                  {
                     Oid krb5 = new Oid("1.2.840.113554.1.2.2");
                     GSSName gssName = cred.getName(krb5);
                     user = gssName.toString();
                     done = true;
                  }
                  catch (GSSException ge)
                  {
                     // Nothing we can do
                  }
               }

               if (done)
               {
                  if (userName != null)
                  {
                     user = userName;

                     if (password != null)
                        pass = password;
                  }

                  if (cri != null || userName != null || copyGssCredentials)
                  {
                     props.setProperty("user", (user == null) ? "" : user);
                     props.setProperty("password", (pass == null) ? "" : pass);
                  }

                  return Boolean.TRUE;
               }
            }
         }

         return Boolean.FALSE;
      }

      /**
       * Run
       * @return The result
       */
      public Boolean run()
      {
         return doCheck();
      }

      /**
       * Add matching properties
       * @param subject The subject
       * @param cri The connection request info
       * @param props The properties
       * @param userName The user name
       * @param password The password
       * @param mcf The managed connection factory
       * @return The result
       */
      static boolean addMatchingProperties(Subject subject, WrappedConnectionRequestInfo cri,
                                           Properties props, String userName, String password,
                                           ManagedConnectionFactory mcf)
      {
         SubjectActions action = new SubjectActions(subject, cri, props, userName, password, mcf);
         Boolean matched = Boolean.FALSE;
         if (System.getSecurityManager() == null)
         {
            matched = action.doCheck();
         }
         else
         {
            matched = AccessController.doPrivileged(action);
         }
         return matched.booleanValue();
      }
   }

   /**
    * Get the validConnectionCheckerProps.
    *
    * @return the validConnectionCheckerProps.
    */
   public final Properties getValidConnectionCheckerProps()
   {
      return validConnectionCheckerProps;
   }

   /**
    * Get the staleConnectionCheckerProps.
    *
    * @return the staleConnectionCheckerProps.
    */
   public final Properties getStaleConnectionCheckerProps()
   {
      return staleConnectionCheckerProps;
   }

   /**
    * Get the exceptionSorterProps.
    *
    * @return the exceptionSorterProps.
    */
   public final Properties getExceptionSorterProps()
   {
      return exceptionSorterProps;
   }

   /**
    * Get the validConnectionCheckerProperties.
    *
    * @return the validConnectionCheckerProperties.
    */
   public final String getValidConnectionCheckerProperties()
   {
      return validConnectionCheckerProperties;
   }

   /**
    * Set the validConnectionCheckerProperties.
    *
    * @param validConnectionCheckerProperties The validConnectionCheckerProperties to set.
    */
   public final void setValidConnectionCheckerProperties(String validConnectionCheckerProperties)
   {
      this.validConnectionCheckerProperties = validConnectionCheckerProperties;
      validConnectionCheckerProps.clear();

      if (validConnectionCheckerProperties != null)
      {
         // Map any \ to \\
         validConnectionCheckerProperties = validConnectionCheckerProperties.replaceAll("\\\\", "\\\\\\\\");
         validConnectionCheckerProperties = validConnectionCheckerProperties.replaceAll(";", "\n");

         InputStream is = new ByteArrayInputStream(validConnectionCheckerProperties.getBytes());
         try
         {
            validConnectionCheckerProps.load(is);
         }
         catch (IOException ioe)
         {
            throw new RuntimeException("Could not load connection properties", ioe);
         }
      }
   }

   /**
    * Get the staleConnectionCheckerProperties.
    *
    * @return the staleConnectionCheckerProperties.
    */
   public final String getStaleConnectionCheckerProperties()
   {
      return staleConnectionCheckerProperties;
   }

   /**
    * Set the staleConnectionCheckerProperties.
    *
    * @param staleConnectionCheckerProperties The staleConnectionCheckerProperties to set.
    */
   public final void setStaleConnectionCheckerProperties(String staleConnectionCheckerProperties)
   {
      this.staleConnectionCheckerProperties = staleConnectionCheckerProperties;
      staleConnectionCheckerProps.clear();

      if (staleConnectionCheckerProperties != null)
      {
         // Map any \ to \\
         staleConnectionCheckerProperties = staleConnectionCheckerProperties.replaceAll("\\\\", "\\\\\\\\");
         staleConnectionCheckerProperties = staleConnectionCheckerProperties.replaceAll(";", "\n");

         InputStream is = new ByteArrayInputStream(staleConnectionCheckerProperties.getBytes());
         try
         {
            staleConnectionCheckerProps.load(is);
         }
         catch (IOException ioe)
         {
            throw new RuntimeException("Could not load connection properties", ioe);
         }
      }
   }

   /**
    * Get the exceptionSorterProperties.
    *
    * @return the exceptionSorterProperties.
    */
   public final String getExceptionSorterProperties()
   {
      return exceptionSorterProperties;
   }

   /**
    * Set the exceptionSorterProperties.
    *
    * @param exceptionSorterProperties The exceptionSorterProperties to set.
    */
   public final void setExceptionSorterProperties(String exceptionSorterProperties)
   {
      this.exceptionSorterProperties = exceptionSorterProperties;
      exceptionSorterProps.clear();

      if (exceptionSorterProperties != null)
      {
         // Map any \ to \\
         exceptionSorterProperties = exceptionSorterProperties.replaceAll("\\\\", "\\\\\\\\");
         exceptionSorterProperties = exceptionSorterProperties.replaceAll(";", "\n");

         InputStream is = new ByteArrayInputStream(exceptionSorterProperties.getBytes());
         try
         {
            exceptionSorterProps.load(is);
         }
         catch (IOException ioe)
         {
            throw new RuntimeException("Could not load connection properties", ioe);
         }
      }
   }

   /**
    * Get the clPlugin.
    *
    * @return the clPlugin.
    */
   public final ClassLoaderPlugin getClassLoaderPlugin()
   {
      if (classLoaderPlugin == null)
      {
         classLoaderPlugin = new TCClassLoaderPlugin();
      }
      return classLoaderPlugin;
   }

   /**
    * Set the clPlugin.
    *
    * @param clPlugin The clPlugin to set.
    */
   public final void setClassLoaderPlugin(ClassLoaderPlugin clPlugin)
   {
      this.classLoaderPlugin = clPlugin;
   }

   /**
    * Set the originalTCCL.
    *
    * @param cl The clPlugin to set.
    */
   public final void setOriginalTCCLn(ClassLoader cl)
   {
      this.originalTCCL = cl;
   }
}
