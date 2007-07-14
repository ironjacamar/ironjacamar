/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.rars.generic.mcf.jdbc.local;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ResourceAllocationException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.rars.generic.cf.GenericConnectionFactory;
import org.jboss.rars.generic.cri.UserPasswordConnectionRequestInfo;
import org.jboss.rars.generic.mcf.GenericManagedConnection;
import org.jboss.rars.generic.mcf.jdbc.JDBCManagedConnectionFactory;
import org.jboss.util.NestedRuntimeException;

/**
 * LocalJDBCManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class LocalJDBCManagedConnectionFactory extends JDBCManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 29477823548495724L;

   /** The log */
   private static final Logger log = Logger.getLogger(LocalJDBCManagedConnectionFactory.class);
   
   /** The user */
   protected String user;
   
   /** The password */
   protected String password;
   
   /** The connection url */
   protected String connectionURL;

   /** The driver class */
   protected String driverClassName;

   /** The connection properties as a string */
   protected String connectionProperties;
   
   /** The connection properties */
   protected Properties connectionProps = new Properties();
   
   /**
    * Get the user.
    * 
    * @return the user.
    */
   public String getUser()
   {
      return user;
   }

   /**
    * Set the user.
    * 
    * @param user the user.
    */
   public void setUser(String user)
   {
      this.user = user;
   }

   /**
    * Get the password.
    * 
    * @return the password.
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * Set the password.
    * 
    * @param password the password.
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * Get the connectionURL.
    * 
    * @return the connectionURL.
    */
   public String getConnectionURL()
   {
      return connectionURL;
   }

   /**
    * Set the connectionURL.
    * 
    * @param connectionURL the connectionURL.
    */
   public void setConnectionURL(String connectionURL)
   {
      this.connectionURL = connectionURL;
   }

   /**
    * Get the driverClassName.
    * 
    * @return the driverClassName.
    */
   public String getDriverClassName()
   {
      return driverClassName;
   }

   /**
    * Set the driverClassName.
    * 
    * @param driverClassName the driverClassName.
    */
   public void setDriverClassName(String driverClassName)
   {
      this.driverClassName = driverClassName;
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

         InputStream is = new ByteArrayInputStream(connectionProperties.getBytes());
         try
         {
            connectionProps.load(is);
         }
         catch (IOException ioe)
         {
            throw new NestedRuntimeException("Could not load connection properties", ioe);
         }
      }
   }

   public ConnectionRequestInfo getConnectionRequestInfo(GenericConnectionFactory cf, Method method, Object[] args) throws ResourceException
   {
      if (args == null || args.length != 2)
         return UserPasswordConnectionRequestInfo.NONE;
      else
      {
         String user = (String) args[0];
         String password = (String) args[1];
         return new UserPasswordConnectionRequestInfo(user, password);
      }
   }

   /**
    * Get the connection properties
    * 
    * @param subject the subject
    * @param cxRequestInfo the connection request info
    * @return the properties
    * @throws ResourceException for any error
    */
   protected Object getRequestID(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      Properties props = new Properties();
      props.putAll(connectionProps);
      if (subject != null)
      {
         if (SubjectActions.addMatchingProperties(subject, props, this) == true)
            return props;
         throw new ResourceAdapterInternalException("No matching credentials in Subject!");
      }

      if (cxRequestInfo != null && cxRequestInfo instanceof UserPasswordConnectionRequestInfo == false)
         throw new ResourceAdapterInternalException("Wrong kind of ConnectionRequestInfo: " + cxRequestInfo.getClass());
      
      UserPasswordConnectionRequestInfo cri = (UserPasswordConnectionRequestInfo) cxRequestInfo;
      if (cri != null)
      {
         if (cri.getUser() != null)
         {
            props.setProperty("user", cri.getUser());
            props.setProperty("password", (cri.getPassword() == null)? "": cri.getPassword());
            return props;
         }
      }
      
      if (user != null)
      {
         props.setProperty("user", user);
         props.setProperty("password", (password == null) ? "" : password);
      }
      
      return props;
   }
   
   protected Object createRealConnection(GenericManagedConnection mc, Subject subject, ConnectionRequestInfo cxRequestInfo, Object requestID) throws ResourceException
   {
      Properties props = (Properties) requestID;

      // Some friendly drivers (Oracle, you guessed right) modify the props you supply.
      // Since we use our copy to identify compatibility in matchManagedConnection, we need
      // a pristine copy for our own use.  So give the friendly driver a copy.
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         // Make yet another copy to mask the password
         Properties logCopy = props;
         if (props.getProperty("password") != null)
         {
            logCopy = (Properties) props.clone();
            logCopy.setProperty("password", "--hidden--");
         }
         log.trace("Using properties: " + logCopy);
      }

      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class.forName(driverClassName, true, cl);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceAdapterInternalException("Unable to load driver class " + driverClassName, e);
      }
      
      try
      {
         return DriverManager.getConnection(connectionURL, props);
      }
      catch (SQLException e)
      {
         throw new ResourceAllocationException("Unable to get connection url=" + connectionURL + " user=" + user, e);
      }
   }

   protected void destroyRealConnection(GenericManagedConnection mc) throws ResourceException
   {
      Connection connection = (Connection) mc.getRealConnection();
      try
      {
         connection.close();
      }
      catch (SQLException e)
      {
         throw new ResourceAdapterInternalException("Error closing the connection", e);
      }
   }

   static class SubjectActions implements PrivilegedAction
   {
      Subject subject;

      Properties props;

      ManagedConnectionFactory mcf;

      SubjectActions(Subject subject, Properties props, ManagedConnectionFactory mcf)
      {
         this.subject = subject;
         this.props = props;
         this.mcf = mcf;
      }

      public Object run()
      {
         Iterator i = subject.getPrivateCredentials().iterator();
         while (i.hasNext())
         {
            Object o = i.next();
            if (o instanceof PasswordCredential)
            {
               PasswordCredential cred = (PasswordCredential) o;
               if (cred.getManagedConnectionFactory().equals(mcf))
               {
                  props.setProperty("user", (cred.getUserName() == null) ? "" : cred.getUserName());
                  if( cred.getPassword() != null )
                     props.setProperty("password", new String(cred.getPassword()));
                  return Boolean.TRUE;
               }
            }
         }
         return Boolean.FALSE;
      }

      static boolean addMatchingProperties(Subject subject, Properties props, ManagedConnectionFactory mcf)
      {
         SubjectActions action = new SubjectActions(subject, props, mcf);
         Boolean matched = (Boolean) AccessController.doPrivileged(action);
         return matched.booleanValue();
      }
   }
}
