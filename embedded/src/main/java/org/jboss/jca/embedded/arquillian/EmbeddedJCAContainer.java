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

import org.jboss.jca.embedded.EmbeddedJCA;

import org.jboss.arquillian.protocol.local.LocalMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

/**
 * Arquillian {@link DeployableContainer} adaptor for EmbeddedJCA
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class EmbeddedJCAContainer implements DeployableContainer
{
   /** EmbeddedJCA */
   private EmbeddedJCA embedded;

   /**
    * Constructor
    */
   public EmbeddedJCAContainer()
   {
      this.embedded = null;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#deploy
    */
   @Override
   public ContainerMethodExecutor deploy(final Context context, final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
         throw new DeploymentException("Could not deploy a null application");

      if (!(ResourceAdapterArchive.class.isInstance(archive)))
         throw new DeploymentException("Could not deploy a non resource adapter application");

      try
      {
         embedded.deploy(ResourceAdapterArchive.class.cast(archive));
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not deploy the application", e);
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Could not deploy the application: " + t.getMessage());
      }

      // Invoke locally
      return new LocalMethodExecutor();
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#undeploy
    */
   @Override
   public void undeploy(final Context context, final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
         throw new DeploymentException("Could not undeploy a null application");

      if (!(ResourceAdapterArchive.class.isInstance(archive)))
         throw new DeploymentException("Could not undeploy a non resource adapter application");

      try
      {
         embedded.undeploy(ResourceAdapterArchive.class.cast(archive));
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not undeploy the application", e);
      }
      catch (Throwable t)
      {
         throw new DeploymentException("Could not undeploy the application: " + t.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#start
    */
   @Override
   public void setup(final Context context, final Configuration configuration)
   {

   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#start
    */
   @Override
   public void start(final Context context) throws LifecycleException
   {
      embedded = new EmbeddedJCA();
      try
      {
         embedded.startup();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not start the EmbeddedJCA container", e);
      }
      catch (Throwable t)
      {
         throw new LifecycleException("Could not start the EmbeddedJCA container: " + t.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.DeployableContainer#stop
    */
   @Override
   public void stop(final Context context) throws LifecycleException
   {
      try
      {
         embedded.shutdown();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not stop the EmbeddedJCA container", e);
      }
      catch (Throwable t)
      {
         throw new LifecycleException("Could not stop the EmbeddedJCA container: " + t.getMessage());
      }
      embedded = null;
   }
}
