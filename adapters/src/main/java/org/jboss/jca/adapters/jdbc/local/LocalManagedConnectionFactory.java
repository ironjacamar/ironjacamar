/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.sql.DataSource;

/**
 * LocalManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class LocalManagedConnectionFactory extends BaseWrapperManagedConnectionFactory
{
   private static final long serialVersionUID = -2751268690794983375L;

   private String driverClass;

   private String dataSourceClass;

   private transient Driver driver;

   private transient DataSource dataSource;

   private String connectionURL;

   private transient URLSelectorStrategy urlSelector;

   /** The connection properties */
   protected String connectionProperties;

   /** The connection properties */
   protected final Properties connectionProps = new Properties();

   private static Map<String, Driver> driverCache = new ConcurrentHashMap<String, Driver>();

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
         throw new ResourceException("driverClass is null");

      if (connectionURL == null && driverClass != null)
         throw new ResourceException("connectionURL is null");

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
   public void setConnectionURL(final String connectionURL) //throws ResourceException
   {
      this.connectionURL = connectionURL;

      if (urlDelimiter != null)
         initUrlSelector();
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
      if (log.isTraceEnabled())
      {
         // Make yet another copy to mask the password
         Properties logCopy = copy;
         if (copy.getProperty("password") != null)
         {
            logCopy = (Properties) props.clone();
            logCopy.setProperty("password", "--hidden--");
         }
         log.trace("Using properties: " + logCopy);
      }

      if (getURLDelimiter() != null && !getURLDelimiter().trim().equals("") && urlSelector == null)
         initUrlSelector();

      if (subject != null)
      {
         try
         {
            return Subject.doAs(subject, new PrivilegedExceptionAction<ManagedConnection>()
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
         catch (PrivilegedActionException pe)
         {
            if (pe.getException() instanceof ResourceException)
            {
               throw (ResourceException)pe.getException();
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

   private LocalManagedConnection getLocalManagedConnection(final Properties props, final Properties copy)
      throws ResourceException
   {
      Connection con = null;
      try
      {
         if (driverClass != null)
         {
            String url = getConnectionURL();
            Driver d = getDriver(url);
            con = d.connect(url, copy);
            if (con == null)
               throw new ResourceException("Wrong driver class [" + d.getClass() + "] for this connection URL [" +
                                           url + "]");
         }
         else
         {
            DataSource d = getDataSource();
            con = d.getConnection(copy.getProperty("user"), copy.getProperty("password"));
            if (con == null)
               throw new ResourceException("Unable to create connection from datasource");
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
         throw new ResourceException("Could not create connection", e);
      }
   }

   private LocalManagedConnection getHALocalManagedConnection(final Properties props, final Properties copy)
      throws ResourceException
   {
      boolean trace = log.isTraceEnabled();

      while (urlSelector.hasMore())
      {
         String url = urlSelector.active();

         if (trace)
            log.tracef("Trying to create a connection to %s", url);

         Connection con = null;
         try
         {
            Driver d = getDriver(url);
            con = d.connect(url, copy);
            if (con == null)
            {
               log.warn("Wrong driver class [" + d.getClass() + "] for this connection URL: " + url);
               urlSelector.fail(url);
            }
            else
            {
               return new LocalManagedConnection(this, con, props, transactionIsolation, preparedStatementCacheSize);
            }
         }
         catch (Exception e)
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
            log.warn("Failed to create connection for " + url + ": " + e.getMessage());
            urlSelector.fail(url);
         }
      }

      // Reset the URL selector for next iteration
      urlSelector.reset();

      // we have supposedly tried all the urls
      throw new ResourceException("Could not create connection using any of the URLs: " +
                                  urlSelector.getData());
   }

   /**
    * Init URL selector
    */
   protected void initUrlSelector()
   {
      boolean trace = log.isTraceEnabled();
      List<String> urlsList = new ArrayList<String>();

      StringTokenizer st = new StringTokenizer(getConnectionURL(), urlDelimiter);
      while (st.hasMoreTokens())
      {
         String url = st.nextToken();
         urlsList.add(url);
         if (trace)
            log.trace("added HA connection url: " + url);
      }

      if (getUrlSelectorStrategyClassName() == null)
      {
         this.urlSelector = new URLSelector();
         this.urlSelector.init(urlsList);
         log.debug("Default URLSelectorStrategy is being used : " + urlSelector);
      }
      else
      {
         this.urlSelector = initUrlSelectorClass(getUrlSelectorStrategyClassName(), urlsList);
         log.debug("Customized URLSelectorStrategy is being used : " + urlSelector);
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
         log.error("Unable to load undefined URLSelectStrategy");
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
            log.error("Unable to load: " + className);
         }
      }

      if (clz == null)
      {
         log.error("Unable to load defined URLSelectStrategy: " + className);
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
         log.error("URLSelectStrategy:" + t.getMessage(), t);
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
               //Next check to see if we are validating on matchManagedConnections
               if ((getValidateOnMatch() && mc.checkValid()) || !getValidateOnMatch())
               {
                  return mc;
               }
            }
         }
      }

      return null;
   }

   /**
    * Is the properties equal
    * @param other The other properties
    * @return True if equal, otherwise false
    */
   private boolean isEqual(Properties other)
   {
      synchronized (connectionProps)
      {
         return connectionProps.equals(other);
      }
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
      boolean trace = log.isTraceEnabled();

      // don't bother if it is loaded already
      if (driver != null)
      {
         return driver;
      }

      if (trace)
         log.trace("Checking driver for URL: " + url);

      if (driverClass == null)
      {
         throw new ResourceException("No Driver class specified (url = " + url + ")!");
      }

      String driverKey = url.substring(0, url.indexOf(":", 6));

      // Check if the driver is already loaded, if not then try to load it
      driver = driverCache.get(driverKey);
      if (driver != null)
         return driver;

      try
      {
         // Load class to trigger static initialization of the driver
         Class<?> clazz = Class.forName(driverClass, true, getClassLoaderPlugin().getClassLoader());

         if (isDriverLoadedForURL(url))
            return driver;

         driver = (Driver)clazz.newInstance();

         DriverManager.registerDriver(driver);
         log.debug("Driver loaded and instance created:" + driver);

         driverCache.put(driverKey, driver);
      }
      catch (Exception e)
      {
         throw new ResourceException("Failed to register driver for: " + driverClass, e);
      }

      return driver;
   }

   private boolean isDriverLoadedForURL(String url)
   {
      boolean trace = log.isTraceEnabled();

      ClassLoader tccl = SecurityActions.getThreadContextClassLoader();
      try
      {
         SecurityActions.setThreadContextClassLoader(getClassLoaderPlugin().getClassLoader());
         driver = DriverManager.getDriver(url);

         if (trace)
            log.trace("Driver already registered for url: " + url);

         return true;
      }
      catch (Exception e)
      {
         if (trace)
            log.trace("Driver not yet registered for url: " + url);

         return false;
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(tccl);
      }
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
            throw new ResourceException("DataSourceClass not defined");

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
            throw new ResourceException("Failed to load datasource: " + dataSourceClass, t);
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
