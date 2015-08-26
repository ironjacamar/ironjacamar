/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.workmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/**
 * Work object input stream
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class WorkObjectInputStream extends ObjectInputStream
{
   /** The classloader to use */
   private WorkClassLoader wcl;

   /**
    * Constructor
    * @param is The input stream
    * @exception StreamCorruptedException - if the stream header is incorrect
    * @exception IOException - if an I/O error occurs while reading stream header
    */
   public WorkObjectInputStream(InputStream is) throws StreamCorruptedException, IOException
   {
      this(is, null);
   }

   /**
    * Constructor
    * @param is The input stream
    * @param wcl The work class loader
    * @exception StreamCorruptedException - if the stream header is incorrect
    * @exception IOException - if an I/O error occurs while reading stream header
    */
   public WorkObjectInputStream(InputStream is, WorkClassLoader wcl) throws StreamCorruptedException, IOException
   {
      super(is);
      this.wcl = wcl;
   }

   /**
    * Set the work class loader
    * @param v The value
    */
   public void setWorkClassLoader(WorkClassLoader v)
   {
      wcl = v;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException
   {
      if (wcl != null)
      {
         try
         {
            return wcl.loadClass(desc.getName());
         }
         catch (Throwable t)
         {
            // Fallback to super
         }
      }
      return super.resolveClass(desc);
   }
}
