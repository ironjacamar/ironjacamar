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

package org.jboss.jca.core.naming;

import org.jboss.jca.core.CoreBundle;
import org.jboss.jca.core.CoreLogger;
import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.util.Hashtable;
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

/**
 * An explicit JNDI strategy that requires a JNDI for each connection factory
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ExplicitJndiStrategy implements JndiStrategy
{
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class, ExplicitJndiStrategy.class.getName());

   /** The bundle */
   private static CoreBundle bundle = Messages.getBundle(CoreBundle.class);
   
   private static ConcurrentMap<String, Object> objs = new ConcurrentHashMap<String, Object>();

   /**
    * Constructor
    */
   public ExplicitJndiStrategy()
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
   public String[] bindConnectionFactories(String deployment, Object[] cfs) throws Throwable
   {
      throw new IllegalStateException("JNDI names are required");
   }

   /**
    * {@inheritDoc}
    */
   public String[] bindConnectionFactories(String deployment, Object[] cfs, String[] jndis) throws Throwable
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (deployment.trim().equals(""))
         throw new IllegalArgumentException("Deployment is empty");

      if (cfs == null)
         throw new IllegalArgumentException("CFS is null");

      if (cfs.length == 0)
         throw new IllegalArgumentException("CFS is empty");

      if (jndis == null)
         throw new IllegalArgumentException("JNDIs is null");

      if (jndis.length == 0)
         throw new IllegalArgumentException("JNDIs is empty");

      if (cfs.length != jndis.length)
         throw new IllegalArgumentException("Number of connection factories doesn't match number of JNDI names");

      Context context = new InitialContext();
      try
      {
         for (int i = 0; i < cfs.length; i++)
         {
            String jndiName = jndis[i];
            Object cf = cfs[i];

            if (log.isTraceEnabled())
               log.tracef("Binding %s under %s", cf.getClass().getName(), jndiName);

            if (cf == null)
               throw new IllegalArgumentException("Connection factory is null");

            if (jndiName == null)
               throw new IllegalArgumentException("JNDI name is null");

            String className = cf.getClass().getName();
            Reference ref = new Reference(className,
                                          new StringRefAddr("class", className),
                                          ExplicitJndiStrategy.class.getName(),
                                          null);
            ref.add(new StringRefAddr("name", jndiName));

            if (objs.putIfAbsent(qualifiedName(jndiName, className), cf) != null)
               throw new Exception(bundle.deploymentFailedSinceJndiNameHasDeployed(className, jndiName));

            Referenceable referenceable = (Referenceable)cf;
            referenceable.setReference(ref);
            
            Util.bind(context, jndiName, cf);

            if (log.isDebugEnabled())
               log.debug("Bound " + cf.getClass().getName() + " under " + jndiName);
         }
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

      return jndis;
   }

   /**
    * {@inheritDoc}
    */
   public void unbindConnectionFactories(String deployment, Object[] cfs) throws Throwable
   {
      throw new IllegalStateException("JNDI names are required");
   }

   /**
    * {@inheritDoc}
    */
   public void unbindConnectionFactories(String deployment, Object[] cfs, String[] jndis) throws Throwable
   {
      if (cfs == null)
         throw new IllegalArgumentException("CFS is null");

      if (cfs.length == 0)
         throw new IllegalArgumentException("CFS is empty");

      if (jndis == null)
         throw new IllegalArgumentException("JNDIs is null");

      if (jndis.length == 0)
         throw new IllegalArgumentException("JNDIs is empty");

      if (cfs.length != jndis.length)
         throw new IllegalArgumentException("Number of connection factories doesn't match number of JNDI names");

      Context context = null;
      try
      {
         context = new InitialContext();

         for (int i = 0; i < cfs.length; i++)
         {
            String jndiName = jndis[i];
            Object cf = cfs[i];

            if (cf == null)
               throw new IllegalArgumentException("Connection factory is null");

            if (jndiName == null)
               throw new IllegalArgumentException("JNDI name is null");

            String className = cf.getClass().getName();

            log.tracef("Unbinding %s under %s", className, jndiName);

            Util.unbind(context, jndiName);

            objs.remove(qualifiedName(jndiName, className));

            if (log.isDebugEnabled())
               log.debug("Unbound " + className + " under " + jndiName);
         }
      }
      catch (Throwable t)
      {
         log.exceptionDuringUnbind(t);
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
   public String[] bindAdminObjects(String deployment, Object[] aos) throws Throwable
   {
      throw new IllegalStateException("JNDI names are required");
   }

   /**
    * {@inheritDoc}
    */
   public String[] bindAdminObjects(String deployment, Object[] aos, String[] jndis) throws Throwable
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (deployment.trim().equals(""))
         throw new IllegalArgumentException("Deployment is empty");

      if (aos == null)
         throw new IllegalArgumentException("AOS is null");

      if (aos.length == 0)
         throw new IllegalArgumentException("AOS is empty");

      if (jndis == null)
         throw new IllegalArgumentException("JNDIs is null");

      if (jndis.length == 0)
         throw new IllegalArgumentException("JNDIs is empty");

      if (aos.length != jndis.length)
         throw new IllegalArgumentException("Number of admin objects doesn't match number of JNDI names");

      Context context = new InitialContext();
      try
      {
         for (int i = 0; i < aos.length; i++)
         {
            String jndiName = jndis[i];
            Object ao = aos[i];

            if (log.isTraceEnabled())
               log.tracef("Binding %s under %s", ao.getClass().getName(), jndiName);

            if (ao == null)
               throw new IllegalArgumentException("Admin object is null");

            if (jndiName == null)
               throw new IllegalArgumentException("JNDI name is null");

            if (ao instanceof Referenceable)
            {
               String className = ao.getClass().getName();
               Reference ref = new Reference(className,
                                             new StringRefAddr("class", className),
                                             ExplicitJndiStrategy.class.getName(),
                                             null);
               ref.add(new StringRefAddr("name", jndiName));

               if (objs.putIfAbsent(qualifiedName(jndiName, className), ao) != null)
                  throw new Exception(bundle.deploymentFailedSinceJndiNameHasDeployed(className, jndiName));

               Referenceable referenceable = (Referenceable)ao;
               referenceable.setReference(ref);
            }
            
            Util.bind(context, jndiName, ao);

            if (log.isDebugEnabled())
               log.debug("Bound " + ao.getClass().getName() + " under " + jndiName);
         }
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

      return jndis;
   }

   /**
    * {@inheritDoc}
    */
   public void unbindAdminObjects(String deployment, Object[] aos) throws Throwable
   {
      throw new IllegalStateException("JNDI names are required");
   }

   /**
    * {@inheritDoc}
    */
   public void unbindAdminObjects(String deployment, Object[] aos, String[] jndis) throws Throwable
   {
      if (aos == null)
         throw new IllegalArgumentException("AOS is null");

      if (aos.length == 0)
         throw new IllegalArgumentException("AOS is empty");

      if (jndis == null)
         throw new IllegalArgumentException("JNDIs is null");

      if (jndis.length == 0)
         throw new IllegalArgumentException("JNDIs is empty");

      if (aos.length != jndis.length)
         throw new IllegalArgumentException("Number of admin objects doesn't match number of JNDI names");

      Context context = null;
      try
      {
         context = new InitialContext();

         for (int i = 0; i < aos.length; i++)
         {
            String jndiName = jndis[i];
            Object ao = aos[i];

            if (ao == null)
               throw new IllegalArgumentException("Admin object is null");

            if (jndiName == null)
               throw new IllegalArgumentException("JNDI name is null");

            String className = ao.getClass().getName();

            log.tracef("Unbinding %s under %s", className, jndiName);

            Util.unbind(context, jndiName);

            objs.remove(qualifiedName(jndiName, className));

            if (log.isDebugEnabled())
               log.debug("Unbound " + className + " under " + jndiName);
         }
      }
      catch (Throwable t)
      {
         log.exceptionDuringUnbind(t);
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
    * Clone the JNDI strategy implementation
    * @return A copy of the implementation
    * @exception CloneNotSupportedException Thrown if the copy operation isn't supported
    *  
    */
   public JndiStrategy clone() throws CloneNotSupportedException
   {
      return (JndiStrategy)super.clone();
   }

   private static String qualifiedName(String name, String className)
   {
      return className + "#" + name;
   }
}
