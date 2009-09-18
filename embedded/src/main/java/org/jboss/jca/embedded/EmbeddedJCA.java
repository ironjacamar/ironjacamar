/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.embedded;

import org.jboss.jca.fungal.impl.KernelConfiguration;
import org.jboss.jca.fungal.impl.KernelImpl;

import java.net.URL;

/**
 * The embedded JBoss JCA container
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class EmbeddedJCA
{
   /** Enable full profile */
   private boolean fullProfile;

   /** Kernel */
   private KernelImpl kernel;

   /**
    * Constructs an embedded JCA environment using
    * the full JCA 1.6 profile
    */
   public EmbeddedJCA()
   {
      this(true);
   }

   /**
    * Constructs an embedded JCA environment. If <code>fullProfile</code>
    * is <code>true</code> then a full JCA 1.6 container is initialized -
    * otherwise only the basic kernel is initialized and services has
    * to be added as deployments
    * @param fullProfile Should a full profile be initialized
    */
   public EmbeddedJCA(boolean fullProfile)
   {
      this.fullProfile = fullProfile;
   }

   /**
    * Startup
    * @exception Throwable If an error occurs
    */
   public void startup() throws Throwable
   {
      KernelConfiguration kernelConfiguration = new KernelConfiguration();
      kernelConfiguration = kernelConfiguration.remoteAccess(false);

      kernel = new KernelImpl(kernelConfiguration);
      kernel.startup();

      if (fullProfile)
      {
         deploy(EmbeddedJCA.class.getClassLoader(), "naming.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "transaction.xml");
         deploy(EmbeddedJCA.class.getClassLoader(), "jca.xml");
      }
   }

   /**
    * Shutdown
    * @exception Throwable If an error occurs
    */
   public void shutdown() throws Throwable
   {
      if (fullProfile)
      {
         undeploy(EmbeddedJCA.class.getClassLoader(), "jca.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "transaction.xml");
         undeploy(EmbeddedJCA.class.getClassLoader(), "naming.xml");
      }

      kernel.shutdown();
   }

   /**
    * Lookup a bean
    * @param name The bean name
    * @param expectedType The expected type for the bean
    * @return The bean instance
    * @exception Throwable If an error occurs
    */
   public <T> T lookup(String name, Class<T> expectedType) throws Throwable
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");

      if (expectedType == null)
         throw new IllegalArgumentException("ExpectedType is null");

      return expectedType.cast(kernel.getBean(name));
   }

   /**
    * Deploy
    * @param url The resource url
    * @exception Throwable If an error occurs
    */
   public void deploy(URL url) throws Throwable
   {
      if (url == null)
         throw new IllegalArgumentException("Url is null");      

      kernel.getMainDeployer().deploy(url);
   }

   /**
    * Deploy
    * @param cl The class loader
    * @param name The resource name
    * @exception Throwable If an error occurs
    */
   public void deploy(ClassLoader cl, String name) throws Throwable
   {
      if (cl == null)
         throw new IllegalArgumentException("ClassLoader is null");

      if (name == null)
         throw new IllegalArgumentException("Name is null");

      URL url = cl.getResource(name);
      kernel.getMainDeployer().deploy(url);
   }

   /**
    * Deploy
    * @param clz The class
    * @exception Throwable If an error occurs
    */
   public void deploy(Class<?> clz) throws Throwable
   {
      if (clz == null)
         throw new IllegalArgumentException("Clz is null");      

      String name = clz.getName().replace('.', '/');
      name += "-jboss-beans.xml";

      URL url = clz.getClassLoader().getResource(name);
      kernel.getMainDeployer().deploy(url);
   }

   /**
    * Undeploy
    * @param url The resource url
    * @exception Throwable If an error occurs
    */
   public void undeploy(URL url) throws Throwable
   {
      if (url == null)
         throw new IllegalArgumentException("Url is null");      

      kernel.getMainDeployer().undeploy(url);
   }

   /**
    * Undeploy
    * @param cl The class loader
    * @param name The resource name
    * @exception Throwable If an error occurs
    */
   public void undeploy(ClassLoader cl, String name) throws Throwable
   {
      if (cl == null)
         throw new IllegalArgumentException("ClassLoader is null");

      if (name == null)
         throw new IllegalArgumentException("Name is null");

      URL url = cl.getResource(name);
      kernel.getMainDeployer().undeploy(url);
   }

   /**
    * Undeploy
    * @param clz The class
    * @exception Throwable If an error occurs
    */
   public void undeploy(Class<?> clz) throws Throwable
   {
      if (clz == null)
         throw new IllegalArgumentException("Clz is null");      

      String name = clz.getName().replace('.', '/');
      name += "-jboss-beans.xml";

      URL url = clz.getClassLoader().getResource(name);
      kernel.getMainDeployer().undeploy(url);
   }
}
