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

package org.jboss.jca.core.naming;

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
import org.jboss.util.naming.Util;

/**
 * A simple JNDI strategy that bind a single connection factory under the
 * name of "java:/eis/&lt;deployment&gt;"
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class SimpleJndiStrategy implements JndiStrategy
{
   private static Logger log = Logger.getLogger(SimpleJndiStrategy.class);

   /** JNDI prefix */
   private static final String JNDI_PREFIX = "java:/eis/";

   private static ConcurrentMap<String, Object> connectionFactories = new ConcurrentHashMap<String, Object>();

   /**
    * Constructor
    */
   public SimpleJndiStrategy()
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

      return connectionFactories.get(qualifiedName(cfname, className));
   }

   /**
    * Bind connection factories for a deployment
    * @param deployment The deployment name
    * @param cfs The connection factories
    * @return The JNDI names for the connection factories
    * @exception Throwable Thrown if an error occurs
    */
   public String[] bindConnectionFactories(String deployment, Object[] cfs) throws Throwable
   {
      if (deployment == null)
         throw new IllegalArgumentException("Deployment is null");

      if (deployment.trim().equals(""))
         throw new IllegalArgumentException("Deployment is empty");

      if (cfs == null)
         throw new IllegalArgumentException("CFS is null");

      if (cfs.length == 0)
         throw new IllegalArgumentException("CFS is empty");

      if (cfs.length > 1)
         throw new IllegalArgumentException("SimpleJndiStrategy only support " + 
                                            "a single connection factory per deployment");

      String jndiName = JNDI_PREFIX + deployment;

      Object cf = cfs[0];

      Context context = new InitialContext();
      try
      {
         String className = cf.getClass().getName();
         Reference ref = new Reference(className,
                                       new StringRefAddr("class", className),
                                       SimpleJndiStrategy.class.getName(),
                                       null);
         ref.add(new StringRefAddr("name", jndiName));

         if (connectionFactories.putIfAbsent(qualifiedName(jndiName, className), cf) != null)
            throw new Exception("Deployment " + className + " failed, " + jndiName + " is already deployed");

         Referenceable referenceable = (Referenceable)cf;
         referenceable.setReference(ref);

         Util.bind(context, jndiName, cf);
      }
      finally
      {
         context.close();
      }

      return new String[] {jndiName};
   }

   /**
    * Unbind connection factories for a deployment
    * @param deployment The deployment name
    * @param jndiNames The JNDI names for the connection factories
    * @exception Throwable Thrown if an error occurs
    */
   public void unbindConnectionFactories(String deployment, String[] jndiNames) throws Throwable
   {
      if (jndiNames == null)
         throw new IllegalArgumentException("JndiNames is null");

      Context context = null;
      try
      {
         context = new InitialContext();

         for (String jndiName : jndiNames)
         {
            connectionFactories.remove(jndiName);

            try
            {
               Util.unbind(context, jndiName);
            }
            catch (Throwable it)
            {
               log.warn("Exception during JNDI unbind for: " + jndiName, it);
            }
         }
      }
      catch (Throwable t)
      {
         log.warn("Exception during JNDI initialization", t);
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
