/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.xa;

import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnectionFactory;
import org.jboss.jca.adapters.jdbc.classloading.TCClassLoaderPlugin;
import org.jboss.jca.adapters.jdbc.spi.URLXASelectorStrategy;
import org.jboss.jca.adapters.jdbc.spi.XAData;
import org.jboss.jca.adapters.jdbc.util.Injection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import static java.security.AccessController.doPrivileged;

/**
 * XAManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:abrock@redhat.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XAManagedConnectionFactory extends BaseWrapperManagedConnectionFactory
{
   private static final long serialVersionUID = 1647927657609573729L;

   private String xaDataSourceClass;

   private String xaDataSourceProperties;

   /** THe XA properties */
   protected final Map<String, String> xaProps;

   private Boolean isSameRMOverrideValue;

   private transient XADataSource xads;

   private String urlProperty;

   private transient URLXASelectorStrategy xadsSelector;

   /**
    * Constructor
    */
   public XAManagedConnectionFactory(Map<String, String> xaDataSourceProps)
   {
      this.xads = null;
      this.xadsSelector = null;
      this.xaProps = xaDataSourceProps == null? Collections.emptyMap(): Collections.unmodifiableMap(xaDataSourceProps);
   }

   /**
    * Constructor.
    * @deprecated this constructor internally requires the use of synchronized map instance.
    * This type of map can cause deadlocks when comparing two connection factories. For that reason, prefer to use
    * {@link #XAManagedConnectionFactory(Map)} instead.
    */
   @Deprecated
   public XAManagedConnectionFactory()
   {
      this.xads = null;
      this.xadsSelector = null;
      // this constructor sets xaDataSourceProps map with a synchronized map instance, which can lead to deadlock
      // when comparing two connection factories
      xaProps = Collections.synchronizedMap(new HashMap<String, String>());
   }

   /**
    * Get the URL property
    * @return The value
    */
   public String getURLProperty()
   {
      return urlProperty;
   }

   /**
    * Set the URL property
    * @param urlProperty The value
    */
   public void setURLProperty(String urlProperty)
   {
      this.urlProperty = urlProperty;
   }

   /**
    * Get the XaDataSourceClass value.
    * @return the XaDataSourceClass value.
    */
   public String getXADataSourceClass()
   {
      return xaDataSourceClass;
   }

   /**
    * Set the XaDataSourceClass value.
    * @param xaDataSourceClass The new XaDataSourceClass value.
    */
   public void setXADataSourceClass(String xaDataSourceClass)
   {
      this.xaDataSourceClass = xaDataSourceClass;
   }

   /**
    * Get the XADataSourceProperties value.
    * @return the XADataSourceProperties value.
    */
   public String getXADataSourceProperties()
   {
      return xaDataSourceProperties;
   }

   /**
    * Set the XADataSourceProperties value.
    * @param xaDataSourceProperties The new XADataSourceProperties value.
    * @exception ResourceException Thrown in case of an error
    * @deprecated this method requires the internal usage of a synchronized map, and will only work with
    * objects created using the deprecated {@link #XAManagedConnectionFactory()} constructor. Because this
    * can lead to a deadlock when comparing two instances of this class, prefer to define the data source
    * properties at the {@link #XAManagedConnectionFactory(Map)} constructor instead.
    */
   @Deprecated
   public void setXADataSourceProperties(String xaDataSourceProperties) throws ResourceException
   {
      this.xaDataSourceProperties = xaDataSourceProperties;
      xaProps.clear();

      if (xaDataSourceProperties != null)
      {
         // Map any \ to \\
         xaDataSourceProperties = xaDataSourceProperties.replaceAll("\\\\", "\\\\\\\\");
         // Map any ; to \n
         xaDataSourceProperties = xaDataSourceProperties.replace(';', '\n');

         InputStream is = new ByteArrayInputStream(xaDataSourceProperties.getBytes());
         try
         {
            Properties p = new Properties();
            p.load(is);

            for (Map.Entry<Object, Object> entry : p.entrySet())
            {
               xaProps.put((String)entry.getKey(), (String)entry.getValue());
            }
         }
         catch (IOException ioe)
         {
            throw new ResourceException(bundle.unableToLoadConnectionProperties(), ioe);
         }
      }
   }

   /**
    * Get the IsSameRMOverrideValue value.
    * @return the IsSameRMOverrideValue value.
    */
   public Boolean getIsSameRMOverrideValue()
   {
      return isSameRMOverrideValue;
   }

   /**
    * Set the IsSameRMOverrideValue value.
    * @param isSameRMOverrideValue The new IsSameRMOverrideValue value.
    */
   public void setIsSameRMOverrideValue(Boolean isSameRMOverrideValue)
   {
      this.isSameRMOverrideValue = isSameRMOverrideValue;
   }

   @SuppressWarnings("unchecked")
   private void initSelector() throws ResourceException
   {
      boolean trace = log.isTraceEnabled();

      if (urlProperty != null && !urlProperty.trim().equals(""))
      {
         String urlsStr = xaProps.get(urlProperty);
         if (urlsStr != null && urlsStr.trim().length() > 0 &&
             urlDelimiter != null && urlDelimiter.trim().length() > 0)
         {
            List<XAData> xaDataList = new ArrayList<XAData>(2);

            Properties xaPropsCopy = new Properties();
            for (Map.Entry<String, String> entry : xaProps.entrySet())
            {
               xaPropsCopy.put(entry.getKey(), entry.getValue());
            }

            StringTokenizer st = new StringTokenizer(urlsStr, urlDelimiter);
            while (st.hasMoreTokens())
            {
               String url = st.nextToken();
               xaPropsCopy.setProperty(urlProperty, url);
               XADataSource xads = createXaDataSource(xaPropsCopy);
               xaDataList.add(new XAData(xads, url));

               if (trace)
                  log.trace("added XA HA connection url: " + url);
            }

            if (getUrlSelectorStrategyClassName() == null)
            {
               xadsSelector = new URLXASelector();
               xadsSelector.init(xaDataList);
               log.debugf("Default URLXASelectorStrategy is being used : %s", xadsSelector);
            }
            else
            {
               xadsSelector = initUrlSelectorClass(getUrlSelectorStrategyClassName(), xaDataList);
               log.debugf("Customized URLXASelectorStrategy is being used : %s", xadsSelector);
            }
         }
      }
   }

   /**
    * Init the URLXASelectStrategy
    * @param className The class name
    * @param urls The list with urls
    * @return The URL selector strategy
    */
   private URLXASelectorStrategy initUrlSelectorClass(String className, List<XAData> xaDatas)
   {
      URLXASelectorStrategy result = null;

      if (className == null || className.trim().equals(""))
      {
         log.undefinedURLXASelectStrategy(getJndiName());
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
            clz = Class.forName(className, true, SecurityActions.getClassLoader(XAManagedConnectionFactory.class));
         }
         catch (ClassNotFoundException cnfe)
         {
            // Not found
         }
      }

      if (clz == null)
      {
         log.errorURLXASelectStrategy(className, getJndiName());
         return null;
      }

      try
      {
         result = (URLXASelectorStrategy)clz.newInstance();

         Method init = clz.getMethod("init", new Class[] {List.class});
         init.invoke(result, new Object[] {xaDatas});
      }
      catch (Throwable t)
      {
         log.errorURLXASelectStrategyExt(className, getJndiName(), t);
      }

      return result;
   }

   @SuppressWarnings("unchecked")
   private XADataSource createXaDataSource(Properties p) throws ResourceException
   {
      if (getXADataSourceClass() == null)
      {
         throw new ResourceException(bundle.xaDatasourceClassNull());
      }

      XADataSource xads = null;
      Class<?> clazz = null;

      try
      {
         clazz = Class.forName(getXADataSourceClass(), true, getClassLoaderPlugin().getClassLoader());
      }
      catch (ClassNotFoundException cnfe)
      {
         // Ignore
      }

      if (clazz == null)
      {
         try
         {
            clazz = Class.forName(getXADataSourceClass(), true, new TCClassLoaderPlugin().getClassLoader());
         }
         catch (ClassNotFoundException cnfe)
         {
            // Ignore
         }
      }

      if (clazz == null)
      {
         try
         {
            clazz = Class.forName(getXADataSourceClass(), true,
                                  SecurityActions.getClassLoader(XAManagedConnectionFactory.class));
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new ResourceException(bundle.failedToLoadXADataSource(getXADataSourceClass()), cnfe);
         }
      }

      try
      {
         Injection injector = new Injection();

         xads = (XADataSource)clazz.newInstance();
         for (Map.Entry<Object, Object> entry : p.entrySet())
         {
            String name = (String)entry.getKey();
            String value = (String)entry.getValue();

            injector.inject(xads, name, value);
         }
      }
      catch (Throwable t)
      {
         throw new ResourceException(bundle.failedToLoadXADataSource(getXADataSourceClass()), t);
      }

      return xads;
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection createManagedConnection(final Subject subject, final ConnectionRequestInfo cri)
      throws javax.resource.ResourceException
   {
      if (urlProperty != null && !urlProperty.trim().equals("") && xadsSelector == null)
         initSelector();

      if (xadsSelector == null)
      {
         final Subject copySubject = subject != null ? SecurityActions.createSubject(false, subject) : null;

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
                           return getXAManagedConnection(copySubject, cri);
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
            return getXAManagedConnection(subject, cri);
         }
      }

      while (xadsSelector.hasMore())
      {
         XAData xaData = xadsSelector.active();

         if (log.isTraceEnabled())
            log.trace("Trying to create an XA connection to " + xaData.getUrl());

         final Subject copySubject = subject != null ? SecurityActions.createSubject(false, subject) : null;

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
                           return getXAManagedConnection(copySubject, cri);
                        }
                     });
                  }
               });
            }
            catch (PrivilegedActionException pe)
            {
               log.errorCreatingXAConnection(xaData.getUrl(), (pe.getException() instanceof PrivilegedActionException)
                       ? ((PrivilegedActionException) pe.getException()).getException()
                       : pe.getException());
               xadsSelector.fail(xaData);
            }
         }
         else
         {
            try
            {
               return getXAManagedConnection(subject, cri);
            }
            catch (ResourceException e)
            {
               log.errorCreatingXAConnection(xaData.getUrl(), e);
               xadsSelector.fail(xaData);
            }
         }
      }

      xadsSelector.reset();

      // we have supposedly tried all the urls
      throw new ResourceException(bundle.unableToCreateConnectionFromURL(xadsSelector.getData()));
   }

   /**
    * Get the managed connection
    * @param subject The subject
    * @param cri The connection request info
    * @return The connection
    * @exception ResourceException Thrown if an error occurs
    */
   public ManagedConnection getXAManagedConnection(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      XAConnection xaConnection = null;
      Properties props = getConnectionProperties(null, subject, cri);

      try
      {
         final String user = props.getProperty("user");
         final String password = props.getProperty("password");

         xaConnection = (user != null)
            ? getXADataSource().getXAConnection(user, password)
            : getXADataSource().getXAConnection();

         return newXAManagedConnection(props, xaConnection);
      }
      catch (Throwable e)
      {
         try
         {
            if (xaConnection != null)
               xaConnection.close();
         }
         catch (Throwable ignored)
         {
            // Ignore
         }
         throw new ResourceException(bundle.unableToCreateConnection(), e);
      }
   }

   /**
    * This method can be overwritten by sublcasses to provide rm specific
    * implementation of XAManagedConnection
    * @param props The properties
    * @param xaConnection The XA connection
    * @return The managed connection
    * @exception SQLException Thrown if an error occurs
    */
   protected ManagedConnection newXAManagedConnection(Properties props, XAConnection xaConnection) throws SQLException
   {
      return new XAManagedConnection(this, xaConnection, props, transactionIsolation, preparedStatementCacheSize);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   public ManagedConnection matchManagedConnections(Set mcs, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      Properties newProps = getConnectionProperties(null, subject, cri);
      for (Iterator<?> i = mcs.iterator(); i.hasNext();)
      {
         Object o = i.next();
         if (o instanceof XAManagedConnection)
         {
            XAManagedConnection mc = (XAManagedConnection) o;

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
    * Is the properties equal
    * @param other The other properties
    * @return True if equal, otherwise false
    */
   private boolean isEqual(Map<String, String> other)
   {
      synchronized (xaProps)
      {
         return xaProps.equals(other);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      result = result * 37 + ((xaDataSourceClass == null) ? 0 : xaDataSourceClass.hashCode());
      result = result * 37 + xaProps.hashCode();
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

      XAManagedConnectionFactory otherMcf = (XAManagedConnectionFactory) other;
      return this.xaDataSourceClass.equals(otherMcf.xaDataSourceClass) && isEqual(otherMcf.xaProps)
         && ((this.userName == null) ? otherMcf.userName == null : this.userName.equals(otherMcf.userName))
         && ((this.password == null) ? otherMcf.password == null : this.password.equals(otherMcf.password))
         && this.transactionIsolation == otherMcf.transactionIsolation;
   }

   /**
    * Get the XA datasource
    * @return The value
    * @exception ResourceException Thrown if an error occurs
    */
   @SuppressWarnings("unchecked")
   protected synchronized XADataSource getXADataSource() throws ResourceException
   {
      if (xadsSelector != null)
      {
         XAData xada = xadsSelector.active();
         return xada.getXADataSource();
      }

      if (xads == null)
      {
         if (xaDataSourceClass == null)
            throw new ResourceException(bundle.xaDatasourceClassNull());

         try
         {
            Class<?> clazz = Class.forName(xaDataSourceClass, true, getClassLoaderPlugin().getClassLoader());

            xads = (XADataSource) clazz.newInstance();
            Injection injector = new Injection();

            for (Map.Entry<String, String> entry : xaProps.entrySet())
            {
               String name = entry.getKey();
               String value = entry.getValue();

               injector.inject(xads, name, value);
            }
         }
         catch (Throwable t)
         {
            throw new ResourceException(bundle.failedToLoadXADataSource(getXADataSourceClass()), t);
         }
      }
      return xads;
   }

   /**
    * {@inheritDoc}
    */
   public String toString() 
   {
      StringBuilder sb = new StringBuilder();

      sb.append("XAManagedConnectionFactory@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[xaDataSourceClass=").append(xaDataSourceClass);
      sb.append(" xaProps=").append(Integer.toHexString(System.identityHashCode(xaProps)));
      sb.append(" userName=").append(userName);
      sb.append(" password=****");
      sb.append(" transactionIsolation=").append(transactionIsolation);
      sb.append("]");

      return sb.toString();
   }
}
