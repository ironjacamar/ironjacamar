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

package org.jboss.jca.core.security;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.security.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;

/**
 * A default implementation of the callback security SPI.
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class DefaultCallback implements Callback
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, DefaultCallback.class.getName());

   /** Trace */
   private static boolean trace = log.isTraceEnabled();

   /** Default callback.properties file name */
   private static final String DEFAULT_CALLBACK_PROPERTIES = "callback.properties";

   /** Is mapping required */
   private boolean mappingRequired;

   /** The domain */
   private String domain;

   /** The default principal */
   private Principal defaultPrincipal;

   /** The default groups */
   private String[] defaultGroups;

   /** The principal map */
   private Map<String, String> principals;

   /** The groups map */
   private Map<String, String> groups;

   /** The configuration file */
   private String file;

   /**
    * Constructor
    */
   public DefaultCallback()
   {
      this(null);
   }

   /**
    * Constructor
    * @param file The file
    */
   public DefaultCallback(String file)
   {
      this.mappingRequired = false;
      this.domain = null;
      this.defaultPrincipal = null;
      this.defaultGroups = null;
      this.principals = new HashMap<String, String>();
      this.groups = new HashMap<String, String>();
      this.file = file;
   }

   /**
    * {@inheritDoc}
    */
   public String getDomain()
   {
      return domain;
   }

   /**
    * Set the domain
    * @param v The value
    */
   public void setDomain(String v)
   {
      this.domain = v;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isMappingRequired()
   {
      return mappingRequired;
   }

   /**
    * Set the user mapping required
    * @param value The value
    */
   public void setMappingRequired(boolean value)
   {
      mappingRequired = value;
   }

   /**
    * {@inheritDoc}
    */
   public Principal getDefaultPrincipal()
   {
      return defaultPrincipal;
   }

   /**
    * Set the default principal
    * @param value The value
    */
   public void setDefaultPrincipal(Principal value)
   {
      defaultPrincipal = value;
   }

   /**
    * {@inheritDoc}
    */
   public String[] getDefaultGroups()
   {
      return defaultGroups;
   }

   /**
    * Set the default groups
    * @param value The value
    */
   public void setDefaultGroups(String[] value)
   {
      defaultGroups = value;
   }

   /**
    * {@inheritDoc}
    */
   public Principal mapPrincipal(String name)
   {
      String mapping = principals.get(name);

      if (mapping != null)
      {
         return new SimplePrincipal(name);
      }

      return null;
   }

   /**
    * Add a principal mapping
    * @param from The from name
    * @param to The to name
    */
   public void addPrincipalMapping(String from, String to)
   {
      principals.put(from, to);
   }

   /**
    * {@inheritDoc}
    */
   public String mapGroup(String name)
   {
      return groups.get(name);
   }

   /**
    * Add a group mapping
    * @param from The from name
    * @param to The to name
    */
   public void addGroupMapping(String from, String to)
   {
      groups.put(from, to);
   }

   /**
    * Set the file name
    * @param value The value
    */
   public void setFile(String value)
   {
      file = value;
   }

   /**
    * {@inheritDoc}
    */
   public void start() throws Throwable
   {
      InputStream is = null;

      try
      {
         if (file != null)
         {
            File f = new File(file);

            if (f.exists())
            {
               if (trace)
                  log.trace("callback.properties: Using file: " + file);

               is = new FileInputStream(f);
            }
         }

         if (is == null)
         {
            if (trace)
               log.trace("callback.properties: Using classloader");

            is = SecurityActions.getResourceAsStream(DEFAULT_CALLBACK_PROPERTIES);
         }

         if (is != null)
         {
            Properties p = new Properties();
            p.load(is);

            if (p.size() > 0)
            {
               Iterator<Map.Entry<Object, Object>> entries = p.entrySet().iterator();
               while (entries.hasNext())
               {
                  Map.Entry<Object, Object> entry = entries.next();
                  String key = (String)entry.getKey();
                  String value = (String)entry.getValue();

                  if ("mapping-required".equals(key))
                  {
                     mappingRequired = Boolean.valueOf(value);
                  }
                  else if ("domain".equals(key))
                  {
                     domain = value;
                  }
                  else if ("default-principal".equals(key))
                  {
                     if (value != null && !value.trim().equals(""))
                        defaultPrincipal = new SimplePrincipal(value);
                  }
                  else if ("default-groups".equals(key))
                  {
                     if (value != null && !value.trim().equals(""))
                     {
                        StringTokenizer st = new StringTokenizer(",");
                        List<String> groups = new ArrayList<String>();
                        while (st.hasMoreTokens())
                        {
                           groups.add(st.nextToken().trim());
                        }
                        defaultGroups = groups.toArray(new String[groups.size()]);
                     }
                  }
                  else if (key.startsWith("map.user"))
                  {
                     if (value != null && value.contains("=>"))
                     {
                        int index = value.indexOf("=>");
                        String from = value.substring(0, index);
                        String to = value.substring(index + 2);
                        addPrincipalMapping(from, to);
                     }
                  }
                  else if (key.startsWith("map.group"))
                  {
                     if (value != null && value.contains("=>"))
                     {
                        int index = value.indexOf("=>");
                        String from = value.substring(0, index);
                        String to = value.substring(index + 2);
                        addGroupMapping(from, to);
                     }
                  }
               }
            }
            else
            {
               if (log.isDebugEnabled())
                  log.debug("Empty callback.properties file");
            }
         }
         else
         {
            log.noCallbackPropertiesFound();
         }
      }
      catch (IOException ioe)
      {
         log.errorWhileLoadingCallbackProperties(ioe);
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void stop() throws Throwable
   {
      principals.clear();
      groups.clear();
   }
}
