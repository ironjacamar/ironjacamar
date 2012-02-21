/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.web;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingEnumeration;

import org.jboss.logging.Logger;

import com.github.fungal.api.util.JMX;

/**
 * A JNDI viewer bean
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class JNDIViewer
{
   private static Logger log = Logger.getLogger(JNDIViewer.class);
   private static boolean trace = log.isTraceEnabled();
   private static final String NEW_LINE = SecurityActions.getSystemProperty("line.separator");

   /** The MBean server */
   private MBeanServer mbeanServer;

   /** The domain */
   private String domain;

   /** The ObjectName */
   private ObjectName objectName;

   /**
    * Constructor
    */
   public JNDIViewer()
   {
      this.mbeanServer = null;
      this.domain = "";
      this.objectName = null;
   }

   /**
    * Set the MBeanServer
    * @param v The value
    */
   public void setMBeanServer(MBeanServer v)
   {
      this.mbeanServer = v;
   }

   /**
    * Set the domain
    * @param v The value
    */
   public void setDomain(String v)
   {
      this.domain = v;
   }

   /**
    * List
    * @return The JNDI view
    */
   public String list()
   {
      StringBuilder sb = new StringBuilder();

      Context c = null;
      try
      {
         c = new InitialContext();
         Context context = (Context)c.lookup("java:");
         sb.append("java:").append(NEW_LINE);
         list(context, " ", sb);
      }
      catch (Throwable t)
      {
         log.debug(t.getMessage(), t);
      }
      finally
      {
         if (c != null)
         {
            try
            {
               c.close();
            }
            catch (Throwable ignore)
            {
               // Ignore
            }
         }
      }

      return sb.toString();
   }

   /**
    * List
    * @param ctx The context
    * @param indent The indentation
    * @param buffer The buffer
    * @exception Throwable Thrown if an error occurs
    */
   private void list(Context ctx, String indent, StringBuilder buffer) throws Throwable
   {
      NamingEnumeration<Binding> bindings = ctx.listBindings("");
      while (bindings.hasMore())
      {
         Binding binding = bindings.next();

         String name = binding.getName();
         String className = binding.getClassName();
         boolean recursive = Context.class.isAssignableFrom(binding.getObject().getClass());
         boolean isLinkRef = LinkRef.class.isAssignableFrom(binding.getObject().getClass());

         buffer.append(indent + " +- " + name);
         
         if (isLinkRef)
         {
            try
            {
               Object obj = ctx.lookupLink(name);

               LinkRef link = (LinkRef) obj;
               buffer.append("[link -> ");
               buffer.append(link.getLinkName());
               buffer.append(']');
            }
            catch (Throwable t)
            {
               log.debug("Invalid LinkRef for: " + name, t);
               buffer.append("invalid]");
            }
         }

         buffer.append(" (class: " + className + ")");

         buffer.append(NEW_LINE);
         
         if (recursive)
         {
            try
            {
               Object value = ctx.lookup(name);
               if (value instanceof Context)
               {
                  Context subctx = (Context) value;
                  list(subctx, indent + " |  ", buffer);
               }
               else
               {
                  buffer.append(indent + " |   NonContext: " + value);
                  buffer.append(NEW_LINE);
               }
            }
            catch (Throwable t)
            {
               buffer.append("Failed to lookup: " + name + ", errmsg=" + t.getMessage());
               buffer.append(NEW_LINE);
            }
         }
      }

      bindings.close();
   }

   /**
    * Start
    * @exception Throwable If an error occurs
    */
   public void start() throws Throwable
   {
      if (mbeanServer != null)
      {
         DynamicMBean dmb = JMX.createMBean(this, "JNDIViewer");
         objectName = new ObjectName(domain + ":name=JNDIViewer");

         mbeanServer.registerMBean(dmb, objectName);
      }
   }

   /**
    * Stop
    * @exception Throwable If an error occurs
    */
   public void stop() throws Throwable
   {
      if (mbeanServer != null && objectName != null)
      {
         mbeanServer.unregisterMBean(objectName);
      }
   }
}
