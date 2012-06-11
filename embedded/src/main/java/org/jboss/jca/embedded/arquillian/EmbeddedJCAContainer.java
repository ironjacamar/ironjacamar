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
package org.jboss.jca.embedded.arquillian;

import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * Arquillian {@link DeployableContainer} adaptor for Embedded JCA
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class EmbeddedJCAContainer implements DeployableContainer<EmbeddedJCAContainerConfiguration>
{
   /** EmbeddedJCA */
   private Embedded embedded;

   /** Context */
   @Inject @ApplicationScoped
   private InstanceProducer<Context> contextProducer;

   /** Kernel */
   @Inject @ApplicationScoped
   private InstanceProducer<Embedded> embeddedProducer;

   /**
    * Constructor
    */
   public EmbeddedJCAContainer()
   {
      this.embedded = null;
   }

   /**
    * {@inheritDoc}
    * @see DeployableContainer#getDefaultProtocol()
    */
   @Override
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Local");
   }
   
   /**
    * {@inheritDoc}
    * @see DeployableContainer#getConfigurationClass()
    */
   @Override
   public Class<EmbeddedJCAContainerConfiguration> getConfigurationClass()
   {
      return EmbeddedJCAContainerConfiguration.class;
   }

   /**
    * {@inheritDoc}
    * @see DeployableContainer#deploy(Descriptor)
    */
   @Override
   public void deploy(Descriptor descriptor) throws DeploymentException 
   {
      if (descriptor == null)
         throw new DeploymentException("Could not deploy a null descriptor");

      try
      {
         embedded.deploy(descriptor);
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Could not undeploy the application: " + t.getMessage(), t);
      }      
   }
   
   /**
    * {@inheritDoc}
    * @see DeployableContainer#undeploy(Descriptor)
    */
   @Override
   public void undeploy(Descriptor descriptor) throws DeploymentException 
   {
      if (descriptor == null)
         throw new DeploymentException("Could not undeploy a null application");

      try
      {
         embedded.undeploy(descriptor);
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Could not undeploy the application: " + t.getMessage(), t);
      }      
   }

   /**
    * {@inheritDoc}
    * @see DeployableContainer#deploy(Archive)
    */
   @Override
   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
         throw new DeploymentException("Could not deploy a null application");

      if (!(ResourceAdapterArchive.class.isInstance(archive)))
         throw new DeploymentException("Could not deploy a non resource adapter application");

      try
      {
         embedded.deploy(ResourceAdapterArchive.class.cast(archive));
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Could not deploy the application: " + t.getMessage(), t);
      }

      // Invoke locally
      return new ProtocolMetaData();
   }

   /**
    * {@inheritDoc}
    * @see DeployableContainer#undeploy(Archive)
    */
   @Override
   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
         throw new DeploymentException("Could not undeploy a null application");

      if (!(ResourceAdapterArchive.class.isInstance(archive)))
         throw new DeploymentException("Could not undeploy a non resource adapter application");

      try
      {
         embedded.undeploy(ResourceAdapterArchive.class.cast(archive));
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Could not undeploy the application: " + t.getMessage(), t);
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#start
    */
   @Override
   public void setup(final EmbeddedJCAContainerConfiguration configuration)
   {
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#start
    */
   @Override
   public void start() throws LifecycleException
   {
      embedded = EmbeddedFactory.create(true);
      try
      {
         embedded.startup();

         contextProducer.set(createContext());
         embeddedProducer.set(embedded);
      }
      catch (Throwable t)
      {
         throw new LifecycleException("Could not start the EmbeddedJCA container: " + t.getMessage(), t);
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#stop
    */
   @Override
   public void stop() throws LifecycleException
   {
      try
      {
         embedded.shutdown();
      }
      catch (Throwable t)
      {
         throw new LifecycleException("Could not stop the EmbeddedJCA container: " + t.getMessage(), t);
      }
      embedded = null;
   }

   /**
    * Create a context
    * @return The context
    * @exception Exception Thrown if an error occurs
    */
   protected InitialContext createContext() throws Exception
   {
      Properties properties = new Properties();
      properties.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      properties.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      return new InitialContext(properties);
   }
}
