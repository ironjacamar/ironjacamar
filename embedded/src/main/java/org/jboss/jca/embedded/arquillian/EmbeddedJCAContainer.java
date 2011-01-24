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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.arquillian.protocol.local.LocalMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
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
   private Embedded embedded;

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
         ResourceAdapterArchive raa = ResourceAdapterArchive.class.cast(archive);
         if (raa.getName() != null && raa.getName().startsWith("complex_"))
         {
            if (raa.get(raa.getName().substring(8)) != null)
            {
               Asset asset = raa.get(raa.getName().substring(8)).getAsset();
               String name = raa.getName().substring(raa.getName().indexOf("complex_"));
               deployinternalAsset(asset, name);

            }
            if (raa.get("resource-adapters-ra.xml") != null)
            {

               Asset asset = raa.get("resource-adapters-ra.xml").getAsset();
               String name = "resource-adapters-ra.xml";
               deployinternalAsset(asset, name);
            }
            if (raa.get("datasources-ds.xml") != null)
            {

               Asset asset = raa.get("datasources-ds.xml").getAsset();
               String name = "datasources-ds.xml";
               deployinternalAsset(asset, name);
            }
            if (raa.get("datasources-xa-ds.xml") != null)
            {

               Asset asset = raa.get("datasources-xa-ds.xml").getAsset();
               String name = "datasources-xa-ds.xml";
               deployinternalAsset(asset, name);
            }

         }
         else
         {
            embedded.deploy(ResourceAdapterArchive.class.cast(archive));
         }

      }
      catch (Throwable t)
      {
         t.printStackTrace();
         throw new DeploymentException("Could not deploy the application: " + t.getMessage());
      }

      // Invoke locally
      return new LocalMethodExecutor();
   }

   /**
    * deploy an internal asset
    *
    * @param asset asset
    * @param name asset
    *
    * @throws IOException IOException
    * @throws FileNotFoundException FileNotFoundException
    * @throws MalformedURLException MalformedURLException
    * @throws Throwable Throwable
    *
    */
   protected void deployinternalAsset(Asset asset, String name) throws IOException, FileNotFoundException, Throwable,
      MalformedURLException
   {
      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      File raaFile = new File(parentDirectory, name);

      if (raaFile.exists())
         recursiveDelete(raaFile);
      InputStream is = null;
      FileOutputStream os = null;

      byte[] buffer = new byte[4096];
      int read = 0;
      try
      {
         is = asset.openStream();
         os = new FileOutputStream(raaFile);
         while ((read = is.read(buffer)) != -1)
         {
            os.write(buffer, 0, read);
         }

         os.flush();
      }
      finally
      {
         if (os != null)
         {
            try
            {
               os.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }

         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
               // Ignore
            }
         }
      }
      embedded.deploy(raaFile.toURI().toURL());

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
         //throw new DeploymentException("Could not undeploy the application", e);
      }
      catch (Throwable t)
      {
         //throw new DeploymentException("Could not undeploy the application: " + t.getMessage());
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
      embedded = EmbeddedFactory.create();
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

   /**
    * Recursive delete
    * @param f The file handler
    * @exception IOException Thrown if a file could not be deleted
    */
   private void recursiveDelete(File f) throws IOException
   {
      if (f != null && f.exists())
      {
         File[] files = f.listFiles();
         if (files != null)
         {
            for (int i = 0; i < files.length; i++)
            {
               if (files[i].isDirectory())
               {
                  recursiveDelete(files[i]);
               }
               else
               {
                  if (!files[i].delete())
                     throw new IOException("Could not delete " + files[i]);
               }
            }
         }
         if (!f.delete())
            throw new IOException("Could not delete " + f);
      }
   }

   /**
    *
    * A SecurityActions.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public static class SecurityActions
   {
      /**
       * Constructor
       */
      private SecurityActions()
      {
      }

      /**
       * Get a system property
       * @param name The property name
       * @return The property value
       */
      @SuppressWarnings("unchecked")
      static String getSystemProperty(final String name)
      {
         return (String) AccessController.doPrivileged(new PrivilegedAction()
         {
            public Object run()
            {
               return System.getProperty(name);
            }
         });
      }

      /**
       * Set a system property
       * @param name The property name
       * @param value The property value
       */
      static void setSystemProperty(final String name, final String value)
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               System.setProperty(name, value);
               return null;
            }
         });
      }
   }
}
