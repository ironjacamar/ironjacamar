/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.jca.plugins.cm;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.aop.AspectManager;
import org.jboss.aop.proxy.container.AOPProxyFactory;
import org.jboss.aop.proxy.container.AOPProxyFactoryParameters;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
import org.jboss.logging.Logger;

/**
 * A POJO ConnectionManagerFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.5 $
 */
public class ConnectionManagerFactory
{
   /** The log */
   private static final Logger log = Logger.getLogger(ConnectionManagerFactory.class);
   
   /** The proxy factory */
   private static final AOPProxyFactory proxyFactory = new GeneratedAOPProxyFactory();
 
   /** The managed connection factory */
   protected ManagedConnectionFactory managedConnectionFactory;

   /** The aspect manager */
   protected AspectManager manager;

   /**
    * Get the manager.
    * 
    * @return the manager.
    */
   public AspectManager getManager()
   {
      return manager;
   }

   /**
    * Set the manager.
    * 
    * @param manager The manager to set.
    */
   public void setManager(AspectManager manager)
   {
      this.manager = manager;
   }

   /**
    * Get the managedConnectionFactory.
    * 
    * @return the managedConnectionFactory.
    */
   public ManagedConnectionFactory getManagedConnectionFactory()
   {
      return managedConnectionFactory;
   }

   /**
    * Set the managedConnectionFactory.
    * 
    * @param managedConnectionFactory the managedConnectionFactory.
    */
   public void setManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory)
   {
      this.managedConnectionFactory = managedConnectionFactory;
   }
   
   /**
    * Get the connection factory
    * 
    * @return the connection factory
    * @throws Exception for any error
    */
   public Object getConnectionFactory() throws Exception
   {
      if (managedConnectionFactory == null)
         throw new IllegalStateException("Null managed connection factory");
      if (manager == null)
         throw new IllegalStateException("Null manager");
      
      Class<?>[] interfaces = new Class<?>[] { ConnectionManager.class }; 
      
      AOPProxyFactoryParameters params = new AOPProxyFactoryParameters();
      params.setProxiedClass(Object.class);
      params.setInterfaces(interfaces);
      params.setTarget(new Object());
      
      ConnectionManager cmProxy = (ConnectionManager) proxyFactory.createAdvisedProxy(params);
      Object result = managedConnectionFactory.createConnectionFactory(cmProxy);
      log.debug("Created connectionFactory=" + result + " for MCF=" + managedConnectionFactory);
      return result;
   }
}
