/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.web.console;

/**
 * A simple tuple of a MBean operation name,
 * index, signature, args and operation result.
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class OpResultInfo
{
   /** Operation name */
   private String name;

   /** Operation signature */
   private String[] signature;

   /** Arguments */
   private String[] args;

   /** The result */
   private Object result;

   /**
    * Constructor
    */
   public OpResultInfo()
   {
   }

   /**
    * Constructor
    * @param name The name
    * @param signature The signature
    * @param args The arguments
    * @param result The result
    */
   public OpResultInfo(String name, String[] signature, String[] args, Object result)
   {
      this.name      = name;
      this.signature = signature;
      this.args      = args;
      this.result    = result;
   }

   /**
    * Get the name
    * @return The value
    */
   public String getName()
   {
      return name;
   }

   /**
    * Get the signature
    * @return The value
    */
   public String[] getSignature()
   {
      return signature;
   }

   /**
    * Get the arguments
    * @return The value
    */
   public String[] getArguments()
   {
      return args;
   }

   /**
    * Get the result
    * @return The value
    */
   public Object getResult()
   {
      return result;
   }
}
