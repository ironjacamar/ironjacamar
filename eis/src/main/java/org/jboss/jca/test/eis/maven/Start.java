/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.eis.maven;

import org.jboss.jca.test.eis.impl.EISServer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Start mojo
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Start extends EISMojo
{
   /** The handler */
   private String handler;

   /** The classpath */
   private List<File> classpath;

   /**
    * Constructor
    */
   public Start()
   {
      this.handler = null;
      this.classpath = null;
   }

   /**
    * Set the handler
    * @param v The value
    */
   public void setHandler(String v)
   {
      this.handler = v;
   }

   /**
    * Set the classpath
    * @param v The value
    */
   public void setClasspath(List<File> v)
   {
      classpath = v;
   }

   /**
    * {@inheritDoc}
    */
   public void execute()  throws MojoExecutionException, MojoFailureException
   {
      ClassLoader oldCl = SecurityActions.getThreadContextClassLoader();
      try
      {
         ClassLoader cl = null;
         if (classpath != null)
         {
            List<URL> urls = new ArrayList<URL>(classpath.size());
            for (File f : classpath)
            {
               urls.add(f.toURI().toURL());
            }

            cl = SecurityActions.createURLCLassLoader(urls.toArray(new URL[urls.size()]), Start.class.getClassLoader());
            SecurityActions.setThreadContextClassLoader(cl);
         }

         EISServer eisServer = new EISServer();
         eisServer.setHost(getHost());
         eisServer.setPort(getPort());

         eisServer.setHandlerClassName(handler);
         eisServer.setClassLoader(cl);
         
         eisServer.startup();
      }
      catch (Throwable t)
      {
         throw new MojoExecutionException("Error during start: " + t.getMessage(), t);
      }
      finally
      {
         SecurityActions.setThreadContextClassLoader(oldCl);
      }
   }
}
