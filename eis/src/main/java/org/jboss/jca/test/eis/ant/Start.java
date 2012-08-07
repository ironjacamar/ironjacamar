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
package org.jboss.jca.test.eis.ant;

import org.jboss.jca.test.eis.impl.EISServer;

import org.apache.tools.ant.BuildException;

/**
 * Start task
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Start extends AbstractTask
{
   /** The handler */
   private String handler;

   /** Classpath ref */
   private String classpathRef;

   /** Classpath */
   private Classpath classpath;

   /**
    * Constructor
    */
   public Start()
   {
      this.handler = null;
      this.classpathRef = null;
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
    * Set the classpath reference
    * @param v The value
    */
   public void setClasspathref(String v)
   {
      classpathRef = v;
   }

   /**
    * Create the classpath
    * @return The value
    */
   public Classpath createClasspath()
   {
      classpath = new Classpath(getProject());
      return classpath;
   }

   /**
    * {@inheritDoc}
    */
   public void execute() throws BuildException
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      try
      {
         ClassLoader cl = null;
         if (classpathRef != null || classpath != null)
         {
            org.apache.tools.ant.types.Path p =
               new org.apache.tools.ant.types.Path(getProject());

            if (classpathRef != null)
            {
               org.apache.tools.ant.types.Reference reference =
                  new org.apache.tools.ant.types.Reference(getProject(), classpathRef);
               p.setRefid(reference);
            }

            if (classpath != null)
            {
               p.append(classpath);
            }

            cl = getProject().createClassLoader(Start.class.getClassLoader(), p);
            Thread.currentThread().setContextClassLoader(cl);
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
         t.printStackTrace(System.err);

         throw new BuildException("Error during start: " + t.getMessage(), t);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * Classpath
    */
   public class Classpath extends org.apache.tools.ant.types.Path
   {
      /**
       * Constructor
       * @param p The project
       */
      public Classpath(org.apache.tools.ant.Project p)
      {
         super(p);
      }
   }
}
