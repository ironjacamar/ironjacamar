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

import org.jboss.jca.core.spi.naming.JndiStrategy;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;

/**
 * A noop JNDI strategy
 * 
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class NoopJndiStrategy implements JndiStrategy
{
   /**
    * Constructor
    */
   public NoopJndiStrategy()
   {
   }

   /**
    * Obtain the connection factory
    * {@inheritDoc}
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws Exception
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public String[] bindConnectionFactories(String deployment, Object[] cfs) throws Throwable
   {
      return new String[0];
   }

   /**
    * {@inheritDoc}
    */
   public String[] bindConnectionFactories(String deployment, Object[] cfs, String[] jndis) throws Throwable
   {
      return new String[0];
   }

   /**
    * {@inheritDoc}
    */
   public void unbindConnectionFactories(String deployment, Object[] cfs) throws Throwable
   {
   }

   /**
    * {@inheritDoc}
    */
   public void unbindConnectionFactories(String deployment, Object[] cfs, String[] jndis) throws Throwable
   {
   }

   /**
    * {@inheritDoc}
    */
   public String[] bindAdminObjects(String deployment, Object[] aos) throws Throwable
   {
      return new String[0];
   }

   /**
    * {@inheritDoc}
    */
   public String[] bindAdminObjects(String deployment, Object[] aos, String[] jndis) throws Throwable
   {
      return new String[0];
   }

   /**
    * {@inheritDoc}
    */
   public void unbindAdminObjects(String deployment, Object[] aos) throws Throwable
   {
   }

   /**
    * {@inheritDoc}
    */
   public void unbindAdminObjects(String deployment, Object[] aos, String[] jndis) throws Throwable
   {
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
}
