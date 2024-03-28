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

package org.jboss.jca.adapters.jdbc.local;

import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnectionFactory;
import org.jboss.jca.adapters.jdbc.classloading.TCClassLoaderPlugin;
import org.jboss.jca.adapters.jdbc.spi.URLSelectorStrategy;
import org.jboss.jca.adapters.jdbc.util.Injection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import static java.security.AccessController.doPrivileged;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.sql.DataSource;

/**
 * LocalManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class LocalManagedConnectionFactory extends BaseWrapperManagedConnectionFactory
{
   private static final long serialVersionUID = -2751268690794983375L;

   private String driverClass;

   private String dataSourceClass;

   private transient volatile Driver driver;

   private transient DataSource dataSource;

   private String connectionURL;

   private transient URLSelectorStrategy urlSelector;

   /** The connection properties */
   protected String connectionProperties;

   /** The connection properties */
   protected final Properties connectionProps = new Properties();

   /**
    * Constructor
    */
   public LocalManagedConnectionFactory()
   {
      this.driver = null;
      this.dataSource = null;
      this.urlSelector = null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      // check some invariants before they come back to haunt us
      if (driverClass == null && dataSourceClass == null)
         throw new ResourceException(bundle.driverClassNull());

      if (dataSourceClass == null && connectionURL == null && driverClass != null)
         throw new ResourceException(bundle.connectionURLNull());

      if (dataSourceClass != null && connectionProps.size() == 0 && (driverClass == null || connectionURL == null))
          throw new ResourceException(bundle.nonConnectionPropertyDefinedForDatasource(dataSourceClass));

      return super.createConnectionFactory(cm);
   }

   /**
    * Get the value of ConnectionURL.
    *
    * @return value of ConnectionURL.
    */
   public String getConnectionURL()
   {
      return connectionURL;
   }

   /**
    * Set the value of ConnectionURL.
    *
    * @param connectionURL  Value to assign to ConnectionURL.
    */
   public void setConnectionURL(final String connectionURL)
   {
      this.connectionURL = connectionURL;
   }

   /**
    * Get the DriverClass value.
    *
    * @return the DriverClass value.
    */
   public String getDriverClass()
   {
      return driverClass;
   }

   /**
    * Set the DriverClass value.
    *
    * @param driverClass The new DriverClass value.
    */
   public void setDriverClass(final String driverClass)
   {
      this.driverClass = driverClass;
      driver = null;
   }

   /**
    * Get the DataSourceClass value.
    *
    * @return the DataSourceClass value.
    */
   public String getDataSourceClass()
   {
      return dataSourceClass;
   }

   /**
    * Set the DataSourceClass value.
    *
    * @param dataSourceClass The new DataSourceClass value.
    */
   public void setDataSourceClass(final String dataSourceClass)
   {
      this.dataSourceClass = dataSourceClass;
      driver = null;
   }

   /**
    * Get the value of connectionProperties.
    *
    * @return value of connectionProperties.
    */
   public String getConnectionProperties()
   {
      return connectionProperties;
   }

   /**
    * Set the value of connectionProperties.
    *
    * @param connectionProperties  Value to assign to connectionProperties.
    */
   public void setConnectionProperties(String connectionProperties)
   {
      this.connectionProperties = connectionProperties;
      connectionProps.clear();

      if (connectionProperties != null)
      {
         // Map any \ to \\
         connectionProperties = connectionProperties.replaceAll("\\\\", "\\\\\\\\");
         connectionProperties = connectionProperties.replaceAll(";", "\n");

         InputStream is = new ByteArrayInputStream(connectionProperties.getBytes());
         try
         {
            connectionProps.load(is);
         }
         catch (IOException ioe)
         {
            throw new RuntimeException("Could not load connection properties", ioe);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      final Properties props = getConnectionProperties(connectionProps, subject, cri);
      // Some friendly drivers (Oracle, you guessed right) modify the props you supply.
      // Since we use our copy to identify compatibility in matchManagedConnection, we need
      // a pristine copy for our own use.  So give the friendly driver a copy.
      final Properties copy = (Properties) props.clone();
      final Subject copySubject = subject != null ? SecurityActions.createSubject(false, subject) : null;

      if (log.isTraceEnabled())
      {
         // Make yet another copy to mask the password
         Properties logCopy = copy;
         if (copy.getProperty("password") != null)
         {
            logCopy = (Properties) props.clone();
            logCopy.setProperty("password", "--hidden--");
         }
         log.tracef("Using properties: %s", logCopy);
      }

      if (getURLDelimiter() != null && !getURLDelimiter().trim().equals("") && urlSelector == null)
         initUrlSelector();

      if (copySubject != null)
      {
         try
         {
            return doPrivileged(new PrivilegedExceptionAction<ManagedConnection>()
            {
               public ManagedConnection run() throws PrivilegedActionException
               {
                  return Subject.doAs(copySubject, new PrivilegedExceptionAction<ManagedConnection>()
                  {
                     public ManagedConnection run() throws ResourceException
                     {
                        if (urlSelector != null)
                        {
                           return getHALocalManagedConnection(props, copy);
                        }
                        else
                        {
                           return getLocalManagedConnection(props, copy);
                        }
                     }
                  });
               }
            });
         }
         catch (PrivilegedActionException pe)
         {
            if (pe.getException() instanceof PrivilegedActionException
                    && ((PrivilegedActionException) pe.getException()).getException() instanceof ResourceException)
            {
               throw (ResourceException)((PrivilegedActionException) pe.getException()).getException();
            }
            else
            {
               throw new ResourceException(pe);
            }
         }
      }
      else
      {
         if (urlSelector != null)
         {
            return getHALocalManagedConnection(props, copy);
         }
         else
         {
            return getLocalManagedConnection(props, copy);
         }
      }
   }

   private LocalManagedConnection createLocalManagedConnection(final String url, final Properties props,
                                                               final Properties copy)
      throws ResourceException
   {
      if (driverClass != null && driver == null)
      {
         try
         {
            getDriver(url);
         }
         catch (ResourceException re)
         {
            log.debug("Exception while registering driver", re);
         }
      }

      Connection con = null;
      try
      {
         if (dataSourceClass != null && !copy.isEmpty())
         {
            DataSource d = getDataSource();

            ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
            SecurityActions.setThreadContextClassLoader(SecurityActions.getClassLoader(d.getClass()));
            try
            {
               con = d.getConnection(copy.getProperty("user"), copy.getProperty("password"));
               if (con == null)
                  throw new ResourceException(bundle.unableToCreateConnectionFromDataSource());
            }
            finally
            {
               SecurityActions.setThreadContextClassLoader(tccl);
            }
         }
         else if (driverClass != null)
         {
            Driver d = getDriver(url);
            ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
            SecurityActions.setThreadContextClassLoader(SecurityActions.getClassLoader(d.getClass()));
            try
            {
               con = d.connect(url, copy);
               if (con == null)
                  throw new ResourceException(bundle.wrongDriverClass(d.getClass().getName(), url));
            }
            finally
            {
               SecurityActions.setThreadContextClassLoader(tccl);
            }
         }
         else
         {
            throw new ResourceException(bundle.unableToCreateConnection());
         }

         return new LocalManagedConnection(this, con, props, transactionIsolation, preparedStatementCacheSize);
      }
      catch (Throwable e)
      {
         if (con != null)
         {
            try
            {
               con.close();
            }
            catch (Throwable ignored)
            {
               // Ignore
            }
         }
         throw new ResourceException(bundle.unableToCreateConnection(), e);
      }
   }

   private LocalManagedConnection getLocalManagedConnection(final Properties props, final Properties copy)
      throws ResourceException
   {
      return createLocalManagedConnection(getConnectionURL(), props, copy);
   }

   private LocalManagedConnection getHALocalManagedConnection(final Properties props, final Properties copy)
      throws ResourceException
   {
      while (urlSelector.hasMore())
      {
         String url = urlSelector.active();

         log.tracef("Trying to create a connection to %s", url);

         try
         {
            LocalManagedConnection con = createLocalManagedConnection(url, props, copy);
            urlSelector.success(url);
            return con;
         }
         catch (Exception e)
         {
            log.errorCreatingConnection(url, e);
            urlSelector.fail(url);
         }
      }

      // Reset the URL selector for next iteration
      urlSelector.reset();

      // we have supposedly tried all the urls
      throw new ResourceException(bundle.unableToCreateConnectionFromURL(urlSelector.getData()));
   }

   /**
    * Init URL selector
    */
   protected void initUrlSelector()
   {
      List<String> urlsList = new ArrayList<String>();

      StringTokenizer st = new StringTokenizer(getConnectionURL(), urlDelimiter);
      while (st.hasMoreTokens())
      {
         String url = st.nextToken();
         urlsList.add(url);
         log.tracef("added HA connection url: %s", url);
      }

      if (getUrlSelectorStrategyClassName() == null)
      {
         this.urlSelector = new URLSelector();
         this.urlSelector.init(urlsList);
         log.debugf("Default URLSelectorStrategy is being used : %s", urlSelector);
      }
      else
      {
         this.urlSelector = initUrlSelectorClass(getUrlSelectorStrategyClassName(), urlsList);
         log.debugf("Customized URLSelectorStrategy is being used : %s", urlSelector);
      }
   }

   /**
    * Init the URLSelectStrategy
    * @param className The class name
    * @param urls The list with urls
    * @return The URL selector strategy
    */
   private URLSelectorStrategy initUrlSelectorClass(String className, List<String> urls)
   {
      URLSelectorStrategy result = null;

      if (className == null || className.trim().equals(""))
      {
         log.undefinedURLSelectStrategy(getJndiName());
         return null;
      }

      Class<?> clz = null;
      try
      {
         clz = Class.forName(className, true, getClassLoaderPlugin().getClassLoader());
      }
      catch (ClassNotFoundException cnfe)
      {
         // Not found
      }

      if (clz == null)
      {
         try
         {
            clz = Class.forName(className, true, new TCClassLoaderPlugin().getClassLoader());
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
            clz = Class.forName(className, true, SecurityActions.getClassLoader(LocalManagedConnectionFactory.class));
         }
         catch (ClassNotFoundException cnfe)
         {
            // Not found
         }
      }

      if (clz == null)
      {
         log.errorURLSelectStrategy(className, getJndiName());
         return null;
      }

      try
      {
         result = (URLSelectorStrategy)clz.newInstance();

         Method init = clz.getMethod("init", new Class[] {List.class});
         init.invoke(result, new Object[] {urls});
      }
      catch (Throwable t)
      {
         log.errorURLSelectStrategyExt(className, getJndiName(), t);
      }

      return result;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   public ManagedConnection matchManagedConnections(final Set mcs, final Subject subject,
                                                    final ConnectionRequestInfo cri) throws ResourceException
   {
      Properties newProps = getConnectionProperties(connectionProps, subject, cri);

      for (Iterator<?> i = mcs.iterator(); i.hasNext();)
      {
         Object o = i.next();

         if (o instanceof LocalManagedConnection)
         {
            LocalManagedConnection mc = (LocalManagedConnection) o;

            if (Boolean.TRUE.equals(getReauthEnabled()))
            {
               return mc;
            }
            else if (mc.getProperties().equals(newProps))
            {
               return mc;
            }
         }
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      result = result * 37 + ((connectionURL == null) ? 0 : connectionURL.hashCode());
      result = result * 37 + ((driverClass == null) ? 0 : driverClass.hashCode());
      result = result * 37 + ((dataSourceClass == null) ? 0 : dataSourceClass.hashCode());
      result = result * 37 + ((userName == null) ? 0 : userName.hashCode());
      result = result * 37 + ((password == null) ? 0 : password.hashCode());
      result = result * 37 + transactionIsolation;
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;

      if (this == other)
         return true;

      if (getClass() != other.getClass())
         return false;

      LocalManagedConnectionFactory otherMcf = (LocalManagedConnectionFactory) other;

      return this.connectionURL.equals(otherMcf.connectionURL)
         && ((this.driverClass == null) ? otherMcf.driverClass == null : this.driverClass.equals(otherMcf.driverClass))
         && ((this.dataSourceClass == null) ? otherMcf.dataSourceClass == null : 
             this.dataSourceClass.equals(otherMcf.dataSourceClass))
         && ((this.userName == null) ? otherMcf.userName == null : this.userName.equals(otherMcf.userName))
         && ((this.password == null) ? otherMcf.password == null : this.password.equals(otherMcf.password))
         && this.transactionIsolation == otherMcf.transactionIsolation;
   }

   /**
    * Check the driver for the given URL.  If it is not registered already
    * then register it.
    *
    * @param url   The JDBC URL which we need a driver for.
    * @return The driver
    * @exception ResourceException Thrown if an error occurs
    */
   protected synchronized Driver getDriver(final String url) throws ResourceException
   {
      // don't bother if it is loaded already
      if (driver != null)
      {
         return driver;
      }

      log.tracef("Checking driver for URL: %s", url);

      if (driverClass == null)
      {
         throw new ResourceException(bundle.noDriverClassForURL(url));
      }

      try
      {
         // Load class to trigger static initialization of the driver
         Class<?> clazz = Class.forName(driverClass, true, getClassLoaderPlugin().getClassLoader());

         driver = (Driver)clazz.newInstance();

         log.debugf("Driver loaded and instance created:%s", driver);

      }
      catch (Exception e)
      {
         throw new ResourceException(bundle.failedToRegisterDriverClass(driverClass), e);
      }

      return driver;
   }

   /**
    * Get the connection url
    * @return The value
    */
   protected String internalGetConnectionURL()
   {
      return connectionURL;
   }

   /**
    * Get the datasource instance
    * @return The handle
    * @exception ResourceException Thrown if an error occurs
    */
   private synchronized DataSource getDataSource() throws ResourceException
   {
      if (dataSource == null)
      {
         if (dataSourceClass == null || dataSourceClass.trim().equals(""))
            throw new ResourceException(bundle.datasourceClassNull());

         ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
         try
         {
            SecurityActions.setThreadContextClassLoader(getClassLoaderPlugin().getClassLoader());

            Class<?> clz = Class.forName(dataSourceClass, true, getClassLoaderPlugin().getClassLoader());
            dataSource = (DataSource)clz.newInstance();

            if (connectionProps != null)
            {
               Injection injector = new Injection();
               Iterator<Map.Entry<Object, Object>> it = connectionProps.entrySet().iterator();
               while (it.hasNext())
               {
                  Map.Entry<Object, Object> entry = it.next();
                  String key = (String)entry.getKey();
                  String value = (String)entry.getValue();
                  injector.inject(dataSource, key, value);
               }
            }
         }
         catch (Throwable t)
         {
            throw new ResourceException(bundle.failedToLoadDataSource(dataSourceClass), t);
         }
         finally
         {
            SecurityActions.setThreadContextClassLoader(tccl);
         }
      }

      return dataSource;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("LocalManagedConnectionFactory@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[connectionURL=").append(connectionURL);
      sb.append(" driverClass=").append(driverClass);
      sb.append(" userName=").append(userName);
      sb.append(" password=****");
      sb.append(" transactionIsolation=").append(transactionIsolation);
      sb.append("]");

      return sb.toString();
   }
}
