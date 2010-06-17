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

package org.jboss.jca.core.security;

import org.jboss.jca.core.spi.security.Callback;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;

/**
 * An user / role implementation based on property files
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Rev: 97162 $
 */
public class UsersRoles implements Callback
{
   /** Serial version uid */
   private static final long serialVersionUID = 1L;

   /** Log instance */
   private static Logger log = Logger.getLogger(UsersRoles.class);

   /** Trace */
   private static boolean trace = log.isTraceEnabled();

   /** Default users.properties file name */
   private static final String DEFAULT_USERS_PROPERTIES = "users.properties";

   /** Default roles.properties file name */
   private static final String DEFAULT_ROLES_PROPERTIES = "roles.properties";

   /** The users map */
   private Map<String, String> users;

   /** The roles map */
   private Map<String, Set<String>> roles;

   /** users.properties name */
   private String usersProperties;

   /** roles.properties name */
   private String rolesProperties;

   /**
    * Constructor
    */
   public UsersRoles()
   {
      users = new HashMap<String, String>();
      roles = new HashMap<String, Set<String>>();
      usersProperties = null;
      rolesProperties = null;
   }

   /**
    * Get the users.properties file name
    * @return The value
    */
   public String getUsersProperties()
   {
      return usersProperties;
   }

   /**
    * Set the users.properties file name
    * @param value The value
    */
   public void setUsersProperties(String value)
   {
      usersProperties = value;
   }

   /**
    * Get the roles.properties file name
    * @return The value
    */
   public String getRolesProperties()
   {
      return rolesProperties;
   }

   /**
    * Set the roles.properties file name
    * @param value The value
    */
   public void setRolesProperties(String value)
   {
      rolesProperties = value;
   }

   /**
    * Get the users
    * @return A set of user names
    */
   public Set<String> getUsers()
   {
      Set<String> s = users.keySet();
      
      if (s != null)
         return Collections.unmodifiableSet(s);

      return null;
   }

   /**
    * Get the credential for an user
    * @param user The user name
    * @return The credential; <code>null</code> if user doesn't exists
    */
   public char[] getCredential(String user)
   {
      String pwd = users.get(user);

      if (pwd != null)
         return pwd.toCharArray();

      return null;
   }

   /**
    * Get the roles for an user
    * @param user The user name
    * @return A set of roles; <code>null</code> if user doesn't exists
    */
   public String[] getRoles(String user)
   {
      Set<String> s = roles.get(user);

      if (s != null)
      {
         String[] result = new String[s.size()];
         return s.toArray(result);
      }

      return null;
   }

   /**
    * Start
    * @exception Throwable Thrown if an error occurs
    */
   public void start() throws Throwable
   {
      InputStream is = null;

      try
      {
         if (usersProperties != null)
         {
            if (trace)
               log.trace("users.properties: Using file: " + usersProperties);

            is = new FileInputStream(usersProperties);
         }
         else
         {
            if (trace)
               log.trace("users.properties: Using classloader");

            is = SecurityActions.getResourceAsStream(DEFAULT_USERS_PROPERTIES);
         }

         if (is != null)
         {
            Properties p = new Properties();
            p.load(is);
            
            Set<?> keys = p.keySet();
            if (keys != null && keys.size() > 0)
            {
               Iterator it = keys.iterator();
               while (it.hasNext())
               {
                  String user = (String)it.next();

                  if (log.isDebugEnabled())
                     log.debug("Adding user: " + user);

                  users.put(user, p.getProperty(user));
               }
            }
            else
            {
               if (log.isDebugEnabled())
                  log.debug("Empty users.properties file");
            }
         }
         else
         {
            log.warn("No users.properties were found");
         }
      }
      catch (IOException ioe)
      {
         log.error("Error while loading users.properties", ioe);
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

      try
      {
         if (rolesProperties != null)
         {
            if (trace)
               log.trace("roles.properties: Using file: " + rolesProperties);

            is = new FileInputStream(rolesProperties);
         }
         else
         {
            if (trace)
               log.trace("roles.properties: Using classloader");

            is = SecurityActions.getResourceAsStream(DEFAULT_ROLES_PROPERTIES);
         }

         if (is != null)
         {
            Properties p = new Properties();
            p.load(is);
            
            Set<?> keys = p.keySet();
            if (keys != null && keys.size() > 0)
            {
               Iterator it = keys.iterator();
               while (it.hasNext())
               {
                  String user = (String)it.next();
                  String value = p.getProperty(user);

                  StringTokenizer st = new StringTokenizer(value, ",");
                  Set<String> s = new HashSet<String>(st.countTokens());
                  
                  while (st.hasMoreTokens())
                  {
                     s.add(st.nextToken().trim());
                  }

                  if (log.isDebugEnabled())
                     log.debug("Adding roles: " + s + " for user: " + user);

                  roles.put(user, s);
               }
            }
            else
            {
               if (log.isDebugEnabled())
                  log.debug("Empty roles.properties file");
            }
         }
         else
         {
            log.warn("No roles.properties were found");
         }
      }
      catch (IOException ioe)
      {
         log.error("Error while loading roles.properties", ioe);
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
    * Stop
    * @exception Throwable Thrown if an error occurs
    */
   public void stop() throws Throwable
   {
      users.clear();
      roles.clear();
   }
}
