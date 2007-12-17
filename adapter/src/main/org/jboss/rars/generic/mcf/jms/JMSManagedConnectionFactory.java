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
package org.jboss.rars.generic.mcf.jms;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;

import org.jboss.rars.generic.cf.GenericConnectionFactory;
import org.jboss.rars.generic.mcf.GenericManagedConnection;
import org.jboss.rars.generic.mcf.indirect.IndirectGenericConnection;
import org.jboss.rars.generic.mcf.indirect.IndirectGenericManagedConnectionFactory;

/**
 * JMSManagedConnectionFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1.1.1 $
 */
public class JMSManagedConnectionFactory extends IndirectGenericManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 8122176068763448640L;
 
   /** The user */
   protected String user;
   
   /** The password */
   protected String password;
   
   /** The connection factory */
   protected ConnectionFactory connectionFactory;
   
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
    * Get the connectionFactory.
    * 
    * @return the connectionFactory.
    */
   public ConnectionFactory getConnectionFactory()
   {
      return connectionFactory;
   }

   /**
    * Set the connectionFactory.
    * 
    * @param connectionFactory the connectionFactory.
    */
   public void setConnectionFactory(ConnectionFactory connectionFactory)
   {
      this.connectionFactory = connectionFactory;
   }

   protected void addConnectionFactoryInterfaces(GenericConnectionFactory impl, Set<Class<?>> interfaces)
   {
      interfaces.add(ConnectionFactory.class);
      interfaces.add(QueueConnectionFactory.class);
      interfaces.add(TopicConnectionFactory.class);
      interfaces.add(Serializable.class);
   }

   protected void addIndirectConnectionInterfaces(IndirectGenericConnection connection, Set<Class<?>> interfaces)
   {
      interfaces.add(Connection.class);
      interfaces.add(QueueConnection.class);
      interfaces.add(TopicConnection.class);
   }

   public Throwable error(Object context, Throwable t)
   {
      if (t instanceof JMSException)
         return t;
      JMSException e;
      if (t instanceof javax.resource.spi.IllegalStateException)
         e = new IllegalStateException("Error during " + context);
      else
         e = new JMSException("Error during " + context);
      e.initCause(t);
      return e;
   }

   protected Object createRealConnection(GenericManagedConnection mc, Subject subject, ConnectionRequestInfo cxRequestInfo, Object requestID) throws ResourceException
   {
      Connection connection;
      try
      {
         // @todo user/password
         connection = connectionFactory.createConnection();
      }
      catch (JMSException e)
      {
         throw new ResourceAdapterInternalException("Unable to create connection ", e);
      }

      try
      {
         // @todo client id
         JMSRealConnection handle = new JMSRealConnection(mc, connection);
         JMSExceptionListener exceptionListener = new JMSExceptionListener(mc);
         connection.setExceptionListener(exceptionListener);
         connection.start();
         
         // @todo parameters and XASession
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         handle.setSession(session);
         return handle;
      }
      catch (Throwable t)
      {
         try
         {
            connection.close();
         }
         catch (Throwable ignored)
         {
         }
         throw new ResourceAdapterInternalException("Error initializing the connection", t);
      }
   }

   protected boolean ignoreChild(Method method, Object object, Class<?> type)
   {
      if (Message.class.isAssignableFrom(type))
         return true;
      return super.ignoreChild(method, object, type);
   }

   protected String getSingletonError()
   {
      return JMSRealConnection.DUPLICATE;
   }
   
   protected class JMSExceptionListener implements ExceptionListener
   {
      private GenericManagedConnection mc;
      
      /**
       * Create a new JMSExceptionListener.
       * 
       * @param mc the managed connection
       */
      public JMSExceptionListener(GenericManagedConnection mc)
      {
         this.mc = mc;
      }
      
      public void onException(JMSException exception)
      {
         mc.broadcastError(exception);
      }
   }
}
