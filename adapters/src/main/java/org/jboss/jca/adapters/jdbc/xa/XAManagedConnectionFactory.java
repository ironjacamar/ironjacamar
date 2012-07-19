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

package org.jboss.jca.adapters.jdbc.xa;

import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnectionFactory;
import org.jboss.jca.adapters.jdbc.classloading.TCClassLoaderPlugin;
import org.jboss.jca.adapters.jdbc.spi.URLXASelectorStrategy;
import org.jboss.jca.adapters.jdbc.spi.XAData;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

/**
 * XAManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class XAManagedConnectionFactory extends BaseWrapperManagedConnectionFactory
{
   private static final long serialVersionUID = 1647927657609573729L;

   private String xaDataSourceClass;

   private String xaDataSourceProperties;

   /** THe XA properties */
   protected final Map<String, String> xaProps = Collections.synchronizedMap(new HashMap<String, String>());

   private Boolean isSameRMOverrideValue;

   private XADataSource xads;

   private String urlProperty;

   private URLXASelectorStrategy xadsSelector;

   /**
    * Constructor
    */
   public XAManagedConnectionFactory()
   {
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
    */
   public void setXADataSourceProperties(String xaDataSourceProperties) throws ResourceException
   {
      this.xaDataSourceProperties = xaDataSourceProperties;
      xaProps.clear();

      if (xaDataSourceProperties != null)
      {
         // Map any \ to \\
         xaDataSourceProperties = xaDataSourceProperties.replaceAll("\\\\", "\\\\\\\\");

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
            throw new ResourceException("Could not load connection properties", ioe);
         }
      }

      initSelector();
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
               log.debug("Default URLXASelectorStrategy is being used : " + xadsSelector);
            }
            else
            {
               xadsSelector = initUrlSelectorClass(getUrlSelectorStrategyClassName(), xaDataList);
               log.debug("Customized URLXASelectorStrategy is being used : " + xadsSelector);
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
         log.error("Unable to load undefined URLXASelectStrategy");
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
            clz = Class.forName(className, true, XAManagedConnectionFactory.class.getClassLoader());
         }
         catch (ClassNotFoundException cnfe)
         {
            log.error("Unable to load: " + className);
         }
      }

      if (clz == null)
      {
         log.error("Unable to load defined URLXASelectStrategy: " + className);
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
         log.error("URLXASelectStrategy:" + t.getMessage(), t);
      }

      return result;
   }

   @SuppressWarnings("unchecked")
   private XADataSource createXaDataSource(Properties p) throws ResourceException
   {
      if (getXADataSourceClass() == null)
      {
         throw new ResourceException("No XADataSourceClass supplied!");
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
            clazz = Class.forName(getXADataSourceClass(), true, XAManagedConnectionFactory.class.getClassLoader());
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new ResourceException("Class not found for XADataSource " + getXADataSourceClass(), cnfe);
         }
      }

      try
      {
         xads = (XADataSource)clazz.newInstance();
         final Class<?>[] noClasses = new Class<?>[]{};
         for (Map.Entry<Object, Object> entry : p.entrySet())
         {
            String name = (String)entry.getKey();
            String value = (String)entry.getValue();
            char firstCharName = Character.toUpperCase(name.charAt(0));
            if (name.length() > 1)
            {
               name = firstCharName + name.substring(1);
            }
            else
            {
               name = "" + firstCharName;
            }

            // This is a bad solution.  On the other hand the only known example
            // of a setter with no getter is for Oracle with password.
            // Anyway, each xadatasource implementation should get its
            // own subclass of this that explicitly sets the
            // properties individually.
            Class<?> type = null;
            try
            {
               Method getter = clazz.getMethod("get" + name, noClasses);
               type = getter.getReturnType();
            }
            catch (NoSuchMethodException e)
            {
               try
               {
                  //HACK for now until we can rethink the XADataSourceProperties variable and pass type information
                  Method isMethod = clazz.getMethod("is" + name, (Class[])null);
                  type = isMethod.getReturnType();
               }
               catch (NoSuchMethodException nsme)
               {
                  type = String.class;
               }
            }

            Method setter = clazz.getMethod("set" + name, new Class<?>[]{type});
            PropertyEditor editor = PropertyEditorManager.findEditor(type);

            if (editor == null)
               throw new ResourceException("No property editor found for type: " + type);

            editor.setAsText(value);
            setter.invoke(xads, new Object[]{editor.getValue()});

         }
      }
      catch (InstantiationException ie)
      {
         throw new ResourceException("Could not create an XADataSource: ", ie);
      }
      catch (IllegalAccessException iae)
      {
         throw new ResourceException("Could not set a property: ", iae);
      }
      catch (IllegalArgumentException iae)
      {
         throw new ResourceException("Could not set a property: ", iae);
      }
      catch (InvocationTargetException ite)
      {
         throw new ResourceException("Could not invoke setter on XADataSource: ", ite);
      }
      catch (NoSuchMethodException nsme)
      {
         throw new ResourceException("Could not find accessor on XADataSource: ", nsme);
      }

      return xads;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
      throws javax.resource.ResourceException
   {
      if (urlProperty != null && !urlProperty.trim().equals("") && xadsSelector == null)
         initSelector();

      if (xadsSelector == null)
      {
         return getXAManagedConnection(subject, cri);
      }

      while (xadsSelector.hasMore())
      {
         XAData xaData = xadsSelector.active();

         if (log.isTraceEnabled())
            log.trace("Trying to create an XA connection to " + xaData.getUrl());

         try
         {
            return getXAManagedConnection(subject, cri);
         }
         catch (ResourceException e)
         {
            log.warn("Failed to create an XA connection to " + xaData.getUrl() + ": " + e.getMessage());
            xadsSelector.fail(xaData);
         }
      }

      xadsSelector.reset();

      // we have supposedly tried all the urls
      throw new ResourceException("Could not create connection using any of the URLs: " +
                                  xadsSelector.getData());
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
      Properties props = getConnectionProperties(subject, cri);

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
         throw new ResourceException("Could not create connection", e);
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
      Properties newProps = getConnectionProperties(subject, cri);
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
      if (this == other)
         return true;

      if (getClass() != other.getClass())
         return false;

      XAManagedConnectionFactory otherMcf = (XAManagedConnectionFactory) other;
      return this.xaDataSourceClass.equals(otherMcf.xaDataSourceClass) && this.xaProps.equals(otherMcf.xaProps)
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
            throw new ResourceException("No XADataSourceClass supplied!");

         try
         {
            Class<?> clazz = Class.forName(xaDataSourceClass, true, getClassLoaderPlugin().getClassLoader());

            xads = (XADataSource) clazz.newInstance();
            final Class<?>[] noClasses = new Class<?>[] {};

            for (Map.Entry<String, String> entry : xaProps.entrySet())
            {
               String name = entry.getKey();
               String value = entry.getValue();
               char firstCharName = Character.toUpperCase(name.charAt(0));

               if (name.length() > 1)
               {
                  name = firstCharName + name.substring(1);
               }
               else
               {
                  name = "" + firstCharName;
               }

               // This is a bad solution.  On the other hand the only known example
               // of a setter with no getter is for Oracle with password.
               // Anyway, each xadatasource implementation should get its
               // own subclass of this that explicitly sets the
               // properties individually.

               Class<?> type = null;
               try
               {
                  Method getter = clazz.getMethod("get" + name, noClasses);
                  type = getter.getReturnType();
               }
               catch (NoSuchMethodException e)
               {
                  try
                  {
                     //HACK for now until we can rethink the XADataSourceProperties variable and pass type information
                     Method isMethod = clazz.getMethod("is" + name, (Class[])null);
                     type = isMethod.getReturnType();
                  }
                  catch (NoSuchMethodException nsme)
                  {
                     type = String.class;
                  }
               }

               Method setter = clazz.getMethod("set" + name, new Class<?>[] {type});
               PropertyEditor editor = PropertyEditorManager.findEditor(type);

               if (editor == null)
                  throw new ResourceException("No property editor found for type: " + type);

               editor.setAsText(value);
               setter.invoke(xads, new Object[] {editor.getValue()});
            }
         }
         catch (ClassNotFoundException cnfe)
         {
            throw new ResourceException("Class not found for XADataSource " + xaDataSourceClass, cnfe);
         }
         catch (InstantiationException ie)
         {
            throw new ResourceException("Could not create an XADataSource: ", ie);
         }
         catch (IllegalAccessException iae)
         {
            throw new ResourceException("Could not set a property: ", iae);
         }
         catch (IllegalArgumentException iae)
         {
            throw new ResourceException("Could not set a property: ", iae);
         }
         catch (InvocationTargetException ite)
         {
            throw new ResourceException("Could not invoke setter on XADataSource: ", ite);
         }
         catch (NoSuchMethodException nsme)
         {
            throw new ResourceException("Could not find accessor on XADataSource: ", nsme);
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
