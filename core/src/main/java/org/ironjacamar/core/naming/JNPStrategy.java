/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.naming;

//import org.ironjacamar.core.CoreBundle;
import org.ironjacamar.core.CoreBundle;
import org.ironjacamar.core.CoreLogger;
import org.ironjacamar.core.spi.naming.JndiStrategy;

import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.resource.Referenceable;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;
//import org.jboss.logging.Messages;

/**
 * An JNP based JNDI strategy
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JNPStrategy implements JndiStrategy
{
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, JNPStrategy.class.getName());

   private static boolean trace = log.isTraceEnabled();
   
   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   private static ConcurrentMap<String, Object> objs = new ConcurrentHashMap<String, Object>();

   private int jndiPort;

   private String jndiProtocol;

   private String jndiHost;


   /**
    * Constructor
    *
    */
   public JNPStrategy()
   {
   }

   /**
    * Obtain the connection factory
    * {@inheritDoc}
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws Exception
   {
      Reference ref = (Reference)obj;
      String className = (String)ref.get("class").getContent();
      String cfname = (String)ref.get("name").getContent();

      return objs.get(qualifiedName(cfname, className));
   }

   /**
    * {@inheritDoc}
    */
   public void bind(String jndiName, Object o) throws NamingException
   {
      if (jndiName == null)
         throw new NamingException();

      if (o == null)
         throw new NamingException();

      Context context = createContext();
      try
      {
         String className = o.getClass().getName();

         if (trace)
            log.trace("Binding " + className + " under " + jndiName);

         Reference ref = new Reference(className,
                                       new StringRefAddr("class", className),
                                       JNPStrategy.class.getName(),
                                       null);
         ref.add(new StringRefAddr("name", jndiName));

         if (objs.putIfAbsent(qualifiedName(jndiName, className), o) != null)
         {
            throw new NamingException(bundle.deploymentFailedSinceJndiNameHasDeployed(className, jndiName));
         }

         if (o instanceof Referenceable)
         {
            Referenceable referenceable = (Referenceable)o;
            referenceable.setReference(ref);
         }
            
         Util.bind(context, jndiName, o);

         if (log.isDebugEnabled())
            log.debug("Bound " + className + " under " + jndiName);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void unbind(String jndiName, Object o) throws NamingException
   {
      if (jndiName == null)
         throw new NamingException();

      if (o == null)
         throw new NamingException();

      Context context = createContext();
      try
      {
         String className = o.getClass().getName();

         if (trace)
            log.trace("Unbinding " + className + " under " + jndiName);

         Util.unbind(context, jndiName);

         objs.remove(qualifiedName(jndiName, className));

         if (log.isDebugEnabled())
            log.debug("Unbound " + className + " under " + jndiName);
      }
      catch (Throwable t)
      {
         //log.exceptionDuringUnbind(t);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException ne)
            {
               // Ignore
            }
         }
      }
   }

   /**
    * Create a context
    * @return The context
    * @exception NamingException Thrown if an error occurs
    */
   protected Context createContext() throws NamingException
   {
      Properties properties = new Properties();
      properties.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      properties.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      properties.setProperty(Context.PROVIDER_URL, jndiProtocol + "://" + jndiHost + ":" + jndiPort);
      return new InitialContext(properties);
   }

   /**
    * get Jndi Port
    * @return the JndiPort
    */
   public int getJndiPort()
   {
      return jndiPort;
   }

   /**
    * get Jndi Protocol
    * @return the Jndi Protocol
    */
   public String getJndiProtocol()
   {
      return jndiProtocol;
   }

   /**
    * get Jndi Host
    * @return the Jndi Host
    */
   public String getJndiHost()
   {
      return jndiHost;
   }

   /**
    * set the jndi port
    * @param jndiPort the jndi port
    */
   public void setJndiPort(int jndiPort)
   {
      this.jndiPort = jndiPort;
   }

   /**
    * set the jndi protocol
    * @param jndiProtocol the jndi protocol
    */
   public void setJndiProtocol(String jndiProtocol)
   {
      this.jndiProtocol = jndiProtocol;
   }

   /**
    * set the jndi host
    * @param jndiHost the jndi host
    */
   public void setJndiHost(String jndiHost)
   {
      this.jndiHost = jndiHost;
   }

   private static String qualifiedName(String name, String className)
   {
      return className + "#" + name;
   }
}
