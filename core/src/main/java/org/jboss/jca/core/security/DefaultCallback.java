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

package org.jboss.jca.core.security;

import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.security.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;

/**
 * A default implementation of the callback security SPI.
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DefaultCallback extends AbstractCallback implements Callback
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, DefaultCallback.class.getName());

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
      if (defaultGroups == null)
         return null;

      return Arrays.copyOf(defaultGroups, defaultGroups.length);
   }

   /**
    * Set the default groups
    * @param value The value
    */
   public void setDefaultGroups(String[] value)
   {
      if (value != null)
      {
         defaultGroups = Arrays.copyOf(value, value.length);
      }
      else
      {
         defaultGroups = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public Principal mapPrincipal(String name)
   {
      String mapping = principals.get(name);

      if (mapping != null)
      {
         return new SimplePrincipal(mapping);
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
               log.tracef("callback.properties: Using file: %s", file);

               is = new FileInputStream(f);
            }
         }

         if (is == null)
         {
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

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.writeBoolean(mappingRequired);
      out.writeUTF(domain);

      if (defaultPrincipal != null)
      {
         out.writeBoolean(true);
         out.writeUTF(defaultPrincipal.getName());
      }
      else
      {
         out.writeBoolean(false);
      }

      out.writeObject(defaultGroups);

      out.writeInt(principals.size());
      if (principals.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = principals.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
         }
      }

      out.writeInt(groups.size());
      if (groups.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = groups.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
         }
      }

      out.writeUTF(file);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      mappingRequired = in.readBoolean();
      domain = in.readUTF();

      if (in.readBoolean())
         defaultPrincipal = new SimplePrincipal(in.readUTF());

      defaultGroups = (String[])in.readObject();

      int i = in.readInt();
      if (i > 0)
      {
         for (int j = 1; j <= i; j++)
         {
            String from = in.readUTF();
            String to = in.readUTF();

            principals.put(from, to);
         }
      }

      i = in.readInt();
      if (i > 0)
      {
         for (int j = 1; j <= i; j++)
         {
            String from = in.readUTF();
            String to = in.readUTF();

            groups.put(from, to);
         }
      }

      file = in.readUTF();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("DefaultCallback@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[mappingRequired=").append(mappingRequired);
      sb.append(" domain=").append(domain);
      sb.append(" defaultPrincipal=").append(defaultPrincipal);
      sb.append(" defaultGroups=").append(defaultGroups == null ? "null" : Arrays.toString(defaultGroups));
      sb.append(" principals=").append(principals);
      sb.append(" groups=").append(groups);
      sb.append(" file=").append(file);
      sb.append("]");

      return sb.toString();
   }
}
