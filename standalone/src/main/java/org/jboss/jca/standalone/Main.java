/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.standalone;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The main class for JBoss JCA standalone
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Main
{
   /** The server */
   private Object server;

   /**
    * Default constructor
    */
   private Main()
   {
   }

   /**
    * Boot
    * @param args The arguments
    */
   private void boot(String[] args)
   {
      try
      {
         String home = SecurityActions.getSystemProperty("jboss.jca.home");
         File root = null;

         if (home != null)
         {
            root = new File(new URI(home));
         }
         else
         {
            home = new File(".").toURI().toURL().toString();
            root = new File(new URI(home.substring(0, home.lastIndexOf("bin"))));
         }

         if (args != null && args.length > 0)
         {
            for (int i = 0; i < args.length; i++)
            {
               if ("-b".equals(args[i]))
               {
                  SecurityActions.setSystemProperty("jboss.jca.bindaddress", args[++i]);
               }
            }
         }

         File libDirectory = new File(root, "/lib/");
         File configDirectory = new File(root, "/server/jca/conf/");
         URL deployDirectory = new File(root, "/server/jca/deploy/").toURI().toURL();

         ClassLoader parent = SecurityActions.getThreadContextClassLoader();

         URL[] libUrls = getUrls(libDirectory);
         URL[] confUrls = getUrls(configDirectory);

         URL[] urls = mergeUrls(libUrls, confUrls);

         URLClassLoader classLoader = new URLClassLoader(urls, parent);
         SecurityActions.setThreadContextClassLoader(classLoader);

         Class serverLoaderClass = Class.forName("org.jboss.bootstrap.ServerLoader", true, classLoader);
         Method serverLoaderMethodLoad = serverLoaderClass.getDeclaredMethod("load", ClassLoader.class); 

         Class serverClass = Class.forName("org.jboss.bootstrap.spi.Server", true, classLoader);
         Method serverMethodInit = serverClass.getDeclaredMethod("init", Properties.class, Map.class); 
         Method serverMethodStart = serverClass.getDeclaredMethod("start"); 

         Class serverConfigClass = Class.forName("org.jboss.bootstrap.spi.ServerConfig", true, classLoader);
         Field serverConfigFieldHomeUrl = serverConfigClass.getDeclaredField("HOME_URL");
         Field serverConfigFieldHomeDir = serverConfigClass.getDeclaredField("HOME_DIR");
         Field serverConfigFieldServerName = serverConfigClass.getDeclaredField("SERVER_NAME");

         SecurityActions.setSystemProperty("xb.builder.useUnorderedSequence", "true");
         SecurityActions.setSystemProperty("jboss.deploy.url", deployDirectory.toURI().toURL().toString());
         SecurityActions.setSystemProperty("jboss.lib.url", libDirectory.toURI().toURL().toString());
         SecurityActions.setSystemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

         Properties props = new Properties(SecurityActions.getSystemProperties());
         props.put((String)serverConfigFieldHomeUrl.get(null), root.toURI().toURL().toString());
         props.put((String)serverConfigFieldHomeDir.get(null), root.getAbsolutePath());
         props.put((String)serverConfigFieldServerName.get(null), "jca");

         props.put("jboss.deploy.url", deployDirectory.toURI().toURL().toString());
         props.put("jboss.lib.url", libDirectory.toURI().toURL().toString());

         String loggingManager = props.getProperty("java.util.logging.manager");
         if (loggingManager == null)
            props.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");

         Constructor serverLoaderConstructor = serverLoaderClass.getDeclaredConstructor(Properties.class); 
         Object serverLoader = serverLoaderConstructor.newInstance(props);

         server = serverLoaderMethodLoad.invoke(serverLoader, classLoader);
         serverMethodInit.invoke(server, props, null);

         serverMethodStart.invoke(server);
      }
      catch (Throwable t)
      {
         t.printStackTrace(System.err);
      }
   }

   /**
    * Shutdown
    */
   private void shutdown()
   {
      try
      {
         if (server != null)
         {
            Method serverMethodShutdown = server.getClass().getDeclaredMethod("shutdown"); 
            serverMethodShutdown.invoke(server);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }

   /**
    * Get the URLs for the directory and all libraries located in the directory
    * @param directrory The directory
    * @return The URLs
    * @exception MalformedURLException MalformedURLException
    * @exception IOException IOException
    */
   private URL[] getUrls(File directory) throws MalformedURLException, IOException
   {
      List<URL> list = new LinkedList<URL>();

      if (directory.exists() && directory.isDirectory())
      {
         // Add directory
         list.add(directory.toURI().toURL());

         // Add the contents of the directory too
         File[] jars = directory.listFiles(new JarFilter());

         for (int j = 0; jars != null && j < jars.length; j++)
         {
            list.add(jars[j].getCanonicalFile().toURI().toURL());
         }
      }
      return list.toArray(new URL[list.size()]);      
   }

   /**
    * Merge URLs into a single array
    * @param urls The URLs
    * @return The combined list
    */
   private URL[] mergeUrls(URL[]... urls)
   {
      List<URL> list = new LinkedList<URL>();

      for (URL[] all : urls)
      {
         for (URL url : all)
         {
            list.add(url);
         }
      }

      return list.toArray(new URL[list.size()]);      
   }

   /**
    * Main
    * @param args The arguments
    */
   public static void main(final String[] args)
   {
      try
      {
         Runnable worker = new Runnable() {
            public void run()
            {
               try
               {
                  Main main = new Main();
                  main.boot(args);
               }
               catch (Exception e)
               {
                  System.err.println("Failed to boot JBoss:");
                  e.printStackTrace();
               }
            }
         };
         
         ThreadGroup threads = new ThreadGroup("jboss");
         new Thread(threads, worker, "main").start();
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
      }
   }
}
